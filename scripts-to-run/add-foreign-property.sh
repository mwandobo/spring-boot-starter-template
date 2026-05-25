#!/bin/bash

# ================================================
# ADD PROPERTY (Relationship) - Smart Naming + Full Service
# ================================================

FEATURE=""
PROPERTY_NAME=""
PROPERTY_TYPE="Long"
MANDATORY="false"
PARENT=""
REFERENCE=""
REFERENCE_PARENT=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --feature)          FEATURE="$2";           shift 2 ;;
    --name)             PROPERTY_NAME="$2";     shift 2 ;;
    --type)             PROPERTY_TYPE="$2";     shift 2 ;;
    --mandatory)        MANDATORY="$2";         shift 2 ;;
    --parent)           PARENT="$2";            shift 2 ;;
    --reference)        REFERENCE="$2";         shift 2 ;;
    --reference-parent) REFERENCE_PARENT="$2";  shift 2 ;;
    *)
      echo "❌ Unknown parameter: $1"
      echo "Usage: ./add-property.sh --feature position --name departmentId --reference department --reference-parent administration"
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

if [ -n "$PARENT" ]; then
    BASE_DIR="src/main/java/com/bonnysimon/starter/features/$PARENT_SNAKE/$FEATURE_SNAKE"
else
    BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_SNAKE"
fi

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
echo "Property        : $PROPERTY_NAME → $PROP_CAMEL ($PROP_SNAKE)"
echo "Reference       : $REFERENCE → $REF_PASCAL ($REF_SNAKE)"
if [ -n "$REFERENCE_PARENT" ]; then echo "Ref Parent      : $REFERENCE_PARENT → $REF_PARENT_SNAKE"; fi
echo "=================================================="

# Validate files
for f in "$ENTITY_FILE" "$CREATE_DTO_FILE" "$RESPONSE_DTO_FILE"; do
  [ ! -f "$f" ] && echo "❌ File not found: $f" && exit 1
done

[ "$MANDATORY" = "true" ] && NULLABLE="false" || NULLABLE="true"

# ================================================================
# 1. ENTITY
# ================================================================
echo "🔧 Updating Entity..."
if ! grep -q "private ${REF_PASCAL}Entity ${REF_SNAKE}" "$ENTITY_FILE"; then
  if ! grep -q "${REF_PASCAL}Entity" "$ENTITY_FILE"; then
    sed -i "/import lombok.Data;/a\\
import com.bonnysimon.starter.features.${REF_PARENT_SNAKE:-$REF_SNAKE}.${REF_SNAKE}.${REF_PASCAL}Entity;" "$ENTITY_FILE"
  fi

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

  echo "✅ Entity updated with foreign key"
fi

# ================================================================
# 2. Create DTO
# ================================================================
echo "🔧 Updating CreateDTO..."
if ! grep -q "private $PROPERTY_TYPE $PROP_CAMEL" "$CREATE_DTO_FILE"; then
  sed -i "/private String description;/a\\
    private $PROPERTY_TYPE $PROP_CAMEL;" "$CREATE_DTO_FILE"
  echo "✅ CreateDTO updated"
fi

# ================================================================
# 3. Response DTO
# ================================================================
echo "🔧 Updating ResponseDTO..."

if ! grep -q "${REF_PASCAL}ResponseDTO" "$RESPONSE_DTO_FILE"; then
  sed -i "/import lombok.Data;/a\\
import com.bonnysimon.starter.features.${REF_PARENT_SNAKE:-$REF_SNAKE}.${REF_SNAKE}.dto.${REF_PASCAL}ResponseDTO;" "$RESPONSE_DTO_FILE"
fi

if ! grep -q "private ${REF_PASCAL}ResponseDTO ${REF_SNAKE}" "$RESPONSE_DTO_FILE"; then
  sed -i "/private String description;/a\\
    private ${REF_PASCAL}ResponseDTO ${REF_SNAKE};" "$RESPONSE_DTO_FILE"
fi

if ! grep -q "private String ${REF_SNAKE}Name" "$RESPONSE_DTO_FILE"; then
  sed -i "/private ${REF_PASCAL}ResponseDTO ${REF_SNAKE};/a\\
    private String ${REF_SNAKE}Name;" "$RESPONSE_DTO_FILE"
fi

echo "✅ ResponseDTO updated"

# ================================================================
# 4. SERVICE - Full Logic (Updated with Smart Naming)
# ================================================================
if [ -f "$SERVICE_FILE" ]; then
  echo "🔧 Updating Service..."

  # Package path for reference
  REF_PACKAGE="com.bonnysimon.starter.features.${REF_PARENT_SNAKE:-$REF_SNAKE}.${REF_SNAKE}"

  # Clean old imports
  sed -i "/${REF_PASCAL}Entity/d" "$SERVICE_FILE"
  sed -i "/${REF_PASCAL}Repository/d" "$SERVICE_FILE"

  # Add imports
  sed -i "/^package /a\\
import ${REF_PACKAGE}.${REF_PASCAL}Entity;" "$SERVICE_FILE"
  sed -i "/^package /a\\
import ${REF_PACKAGE}.${REF_PASCAL}Repository;" "$SERVICE_FILE"

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

  # Add FK logic
  if ! grep -q "validate${REF_PASCAL}Exists(request.get${PROP_PASCAL}())" "$SERVICE_FILE"; then
    sed -i "/entity\.setDescription(request\.getDescription());/a\\
        ${REF_PASCAL}Entity ${REF_SNAKE} = validate${REF_PASCAL}Exists(request.get${PROP_PASCAL}());\\
        entity.set${REF_PASCAL}(${REF_SNAKE});" "$SERVICE_FILE"
  fi

  echo "✅ Service updated with relationship logic"
fi

echo ""
echo "🎉 SUCCESS: Relationship property '$PROP_CAMEL' added to '$FEATURE_PASCAL'"
echo "DB Column: $PROP_SNAKE"
echo ""
echo "⚠️ Don't forget to run migration:"
echo "   ALTER TABLE ${FEATURE_PASCAL^^} ADD COLUMN ${PROP_SNAKE^^} BIGINT;"