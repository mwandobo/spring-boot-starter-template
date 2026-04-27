package com.bonnysimon.starter.features.approval.repository;

import com.bonnysimon.starter.features.approval.entity.SysApproval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SysApprovalRepository  extends JpaRepository<SysApproval, Long> {
    SysApproval findByName(String name);

    Page<SysApproval> findAll(Specification<SysApproval> spec, Pageable pageable);

    Optional<SysApproval> findByEntityName(String entityName);
}