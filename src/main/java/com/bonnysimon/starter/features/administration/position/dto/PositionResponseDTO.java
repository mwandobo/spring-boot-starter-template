package com.bonnysimon.starter.features.administration.position.dto;

import com.bonnysimon.starter.features.administration.position.PositionEntity;
import java.time.format.DateTimeFormatter;
import lombok.Data;
import com.bonnysimon.starter.features.administration.department.dto.DepartmentResponseDTO;

@Data
public class PositionResponseDTO {
    private Long id;
    private String name;
    private String description;
    private DepartmentResponseDTO department;
    private String departmentName;
    private String approvalStatus;
    private String createdAt;
    private String updatedAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static  PositionResponseDTO fromEntity( PositionEntity position) {
            PositionResponseDTO dto = new PositionResponseDTO();
            dto.setId(position.getId());
            dto.setName(position.getName());
            dto.setDescription(position.getDescription());
            dto.setDepartment(position.getDepartment() != null ? DepartmentResponseDTO.fromEntity(position.getDepartment()) : null);
              dto.setDepartmentName(position.getDepartment() != null ? position.getDepartment().getName() : null);
            dto.setUpdatedAt(position.getUpdatedAt() != null ? position.getUpdatedAt().toString() : null);
            dto.setCreatedAt(position.getCreatedAt() != null ? position.getCreatedAt().toString() : null);
            return dto;
        }
}
