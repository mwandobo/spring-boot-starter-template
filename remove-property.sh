#!/bin/bash

# ===============================
# Remove Property from Feature
# ===============================

if [ $# -lt 2 ]; then
  echo "❌ Usage: ./remove-property.sh <feature> <propertyName>"
  exit 1
fi

FEATURE_LOWER=$(echo "$1" | tr '[:upper:]' '[:lower:]')
FEATURE_UPPER="$(tr '[:lower:]' '[:upper:]' <<< ${FEATURE_LOWER:0:1})${FEATURE_LOWER:1}"

PROPERTY_NAME="$2"
PROPERTY_CAMEL="$(tr '[:lower:]' '[:upper:]' <<< ${PROPERTY_NAME:0:1})${PROPERTY_NAME:1}"

BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_LOWER"
ENTITY_FILE="$BASE_DIR/${FEATURE_UPPER}Entity.java"
DTO_FILE="$BASE_DIR/dto/Create${FEATURE_UPPER}DTO.java"
SERVICE_FILE="$BASE_DIR/${FEATURE_UPPER}Service.java"
HTTP_FILE="http-client.http"

echo "🧹 Removing property '$PROPERTY_NAME' from feature '$FEATURE_UPPER'"

# -------------------------------
# Entity (remove @Column + field)
# -------------------------------
if [ -f "$ENTITY_FILE" ]; then
  # Remove @Column annotation immediately before the field
  sed -i "/@Column/{N;/\n[[:space:]]*private .* $PROPERTY_NAME;/d;}" "$ENTITY_FILE"

  # Remove field if still present
  sed -i "/private .* $PROPERTY_NAME;/d" "$ENTITY_FILE"

  echo "✅ Entity updated"
fi

# -------------------------------
# DTO
# -------------------------------
if [ -f "$DTO_FILE" ]; then
  sed -i "/private .* $PROPERTY_NAME;/d" "$DTO_FILE"
  echo "✅ DTO updated"
fi

# -------------------------------
# Service
# -------------------------------
if [ -f "$SERVICE_FILE" ]; then
  sed -i "/entity.set$PROPERTY_CAMEL(request.get$PROPERTY_CAMEL());/d" "$SERVICE_FILE"
  echo "✅ Service updated"
fi

# -------------------------------
# HTTP Client
# -------------------------------
if [ -f "$HTTP_FILE" ]; then
  sed -i "/\"$PROPERTY_NAME\"/d" "$HTTP_FILE"
  echo "✅ HTTP client updated"
fi

echo "🎉 Property '$PROPERTY_NAME' fully removed"
