package com.bonnysimon.starter.core.dto;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp,
        Boolean approvalMode // ✅ new field
) {
    public static <T> ApiResponse<T> success(T data, Boolean approvalMode) {
        return new ApiResponse<>(true, "Success", data, Instant.now(), approvalMode);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, Instant.now(), null);
    }

    public static ApiResponse<?> error(String message) {
        return new ApiResponse<>(false, message, null, Instant.now(), null);
    }
}
