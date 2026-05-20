package com.bonnysimon.starter.features.asset_management.asset;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.features.asset_management.asset.dto.CreateAssetDTO;
import com.bonnysimon.starter.features.asset_management.asset.dto.AssetResponseDTO;
import com.bonnysimon.starter.features.asset_management.assetcategory.AssetCategoryEntity;
import com.bonnysimon.starter.features.asset_management.assetcategory.AssetCategoryRepository;
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
public class AssetService {
    private final AssetRepository repository;
    private final AssetCategoryRepository assetcategoryRepository;
    private final ApprovalStatusUtil approvalStatusUtil;
    private final CurrentUserService currentUserService;

    public PagedResponse<AssetResponseDTO> findAll(
            PaginationRequest pagination,
            String search
    ) {
        Specification<AssetEntity> spec = getEntitySpecification(search);
        boolean hasApprovalMode = approvalStatusUtil.hasApprovalMode(AssetEntity.class.getSimpleName());

        Page<AssetEntity> page =
                repository.findAll(spec, pagination.toPageable());

        List<AssetEntity> entities = page.getContent();

        List<Long> ids = entities.stream()
                        .map(AssetEntity::getId)
                        .toList();
        Map<Long, String> statusMap = hasApprovalMode
                        ? approvalStatusUtil.getBulkApprovalStatuses(AssetEntity.class.getSimpleName(), ids)
                        : Collections.emptyMap();

      List<AssetResponseDTO> result = entities.stream()
                      .map(entity -> {
                          AssetResponseDTO dto = AssetResponseDTO.fromEntity(entity);

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

    private static Specification< AssetEntity> getEntitySpecification(String search) {
        Specification< AssetEntity> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

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
    public AssetResponseDTO create(CreateAssetDTO request) {
        repository.findByName(request.getName())
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Asset with name '" + request.getName() + "' already exists"
                    );
                });

        AssetEntity entity = new AssetEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        AssetCategoryEntity assetcategory = validateAssetcategoryExists(request.getAsset_category_id());
        entity.setAssetcategory(assetcategory);
        AssetEntity savedEntity = repository.save(entity);

        return  AssetResponseDTO.fromEntity(savedEntity);
    }

    public  ApprovalAwareDTO<AssetResponseDTO> findOne  (Long  assetId) {
          AssetEntity   asset = repository.findById( assetId)
                 .orElseThrow(() -> new IllegalStateException(" Asset not found"));

          AssetResponseDTO dto = AssetResponseDTO.fromEntity(asset);

           return approvalStatusUtil.attachApprovalInfo(
                    dto,
                    asset.getId(),
                    AssetEntity.class.getSimpleName(),
                    currentUserService.getCurrentUserRoleId()
                );
     }

    @Transactional
    public AssetResponseDTO update(Long id, CreateAssetDTO request) {
        AssetEntity entity = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Asset not found with id: " + id
                        )
                );

        repository.findByName(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Asset with name '" + request.getName() + "' already exists"
                    );
                });

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        AssetCategoryEntity assetcategory = validateAssetcategoryExists(request.getAsset_category_id());
        entity.setAssetcategory(assetcategory);

        AssetEntity updatedEntity = repository.save(entity);

        return  AssetResponseDTO.fromEntity(updatedEntity);
    }

    @Transactional
    public void delete(Long id, boolean soft) {
        AssetEntity entity = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Asset not found with id: " + id
                        )
                );

        if (soft) {
            entity.setDeleted(true);
            repository.save(entity);
        } else {
            repository.delete(entity);
        }
    }

    private AssetCategoryEntity validateAssetcategoryExists(Long id) {
        if (id == null) {
            if ("false" == "false") {
                throw new IllegalArgumentException("Assetcategory ID is required");
            }
            return null;
        }
        return assetcategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Assetcategory not found with id: " + id));
    }

}
