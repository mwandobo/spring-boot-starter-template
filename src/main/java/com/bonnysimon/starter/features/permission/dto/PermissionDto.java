package com.bonnysimon.starter.features.permission.dto;

import lombok.Data;

import java.util.List;

@Data
public class PermissionDto {
    private String name;
    private String group;
    private String description;
}