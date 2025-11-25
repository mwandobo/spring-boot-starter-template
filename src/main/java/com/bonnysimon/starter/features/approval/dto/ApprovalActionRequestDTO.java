package com.bonnysimon.starter.features.approval.dto;

import com.bonnysimon.starter.features.approval.enums.ApprovalActionEnum;
import com.bonnysimon.starter.features.approval.enums.StatusEnum;
import lombok.Data;

@Data
public class ApprovalActionRequestDTO {
    private String name;
    private ApprovalActionEnum action;
    private String entityName;
    private Long entityId;
    private String description;
    private Long approvalLevelId;
    private StatusEnum status;
}
