package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loot_boxes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LootBox {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "box_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private BoxType boxType;
    
    @Column(name = "rarity", nullable = false)
    @Enumerated(EnumType.STRING)
    private Rarity rarity;
    
    @Column(name = "is_opened", nullable = false)
    @Builder.Default
    private Boolean isOpened = false;
    
    @Column(name = "opened_at")
    private LocalDateTime openedAt;
    
    @Column(name = "earned_from")
    @Enumerated(EnumType.STRING)
    private EarnedFrom earnedFrom;
    
    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents; // JSON con el contenido de la caja
    
    @Column(name = "fitcoins_reward", precision = 10, scale = 2)
    private BigDecimal fitcoinsReward;
    
    @Column(name = "fitgems_reward")
    private Integer fitgemsReward;
    
    @Column(name = "avatar_items", columnDefinition = "TEXT")
    private String avatarItems; // JSON array de items para avatar
    
    @Column(name = "special_rewards", columnDefinition = "TEXT")
    private String specialRewards; // JSON array de recompensas especiales
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum BoxType {
        DAILY,          // Caja diaria gratis
        WORKOUT,        // Por completar workout
        STREAK,         // Por mantener streak
        ACHIEVEMENT,    // Por desbloquear logro
        PREMIUM,        // Caja premium comprada
        LEGENDARY,      // Caja legendaria especial
        SEASONAL,       // Caja de evento temporal
        CREATOR,        // Por crear contenido
        SOCIAL,         // Por interacciones sociales
        CHALLENGE       // Por completar desafío
    }
    
    public enum Rarity {
        COMMON,         // 60% probabilidad
        UNCOMMON,       // 25% probabilidad
        RARE,           // 10% probabilidad
        EPIC,           // 4% probabilidad
        LEGENDARY       // 1% probabilidad
    }
    
    public enum EarnedFrom {
        DAILY_LOGIN,
        WORKOUT_COMPLETED,
        STREAK_MILESTONE,
        ACHIEVEMENT_UNLOCKED,
        PURCHASE,
        SPECIAL_EVENT,
        CONTENT_CREATION,
        SOCIAL_INTERACTION,
        CHALLENGE_COMPLETION,
        REFERRAL_BONUS,
        ADMIN_GIFT
    }
    
    // Business methods
    public void openBox() {
        if (isOpened) {
            throw new IllegalStateException("Box already opened");
        }
        
        if (isExpired()) {
            throw new IllegalStateException("Box has expired");
        }
        
        generateContents();
        this.isOpened = true;
        this.openedAt = LocalDateTime.now();
    }
    
    private void generateContents() {
        // Generar contenido basado en rarity y boxType
        switch (rarity) {
            case COMMON:
                this.fitcoinsReward = BigDecimal.valueOf(10 + Math.random() * 40); // 10-50 coins
                break;
            case UNCOMMON:
                this.fitcoinsReward = BigDecimal.valueOf(50 + Math.random() * 100); // 50-150 coins
                this.fitgemsReward = 1 + (int)(Math.random() * 3); // 1-3 gems
                break;
            case RARE:
                this.fitcoinsReward = BigDecimal.valueOf(150 + Math.random() * 200); // 150-350 coins
                this.fitgemsReward = 3 + (int)(Math.random() * 7); // 3-10 gems
                break;
            case EPIC:
                this.fitcoinsReward = BigDecimal.valueOf(350 + Math.random() * 400); // 350-750 coins
                this.fitgemsReward = 10 + (int)(Math.random() * 15); // 10-25 gems
                break;
            case LEGENDARY:
                this.fitcoinsReward = BigDecimal.valueOf(750 + Math.random() * 1250); // 750-2000 coins
                this.fitgemsReward = 25 + (int)(Math.random() * 50); // 25-75 gems
                break;
        }
        
        // TODO: Generar avatar items y special rewards basado en rarity
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public static LootBox createDailyBox(User user) {
        return LootBox.builder()
                .user(user)
                .boxType(BoxType.DAILY)
                .rarity(Rarity.COMMON)
                .earnedFrom(EarnedFrom.DAILY_LOGIN)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
    }
    
    public static LootBox createWorkoutBox(User user, int workoutIntensity) {
        Rarity rarity = workoutIntensity >= 8 ? Rarity.RARE : 
                       workoutIntensity >= 6 ? Rarity.UNCOMMON : 
                       Rarity.COMMON;
        
        return LootBox.builder()
                .user(user)
                .boxType(BoxType.WORKOUT)
                .rarity(rarity)
                .earnedFrom(EarnedFrom.WORKOUT_COMPLETED)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
    }
    
    public static LootBox createStreakBox(User user, int streakDays) {
        Rarity rarity = streakDays >= 100 ? Rarity.LEGENDARY :
                       streakDays >= 50 ? Rarity.EPIC :
                       streakDays >= 21 ? Rarity.RARE :
                       streakDays >= 7 ? Rarity.UNCOMMON :
                       Rarity.COMMON;
        
        return LootBox.builder()
                .user(user)
                .boxType(BoxType.STREAK)
                .rarity(rarity)
                .earnedFrom(EarnedFrom.STREAK_MILESTONE)
                .build();
    }
}