package com.bonnysimon.starter.features.approval.entity;

import com.bonnysimon.starter.core.entity.BaseEntity;
import com.bonnysimon.starter.features.approval.enums.StatusEnum;
import com.bonnysimon.starter.features.role.Role;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_approvals")
public class UserApproval extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column()
    private String description;

    @OneToOne(fetch = FetchType.EAGER) // 🔹 one-to-one instead of many-to-one
    @JoinColumn(name = "sys_approval_id", unique = true, nullable = false) // 🔹 enforce unique
    private SysApproval sysApproval;

    @Enumerated(EnumType.STRING) // Store enum as text in DB (better readability than ORDINAL)
    @Column(nullable = false)
    private StatusEnum status = StatusEnum.PENDING; // default value
}
