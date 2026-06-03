#!/bin/bash

# ================================================
# ADD PROPERTY (Relationship) - Fixed Service Logic
# ================================================

FEATURE=""
PROPERTY_NAME=""
MANDATORY="false"
PARENT=""
REFERENCE=""
REFERENCE_PARENT=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --feature)          FEATURE="$2";           shift 2 ;;
    --name)             PROPERTY_NAME="$2";     shift 2 ;;
    --mandatory)        MANDATORY="$2";         shift 2 ;;
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
PROP_PASCAL=$(to_pascal_case "$PROPERTY_NAME")

REF_PASCAL=$(to_pascal_case "$REFERENCE")
REF_SNAKE=$(to_snake_case "$REFERENCE")

PARENT_SNAKE=$( [ -n "$PARENT" ] && to_snake_case "$PARENT" || echo "" )
REF_PARENT_SNAKE=$( [ -n "$REFERENCE_PARENT" ] && to_snake_case "$REFERENCE_PARENT" || echo "" )

# ====================== READ BASE PACKAGE (NO HARDCODING) ======================
PATH_FILE=".path-to-packages"

if [ ! -f "$PATH_FILE" ]; then
  echo "❌ Error: $PATH_FILE not found!"
  echo "   Run ./setup.sh first to set your package name."
  exit 1
fi

BASE_PACKAGE=$(cat "$PATH_FILE" | tr -d ' \t\r\n')
if [ -z "$BASE_PACKAGE" ]; then
  echo "❌ Error: Package name in $PATH_FILE is empty!"
  exit 1
fi

echo "📦 Using base package: $BASE_PACKAGE"

# Parent Resolution
if [ -n "$PARENT" ]; then
    PARENT_SNAKE=$(to_snake_case "$PARENT")
    FULL_PACKAGE="$BASE_PACKAGE.features.$PARENT_SNAKE.$FEATURE_SNAKE"
else
    FULL_PACKAGE="$BASE_PACKAGE.features.$FEATURE_SNAKE"
    PARENT_SNAKE=""
fi

BASE_DIR="src/main/java/$(echo "$FULL_PACKAGE" | tr '.' '/')"

ENTITY_FILE="$BASE_DIR/${FEATURE_PASCAL}Entity.java"
CREATE_DTO_FILE="$BASE_DIR/dto/Create${FEATURE_PASCAL}DTO.java"
RESPONSE_DTO_FILE="$BASE_DIR/dto/${FEATURE_PASCAL}ResponseDTO.java"
SERVICE_FILE="$BASE_DIR/${FEATURE_PASCAL}Service.java"

# ====================== LOGGING ======================
echo "=================================================="
echo "🚀 ADD RELATIONSHIP PROPERTY STARTED"
echo "=================================================="
echo "Feature         : $FEATURE → $FEATURE_PASCAL ($FEATURE_SNAKE)"
if [ -n "$PARENT" ]; then echo "Feature Parent  : $PARENT → $PARENT_SNAKE"; fi
echo "BASE_DIR        : $BASE_DIR"
echo "BASE_PACKAGE    : $BASE_PACKAGE"
echo "Property        : $PROPERTY_NAME → $PROP_CAMEL ($PROP_SNAKE)"
echo "Reference       : $REFERENCE → $REF_PASCAL ($REF_SNAKE)"
if [ -n "$REFERENCE_PARENT" ]; then echo "Ref Parent      : $REFERENCE_PARENT → $REF_PARENT_SNAKE"; fi
echo "=================================================="

# Validate files
for f in "$ENTITY_FILE" "$CREATE_DTO_FILE" "$RESPONSE_DTO_FILE"; do
  [ ! -f "$f" ] && echo "❌ File not found: $f" && exit 1
done

[ "$MANDATORY" = "true" ] && NULLABLE="false" || NULLABLE="true"

# ====================== ENTITY ======================
echo "🔧 Updating Entity..."
if ! grep -q "private ${REF_PASCAL}Entity ${REF_SNAKE}" "$ENTITY_FILE"; then
  if [ -n "$REFERENCE_PARENT" ]; then
    REF_IMPORT="$BASE_PACKAGE.features.$REF_PARENT_SNAKE.$REF_SNAKE"
  else
    REF_IMPORT="$BASE_PACKAGE.starter.features.$REF_SNAKE"
  fi

  sed -i "/import lombok.Data;/a import ${REF_IMPORT}.${REF_PASCAL}Entity;" "$ENTITY_FILE"

  awk '
    /private String description;/ {
      print $0
      print ""
      print "    @ManyToOne(fetch = FetchType.LAZY)"
      print "    @JoinColumn(name = \"'"${PROP_SNAKE}"'\")"
      print "    private '"${REF_PASCAL}Entity ${REF_SNAKE}"';"
      print ""
      next
    }
    { print }
  ' "$ENTITY_FILE" > "$ENTITY_FILE.tmp" && mv "$ENTITY_FILE.tmp" "$ENTITY_FILE"

  echo "✅ Entity updated"
