package com.fitlife.repository;

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
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Page<User> findByIsActiveTrueOrderByTotalPointsDesc(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.totalPoints DESC")
    List<User> findByIsActiveTrueOrderByTotalPointsDesc();
    
    Page<User> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);
    
    Long countByIsActiveTrue();
    
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.lastLogin >= :since ORDER BY u.lastLogin DESC")
    Page<User> findActiveUsersSince(@Param("since") LocalDateTime since, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.totalPoints >= :minPoints ORDER BY u.totalPoints DESC")
    Page<User> findUsersByMinPoints(@Param("minPoints") Integer minPoints, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.streakDays >= :minStreak ORDER BY u.streakDays DESC")
    Page<User> findUsersByMinStreak(@Param("minStreak") Integer minStreak, Pageable pageable);
    
    // Métodos adicionales requeridos por SocialService
    List<User> findTop10ByIsActiveTrueAndIdNotOrderByTotalPointsDesc(Long excludeId);
}