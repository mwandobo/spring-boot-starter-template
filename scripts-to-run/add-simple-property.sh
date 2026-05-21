#!/bin/bash
# ================================================
# ADD PROPERTY - Enhanced Logging + Smart Imports (All Files)
# ================================================

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
      exit 1
      ;;
  esac
done

if [[ -z "$FEATURE" || -z "$PROPERTY_NAME" || -z "$PROPERTY_TYPE" ]]; then
  echo "❌ Missing required parameters"
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

# ====================== ENHANCED LOGGING ======================
echo "=================================================="
echo "🚀 ADD PROPERTY OPERATION STARTED"
echo "=================================================="
echo "Feature        : $FEATURE → $FEATURE_PASCAL ($FEATURE_SNAKE)"
if [ -n "$PARENT" ]; then
echo "Parent         : $PARENT → $(to_snake_case "$PARENT")"
fi
echo "Property       : $PROPERTY_NAME → $PROP_CAMEL"
echo "DB Column      : $PROP_SNAKE"
echo "Data Type      : $PROPERTY_TYPE"
echo "=================================================="

# Validate files
for f in "$ENTITY_FILE" "$CREATE_DTO_FILE" "$RESPONSE_DTO_FILE"; do
  [ ! -f "$f" ] && echo "❌ File not found: $f" && exit 1
done

# Function to add import if needed
add_import() {
  local file=$1
  local import=$2
  if ! grep -q "$import" "$file"; then
    sed -i "/import lombok.Data;/a $import" "$file"
    echo "✅ Added import in $(basename "$file"): $import"
  fi
}

# ================================================================
# 1. ENTITY
# ================================================================
echo "🔧 Updating Entity..."
if grep -q "private .* $PROP_CAMEL;" "$ENTITY_FILE"; then
  echo "⚠️ Property already exists in Entity"
else
  case "$PROPERTY_TYPE" in
    "BigDecimal") add_import "$ENTITY_FILE" "import java.math.BigDecimal;" ;;
    "LocalDate") add_import "$ENTITY_FILE" "import java.time.LocalDate;" ;;
    "LocalDateTime") add_import "$ENTITY_FILE" "import java.time.LocalDateTime;" ;;
    "UUID") add_import "$ENTITY_FILE" "import java.util.UUID;" ;;
  esac

  sed -i "/private String description;/a\\
\\
    @Column(name = \"${PROP_SNAKE}\", nullable = ${MANDATORY})\\
    private ${PROPERTY_TYPE} ${PROP_CAMEL};" "$ENTITY_FILE"
  echo "✅ Entity field added"
fi

# ================================================================
# 2. CreateDTO
# ================================================================
echo "🔧 Updating CreateDTO..."
if ! grep -q "private .* $PROP_CAMEL;" "$CREATE_DTO_FILE"; then
  case "$PROPERTY_TYPE" in
    "BigDecimal") add_import "$CREATE_DTO_FILE" "import java.math.BigDecimal;" ;;
    "LocalDate") add_import "$CREATE_DTO_FILE" "import java.time.LocalDate;" ;;
    "LocalDateTime") add_import "$CREATE_DTO_FILE" "import java.time.LocalDateTime;" ;;
    "UUID") add_import "$CREATE_DTO_FILE" "import java.util.UUID;" ;;
  esac

  sed -i "/private String description;/a\\
    private ${PROPERTY_TYPE} ${PROP_CAMEL};" "$CREATE_DTO_FILE"
  echo "✅ CreateDTO updated"
else
  echo "⚠️ CreateDTO already has this property"
fi

# ================================================================
# 3. ResponseDTO
# ================================================================
echo "🔧 Updating ResponseDTO..."
if ! grep -q "private .* $PROP_CAMEL;" "$RESPONSE_DTO_FILE"; then
  case "$PROPERTY_TYPE" in
    "BigDecimal") add_import "$RESPONSE_DTO_FILE" "import java.math.BigDecimal;" ;;
    "LocalDate") add_import "$RESPONSE_DTO_FILE" "import java.time.LocalDate;" ;;
    "LocalDateTime") add_import "$RESPONSE_DTO_FILE" "import java.time.LocalDateTime;" ;;
    "UUID") add_import "$RESPONSE_DTO_FILE" "import java.util.UUID;" ;;
  esac

  sed -i "/private String description;/a\\
    private ${PROPERTY_TYPE} ${PROP_CAMEL};" "$RESPONSE_DTO_FILE"
  echo "✅ ResponseDTO field added"
else
  echo "⚠️ ResponseDTO already has this property"
fi

if ! grep -q "set${PROP_PASCAL}(" "$RESPONSE_DTO_FILE"; then
  sed -i "/dto\.setDescription(${FEATURE_SNAKE}\.getDescription());/a\\
            dto.set${PROP_PASCAL}(${FEATURE_SNAKE}.get${PROP_PASCAL}());" "$RESPONSE_DTO_FILE"
  echo "✅ ResponseDTO mapping added"
fi

# ================================================================
# 4. SERVICE
# ================================================================
if [ -f "$SERVICE_FILE" ]; then
  echo "🔧 Updating Service..."
  if ! grep -q "set${PROP_PASCAL}(request.get${PROP_PASCAL}())" "$SERVICE_FILE"; then
    sed -i "/entity\.setDescription(request\.getDescription());/a\\
        entity.set${PROP_PASCAL}(request.get${PROP_PASCAL}());" "$SERVICE_FILE"
    echo "✅ Service mapping added"
  fi
fi

echo ""
echo "🎉 SUCCESS: Property '$PROP_CAMEL' ($PROP_SNAKE) added successfully!"
echo "DB Column: ${PROP_SNAKE}"
echo ""
echo "⚠️ Next Step - Run this migration:"
echo "   ALTER TABLE ${FEATURE_PASCAL^^} ADD COLUMN ${PROP_SNAKE^^} ${PROPERTY_TYPE^^};"