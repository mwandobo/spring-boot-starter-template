package com.bonnysimon.starter.features.fetch_data;

import com.bonnysimon.starter.features.administration.department.DepartmentEntity;
import com.bonnysimon.starter.features.administration.department.DepartmentRepository;
import com.bonnysimon.starter.features.administration.department.dto.DepartmentResponseDTO;
import com.bonnysimon.starter.features.approval.dto.SysApprovalResponseDTO;
import com.bonnysimon.starter.features.approval.dto.UserApprovalResponseDTO;
import com.bonnysimon.starter.features.approval.entity.SysApproval;
import com.bonnysimon.starter.features.approval.repository.SysApprovalRepository;
import com.bonnysimon.starter.features.approval.util.ApprovalStatusUtil;
import com.bonnysimon.starter.features.role.RoleEntity;
import com.bonnysimon.starter.features.role.RoleRepository;
import com.bonnysimon.starter.features.role.dto.RoleResponseDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Data
@Service
@RequiredArgsConstructor
public class FetchDataService {
    private final ApprovalStatusUtil approvalStatusUtil;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final SysApprovalRepository sysApprovalRepository;


    private <E, D> List<D> fetchData(
            List<E> entities,
            String entityName,
            Function<E, Long> idExtractor,
            Function<E, D> dtoMapper,
            BiConsumer<D, String> approvalSetter
    ) {
        boolean hasApprovalMode = approvalStatusUtil.hasApprovalMode(entityName);

        List<Long> ids = entities.stream()
                .map(idExtractor)
                .toList();

        Map<Long, String> statusMap = hasApprovalMode
                ? approvalStatusUtil.getBulkApprovalStatuses(entityName, ids)
                : Collections.emptyMap();

        return entities.stream()
                .map(entity -> {
                    D dto = dtoMapper.apply(entity);

                    if (hasApprovalMode) {
                        approvalSetter.accept(
                                dto,
                                statusMap.get(idExtractor.apply(entity))
                        );
                    }

                    return dto;
                })
                .toList();
    }


    public List<DepartmentResponseDTO> fetchDepartments() {

        return fetchData(
                departmentRepository.findAll(),
                DepartmentEntity.class.getSimpleName(),
                DepartmentEntity::getId,
                DepartmentResponseDTO::fromEntity,
                DepartmentResponseDTO::setApprovalStatus
        );
    }


    public List<RoleResponseDTO> fetchRoles() {

        return fetchData(
                roleRepository.findAll(),
                RoleEntity.class.getSimpleName(),
                RoleEntity::getId,
                RoleResponseDTO::fromEntity,
                RoleResponseDTO::setApprovalStatus
        );
    }

    public List<SysApprovalResponseDTO> fetchSysApprovals() {

        return fetchData(
                sysApprovalRepository.findAll(),
                SysApproval.class.getSimpleName(),
                SysApproval::getId,
                SysApprovalResponseDTO::fromEntity,
                SysApprovalResponseDTO::setApprovalStatus
        );
    }





//    public List<DepartmentResponseDTO> fetchDepartments() {
//
//        List<DepartmentEntity> list = departmentRepository.findAll();
//        boolean hasApprovalMode = approvalStatusUtil.hasApprovalMode(DepartmentEntity.class.getSimpleName());
//        List<Long> ids = list.stream()
//                .map(DepartmentEntity::getId)
//                .toList();
//        Map<Long, String> statusMap = hasApprovalMode
//                ? approvalStatusUtil.getBulkApprovalStatuses(DepartmentEntity.class.getSimpleName(), ids)
//                : Collections.emptyMap();
//
//        return list.stream()
//                .map(entity -> {
//                    DepartmentResponseDTO dto = DepartmentResponseDTO.fromEntity(entity);
//
//                    if (hasApprovalMode) {
//                        dto.setApprovalStatus(
//                                statusMap.get(entity.getId())
//                        );
//                    }
//
//                    return dto;
//                })
//                .toList();
//    }
}
