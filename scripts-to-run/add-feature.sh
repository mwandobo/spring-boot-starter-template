#!/bin/bash

# ===============================
# Spring Boot Feature Generator
# ===============================

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
      echo "Usage: ./scripts-to-run/generate-feature.sh --name <FeatureName> [--plural <s|es|ies>] [--parent <parent>]"
      echo "Example: ./scripts-to-run/add-feature.sh --name department --plural s --parent administration"
      exit 0
      ;;
    *)
      echo "❌ Unknown parameter: $1"
      echo "Usage: ./generate-feature.sh --name <FeatureName> [--plural <s|es|ies>] [--parent <parent>]"
      exit 1
      ;;
  esac
done




if [ -z "$FEATURE_NAME" ]; then
  echo "❌ Feature name is required"
  echo "Usage: ./generate-feature.sh --name department --plural s"
  exit 1
fi

FEATURE_LOWER=$(echo "$FEATURE_NAME" | tr '[:upper:]' '[:lower:]')
FEATURE_UPPER="$(tr '[:lower:]' '[:upper:]' <<< ${FEATURE_LOWER:0:1})${FEATURE_LOWER:1}"
PARENT_LOWER=$(echo "$PARENT" | tr '[:upper:]' '[:lower:]')

# -------------------------------
# Plural handling (explicit)
# -------------------------------
if [ -z "$PLURAL_SUFFIX" ]; then
  FEATURE_PLURAL="$FEATURE_LOWER"
else
  case "$PLURAL_SUFFIX" in
    s)
      FEATURE_PLURAL="${FEATURE_LOWER}s"
      ;;
    es)
      FEATURE_PLURAL="${FEATURE_LOWER}es"
      ;;
    ies)
      FEATURE_PLURAL="${FEATURE_LOWER%y}ies"
      ;;
    *)
      echo "❌ Invalid plural suffix: $PLURAL_SUFFIX (use: s | es | ies)"
      exit 1
      ;;
  esac
fi


BASE_PACKAGE="com.bonnysimon.starter.features"
if [ -n "$PARENT_LOWER" ]; then
  BASE_PACKAGE="$BASE_PACKAGE.$PARENT_LOWER"
  BASE_DIR="src/main/java/com/bonnysimon/starter/features/$PARENT_LOWER/$FEATURE_LOWER"
else
  BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_LOWER"
fi

echo "🚀 Creating feature: $FEATURE_UPPER"

mkdir -p "$BASE_DIR/dto"

# -------------------------------
# Entity (Minimal)
# -------------------------------
cat <<EOF > "$BASE_DIR/${FEATURE_UPPER}Entity.java"
package $BASE_PACKAGE.$FEATURE_LOWER;

import com.bonnysimon.starter.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "${FEATURE_LOWER}")
public class ${FEATURE_UPPER}Entity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column
    private String description;
}
EOF

# -------------------------------
# Repository
# -------------------------------
cat <<EOF > "$BASE_DIR/${FEATURE_UPPER}Repository.java"
package $BASE_PACKAGE.$FEATURE_LOWER;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ${FEATURE_UPPER}Repository extends JpaRepository<${FEATURE_UPPER}Entity, Long> {
    Optional<${FEATURE_UPPER}Entity> findByName(String name);

     Page<${FEATURE_UPPER}Entity> findAll(
                Specification<${FEATURE_UPPER}Entity> spec,
                Pageable pageable
        );
}
EOF

# -------------------------------
# DTO
# -------------------------------
cat <<EOF > "$BASE_DIR/dto/Create${FEATURE_UPPER}DTO.java"
package $BASE_PACKAGE.$FEATURE_LOWER.dto;

import lombok.Data;

@Data
public class Create${FEATURE_UPPER}DTO {
    private String name;
    private String description;
}
EOF


# -------------------------------
# RESPONSE DTO
# -------------------------------
cat <<EOF > "$BASE_DIR/dto/${FEATURE_UPPER}ResponseDTO.java"
package $BASE_PACKAGE.$FEATURE_LOWER.dto;

