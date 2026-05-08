package com.bonnysimon.starter.features.permission.dto;
import com.bonnysimon.starter.features.permission.PermissionEntity;
import lombok.Data;

import java.util.List;

@Data
public class PermissionResponseDTO {
    private Long id;
    private String name;
    private String group;

    public static PermissionResponseDTO fromEntity(PermissionEntity permission) {

        PermissionResponseDTO dto = new PermissionResponseDTO();

        dto.setId(permission.getId());
        dto.setName(permission.getName());
        dto.setGroup(permission.getGroup());

        return dto;
    }
}