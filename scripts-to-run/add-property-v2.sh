#!/bin/bash

# ===============================
# ADD PROPERTY - Full (Entity + DTOs)
# Entity part is exactly the one you said is perfect
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
      echo "Usage: ./add-property.sh --feature position --name department_id --type Long --mandatory true --reference department --parent administration"
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
PROP_CAP="$(tr '[:lower:]' '[:upper:]' <<< ${PROPERTY_NAME:0:1})${PROPERTY_NAME:1}"

if [ -n "$PARENT" ]; then
  PARENT_LOWER=$(echo "$PARENT" | tr '[:upper:]' '[:lower:]')
  BASE_DIR="src/main/java/com/bonnysimon/starter/features/$PARENT_LOWER/$FEATURE_LOWER"
else
  BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_LOWER"
fi

ENTITY_FILE="$BASE_DIR/${FEATURE_UPPER}Entity.java"
CREATE_DTO_FILE="$BASE_DIR/dto/Create${FEATURE_UPPER}DTO.java"
RESPONSE_DTO_FILE="$BASE_DIR/dto/${FEATURE_UPPER}ResponseDTO.java"

# Validate files
for f in "$ENTITY_FILE" "$CREATE_DTO_FILE" "$RESPONSE_DTO_FILE"; do
  [ ! -f "$f" ] && echo "❌ File not found: $f" && exit 1
done

[ "$MANDATORY" = "true" ] && NULLABLE="false" || NULLABLE="true"

IS_REFERENCE=false
if [ -n "$REFERENCE" ]; then
  IS_REFERENCE=true
  REF_LOWER=$(echo "$REFERENCE" | tr '[:upper:]' '[:lower:]')
  REF_UPPER="$(tr '[:lower:]' '[:upper:]' <<< ${REF_LOWER:0:1})${REF_LOWER:1}"
fi

echo "🚀 Adding property '$PROPERTY_NAME' to '$FEATURE_UPPER'"

# ================================================================
# 1. ENTITY - Exactly the version you said is perfect (unchanged)
# ================================================================
if [ "$IS_REFERENCE" = true ]; then
  echo "   → Entity: Keeping your perfect implementation (no changes)"

  if ! grep -q "private ${REF_UPPER}Entity ${REF_LOWER}" "$ENTITY_FILE"; then
    # 1. Add import
    if ! grep -q "${REF_UPPER}Entity" "$ENTITY_FILE"; then
      sed -i "/import lombok.Data;/a\\
import com.bonnysimon.starter.features.${PARENT_LOWER:-}.${REF_LOWER}.${REF_UPPER}Entity;" "$ENTITY_FILE"
    fi

    # 2. Add jakarta.persistence.* if missing
    if ! grep -q "import jakarta.persistence\.\*;" "$ENTITY_FILE"; then
      sed -i "/import com.bonnysimon.starter.features.*Entity;/a\\
import jakarta.persistence.*;" "$ENTITY_FILE"
    fi

    # 3. Add field using awk (your working method)
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

    echo "✅ Entity updated with foreign key"
  else
    echo "   Entity already has the relationship"
  fi
fi

# ================================================================
# 2. Create DTO - Add departmentId (Long)
# ================================================================
if [ "$IS_REFERENCE" = true ]; then
  if ! grep -q "private $PROPERTY_TYPE $PROPERTY_NAME" "$CREATE_DTO_FILE"; then
    sed -i "/private String description;/a\\
    private $PROPERTY_TYPE $PROPERTY_NAME;" "$CREATE_DTO_FILE"
    echo "✅ CreateDTO updated → added $PROPERTY_NAME"
  else
    echo "⚠️ CreateDTO already has $PROPERTY_NAME"
  fi
fi

# ================================================================
# 3. Response DTO - Add nested DepartmentResponseDTO + correct mapping
# ================================================================
if [ "$IS_REFERENCE" = true ]; then

  # Add import
  if ! grep -q "${REF_UPPER}ResponseDTO" "$RESPONSE_DTO_FILE"; then
    sed -i "/import lombok.Data;/a\\
