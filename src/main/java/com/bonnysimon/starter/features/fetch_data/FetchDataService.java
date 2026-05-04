package com.bonnysimon.starter.features.fetch_data;

import com.bonnysimon.starter.features.administration.department.DepartmentEntity;
import com.bonnysimon.starter.features.administration.department.DepartmentRepository;
import com.bonnysimon.starter.features.administration.department.dto.DepartmentResponseDTO;
import com.bonnysimon.starter.features.approval.util.ApprovalStatusUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Service
@RequiredArgsConstructor
public class FetchDataService {
    private final ApprovalStatusUtil approvalStatusUtil;
    private final DepartmentRepository departmentRepository;

    public List<DepartmentResponseDTO> fetchDepartments() {

        List<DepartmentEntity> list = departmentRepository.findAll();
        boolean hasApprovalMode = approvalStatusUtil.hasApprovalMode(DepartmentEntity.class.getSimpleName());
        List<Long> ids = list.stream()
                .map(DepartmentEntity::getId)
                .toList();
        Map<Long, String> statusMap = hasApprovalMode
                ? approvalStatusUtil.getBulkApprovalStatuses(DepartmentEntity.class.getSimpleName(), ids)
                : Collections.emptyMap();

        return list.stream()
                .map(entity -> {
                    DepartmentResponseDTO dto = DepartmentResponseDTO.fromEntity(entity);

                    if (hasApprovalMode) {
                        dto.setApprovalStatus(
                                statusMap.get(entity.getId())
                        );
                    }

                    return dto;
                })
                .toList();
    }
}
