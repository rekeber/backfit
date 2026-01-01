package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_recommendations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AIRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "recommendation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RecommendationType type;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @Column(name = "is_applied")
    private Boolean isApplied = false;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coaching_session_id")
    private AICoachingSession coachingSession;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum RecommendationType {
        NUTRITION_MEAL,
        NUTRITION_SNACK,
        NUTRITION_HYDRATION,
        WORKOUT_ROUTINE,
        WORKOUT_RECOVERY,
        WELLNESS_MOTIVATION,
        WELLNESS_SLEEP,
        GENERAL_TIP
    }
    
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
}