package com.bonnysimon.starter.features.approval.dto;

import com.bonnysimon.starter.features.approval.entity.SysApproval;
import com.bonnysimon.starter.features.approval.entity.UserApproval;
import com.bonnysimon.starter.features.approval.enums.StatusEnum;
import lombok.Data;

@Data
public class SysApprovalResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String approvalStatus;
    private String entityName;
    private StatusEnum status;

    public static SysApprovalResponseDTO fromEntity(SysApproval entity) {
        SysApprovalResponseDTO dto = new SysApprovalResponseDTO();
        dto.setName(entity.getName());
        dto.setId(entity.getId());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
