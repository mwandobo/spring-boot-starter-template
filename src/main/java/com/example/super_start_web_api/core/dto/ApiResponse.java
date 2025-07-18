package com.example.super_start_web_api.core.dto;
import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, Instant.now());
    }

    public static ApiResponse<?> error(String message) {
        return new ApiResponse<>(false, message, null, Instant.now());
    }
}