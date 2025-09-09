package com.bonnysimon.starter.features.role;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.approval.utils.ApprovalStatusUtil;
import com.bonnysimon.starter.features.role.dto.AssignRoleRequest;
import com.bonnysimon.starter.features.role.dto.CreateRoleRequest;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ApprovalStatusUtil approvalStatusUtil; // 👈 inject util


    public PaginationResponse<Role> findAll(PaginationRequest pagination, String search) {
        Specification<Role> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%")
                    )
            );
        }

        Page<Role> roles = roleRepository.findAll(spec, pagination.toPageable());

        roles.forEach(role -> {
            if (approvalStatusUtil.hasApprovalMode("Role")) {
                role.setApproveStatus(
                        approvalStatusUtil.getApprovalStatus("Role", String.valueOf(role.getId()))
                );
            } else {
                role.setApproveStatus("N/A");
            }
        });

        return PaginationResponse.of(roles);
    }

    @Transactional
    public User assignRolesToUser(AssignRoleRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(role);

        return userRepository.save(user);
    }



    @Transactional
    public Role create(CreateRoleRequest request) {
        // Check if role already exists
        roleRepository.findByName(request.getName())
                .ifPresent(r -> {
                    throw new IllegalArgumentException("Role with name '" + request.getName() + "' already exists");
                });

        // Map DTO -> Entity
        Role role = new Role();
        role.setName(request.getName());

        return roleRepository.save(role);
    }

    @Transactional
    public Role update(Long id, CreateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));

        // Check if another role with the same name exists
        roleRepository.findByName(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Role with name '" + request.getName() + "' already exists");
                });

        // Update fields
        role.setName(request.getName());

        return roleRepository.save(role);
    }

    public void delete(Long id, boolean soft) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));

        if (soft) {
            // Soft delete (flag from BaseEntity)
            role.setDeleted(true);
            roleRepository.save(role);
        } else {
            // Hard delete
            roleRepository.delete(role);
        }
    }

    public Role findOne(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));

        if (approvalStatusUtil.hasApprovalMode("Role")) {
            role.setApproveStatus(
                    approvalStatusUtil.getApprovalStatus("Role", String.valueOf(role.getId()))
            );
        } else {
            role.setApproveStatus("N/A");
        }

        return role;
    }
}