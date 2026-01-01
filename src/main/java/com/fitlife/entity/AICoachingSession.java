package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ai_coaching_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AICoachingSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "session_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionType sessionType;
    
    @Column(name = "user_message", columnDefinition = "TEXT")
    private String userMessage;
    
    @Column(name = "ai_response", columnDefinition = "TEXT")
    private String aiResponse;
    
    @Column(name = "context_data", columnDefinition = "TEXT")
    private String contextData; // JSON string with user context
    
    @Column(name = "sentiment_score")
    private Double sentimentScore;
    
    @Column(name = "satisfaction_rating")
    private Integer satisfactionRating; // 1-5 stars
    
    @Column(name = "session_duration_seconds")
    private Integer sessionDurationSeconds;
    
    @OneToMany(mappedBy = "coachingSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AIRecommendation> generatedRecommendations;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum SessionType {
        NUTRITION_QUERY,
        MEAL_PLANNING,
        MOTIVATION_SUPPORT,
        PROGRESS_REVIEW,
        GENERAL_CHAT
    }
}