import com.bonnysimon.starter.features.${PARENT_LOWER:-}.${REF_LOWER}.dto.${REF_UPPER}ResponseDTO;" "$RESPONSE_DTO_FILE"
    echo "   ✅ Import added for ${REF_UPPER}ResponseDTO"
  fi

  # Add field
  if ! grep -q "private ${REF_UPPER}ResponseDTO ${REF_LOWER}" "$RESPONSE_DTO_FILE"; then
    sed -i "/private String description;/a\\
    private ${REF_UPPER}ResponseDTO ${REF_LOWER};" "$RESPONSE_DTO_FILE"
    echo "✅ ResponseDTO updated → added ${REF_LOWER} (nested DTO)"
  fi

  # Add correct mapping in fromEntity()
  if ! grep -q "set${REF_UPPER}(" "$RESPONSE_DTO_FILE"; then
    sed -i "/setDepartment_id/d" "$RESPONSE_DTO_FILE" 2>/dev/null || true
    sed -i "/dto\.setDescription(${FEATURE_LOWER}\.getDescription());/a\\
            dto.set${REF_UPPER}(${FEATURE_LOWER}.get${REF_UPPER}() != null ? ${REF_UPPER}ResponseDTO.fromEntity(${FEATURE_LOWER}.get${REF_UPPER}()) : null);" "$RESPONSE_DTO_FILE"
    echo "✅ ResponseDTO.fromEntity() mapping added correctly"
  fi
fi






# ================================================================
# 4. SERVICE IMPLEMENTATION - Returns Entity
# ================================================================
if [ "$IS_REFERENCE" = true ]; then

  echo "   → Updating Service..."

  SERVICE_FILE="$BASE_DIR/${FEATURE_UPPER}Service.java"

  # Add import
  if ! grep -q "${REF_UPPER}Repository" "$SERVICE_FILE"; then
    sed -i "/import .*${FEATURE_UPPER}Entity;/a\\
import com.bonnysimon.starter.features.${PARENT_LOWER:-}.${REF_LOWER}.${REF_UPPER}Repository;" "$SERVICE_FILE"
    echo "✅ Imported ${REF_UPPER}Repository"
  fi

  # Add repository field
  if ! grep -q "${REF_UPPER}Repository ${REF_LOWER}Repository" "$SERVICE_FILE"; then
    sed -i "/private final ${FEATURE_UPPER}Repository repository;/a\\
    private final ${REF_UPPER}Repository ${REF_LOWER}Repository;" "$SERVICE_FILE"
    echo "✅ Injected ${REF_UPPER}Repository"
  fi

  # Add validation method that RETURNS the entity
  if ! grep -q "validate${REF_UPPER}Exists" "$SERVICE_FILE"; then
    sed -i '$i\
\
    private '"${REF_UPPER}"'Entity validate'"${REF_UPPER}"'Exists(Long id) {\
        if (id == null) {\
            if ("'"$NULLABLE"'" == "false") {\
                throw new IllegalArgumentException("'"${REF_UPPER}"' ID is required");\
            }\
            return null;\
        }\
        return '"${REF_LOWER}"'Repository.findById(id)\
                .orElseThrow(() -> new IllegalStateException("'"${REF_UPPER}"' not found with id: " + id));\
    }\
' "$SERVICE_FILE"
    echo "✅ Added validate${REF_UPPER}Exists() method (returns entity)"
  fi

  # Fix getter name and update validation calls to use the returned entity
  sed -i "s/getDepartment_id/get${PROP_CAP}/g" "$SERVICE_FILE"

  # Replace the old validation call with entity assignment + setDepartment
  sed -i "/entity\.setDescription(request\.getDescription());/a\\
        ${REF_LOWER}Entity ${REF_LOWER} = validate${REF_UPPER}Exists(request.get${PROP_CAP}());" "$SERVICE_FILE"

  sed -i "/${REF_LOWER}Entity ${REF_LOWER} = validate${REF_UPPER}Exists(request.get${PROP_CAP}());/a\\
        entity.set${REF_UPPER}(${REF_LOWER});" "$SERVICE_FILE"

  echo "✅ Added foreign key validation + entity assignment in create() and update()"
fi













