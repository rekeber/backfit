package com.fitlife.repository;

import com.fitlife.entity.Creator;
import com.fitlife.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreatorRepository extends JpaRepository<Creator, Long> {
    
    Optional<Creator> findByUser(User user);
    
    Optional<Creator> findByCreatorHandle(String creatorHandle);
    
    List<Creator> findByTierAndIsActiveTrue(Creator.CreatorTier tier);
    
    List<Creator> findByIsVerifiedTrueAndIsActiveTrue();
    
    Page<Creator> findByIsActiveTrueOrderByFollowerCountDesc(Pageable pageable);
    
    Page<Creator> findByIsActiveTrueOrderByTotalEarningsDesc(Pageable pageable);
    
    @Query("SELECT c FROM Creator c WHERE c.isActive = true AND c.followerCount >= :minFollowers ORDER BY c.engagementRate DESC")
    Page<Creator> findTopCreatorsByEngagement(@Param("minFollowers") Long minFollowers, Pageable pageable);
    
    @Query("SELECT c FROM Creator c WHERE c.isActive = true AND c.specialties LIKE %:specialty% ORDER BY c.averageRating DESC")
    List<Creator> findBySpecialty(@Param("specialty") String specialty);
    
    @Query("SELECT c FROM Creator c WHERE c.applicationStatus = :status ORDER BY c.applicationDate ASC")
    List<Creator> findByApplicationStatus(@Param("status") Creator.ApplicationStatus status);
    
    @Query("SELECT COUNT(c) FROM Creator c WHERE c.tier = :tier AND c.isActive = true")
    Long countByTier(@Param("tier") Creator.CreatorTier tier);
    
    @Query("SELECT c FROM Creator c WHERE c.isActive = true AND c.totalEarnings >= :minEarnings ORDER BY c.monthlyEarnings DESC")
    List<Creator> findTopEarners(@Param("minEarnings") BigDecimal minEarnings);
    
    @Query("SELECT c FROM Creator c WHERE c.isActive = true AND c.followerCount BETWEEN :minFollowers AND :maxFollowers")
    List<Creator> findByFollowerRange(@Param("minFollowers") Long minFollowers, @Param("maxFollowers") Long maxFollowers);
    
    @Query("SELECT c FROM Creator c WHERE c.isActive = true AND c.averageRating >= :minRating AND c.totalRatings >= :minRatingCount")
    List<Creator> findHighRatedCreators(@Param("minRating") BigDecimal minRating, @Param("minRatingCount") Long minRatingCount);
}