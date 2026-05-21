#!/bin/bash

# ================================================
# REMOVE PROPERTY - Smart Naming + Enhanced Logging
# ================================================

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
    --help|-h)
      echo "Usage: ./remove-property.sh --feature <feature> --name <property> [--parent <parent>]"
      echo "Example: ./remove-property.sh --feature \"new user\" --name yearlyDate --parent newManagement"
      exit 0
      ;;
    *)
      echo "❌ Unknown parameter: $1"
      exit 1
      ;;
  esac
done

if [[ -z "$FEATURE" || -z "$PROPERTY_NAME" ]]; then
  echo "❌ Missing required parameters"
  echo "Usage: ./remove-property.sh --feature <feature> --name <property> [--parent <parent>]"
  exit 1
fi

# ====================== SMART NAMING ======================
to_camel_case() {
    echo "$1" | sed -E 's/[_ -]+(.)/\U\1/g' | sed 's/^[A-Z]/\l&/'
}

to_snake_case() {
    echo "$1" | sed -E 's/([a-z0-9])([A-Z])/\1_\2/g' | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/_/g' | sed 's/^_//;s/_$//'
}

to_pascal_case() {
    echo "$1" | sed -E 's/[_ -]+(.)/\U\1/g' | sed 's/^[a-z]/\U&/'
}

# Resolve names
FEATURE_PASCAL=$(to_pascal_case "$FEATURE")
FEATURE_SNAKE=$(to_snake_case "$FEATURE")

PROP_CAMEL=$(to_camel_case "$PROPERTY_NAME")
PROP_SNAKE=$(to_snake_case "$PROPERTY_NAME")
PROP_PASCAL=$(to_pascal_case "$PROPERTY_NAME")

if [ -n "$PARENT" ]; then
    PARENT_SNAKE=$(to_snake_case "$PARENT")
    BASE_DIR="src/main/java/com/bonnysimon/starter/features/$PARENT_SNAKE/$FEATURE_SNAKE"
else
    BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_SNAKE"
fi

ENTITY_FILE="$BASE_DIR/${FEATURE_PASCAL}Entity.java"
CREATE_DTO_FILE="$BASE_DIR/dto/Create${FEATURE_PASCAL}DTO.java"
RESPONSE_DTO_FILE="$BASE_DIR/dto/${FEATURE_PASCAL}ResponseDTO.java"
SERVICE_FILE="$BASE_DIR/${FEATURE_PASCAL}Service.java"
HTTP_FILE="http-client.http"

# ====================== ENHANCED LOGGING ======================
echo "=================================================="
echo "🧹 REMOVE PROPERTY OPERATION STARTED"
echo "=================================================="
echo "Feature Raw     : $FEATURE"
echo "Feature Resolved: $FEATURE_PASCAL ($FEATURE_SNAKE)"
if [ -n "$PARENT" ]; then
echo "Parent Raw      : $PARENT"
echo "Parent Resolved : $(to_pascal_case "$PARENT") ($(to_snake_case "$PARENT"))"
fi
echo "Property Raw    : $PROPERTY_NAME"
echo "Property Camel  : $PROP_CAMEL"
echo "DB Column       : $PROP_SNAKE"
echo "Target Path     : $BASE_DIR"
echo "=================================================="

# Check if Entity exists
if [ ! -f "$ENTITY_FILE" ]; then
  echo "❌ Entity file not found: $ENTITY_FILE"
  echo "   Please check feature name and parent spelling."
  exit 1
fi

echo "🚀 Starting removal of property '$PROP_CAMEL'..."

# ================================================================
# 1. Entity
# ================================================================
echo "🔧 Updating Entity..."
sed -i "/@Column.*name = \"$PROP_SNAKE\"/,/private .* $PROP_CAMEL;/d" "$ENTITY_FILE"
sed -i "/private .* $PROP_CAMEL;/d" "$ENTITY_FILE"
sed -i "/@Column.*nullable.*$/d" "$ENTITY_FILE"
# Clean extra blank lines
sed -i '/^$/N;/^\n$/D' "$ENTITY_FILE"
echo "✅ Entity updated"

# ================================================================
# 2. CreateDTO
# ================================================================
echo "🔧 Updating CreateDTO..."
if [ -f "$CREATE_DTO_FILE" ]; then
  sed -i "/private .* $PROP_CAMEL;/d" "$CREATE_DTO_FILE"
  echo "✅ CreateDTO updated"
fi

# ================================================================
# 3. ResponseDTO
# ================================================================
echo "🔧 Updating ResponseDTO..."
if [ -f "$RESPONSE_DTO_FILE" ]; then
  sed -i "/private .* $PROP_CAMEL;/d" "$RESPONSE_DTO_FILE"
  sed -i "/set${PROP_PASCAL}(/d" "$RESPONSE_DTO_FILE"
  echo "✅ ResponseDTO updated"
fi

# ================================================================
# 4. Service
# ================================================================
echo "🔧 Updating Service..."
if [ -f "$SERVICE_FILE" ]; then
  sed -i "/set${PROP_PASCAL}(request\.get${PROP_PASCAL}());/d" "$SERVICE_FILE"
  echo "✅ Service mapping removed"
fi

# ================================================================
# 5. HTTP Client
# ================================================================
echo "🔧 Updating HTTP Client..."
if [ -f "$HTTP_FILE" ]; then
  sed -i "/\"$PROP_SNAKE\"/d" "$HTTP_FILE"
  sed -i "/\"$PROP_CAMEL\"/d" "$HTTP_FILE"
  echo "✅ HTTP client updated"
fi

echo ""
echo "🎉 SUCCESS: Property '$PROP_CAMEL' has been removed from '$FEATURE_PASCAL'"
echo "   DB Column removed: $PROP_SNAKE"
echo ""
echo "⚠️  Don't forget to drop the column from database:"
echo "   ALTER TABLE ${FEATURE_PASCAL^^} DROP COLUMN ${PROP_SNAKE^^};"