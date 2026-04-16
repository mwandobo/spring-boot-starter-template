#!/bin/bash

# ===============================
# Remove Feature (Supports --parent)
# ===============================

FEATURE=""
PARENT=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --feature)
      FEATURE="$2"
      shift 2
      ;;
    --parent)
      PARENT="$2"
      shift 2
      ;;
    --help|-h)
      echo "Usage: ./remove-feature.sh --feature <name> [--parent <parent>]"
      echo "Example: ./remove-feature.sh --feature department --parent administration"
      exit 0
      ;;
    *)
      echo "❌ Unknown parameter: $1"
      echo "Usage: ./remove-feature.sh --feature department [--parent administration]"
      exit 1
      ;;
  esac
done

if [ -z "$FEATURE" ]; then
  echo "❌ Feature name is required"
  echo "Usage: ./remove-feature.sh --feature department [--parent administration]"
  exit 1
fi

FEATURE_LOWER=$(echo "$FEATURE" | tr '[:upper:]' '[:lower:]')
FEATURE_UPPER="$(tr '[:lower:]' '[:upper:]' <<< ${FEATURE_LOWER:0:1})${FEATURE_LOWER:1}"

# Determine correct BASE_DIR (with or without parent)
if [ -n "$PARENT" ]; then
  PARENT_LOWER=$(echo "$PARENT" | tr '[:upper:]' '[:lower:]')
  BASE_DIR="src/main/java/com/bonnysimon/starter/features/$PARENT_LOWER/$FEATURE_LOWER"
  FEATURE_PATH="$PARENT_LOWER/$FEATURE_LOWER"
else
  BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_LOWER"
  FEATURE_PATH="$FEATURE_LOWER"
fi

HTTP_FILE="http-client.http"

echo "🗑️ Removing feature: $FEATURE_UPPER"

# -------------------------------
# Remove feature source files
# -------------------------------
if [ -d "$BASE_DIR" ]; then
  rm -rf "$BASE_DIR"
  echo "✅ Deleted feature directory: $BASE_DIR"
else
  echo "⚠️ Feature directory not found: $BASE_DIR"
  echo "   (Make sure you used the correct --parent if any)"
fi

# -------------------------------
# Remove HTTP client block
# -------------------------------
if [ -f "$HTTP_FILE" ]; then
  HTTP_MARKER="### FEATURE: $FEATURE_LOWER"

  if grep -q "^$HTTP_MARKER" "$HTTP_FILE"; then
    awk "
      BEGIN { skip=0 }
      /^### FEATURE: $FEATURE_LOWER/ { skip=1; next }
      /^### FEATURE:/ && skip==1 { skip=0 }
      skip==0 { print }
    " "$HTTP_FILE" > "${HTTP_FILE}.tmp" && mv "${HTTP_FILE}.tmp" "$HTTP_FILE"

    echo "✅ HTTP client entries removed"
  else
    echo "⚠️ No HTTP client entries found for this feature"
  fi
else
  echo "⚠️ http-client.http not found — skipping"
fi

echo "🎉 Feature '$FEATURE_UPPER' removed successfully!"