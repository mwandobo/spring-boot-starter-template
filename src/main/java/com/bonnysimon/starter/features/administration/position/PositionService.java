package com.bonnysimon.starter.features.administration.position;
import com.bonnysimon.starter.features.administration.department.DepartmentRepository;
import com.bonnysimon.starter.features.administration.department.DepartmentEntity;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.features.administration.position.dto.CreatePositionDTO;
import com.bonnysimon.starter.features.administration.position.dto.PositionResponseDTO;
import com.bonnysimon.starter.features.administration.position.PositionEntity;
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
public class PositionService {
    private final PositionRepository repository;
    private final DepartmentRepository departmentRepository;
    private final ApprovalStatusUtil approvalStatusUtil;
    private final CurrentUserService currentUserService;

    public PagedResponse<PositionResponseDTO> findAll(
            PaginationRequest pagination,
            String search
    ) {
        Specification<PositionEntity> spec = getEntitySpecification(search);
        boolean hasApprovalMode = approvalStatusUtil.hasApprovalMode(PositionEntity.class.getSimpleName());

        Page<PositionEntity> page =
                repository.findAll(spec, pagination.toPageable());

        List<PositionEntity> entities = page.getContent();

        List<Long> ids = entities.stream()
                .map(PositionEntity::getId)
                .toList();
        Map<Long, String> statusMap = hasApprovalMode
                ? approvalStatusUtil.getBulkApprovalStatuses(PositionEntity.class.getSimpleName(), ids)
                : Collections.emptyMap();

        List<PositionResponseDTO> result = entities.stream()
                .map(entity -> {
                    PositionResponseDTO dto = PositionResponseDTO.fromEntity(entity);

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

    private static Specification<PositionEntity> getEntitySpecification(String search) {
        Specification<PositionEntity> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

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
    public PositionResponseDTO create(CreatePositionDTO request) {
        repository.findByName(request.getName())
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Position with name '" + request.getName() + "' already exists"
                    );
                });

        PositionEntity entity = new PositionEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        DepartmentEntity department = validateDepartmentExists(request.getDepartment_id());
        entity.setDepartment(department);
        PositionEntity savedEntity = repository.save(entity);

        return PositionResponseDTO.fromEntity(savedEntity);
    }

    public ApprovalAwareDTO<PositionResponseDTO> findOne(Long positionId) {
        PositionEntity position = repository.findById(positionId)
                .orElseThrow(() -> new IllegalStateException(" Position not found"));

        PositionResponseDTO dto = PositionResponseDTO.fromEntity(position);

        return approvalStatusUtil.attachApprovalInfo(
                dto,
                position.getId(),
                PositionEntity.class.getSimpleName(),
                currentUserService.getCurrentUserRoleId()
        );
    }

    @Transactional
    public PositionResponseDTO update(Long id, CreatePositionDTO request) {
        PositionEntity entity = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Position not found with id: " + id
                        )
                );

        repository.findByName(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Position with name '" + request.getName() + "' already exists"
                    );
                });

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        DepartmentEntity department = validateDepartmentExists(request.getDepartment_id());
        entity.setDepartment(department);

        PositionEntity updatedEntity = repository.save(entity);

        return PositionResponseDTO.fromEntity(updatedEntity);
    }

    @Transactional
    public void delete(Long id, boolean soft) {
        PositionEntity entity = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Position not found with id: " + id
                        )
                );

        if (soft) {
            entity.setDeleted(true);
            repository.save(entity);
        } else {
            repository.delete(entity);
        }
    }


    private DepartmentEntity validateDepartmentExists(Long id) {
        if (id == null) {
            if ("false" == "false") {
                throw new IllegalArgumentException("Department ID is required");
            }
            return null;
        }
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Department not found with id: " + id));
    }

}