## ================================================================
## 4. SERVICE IMPLEMENTATION
## ================================================================
#if [ "$IS_REFERENCE" = true ]; then
#
#  echo "   → Updating Service..."
#
#  SERVICE_FILE="$BASE_DIR/${FEATURE_UPPER}Service.java"
#
#  # Add import
#  if ! grep -q "${REF_UPPER}Repository" "$SERVICE_FILE"; then
#    sed -i "/import .*${FEATURE_UPPER}Entity;/a\\
#import com.bonnysimon.starter.features.${PARENT_LOWER:-}.${REF_LOWER}.${REF_UPPER}Repository;" "$SERVICE_FILE"
#    echo "✅ Imported ${REF_UPPER}Repository"
#  fi
#
#  # Add repository field
#  if ! grep -q "${REF_UPPER}Repository ${REF_LOWER}Repository" "$SERVICE_FILE"; then
#    sed -i "/private final ${FEATURE_UPPER}Repository repository;/a\\
#    private final ${REF_UPPER}Repository ${REF_LOWER}Repository;" "$SERVICE_FILE"
#    echo "✅ Injected ${REF_UPPER}Repository"
#  fi
#
#  # Add validation method inside the class
#  if ! grep -q "validate${REF_UPPER}Exists" "$SERVICE_FILE"; then
#    awk '
#      /    public void delete\(Long id, boolean soft\)/ {
#        print $0
#        print ""
#        print "    private void validate'"${REF_UPPER}"'Exists(Long id) {"
#        print "        if (id == null) {"
#        print "            if (\"'"$NULLABLE"'\" == \"false\") {"
#        print "                throw new IllegalArgumentException(\""'"${REF_UPPER}"' ID is required\");"
#        print "            }"
#        print "            return;"
#        print "        }"
#        print "        '"${REF_LOWER}"'Repository.findById(id)"
#        print "                .orElseThrow(() -> new IllegalStateException(\""'"${REF_UPPER}"' not found with id: \" + id));"
#        print "    }"
#        print ""
#        next
#      }
#      { print }
#    ' "$SERVICE_FILE" > "$SERVICE_FILE.tmp" && mv "$SERVICE_FILE.tmp" "$SERVICE_FILE"
#    echo "✅ Added validate${REF_UPPER}Exists() method"
#  fi
#
#  # Fix getter and add validation calls
#  sed -i "s/getDepartment_id/get${PROP_CAP}/g" "$SERVICE_FILE"
#  sed -i "/entity\.setDescription(request\.getDescription());/a\\
#        validate${REF_UPPER}Exists(request.get${PROP_CAP}());" "$SERVICE_FILE"
#
#  echo "✅ Added foreign key validation in create() and update()"
#fi



## ================================================================
## 4. SERVICE IMPLEMENTATION
## ================================================================
#if [ "$IS_REFERENCE" = true ]; then
#
#  echo "   → Updating Service..."
#
#  SERVICE_FILE="$BASE_DIR/${FEATURE_UPPER}Service.java"
#
#  # Add import
#  if ! grep -q "${REF_UPPER}Repository" "$SERVICE_FILE"; then
#    sed -i "/import .*${FEATURE_UPPER}Entity;/a\\
#import com.bonnysimon.starter.features.${PARENT_LOWER:-}.${REF_LOWER}.${REF_UPPER}Repository;" "$SERVICE_FILE"
#    echo "✅ Imported ${REF_UPPER}Repository"
#  fi
#
#  # Add repository field
#  if ! grep -q "${REF_UPPER}Repository ${REF_LOWER}Repository" "$SERVICE_FILE"; then
#    sed -i "/private final ${FEATURE_UPPER}Repository repository;/a\\
#    private final ${REF_UPPER}Repository ${REF_LOWER}Repository;" "$SERVICE_FILE"
#    echo "✅ Injected ${REF_UPPER}Repository"
#  fi
#
#  # Add validation method using cat (simple and reliable)
#  if ! grep -q "validate${REF_UPPER}Exists" "$SERVICE_FILE"; then
#    cat << EOF >> "$SERVICE_FILE"
#
#    private void validate${REF_UPPER}Exists(Long id) {
#        if (id == null) {
#            if ("$NULLABLE" == "false") {
#                throw new IllegalArgumentException("${REF_UPPER} ID is required");
#            }
#            return;
#        }
#        ${REF_LOWER}Repository.findById(id)
#                .orElseThrow(() -> new IllegalStateException("${REF_UPPER} not found with id: " + id));
#    }
#EOF
#    echo "✅ Added validate${REF_UPPER}Exists() method"
#  fi
#
#  # Fix getter name and add validation calls
#  sed -i "s/getDepartment_id/get${PROP_CAP}/g" "$SERVICE_FILE"
#  sed -i "/entity\.setDescription(request\.getDescription());/a\\
#        validate${REF_UPPER}Exists(request.get${PROP_CAP}());" "$SERVICE_FILE"
#
#  echo "✅ Added foreign key validation in create() and update()"
#fi
#
#







