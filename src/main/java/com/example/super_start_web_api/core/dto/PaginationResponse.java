package com.example.super_start_web_api.core.dto;



import org.springframework.data.domain.Page;

import java.util.List;

public record PaginationResponse<T>(
        List<T> content,
        int currentPage,
        int totalPages,
        long totalItems
) {
    public static <T> PaginationResponse<T> of(Page<T> page) {
        return new PaginationResponse<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getTotalPages(),
                page.getTotalElements()
        );
    }
}