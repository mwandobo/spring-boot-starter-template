package com.bonnysimon.starter.features.approval.dto;

import com.bonnysimon.starter.features.approval.entity.ApprovalLevel;
import com.bonnysimon.starter.features.approval.entity.UserApproval;
import com.bonnysimon.starter.features.approval.enums.StatusEnum;
import com.bonnysimon.starter.features.role.dto.RoleResponseDTO;

import lombok.Data;

@Data
public class ApprovalLevelResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String approvalStatus;
    private String entityName;
    private Long sysApprovalId;
    private Integer level;
    private UserApprovalResponseDTO userApproval;
    private String userApprovalName;
    private RoleResponseDTO role;
    private String roleName;
    private StatusEnum status;

    public static ApprovalLevelResponseDTO fromEntity(ApprovalLevel entity) {
        ApprovalLevelResponseDTO dto = new ApprovalLevelResponseDTO();
        dto.setName(entity.getName());
        dto.setId(entity.getId());
        dto.setLevel(entity.getLevel());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setUserApproval(entity.getUserApproval() != null ? UserApprovalResponseDTO.fromEntity(entity.getUserApproval()) : null);
        dto.setUserApprovalName(entity.getUserApproval() != null ? entity.getUserApproval().getName() : null);
        dto.setRole(entity.getRole() != null ? RoleResponseDTO.fromEntity(entity.getRole()) : null);
        dto.setRoleName(entity.getRole() != null ? entity.getRole().getName() : null);
        return dto;
    }
}
