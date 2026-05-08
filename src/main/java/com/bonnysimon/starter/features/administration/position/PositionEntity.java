package com.bonnysimon.starter.features.administration.position;

import com.bonnysimon.starter.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import com.bonnysimon.starter.features.administration.department.DepartmentEntity;

@Data
@Entity
@Table(name = "position")
public class PositionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;


}
