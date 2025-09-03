package com.bonnysimon.starter.features.approval.dto;

import com.bonnysimon.starter.features.approval.enums.StatusEnum;
import lombok.Data;

@Data
public class UserApprovalRequestDTO {
    private String name;
    private String description;
    private String entityName;
    private Long sysApprovalId;
    private StatusEnum status;
}
