package com.bonnysimon.starter.features.department;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {
    Optional<DepartmentEntity> findByName(String name);

     Page<DepartmentEntity> findAll(
                Specification<DepartmentEntity> spec,
                Pageable pageable
        );
}
