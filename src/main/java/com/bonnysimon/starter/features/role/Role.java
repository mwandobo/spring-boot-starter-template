package com.bonnysimon.starter.features.role;

import com.bonnysimon.starter.core.entity.BaseEntity;
import com.bonnysimon.starter.features.permission.Permission;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Data
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name="role_id"),
            inverseJoinColumns = @JoinColumn(name="permission_id")
    )
    private Set<Permission> permissions;

    @Transient
    private String approveStatus;
}
