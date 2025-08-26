package com.bonnysimon.starter.features.permission.services;

// feature/permission/service/PermissionAssignmentService.java
//package com.example.feature.permission.service;

//import com.example.feature.permission.dto.AssignPermissionRequest;
//import com.example.feature.permission.Permission;
//import com.example.feature.permission.PermissionRepository;
//import com.example.feature.role.Role;
//import com.example.feature.role.RoleRepository;
import com.bonnysimon.starter.features.permission.Permission;
import com.bonnysimon.starter.features.permission.PermissionRepository;
import com.bonnysimon.starter.features.permission.dto.AssignPermissionRequest;
import com.bonnysimon.starter.features.role.Role;
import com.bonnysimon.starter.features.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionAssignmentService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional
    public Role assignPermissionsToRole(AssignPermissionRequest request) {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());
        role.setPermissions(new HashSet<>(permissions));

        return roleRepository.save(role);
    }
}
