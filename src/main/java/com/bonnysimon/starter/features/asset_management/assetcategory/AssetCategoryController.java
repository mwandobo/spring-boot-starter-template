package com.bonnysimon.starter.features.asset_management.assetcategory;

import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.features.asset_management.assetcategory.dto.CreateAssetCategoryDTO;
import com.bonnysimon.starter.features.asset_management.assetcategory.dto.AssetCategoryResponseDTO;
import com.bonnysimon.starter.core.dto.PagedResponse;
import com.bonnysimon.starter.features.approval.dto.ApprovalAwareDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/asset-categories")
@RequiredArgsConstructor
public class AssetCategoryController {

    private final AssetCategoryService service;

    @GetMapping
    public PagedResponse<AssetCategoryResponseDTO> findAll(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
                return service.findAll(pagination, search);
    }

    @PostMapping
    public AssetCategoryResponseDTO  create(
            @RequestBody CreateAssetCategoryDTO request
    ) {
        return service.create(request);
    }

     @GetMapping("/{id}")
        public ApprovalAwareDTO<AssetCategoryResponseDTO> findOne(
                @PathVariable Long id
        ) {
            return service.findOne(id);
        }

    @PatchMapping("/{id}")
    public AssetCategoryResponseDTO update(
            @PathVariable Long id,
            @RequestBody CreateAssetCategoryDTO request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @RequestParam(name = "soft", defaultValue = "false") boolean soft
    ) {
        service.delete(id, soft);
        return ApiResponse.success(null);
    }
}
