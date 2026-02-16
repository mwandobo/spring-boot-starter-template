#!/bin/bash

# ===============================
# Spring Boot Feature Generator
# ===============================

if [ -z "$1" ]; then
  echo "❌ Feature name is required"
  echo "Usage: ./generate-feature.sh department"
  exit 1
fi

FEATURE_LOWER=$(echo "$1" | tr '[:upper:]' '[:lower:]')
FEATURE_UPPER="$(tr '[:lower:]' '[:upper:]' <<< ${FEATURE_LOWER:0:1})${FEATURE_LOWER:1}"

# -------------------------------
# Plural handling (explicit)
# -------------------------------
if [ -z "$2" ]; then
  # no suffix provided → no plural
  FEATURE_PLURAL="$FEATURE_LOWER"
else
  case "$2" in
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
      echo "❌ Invalid plural suffix: $2 (use: s | es | ies)"
      exit 1
      ;;
  esac
fi


BASE_PACKAGE="com.bonnysimon.starter.features"
BASE_DIR="src/main/java/com/bonnysimon/starter/features/$FEATURE_LOWER"

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
# Service (CRUD)
# -------------------------------
cat <<EOF > "$BASE_DIR/${FEATURE_UPPER}Service.java"
package $BASE_PACKAGE.$FEATURE_LOWER;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.$FEATURE_LOWER.dto.Create${FEATURE_UPPER}DTO;
import com.bonnysimon.starter.features.$FEATURE_LOWER.${FEATURE_UPPER}Entity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ${FEATURE_UPPER}Service {

    private final ${FEATURE_UPPER}Repository repository;

    public PaginationResponse<${FEATURE_UPPER}Entity> findAll(
            PaginationRequest pagination,
            String search
    ) {
        Specification<${FEATURE_UPPER}Entity> spec =
                (root, query, cb) -> cb.isFalse(root.get("deleted"));

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("name")),
                            "%" + search.toLowerCase() + "%"
                    )
                    cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%")
            );
        }

        Page<${FEATURE_UPPER}Entity> page =
                repository.findAll(spec, pagination.toPageable());

        return PaginationResponse.of(page);
    }

    @Transactional
    public ${FEATURE_UPPER}Entity create(Create${FEATURE_UPPER}DTO request) {
        repository.findByName(request.getName())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "${FEATURE_UPPER} with name '" + request.getName() + "' already exists"
                    );
                });

        ${FEATURE_UPPER}Entity entity = new ${FEATURE_UPPER}Entity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        return repository.save(entity);
    }

    @Transactional
    public ${FEATURE_UPPER}Entity update(Long id, Create${FEATURE_UPPER}DTO request) {
        ${FEATURE_UPPER}Entity entity = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "${FEATURE_UPPER} not found with id: " + id
                        )
                );

        repository.findByName(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "${FEATURE_UPPER} with name '" + request.getName() + "' already exists"
                    );
                });

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        return repository.save(entity);
    }

    @Transactional
    public void delete(Long id, boolean soft) {
        ${FEATURE_UPPER}Entity entity = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
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
import com.bonnysimon.starter.features.$FEATURE_LOWER.dto.Create${FEATURE_UPPER}DTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/${FEATURE_PLURAL}")
@RequiredArgsConstructor
public class ${FEATURE_UPPER}Controller {

    private final ${FEATURE_UPPER}Service service;

    @GetMapping
    public ApiResponse<PaginationResponse<${FEATURE_UPPER}Entity>> findAll(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(
                service.findAll(pagination, search)
        );
    }

    @PostMapping
    public ApiResponse<${FEATURE_UPPER}Entity> create(
            @RequestBody Create${FEATURE_UPPER}DTO request
    ) {
        return ApiResponse.success(
                service.create(request)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<${FEATURE_UPPER}Entity> update(
            @PathVariable Long id,
            @RequestBody Create${FEATURE_UPPER}DTO request
    ) {
        return ApiResponse.success(
                service.update(id, request)
        );
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