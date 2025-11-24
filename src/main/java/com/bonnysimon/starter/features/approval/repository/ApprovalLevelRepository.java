package com.bonnysimon.starter.features.approval.repository;

import com.bonnysimon.starter.features.approval.entity.ApprovalLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalLevelRepository extends JpaRepository<ApprovalLevel, Long> {
    ApprovalLevel findByName(String name);

    Page<ApprovalLevel> findAll(Specification<ApprovalLevel> spec, Pageable pageable);

    Optional<ApprovalLevel> findByRoleIdAndUserApprovalId(Long roleId, Long userApprovalId);

    List<ApprovalLevel> findByUserApprovalIdOrderByLevelAsc(Long userApprovalId);
}