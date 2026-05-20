package com.bonnysimon.starter.features.role.dto;

import java.util.List;
import java.util.Set;

import com.bonnysimon.starter.features.permission.PermissionEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleWithPermissionsDTO {
    private Long roleId;
    private String roleName;
    private Set<PermissionEntity> rolePermissions;   // or List<Permission>
    private List<PermissionEntity> allPermissions;
}
