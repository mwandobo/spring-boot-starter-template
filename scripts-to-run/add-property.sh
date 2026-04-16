#!/bin/bash

# ===============================
# Add Property to Feature (Robust Version)
# ===============================

FEATURE=""
PROPERTY_NAME=""
PROPERTY_TYPE=""
MANDATORY="false"
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
    --type)
      PROPERTY_TYPE="$2"
      shift 2
      ;;
    --mandatory)
      MANDATORY="$2"
      shift 2
      ;;
    --parent)
      PARENT="$2"
      shift 2
      ;;
    *)
      echo "❌ Unknown parameter: $1"
      echo "Usage: ./add-property.sh --feature department --name code --type String --mandatory true [--parent administration]"
      exit 1
      ;;
  esac
done

if [[ -z "$FEATURE" || -z "$PROPERTY_NAME" || -z "$PROPERTY_TYPE" ]]; then
  echo "❌ Missing required parameters"
  echo "Usage: ./add-property.sh --feature department --name code --type String --mandatory true [--parent administration]"
  exit 1
fi

FEATURE_LOWER=$(echo "$FEATURE" | tr '[:upper:]' '[:lower:]')
FEATURE_UPPER="$(tr '[:lower:]' '[:upper:]' <<< ${FEATURE_LOWER:0:1})${FEATURE_LOWER:1}"
PROP_CAP="$(tr '[:lower:]' '[:upper:]' <<< ${PROPERTY_NAME:0:1})${PROPERTY_NAME:1}"

# Support parent folder
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

# Validate files
for f in "$ENTITY_FILE" "$CREATE_DTO_FILE" "$RESPONSE_DTO_FILE" "$SERVICE_FILE"; do
  if [ ! -f "$f" ]; then
    echo "❌ Missing file: $f"
    exit 1
  fi
done

[ "$MANDATORY" = "true" ] && NULLABLE="false" || NULLABLE="true"

echo "🚀 Adding property '$PROPERTY_NAME' ($PROPERTY_TYPE) to '$FEATURE_UPPER'"

# -------------------------------
# 1. Entity
# -------------------------------
if ! grep -q "private $PROPERTY_TYPE $PROPERTY_NAME;" "$ENTITY_FILE"; then
  sed -i "/private String description;/a\\
\\
    @Column(nullable = $NULLABLE)\\
    private $PROPERTY_TYPE $PROPERTY_NAME;" "$ENTITY_FILE"
  echo "✅ Entity updated"
else
  echo "⚠️ Entity already has '$PROPERTY_NAME'"
fi

# -------------------------------
# 2. Create DTO
# -------------------------------
if ! grep -q "private $PROPERTY_TYPE $PROPERTY_NAME;" "$CREATE_DTO_FILE"; then
  sed -i "/private String description;/a\\
    private $PROPERTY_TYPE $PROPERTY_NAME;" "$CREATE_DTO_FILE"
  echo "✅ CreateDTO updated"
else
  echo "⚠️ CreateDTO already has '$PROPERTY_NAME'"
fi

# -------------------------------
# 3. Response DTO
# -------------------------------
if ! grep -q "private $PROPERTY_TYPE $PROPERTY_NAME;" "$RESPONSE_DTO_FILE"; then
  # Add field
  sed -i "/private String description;/a\\
    private $PROPERTY_TYPE $PROPERTY_NAME;" "$RESPONSE_DTO_FILE"

  # Add mapping in fromEntity()
  sed -i "/dto\.setDescription(${FEATURE_LOWER}\.getDescription());/a\\
            dto.set${PROP_CAP}(${FEATURE_LOWER}.get${PROP_CAP}());" "$RESPONSE_DTO_FILE"

  echo "✅ ResponseDTO updated"
else
  echo "⚠️ ResponseDTO already has '$PROPERTY_NAME'"
fi

# -------------------------------
# 4. Service Search
# -------------------------------
if ! grep -q "root\.get(\"$PROPERTY_NAME\")" "$SERVICE_FILE"; then
  sed -i "/likePattern.*description.*likePattern/a\\
                        , cb.like(cb.lower(root.get(\"$PROPERTY_NAME\")), likePattern)" "$SERVICE_FILE"
  echo "✅ Search specification updated"
else
  echo "⚠️ Search already contains '$PROPERTY_NAME'"
fi

# -------------------------------
# 5. Service Create/Update
# -------------------------------
if ! grep -q "entity\.set${PROP_CAP}" "$SERVICE_FILE"; then
  sed -i "/entity\.setDescription(request\.getDescription());/a\\
        entity.set${PROP_CAP}(request.get${PROP_CAP}());" "$SERVICE_FILE"
  echo "✅ Service mapping updated"
else
  echo "⚠️ Service already maps '$PROPERTY_NAME'"
fi

# -------------------------------
# 6. HTTP Client
# -------------------------------
if [ -f "$HTTP_FILE" ] && ! grep -q "\"$PROPERTY_NAME\":" "$HTTP_FILE"; then
  sed -i "/\"description\":/a\\
  ,\"$PROPERTY_NAME\": \"Sample ${PROPERTY_NAME^}\"" "$HTTP_FILE"
  echo "✅ HTTP client updated"
fi

echo "🎉 Property '$PROPERTY_NAME' added successfully!"