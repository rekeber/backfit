package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ad_campaigns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AdCampaign {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "campaign_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CampaignType campaignType;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CampaignStatus status = CampaignStatus.DRAFT;
    
    @Column(name = "budget", precision = 12, scale = 2, nullable = false)
    private BigDecimal budget;
    
    @Column(name = "spent_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal spentAmount = BigDecimal.ZERO;
    
    @Column(name = "target_demographics", columnDefinition = "TEXT")
    private String targetDemographics; // JSON con targeting
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "impressions")
    @Builder.Default
    private Long impressions = 0L;
    
    @Column(name = "clicks")
    @Builder.Default
    private Long clicks = 0L;
    
    @Column(name = "conversions")
    @Builder.Default
    private Long conversions = 0L;
    
    @Column(name = "ctr", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal ctr = BigDecimal.ZERO; // Click-through rate
    
    @Column(name = "cpc", precision = 8, scale = 4)
    @Builder.Default
    private BigDecimal cpc = BigDecimal.ZERO; // Cost per click
    
    @Column(name = "cpm", precision = 8, scale = 4)
    @Builder.Default
    private BigDecimal cpm = BigDecimal.ZERO; // Cost per mille (1000 impressions)
    
    @Column(name = "roi", precision = 8, scale = 4)
    @Builder.Default
    private BigDecimal roi = BigDecimal.ZERO; // Return on investment
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum CampaignType {
        SPONSORED_CONTENT,    // Contenido patrocinado
        BANNER_ADS,          // Anuncios banner
        VIDEO_ADS,           // Anuncios de video
        INFLUENCER_COLLAB,   // Colaboración con influencers
        NATIVE_ADS,          // Anuncios nativos
        PRODUCT_PLACEMENT,   // Colocación de productos
        STORY_ADS,           // Anuncios en stories
        CHALLENGE_SPONSOR,   // Patrocinio de desafíos
        LIVE_STREAM_SPONSOR, // Patrocinio de live streams
        EMAIL_CAMPAIGN       // Campaña de email
    }
    
    public enum CampaignStatus {
        DRAFT,      // Borrador
        PENDING,    // Pendiente de aprobación
        APPROVED,   // Aprobada
        ACTIVE,     // Activa
        PAUSED,     // Pausada
        COMPLETED,  // Completada
        CANCELLED,  // Cancelada
        REJECTED    // Rechazada
    }
    
    // Business methods
    public void updateMetrics(Long newImpressions, Long newClicks, Long newConversions) {
        this.impressions = newImpressions;
        this.clicks = newClicks;
        this.conversions = newConversions;
        
        // Calculate CTR (Click-Through Rate)
        if (impressions > 0) {
            this.ctr = BigDecimal.valueOf(clicks)
                    .divide(BigDecimal.valueOf(impressions), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        
        // Calculate CPC (Cost Per Click)
        if (clicks > 0) {
            this.cpc = spentAmount.divide(BigDecimal.valueOf(clicks), 4, BigDecimal.ROUND_HALF_UP);
        }
        
        // Calculate CPM (Cost Per Mille)
        if (impressions > 0) {
            this.cpm = spentAmount.multiply(BigDecimal.valueOf(1000))
                    .divide(BigDecimal.valueOf(impressions), 4, BigDecimal.ROUND_HALF_UP);
        }
        
        // Calculate ROI (simplified - would need revenue data)
        if (spentAmount.compareTo(BigDecimal.ZERO) > 0 && conversions > 0) {
            // Assuming average conversion value of $50 for demo
            BigDecimal revenue = BigDecimal.valueOf(conversions).multiply(BigDecimal.valueOf(50));
            this.roi = revenue.subtract(spentAmount)
                    .divide(spentAmount, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }
    
    public void addSpending(BigDecimal amount) {
        this.spentAmount = this.spentAmount.add(amount);
        
        // Update brand total spending
        if (brand != null) {
            brand.addSpending(amount);
        }
    }
    
    public BigDecimal getRemainingBudget() {
        return budget.subtract(spentAmount);
    }
    
    public boolean isActive() {
        return status == CampaignStatus.ACTIVE && 
               LocalDateTime.now().isAfter(startDate) && 
               LocalDateTime.now().isBefore(endDate);
    }
    
    public boolean isBudgetExhausted() {
        return spentAmount.compareTo(budget) >= 0;
    }
    
    public void pause() {
        if (status == CampaignStatus.ACTIVE) {
            this.status = CampaignStatus.PAUSED;
        }
    }
    
    public void resume() {
        if (status == CampaignStatus.PAUSED) {
            this.status = CampaignStatus.ACTIVE;
        }
    }
    
    public void complete() {
        this.status = CampaignStatus.COMPLETED;
    }
}