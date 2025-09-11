package com.bonnysimon.starter.features.user.dto;

import lombok.Data;

@Data
public class VerifyOtpDTO {
    private String email;
    private String otp;
}
