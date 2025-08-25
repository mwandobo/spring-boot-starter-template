package com.bonnysimon.starter.features.auth.dtos;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}