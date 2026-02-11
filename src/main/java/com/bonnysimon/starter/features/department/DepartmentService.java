package com.bonnysimon.starter.features.department;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.department.dto.CreateDepartmentDTO;
import com.bonnysimon.starter.features.department.DepartmentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository repository;

    public PaginationResponse<DepartmentEntity> findAll(
            PaginationRequest pagination,
            String search
    ) {
        Specification<DepartmentEntity> spec =
                (root, query, cb) -> cb.isFalse(root.get("deleted"));

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("name")),
                            "%" + search.toLowerCase() + "%"
                    )
            );
        }

        Page<DepartmentEntity> page =
                repository.findAll(spec, pagination.toPageable());

        return PaginationResponse.of(page);
    }

    @Transactional
    public DepartmentEntity create(CreateDepartmentDTO request) {
        repository.findByName(request.getName())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Department with name '" + request.getName() + "' already exists"
                    );
                });

        DepartmentEntity entity = new DepartmentEntity();
        entity.setName(request.getName());

        return repository.save(entity);
    }

    @Transactional
    public DepartmentEntity update(Long id, CreateDepartmentDTO request) {
        DepartmentEntity entity = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Department not found with id: " + id
                        )
                );

        repository.findByName(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Department with name '" + request.getName() + "' already exists"
                    );
                });

        entity.setName(request.getName());

        return repository.save(entity);
    }

    @Transactional
    public void delete(Long id, boolean soft) {
        DepartmentEntity entity = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Department not found with id: " + id
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
