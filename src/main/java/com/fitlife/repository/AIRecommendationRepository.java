package com.fitlife.repository;

import com.fitlife.entity.AIRecommendation;
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
public interface AIRecommendationRepository extends JpaRepository<AIRecommendation, Long> {
    
    List<AIRecommendation> findByUserAndIsReadFalseOrderByPriorityDescCreatedAtDesc(User user);
    
    Page<AIRecommendation> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<AIRecommendation> findByUserAndTypeOrderByCreatedAtDesc(User user, AIRecommendation.RecommendationType type);
    
    @Query("SELECT r FROM AIRecommendation r WHERE r.user = :user AND r.expiresAt > :now ORDER BY r.priority DESC, r.createdAt DESC")
    List<AIRecommendation> findActiveRecommendations(@Param("user") User user, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(r) FROM AIRecommendation r WHERE r.user = :user AND r.isRead = false")
    Long countUnreadByUser(@Param("user") User user);
    
    @Query("SELECT r FROM AIRecommendation r WHERE r.user = :user AND r.priority = :priority AND r.isRead = false ORDER BY r.createdAt DESC")
    List<AIRecommendation> findByUserAndPriorityUnread(
            @Param("user") User user, 
            @Param("priority") AIRecommendation.Priority priority);
}