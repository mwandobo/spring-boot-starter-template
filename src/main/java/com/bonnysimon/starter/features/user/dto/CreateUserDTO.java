package com.bonnysimon.starter.features.user.dto;

import lombok.Data;

@Data
public class CreateUserDTO {
    private String name;
    private String email;
    private Long roleId;
}
