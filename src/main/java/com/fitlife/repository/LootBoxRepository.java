package com.fitlife.repository;

import com.fitlife.entity.LootBox;
import com.fitlife.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LootBoxRepository extends JpaRepository<LootBox, Long> {
    
    // Find loot boxes by user
    List<LootBox> findByUserOrderByCreatedAtDesc(User user);
    
    // Find unopened loot boxes by user
    List<LootBox> findByUserAndIsOpenedFalseOrderByCreatedAtDesc(User user);
    
    // Find opened loot boxes by user
    List<LootBox> findByUserAndIsOpenedTrueOrderByOpenedAtDesc(User user);
    
    // Find loot boxes by type
    List<LootBox> findByBoxTypeOrderByCreatedAtDesc(LootBox.BoxType boxType);
    
    // Find loot boxes by user and type
    List<LootBox> findByUserAndBoxTypeOrderByCreatedAtDesc(User user, LootBox.BoxType boxType);
    
    // Find loot boxes by user, type and date range
    List<LootBox> findByUserAndBoxTypeAndCreatedAtAfter(User user, LootBox.BoxType boxType, LocalDateTime after);
    
    // Find loot boxes by rarity
    List<LootBox> findByRarityOrderByCreatedAtDesc(LootBox.Rarity rarity);
    
    // Find loot boxes by earned from
    List<LootBox> findByEarnedFromOrderByCreatedAtDesc(LootBox.EarnedFrom earnedFrom);
    
    // Find expired loot boxes
    @Query("SELECT lb FROM LootBox lb WHERE lb.expiresAt IS NOT NULL AND lb.expiresAt < :now AND lb.isOpened = false")
    List<LootBox> findExpiredLootBoxes(@Param("now") LocalDateTime now);
    
    // Find loot boxes expiring soon
    @Query("SELECT lb FROM LootBox lb WHERE lb.expiresAt IS NOT NULL AND lb.expiresAt <= :deadline AND lb.expiresAt > :now AND lb.isOpened = false")
    List<LootBox> findLootBoxesExpiringSoon(@Param("now") LocalDateTime now, @Param("deadline") LocalDateTime deadline);
    
    // Count unopened loot boxes by user
    long countByUserAndIsOpenedFalse(User user);
    
    // Count loot boxes by user and type
    long countByUserAndBoxType(User user, LootBox.BoxType boxType);
    
    // Count opened loot boxes by user in date range
    @Query("SELECT COUNT(lb) FROM LootBox lb WHERE lb.user = :user AND lb.isOpened = true AND lb.openedAt >= :startDate AND lb.openedAt <= :endDate")
    long countOpenedLootBoxesByUserInDateRange(@Param("user") User user, 
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    // Find user's daily loot box for today
    @Query("SELECT lb FROM LootBox lb WHERE lb.user = :user AND lb.boxType = 'DAILY' AND lb.createdAt >= :todayStart AND lb.createdAt < :tomorrowStart")
    Optional<LootBox> findTodaysDailyLootBox(@Param("user") User user, 
                                           @Param("todayStart") LocalDateTime todayStart, 
                                           @Param("tomorrowStart") LocalDateTime tomorrowStart);
    
    // Find recent loot boxes by user (for history)
    Page<LootBox> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find loot boxes by user and opened status with pagination
    Page<LootBox> findByUserAndIsOpenedOrderByCreatedAtDesc(User user, Boolean isOpened, Pageable pageable);
    
    // Get loot box statistics for user
    @Query("SELECT lb.boxType, lb.rarity, COUNT(lb) FROM LootBox lb WHERE lb.user = :user AND lb.isOpened = true GROUP BY lb.boxType, lb.rarity")
    List<Object[]> getLootBoxStatsByUser(@Param("user") User user);
    
    // Find premium loot boxes (purchased)
    @Query("SELECT lb FROM LootBox lb WHERE lb.earnedFrom = 'PURCHASE' ORDER BY lb.createdAt DESC")
    List<LootBox> findPremiumLootBoxes();
    
    // Find seasonal/event loot boxes
    @Query("SELECT lb FROM LootBox lb WHERE lb.boxType IN ('SEASONAL', 'LEGENDARY') AND lb.isOpened = false")
    List<LootBox> findSpecialEventLootBoxes();
    
    // Clean up expired loot boxes (for scheduled task)
    @Query("DELETE FROM LootBox lb WHERE lb.expiresAt < :cutoffDate AND lb.isOpened = false")
    void deleteExpiredLootBoxes(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find loot boxes that can be auto-opened (no expiry or not expired)
    @Query("SELECT lb FROM LootBox lb WHERE lb.user = :user AND lb.isOpened = false AND (lb.expiresAt IS NULL OR lb.expiresAt > :now)")
    List<LootBox> findOpenableLootBoxesByUser(@Param("user") User user, @Param("now") LocalDateTime now);
}