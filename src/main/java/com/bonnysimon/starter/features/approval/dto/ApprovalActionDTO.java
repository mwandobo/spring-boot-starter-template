package com.bonnysimon.starter.features.approval.dto;

import com.bonnysimon.starter.features.approval.enums.StatusEnum;
import lombok.Data;

@Data
public class ApprovalActionDTO {
    private String name;
    private String description;
    private Long userApprovalId;
    private StatusEnum status;
}
