package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_avatars")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserAvatar {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "avatar_name")
    private String avatarName;
    
    @Column(name = "body_type")
    @Enumerated(EnumType.STRING)
    private BodyType bodyType;
    
    @Column(name = "skin_tone")
    @Enumerated(EnumType.STRING)
    private SkinTone skinTone;
    
    @Column(name = "hair_style")
    @Enumerated(EnumType.STRING)
    private HairStyle hairStyle;
    
    @Column(name = "hair_color")
    @Enumerated(EnumType.STRING)
    private HairColor hairColor;
    
    @Column(name = "eye_color")
    @Enumerated(EnumType.STRING)
    private EyeColor eyeColor;
    
    @Column(name = "current_outfit")
    private String currentOutfit; // JSON con outfit actual
    
    @Column(name = "unlocked_outfits", columnDefinition = "TEXT")
    private String unlockedOutfits; // JSON array de outfits desbloqueados
    
    @Column(name = "unlocked_accessories", columnDefinition = "TEXT")
    private String unlockedAccessories; // JSON array de accesorios
    
    @Column(name = "current_accessories", columnDefinition = "TEXT")
    private String currentAccessories; // JSON array de accesorios equipados
    
    @Column(name = "fitness_level")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FitnessLevel fitnessLevel = FitnessLevel.BEGINNER;
    
    @Column(name = "muscle_definition")
    @Builder.Default
    private Integer muscleDefinition = 1; // 1-10 scale
    
    @Column(name = "body_fat_percentage")
    private Double bodyFatPercentage;
    
    @Column(name = "avatar_3d_model_url")
    private String avatar3dModelUrl; // URL del modelo 3D
    
    @Column(name = "future_projection_url")
    private String futureProjectionUrl; // URL del avatar "objetivo"
    
    @Column(name = "transformation_progress")
    @Builder.Default
    private Integer transformationProgress = 0; // 0-100%
    
    @Column(name = "total_workouts_completed")
    @Builder.Default
    private Long totalWorkoutsCompleted = 0L;
    
    @Column(name = "total_calories_burned")
    @Builder.Default
    private Long totalCaloriesBurned = 0L;
    
    @Column(name = "total_weight_lost")
    @Builder.Default
    private Double totalWeightLost = 0.0;
    
    @Column(name = "achievements_displayed", columnDefinition = "TEXT")
    private String achievementsDisplayed; // JSON array de achievements mostrados
    
    @Column(name = "last_body_scan_date")
    private LocalDateTime lastBodyScanDate;
    
    @Column(name = "next_evolution_date")
    private LocalDateTime nextEvolutionDate; // Cuándo el avatar puede evolucionar
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum BodyType {
        ECTOMORPH,
        MESOMORPH,
        ENDOMORPH
    }
    
    public enum SkinTone {
        VERY_LIGHT,
        LIGHT,
        MEDIUM_LIGHT,
        MEDIUM,
        MEDIUM_DARK,
        DARK,
        VERY_DARK
    }
    
    public enum HairStyle {
        SHORT,
        MEDIUM,
        LONG,
        CURLY,
        WAVY,
        STRAIGHT,
        BALD,
        BUZZ_CUT,
        PONYTAIL,
        BUN
    }
    
    public enum HairColor {
        BLACK,
        BROWN,
        BLONDE,
        RED,
        GRAY,
        WHITE,
        BLUE,
        GREEN,
        PURPLE,
        PINK
    }
    
    public enum EyeColor {
        BROWN,
        BLUE,
        GREEN,
        HAZEL,
        GRAY,
        AMBER
    }
    
    public enum FitnessLevel {
        BEGINNER,
        NOVICE,
        INTERMEDIATE,
        ADVANCED,
        EXPERT,
        ELITE
    }
    
    // Business methods
    public void updateFitnessLevel() {
        if (totalWorkoutsCompleted >= 500) {
            this.fitnessLevel = FitnessLevel.ELITE;
        } else if (totalWorkoutsCompleted >= 300) {
            this.fitnessLevel = FitnessLevel.EXPERT;
        } else if (totalWorkoutsCompleted >= 150) {
            this.fitnessLevel = FitnessLevel.ADVANCED;
        } else if (totalWorkoutsCompleted >= 75) {
            this.fitnessLevel = FitnessLevel.INTERMEDIATE;
        } else if (totalWorkoutsCompleted >= 25) {
            this.fitnessLevel = FitnessLevel.NOVICE;
        } else {
            this.fitnessLevel = FitnessLevel.BEGINNER;
        }
    }
    
    public void updateMuscleDefinition() {
        // Basado en workouts completados y peso perdido
        int definition = 1;
        
        if (totalWorkoutsCompleted >= 100) definition += 2;
        if (totalWorkoutsCompleted >= 200) definition += 2;
        if (totalWorkoutsCompleted >= 300) definition += 2;
        
        if (totalWeightLost >= 5.0) definition += 1;
        if (totalWeightLost >= 10.0) definition += 1;
        if (totalWeightLost >= 20.0) definition += 1;
        
        this.muscleDefinition = Math.min(definition, 10);
    }
    
    public void updateTransformationProgress(Double currentWeight, Double targetWeight, Double startWeight) {
        if (startWeight != null && targetWeight != null && currentWeight != null) {
            double totalWeightToLose = startWeight - targetWeight;
            double weightLost = startWeight - currentWeight;
            
            if (totalWeightToLose > 0) {
                this.transformationProgress = (int) Math.min(100, (weightLost / totalWeightToLose) * 100);
            }
        }
    }
    
    public boolean canEvolve() {
        return nextEvolutionDate != null && LocalDateTime.now().isAfter(nextEvolutionDate);
    }
    
    public void completeWorkout(int caloriesBurned) {
        this.totalWorkoutsCompleted++;
        this.totalCaloriesBurned += caloriesBurned;
        updateFitnessLevel();
        updateMuscleDefinition();
        
        // Programar próxima evolución
        if (totalWorkoutsCompleted % 10 == 0) { // Cada 10 workouts
            this.nextEvolutionDate = LocalDateTime.now().plusDays(1);
        }
    }
}