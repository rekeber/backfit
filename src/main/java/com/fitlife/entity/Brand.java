package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "brands")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Brand {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "brand_handle", unique = true, nullable = false)
    private String brandHandle; // @brandname único
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "logo_url")
    private String logoUrl;
    
    @Column(name = "website_url")
    private String websiteUrl;
    
    @Column(name = "industry", nullable = false)
    @Enumerated(EnumType.STRING)
    private Industry industry;
    
    @Column(name = "company_size")
    @Enumerated(EnumType.STRING)
    private CompanySize companySize;
    
    @Column(name = "contact_email", nullable = false)
    private String contactEmail;
    
    @Column(name = "contact_phone")
    private String contactPhone;
    
    @Column(name = "contact_person")
    private String contactPerson;
    
    @Column(name = "monthly_budget", precision = 12, scale = 2)
    private BigDecimal monthlyBudget;
    
    @Column(name = "total_spent", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;
    
    @Column(name = "target_demographics", columnDefinition = "TEXT")
    private String targetDemographics; // JSON con targeting
    
    @Column(name = "brand_values", columnDefinition = "TEXT")
    private String brandValues; // JSON array de valores
    
    @Column(name = "preferred_content_types", columnDefinition = "TEXT")
    private String preferredContentTypes; // JSON array
    
    @Column(name = "blacklisted_keywords", columnDefinition = "TEXT")
    private String blacklistedKeywords; // JSON array
    
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "account_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.PENDING;
    
    @Column(name = "verification_date")
    private LocalDateTime verificationDate;
    
    @Column(name = "total_campaigns", nullable = false)
    @Builder.Default
    private Long totalCampaigns = 0L;
    
    @Column(name = "successful_campaigns", nullable = false)
    @Builder.Default
    private Long successfulCampaigns = 0L;
    
    @Column(name = "average_roi", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal averageRoi = BigDecimal.ZERO;
    
    @Column(name = "brand_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal brandRating = BigDecimal.ZERO;
    
    @Column(name = "total_ratings", nullable = false)
    @Builder.Default
    private Long totalRatings = 0L;
    
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BrandCollaboration> collaborations;
    
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AdCampaign> campaigns;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum Industry {
        FITNESS_EQUIPMENT,
        SUPPLEMENTS,
        HEALTHY_FOOD,
        SPORTSWEAR,
        WELLNESS,
        TECHNOLOGY,
        BEAUTY,
        LIFESTYLE,
        MEDICAL,
        OTHER
    }
    
    public enum CompanySize {
        STARTUP,      // 1-10 empleados
        SMALL,        // 11-50 empleados
        MEDIUM,       // 51-200 empleados
        LARGE,        // 201-1000 empleados
        ENTERPRISE    // 1000+ empleados
    }
    
    public enum AccountStatus {
        PENDING,
        APPROVED,
        SUSPENDED,
        BANNED
    }
    
    // Business methods
    public BigDecimal getSuccessRate() {
        if (totalCampaigns == 0) return BigDecimal.ZERO;
        
        return BigDecimal.valueOf(successfulCampaigns)
                .divide(BigDecimal.valueOf(totalCampaigns), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    public boolean canCreateCampaigns() {
        return isActive && 
               accountStatus == AccountStatus.APPROVED && 
               monthlyBudget != null && 
               monthlyBudget.compareTo(BigDecimal.valueOf(100)) >= 0;
    }
    
    public void addCampaign(boolean successful) {
        this.totalCampaigns++;
        if (successful) {
            this.successfulCampaigns++;
        }
    }
    
    public void addSpending(BigDecimal amount) {
        this.totalSpent = this.totalSpent.add(amount);
    }
}