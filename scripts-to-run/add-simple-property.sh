#!/bin/bash
# ===============================
# ADD NORMAL PROPERTY - FIXED (DTOs + Entity)
# ===============================
FEATURE=""
PROPERTY_NAME=""
PROPERTY_TYPE=""
MANDATORY="false"
PARENT=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --feature) FEATURE="$2"; shift 2 ;;
    --name) PROPERTY_NAME="$2"; shift 2 ;;
    --type) PROPERTY_TYPE="$2"; shift 2 ;;
    --mandatory) MANDATORY="$2"; shift 2 ;;
    --parent) PARENT="$2"; shift 2 ;;
    *)
      echo "❌ Unknown parameter: $1"
      echo "Usage: ./add-simple-property.sh --feature position --name salary --type BigDecimal --parent administration"
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

echo "🚀 Adding normal property '$PROPERTY_NAME' ($PROPERTY_TYPE) to '$FEATURE_UPPER'"

# ================================================================
# 1. ENTITY
# ================================================================
if grep -q "private $PROPERTY_TYPE $PROPERTY_NAME;" "$ENTITY_FILE"; then
  echo "⚠️ Entity already has property '$PROPERTY_NAME'"
else
  # Add import if needed
  if [[ "$PROPERTY_TYPE" == "BigDecimal" ]] && ! grep -q "BigDecimal" "$ENTITY_FILE"; then
    sed -i "/import lombok.Data;/a\\
import java.math.BigDecimal;" "$ENTITY_FILE"
  elif [[ "$PROPERTY_TYPE" == "LocalDate" ]] && ! grep -q "LocalDate" "$ENTITY_FILE"; then
    sed -i "/import lombok.Data;/a\\
import java.time.LocalDate;" "$ENTITY_FILE"
  fi

  # Insert before last }
  awk -v type="$PROPERTY_TYPE" -v name="$PROPERTY_NAME" '
    BEGIN { inserted=0 }
    /^\s*}$/ && inserted == 0 {
      print ""
      print "    @Column(name = \"" tolower(name) "\", nullable = true)"
      print "    private " type " " name ";"
      print ""
      inserted=1
    }
    { print }
  ' "$ENTITY_FILE" > "$ENTITY_FILE.tmp" && mv "$ENTITY_FILE.tmp" "$ENTITY_FILE"

  echo "✅ Entity updated"
fi

# ================================================================
# 2. CreateDTO
# ================================================================
if ! grep -q "private $PROPERTY_TYPE $PROPERTY_NAME;" "$CREATE_DTO_FILE"; then
  sed -i "/private String description;/a\\
    private $PROPERTY_TYPE $PROPERTY_NAME;" "$CREATE_DTO_FILE"
  echo "✅ CreateDTO updated"
else
  echo "⚠️ CreateDTO already has $PROPERTY_NAME"
fi

# ================================================================
# 3. ResponseDTO - FIXED
# ================================================================
if ! grep -q "private $PROPERTY_TYPE $PROPERTY_NAME;" "$RESPONSE_DTO_FILE"; then
  # Add the field
  sed -i "/private String description;/a\\
    private $PROPERTY_TYPE $PROPERTY_NAME;" "$RESPONSE_DTO_FILE"
  echo "✅ ResponseDTO field added"
else
  echo "⚠️ ResponseDTO already has $PROPERTY_NAME"
fi

# Add mapping in fromEntity() - Fixed condition
if ! grep -q "set${PROP_CAP}(" "$RESPONSE_DTO_FILE"; then
  sed -i "/dto\.setDescription(${FEATURE_LOWER}\.getDescription());/a\\
            dto.set${PROP_CAP}(${FEATURE_LOWER}.get${PROP_CAP}());" "$RESPONSE_DTO_FILE"
  echo "✅ ResponseDTO.fromEntity() mapping added"
fi

# ================================================================
# 4. SERVICE
# ================================================================
SERVICE_FILE="$BASE_DIR/${FEATURE_UPPER}Service.java"
if [ -f "$SERVICE_FILE" ]; then
  if ! grep -q "entity\.set${PROP_CAP}(request.get${PROP_CAP}())" "$SERVICE_FILE"; then
    sed -i "/entity\.setDescription(request\.getDescription());/a\\
        entity.set${PROP_CAP}(request.get${PROP_CAP}());" "$SERVICE_FILE"
    echo "✅ Service mapping added"
  fi
fi




# ================================================================
# 5. HTTP Client - FIXED (No trailing comma)
# ================================================================
HTTP_FILE="http-client.http"

if [ -f "$HTTP_FILE" ]; then
  FEATURE_PLURAL=$(echo "$FEATURE_LOWER" | sed 's/$/s/')

  echo " → Updating http-client.http for feature: $FEATURE_PLURAL"

  if grep -A 100 "### FEATURE: $FEATURE_PLURAL" "$HTTP_FILE" | grep -q "\"$PROPERTY_NAME\":"; then
    echo "⚠️ '$PROPERTY_NAME' already exists in $FEATURE_PLURAL block"
  else
    awk -v feature="### FEATURE: $FEATURE_PLURAL" -v prop="$PROPERTY_NAME" '
      BEGIN { in_block=0; in_json=0 }

      $0 == feature { in_block=1; print; next }

      in_block && /^\s*### FEATURE:/ && $0 != feature {
        in_block=0
      }

      in_block {
        if ($0 ~ /^[[:space:]]*\{/) {
          in_json=1
          print
          next
        }

        if (in_json && /^[[:space:]]*\}/) {
          # Add new property BEFORE closing brace, without trailing comma
          print "    \"" prop "\": null"
          print $0
          in_json=0
          next
        }

        # Ensure previous lines have comma (except if it was the last one)
        if (in_json && $0 !~ /,$/ && $0 !~ /^[[:space:]]*\}/ && $0 !~ /^[[:space:]]*$/) {
          print $0 ","
        } else {
          print
        }
        next
      }

      { print }
    ' "$HTTP_FILE" > "$HTTP_FILE.tmp" && mv "$HTTP_FILE.tmp" "$HTTP_FILE"

    echo "✅ Successfully added '$PROPERTY_NAME' to $FEATURE_PLURAL block (no trailing comma)"
  fi
else
  echo "⚠️ http-client.http not found"
fi

echo ""
echo "🎉 Property '$PROPERTY_NAME' added successfully!"
echo "⚠️ Run this migration:"
echo "   ALTER TABLE MBIMS.${FEATURE_UPPER^^} ADD COLUMN ${PROPERTY_NAME^^} VARCHAR(255);"