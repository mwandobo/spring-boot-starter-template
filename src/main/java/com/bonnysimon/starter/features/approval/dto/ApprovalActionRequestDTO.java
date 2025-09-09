package com.bonnysimon.starter.features.approval.dto;

import com.bonnysimon.starter.features.approval.enums.ApprovalActionEnum;
import com.bonnysimon.starter.features.approval.enums.StatusEnum;
import lombok.Data;

@Data
public class ApprovalActionRequestDTO {
    private String name;
    private String action;
    private String entityName;
    private String entityId;
    private String description;
    private Long approvalLevelId;
    private StatusEnum status;
}
