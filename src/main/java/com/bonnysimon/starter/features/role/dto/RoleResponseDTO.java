package com.bonnysimon.starter.features.role.dto;

import com.bonnysimon.starter.features.permission.PermissionEntity;
import com.bonnysimon.starter.features.permission.dto.PermissionResponseDTO;
import com.bonnysimon.starter.features.role.RoleEntity;
import lombok.Data;

import java.util.List;

@Data
public class RoleResponseDTO {
    private Long id;
    private String name;
    private List<PermissionResponseDTO> permissions;

    public static RoleResponseDTO fromEntity(RoleEntity role) {
        RoleResponseDTO dto = new RoleResponseDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setPermissions(
                role.getPermissions()
                        .stream()
                        .map(PermissionResponseDTO::fromEntity)
                        .toList()
        );
        return dto;
    }
}