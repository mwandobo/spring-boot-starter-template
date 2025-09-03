package com.bonnysimon.starter.features.approval.dto;

import com.bonnysimon.starter.features.approval.enums.StatusEnum;
import lombok.Data;

@Data
public class SysApprovalRequestDTO {
    private String name;
    private String description;
    private String entityName;
    private StatusEnum status;
}
