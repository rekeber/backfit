package com.fitlife.repository;

import com.fitlife.entity.Brand;
import com.fitlife.entity.BrandCollaboration;
import com.fitlife.entity.Creator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BrandCollaborationRepository extends JpaRepository<BrandCollaboration, Long> {
    
    // Find collaborations by brand
    List<BrandCollaboration> findByBrandOrderByCreatedAtDesc(Brand brand);
    
    // Find collaborations by creator
    List<BrandCollaboration> findByCreatorOrderByCreatedAtDesc(Creator creator);
    
    // Find collaborations by status
    List<BrandCollaboration> findByStatusOrderByCreatedAtDesc(BrandCollaboration.CollaborationStatus status);
    
    // Find active collaborations for a creator
    @Query("SELECT bc FROM BrandCollaboration bc WHERE bc.creator = :creator AND bc.status IN ('ACCEPTED', 'IN_PROGRESS')")
    List<BrandCollaboration> findActiveCollaborationsByCreator(@Param("creator") Creator creator);
    
    // Find pending collaborations for a creator
    List<BrandCollaboration> findByCreatorAndStatus(Creator creator, BrandCollaboration.CollaborationStatus status);
    
    // Find collaborations by brand and status
    List<BrandCollaboration> findByBrandAndStatus(Brand brand, BrandCollaboration.CollaborationStatus status);
    
    // Find collaborations by collaboration type
    List<BrandCollaboration> findByCollaborationType(BrandCollaboration.CollaborationType collaborationType);
    
    // Find collaborations within date range
    @Query("SELECT bc FROM BrandCollaboration bc WHERE bc.startDate >= :startDate AND bc.endDate <= :endDate")
    List<BrandCollaboration> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    // Find collaborations by budget range
    @Query("SELECT bc FROM BrandCollaboration bc WHERE bc.budget >= :minBudget AND bc.budget <= :maxBudget")
    List<BrandCollaboration> findByBudgetRange(@Param("minBudget") BigDecimal minBudget, 
                                             @Param("maxBudget") BigDecimal maxBudget);
    
    // Find collaborations with high AI match score
    @Query("SELECT bc FROM BrandCollaboration bc WHERE bc.aiMatchScore >= :minScore ORDER BY bc.aiMatchScore DESC")
    List<BrandCollaboration> findByHighMatchScore(@Param("minScore") BigDecimal minScore);
    
    // Find completed collaborations for analytics
    @Query("SELECT bc FROM BrandCollaboration bc WHERE bc.status = 'COMPLETED' AND bc.brand = :brand")
    List<BrandCollaboration> findCompletedCollaborationsByBrand(@Param("brand") Brand brand);
    
    // Find collaborations needing payment
    @Query("SELECT bc FROM BrandCollaboration bc WHERE bc.status = 'COMPLETED' AND bc.paymentStatus = 'PENDING'")
    List<BrandCollaboration> findCollaborationsNeedingPayment();
    
    // Count collaborations by creator
    long countByCreator(Creator creator);
    
    // Count successful collaborations by creator
    @Query("SELECT COUNT(bc) FROM BrandCollaboration bc WHERE bc.creator = :creator AND bc.status = 'COMPLETED' AND bc.creatorRating >= 4")
    long countSuccessfulCollaborationsByCreator(@Param("creator") Creator creator);
    
    // Get total earnings for creator
    @Query("SELECT COALESCE(SUM(bc.creatorFee), 0) FROM BrandCollaboration bc WHERE bc.creator = :creator AND bc.paymentStatus = 'PAID'")
    BigDecimal getTotalEarningsByCreator(@Param("creator") Creator creator);
    
    // Get monthly earnings for creator
    @Query("SELECT COALESCE(SUM(bc.creatorFee), 0) FROM BrandCollaboration bc WHERE bc.creator = :creator AND bc.paymentStatus = 'PAID' AND bc.paymentDate >= :startOfMonth")
    BigDecimal getMonthlyEarningsByCreator(@Param("creator") Creator creator, @Param("startOfMonth") LocalDateTime startOfMonth);
    
    // Find trending collaborations (high engagement)
    @Query("SELECT bc FROM BrandCollaboration bc WHERE bc.status = 'COMPLETED' AND bc.actualMetrics IS NOT NULL ORDER BY bc.createdAt DESC")
    Page<BrandCollaboration> findTrendingCollaborations(Pageable pageable);
    
    // Find collaborations expiring soon
    @Query("SELECT bc FROM BrandCollaboration bc WHERE bc.status IN ('ACCEPTED', 'IN_PROGRESS') AND bc.deadline <= :deadline")
    List<BrandCollaboration> findCollaborationsExpiringSoon(@Param("deadline") LocalDateTime deadline);
    
    // Check if creator has existing collaboration with brand
    boolean existsByBrandAndCreatorAndStatusIn(Brand brand, Creator creator, List<BrandCollaboration.CollaborationStatus> statuses);
    
    // Find collaborations by brand and creator
    List<BrandCollaboration> findByBrandAndCreator(Brand brand, Creator creator);
    
    // Find collaborations by brand with pagination
    Page<BrandCollaboration> findByBrandOrderByCreatedAtDesc(Brand brand, Pageable pageable);
    
    // Find collaborations by brand and status with pagination
    Page<BrandCollaboration> findByBrandAndStatus(Brand brand, BrandCollaboration.CollaborationStatus status, Pageable pageable);
    
    // Get total spent by brand
    @Query("SELECT COALESCE(SUM(bc.budget), 0) FROM BrandCollaboration bc WHERE bc.brand = :brand AND bc.paymentStatus = 'PAID'")
    BigDecimal getTotalSpentByBrand(@Param("brand") Brand brand);
    
    // Count collaborations by brand
    long countByBrand(Brand brand);
    
    // Count successful collaborations by brand
    @Query("SELECT COUNT(bc) FROM BrandCollaboration bc WHERE bc.brand = :brand AND bc.status = 'COMPLETED' AND bc.brandRating >= 4")
    long countSuccessfulCollaborationsByBrand(@Param("brand") Brand brand);
}