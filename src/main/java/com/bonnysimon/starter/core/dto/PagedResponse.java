package com.bonnysimon.starter.core.dto;
import java.util.List;

public record PagedResponse<T>(
        List<T> data,
        PaginationDto pagination,
        boolean hasApprovalMode
) { }
