package com.bonnysimon.starter.core.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class ResponseMapper {

    public static <T> PagedResponse<T> fromPage(Page<T> page, boolean hasApprovalMode) {
        return new PagedResponse<>(
                page.getContent(),
                new PaginationDto(
                        page.getTotalElements(),
                        page.getNumber() + 1,
                        page.getSize(),
                        page.getTotalPages()
                ),
                hasApprovalMode
        );
    }
}