#
#
## ================================================================
## 4. SERVICE IMPLEMENTATION (Smart Insertion)
## ================================================================
#if [ "$IS_REFERENCE" = true ]; then
#
#  echo "   → Updating Service..."
#
#  SERVICE_FILE="$BASE_DIR/${FEATURE_UPPER}Service.java"
#
#  # Add import
#  if ! grep -q "${REF_UPPER}Repository" "$SERVICE_FILE"; then
#    sed -i "/import .*${FEATURE_UPPER}Entity;/a\\
#import com.bonnysimon.starter.features.${PARENT_LOWER:-}.${REF_LOWER}.${REF_UPPER}Repository;" "$SERVICE_FILE"
#    echo "✅ Imported ${REF_UPPER}Repository"
#  fi
#
#  # Add repository field
#  if ! grep -q "${REF_UPPER}Repository ${REF_LOWER}Repository" "$SERVICE_FILE"; then
#    sed -i "/private final ${FEATURE_UPPER}Repository repository;/a\\
#    private final ${REF_UPPER}Repository ${REF_LOWER}Repository;" "$SERVICE_FILE"
#    echo "✅ Injected ${REF_UPPER}Repository"
#  fi
#
#  # Add validation method just before the last } of the class
#  if ! grep -q "validate${REF_UPPER}Exists" "$SERVICE_FILE"; then
#    # Insert before the final closing brace of the class
#    sed -i '$i\
#\
#    private void validate'"${REF_UPPER}"'Exists(Long id) {\
#        if (id == null) {\
#            if ("'"$NULLABLE"'" == "false") {\
#                throw new IllegalArgumentException("'"${REF_UPPER}"' ID is required");\
#            }\
#            return;\
#        }\
#        '"${REF_LOWER}"'Repository.findById(id)\
#                .orElseThrow(() -> new IllegalStateException("'"${REF_UPPER}"' not found with id: " + id));\
#    }\
#
#' "$SERVICE_FILE"
#    echo "✅ Added validate${REF_UPPER}Exists() method inside class"
#  fi
#
#  # Fix getter name and ensure validation call
#  sed -i "s/getDepartment_id/get${PROP_CAP}/g" "$SERVICE_FILE"
#  sed -i "/entity\.setDescription(request\.getDescription());/a\\
#        validate${REF_UPPER}Exists(request.get${PROP_CAP}());" "$SERVICE_FILE"
#  sed -i "/validate${REF_UPPER}Exists(request.get${PROP_CAP}());/a\\
#          entity.set${REF_UPPER}(request.get${PROP_CAP}() != null ? ${REF_LOWER}Repository.getReferenceById(request.get${PROP_CAP}()) : null);" "$SERVICE_FILE"
#
#
#  echo "✅ Added foreign key validation in create() and update()"
#fi
#





