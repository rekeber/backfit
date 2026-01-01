package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_views")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ContentView {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private CreatorContent content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Can be null for anonymous views
    
    @Column(name = "view_duration_seconds")
    private Integer viewDurationSeconds;
    
    @Column(name = "completion_percentage", precision = 5, scale = 2)
    private BigDecimal completionPercentage;
    
    @Column(name = "device_type")
    private String deviceType; // mobile, desktop, tablet
    
    @Column(name = "ip_address")
    private String ipAddress; // For analytics and fraud detection
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Business methods
    public void calculateCompletionPercentage(Integer totalDurationSeconds) {
        if (totalDurationSeconds != null && totalDurationSeconds > 0 && viewDurationSeconds != null) {
            this.completionPercentage = BigDecimal.valueOf(viewDurationSeconds)
                    .divide(BigDecimal.valueOf(totalDurationSeconds), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }
    
    public boolean isHighEngagement() {
        return completionPercentage != null && completionPercentage.compareTo(BigDecimal.valueOf(80)) >= 0;
    }
    
    public boolean isMediumEngagement() {
        return completionPercentage != null && 
               completionPercentage.compareTo(BigDecimal.valueOf(50)) >= 0 &&
               completionPercentage.compareTo(BigDecimal.valueOf(80)) < 0;
    }
    
    public boolean isLowEngagement() {
        return completionPercentage != null && completionPercentage.compareTo(BigDecimal.valueOf(50)) < 0;
    }
}