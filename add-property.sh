#!/bin/bash

# ===============================
# Add Property to Feature
# ===============================

if [ $# -lt 3 ]; then
  echo "❌ Usage: ./add-property.sh <feature> <propertyName> <type> [mandatory]"
  exit 1
fi

FEATURE_LOWER=$(echo "$1" | tr '[:upper:]' '[:lower:]')
FEATURE_UPPER="$(tr '[:lower:]' '[:upper:]' <<< ${FEATURE_LOWER:0:1})${FEATURE_LOWER:1}"

PROPERTY_NAME="$2"
PROPERTY_TYPE="$3"
MANDATORY="${4:-false}"

BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_LOWER"
ENTITY_FILE="$BASE_DIR/${FEATURE_UPPER}Entity.java"
DTO_FILE="$BASE_DIR/dto/Create${FEATURE_UPPER}DTO.java"
SERVICE_FILE="$BASE_DIR/${FEATURE_UPPER}Service.java"
HTTP_FILE="http-client.http"

# -------------------------------
# Validate
# -------------------------------
for f in "$ENTITY_FILE" "$DTO_FILE" "$SERVICE_FILE"; do
  [ ! -f "$f" ] && echo "❌ Missing file: $f" && exit 1
done

# -------------------------------
# Column nullable
# -------------------------------
[ "$MANDATORY" = "true" ] && NULLABLE="false" || NULLABLE="true"

# -------------------------------
# Entity
# -------------------------------
if ! grep -q "private $PROPERTY_TYPE $PROPERTY_NAME;" "$ENTITY_FILE"; then
  sed -i "/^}/i\\
    @Column(nullable = $NULLABLE)\n\
    private $PROPERTY_TYPE $PROPERTY_NAME;\n" "$ENTITY_FILE"
  echo "✅ Entity updated"
else
  echo "⚠️ Entity already has '$PROPERTY_NAME'"
fi

# -------------------------------
# DTO
# -------------------------------
if ! grep -q "private $PROPERTY_TYPE $PROPERTY_NAME;" "$DTO_FILE"; then
  sed -i "/^}/i\\
    private $PROPERTY_TYPE $PROPERTY_NAME;\n" "$DTO_FILE"
  echo "✅ DTO updated"
else
  echo "⚠️ DTO already has '$PROPERTY_NAME'"
fi

## -------------------------------
## Service (create + update)
## -------------------------------
#SETTER="entity.set$(tr '[:lower:]' '[:upper:]' <<< ${PROPERTY_NAME:0:1})${PROPERTY_NAME:1}(request.get$(tr '[:lower:]' '[:upper:]' <<< ${PROPERTY_NAME:0:1})${PROPERTY_NAME:1}());"
#
#if ! grep -q "$SETTER" "$SERVICE_FILE"; then
#  sed -i "/entity.setDescription/a\\
#        $SETTER" "$SERVICE_FILE"
#  echo "✅ Service updated"
#else
#  echo "⚠️ Service already maps '$PROPERTY_NAME'"
#fi

# -------------------------------
# Service (create + update + search)
# -------------------------------
SETTER="entity.set$(tr '[:lower:]' '[:upper:]' <<< ${PROPERTY_NAME:0:1})${PROPERTY_NAME:1}(request.get$(tr '[:lower:]' '[:upper:]' <<< ${PROPERTY_NAME:0:1})${PROPERTY_NAME:1}());"

if ! grep -q "$SETTER" "$SERVICE_FILE"; then
  sed -i "/entity.setDescription/a\\
        $SETTER" "$SERVICE_FILE"
  echo "✅ Service create/update updated"
else
  echo "⚠️ Service already maps '$PROPERTY_NAME'"
fi

# ---- search specification ----
#if ! grep -q "root.get(\"$PROPERTY_NAME\")" "$SERVICE_FILE"; then
#  sed -i "/cb.like(cb.lower(root.get(\"description\"))/{
#    s/)/),\\
#                        $SEARCH_LINE/
#  }" "$SERVICE_FILE"
#  echo "✅ Service search updated"
#else
#  echo "⚠️ Service search already includes '$PROPERTY_NAME'"
#fi

#if ! grep -q "root.get(\"$PROPERTY_NAME\")" "$SERVICE_FILE"; then
#  sed -i "/cb.like(cb.lower(root.get(\"description\"))/{
#    s/)/),\\
#                    $SEARCH_EXPR/
#  }" "$SERVICE_FILE"
#  echo "✅ Service search updated"
#else
#  echo "⚠️ Service search already contains '$PROPERTY_NAME'"
#fi

#SEARCH_LINE="cb.like(cb.lower(root.get(\"$PROPERTY_NAME\")), \"%\" + search.toLowerCase() + \"%\")"
#
#if ! grep -q "root.get(\"$PROPERTY_NAME\")" "$SERVICE_FILE"; then
#  sed -i "/cb.or(/,/)/{
#    /cb.like(cb.lower(root.get(\"description\")))/ s/)/),\
#                          $SEARCH_LINE/
#  }" "$SERVICE_FILE"
#
#  echo "✅ Search spec extended with '$PROPERTY_NAME'"
#else
#  echo "⚠️ Search already contains '$PROPERTY_NAME'"
#fi

#SEARCH_LINE="cb.like(cb.lower(root.get(\"$PROPERTY_NAME\")), \"%\" + search.toLowerCase() + \"%\")"
#
#if ! grep -q "root.get(\"$PROPERTY_NAME\")" "$SERVICE_FILE"; then
#  sed -i "/cb.or(/,/)/{
#    /)/{
#      s/)/,\n                          $SEARCH_LINE\n                  )/
#      b
#    }
#  }" "$SERVICE_FILE"
#
#  echo "✅ Search spec extended with '$PROPERTY_NAME'"
#else
#  echo "⚠️ Search already contains '$PROPERTY_NAME'"
#fi


SEARCH_LINE="cb.like(cb.lower(root.get(\"$PROPERTY_NAME\")), \"%\" + search.toLowerCase() + \"%\")"

if ! grep -q "root.get(\"$PROPERTY_NAME\")" "$SERVICE_FILE"; then
  sed -i "/cb.or(/,/)/{
    /cb.like(cb.lower(root.get(\"description\"))/a\\
                          ,$SEARCH_LINE
  }" "$SERVICE_FILE"

  echo "✅ Search spec extended with '$PROPERTY_NAME'"
else
  echo "⚠️ Search already contains '$PROPERTY_NAME'"
fi




# -------------------------------
# HTTP client
# -------------------------------
HTTP_MARKER="### FEATURE:"

if grep -q "$HTTP_MARKER" "$HTTP_FILE"; then
  if ! grep -q "\"$PROPERTY_NAME\":" "$HTTP_FILE"; then
    sed -i "/\"description\"/a\\
  ,\"$PROPERTY_NAME\": \"Sample ${PROPERTY_NAME^}\"" "$HTTP_FILE"
    echo "✅ HTTP client updated"
  else
    echo "⚠️ HTTP already contains '$PROPERTY_NAME'"
  fi
fi

echo "🎉 Property '$PROPERTY_NAME' added to feature '$FEATURE_UPPER'"
