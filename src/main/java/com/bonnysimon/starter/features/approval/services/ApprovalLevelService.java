package com.bonnysimon.starter.features.approval.services;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.approval.dto.ApprovalLevelRequestDto;
import com.bonnysimon.starter.features.approval.entity.ApprovalLevel;
import com.bonnysimon.starter.features.approval.entity.UserApproval;
import com.bonnysimon.starter.features.approval.enums.StatusEnum;
import com.bonnysimon.starter.features.approval.repository.ApprovalLevelRepository;
import com.bonnysimon.starter.features.approval.repository.UserApprovalRepository;
import com.bonnysimon.starter.features.role.Role;
import com.bonnysimon.starter.features.role.RoleRepository;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApprovalLevelService {

    private final ApprovalLevelRepository repository;
    private final UserApprovalRepository userApprovalRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public PaginationResponse<ApprovalLevel> findAll(PaginationRequest pagination, String search) {
        Specification<ApprovalLevel> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%")
            );
        }

        Page<ApprovalLevel> approvalLevels = repository.findAll(spec, pagination.toPageable());
        return PaginationResponse.of(approvalLevels);
    }

    @Transactional
    public ApprovalLevel create(ApprovalLevelRequestDto request) {
        ApprovalLevel level = new ApprovalLevel();
        level.setName(request.getName());
        level.setDescription(request.getDescription());
        level.setLevel(request.getLevel());
        level.setStatus(request.getStatus() != null ? request.getStatus() : StatusEnum.PENDING);

        UserApproval userApproval = userApprovalRepository.findById(request.getUserApprovalId())
                .orElseThrow(() -> new IllegalArgumentException("UserApproval not found"));
        level.setUserApproval(userApproval);

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found"));
            level.setRole(role);
        }

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            level.setUser(user);
        }

        return repository.save(level);
    }

    @Transactional
    public ApprovalLevel update(Long id, ApprovalLevelRequestDto request) {
        ApprovalLevel level = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ApprovalLevel not found"));

        level.setName(request.getName());
        level.setDescription(request.getDescription());
        level.setLevel(request.getLevel());
        if (request.getStatus() != null) {
            level.setStatus(request.getStatus());
        }

        if (request.getUserApprovalId() != null) {
            UserApproval userApproval = userApprovalRepository.findById(request.getUserApprovalId())
                    .orElseThrow(() -> new IllegalArgumentException("UserApproval not found"));
            level.setUserApproval(userApproval);
        }

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found"));
            level.setRole(role);
            level.setUser(null); // clear user if role is set
        }

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            level.setUser(user);
            level.setRole(null); // clear role if user is set
        }

        return repository.save(level);
    }

    @Transactional
    public void delete(Long id, boolean soft) {
        ApprovalLevel level = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ApprovalLevel not found"));

        if (soft) {
            level.setDeleted(true);
            repository.save(level);
        } else {
            repository.delete(level);
        }
    }
}
