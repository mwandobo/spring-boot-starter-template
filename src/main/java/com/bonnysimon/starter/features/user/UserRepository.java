package com.bonnysimon.starter.features.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByName(String name);

    Page<UserEntity> findAll(
            Specification<UserEntity> spec,
            Pageable pageable
    );

    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findByRoleId(Long id);

    List<UserEntity> findByRoleIdIn(List<Long> roleIds);

    boolean existsByEmail(String email);


    @Query("""
            SELECT DISTINCT u
            FROM UserEntity u
            LEFT JOIN FETCH u.role r
            LEFT JOIN FETCH r.permissions
            WHERE u.email = :email
            """)
    Optional<UserEntity> findByEmailWithAuthorities(String email);


}
