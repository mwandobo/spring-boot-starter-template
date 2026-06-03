#!/bin/bash

# ================================================
# REMOVE FEATURE - Smart Naming + Enhanced Logging
# ================================================

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
      echo "Example: ./remove-feature.sh --feature \"new user\" --parent newManagement"
      exit 0
      ;;
    *)
      echo "❌ Unknown parameter: $1"
      exit 1
      ;;
  esac
done

if [ -z "$FEATURE" ]; then
  echo "❌ Feature name is required"
  exit 1
fi

# ====================== SMART NAMING ======================
to_pascal_case() {
    echo "$1" | sed -E 's/[_ -]+(.)/\U\1/g' | sed 's/^[a-z]/\U&/'
}

to_snake_case() {
    echo "$1" | sed -E 's/([a-z0-9])([A-Z])/\1_\2/g' | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/_/g' | sed 's/^_//;s/_$//'
}

RAW_FEATURE="$FEATURE"
FEATURE_PASCAL=$(to_pascal_case "$RAW_FEATURE")
FEATURE_SNAKE=$(to_snake_case "$RAW_FEATURE")

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

HTTP_FILE="http-client.http"
HTTP_MARKER="### FEATURE: $FEATURE_SNAKE"

# ====================== ENHANCED LOGGING ======================
echo "=================================================="
echo "🗑️  REMOVE FEATURE OPERATION STARTED"
echo "=================================================="
echo "Raw Feature     : $RAW_FEATURE"
echo "Resolved Feature: $FEATURE_PASCAL ($FEATURE_SNAKE)"
if [ -n "$PARENT" ]; then
echo "Raw Parent      : $RAW_PARENT"
echo "Resolved Parent : $PARENT_PASCAL ($PARENT_SNAKE)"
fi
echo "Target Path     : $BASE_DIR"
echo "HTTP Marker     : $HTTP_MARKER"
echo "=================================================="

echo "🚀 Starting removal of feature '$FEATURE_PASCAL'..."

# -------------------------------
# Remove feature source files
# -------------------------------
if [ -d "$BASE_DIR" ]; then
  rm -rf "$BASE_DIR"
  echo "✅ Successfully deleted directory: $BASE_DIR"
else
  echo "⚠️  Directory not found: $BASE_DIR"
  echo "   Please check the feature name and parent (if any)"
fi

# -------------------------------
# Remove from http-client.http
# -------------------------------
if [ -f "$HTTP_FILE" ]; then
  if grep -q "^$HTTP_MARKER" "$HTTP_FILE"; then
    awk "
      BEGIN { skip=0 }
      /^### FEATURE: $FEATURE_SNAKE/ { skip=1; next }
      /^### FEATURE:/ && skip==1 { skip=0 }
      skip==0 { print }
    " "$HTTP_FILE" > "${HTTP_FILE}.tmp" && mv "${HTTP_FILE}.tmp" "$HTTP_FILE"

    echo "✅ Removed HTTP client entries for this feature"
  else
    echo "⚠️  No HTTP client block found for this feature"
  fi
else
  echo "⚠️  http-client.http file not found — skipping"
fi

echo ""
echo "🎉 Feature '$FEATURE_PASCAL' removed successfully!"
echo "   Path: $BASE_DIR"