package com.bonnysimon.starter.features.notification;

import com.bonnysimon.starter.core.entity.BaseEntity;
import com.bonnysimon.starter.features.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@Table(name = "notifications")
public class NotificationEntity extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "for_name", length = 255)
    private String forName;

    @Column(name = "for_id", length = 255)
    private Long forId;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "expires_at", length = 255)
    private String expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notified_personnel_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User notifiedPersonnel;

    @Column(name = "redirect_url", length = 255)
    private String redirectUrl;

    @Column(name = "group_name", length = 255)
    private String groupName;
}
