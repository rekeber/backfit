package com.fitlife.repository;

import com.fitlife.entity.AICoachingSession;
import com.fitlife.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AICoachingSessionRepository extends JpaRepository<AICoachingSession, Long> {
    
    List<AICoachingSession> findByUserOrderByCreatedAtDesc(User user);
    
    Page<AICoachingSession> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<AICoachingSession> findByUserAndSessionTypeOrderByCreatedAtDesc(
            User user, AICoachingSession.SessionType sessionType);
    
    @Query("SELECT s FROM AICoachingSession s WHERE s.user = :user AND s.createdAt >= :since ORDER BY s.createdAt DESC")
    List<AICoachingSession> findByUserSince(@Param("user") User user, @Param("since") LocalDateTime since);
    
    @Query("SELECT AVG(s.satisfactionRating) FROM AICoachingSession s WHERE s.user = :user AND s.satisfactionRating IS NOT NULL")
    Double getAverageSatisfactionByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(s) FROM AICoachingSession s WHERE s.user = :user AND s.sessionType = :type")
    Long countByUserAndType(@Param("user") User user, @Param("type") AICoachingSession.SessionType type);
}