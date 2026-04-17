#!/bin/bash

# ===============================
# ADD PROPERTY - Full (Entity + DTOs)
# Entity part is exactly the one you said is perfect
# ===============================

FEATURE=""
PROPERTY_NAME=""
PROPERTY_TYPE=""
MANDATORY="false"
PARENT=""
REFERENCE=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --feature)   FEATURE="$2";      shift 2 ;;
    --name)      PROPERTY_NAME="$2"; shift 2 ;;
    --type)      PROPERTY_TYPE="$2"; shift 2 ;;
    --mandatory) MANDATORY="$2";    shift 2 ;;
    --parent)    PARENT="$2";       shift 2 ;;
    --reference) REFERENCE="$2";    shift 2 ;;
    *)
      echo "❌ Unknown parameter: $1"
      echo "Usage: ./add-property.sh --feature position --name departmentId --type Long --mandatory true --reference department --parent administration"
      exit 1
      ;;
  esac
done

if [[ -z "$FEATURE" || -z "$PROPERTY_NAME" || -z "$PROPERTY_TYPE" ]]; then
  echo "❌ Missing required parameters"
  exit 1
fi

FEATURE_LOWER=$(echo "$FEATURE" | tr '[:upper:]' '[:lower:]')
FEATURE_UPPER="$(tr '[:lower:]' '[:upper:]' <<< ${FEATURE_LOWER:0:1})${FEATURE_LOWER:1}"
PROP_CAP="$(tr '[:lower:]' '[:upper:]' <<< ${PROPERTY_NAME:0:1})${PROPERTY_NAME:1}"

if [ -n "$PARENT" ]; then
  PARENT_LOWER=$(echo "$PARENT" | tr '[:upper:]' '[:lower:]')
  BASE_DIR="src/main/java/com/bonnysimon/starter/features/$PARENT_LOWER/$FEATURE_LOWER"
else
  BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_LOWER"
fi

ENTITY_FILE="$BASE_DIR/${FEATURE_UPPER}Entity.java"
CREATE_DTO_FILE="$BASE_DIR/dto/Create${FEATURE_UPPER}DTO.java"
RESPONSE_DTO_FILE="$BASE_DIR/dto/${FEATURE_UPPER}ResponseDTO.java"

# Validate files
for f in "$ENTITY_FILE" "$CREATE_DTO_FILE" "$RESPONSE_DTO_FILE"; do
  [ ! -f "$f" ] && echo "❌ File not found: $f" && exit 1
done

[ "$MANDATORY" = "true" ] && NULLABLE="false" || NULLABLE="true"

IS_REFERENCE=false
if [ -n "$REFERENCE" ]; then
  IS_REFERENCE=true
  REF_LOWER=$(echo "$REFERENCE" | tr '[:upper:]' '[:lower:]')
  REF_UPPER="$(tr '[:lower:]' '[:upper:]' <<< ${REF_LOWER:0:1})${REF_LOWER:1}"
fi

echo "🚀 Adding property '$PROPERTY_NAME' to '$FEATURE_UPPER'"

# ================================================================
# 1. ENTITY - Exactly the version you said is perfect (unchanged)
# ================================================================
if [ "$IS_REFERENCE" = true ]; then
  echo "   → Entity: Keeping your perfect implementation (no changes)"

  if ! grep -q "private ${REF_UPPER}Entity ${REF_LOWER}" "$ENTITY_FILE"; then
    # 1. Add import
    if ! grep -q "${REF_UPPER}Entity" "$ENTITY_FILE"; then
      sed -i "/import lombok.Data;/a\\
import com.bonnysimon.starter.features.${PARENT_LOWER:-}.${REF_LOWER}.${REF_UPPER}Entity;" "$ENTITY_FILE"
    fi

    # 2. Add jakarta.persistence.* if missing
    if ! grep -q "import jakarta.persistence\.\*;" "$ENTITY_FILE"; then
      sed -i "/import com.bonnysimon.starter.features.*Entity;/a\\
import jakarta.persistence.*;" "$ENTITY_FILE"
    fi

    # 3. Add field using awk (your working method)
    awk '
      /private String description;/ {
        print $0
        print ""
        print "    @ManyToOne(fetch = FetchType.LAZY)"
        print "    @JoinColumn(name = \"'"${PROPERTY_NAME}"'\")"
        print "    private '"${REF_UPPER}Entity ${REF_LOWER}"';"
        print ""
        next
      }
      { print }
    ' "$ENTITY_FILE" > "$ENTITY_FILE.tmp" && mv "$ENTITY_FILE.tmp" "$ENTITY_FILE"

    echo "✅ Entity updated with foreign key"
  else
    echo "   Entity already has the relationship"
  fi
fi

# ================================================================
# 2. Create DTO - Add departmentId (Long)
# ================================================================
if [ "$IS_REFERENCE" = true ]; then
  if ! grep -q "private $PROPERTY_TYPE $PROPERTY_NAME" "$CREATE_DTO_FILE"; then
    sed -i "/private String description;/a\\
    private $PROPERTY_TYPE $PROPERTY_NAME;" "$CREATE_DTO_FILE"
    echo "✅ CreateDTO updated → added $PROPERTY_NAME"
  else
    echo "⚠️ CreateDTO already has $PROPERTY_NAME"
  fi
fi

# ================================================================
# 3. Response DTO - Add nested DepartmentResponseDTO + correct mapping
# ================================================================
if [ "$IS_REFERENCE" = true ]; then

  # Add import
  if ! grep -q "${REF_UPPER}ResponseDTO" "$RESPONSE_DTO_FILE"; then
    sed -i "/import lombok.Data;/a\\
import com.bonnysimon.starter.features.${PARENT_LOWER:-}.${REF_LOWER}.dto.${REF_UPPER}ResponseDTO;" "$RESPONSE_DTO_FILE"
    echo "   ✅ Import added for ${REF_UPPER}ResponseDTO"
  fi

  # Add field
  if ! grep -q "private ${REF_UPPER}ResponseDTO ${REF_LOWER}" "$RESPONSE_DTO_FILE"; then
    sed -i "/private String description;/a\\
    private ${REF_UPPER}ResponseDTO ${REF_LOWER};" "$RESPONSE_DTO_FILE"
    echo "✅ ResponseDTO updated → added ${REF_LOWER} (nested DTO)"
  fi

  # Add correct mapping in fromEntity()
  if ! grep -q "set${REF_UPPER}(" "$RESPONSE_DTO_FILE"; then
    sed -i "/setDepartment_id/d" "$RESPONSE_DTO_FILE" 2>/dev/null || true
    sed -i "/dto\.setDescription(${FEATURE_LOWER}\.getDescription());/a\\
            dto.set${REF_UPPER}(${FEATURE_LOWER}.get${REF_UPPER}() != null ? ${REF_UPPER}ResponseDTO.fromEntity(${FEATURE_LOWER}.get${REF_UPPER}()) : null);" "$RESPONSE_DTO_FILE"
    echo "✅ ResponseDTO.fromEntity() mapping added correctly"
  fi
fi

echo ""
echo "🎉 All DTOs updated successfully!"
echo "⚠️  Don't forget to add the column in database:"
echo "   ALTER TABLE MBIMS.${FEATURE_UPPER^^} ADD COLUMN ${PROPERTY_NAME^^} BIGINT;"