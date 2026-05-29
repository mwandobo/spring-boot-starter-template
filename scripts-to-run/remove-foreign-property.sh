#!/bin/bash

# ================================================
# REMOVE FOREIGN PROPERTY - Careful & Smart
# ================================================

FEATURE=""
PROPERTY_NAME=""
PARENT=""
REFERENCE=""
REFERENCE_PARENT=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --feature)          FEATURE="$2";           shift 2 ;;
    --name)             PROPERTY_NAME="$2";     shift 2 ;;
    --parent)           PARENT="$2";            shift 2 ;;
    --reference)        REFERENCE="$2";         shift 2 ;;
    --reference-parent) REFERENCE_PARENT="$2";  shift 2 ;;
    *)
      echo "❌ Unknown parameter: $1"
      exit 1
      ;;
  esac
done

if [[ -z "$FEATURE" || -z "$PROPERTY_NAME" || -z "$REFERENCE" ]]; then
  echo "❌ Missing required parameters"
  exit 1
fi

# ====================== SMART NAMING ======================
to_camel_case() { echo "$1" | sed -E 's/[_ -]+(.)/\U\1/g' | sed 's/^[A-Z]/\l&/'; }
to_snake_case() { echo "$1" | sed -E 's/([a-z0-9])([A-Z])/\1_\2/g' | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/_/g' | sed 's/^_//;s/_$//'; }
to_pascal_case() { echo "$1" | sed -E 's/[_ -]+(.)/\U\1/g' | sed 's/^[a-z]/\U&/'; }

FEATURE_PASCAL=$(to_pascal_case "$FEATURE")
FEATURE_SNAKE=$(to_snake_case "$FEATURE")

PROP_CAMEL=$(to_camel_case "$PROPERTY_NAME")
PROP_SNAKE=$(to_snake_case "$PROPERTY_NAME")

REF_PASCAL=$(to_pascal_case "$REFERENCE")
REF_SNAKE=$(to_snake_case "$REFERENCE")

PARENT_SNAKE=$( [ -n "$PARENT" ] && to_snake_case "$PARENT" || echo "" )
REF_PARENT_SNAKE=$( [ -n "$REFERENCE_PARENT" ] && to_snake_case "$REFERENCE_PARENT" || echo "" )

if [ -n "$PARENT" ]; then
    BASE_DIR="src/main/java/com/bonnysimon/starter/features/$PARENT_SNAKE/$FEATURE_SNAKE"
else
    BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_SNAKE"
fi

ENTITY_FILE="$BASE_DIR/${FEATURE_PASCAL}Entity.java"
CREATE_DTO_FILE="$BASE_DIR/dto/Create${FEATURE_PASCAL}DTO.java"
RESPONSE_DTO_FILE="$BASE_DIR/dto/${FEATURE_PASCAL}ResponseDTO.java"
SERVICE_FILE="$BASE_DIR/${FEATURE_PASCAL}Service.java"

echo "=================================================="
echo "🧹 REMOVE FOREIGN PROPERTY"
echo "=================================================="
echo "Feature    : $FEATURE → $FEATURE_PASCAL"
echo "Property   : $PROPERTY_NAME → $PROP_CAMEL"
echo "Reference  : $REFERENCE → $REF_PASCAL"
echo "=================================================="

# ====================== 1. ENTITY ======================
echo "🔧 Cleaning Entity..."

# Remove the entire @ManyToOne block + field (more robust)
sed -i "/@ManyToOne/,/private .*${REF_PASCAL}Entity ${REF_SNAKE};/d" "$ENTITY_FILE"

# Extra safety removals
sed -i "/${REF_PASCAL}Entity ${REF_SNAKE}/d" "$ENTITY_FILE"
sed -i "/@JoinColumn(name = \"${PROP_SNAKE}\")/d" "$ENTITY_FILE"
sed -i "/@ManyToOne(fetch = FetchType.LAZY)/d" "$ENTITY_FILE"

# Clean extra blank lines
sed -i '/^$/N;/^\n$/D' "$ENTITY_FILE"

echo "✅ Entity cleaned"

# ====================== 2. CreateDTO ======================
echo "🔧 Cleaning CreateDTO..."
if [ -f "$CREATE_DTO_FILE" ]; then
  sed -i "/private .* $PROP_CAMEL;/d" "$CREATE_DTO_FILE"
  sed -i '/^$/N;/^\n$/D' "$CREATE_DTO_FILE"
  echo "✅ CreateDTO cleaned"
fi

# ====================== 3. ResponseDTO ======================
echo "🔧 Cleaning ResponseDTO..."
if [ -f "$RESPONSE_DTO_FILE" ]; then

  # Remove import - more specific
  sed -i "/import .*${REF_PASCAL}ResponseDTO/d" "$RESPONSE_DTO_FILE"

  # Remove fields - only lines starting with private
  sed -i "/^[[:space:]]*private ${REF_PASCAL}ResponseDTO ${REF_SNAKE};/d" "$RESPONSE_DTO_FILE"
  sed -i "/^[[:space:]]*private String ${REF_SNAKE}Name;/d" "$RESPONSE_DTO_FILE"

  # Remove mapping lines inside fromEntity method
  sed -i "/dto\.set${REF_PASCAL}(/d" "$RESPONSE_DTO_FILE"
  sed -i "/dto\.set${REF_PASCAL}Name(/d" "$RESPONSE_DTO_FILE"

  # Clean extra blank lines
  sed -i '/^$/N;/^\n$/D' "$RESPONSE_DTO_FILE"

  echo "✅ ResponseDTO cleaned (removed ${REF_PASCAL}ResponseDTO fields)"
fi

echo ""
echo "🎉 SUCCESS: Foreign key '$PROP_CAMEL' removed from '$FEATURE_PASCAL'"
echo "DB Column : $PROP_SNAKE"
echo ""
echo "⚠️ Run this SQL:"
echo "   ALTER TABLE ${FEATURE_PASCAL^^} DROP COLUMN ${PROP_SNAKE^^};"