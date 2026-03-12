package com.bonnysimon.starter.features.auth.dtos;

import lombok.Data;

@Data
public class PasswordRecoveryRequest {
    private String email;
    private String password;
}