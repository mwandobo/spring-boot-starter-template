#!/bin/bash
# ================================================
# ADD PROPERTY - Full Smart Naming (Feature + Parent + Property)
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
      echo "Usage: ./add-simple-property.sh --feature department --name code --type String"
      echo "       ./add-simple-property.sh --feature newDepartment --name basicSalary --type BigDecimal --parent administration"
      exit 1
      ;;
  esac
done

if [[ -z "$FEATURE" || -z "$PROPERTY_NAME" || -z "$PROPERTY_TYPE" ]]; then
  echo "❌ Missing required parameters"
  exit 1
fi

# ====================== SMART NAMING FUNCTIONS ======================
to_camel_case() {
    echo "$1" | sed -E 's/[_ -]+(.)/\U\1/g' | sed 's/^[A-Z]/\l&/'
}

to_snake_case() {
    echo "$1" | sed -E 's/([a-z0-9])([A-Z])/\1_\2/g' | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/_/g' | sed 's/^_//;s/_$//'
}

to_pascal_case() {
    echo "$1" | sed -E 's/[_ -]+(.)/\U\1/g' | sed 's/^[a-z]/\U&/'
}

# Apply conversions
FEATURE_PASCAL=$(to_pascal_case "$FEATURE")      # NewDepartment
FEATURE_SNAKE=$(to_snake_case "$FEATURE")        # new_department

PROP_CAMEL=$(to_camel_case "$PROPERTY_NAME")     # basicSalary
PROP_SNAKE=$(to_snake_case "$PROPERTY_NAME")     # basic_salary
PROP_PASCAL=$(to_pascal_case "$PROPERTY_NAME")   # BasicSalary

# Parent handling
if [ -n "$PARENT" ]; then
    PARENT_SNAKE=$(to_snake_case "$PARENT")
    BASE_DIR="src/main/java/com/bonnysimon/starter/features/$PARENT_SNAKE/$FEATURE_SNAKE"
    BASE_PACKAGE="com.bonnysimon.starter.features.$PARENT_SNAKE.$FEATURE_SNAKE"
else
    BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_SNAKE"
    BASE_PACKAGE="com.bonnysimon.starter.features.$FEATURE_SNAKE"
fi

ENTITY_FILE="$BASE_DIR/${FEATURE_PASCAL}Entity.java"
CREATE_DTO_FILE="$BASE_DIR/dto/Create${FEATURE_PASCAL}DTO.java"
RESPONSE_DTO_FILE="$BASE_DIR/dto/${FEATURE_PASCAL}ResponseDTO.java"
SERVICE_FILE="$BASE_DIR/${FEATURE_PASCAL}Service.java"

echo "=================================================="
echo "Feature        : $FEATURE → $FEATURE_PASCAL ($FEATURE_SNAKE)"
if [ -n "$PARENT" ]; then
echo "Parent         : $PARENT → $PARENT_SNAKE"
fi
echo "Property       : $PROPERTY_NAME"
echo "→ Java Field   : $PROP_CAMEL"
echo "→ DB Column    : $PROP_SNAKE"
echo "=================================================="

# Validate files exist
for f in "$ENTITY_FILE" "$CREATE_DTO_FILE" "$RESPONSE_DTO_FILE"; do
  [ ! -f "$f" ] && echo "❌ File not found: $f" && exit 1
done

echo "🚀 Adding property '$PROP_CAMEL' ($PROPERTY_TYPE) to '$FEATURE_PASCAL'"

# ================================================================
# 1. ENTITY
# ================================================================
if grep -q "private .* $PROP_CAMEL;" "$ENTITY_FILE"; then
  echo "⚠️ Entity already has property '$PROP_CAMEL'"
else
  # Add imports if needed
  if [[ "$PROPERTY_TYPE" == "BigDecimal" ]] && ! grep -q "BigDecimal" "$ENTITY_FILE"; then
    sed -i "/import lombok.Data;/a import java.math.BigDecimal;" "$ENTITY_FILE"
  elif [[ "$PROPERTY_TYPE" == "LocalDate" ]] && ! grep -q "LocalDate" "$ENTITY_FILE"; then
    sed -i "/import lombok.Data;/a import java.time.LocalDate;" "$ENTITY_FILE"
  fi

  # Add field
  sed -i "/private String description;/a\\
\\
    @Column(name = \"${PROP_SNAKE}\", nullable = ${MANDATORY})\\
    private ${PROPERTY_TYPE} ${PROP_CAMEL};" "$ENTITY_FILE"

  echo "✅ Entity updated"
fi

# ================================================================
# 2. CreateDTO
# ================================================================
if ! grep -q "private .* $PROP_CAMEL;" "$CREATE_DTO_FILE"; then
  sed -i "/private String description;/a\\
    private ${PROPERTY_TYPE} ${PROP_CAMEL};" "$CREATE_DTO_FILE"
  echo "✅ CreateDTO updated"
fi

# ================================================================
# 3. ResponseDTO
# ================================================================
if ! grep -q "private .* $PROP_CAMEL;" "$RESPONSE_DTO_FILE"; then
  sed -i "/private String description;/a\\
    private ${PROPERTY_TYPE} ${PROP_CAMEL};" "$RESPONSE_DTO_FILE"
  echo "✅ ResponseDTO field added"
fi

if ! grep -q "set${PROP_PASCAL}(" "$RESPONSE_DTO_FILE"; then
  sed -i "/dto\.setDescription(${FEATURE_SNAKE}\.getDescription());/a\\
            dto.set${PROP_PASCAL}(${FEATURE_SNAKE}.get${PROP_PASCAL}());" "$RESPONSE_DTO_FILE"
  echo "✅ ResponseDTO.fromEntity() mapping added"
fi

# ================================================================
# 4. SERVICE
# ================================================================
if [ -f "$SERVICE_FILE" ]; then
  if ! grep -q "set${PROP_PASCAL}(request.get${PROP_PASCAL}())" "$SERVICE_FILE"; then
    sed -i "/entity\.setDescription(request\.getDescription());/a\\
        entity.set${PROP_PASCAL}(request.get${PROP_PASCAL}());" "$SERVICE_FILE"
    echo "✅ Service mapping added"
  fi
fi

echo ""
echo "🎉 Property '$PROP_CAMEL' added successfully!"
echo "DB Column: ${PROP_SNAKE}"
echo "⚠️ Don't forget to create migration:"
echo "   ALTER TABLE ${FEATURE_PASCAL^^} ADD COLUMN ${PROP_SNAKE^^} ${PROPERTY_TYPE^^};"