import $BASE_PACKAGE.$FEATURE_LOWER.${FEATURE_UPPER}Entity;
import java.time.format.DateTimeFormatter;
import lombok.Data;

@Data
public class ${FEATURE_UPPER}ResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String createdAt;
    private String updatedAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static  ${FEATURE_UPPER}ResponseDTO fromEntity( ${FEATURE_UPPER}Entity ${FEATURE_LOWER}) {
            ${FEATURE_UPPER}ResponseDTO dto = new ${FEATURE_UPPER}ResponseDTO();
            dto.setId(${FEATURE_LOWER}.getId());
            dto.setName(${FEATURE_LOWER}.getName());
            dto.setDescription(${FEATURE_LOWER}.getDescription());
            dto.setUpdatedAt(${FEATURE_LOWER}.getUpdatedAt() != null ? ${FEATURE_LOWER}.getUpdatedAt().toString() : null);
            dto.setCreatedAt(${FEATURE_LOWER}.getCreatedAt() != null ? ${FEATURE_LOWER}.getCreatedAt().toString() : null);
            return dto;
        }
}
EOF

# -------------------------------
# Service (CRUD)
# -------------------------------
cat <<EOF > "$BASE_DIR/${FEATURE_UPPER}Service.java"
package $BASE_PACKAGE.$FEATURE_LOWER;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import $BASE_PACKAGE.$FEATURE_LOWER.dto.Create${FEATURE_UPPER}DTO;
import $BASE_PACKAGE.$FEATURE_LOWER.dto.${FEATURE_UPPER}ResponseDTO;
import $BASE_PACKAGE.$FEATURE_LOWER.${FEATURE_UPPER}Entity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bonnysimon.starter.core.dto.PagedResponse;
import com.bonnysimon.starter.core.dto.PaginationDto;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ${FEATURE_UPPER}Service {

    private final ${FEATURE_UPPER}Repository repository;

    public PagedResponse<${FEATURE_UPPER}ResponseDTO> findAll(
            PaginationRequest pagination,
            String search
    ) {
        Specification<${FEATURE_UPPER}Entity> spec = getEntitySpecification(search);

        Page<${FEATURE_UPPER}Entity> page =
                repository.findAll(spec, pagination.toPageable());

        List<${FEATURE_UPPER}ResponseDTO> dtoList = page.getContent()
                        .stream()
                        .map(${FEATURE_UPPER}ResponseDTO::fromEntity)
                        .toList();

        return new PagedResponse<>(
                        dtoList,
                        new PaginationDto(
                                page.getTotalElements(),
                                page.getNumber() + 1,
                                page.getSize(),
                                page.getTotalPages()
                        ),
                        false // or dynamic logic
                );
    }

        private static Specification< ${FEATURE_UPPER}Entity> getEntitySpecification(String search) {
            Specification< ${FEATURE_UPPER}Entity> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

            // Optional search filter (case-insensitive)
            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.trim().toLowerCase() + "%";
                spec = spec.and((root, query, cb) ->
                        cb.or(
                                cb.like(cb.lower(root.get("title")), likePattern),
                                cb.like(cb.lower(root.get("description")), likePattern)
                        )
                );
            }
            return spec;
        }

    @Transactional
    public ${FEATURE_UPPER}ResponseDTO create(Create${FEATURE_UPPER}DTO request) {
        repository.findByName(request.getName())
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "${FEATURE_UPPER} with name '" + request.getName() + "' already exists"
                    );
                });

        ${FEATURE_UPPER}Entity entity = new ${FEATURE_UPPER}Entity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        ${FEATURE_UPPER}Entity savedEntity = repository.save(entity);

        return  ${FEATURE_UPPER}ResponseDTO.fromEntity(savedEntity);
    }

      public ${FEATURE_UPPER}ResponseDTO findOne  (Long  ${FEATURE_LOWER}Id) {
            ${FEATURE_UPPER}Entity   ${FEATURE_LOWER} = repository.findById( ${FEATURE_LOWER}Id)
                   .orElseThrow(() -> new IllegalStateException(" ${FEATURE_UPPER} not found"));
           return  ${FEATURE_UPPER}ResponseDTO.fromEntity(${FEATURE_LOWER});
       }

    @Transactional
    public ${FEATURE_UPPER}ResponseDTO update(Long id, Create${FEATURE_UPPER}DTO request) {
        ${FEATURE_UPPER}Entity entity = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "${FEATURE_UPPER} not found with id: " + id
                        )
                );

        repository.findByName(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "${FEATURE_UPPER} with name '" + request.getName() + "' already exists"
                    );
                });

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        ${FEATURE_UPPER}Entity updatedEntity = repository.save(entity);

        return  ${FEATURE_UPPER}ResponseDTO.fromEntity(updatedEntity);
    }

    @Transactional
    public void delete(Long id, boolean soft) {
        ${FEATURE_UPPER}Entity entity = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "${FEATURE_UPPER} not found with id: " + id
                        )
                );

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
cat <<EOF > "$BASE_DIR/${FEATURE_UPPER}Controller.java"
package $BASE_PACKAGE.$FEATURE_LOWER;

