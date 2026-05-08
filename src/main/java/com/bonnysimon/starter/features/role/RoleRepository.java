package com.bonnysimon.starter.features.role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);


    Page<RoleEntity> findAll(Specification<RoleEntity> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"permissions"})
    Optional<RoleEntity> findWithPermissionsById(Long id);
}
