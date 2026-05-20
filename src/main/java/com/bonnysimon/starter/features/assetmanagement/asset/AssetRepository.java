package com.bonnysimon.starter.features.assetmanagement.asset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<AssetEntity, Long> {
    Optional<AssetEntity> findByName(String name);

     Page<AssetEntity> findAll(
                Specification<AssetEntity> spec,
                Pageable pageable
        );
}
