package com.bonnysimon.starter.features.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class OtpVerificationResponse {
    private boolean success;
}