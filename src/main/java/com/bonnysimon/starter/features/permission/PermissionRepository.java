package com.bonnysimon.starter.features.permission;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
    PermissionEntity findByName(String name);

    Page<PermissionEntity> findAll(Specification<PermissionEntity> spec, Pageable pageable);
}
