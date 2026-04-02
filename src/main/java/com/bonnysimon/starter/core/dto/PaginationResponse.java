package com.bonnysimon.starter.core.dto;



import org.springframework.data.domain.Page;

import java.util.List;

public record PaginationResponse<T>(
        List<T> data,
        Pagination pagination
) {

    public record Pagination(
            long total,
            int page,
            int limit,
            int totalPages
    ) {}

    public static <T> PaginationResponse<T> of(Page<T> page) {
        return new PaginationResponse<>(
                page.getContent(),
                new Pagination(
                        page.getTotalElements(),
                        page.getNumber() + 1,
                        page.getSize(),
                        page.getTotalPages()
                )
        );
    }
}