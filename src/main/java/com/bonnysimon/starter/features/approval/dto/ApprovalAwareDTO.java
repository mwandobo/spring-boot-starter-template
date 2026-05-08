package com.bonnysimon.starter.features.approval.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApprovalAwareDTO<T> {
    @JsonUnwrapped
    private T data;
    private boolean hasApprovalMode;
    private String approvalStatus;
    private boolean isMyLevelApproved;
    private boolean shouldApprove;
    private Long currentLevelId;
}
