package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "creator_contents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CreatorContent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator creator;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "content_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContentType contentType;
    
    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContentCategory category;
    
    @Column(name = "media_url", nullable = false)
    private String mediaUrl; // URL del video, imagen, etc.
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds; // Para videos
    
    @Column(name = "difficulty_level")
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;
    
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags; // JSON array de tags
    
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;
    
    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Long likeCount = 0L;
    
    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Long commentCount = 0L;
    
    @Column(name = "share_count", nullable = false)
    @Builder.Default
    private Long shareCount = 0L;
    
    @Column(name = "save_count", nullable = false)
    @Builder.Default
    private Long saveCount = 0L;
    
    @Column(name = "earnings", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal earnings = BigDecimal.ZERO;
    
    @Column(name = "is_monetized", nullable = false)
    @Builder.Default
    private Boolean isMonetized = false;
    
    @Column(name = "is_premium", nullable = false)
    @Builder.Default
    private Boolean isPremium = false;
    
    @Column(name = "premium_price", precision = 8, scale = 2)
    private BigDecimal premiumPrice;
    
    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = false;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "moderation_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ModerationStatus moderationStatus = ModerationStatus.PENDING;
    
    @Column(name = "moderation_notes")
    private String moderationNotes;
    
    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis; // Análisis automático de calidad/engagement
    
    @Column(name = "target_audience", columnDefinition = "TEXT")
    private String targetAudience; // JSON con demografía objetivo
    
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContentLike> likes;
    
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContentComment> comments;
    
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContentView> views;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum ContentType {
        VIDEO,
        IMAGE,
        RECIPE,
        WORKOUT,
        ARTICLE,
        LIVE_STREAM,
        STORY,
        REEL
    }
    
    public enum ContentCategory {
        NUTRITION,
        WORKOUT,
        MOTIVATION,
        TRANSFORMATION,
        RECIPE,
        TIPS,
        CHALLENGE,
        LIFESTYLE,
        PRODUCT_REVIEW,
        EDUCATION
    }
    
    public enum DifficultyLevel {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        EXPERT
    }
    
    public enum ModerationStatus {
        PENDING,
        APPROVED,
        REJECTED,
        FLAGGED,
        REMOVED
    }
    
    // Business methods
    public void incrementView() {
        this.viewCount++;
    }
    
    public void incrementLike() {
        this.likeCount++;
    }
    
    public void decrementLike() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
    
    public void incrementComment() {
        this.commentCount++;
    }
    
    public void incrementShare() {
        this.shareCount++;
    }
    
    public void incrementSave() {
        this.saveCount++;
    }
    
    public BigDecimal getEngagementRate() {
        if (viewCount == 0) return BigDecimal.ZERO;
        
        long totalEngagements = likeCount + commentCount + shareCount + saveCount;
        return BigDecimal.valueOf(totalEngagements)
                .divide(BigDecimal.valueOf(viewCount), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    public boolean isEligibleForMonetization() {
        return isPublished && 
               moderationStatus == ModerationStatus.APPROVED && 
               viewCount >= 1000 && 
               getEngagementRate().compareTo(BigDecimal.valueOf(2.0)) >= 0;
    }
}