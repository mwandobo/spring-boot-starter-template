package com.bonnysimon.starter.features.approval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApprovalAwareDTO<T> {
    private T data;
    private boolean hasApprovalMode;
    private String approvalStatus;
    private boolean isMyLevelApproved;
    private boolean shouldApprove;
    private Long currentLevelId;
}
