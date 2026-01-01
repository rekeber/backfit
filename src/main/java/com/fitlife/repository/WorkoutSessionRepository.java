package com.fitlife.repository;

import com.fitlife.entity.WorkoutSession;
import com.fitlife.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {
    
    List<WorkoutSession> findByUserOrderByStartTimeDesc(User user);
    
    Page<WorkoutSession> findByUserOrderByStartTimeDesc(User user, Pageable pageable);
    
    @Query("SELECT ws FROM WorkoutSession ws WHERE ws.user = :user AND ws.startTime >= :startDate AND ws.startTime <= :endDate ORDER BY ws.startTime DESC")
    List<WorkoutSession> findByUserAndDateRange(@Param("user") User user, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(ws) FROM WorkoutSession ws WHERE ws.user = :user AND ws.startTime >= :startDate")
    Long countByUserSince(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT SUM(ws.caloriesBurned) FROM WorkoutSession ws WHERE ws.user = :user AND ws.startTime >= :startDate")
    Integer sumCaloriesBurnedByUserSince(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT SUM(ws.duration) FROM WorkoutSession ws WHERE ws.user = :user AND ws.startTime >= :startDate")
    Integer sumDurationByUserSince(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT ws FROM WorkoutSession ws WHERE ws.user = :user ORDER BY ws.startTime DESC")
    List<WorkoutSession> findRecentByUser(@Param("user") User user, Pageable pageable);
    
    // Dashboard methods
    @Query("SELECT COUNT(ws) FROM WorkoutSession ws WHERE ws.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COALESCE(SUM(ws.caloriesBurned), 0) FROM WorkoutSession ws WHERE ws.user.id = :userId")
    BigDecimal getTotalCaloriesBurnedByUser(@Param("userId") Long userId);
}