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
echo "🚀 Creating feature: $FEATURE_PASCAL"

# -------------------------------
# Entity
# -------------------------------
cat <<EOF > "$BASE_DIR/${FEATURE_PASCAL}Entity.java"
package $BASE_PACKAGE.$FEATURE_SNAKE;

import com.bonnysimon.starter.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "${FEATURE_SNAKE}")
public class ${FEATURE_PASCAL}Entity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(columnDefinition = "VARCHAR(1000)")
    private String description;
}
EOF

# -------------------------------
# Repository
# -------------------------------
cat <<EOF > "$BASE_DIR/${FEATURE_PASCAL}Repository.java"
package $BASE_PACKAGE.$FEATURE_SNAKE;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ${FEATURE_PASCAL}Repository extends JpaRepository<${FEATURE_PASCAL}Entity, Long> {
    Optional<${FEATURE_PASCAL}Entity> findByName(String name);

    Page<${FEATURE_PASCAL}Entity> findAll(Specification<${FEATURE_PASCAL}Entity> spec, Pageable pageable);
}
EOF

# -------------------------------
# DTO - Create
# -------------------------------
cat <<EOF > "$BASE_DIR/dto/Create${FEATURE_PASCAL}DTO.java"
package $BASE_PACKAGE.$FEATURE_SNAKE.dto;

import lombok.Data;

@Data
public class Create${FEATURE_PASCAL}DTO {
    private String name;
    private String description;
}
EOF

# -------------------------------
# DTO - Response
# -------------------------------
cat <<EOF > "$BASE_DIR/dto/${FEATURE_PASCAL}ResponseDTO.java"
package $BASE_PACKAGE.$FEATURE_SNAKE.dto;

import $BASE_PACKAGE.$FEATURE_SNAKE.${FEATURE_PASCAL}Entity;
import lombok.Data;

@Data
public class ${FEATURE_PASCAL}ResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String approvalStatus;
    private String createdAt;
    private String updatedAt;

    public static ${FEATURE_PASCAL}ResponseDTO fromEntity(${FEATURE_PASCAL}Entity entity) {
        ${FEATURE_PASCAL}ResponseDTO dto = new ${FEATURE_PASCAL}ResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
        dto.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null);
        return dto;
    }
}
EOF

# -------------------------------
# Service
# -------------------------------
cat <<EOF > "$BASE_DIR/${FEATURE_PASCAL}Service.java"
package $BASE_PACKAGE.$FEATURE_SNAKE;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import $BASE_PACKAGE.$FEATURE_SNAKE.dto.Create${FEATURE_PASCAL}DTO;
import $BASE_PACKAGE.$FEATURE_SNAKE.dto.${FEATURE_PASCAL}ResponseDTO;
import $BASE_PACKAGE.$FEATURE_SNAKE.${FEATURE_PASCAL}Entity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bonnysimon.starter.core.dto.PagedResponse;
import com.bonnysimon.starter.core.dto.PaginationDto;
import com.bonnysimon.starter.features.approval.util.ApprovalStatusUtil;
import com.bonnysimon.starter.core.services.CurrentUserService;
import com.bonnysimon.starter.features.approval.dto.ApprovalAwareDTO;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ${FEATURE_PASCAL}Service {
    private final ${FEATURE_PASCAL}Repository repository;
    private final ApprovalStatusUtil approvalStatusUtil;
    private final CurrentUserService currentUserService;

    public PagedResponse<${FEATURE_PASCAL}ResponseDTO> findAll(PaginationRequest pagination, String search) {
        Specification<${FEATURE_PASCAL}Entity> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));
        // Add search logic here if needed

        Page<${FEATURE_PASCAL}Entity> page = repository.findAll(spec, pagination.toPageable());

        List<${FEATURE_PASCAL}ResponseDTO> result = page.getContent().stream()
                .map(${FEATURE_PASCAL}ResponseDTO::fromEntity)
                .toList();

        return new PagedResponse<>(
                result,
                new PaginationDto(page.getTotalElements(), page.getNumber() + 1, page.getSize(), page.getTotalPages()),
                false
        );
    }

    @Transactional
    public ${FEATURE_PASCAL}ResponseDTO create(Create${FEATURE_PASCAL}DTO request) {
        ${FEATURE_PASCAL}Entity entity = new ${FEATURE_PASCAL}Entity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        ${FEATURE_PASCAL}Entity saved = repository.save(entity);
        return ${FEATURE_PASCAL}ResponseDTO.fromEntity(saved);
    }

    public ApprovalAwareDTO<${FEATURE_PASCAL}ResponseDTO> findOne(Long id) {
        ${FEATURE_PASCAL}Entity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("${FEATURE_PASCAL} not found"));
        return approvalStatusUtil.attachApprovalInfo(
                ${FEATURE_PASCAL}ResponseDTO.fromEntity(entity),
                entity.getId(),
                ${FEATURE_PASCAL}Entity.class.getSimpleName(),
                currentUserService.getCurrentUserRoleId()
        );
    }

    @Transactional
    public ${FEATURE_PASCAL}ResponseDTO update(Long id, Create${FEATURE_PASCAL}DTO request) {
        ${FEATURE_PASCAL}Entity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("${FEATURE_PASCAL} not found"));

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        ${FEATURE_PASCAL}Entity updated = repository.save(entity);
        return ${FEATURE_PASCAL}ResponseDTO.fromEntity(updated);
    }

    @Transactional
    public void delete(Long id, boolean soft) {
        ${FEATURE_PASCAL}Entity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("${FEATURE_PASCAL} not found"));
        if (soft) {
            entity.setDeleted(true);
            repository.save(entity);
        } else {
            repository.delete(entity);
        }
    }
}
EOF

# -------------------------------
# Controller
# -------------------------------
cat <<EOF > "$BASE_DIR/${FEATURE_PASCAL}Controller.java"
package $BASE_PACKAGE.$FEATURE_SNAKE;

import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import $BASE_PACKAGE.$FEATURE_SNAKE.dto.Create${FEATURE_PASCAL}DTO;
import $BASE_PACKAGE.$FEATURE_SNAKE.dto.${FEATURE_PASCAL}ResponseDTO;
import com.bonnysimon.starter.core.dto.PagedResponse;
import com.bonnysimon.starter.features.approval.dto.ApprovalAwareDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/${FEATURE_PLURAL_KEBAB}")
@RequiredArgsConstructor
public class ${FEATURE_PASCAL}Controller {

    private final ${FEATURE_PASCAL}Service service;

    @GetMapping
    public PagedResponse<${FEATURE_PASCAL}ResponseDTO> findAll(
            PaginationRequest pagination,
            @RequestParam(required = false) String search) {
        return service.findAll(pagination, search);
    }

    @PostMapping
    public ${FEATURE_PASCAL}ResponseDTO create(@RequestBody Create${FEATURE_PASCAL}DTO request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public ApprovalAwareDTO<${FEATURE_PASCAL}ResponseDTO> findOne(@PathVariable Long id) {
        return service.findOne(id);
    }

    @PatchMapping("/{id}")
    public ${FEATURE_PASCAL}ResponseDTO update(@PathVariable Long id, @RequestBody Create${FEATURE_PASCAL}DTO request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @RequestParam(name = "soft", defaultValue = "false") boolean soft) {
        service.delete(id, soft);
        return ApiResponse.success(null);
    }
}
EOF

echo "✅ Feature '$FEATURE_PASCAL' created successfully!"
echo "   Location : $BASE_DIR"
echo "   API Route: /api/v1/${FEATURE_PLURAL_KEBAB}"