#
## ================================================================
## 4. SERVICE IMPLEMENTATION - Returns Entity (Clean Version)
## ================================================================
#if [ "$IS_REFERENCE" = true ]; then
#
#  echo "   → Updating Service..."
#
#  SERVICE_FILE="$BASE_DIR/${FEATURE_UPPER}Service.java"
#
#  # 1. Add import for Repository
#  if ! grep -q "${REF_UPPER}Repository" "$SERVICE_FILE"; then
#    sed -i "/import .*${FEATURE_UPPER}Entity;/a\\
#import com.bonnysimon.starter.features.${PARENT_LOWER:-}.${REF_LOWER}.${REF_UPPER}Repository;" "$SERVICE_FILE"
#    echo "✅ Imported ${REF_UPPER}Repository"
#  fi
#
#  # 2. Add import for the referenced Entity (DepartmentEntity)
#  if ! grep -q "${REF_UPPER}Entity" "$SERVICE_FILE"; then
#    sed -i "/import .*${FEATURE_UPPER}Entity;/a\\
#import com.bonnysimon.starter.features.${PARENT_LOWER:-}.${REF_LOWER}.${REF_UPPER}Entity;" "$SERVICE_FILE"
#    echo "✅ Imported ${REF_UPPER}Entity"
#  fi
#
#  # 3. Add repository field
#  if ! grep -q "${REF_UPPER}Repository ${REF_LOWER}Repository" "$SERVICE_FILE"; then
#    sed -i "/private final ${FEATURE_UPPER}Repository repository;/a\\
#    private final ${REF_UPPER}Repository ${REF_LOWER}Repository;" "$SERVICE_FILE"
#    echo "✅ Injected ${REF_UPPER}Repository"
#  fi
#
#  # 4. Add validation method that RETURNS the entity (placed before last })
#  if ! grep -q "validate${REF_UPPER}Exists" "$SERVICE_FILE"; then
#    awk '
#      /    public void delete\(Long id, boolean soft\)/ {
#        print $0
#        print ""
#        print "    private '"${REF_UPPER}"'Entity validate'"${REF_UPPER}"'Exists(Long id) {"
#        print "        if (id == null) {"
#        print "            if (\"'"$NULLABLE"'\" == \"false\") {"
#        print "                throw new IllegalArgumentException(\""'"${REF_UPPER}"' ID is required\");"
#        print "            }"
#        print "            return null;"
#        print "        }"
#        print "        return '"${REF_LOWER}"'Repository.findById(id)"
#        print "                .orElseThrow(() -> new IllegalStateException(\""'"${REF_UPPER}"' not found with id: \" + id));"
#        print "    }"
#        print ""
#        next
#      }
#      { print }
#    ' "$SERVICE_FILE" > "$SERVICE_FILE.tmp" && mv "$SERVICE_FILE.tmp" "$SERVICE_FILE"
#    echo "✅ Added validate${REF_UPPER}Exists() method (returns entity)"
#  fi
#
#  # 5. Fix getter and add proper usage in create() and update()
#  sed -i "s/getDepartment_id/get${PROP_CAP}/g" "$SERVICE_FILE"
#
#  # Replace old validation calls with proper entity assignment
#  sed -i "/entity\.setDescription(request\.getDescription());/a\\
#        ${REF_LOWER}Entity ${REF_LOWER} = validate${REF_UPPER}Exists(request.get${PROP_CAP}());" "$SERVICE_FILE"
#
#  sed -i "/${REF_LOWER}Entity ${REF_LOWER} = validate${REF_UPPER}Exists(request.get${PROP_CAP}());/a\\
#        entity.set${REF_UPPER}(${REF_LOWER});" "$SERVICE_FILE"
#
#  echo "✅ Added foreign key validation + entity assignment in create() and update()"
#fi



