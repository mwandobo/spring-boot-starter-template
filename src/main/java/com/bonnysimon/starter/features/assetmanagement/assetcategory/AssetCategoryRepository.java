package com.bonnysimon.starter.features.assetmanagement.assetcategory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AssetCategoryRepository extends JpaRepository<AssetCategoryEntity, Long> {
    Optional<AssetCategoryEntity> findByName(String name);

     Page<AssetCategoryEntity> findAll(
                Specification<AssetCategoryEntity> spec,
                Pageable pageable
        );
}
