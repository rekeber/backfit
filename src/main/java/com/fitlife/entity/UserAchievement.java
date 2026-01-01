package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;
    
    @Column(name = "earned_at", nullable = false)
    @CreatedDate
    private LocalDateTime earnedAt;
    
    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;
    
    @Column(name = "progress", nullable = false)
    @Builder.Default
    private Double progress = 0.0; // Progreso hacia el logro (0.0 - 1.0)
    
    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;
    
    @Column(name = "points_earned")
    private Integer pointsEarned;
    
    // Métodos de negocio
    public void complete() {
        this.isCompleted = true;
        this.progress = 1.0;
        this.earnedAt = LocalDateTime.now();
        this.unlockedAt = LocalDateTime.now();
        if (this.achievement != null) {
            this.pointsEarned = this.achievement.getPoints();
        }
    }
    
    public void updateProgress(Double newProgress) {
        this.progress = Math.max(0.0, Math.min(1.0, newProgress));
        if (this.progress >= 1.0 && !this.isCompleted) {
            complete();
        }
    }
}