package com.bonnysimon.starter.features.asset_management.asset;

import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.features.asset_management.asset.dto.CreateAssetDTO;
import com.bonnysimon.starter.features.asset_management.asset.dto.AssetResponseDTO;
import com.bonnysimon.starter.core.dto.PagedResponse;
import com.bonnysimon.starter.features.approval.dto.ApprovalAwareDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService service;

    @GetMapping
    public PagedResponse<AssetResponseDTO> findAll(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
                return service.findAll(pagination, search);
    }

    @PostMapping
    public AssetResponseDTO  create(
            @RequestBody CreateAssetDTO request
    ) {
        return service.create(request);
    }

     @GetMapping("/{id}")
        public ApprovalAwareDTO<AssetResponseDTO> findOne(
                @PathVariable Long id
        ) {
            return service.findOne(id);
        }

    @PatchMapping("/{id}")
    public AssetResponseDTO update(
            @PathVariable Long id,
            @RequestBody CreateAssetDTO request
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
