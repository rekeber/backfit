package com.fitlife.repository;

import com.fitlife.entity.Brand;
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
public interface BrandRepository extends JpaRepository<Brand, Long> {
    
    // Find brand by handle
    Optional<Brand> findByBrandHandle(String brandHandle);
    
    // Find brand by contact email
    Optional<Brand> findByContactEmail(String contactEmail);
    
    // Find brands by industry
    List<Brand> findByIndustryOrderByCreatedAtDesc(Brand.Industry industry);
    
    // Find brands by company size
    List<Brand> findByCompanySizeOrderByCreatedAtDesc(Brand.CompanySize companySize);
    
    // Find brands by account status
    List<Brand> findByAccountStatusOrderByCreatedAtDesc(Brand.AccountStatus accountStatus);
    
    // Find active brands
    List<Brand> findByIsActiveTrueOrderByCreatedAtDesc();
    
    // Find verified brands
    List<Brand> findByIsVerifiedTrueOrderByCreatedAtDesc();
    
    // Find brands by verification status and activity
    List<Brand> findByIsVerifiedAndIsActiveOrderByCreatedAtDesc(Boolean isVerified, Boolean isActive);
    
    // Find brands with monthly budget range
    @Query("SELECT b FROM Brand b WHERE b.monthlyBudget >= :minBudget AND b.monthlyBudget <= :maxBudget")
    List<Brand> findByMonthlyBudgetRange(@Param("minBudget") BigDecimal minBudget, 
                                        @Param("maxBudget") BigDecimal maxBudget);
    
    // Find brands with high spending
    @Query("SELECT b FROM Brand b WHERE b.totalSpent >= :minSpent ORDER BY b.totalSpent DESC")
    List<Brand> findByHighSpending(@Param("minSpent") BigDecimal minSpent);
    
    // Find brands with high success rate
    @Query("SELECT b FROM Brand b WHERE b.totalCampaigns > 0 AND (b.successfulCampaigns * 100.0 / b.totalCampaigns) >= :minSuccessRate")
    List<Brand> findByHighSuccessRate(@Param("minSuccessRate") Double minSuccessRate);
    
    // Find brands with high rating
    @Query("SELECT b FROM Brand b WHERE b.brandRating >= :minRating ORDER BY b.brandRating DESC")
    List<Brand> findByHighRating(@Param("minRating") BigDecimal minRating);
    
    // Find brands needing verification
    @Query("SELECT b FROM Brand b WHERE b.accountStatus = 'PENDING' AND b.isVerified = false")
    List<Brand> findBrandsNeedingVerification();
    
    // Find brands with recent activity
    @Query("SELECT b FROM Brand b WHERE b.updatedAt >= :since ORDER BY b.updatedAt DESC")
    List<Brand> findBrandsWithRecentActivity(@Param("since") LocalDateTime since);
    
    // Find brands by target demographics (JSON search would be database-specific)
    @Query("SELECT b FROM Brand b WHERE b.targetDemographics LIKE %:demographic%")
    List<Brand> findByTargetDemographic(@Param("demographic") String demographic);
    
    // Find brands by preferred content types
    @Query("SELECT b FROM Brand b WHERE b.preferredContentTypes LIKE %:contentType%")
    List<Brand> findByPreferredContentType(@Param("contentType") String contentType);
    
    // Count brands by industry
    @Query("SELECT b.industry, COUNT(b) FROM Brand b GROUP BY b.industry")
    List<Object[]> countBrandsByIndustry();
    
    // Count brands by company size
    @Query("SELECT b.companySize, COUNT(b) FROM Brand b GROUP BY b.companySize")
    List<Object[]> countBrandsByCompanySize();
    
    // Count brands by account status
    @Query("SELECT b.accountStatus, COUNT(b) FROM Brand b GROUP BY b.accountStatus")
    List<Object[]> countBrandsByAccountStatus();
    
    // Get average monthly budget by industry
    @Query("SELECT b.industry, AVG(b.monthlyBudget) FROM Brand b WHERE b.monthlyBudget IS NOT NULL GROUP BY b.industry")
    List<Object[]> getAverageMonthlyBudgetByIndustry();
    
    // Get total spending by industry
    @Query("SELECT b.industry, SUM(b.totalSpent) FROM Brand b GROUP BY b.industry")
    List<Object[]> getTotalSpendingByIndustry();
    
    // Find top spending brands
    @Query("SELECT b FROM Brand b ORDER BY b.totalSpent DESC")
    Page<Brand> findTopSpendingBrands(Pageable pageable);
    
    // Find most successful brands
    @Query("SELECT b FROM Brand b WHERE b.totalCampaigns > 0 ORDER BY (b.successfulCampaigns * 100.0 / b.totalCampaigns) DESC, b.totalCampaigns DESC")
    Page<Brand> findMostSuccessfulBrands(Pageable pageable);
    
    // Find brands with highest ROI
    @Query("SELECT b FROM Brand b ORDER BY b.averageRoi DESC")
    Page<Brand> findBrandsWithHighestROI(Pageable pageable);
    
    // Find brands by name (search)
    @Query("SELECT b FROM Brand b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Brand> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Find brands by contact person
    @Query("SELECT b FROM Brand b WHERE LOWER(b.contactPerson) LIKE LOWER(CONCAT('%', :contactPerson, '%'))")
    List<Brand> findByContactPersonContainingIgnoreCase(@Param("contactPerson") String contactPerson);
    
    // Find brands with website
    List<Brand> findByWebsiteUrlIsNotNull();
    
    // Find brands without logo
    List<Brand> findByLogoUrlIsNull();
    
    // Find brands created in date range
    @Query("SELECT b FROM Brand b WHERE b.createdAt >= :startDate AND b.createdAt <= :endDate")
    List<Brand> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    // Find brands verified in date range
    @Query("SELECT b FROM Brand b WHERE b.verificationDate >= :startDate AND b.verificationDate <= :endDate")
    List<Brand> findByVerificationDateBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    // Check if brand handle exists (case insensitive)
    @Query("SELECT COUNT(b) > 0 FROM Brand b WHERE LOWER(b.brandHandle) = LOWER(:brandHandle)")
    boolean existsByBrandHandleIgnoreCase(@Param("brandHandle") String brandHandle);
    
    // Check if contact email exists
    boolean existsByContactEmail(String contactEmail);
}