package com.fitlife.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_activities")
public class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activity_type", nullable = false)
    private String activityType; // FOOD_LOGGED, WORKOUT_COMPLETED, WATER_LOGGED, etc.

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "icon_type")
    private String iconType; // For frontend icon selection

    @Column(name = "reference_id")
    private Long referenceId; // ID of related entity (food entry, workout, etc.)

    @Column(name = "reference_type")
    private String referenceType; // Entity type for reference

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public UserActivity() {
        this.createdAt = LocalDateTime.now();
    }

    public UserActivity(User user, String activityType, String title, String description, String iconType) {
        this();
        this.user = user;
        this.activityType = activityType;
        this.title = title;
        this.description = description;
        this.iconType = iconType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconType() { return iconType; }
    public void setIconType(String iconType) { this.iconType = iconType; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}