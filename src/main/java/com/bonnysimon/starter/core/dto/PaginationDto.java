package com.bonnysimon.starter.core.dto;

public record PaginationDto(
        long total,
        int page,
        int limit,
        int totalPages
) {}