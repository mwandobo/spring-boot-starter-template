package com.bonnysimon.starter.features.approval.repository;

import com.bonnysimon.starter.features.approval.entity.UserApproval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserApprovalRepository extends JpaRepository<UserApproval, Long> {
    UserApproval findByName(String name);

    Page<UserApproval> findAll(Specification<UserApproval> spec, Pageable pageable);
}