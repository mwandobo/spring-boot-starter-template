package com.bonnysimon.starter.features.approval.dto;

import com.bonnysimon.starter.features.approval.entity.ApprovalAction;
import com.bonnysimon.starter.features.approval.entity.ApprovalLevel;
import com.bonnysimon.starter.features.approval.enums.StatusEnum;
import lombok.Data;

@Data
public class ApprovalActionResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String approvalStatus;
    private String entityName;
    private Long sysApprovalId;
    private ApprovalLevelResponseDTO approvalLevel;
    private String approvalLevelName;
    private StatusEnum status;

    public static ApprovalActionResponseDTO fromEntity(ApprovalAction entity) {
        ApprovalActionResponseDTO dto = new ApprovalActionResponseDTO();
        dto.setName(entity.getName());
        dto.setId(entity.getId());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setApprovalLevel(entity.getApprovalLevel() != null ? ApprovalLevelResponseDTO.fromEntity(entity.getApprovalLevel()) : null);
        dto.setApprovalLevelName(entity.getApprovalLevel() != null ? entity.getApprovalLevel().getName() : null);
        return dto;
    }
}
