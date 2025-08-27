package com.bonnysimon.starter.features.role;

import com.bonnysimon.starter.features.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);

    Page<Role> findAll(Specification<User> spec, Pageable pageable);
}
