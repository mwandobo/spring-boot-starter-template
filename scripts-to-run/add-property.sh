#!/bin/bash

# ===============================
# Add Property to Feature
# ===============================

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
    *)
      echo "❌ Unknown parameter: $1"
      exit 1
      ;;
  esac
done

# -------------------------------
# Validate
# -------------------------------
if [[ -z "$FEATURE" || -z "$PROPERTY_NAME" || -z "$PROPERTY_TYPE" ]]; then
  echo "❌ Usage:"
  echo "./add-property.sh --feature department --name code --type String --mandatory true"
  exit 1
fi

FEATURE_LOWER=$(echo "$FEATURE" | tr '[:upper:]' '[:lower:]')
FEATURE_UPPER="$(tr '[:lower:]' '[:upper:]' <<< ${FEATURE_LOWER:0:1})${FEATURE_LOWER:1}"

MANDATORY="${MANDATORY:-false}"

BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_LOWER"
ENTITY_FILE="$BASE_DIR/${FEATURE_UPPER}Entity.java"
DTO_FILE="$BASE_DIR/dto/Create${FEATURE_UPPER}DTO.java"
SERVICE_FILE="$BASE_DIR/${FEATURE_UPPER}Service.java"
HTTP_FILE="http-client.http"

# -------------------------------
# Validate files
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

# -------------------------------
# Service search
# -------------------------------
SEARCH_LINE="cb.like(cb.lower(root.get(\"$PROPERTY_NAME\")), \"%\" + search.toLowerCase() + \"%\")"

if ! grep -q "root.get(\"$PROPERTY_NAME\")" "$SERVICE_FILE"; then
  sed -i "/cb.like(cb.lower(root.get(\"description\"))/a\\
                          ,$SEARCH_LINE" "$SERVICE_FILE"

  echo "✅ Search spec extended with '$PROPERTY_NAME'"
else
  echo "⚠️ Search already contains '$PROPERTY_NAME'"
fi

# -------------------------------
# Service create/update
# -------------------------------
PROP_CAP="$(tr '[:lower:]' '[:upper:]' <<< ${PROPERTY_NAME:0:1})${PROPERTY_NAME:1}"

SETTER="entity.set${PROP_CAP}(request.get${PROP_CAP}());"

if ! grep -q "$SETTER" "$SERVICE_FILE"; then
  sed -i "/entity.setDescription/a\\
        $SETTER" "$SERVICE_FILE"
  echo "✅ Service create/update updated"
else
  echo "⚠️ Service already maps '$PROPERTY_NAME'"
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