package com.fitlife.repository;

import com.fitlife.entity.User;
import com.fitlife.entity.Achievement;
import com.fitlife.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    
    List<UserAchievement> findByUserOrderByUnlockedAtDesc(User user);
    
    List<UserAchievement> findByUser(User user);
    
    List<UserAchievement> findByUserAndIsCompletedTrueOrderByUnlockedAtDesc(User user);
    
    List<UserAchievement> findByUserAndIsCompletedFalseOrderByUnlockedAtDesc(User user);
    
    List<UserAchievement> findByUserAndUnlockedAtAfterOrderByUnlockedAtDesc(User user, LocalDateTime since);
    
    boolean existsByUserAndAchievement(User user, Achievement achievement);
    
    long countByUserAndIsCompletedTrue(User user);
    
    long countByUser(User user);
    
    // Dashboard methods
    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}