package com.bonnysimon.starter.features.approval.dto;

import com.bonnysimon.starter.features.approval.entity.UserApproval;
import com.bonnysimon.starter.features.approval.enums.StatusEnum;
import lombok.Data;

@Data
public class UserApprovalResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String approvalStatus;
    private String entityName;
    private Long sysApprovalId;
    private SysApprovalResponseDTO sysApproval;
    private String sysApprovalName;
    private StatusEnum status;

    public static UserApprovalResponseDTO fromEntity(UserApproval entity) {
        UserApprovalResponseDTO dto = new UserApprovalResponseDTO();
        dto.setName(entity.getName());
        dto.setId(entity.getId());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setSysApproval(entity.getSysApproval() != null ? SysApprovalResponseDTO.fromEntity(entity.getSysApproval()) : null);
        dto.setSysApprovalName(entity.getSysApproval() != null ? entity.getSysApproval().getName() : null);
        return dto;

    }
}
