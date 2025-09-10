package com.bonnysimon.starter.features.approval.services;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.approval.dto.UserApprovalRequestDTO;
import com.bonnysimon.starter.features.approval.entity.SysApproval;
import com.bonnysimon.starter.features.approval.entity.UserApproval;
import com.bonnysimon.starter.features.approval.repository.SysApprovalRepository;
import com.bonnysimon.starter.features.approval.repository.UserApprovalRepository;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserApprovalService {
    private final SysApprovalRepository sysApprovalRepository;
    private final UserApprovalRepository repository;
    private final UserRepository userRepository;

    public PaginationResponse<UserApproval> findAll(PaginationRequest pagination, String search) {
        Specification<UserApproval> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%")
            );
        }

        Page<UserApproval> userApprovals = repository.findAll(spec, pagination.toPageable());
        return PaginationResponse.of(userApprovals);
    }

    @Transactional
    public UserApproval create(UserApprovalRequestDTO request) {
        repository.findBySysApprovalId(request.getSysApprovalId())
                .ifPresent(existing -> {
                    throw new IllegalStateException("Approval exists");
                });

        UserApproval userApproval = new UserApproval();
        userApproval.setName(request.getName());
        userApproval.setDescription(request.getDescription());

        SysApproval sysApproval = sysApprovalRepository.findById(request.getSysApprovalId())
                .orElseThrow(() -> new IllegalStateException("System approval does not exist"));
        userApproval.setSysApproval(sysApproval);

        return repository.save(userApproval);
    }

    @Transactional
    public UserApproval update(Long id, UserApprovalRequestDTO request) {
        UserApproval userApproval = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UserApproval not found with id: " + id));

        // Check if another userApproval with the same name exists
        UserApproval existing = repository.findByName(request.getName());
        if (existing != null && !existing.getId().equals(id)) {
            throw new IllegalArgumentException("UserApproval with name '" + request.getName() + "' already exists");
        }

        userApproval.setName(request.getName());
        userApproval.setDescription(request.getDescription());

        SysApproval sysApproval = sysApprovalRepository.findById(request.getSysApprovalId())
                .orElseThrow(() -> new IllegalArgumentException("System approval does not exist"));
        userApproval.setSysApproval(sysApproval);

        return repository.save(userApproval);
    }

    @Transactional
    public void delete(Long id, boolean soft) {
        UserApproval userApproval = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UserApproval not found with id: " + id));

        if (soft) {
            userApproval.setDeleted(true); // soft delete flag from BaseEntity
            repository.save(userApproval);
        } else {
            repository.delete(userApproval);
        }
    }
}
