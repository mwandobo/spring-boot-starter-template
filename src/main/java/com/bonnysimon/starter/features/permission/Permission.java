package com.bonnysimon.starter.features.permission;

import com.bonnysimon.starter.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column()
    private String name;

    @Column()
    private String description;

    @Column()
    private String group;
}
