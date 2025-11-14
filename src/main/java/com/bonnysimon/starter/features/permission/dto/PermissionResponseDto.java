package com.bonnysimon.starter.features.permission.dto;
import com.bonnysimon.starter.features.permission.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponseDto {
    private Long id;
    private String name;
    private String group;
    private String description;

    public PermissionResponseDto(Permission permission) {
        this.id = permission.getId();
        this.name = permission.getName();
        this.group = permission.getGroup();
        this.description = permission.getDescription();
    }

    public static PermissionResponseDto fromEntity(Permission permission) {
        return new PermissionResponseDto(
                permission.getId(),
                permission.getName(),
                permission.getGroup(),
                permission.getDescription()
        );
    }
}
