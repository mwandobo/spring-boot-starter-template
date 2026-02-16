#!/bin/bash

# ===============================
# Remove Feature
# ===============================

if [ -z "$1" ]; then
  echo "❌ Usage: ./remove-feature.sh <feature>"
  exit 1
fi

FEATURE_LOWER=$(echo "$1" | tr '[:upper:]' '[:lower:]')

BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_LOWER"
HTTP_FILE="http-client.http"

echo "🗑️ Removing feature: $FEATURE_LOWER"

# -------------------------------
# Remove feature source files
# -------------------------------
if [ -d "$BASE_DIR" ]; then
  rm -rf "$BASE_DIR"
  echo "✅ Deleted feature directory: $BASE_DIR"
else
  echo "⚠️ Feature directory not found — skipping"
fi

# -------------------------------
# Remove HTTP client block
# -------------------------------
if [ -f "$HTTP_FILE" ]; then
  if grep -q "^### FEATURE: $FEATURE_LOWER" "$HTTP_FILE"; then
    awk "
      BEGIN { skip=0 }
      /^### FEATURE: $FEATURE_LOWER/ { skip=1; next }
      /^### FEATURE:/ && skip==1 { skip=0 }
      skip==0 { print }
    " "$HTTP_FILE" > "$HTTP_FILE.tmp" && mv "$HTTP_FILE.tmp" "$HTTP_FILE"

    echo "✅ HTTP client entries removed"
  else
    echo "⚠️ No HTTP client entries found for feature"
  fi
else
  echo "⚠️ http-client.http not found — skipping"
fi

echo "🎉 Feature '$FEATURE_LOWER' removed successfully"
