package com.bonnysimon.starter.features.permission.services;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.permission.PermissionEntity;
import com.bonnysimon.starter.features.permission.PermissionRepository;
import com.bonnysimon.starter.features.permission.dto.AssignPermissionRequest;
import com.bonnysimon.starter.features.role.RoleEntity;
import com.bonnysimon.starter.features.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public PaginationResponse<PermissionEntity> findAll(PaginationRequest pagination, String search) {
        Specification<PermissionEntity> spec = (root, query, cb) ->
                cb.or(
                        cb.isFalse(root.get("deleted")),
                        cb.isNull(root.get("deleted"))
                );

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%")
                            )
            );
        }

        Page<PermissionEntity> permissionDtos = permissionRepository.findAll(spec, pagination.toPageable());

        return PaginationResponse.of(permissionDtos);
    }

    @Transactional
    public RoleEntity assignPermissionsToRole(AssignPermissionRequest request) {
        RoleEntity role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new IllegalStateException("Role not found"));

        List<PermissionEntity> permissions = permissionRepository.findAllById(request.getPermissionIds());
        role.setPermissions(new HashSet<>(permissions));

        return roleRepository.save(role);
    }
}
