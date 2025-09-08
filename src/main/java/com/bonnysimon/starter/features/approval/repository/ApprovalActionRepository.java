package com.bonnysimon.starter.features.approval.repository;

import com.bonnysimon.starter.features.approval.entity.ApprovalAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalActionRepository extends JpaRepository<ApprovalAction, Long> {
    ApprovalAction findByName(String name);

    Page<ApprovalAction> findAll(Specification<ApprovalAction> spec, Pageable pageable);
}