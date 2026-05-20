package com.bonnysimon.starter.features.assetmanagement.assetcategory;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.features.assetmanagement.assetcategory.dto.CreateAssetCategoryDTO;
import com.bonnysimon.starter.features.assetmanagement.assetcategory.dto.AssetCategoryResponseDTO;
import com.bonnysimon.starter.features.assetmanagement.assetcategory.AssetCategoryEntity;
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
public class AssetCategoryService {
    private final AssetCategoryRepository repository;
    private final ApprovalStatusUtil approvalStatusUtil;
    private final CurrentUserService currentUserService;

    public PagedResponse<AssetCategoryResponseDTO> findAll(
            PaginationRequest pagination,
            String search
    ) {
        Specification<AssetCategoryEntity> spec = getEntitySpecification(search);
        boolean hasApprovalMode = approvalStatusUtil.hasApprovalMode(AssetCategoryEntity.class.getSimpleName());

        Page<AssetCategoryEntity> page =
                repository.findAll(spec, pagination.toPageable());

        List<AssetCategoryEntity> entities = page.getContent();

        List<Long> ids = entities.stream()
                        .map(AssetCategoryEntity::getId)
                        .toList();
        Map<Long, String> statusMap = hasApprovalMode
                        ? approvalStatusUtil.getBulkApprovalStatuses(AssetCategoryEntity.class.getSimpleName(), ids)
                        : Collections.emptyMap();

      List<AssetCategoryResponseDTO> result = entities.stream()
                      .map(entity -> {
                          AssetCategoryResponseDTO dto = AssetCategoryResponseDTO.fromEntity(entity);

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

    private static Specification< AssetCategoryEntity> getEntitySpecification(String search) {
        Specification< AssetCategoryEntity> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

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
    public AssetCategoryResponseDTO create(CreateAssetCategoryDTO request) {
        repository.findByName(request.getName())
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "AssetCategory with name '" + request.getName() + "' already exists"
                    );
                });

        AssetCategoryEntity entity = new AssetCategoryEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        AssetCategoryEntity savedEntity = repository.save(entity);

        return  AssetCategoryResponseDTO.fromEntity(savedEntity);
    }

    public  ApprovalAwareDTO<AssetCategoryResponseDTO> findOne  (Long  assetcategoryId) {
          AssetCategoryEntity   assetcategory = repository.findById( assetcategoryId)
                 .orElseThrow(() -> new IllegalStateException(" AssetCategory not found"));

          AssetCategoryResponseDTO dto = AssetCategoryResponseDTO.fromEntity(assetcategory);

           return approvalStatusUtil.attachApprovalInfo(
                    dto,
                    assetcategory.getId(),
                    AssetCategoryEntity.class.getSimpleName(),
                    currentUserService.getCurrentUserRoleId()
                );
     }

    @Transactional
    public AssetCategoryResponseDTO update(Long id, CreateAssetCategoryDTO request) {
        AssetCategoryEntity entity = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "AssetCategory not found with id: " + id
                        )
                );

        repository.findByName(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "AssetCategory with name '" + request.getName() + "' already exists"
                    );
                });

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        AssetCategoryEntity updatedEntity = repository.save(entity);

        return  AssetCategoryResponseDTO.fromEntity(updatedEntity);
    }

    @Transactional
    public void delete(Long id, boolean soft) {
        AssetCategoryEntity entity = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "AssetCategory not found with id: " + id
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
