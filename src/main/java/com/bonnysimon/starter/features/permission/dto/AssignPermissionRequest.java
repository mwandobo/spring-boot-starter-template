package com.bonnysimon.starter.features.permission.dto;

import lombok.Data;
import java.util.List;

@Data
public class AssignPermissionRequest {
    private Long roleId;
    private List<Long> permissionIds;
}