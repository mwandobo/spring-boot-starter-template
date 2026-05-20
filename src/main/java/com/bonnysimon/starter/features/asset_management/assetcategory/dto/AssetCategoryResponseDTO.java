package com.bonnysimon.starter.features.asset_management.assetcategory.dto;

import com.bonnysimon.starter.features.asset_management.assetcategory.AssetCategoryEntity;
import java.time.format.DateTimeFormatter;
import lombok.Data;

@Data
public class AssetCategoryResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String approvalStatus;
    private String createdAt;
    private String updatedAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static  AssetCategoryResponseDTO fromEntity( AssetCategoryEntity assetcategory) {
            AssetCategoryResponseDTO dto = new AssetCategoryResponseDTO();
            dto.setId(assetcategory.getId());
            dto.setName(assetcategory.getName());
            dto.setDescription(assetcategory.getDescription());
            dto.setUpdatedAt(assetcategory.getUpdatedAt() != null ? assetcategory.getUpdatedAt().toString() : null);
            dto.setCreatedAt(assetcategory.getCreatedAt() != null ? assetcategory.getCreatedAt().toString() : null);
            return dto;
        }
}
