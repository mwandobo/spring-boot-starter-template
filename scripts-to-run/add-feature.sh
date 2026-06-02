#!/bin/bash

# ================================================
# Spring Boot Feature Generator - FULLY DYNAMIC
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

# Parent handling
if [ -n "$PARENT" ]; then
    PARENT_SNAKE=$(to_snake_case "$PARENT")
    FULL_PACKAGE="$BASE_PACKAGE.features.$PARENT_SNAKE.$FEATURE_SNAKE"
else
    FULL_PACKAGE="$BASE_PACKAGE.features.$FEATURE_SNAKE"
fi

BASE_DIR="src/main/java/$(echo "$FULL_PACKAGE" | tr '.' '/')"

mkdir -p "$BASE_DIR/dto"

# ====================== DYNAMIC CORE PACKAGES ======================
CORE_DTO="$BASE_PACKAGE.core.dto"
CORE_ENTITY="$BASE_PACKAGE.core.entity"
CORE_SERVICES="$BASE_PACKAGE.core.services"
APPROVAL_UTIL="$BASE_PACKAGE.features.approval.util"
APPROVAL_DTO="$BASE_PACKAGE.features.approval.dto"

# ====================== LOGGING ======================
echo "=================================================="
echo "🚀 Feature Generation Started"
echo "=================================================="
echo "Base Package : $BASE_PACKAGE"
echo "Feature      : $FEATURE_PASCAL"
echo "Full Package : $FULL_PACKAGE"
echo "Location     : $BASE_DIR"
echo "API Route    : /api/v1/${FEATURE_PLURAL_KEBAB}"
echo "=================================================="
echo ""

# -------------------------------
# Entity
# -------------------------------
cat <<EOF > "$BASE_DIR/${FEATURE_PASCAL}Entity.java"
package $FULL_PACKAGE;

import $CORE_ENTITY.BaseEntity;
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
package $FULL_PACKAGE;

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
# DTOs
# -------------------------------
cat <<EOF > "$BASE_DIR/dto/Create${FEATURE_PASCAL}DTO.java"
package $FULL_PACKAGE.dto;

import lombok.Data;

@Data
public class Create${FEATURE_PASCAL}DTO {
    private String name;
    private String description;
}
EOF

cat <<EOF > "$BASE_DIR/dto/${FEATURE_PASCAL}ResponseDTO.java"
package $FULL_PACKAGE.dto;

import $FULL_PACKAGE.${FEATURE_PASCAL}Entity;
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
package $FULL_PACKAGE;

import $CORE_DTO.PaginationRequest;
import $FULL_PACKAGE.dto.Create${FEATURE_PASCAL}DTO;
import $FULL_PACKAGE.dto.${FEATURE_PASCAL}ResponseDTO;
import $FULL_PACKAGE.${FEATURE_PASCAL}Entity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import $CORE_DTO.PagedResponse;
import $CORE_DTO.PaginationDto;
import $APPROVAL_UTIL.ApprovalStatusUtil;
import $CORE_SERVICES.CurrentUserService;
import $APPROVAL_DTO.ApprovalAwareDTO;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ${FEATURE_PASCAL}Service {
    private final ${FEATURE_PASCAL}Repository repository;
    private final ApprovalStatusUtil approvalStatusUtil;
    private final CurrentUserService currentUserService;

    public PagedResponse<${FEATURE_PASCAL}ResponseDTO> findAll(PaginationRequest pagination, String search) {
        Specification<${FEATURE_PASCAL}Entity> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

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
package $FULL_PACKAGE;

import $CORE_DTO.ApiResponse;
import $CORE_DTO.PaginationRequest;
import $FULL_PACKAGE.dto.Create${FEATURE_PASCAL}DTO;
import $FULL_PACKAGE.dto.${FEATURE_PASCAL}ResponseDTO;
import $CORE_DTO.PagedResponse;
import $APPROVAL_DTO.ApprovalAwareDTO;
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
echo "   Package  : $FULL_PACKAGE"