fi

# ====================== CreateDTO ======================
echo "🔧 Updating CreateDTO..."
if ! grep -q "private $PROPERTY_TYPE $PROP_CAMEL" "$CREATE_DTO_FILE"; then
  sed -i "/private String description;/a\\
    private Long $PROP_CAMEL;" "$CREATE_DTO_FILE"
fi

# ====================== ResponseDTO ======================
echo "🔧 Updating ResponseDTO..."
if [ -n "$REFERENCE_PARENT" ]; then
  REF_DTO_IMPORT="$BASE_PACKAGE.features.$REF_PARENT_SNAKE.$REF_SNAKE.dto"
else
  REF_DTO_IMPORT="$BASE_PACKAGE.features.$REF_SNAKE.dto"
fi

sed -i "/import lombok.Data;/a import ${REF_DTO_IMPORT}.${REF_PASCAL}ResponseDTO;" "$RESPONSE_DTO_FILE"

sed -i "/private String description;/a\\
    private ${REF_PASCAL}ResponseDTO ${REF_SNAKE};" "$RESPONSE_DTO_FILE"

sed -i "/private ${REF_PASCAL}ResponseDTO ${REF_SNAKE};/a\\
    private String ${REF_SNAKE}Name;" "$RESPONSE_DTO_FILE"

echo "✅ ResponseDTO updated"

# ====================== SERVICE - FIXED ======================
if [ -f "$SERVICE_FILE" ]; then
  echo "🔧 Updating Service..."

  if [ -n "$REFERENCE_PARENT" ]; then
    REF_PACKAGE="$BASE_PACKAGE.features.$REF_PARENT_SNAKE.$REF_SNAKE"
  else
    REF_PACKAGE="$BASE_PACKAGE.features.$REF_SNAKE"
  fi

  # Add imports
  sed -i "/^package /a import ${REF_PACKAGE}.${REF_PASCAL}Entity;" "$SERVICE_FILE"
  sed -i "/^package /a import ${REF_PACKAGE}.${REF_PASCAL}Repository;" "$SERVICE_FILE"

  # Inject repository
  if ! grep -q "${REF_PASCAL}Repository ${REF_SNAKE}Repository" "$SERVICE_FILE"; then
    sed -i "/private final ${FEATURE_PASCAL}Repository repository;/a\\
    private final ${REF_PASCAL}Repository ${REF_SNAKE}Repository;" "$SERVICE_FILE"
  fi

  # Add validation method
  if ! grep -q "validate${REF_PASCAL}Exists" "$SERVICE_FILE"; then
    sed -i '$i\
\
    private '"${REF_PASCAL}"'Entity validate'"${REF_PASCAL}"'Exists(Long id) {\
        if (id == null) {\
            if ("'"$NULLABLE"'" == "false") {\
                throw new IllegalArgumentException("'"${REF_PASCAL}"' ID is required");\
            }\
            return null;\
        }\
        return '"${REF_SNAKE}"'Repository.findById(id)\
                .orElseThrow(() -> new IllegalStateException("'"${REF_PASCAL}"' not found with id: " + id));\
    }\
' "$SERVICE_FILE"
  fi

  # Add FK logic in create and update
  sed -i "/entity\.setDescription(request\.getDescription());/a\\
        ${REF_PASCAL}Entity ${REF_SNAKE} = validate${REF_PASCAL}Exists(request.get${PROP_PASCAL}());\\
        entity.set${REF_PASCAL}(${REF_SNAKE});" "$SERVICE_FILE"

  echo "✅ Service updated"
fi

echo ""
echo "🎉 SUCCESS: Relationship property '$PROP_CAMEL' added to '$FEATURE_PASCAL'"
echo "DB Column: $PROP_SNAKE"
echo ""
echo "⚠️ Run migration:"
echo "   ALTER TABLE ${FEATURE_PASCAL^^} ADD COLUMN ${PROP_SNAKE^^} BIGINT;"