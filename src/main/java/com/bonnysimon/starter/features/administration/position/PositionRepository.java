package com.bonnysimon.starter.features.administration.position;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<PositionEntity, Long> {
    Optional<PositionEntity> findByName(String name);

     Page<PositionEntity> findAll(
                Specification<PositionEntity> spec,
                Pageable pageable
        );
}
