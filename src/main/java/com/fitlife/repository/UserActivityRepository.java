package com.fitlife.repository;

import com.fitlife.entity.UserActivity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    
    List<UserActivity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId AND ua.createdAt >= :since ORDER BY ua.createdAt DESC")
    List<UserActivity> findRecentActivitiesByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since, Pageable pageable);
    
    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.user.id = :userId AND ua.activityType = :activityType")
    Long countByUserIdAndActivityType(@Param("userId") Long userId, @Param("activityType") String activityType);
}