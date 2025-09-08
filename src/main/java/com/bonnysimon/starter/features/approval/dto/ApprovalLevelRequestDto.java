package com.bonnysimon.starter.features.approval.dto;

import com.bonnysimon.starter.features.approval.enums.StatusEnum;
import lombok.Data;

@Data
public class ApprovalLevelRequestDto {
    private String name;
    private String description;
    private String level;
    private Long userApprovalId;
    private Long roleId;
    private Long userId;
    private StatusEnum status;
}
