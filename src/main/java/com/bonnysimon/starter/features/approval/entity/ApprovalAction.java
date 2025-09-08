package com.bonnysimon.starter.features.approval.entity;

import com.bonnysimon.starter.core.entity.BaseEntity;
import com.bonnysimon.starter.features.approval.enums.StatusEnum;
import com.bonnysimon.starter.features.role.Role;
import com.bonnysimon.starter.features.user.model.User;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "approval_actions")
public class ApprovalAction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column()
    private String action;

    @Column()
    private String entityName;

    @Column()
    private String entityId;

    @Column()
    private String remark;

    @Column()
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approval_level_id")
    private ApprovalLevel approvalLevel;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_approval_id")
    private UserApproval userApproval;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING) // Store enum as text in DB (better readability than ORDINAL)
    @Column(nullable = false)
    private StatusEnum status = StatusEnum.PENDING; // default value
}
