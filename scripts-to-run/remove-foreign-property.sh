#!/bin/bash
# ===============================
# Remove Foreign Property - Entity Only
# ===============================
FEATURE=""
PROPERTY_NAME=""      # e.g. department_id
REFERENCE=""          # e.g. department
PARENT=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --feature)   FEATURE="$2"; shift 2 ;;
    --name)      PROPERTY_NAME="$2"; shift 2 ;;
    --reference) REFERENCE="$2"; shift 2 ;;
    --parent)    PARENT="$2"; shift 2 ;;
    *)
      echo "❌ Unknown parameter: $1"
      echo "Usage: ./remove-foreign-property.sh --feature position --name department_id --reference department [--parent administration]"
      exit 1 ;;
  esac
done

if [[ -z "$FEATURE" || -z "$PROPERTY_NAME" || -z "$REFERENCE" ]]; then
  echo "❌ Missing required parameters"
  exit 1
fi

# ====================== Naming Conventions ======================
FEATURE_LOWER=$(echo "$FEATURE" | tr '[:upper:]' '[:lower:]')
FEATURE_UPPER="$(tr '[:lower:]' '[:upper:]' <<< ${FEATURE_LOWER:0:1})${FEATURE_LOWER:1}"

REF_LOWER=$(echo "$REFERENCE" | tr '[:upper:]' '[:lower:]')
REF_UPPER="$(tr '[:lower:]' '[:upper:]' <<< ${REF_LOWER:0:1})${REF_LOWER:1}"

# Support for nested features (--parent)
if [ -n "$PARENT" ]; then
  PARENT_LOWER=$(echo "$PARENT" | tr '[:upper:]' '[:lower:]')
  BASE_DIR="src/main/java/com/bonnysimon/starter/features/$PARENT_LOWER/$FEATURE_LOWER"
else
  BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_LOWER"
fi

ENTITY_FILE="$BASE_DIR/${FEATURE_UPPER}Entity.java"

echo "🧹 Removing foreign relationship to '$REF_UPPER' from ${FEATURE_UPPER}Entity"

if [ ! -f "$ENTITY_FILE" ]; then
  echo "❌ Entity file not found: $ENTITY_FILE"
  exit 1
fi

# ====================== Remove FK from Entity ======================
echo " → Processing Entity..."

# Robust removal using awk - removes @ManyToOne block cleanly
awk -v ref="${REF_UPPER}" -v field="${REF_LOWER}" '
  BEGIN { skip = 0 }
  # Start skipping when we find @ManyToOne
  /@ManyToOne/ { skip = 1; next }
  # End skipping after the private field line
  skip && $0 ~ "private " ref "Entity " field ";" { skip = 0; next }
  # Skip all lines while inside the block
  skip { next }
  # Print everything else
  { print }
' "$ENTITY_FILE" > "$ENTITY_FILE.tmp" && mv "$ENTITY_FILE.tmp" "$ENTITY_FILE"

# Remove import for the referenced Entity
sed -i "/${REF_UPPER}Entity/d" "$ENTITY_FILE"

# Optional: Remove jakarta.persistence.* if it might no longer be needed
# sed -i "/import jakarta.persistence\.\*;/d" "$ENTITY_FILE"

# ================================================================
# 2. Create DTO - Remove the ID field (e.g. department_id)
# ================================================================
CREATE_DTO_FILE="$BASE_DIR/dto/Create${FEATURE_UPPER}DTO.java"

if [ -f "$CREATE_DTO_FILE" ]; then
  echo " → Processing Create${FEATURE_UPPER}DTO..."

  sed -i "/private .* $PROPERTY_NAME;/d" "$CREATE_DTO_FILE"

  # Clean up extra blank lines
  sed -i '/^$/N;/^\n$/D' "$CREATE_DTO_FILE"

  echo "✅ CreateDTO cleaned (removed $PROPERTY_NAME)"
else
  echo "⚠️  CreateDTO file not found (skipped)"
fi

