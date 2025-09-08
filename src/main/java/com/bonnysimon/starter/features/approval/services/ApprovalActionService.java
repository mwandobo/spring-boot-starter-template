package com.bonnysimon.starter.features.approval.services;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.core.services.CurrentUserService;
import com.bonnysimon.starter.features.approval.dto.ApprovalActionRequestDTO;
import com.bonnysimon.starter.features.approval.entity.ApprovalAction;
import com.bonnysimon.starter.features.approval.entity.ApprovalLevel;
import com.bonnysimon.starter.features.approval.repository.ApprovalActionRepository;
import com.bonnysimon.starter.features.approval.repository.ApprovalLevelRepository;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApprovalActionService {
    private final ApprovalActionRepository repository;
    private final ApprovalLevelRepository approvalLevelRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public PaginationResponse<ApprovalAction> findAll(PaginationRequest pagination, String search) {
        Specification<ApprovalAction> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%")
            );
        }

        Page<ApprovalAction> approvalLevels = repository.findAll(spec, pagination.toPageable());
        return PaginationResponse.of(approvalLevels);
    }

    @Transactional
    public ApprovalAction create(ApprovalActionRequestDTO request) {

        ApprovalLevel approvalLevel = approvalLevelRepository.findById(request.getApprovalLevelId())
                .orElseThrow(() -> new IllegalArgumentException("Approval Level not found"));

        Long userId = currentUserService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User Not found"));

        ApprovalAction action = new ApprovalAction();
        action.setApprovalLevel(approvalLevel);
        action.setUser(user);
        action.setName(request.getName());
        action.setDescription(request.getDescription());
        action.setAction(request.getAction());
        action.setEntityName(request.getEntityName());
        action.setEntityId(request.getEntityId());

        return repository.save(action);
    }

    @Transactional
    public ApprovalAction update(Long id, ApprovalActionRequestDTO request) {
        ApprovalAction action = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Approval Action not found"));

        ApprovalLevel approvalLevel = approvalLevelRepository.findById(request.getApprovalLevelId())
                .orElseThrow(() -> new IllegalArgumentException("Approval Level not found"));

        Long userId = currentUserService.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User Not found"));

        action.setApprovalLevel(approvalLevel);
        action.setUser(user);
        action.setName(request.getName());
        action.setDescription(request.getDescription());
        action.setAction(request.getAction());
        action.setEntityName(request.getEntityName());
        action.setEntityId(request.getEntityId());

        return repository.save(action);
    }

    @Transactional
    public void delete(Long id, boolean soft) {
        ApprovalAction action = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ApprovalAction not found"));

        if (soft) {
            action.setDeleted(true);
            repository.save(action);
        } else {
            repository.delete(action);
        }
    }
}
