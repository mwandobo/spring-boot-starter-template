package com.bonnysimon.starter.features.administration.position.dto;

import lombok.Data;

@Data
public class CreatePositionDTO {
    private String name;
    private String description;
    private Long department_id;
}