## ================================================================
## 4. SERVICE IMPLEMENTATION - Fixed Import + Clean Logic
## ================================================================
#if [ "$IS_REFERENCE" = true ]; then
#
#  echo "   → Updating Service..."
#
#  SERVICE_FILE="$BASE_DIR/${FEATURE_UPPER}Service.java"
#
#  # Add import for Repository
#  if ! grep -q "${REF_UPPER}Repository" "$SERVICE_FILE"; then
#    sed -i "/import .*Repository;/a\\
#import com.bonnysimon.starter.features.${PARENT_LOWER:-}.${REF_LOWER}.${REF_UPPER}Repository;" "$SERVICE_FILE"
#    echo "✅ Imported ${REF_UPPER}Repository"
#  fi
#
#  # Add import for Entity (DepartmentEntity) - More reliable pattern
#  if ! grep -q "${REF_UPPER}Entity" "$SERVICE_FILE"; then
#    sed -i "/import .*PositionEntity;/a\\
#import com.bonnysimon.starter.features.${PARENT_LOWER:-}.${REF_LOWER}.${REF_UPPER}Entity;" "$SERVICE_FILE"
#    echo "✅ Imported ${REF_UPPER}Entity"
#  fi
#
#  # Add repository field
#  if ! grep -q "${REF_UPPER}Repository ${REF_LOWER}Repository" "$SERVICE_FILE"; then
#    sed -i "/private final ${FEATURE_UPPER}Repository repository;/a\\
#    private final ${REF_UPPER}Repository ${REF_LOWER}Repository;" "$SERVICE_FILE"
#    echo "✅ Injected ${REF_UPPER}Repository"
#  fi
#
#  # Add validation method (returns entity) - placed before last }
#  if ! grep -q "validate${REF_UPPER}Exists" "$SERVICE_FILE"; then
#    awk '
#      /    public void delete\(Long id, boolean soft\)/ {
#        print $0
#        print ""
#        print "    private '"${REF_UPPER}"'Entity validate'"${REF_UPPER}"'Exists(Long id) {"
#        print "        if (id == null) {"
#        print "            if (\"'"$NULLABLE"'\" == \"false\") {"
#        print "                throw new IllegalArgumentException(\""'"${REF_UPPER}"' ID is required\");"
#        print "            }"
#        print "            return null;"
#        print "        }"
#        print "        return '"${REF_LOWER}"'Repository.findById(id)"
#        print "                .orElseThrow(() -> new IllegalStateException(\""'"${REF_UPPER}"' not found with id: \" + id));"
#        print "    }"
#        print ""
#        next
#      }
#      { print }
#    ' "$SERVICE_FILE" > "$SERVICE_FILE.tmp" && mv "$SERVICE_FILE.tmp" "$SERVICE_FILE"
#    echo "✅ Added validate${REF_UPPER}Exists() method"
#  fi
#
#  # Clean up any duplicate or wrong lines
#  sed -i '/departmentEntity department =/d' "$SERVICE_FILE"
#  sed -i "s/getDepartment_id/get${PROP_CAP}/g" "$SERVICE_FILE"
#
#  # Add clean entity assignment
#  sed -i "/entity\.setDescription(request\.getDescription());/a\\
#        ${REF_LOWER}Entity ${REF_LOWER} = validate${REF_UPPER}Exists(request.get${PROP_CAP}());" "$SERVICE_FILE"
#
#  sed -i "/${REF_LOWER}Entity ${REF_LOWER} = validate${REF_UPPER}Exists(request.get${PROP_CAP}());/a\\
#        entity.set${REF_UPPER}(${REF_LOWER});" "$SERVICE_FILE"
#
#  echo "✅ Added foreign key validation + entity assignment"
#fi



# ================================================================
# 4. SERVICE IMPLEMENTATION - CLEAN + SAFE + NO DUPLICATES
# ================================================================
if [ "$IS_REFERENCE" = true ]; then

  echo "   → Updating Service..."

  SERVICE_FILE="$BASE_DIR/${FEATURE_UPPER}Service.java"

  PACKAGE_PATH="com.bonnysimon.starter.features.${PARENT_LOWER:+$PARENT_LOWER.}${REF_LOWER}"

  # ============================================================
  # 1. IMPORTS (SAFE)
  # ============================================================

#  # Import Repository
#  if ! grep -q "import .*${REF_UPPER}Repository;" "$SERVICE_FILE"; then
#    sed -i "/^import /a\\
#import ${PACKAGE_PATH}.${REF_UPPER}Repository;" "$SERVICE_FILE"
#    echo "✅ Imported ${REF_UPPER}Repository"
#  fi
#
#  # Import Entity
#  if ! grep -q "import .*${REF_UPPER}Entity;" "$SERVICE_FILE"; then
#    sed -i "/^import /a\\
#import ${PACKAGE_PATH}.${REF_UPPER}Entity;" "$SERVICE_FILE"
#    echo "✅ Imported ${REF_UPPER}Entity"
#  fi


