#!/bin/bash

# ===============================
# Remove Property from Feature (Supports --parent)
# ===============================

FEATURE=""
PROPERTY_NAME=""
PARENT=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --feature)
      FEATURE="$2"
      shift 2
      ;;
    --name)
      PROPERTY_NAME="$2"
      shift 2
      ;;
    --parent)
      PARENT="$2"
      shift 2
      ;;
    *)
      echo "❌ Unknown parameter: $1"
      echo "Usage: ./remove-property.sh --feature department --name code [--parent administration]"
      exit 1
      ;;
  esac
done

if [[ -z "$FEATURE" || -z "$PROPERTY_NAME" ]]; then
  echo "❌ Usage: ./remove-property.sh --feature <feature> --name <property> [--parent <parent>]"
  echo "Example: ./remove-property.sh --feature department --name code --parent administration"
  exit 1
fi

FEATURE_LOWER=$(echo "$FEATURE" | tr '[:upper:]' '[:lower:]')
FEATURE_UPPER="$(tr '[:lower:]' '[:upper:]' <<< ${FEATURE_LOWER:0:1})${FEATURE_LOWER:1}"
PROP_CAMEL="$(tr '[:lower:]' '[:upper:]' <<< ${PROPERTY_NAME:0:1})${PROPERTY_NAME:1}"

# Support for --parent
if [ -n "$PARENT" ]; then
  PARENT_LOWER=$(echo "$PARENT" | tr '[:upper:]' '[:lower:]')
  BASE_DIR="src/main/java/com/bonnysimon/starter/features/$PARENT_LOWER/$FEATURE_LOWER"
else
  BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_LOWER"
fi

ENTITY_FILE="$BASE_DIR/${FEATURE_UPPER}Entity.java"
CREATE_DTO_FILE="$BASE_DIR/dto/Create${FEATURE_UPPER}DTO.java"
RESPONSE_DTO_FILE="$BASE_DIR/dto/${FEATURE_UPPER}ResponseDTO.java"
SERVICE_FILE="$BASE_DIR/${FEATURE_UPPER}Service.java"
HTTP_FILE="http-client.http"

echo "🧹 Removing property '$PROPERTY_NAME' from feature '$FEATURE_UPPER'"

# Check if files exist
if [ ! -f "$ENTITY_FILE" ]; then
  echo "❌ Entity file not found: $ENTITY_FILE"
  exit 1
fi

# -------------------------------
# 1. Entity - Remove field + @Column (Fixed)
# -------------------------------
if [ -f "$ENTITY_FILE" ]; then
  echo "   → Processing Entity..."

  # Remove multi-line @Column + field
  sed -i "/@Column.*$PROPERTY_NAME/,/private .* $PROPERTY_NAME;/d" "$ENTITY_FILE"

  # Remove any remaining field
  sed -i "/private .* $PROPERTY_NAME;/d" "$ENTITY_FILE"

  # Remove any orphaned @Column line (this fixes your current issue)
  sed -i "/@Column.*nullable.*$/d" "$ENTITY_FILE"

  # Clean up extra blank lines
  sed -i '/^$/N;/^\n$/D' "$ENTITY_FILE"

  echo "✅ Entity updated"
fi

# -------------------------------
# 2. Create DTO
# -------------------------------
if [ -f "$CREATE_DTO_FILE" ]; then
  sed -i "/private .* $PROPERTY_NAME;/d" "$CREATE_DTO_FILE"
  echo "✅ CreateDTO updated"
fi

# -------------------------------
# 3. Response DTO
# -------------------------------
if [ -f "$RESPONSE_DTO_FILE" ]; then
  sed -i "/private .* $PROPERTY_NAME;/d" "$RESPONSE_DTO_FILE"
  sed -i "/dto\.set${PROP_CAMEL}(/d" "$RESPONSE_DTO_FILE"
  echo "✅ ResponseDTO updated"
fi

# -------------------------------
# 4. Service - Create/Update mapping
# -------------------------------
if [ -f "$SERVICE_FILE" ]; then
  sed -i "/entity\.set${PROP_CAMEL}(request\.get${PROP_CAMEL}());/d" "$SERVICE_FILE"
  echo "✅ Service mapping updated"
fi

# -------------------------------
# 5. Service - Search Specification
# -------------------------------
if [ -f "$SERVICE_FILE" ]; then
  sed -i "/root\.get(\"$PROPERTY_NAME\")/d" "$SERVICE_FILE"
  echo "✅ Removed from search specification"
fi

# -------------------------------
# 6. HTTP Client
# -------------------------------
if [ -f "$HTTP_FILE" ]; then
  sed -i "/\"$PROPERTY_NAME\"/d" "$HTTP_FILE"
  echo "✅ HTTP client updated"
fi

echo "🎉 Property '$PROPERTY_NAME' successfully removed from '$FEATURE_UPPER'"