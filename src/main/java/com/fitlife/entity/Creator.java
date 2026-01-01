package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "creators")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Creator {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "creator_handle", unique = true, nullable = false)
    private String creatorHandle; // @username único para creators
    
    @Column(name = "tier", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CreatorTier tier = CreatorTier.BRONZE;
    
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;
    
    @Column(name = "follower_count", nullable = false)
    @Builder.Default
    private Long followerCount = 0L;
    
    @Column(name = "total_views", nullable = false)
    @Builder.Default
    private Long totalViews = 0L;
    
    @Column(name = "total_earnings", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalEarnings = BigDecimal.ZERO;
    
    @Column(name = "monthly_earnings", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal monthlyEarnings = BigDecimal.ZERO;
    
    @Column(name = "revenue_share_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal revenueSharePercentage = new BigDecimal("70.00"); // 70% para creator
    
    @Column(name = "specialties", columnDefinition = "TEXT")
    private String specialties; // JSON array: ["nutrition", "strength", "cardio"]
    
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "website_url")
    private String websiteUrl;
    
    @Column(name = "instagram_handle")
    private String instagramHandle;
    
    @Column(name = "youtube_channel")
    private String youtubeChannel;
    
    @Column(name = "tiktok_handle")
    private String tiktokHandle;
    
    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;
    
    @Column(name = "total_ratings", nullable = false)
    @Builder.Default
    private Long totalRatings = 0L;
    
    @Column(name = "content_count", nullable = false)
    @Builder.Default
    private Long contentCount = 0L;
    
    @Column(name = "engagement_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal engagementRate = BigDecimal.ZERO;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "application_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicationStatus applicationStatus = ApplicationStatus.PENDING;
    
    @Column(name = "application_date")
    private LocalDateTime applicationDate;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CreatorContent> contents;
    
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BrandCollaboration> collaborations;
    
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CreatorEarning> earnings;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum CreatorTier {
        BRONZE,    // 0-1K followers
        SILVER,    // 1K-10K followers
        GOLD,      // 10K-100K followers
        DIAMOND    // 100K+ followers
    }
    
    public enum ApplicationStatus {
        PENDING,
        APPROVED,
        REJECTED,
        SUSPENDED
    }
    
    // Business methods
    public void updateTier() {
        if (followerCount >= 100000) {
            this.tier = CreatorTier.DIAMOND;
            this.revenueSharePercentage = new BigDecimal("80.00"); // 80% para Diamond
        } else if (followerCount >= 10000) {
            this.tier = CreatorTier.GOLD;
            this.revenueSharePercentage = new BigDecimal("75.00"); // 75% para Gold
        } else if (followerCount >= 1000) {
            this.tier = CreatorTier.SILVER;
            this.revenueSharePercentage = new BigDecimal("70.00"); // 70% para Silver
        } else {
            this.tier = CreatorTier.BRONZE;
            this.revenueSharePercentage = new BigDecimal("60.00"); // 60% para Bronze
        }
    }
    
    public boolean canReceivePayments() {
        return isActive && applicationStatus == ApplicationStatus.APPROVED && totalEarnings.compareTo(new BigDecimal("10.00")) >= 0;
    }
    
    public void addFollower() {
        this.followerCount++;
        updateTier();
    }
    
    public void removeFollower() {
        if (this.followerCount > 0) {
            this.followerCount--;
            updateTier();
        }
    }
}