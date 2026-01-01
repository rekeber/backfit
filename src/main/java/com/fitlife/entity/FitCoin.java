package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fitcoins")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FitCoin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "balance_after", precision = 10, scale = 2, nullable = false)
    private BigDecimal balanceAfter;
    
    @Column(name = "source", nullable = false)
    @Enumerated(EnumType.STRING)
    private CoinSource source;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "reference_id")
    private Long referenceId; // ID de la actividad que generó las monedas
    
    @Column(name = "reference_type")
    private String referenceType; // Tipo de referencia (workout, food_entry, etc.)
    
    @Column(name = "multiplier", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal multiplier = BigDecimal.ONE;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Para monedas temporales
    
    @Column(name = "is_bonus", nullable = false)
    @Builder.Default
    private Boolean isBonus = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum TransactionType {
        EARNED,
        SPENT,
        BONUS,
        PENALTY,
        EXPIRED,
        REFUND,
        TRANSFER
    }
    
    public enum CoinSource {
        WORKOUT_COMPLETED,
        FOOD_LOGGED,
        WEIGHT_LOGGED,
        DAILY_LOGIN,
        STREAK_BONUS,
        ACHIEVEMENT_UNLOCKED,
        SOCIAL_INTERACTION,
        CONTENT_CREATED,
        CONTENT_LIKED,
        REFERRAL_BONUS,
        CHALLENGE_COMPLETED,
        MILESTONE_REACHED,
        PREMIUM_BONUS,
        CREATOR_EARNINGS,
        BRAND_COLLABORATION,
        DAILY_SPIN,
        LOOT_BOX,
        PURCHASE_REFUND,
        ADMIN_ADJUSTMENT
    }
    
    // Business methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public static BigDecimal calculateEarnings(CoinSource source, BigDecimal baseAmount, int streakDays, boolean isPremium) {
        BigDecimal amount = baseAmount;
        
        // Streak multiplier
        if (streakDays > 0) {
            BigDecimal streakMultiplier = BigDecimal.valueOf(1.0 + (streakDays * 0.01)); // 1% por día de streak
            amount = amount.multiply(streakMultiplier);
        }
        
        // Premium multiplier
        if (isPremium) {
            amount = amount.multiply(BigDecimal.valueOf(1.5)); // 50% más para premium
        }
        
        return amount;
    }
}