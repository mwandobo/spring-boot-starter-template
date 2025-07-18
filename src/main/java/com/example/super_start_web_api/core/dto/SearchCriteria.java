package com.example.super_start_web_api.core.dto;

public record SearchCriteria(
        String key,
        String operation,
        Object value
) {}