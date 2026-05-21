#!/bin/bash

# ================================================
# Spring Boot Feature Generator - Smart Naming + Enhanced Logging
# ================================================

FEATURE_NAME=""
PLURAL_SUFFIX=""
PARENT=""

while [[ "$#" -gt 0 ]]; do
  case $1 in
    --name)
      FEATURE_NAME="$2"
      shift 2
      ;;
    --plural)
      PLURAL_SUFFIX="$2"
      shift 2
      ;;
    --parent)
      PARENT="$2"
      shift 2
      ;;
    --help|-h)
      echo "Usage: ./generate-feature.sh --name <FeatureName> [--plural s|es|ies] [--parent <parent>]"
      echo "Example: ./generate-feature.sh --name \"new user\" --parent newManagement"
      exit 0
      ;;
    *)
      echo "❌ Unknown parameter: $1"
      exit 1
      ;;
  esac
done

if [ -z "$FEATURE_NAME" ]; then
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

to_kebab_case() {
    echo "$1" | sed -E 's/([a-z0-9])([A-Z])/\1-\2/g' | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/-/g' | sed 's/^-//;s/-$//'
}

RAW_INPUT="$FEATURE_NAME"
FEATURE_PASCAL=$(to_pascal_case "$RAW_INPUT")
FEATURE_SNAKE=$(to_snake_case "$RAW_INPUT")
FEATURE_KEBAB=$(to_kebab_case "$RAW_INPUT")

# Parent Resolution
if [ -n "$PARENT" ]; then
    PARENT_RAW="$PARENT"
    PARENT_SNAKE=$(to_snake_case "$PARENT")
    PARENT_PASCAL=$(to_pascal_case "$PARENT")
else
    PARENT_SNAKE=""
    PARENT_PASCAL=""
fi

# Plural handling for API Route only
if [ -n "$PLURAL_SUFFIX" ]; then
    case "$PLURAL_SUFFIX" in
        s)   FEATURE_PLURAL_KEBAB="${FEATURE_KEBAB}s" ;;
        es)  FEATURE_PLURAL_KEBAB="${FEATURE_KEBAB}es" ;;
        ies)
            if [[ "$FEATURE_KEBAB" == *y ]]; then
                FEATURE_PLURAL_KEBAB="${FEATURE_KEBAB%y}ies"
            else
                FEATURE_PLURAL_KEBAB="${FEATURE_KEBAB}es"
            fi ;;
        *)   FEATURE_PLURAL_KEBAB="${FEATURE_KEBAB}s" ;;
    esac
else
    FEATURE_PLURAL_KEBAB="${FEATURE_KEBAB}s"
fi

# ====================== ENHANCED LOGGING ======================
echo "=================================================="
echo "🚀 Feature Generation Started"
echo "=================================================="
echo "Raw Input       : $RAW_INPUT"
echo "Feature Name    : $FEATURE_PASCAL"
echo "Folder Name     : $FEATURE_SNAKE"
if [ -n "$PARENT" ]; then
echo "Parent Raw      : $PARENT_RAW"
echo "Parent Resolved : $PARENT_PASCAL ($PARENT_SNAKE)"
echo "Base Location   : features/$PARENT_SNAKE/$FEATURE_SNAKE"
else
echo "Parent          : (None - Root level)"
fi
echo "API Route       : /api/v1/${FEATURE_PLURAL_KEBAB}"
echo "Plural Strategy : ${PLURAL_SUFFIX:-default (s)}"
echo "=================================================="

# ====================== SETUP PATHS ======================
BASE_PACKAGE="com.bonnysimon.starter.features"
if [ -n "$PARENT" ]; then
    BASE_PACKAGE="$BASE_PACKAGE.$PARENT_SNAKE"
    BASE_DIR="src/main/java/com/bonnysimon/starter/features/$PARENT_SNAKE/$FEATURE_SNAKE"
else
    BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_SNAKE"
fi

mkdir -p "$BASE_DIR/dto"

echo "📁 Creating files in: $BASE_DIR"
echo ""

# ... [Rest of your script remains the same - Entity, Repository, DTOs, Service, Controller]

# (Keep the rest of your generation code unchanged from here onwards)