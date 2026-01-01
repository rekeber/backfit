package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "nutrition_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionGoal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer dailyCalories = 2000;
    
    @Column(nullable = false)
    @Builder.Default
    private Double protein = 150.0; // gramos
    
    @Column(nullable = false)
    @Builder.Default
    private Double carbs = 250.0; // gramos
    
    @Column(nullable = false)
    @Builder.Default
    private Double fat = 67.0; // gramos
    
    @Column(nullable = false)
    @Builder.Default
    private Double fiber = 25.0; // gramos
    
    @Column(nullable = false)
    @Builder.Default
    private Double water = 2.0; // litros
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ActivityLevel activityLevel = ActivityLevel.MODERATE;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Goal goal = Goal.MAINTAIN;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum ActivityLevel {
        SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE
    }
    
    public enum Goal {
        LOSE_WEIGHT, MAINTAIN, GAIN_WEIGHT, GAIN_MUSCLE
    }
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}