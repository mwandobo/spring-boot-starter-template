package com.bonnysimon.starter.features.user.dto;

import lombok.Data;

@Data
public class CreateUserDTO {
    private String name;
    private String description;
    private Long department_id;
    private String email;
    private Long role_id;
    private Boolean isRecoveryRequested;
    private Boolean isOtpVerified;
    private String phone;
    private String otp;
    private String password;
}