# ================================================================
# 3. Response DTO - Remove nested DTO, name field and mappings
# ================================================================
RESPONSE_DTO_FILE="$BASE_DIR/dto/${FEATURE_UPPER}ResponseDTO.java"

if [ -f "$RESPONSE_DTO_FILE" ]; then
  echo " → Processing ${FEATURE_UPPER}ResponseDTO..."

  # Remove import
  sed -i "/${REF_UPPER}ResponseDTO/d" "$RESPONSE_DTO_FILE"

  # Remove fields
  sed -i "/private ${REF_UPPER}ResponseDTO ${REF_LOWER};/d" "$RESPONSE_DTO_FILE"
  sed -i "/private String ${REF_LOWER}Name;/d" "$RESPONSE_DTO_FILE"

  # Remove mapping lines in fromEntity() method
  sed -i "/set${REF_UPPER}(${FEATURE_LOWER}\.get${REF_UPPER}()/d" "$RESPONSE_DTO_FILE"
  sed -i "/set${REF_UPPER}Name(/d" "$RESPONSE_DTO_FILE"

  # Clean up extra blank lines
  sed -i '/^$/N;/^\n$/D' "$RESPONSE_DTO_FILE"

  echo "✅ ResponseDTO cleaned (removed nested ${REF_UPPER}ResponseDTO and ${REF_LOWER}Name)"
else
  echo "⚠️  ResponseDTO file not found (skipped)"
fi

# Clean up extra blank lines
sed -i '/^$/N;/^\n$/D' "$ENTITY_FILE"


# ================================================================
# 4. SERVICE - Remove repository, validation method & logic
# ================================================================
SERVICE_FILE="$BASE_DIR/${FEATURE_UPPER}Service.java"

if [ -f "$SERVICE_FILE" ]; then
  echo " → Processing ${FEATURE_UPPER}Service..."

  # ------------------- Remove Imports -------------------
  sed -i "/${REF_UPPER}Entity/d" "$SERVICE_FILE"
  sed -i "/${REF_UPPER}Repository/d" "$SERVICE_FILE"

  # ------------------- Remove Injected Repository -------------------
  sed -i "/private final ${REF_UPPER}Repository ${REF_LOWER}Repository;/d" "$SERVICE_FILE"

  # ------------------- Remove validateXXXExists Method (Robust) -------------------
  echo "   → Removing validate${REF_UPPER}Exists method..."

  # Multiple patterns to catch broken or normal validation methods
  sed -i "/private ${REF_UPPER}Entity validate${REF_UPPER}Exists/,/^[[:space:]]*}/d" "$SERVICE_FILE"
  sed -i "/validate${REF_UPPER}Exists(Long id)/,/^[[:space:]]*}/d" "$SERVICE_FILE"
  sed -i "/if (id == null)/,/orElseThrow.*${REF_UPPER}/d" "$SERVICE_FILE"

  # Extra safety passes
  sed -i "/validate${REF_UPPER}Exists/d" "$SERVICE_FILE"

  # ------------------- Remove Usage in create/update -------------------
  sed -i "/${REF_LOWER}Entity ${REF_LOWER} = validate${REF_UPPER}Exists/d" "$SERVICE_FILE"
  sed -i "/entity\.set${REF_UPPER}(${REF_LOWER});/d" "$SERVICE_FILE"

  # ------------------- Remove the ONE extra trailing closing brace -------------------
  echo "   → Removing extra trailing closing brace..."

  # This safely removes only the very last } in the file if it's on its own line (extra class closing)
  sed -i '$s/^[[:space:]]*}[[:space:]]*$//' "$SERVICE_FILE"

  # ------------------- Final Cleanup -------------------
  sed -i '/^$/N;/^\n$/D' "$SERVICE_FILE"

  echo "✅ Service cleaned successfully"
else
  echo "⚠️  Service file not found (skipped)"
fi



echo "✅ Entity successfully cleaned (${REF_UPPER} relationship removed)"