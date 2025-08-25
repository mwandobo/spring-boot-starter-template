package com.bonnysimon.starter.core.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PaginationRequest(
        Integer page,
        Integer size,
        String sortBy,
        Sort.Direction direction
) {
    public Pageable toPageable() {
        int pageVal = (page == null || page < 1) ? 1 : page;
        int sizeVal = (size == null || size < 1 || size > 100) ? 10 : size;
        String sortByVal = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort.Direction dirVal = (direction == null) ? Sort.Direction.DESC : direction;

        return PageRequest.of(pageVal - 1, sizeVal, dirVal, sortByVal);
    }
}
