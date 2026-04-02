package com.bonnysimon.starter.features.role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);


    Page<Role> findAll(Specification<Role> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"permissions"})
    Optional<Role> findWithPermissionsById(Long id);
}
