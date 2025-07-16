package com.example.super_start_web_api.features.auth.dtos;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}