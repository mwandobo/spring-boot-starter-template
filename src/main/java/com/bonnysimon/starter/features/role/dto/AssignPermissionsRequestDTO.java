package com.bonnysimon.starter.features.role.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignPermissionsRequestDTO {
    private List<Long> permissions;
}