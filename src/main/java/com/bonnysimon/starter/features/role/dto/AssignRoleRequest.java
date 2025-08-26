package com.bonnysimon.starter.features.role.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignRoleRequest {
    private Long userId;
    private Long roleId;
}