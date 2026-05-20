package com.bonnysimon.starter.features.role;

import com.bonnysimon.starter.core.dto.PagedResponse;
import com.bonnysimon.starter.core.dto.PaginationDto;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.core.services.CurrentUserService;
import com.bonnysimon.starter.features.approval.util.ApprovalStatusUtil;
import com.bonnysimon.starter.features.permission.PermissionEntity;
import com.bonnysimon.starter.features.permission.PermissionRepository;
import com.bonnysimon.starter.features.role.dto.AssignPermissionsRequestDTO;
import com.bonnysimon.starter.features.role.dto.AssignRoleRequest;
import com.bonnysimon.starter.features.role.dto.CreateRoleRequest;
import com.bonnysimon.starter.features.role.dto.RoleResponseDTO;
import com.bonnysimon.starter.features.role.dto.RoleWithPermissionsDTO;
import com.bonnysimon.starter.features.user.UserEntity;
import com.bonnysimon.starter.features.user.UserRepository;
import com.bonnysimon.starter.features.user.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Permission;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoleService {

        private final UserRepository userRepository;
        private final RoleRepository repository;
        private final RoleRepository roleRepository;
        private final PermissionRepository permissionRepository;
        private final ApprovalStatusUtil approvalStatusUtil;

        public PagedResponse<RoleResponseDTO> findAll(
                        PaginationRequest pagination,
                        String search) {
                Specification<RoleEntity> spec = getEntitySpecification(search);
                boolean hasApprovalMode = approvalStatusUtil.hasApprovalMode(RoleEntity.class.getSimpleName());

                Page<RoleEntity> page = repository.findAll(spec, pagination.toPageable());

                List<RoleEntity> entities = page.getContent();

                List<Long> ids = entities.stream()
                                .map(RoleEntity::getId)
                                .toList();
                Map<Long, String> statusMap = hasApprovalMode
                                ? approvalStatusUtil.getBulkApprovalStatuses(UserEntity.class.getSimpleName(), ids)
                                : Collections.emptyMap();

                List<RoleResponseDTO> result = entities.stream()
                                .map(entity -> {
                                        RoleResponseDTO dto = RoleResponseDTO.fromEntity(entity);

                                        if (hasApprovalMode) {
                                                dto.setApprovalStatus(
                                                                statusMap.get(entity.getId()));
                                        }

                                        return dto;
                                })
                                .toList();

                return new PagedResponse<>(
                                result,
                                new PaginationDto(
                                                page.getTotalElements(),
                                                page.getNumber() + 1,
                                                page.getSize(),
                                                page.getTotalPages()),
                                hasApprovalMode // or dynamic logic
                );
        }

        private static Specification<RoleEntity> getEntitySpecification(String search) {
                Specification<RoleEntity> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

                // Optional search filter (case-insensitive)
                if (search != null && !search.trim().isEmpty()) {
                        String likePattern = "%" + search.trim().toLowerCase() + "%";
                        spec = spec.and((root, query, cb) -> cb.or(
                                        cb.like(cb.lower(root.get("title")), likePattern),
                                        cb.like(cb.lower(root.get("description")), likePattern)));
                }
                return spec;
        }

        @Transactional
        public UserEntity assignRolesToUser(AssignRoleRequest request) {
                UserEntity user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new IllegalStateException("User not found"));

                RoleEntity role = repository.findById(request.getRoleId())
                                .orElseThrow(() -> new IllegalStateException("User not found"));
                user.setRole(role);

                return userRepository.save(user);
        }

        // @Transactional
        // public RoleResponseDTO assignPermissions(Long roleId,
        // AssignPermissionsRequestDTO request) {
        // RoleEntity role = repository.findById(roleId)
        // .orElseThrow(() -> new IllegalStateException("Role not found"));

        // // UserEntity user = userRepository.findById(request.getUserId())
        // // .orElseThrow(() -> new IllegalStateException("User not found"));

        // user.setRole(role);

        // return userRepository.save(user);
        // }

        @Transactional
        public RoleResponseDTO assignPermissions(Long roleId, AssignPermissionsRequestDTO request) {

                // Fetch the role
                RoleEntity role = roleRepository.findById(roleId)
                                .orElseThrow(() -> new IllegalStateException("Role not found with id: " + roleId));

                // Fetch all permissions by their IDs
                List<PermissionEntity> permissions = permissionRepository.findAllById(request.getPermissions());

                // Validate that all requested permissions exist
                if (permissions.size() != request.getPermissions().size()) {
                        throw new IllegalArgumentException("One or more permissions not found");
                }

                // Clear existing permissions and assign new ones
                role.getPermissions().clear();
                role.getPermissions().addAll(permissions);

                // Save and return
                RoleEntity savedRole = roleRepository.save(role);

                return RoleResponseDTO.fromEntity(savedRole);
        }

        public RoleResponseDTO findOne(Long id) {
                RoleEntity role = repository.findWithPermissionsById(id)
                                .orElseThrow(() -> new IllegalStateException("User not found"));

                return RoleResponseDTO.fromEntity(role);
        }

        @Transactional
        public RoleEntity create(CreateRoleRequest request) {
                // Check if role already exists
                repository.findByName(request.getName())
                                .ifPresent(r -> {
                                        throw new IllegalStateException(
                                                        "Role with name '" + request.getName() + "' already exists");
                                });

                // Map DTO -> Entity
                RoleEntity role = new RoleEntity();
                role.setName(request.getName());

                return repository.save(role);
        }

        @Transactional
        public RoleEntity update(Long id, CreateRoleRequest request) {
                RoleEntity role = repository.findById(id)
                                .orElseThrow(() -> new IllegalStateException("Role not found with id: " + id));

                // Check if another role with the same name exists
                repository.findByName(request.getName())
                                .filter(existing -> !existing.getId().equals(id))
                                .ifPresent(existing -> {
                                        throw new IllegalStateException(
                                                        "Role with name '" + request.getName() + "' already exists");
                                });

                // Update fields
                role.setName(request.getName());

                return repository.save(role);
        }

        public void delete(Long id, boolean soft) {
                RoleEntity role = repository.findById(id)
                                .orElseThrow(() -> new IllegalStateException("Role not found with id: " + id));

                if (soft) {
                        // Soft delete (flag from BaseEntity)
                        role.setDeleted(true);
                        repository.save(role);
                } else {
                        // Hard delete
                        repository.delete(role);
                }
        }

        // public Map<String, Object> getRoleWithPermissions(Long roleId) {

        // // Fetch all permissions
        // List<PermissionEntity> allPermissions = permissionRepository.findAll();

        // // Fetch role with permissions
        // RoleEntity role = roleRepository.findById(roleId)
        // .orElseThrow(() -> new RuntimeException("Role not found"));

        // return Map.of(
        // "roleId", role.getId(),
        // "roleName", role.getName(),
        // "rolePermissions", role.getPermissions(),
        // "allPermissions", allPermissions
        // );
        // }

        /**
         * Get a role with its permissions + all available permissions in the system
         */
        public RoleWithPermissionsDTO getRoleWithPermissions(Long roleId) {

                // Fetch all permissions
                List<PermissionEntity> allPermissions = permissionRepository.findAll();

                // Fetch role with its permissions (eagerly loaded)
                RoleEntity role = roleRepository.findRoleWithPermissions(roleId)
                                .orElseThrow(() -> new RuntimeException("Role not found"));

                return RoleWithPermissionsDTO.builder()
                                .roleId(role.getId())
                                .roleName(role.getName())
                                .rolePermissions(role.getPermissions())
                                .allPermissions(allPermissions)
                                .build();
        }

}