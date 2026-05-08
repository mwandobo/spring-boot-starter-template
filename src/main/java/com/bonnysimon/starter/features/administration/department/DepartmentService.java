package com.bonnysimon.starter.features.administration.department;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.features.administration.department.dto.CreateDepartmentDTO;
import com.bonnysimon.starter.features.administration.department.dto.DepartmentResponseDTO;
import com.bonnysimon.starter.features.administration.department.DepartmentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bonnysimon.starter.core.dto.PagedResponse;
import com.bonnysimon.starter.core.dto.PaginationDto;
import com.bonnysimon.starter.features.approval.util.ApprovalStatusUtil;
import com.bonnysimon.starter.core.services.CurrentUserService;
import com.bonnysimon.starter.features.approval.dto.ApprovalAwareDTO;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository repository;
    private final ApprovalStatusUtil approvalStatusUtil;
    private final CurrentUserService currentUserService;

    public PagedResponse<DepartmentResponseDTO> findAll(
            PaginationRequest pagination,
            String search
    ) {
        Specification<DepartmentEntity> spec = getEntitySpecification(search);
        boolean hasApprovalMode = approvalStatusUtil.hasApprovalMode(DepartmentEntity.class.getSimpleName());

        Page<DepartmentEntity> page =
                repository.findAll(spec, pagination.toPageable());

        List<DepartmentEntity> entities = page.getContent();

        List<Long> ids = entities.stream()
                        .map(DepartmentEntity::getId)
                        .toList();
        Map<Long, String> statusMap = hasApprovalMode
                        ? approvalStatusUtil.getBulkApprovalStatuses(DepartmentEntity.class.getSimpleName(), ids)
                        : Collections.emptyMap();

      List<DepartmentResponseDTO> result = entities.stream()
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

        return new PagedResponse<>(
                        result,
                        new PaginationDto(
                                page.getTotalElements(),
                                page.getNumber() + 1,
                                page.getSize(),
                                page.getTotalPages()
                        ),
                        hasApprovalMode // or dynamic logic
                );
    }

    private static Specification< DepartmentEntity> getEntitySpecification(String search) {
        Specification< DepartmentEntity> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

        // Optional search filter (case-insensitive)
        if (search != null && !search.trim().isEmpty()) {
            String likePattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), likePattern),
                            cb.like(cb.lower(root.get("description")), likePattern)
                    )
            );
        }
        return spec;
    }

    @Transactional
    public DepartmentResponseDTO create(CreateDepartmentDTO request) {
        repository.findByName(request.getName())
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Department with name '" + request.getName() + "' already exists"
                    );
                });

        DepartmentEntity entity = new DepartmentEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        DepartmentEntity savedEntity = repository.save(entity);

        return  DepartmentResponseDTO.fromEntity(savedEntity);
    }

    public  ApprovalAwareDTO<DepartmentResponseDTO> findOne  (Long  departmentId) {
          DepartmentEntity   department = repository.findById( departmentId)
                 .orElseThrow(() -> new IllegalStateException(" Department not found"));

          DepartmentResponseDTO dto = DepartmentResponseDTO.fromEntity(department);

           return approvalStatusUtil.attachApprovalInfo(
                    dto,
                    department.getId(),
                    DepartmentEntity.class.getSimpleName(),
                    currentUserService.getCurrentUserRoleId()
                );
     }

    @Transactional
    public DepartmentResponseDTO update(Long id, CreateDepartmentDTO request) {
        DepartmentEntity entity = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Department not found with id: " + id
                        )
                );

        repository.findByName(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Department with name '" + request.getName() + "' already exists"
                    );
                });

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        DepartmentEntity updatedEntity = repository.save(entity);

        return  DepartmentResponseDTO.fromEntity(updatedEntity);
    }

    @Transactional
    public void delete(Long id, boolean soft) {
        DepartmentEntity entity = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Department not found with id: " + id
                        )
                );

        if (soft) {
            entity.setDeleted(true);
            repository.save(entity);
        } else {
            repository.delete(entity);
        }
    }
}
