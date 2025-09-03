package com.bonnysimon.starter.features.approval.entity;

import com.bonnysimon.starter.core.entity.BaseEntity;
import com.bonnysimon.starter.features.approval.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sys_approvals")
public class SysApproval extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column()
    private String description;

    @Column()
    private String entityName;

    @Enumerated(EnumType.STRING) // Store enum as text in DB (better readability than ORDINAL)
    @Column(nullable = false)
    private StatusEnum status = StatusEnum.PENDING; // default value
}
