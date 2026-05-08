package com.bonnysimon.starter.features.role;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.role.dto.AssignRoleRequest;
import com.bonnysimon.starter.features.role.dto.CreateRoleRequest;
import com.bonnysimon.starter.features.user.UserEntity;
import com.bonnysimon.starter.features.user.UserRepository;
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

    public PaginationResponse<RoleEntity> findAll(PaginationRequest pagination, String search) {
        Specification<RoleEntity> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%")
                    )
            );
        }

        Page<RoleEntity> roleDtos = roleRepository.findAll(spec, pagination.toPageable());

        return PaginationResponse.of(roleDtos);
    }

    @Transactional
    public UserEntity assignRolesToUser(AssignRoleRequest request) {
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        RoleEntity role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        user.setRole(role);

        return userRepository.save(user);
    }


    public RoleEntity findOne(Long id) {
        RoleEntity role = roleRepository.findWithPermissionsById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));

       return role;
    }

    @Transactional
    public RoleEntity create(CreateRoleRequest request) {
        // Check if role already exists
        roleRepository.findByName(request.getName())
                .ifPresent(r -> {
                    throw new IllegalStateException("Role with name '" + request.getName() + "' already exists");
                });

        // Map DTO -> Entity
        RoleEntity role = new RoleEntity();
        role.setName(request.getName());

        return roleRepository.save(role);
    }

    @Transactional
    public RoleEntity update(Long id, CreateRoleRequest request) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Role not found with id: " + id));

        // Check if another role with the same name exists
        roleRepository.findByName(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalStateException("Role with name '" + request.getName() + "' already exists");
                });

        // Update fields
        role.setName(request.getName());

        return roleRepository.save(role);
    }

    public void delete(Long id, boolean soft) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Role not found with id: " + id));

        if (soft) {
            // Soft delete (flag from BaseEntity)
            role.setDeleted(true);
            roleRepository.save(role);
        } else {
            // Hard delete
            roleRepository.delete(role);
        }
    }
}