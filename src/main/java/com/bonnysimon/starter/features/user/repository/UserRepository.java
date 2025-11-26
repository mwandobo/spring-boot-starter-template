package com.bonnysimon.starter.features.user.repository;

import com.bonnysimon.starter.features.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Page<User> findAll(Specification<User> spec, Pageable pageable);

    List<User> findByRoleId(Long id);

    List<User> findByRoleIdIn(List<Long> roleIds);
}