# ============================================================
# IMPORTS (INSERT ONLY ONCE, CLEAN)
# ============================================================

PACKAGE_PATH="com.bonnysimon.starter.features.${PARENT_LOWER:+$PARENT_LOWER.}${REF_LOWER}"

# Remove duplicate imports first (cleanup existing mess)
sed -i "/import ${PACKAGE_PATH//\//\\/}\.${REF_UPPER}Entity;/d" "$SERVICE_FILE"

# Add Entity import once (after package line)
if ! grep -q "import ${PACKAGE_PATH}.${REF_UPPER}Entity;" "$SERVICE_FILE"; then
  sed -i "/^package /a\\
import ${PACKAGE_PATH}.${REF_UPPER}Entity;" "$SERVICE_FILE"
  echo "✅ Imported ${REF_UPPER}Entity"
fi


# Remove duplicate repository imports
sed -i "/import ${PACKAGE_PATH//\//\\/}\.${REF_UPPER}Repository;/d" "$SERVICE_FILE"

# Add Repository import once
if ! grep -q "import ${PACKAGE_PATH}.${REF_UPPER}Repository;" "$SERVICE_FILE"; then
  sed -i "/^package /a\\
import ${PACKAGE_PATH}.${REF_UPPER}Repository;" "$SERVICE_FILE"
  echo "✅ Imported ${REF_UPPER}Repository"
fi

  # ============================================================
  # 2. INJECT REPOSITORY
  # ============================================================

  if ! grep -q "private final ${REF_UPPER}Repository ${REF_LOWER}Repository;" "$SERVICE_FILE"; then
    sed -i "/private final ${FEATURE_UPPER}Repository repository;/a\\
    private final ${REF_UPPER}Repository ${REF_LOWER}Repository;" "$SERVICE_FILE"
    echo "✅ Injected ${REF_UPPER}Repository"
  fi

  # ============================================================
  # 3. CLEAN OLD / BROKEN INSERTIONS
  # ============================================================

  sed -i "/${REF_LOWER}Entity ${REF_LOWER} = validate/d" "$SERVICE_FILE"
  sed -i "/entity\.set${REF_UPPER}(/d" "$SERVICE_FILE"

  # ============================================================
  # 4. VALIDATION METHOD (RETURNS ENTITY)
  # ============================================================

  if ! grep -q "validate${REF_UPPER}Exists" "$SERVICE_FILE"; then
    sed -i '$i\
\
    private '"${REF_UPPER}"'Entity validate'"${REF_UPPER}"'Exists(Long id) {\
        if (id == null) {\
            throw new IllegalArgumentException("'"${REF_UPPER}"' ID is required");\
        }\
        return '"${REF_LOWER}"'Repository.findById(id)\
                .orElseThrow(() -> new IllegalStateException("'"${REF_UPPER}"' not found with id: " + id));\
    }\
' "$SERVICE_FILE"

    echo "✅ Added validate${REF_UPPER}Exists() method"
  fi

  # ============================================================
  # 5. FIX GETTER NAME
  # ============================================================

  sed -i "s/get${REF_UPPER}_id/get${PROP_CAP}/g" "$SERVICE_FILE"

  # ============================================================
  # 6. ADD FK LOGIC (ONLY IF NOT EXISTS)
  # ============================================================

  if ! grep -q "validate${REF_UPPER}Exists(request.get${PROP_CAP}())" "$SERVICE_FILE"; then

    sed -i "/entity\.setDescription(request\.getDescription());/a\\
        ${REF_UPPER}Entity ${REF_LOWER} = validate${REF_UPPER}Exists(request.get${PROP_CAP}());\\
        entity.set${REF_UPPER}(${REF_LOWER});" "$SERVICE_FILE"

    echo "✅ Added FK validation + assignment"
  else
    echo "⚠️ FK logic already exists, skipping"
  fi

fi




echo ""
echo "🎉 All DTOs updated successfully!"
echo "⚠️  Don't forget to add the column in database:"
echo "   ALTER TABLE MBIMS.${FEATURE_UPPER^^} ADD COLUMN ${PROPERTY_NAME^^} BIGINT;"