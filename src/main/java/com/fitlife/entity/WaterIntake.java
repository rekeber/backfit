package com.fitlife.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "water_intake")
public class WaterIntake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "glasses", nullable = false)
    private Integer glasses;

    @Column(name = "goal_glasses")
    private Integer goalGlasses;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public WaterIntake() {
        this.createdAt = LocalDateTime.now();
    }

    public WaterIntake(User user, LocalDate date, Integer glasses) {
        this();
        this.user = user;
        this.date = date;
        this.glasses = glasses;
        this.goalGlasses = 8; // Default goal
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getGlasses() { return glasses; }
    public void setGlasses(Integer glasses) { this.glasses = glasses; }

    public Integer getGoalGlasses() { return goalGlasses; }
    public void setGoalGlasses(Integer goalGlasses) { this.goalGlasses = goalGlasses; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}