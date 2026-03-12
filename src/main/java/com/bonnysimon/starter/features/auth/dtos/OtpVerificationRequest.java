package com.bonnysimon.starter.features.auth.dtos;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String otp;
}