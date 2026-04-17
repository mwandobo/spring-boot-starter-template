#!/bin/bash

# ===============================
# ADD PROPERTY - ENTITY ONLY (Using cat - Git Bash Safe)
# ===============================

FEATURE=""
PROPERTY_NAME=""
PROPERTY_TYPE=""
MANDATORY="false"
PARENT=""
REFERENCE=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --feature)   FEATURE="$2";      shift 2 ;;
    --name)      PROPERTY_NAME="$2"; shift 2 ;;
    --type)      PROPERTY_TYPE="$2"; shift 2 ;;
    --mandatory) MANDATORY="$2";    shift 2 ;;
    --parent)    PARENT="$2";       shift 2 ;;
    --reference) REFERENCE="$2";    shift 2 ;;
    *)
      echo "❌ Unknown parameter: $1"
      echo "Usage: ./add-property.sh --feature position --name departmentId --type Long --mandatory true --reference department --parent administration"
      exit 1
      ;;
  esac
done

if [[ -z "$FEATURE" || -z "$PROPERTY_NAME" || -z "$PROPERTY_TYPE" ]]; then
  echo "❌ Missing required parameters"
  exit 1
fi

FEATURE_LOWER=$(echo "$FEATURE" | tr '[:upper:]' '[:lower:]')
FEATURE_UPPER="$(tr '[:lower:]' '[:upper:]' <<< ${FEATURE_LOWER:0:1})${FEATURE_LOWER:1}"

if [ -n "$PARENT" ]; then
  PARENT_LOWER=$(echo "$PARENT" | tr '[:upper:]' '[:lower:]')
  BASE_DIR="src/main/java/com/bonnysimon/starter/features/$PARENT_LOWER/$FEATURE_LOWER"
else
  BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_LOWER"
fi

ENTITY_FILE="$BASE_DIR/${FEATURE_UPPER}Entity.java"

[ ! -f "$ENTITY_FILE" ] && echo "❌ Entity file not found: $ENTITY_FILE" && exit 1

[ "$MANDATORY" = "true" ] && NULLABLE="false" || NULLABLE="true"

IS_REFERENCE=false
if [ -n "$REFERENCE" ]; then
  IS_REFERENCE=true
  REF_LOWER=$(echo "$REFERENCE" | tr '[:upper:]' '[:lower:]')
  REF_UPPER="$(tr '[:lower:]' '[:upper:]' <<< ${REF_LOWER:0:1})${REF_LOWER:1}"
fi

echo "🚀 Updating ENTITY only for property '$PROPERTY_NAME' → ${REF_UPPER}Entity"

if [ "$IS_REFERENCE" = true ]; then

  echo "   → Adding @ManyToOne relationship..."

  # 1. Add import for DepartmentEntity
  if ! grep -q "${REF_UPPER}Entity" "$ENTITY_FILE"; then
    sed -i "/import lombok.Data;/a\\
import com.bonnysimon.starter.features.${PARENT_LOWER:-}.${REF_LOWER}.${REF_UPPER}Entity;" "$ENTITY_FILE"
    echo "   ✅ Import added for ${REF_UPPER}Entity"
  fi

  # 2. Add jakarta.persistence.* import (if missing)
  if ! grep -q "import jakarta.persistence\.\*;" "$ENTITY_FILE"; then
    sed -i "/import com.bonnysimon.starter.features.*Entity;/a\\
import jakarta.persistence.*;" "$ENTITY_FILE"
    echo "   ✅ persistence import added"
  fi

  # 3. Add the field using cat (most reliable method on Git Bash)
  if ! grep -q "private ${REF_UPPER}Entity ${REF_LOWER}" "$ENTITY_FILE"; then

    # Create temporary file with the new field
    awk '
      /private String description;/ {
        print $0
        print ""
        print "    @ManyToOne(fetch = FetchType.LAZY)"
        print "    @JoinColumn(name = \"'"${PROPERTY_NAME}"'\")"
        print "    private '"${REF_UPPER}Entity ${REF_LOWER}"';"
        print ""
        next
      }
      { print }
    ' "$ENTITY_FILE" > "$ENTITY_FILE.tmp" && mv "$ENTITY_FILE.tmp" "$ENTITY_FILE"

    echo "✅ Entity successfully updated with foreign key"
  else
    echo "⚠️ Field already exists"
  fi

else
  echo "❌ This script is only for foreign key (--reference)"
fi

echo "🎉 Entity update finished!"