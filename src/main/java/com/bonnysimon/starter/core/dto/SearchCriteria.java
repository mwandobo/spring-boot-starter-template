package com.bonnysimon.starter.core.dto;

public record SearchCriteria(
        String key,
        String operation,
        Object value
) {}