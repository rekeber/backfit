package com.fitlife.repository;

import com.fitlife.entity.FoodImageAnalysis;
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
public interface FoodImageAnalysisRepository extends JpaRepository<FoodImageAnalysis, Long> {
    
    List<FoodImageAnalysis> findByUserOrderByCreatedAtDesc(User user);
    
    Page<FoodImageAnalysis> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<FoodImageAnalysis> findByUserAndProcessingStatusOrderByCreatedAtDesc(
            User user, FoodImageAnalysis.ProcessingStatus status);
    
    @Query("SELECT f FROM FoodImageAnalysis f WHERE f.user = :user AND f.createdAt >= :since ORDER BY f.createdAt DESC")
    List<FoodImageAnalysis> findByUserSince(@Param("user") User user, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(f) FROM FoodImageAnalysis f WHERE f.user = :user AND f.processingStatus = :status")
    Long countByUserAndStatus(@Param("user") User user, @Param("status") FoodImageAnalysis.ProcessingStatus status);
    
    @Query("SELECT AVG(f.confidenceScore) FROM FoodImageAnalysis f WHERE f.user = :user AND f.processingStatus = 'COMPLETED'")
    Double getAverageConfidenceScoreByUser(@Param("user") User user);
}