import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import $BASE_PACKAGE.$FEATURE_LOWER.dto.Create${FEATURE_UPPER}DTO;
import $BASE_PACKAGE.$FEATURE_LOWER.dto.${FEATURE_UPPER}ResponseDTO;
import com.bonnysimon.starter.core.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/${FEATURE_PLURAL}")
@RequiredArgsConstructor
public class ${FEATURE_UPPER}Controller {

    private final ${FEATURE_UPPER}Service service;

    @GetMapping
    public PagedResponse<${FEATURE_UPPER}ResponseDTO> findAll(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
                return service.findAll(pagination, search);
    }

    @PostMapping
    public ${FEATURE_UPPER}ResponseDTO  create(
            @RequestBody Create${FEATURE_UPPER}DTO request
    ) {
        return service.create(request);
    }

     @GetMapping("/{id}")
        public ${FEATURE_UPPER}ResponseDTO findOne(
                @PathVariable Long id
        ) {
            return service.findOne(id);
        }

    @PutMapping("/{id}")
    public ${FEATURE_UPPER}ResponseDTO update(
            @PathVariable Long id,
            @RequestBody Create${FEATURE_UPPER}DTO request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @RequestParam(name = "soft", defaultValue = "false") boolean soft
    ) {
        service.delete(id, soft);
        return ApiResponse.success(null);
    }
}
EOF


# -------------------------------
# HTTP Client Requests (SAFE)
# -------------------------------
HTTP_FILE="http-client.http"
HTTP_MARKER="### FEATURE: ${FEATURE_PLURAL}"

touch "$HTTP_FILE"

if grep -Fxq "$HTTP_MARKER" "$HTTP_FILE"; then
  echo "⚠️ HTTP requests for '${FEATURE_PLURAL}' already exist — skipping"
else
cat <<EOF >> "$HTTP_FILE"

$HTTP_MARKER

###
GET {{base_url}}/${FEATURE_PLURAL}
Authorization: Bearer {{token}}

###
POST {{base_url}}/${FEATURE_PLURAL}
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "name": "Sample Name",
  "description": "Sample Description"
}

###
GET {{base_url}}/${FEATURE_PLURAL}/1
Authorization: Bearer {{token}}

###
PUT {{base_url}}/${FEATURE_PLURAL}/1
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "name": "Sample Name Edited",
  "description": "Sample Description Edited"
}

###
DELETE {{base_url}}/${FEATURE_PLURAL}/1
Authorization: Bearer {{token}}

EOF
fi

echo "✅ Feature '$FEATURE_UPPER' created successfully!"