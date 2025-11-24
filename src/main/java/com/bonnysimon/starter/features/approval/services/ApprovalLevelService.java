package com.bonnysimon.starter.features.approval.services;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.approval.dto.ApprovalLevelRequestDTO;
import com.bonnysimon.starter.features.approval.entity.ApprovalLevel;
import com.bonnysimon.starter.features.approval.entity.UserApproval;
import com.bonnysimon.starter.features.approval.repository.ApprovalLevelRepository;
import com.bonnysimon.starter.features.approval.repository.UserApprovalRepository;
import com.bonnysimon.starter.features.role.Role;
import com.bonnysimon.starter.features.role.RoleRepository;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Slf4j
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
    public ApprovalLevel create(ApprovalLevelRequestDTO request) {
        Optional<UserApproval> userApproval = userApprovalRepository.findById(request.getUserApprovalId());
        if (userApproval.isEmpty()) {
            throw new IllegalStateException("User Approval Not Found");
        }

        Optional<Role> role = roleRepository.findById(request.getRoleId());
        if (role.isEmpty()) {
            throw new IllegalStateException("Role Not Found");
        }

        Optional<ApprovalLevel> existing = repository
                .findByRoleIdAndUserApprovalId(request.getRoleId(), request.getUserApprovalId());

        if (existing.isPresent()) {
            throw new IllegalStateException("Approval Level already exists for this role and userApproval");
        }

        int nextLevel = updateApprovalLevelOrder(
                request.getUserApprovalId(),
                "CREATE",
                null
        );



        ApprovalLevel level = new ApprovalLevel();
        level.setName(request.getName());
        level.setDescription(request.getDescription());
        level.setLevel(nextLevel);
        level.setUserApproval(userApproval.get());
        level.setRole(role.get());

        return repository.save(level);
    }

    @Transactional
    public ApprovalLevel update(Long id, ApprovalLevelRequestDTO request) {
        ApprovalLevel level = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ApprovalLevel not found"));

        level.setName(request.getName());
        level.setDescription(request.getDescription());
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
                    .orElseThrow(() -> new IllegalStateException("Role not found"));
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


    @Transactional
    public int updateApprovalLevelOrder(
            Long userApprovalId,
            String action,
            ApprovalLevel affectedLevel
    ) {

        List<ApprovalLevel> levels = repository.findByUserApprovalIdOrderByLevelAsc(userApprovalId);

        if ("CREATE".equalsIgnoreCase(action)) {
            // Return next level
            return levels.isEmpty() ? 1 : levels.get(levels.size() - 1).getLevel() + 1;
        }

        if ("DELETE".equalsIgnoreCase(action) && affectedLevel != null) {
            int deletedLevelNum = affectedLevel.getLevel();

            // Shift levels down after deletion
            for (ApprovalLevel lvl : levels) {
                if (lvl.getLevel() > deletedLevelNum) {
                    lvl.setLevel(lvl.getLevel() - 1);
                    repository.save(lvl); // update
                }
            }

            return deletedLevelNum;
        }

        return 1;
    }

}
