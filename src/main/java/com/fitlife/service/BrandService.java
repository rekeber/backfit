package com.fitlife.service;

import com.fitlife.entity.*;
import com.fitlife.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BrandService {
    
    private final BrandRepository brandRepository;
    private final BrandCollaborationRepository collaborationRepository;
    private final CreatorRepository creatorRepository;
    private final UserRepository userRepository;
    private final CreatorEconomyService creatorEconomyService;
    
    // Brand Management
    public Brand registerBrand(Brand brand) {
        // Check if brand handle is available
        if (brandRepository.findByBrandHandle(brand.getBrandHandle()).isPresent()) {
            throw new RuntimeException("Brand handle already taken");
        }
        
        // Check if contact email is already registered
        if (brandRepository.findByContactEmail(brand.getContactEmail()).isPresent()) {
            throw new RuntimeException("Contact email already registered");
        }
        
        brand.setAccountStatus(Brand.AccountStatus.PENDING);
        brand = brandRepository.save(brand);
        
        log.info("Brand registered: {} with handle {}", brand.getName(), brand.getBrandHandle());
        return brand;
    }
    
    public Brand approveBrand(Long brandId, String adminEmail) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found"));
        
        if (brand.getAccountStatus() != Brand.AccountStatus.PENDING) {
            throw new RuntimeException("Brand is not pending approval");
        }
        
        brand.setAccountStatus(Brand.AccountStatus.APPROVED);
        brand.setIsVerified(true);
        brand.setVerificationDate(LocalDateTime.now());
        
        brand = brandRepository.save(brand);
        
        log.info("Brand {} approved by admin {}", brand.getName(), adminEmail);
        return brand;
    }
    
    // Collaboration Management
    public BrandCollaboration createCollaboration(
            String brandEmail, 
            Long creatorId, 
            BrandCollaboration.CollaborationType collaborationType,
            String title,
            String description,
            String requirements,
            String deliverables,
            BigDecimal budget,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime deadline,
            String targetMetrics) {
        
        Brand brand = getBrandByContactEmail(brandEmail);
        Creator creator = creatorRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));
        
        if (!brand.canCreateCampaigns()) {
            throw new RuntimeException("Brand cannot create campaigns");
        }
        
        if (!creator.getIsActive() || creator.getApplicationStatus() != Creator.ApplicationStatus.APPROVED) {
            throw new RuntimeException("Creator is not active or approved");
        }
        
        // Check if there's already an active collaboration between brand and creator
        List<BrandCollaboration.CollaborationStatus> activeStatuses = Arrays.asList(
                BrandCollaboration.CollaborationStatus.PROPOSED,
                BrandCollaboration.CollaborationStatus.ACCEPTED,
                BrandCollaboration.CollaborationStatus.IN_PROGRESS
        );
        
        boolean hasActiveCollaboration = collaborationRepository.existsByBrandAndCreatorAndStatusIn(
                brand, creator, activeStatuses);
        
        if (hasActiveCollaboration) {
            throw new RuntimeException("There's already an active collaboration with this creator");
        }
        
        // Calculate creator fee (brand pays platform fee)
        BigDecimal creatorFee = budget.multiply(creator.getRevenueSharePercentage())
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal platformFee = budget.subtract(creatorFee);
        
        // Calculate AI match score
        BigDecimal aiMatchScore = calculateCreatorMatch(brandEmail, creatorId, 
                collaborationType.toString(), targetMetrics, budget);
        
        BrandCollaboration collaboration = BrandCollaboration.builder()
                .brand(brand)
                .creator(creator)
                .collaborationType(collaborationType)
                .title(title)
                .description(description)
                .requirements(requirements)
                .deliverables(deliverables)
                .budget(budget)
                .creatorFee(creatorFee)
                .platformFee(platformFee)
                .startDate(startDate)
                .endDate(endDate)
                .deadline(deadline)
                .targetMetrics(targetMetrics)
                .aiMatchScore(aiMatchScore)
                .status(BrandCollaboration.CollaborationStatus.PROPOSED)
                .paymentStatus(BrandCollaboration.PaymentStatus.PENDING)
                .build();
        
        collaboration = collaborationRepository.save(collaboration);
        
        // Update brand campaign count
        brand.setTotalCampaigns(brand.getTotalCampaigns() + 1);
        brandRepository.save(brand);
        
        log.info("Collaboration created between brand {} and creator {} for ${}", 
                brand.getName(), creator.getCreatorHandle(), budget);
        
        return collaboration;
    }
    
    public BrandCollaboration approveCollaboration(Long collaborationId, String brandEmail) {
        Brand brand = getBrandByContactEmail(brandEmail);
        BrandCollaboration collaboration = collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new RuntimeException("Collaboration not found"));
        
        if (!collaboration.getBrand().equals(brand)) {
            throw new RuntimeException("Brand not authorized to approve this collaboration");
        }
        
        if (collaboration.getStatus() != BrandCollaboration.CollaborationStatus.PROPOSED) {
            throw new RuntimeException("Collaboration is not in proposed status");
        }
        
        collaboration.setStatus(BrandCollaboration.CollaborationStatus.ACCEPTED);
        collaboration = collaborationRepository.save(collaboration);
        
        log.info("Collaboration {} approved by brand {}", collaborationId, brand.getName());
        return collaboration;
    }
    
    public BrandCollaboration completeCollaboration(
            Long collaborationId, 
            String brandEmail, 
            String actualMetrics,
            Integer brandRating,
            String brandFeedback) {
        
        Brand brand = getBrandByContactEmail(brandEmail);
        BrandCollaboration collaboration = collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new RuntimeException("Collaboration not found"));
        
        if (!collaboration.getBrand().equals(brand)) {
            throw new RuntimeException("Brand not authorized to complete this collaboration");
        }
        
        if (collaboration.getStatus() != BrandCollaboration.CollaborationStatus.IN_PROGRESS) {
            throw new RuntimeException("Collaboration is not in progress");
        }
        
        collaboration.setStatus(BrandCollaboration.CollaborationStatus.COMPLETED);
        collaboration.setActualMetrics(actualMetrics);
        collaboration.setBrandRating(brandRating);
        collaboration.setBrandFeedback(brandFeedback);
        
        collaboration = collaborationRepository.save(collaboration);
        
        // Update brand success metrics
        if (brandRating >= 4) {
            brand.setSuccessfulCampaigns(brand.getSuccessfulCampaigns() + 1);
        }
        brand.addCampaign(brandRating >= 4);
        brandRepository.save(brand);
        
        // Create creator earning record
        createCreatorEarning(collaboration);
        
        log.info("Collaboration {} completed by brand {} with rating {}", 
                collaborationId, brand.getName(), brandRating);
        
        return collaboration;
    }
    
    // Creator Discovery for Brands
    public Page<Object> discoverCreatorsForBrand(
            String specialty, 
            String minTier, 
            Long minFollowers, 
            BigDecimal minEngagementRate, 
            Pageable pageable) {
        
        // This would typically use a more sophisticated query
        // For now, using the existing creator discovery method
        Creator.CreatorTier tier = minTier != null ? Creator.CreatorTier.valueOf(minTier) : null;
        Page<Creator> creators = creatorEconomyService.discoverCreators(specialty, tier, pageable);
        
        // Filter by additional criteria
        List<Object> filteredCreators = creators.getContent().stream()
                .filter(creator -> minFollowers == null || creator.getFollowerCount() >= minFollowers)
                .filter(creator -> minEngagementRate == null || 
                        creator.getEngagementRate().compareTo(minEngagementRate) >= 0)
                .map(creator -> {
                    Map<String, Object> creatorMap = new HashMap<>();
                    creatorMap.put("id", creator.getId());
                    creatorMap.put("creatorHandle", creator.getCreatorHandle());
                    creatorMap.put("tier", creator.getTier());
                    creatorMap.put("followerCount", creator.getFollowerCount());
                    creatorMap.put("engagementRate", creator.getEngagementRate());
                    creatorMap.put("averageRating", creator.getAverageRating());
                    creatorMap.put("specialties", creator.getSpecialties());
                    creatorMap.put("bio", creator.getBio());
                    return creatorMap;
                })
                .collect(java.util.stream.Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(filteredCreators, pageable, filteredCreators.size());
    }
    
    // AI Matching Algorithm
    public BigDecimal calculateCreatorMatch(
            String brandEmail, 
            Long creatorId, 
            String campaignType,
            String targetAudience, 
            BigDecimal budget) {
        
        Brand brand = getBrandByContactEmail(brandEmail);
        Creator creator = creatorRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));
        
        BigDecimal matchScore = BigDecimal.ZERO;
        
        // Industry alignment (30% weight)
        if (isIndustryMatch(brand.getIndustry(), creator.getSpecialties())) {
            matchScore = matchScore.add(BigDecimal.valueOf(30));
        }
        
        // Follower count vs budget (25% weight)
        BigDecimal followerBudgetRatio = calculateFollowerBudgetMatch(creator.getFollowerCount(), budget);
        matchScore = matchScore.add(followerBudgetRatio.multiply(BigDecimal.valueOf(0.25)));
        
        // Engagement rate (20% weight)
        BigDecimal engagementScore = creator.getEngagementRate().multiply(BigDecimal.valueOf(20));
        matchScore = matchScore.add(engagementScore);
        
        // Creator rating (15% weight)
        BigDecimal ratingScore = creator.getAverageRating().multiply(BigDecimal.valueOf(3)); // Scale to 15
        matchScore = matchScore.add(ratingScore);
        
        // Previous collaboration success (10% weight)
        BigDecimal collaborationScore = calculateCollaborationHistory(brand, creator);
        matchScore = matchScore.add(collaborationScore);
        
        // Ensure score is between 0 and 100
        matchScore = matchScore.min(BigDecimal.valueOf(100)).max(BigDecimal.ZERO);
        
        return matchScore.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    // Analytics
    public Object getBrandAnalytics(String brandEmail) {
        Brand brand = getBrandByContactEmail(brandEmail);
        
        List<BrandCollaboration> completedCollaborations = 
                collaborationRepository.findCompletedCollaborationsByBrand(brand);
        
        BigDecimal totalSpent = collaborationRepository.getTotalSpentByBrand(brand);
        Long totalCollaborations = collaborationRepository.countByBrand(brand);
        Long successfulCollaborations = collaborationRepository.countSuccessfulCollaborationsByBrand(brand);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("brandId", brand.getId());
        analytics.put("totalCampaigns", brand.getTotalCampaigns());
        analytics.put("successfulCampaigns", brand.getSuccessfulCampaigns());
        analytics.put("successRate", brand.getSuccessRate());
        analytics.put("totalSpent", totalSpent);
        analytics.put("averageRoi", brand.getAverageRoi());
        analytics.put("brandRating", brand.getBrandRating());
        analytics.put("totalCollaborations", totalCollaborations);
        analytics.put("successfulCollaborations", successfulCollaborations);
        analytics.put("monthlyBudget", brand.getMonthlyBudget());
        analytics.put("remainingBudget", brand.getMonthlyBudget().subtract(totalSpent));
        
        return analytics;
    }
    
    public Page<BrandCollaboration> getBrandCollaborations(
            String brandEmail, 
            BrandCollaboration.CollaborationStatus status, 
            Pageable pageable) {
        
        Brand brand = getBrandByContactEmail(brandEmail);
        
        if (status != null) {
            return collaborationRepository.findByBrandAndStatus(brand, status, pageable);
        } else {
            return collaborationRepository.findByBrandOrderByCreatedAtDesc(brand, pageable);
        }
    }
    
    // Helper methods
    private Brand getBrandByContactEmail(String email) {
        return brandRepository.findByContactEmail(email)
                .orElseThrow(() -> new RuntimeException("Brand not found: " + email));
    }
    
    private void createCreatorEarning(BrandCollaboration collaboration) {
        CreatorEarning earning = CreatorEarning.builder()
                .creator(collaboration.getCreator())
                .collaboration(collaboration)
                .earningType(CreatorEarning.EarningType.COLLABORATION_FEE)
                .amount(collaboration.getCreatorFee())
                .status(CreatorEarning.PaymentStatus.PENDING)
                .build();
        
        // This would be saved via CreatorEarningRepository
        // creatorEarningRepository.save(earning);
        
        log.info("Creator earning created for collaboration {} amount ${}", 
                collaboration.getId(), collaboration.getCreatorFee());
    }
    
    private boolean isIndustryMatch(Brand.Industry brandIndustry, String creatorSpecialties) {
        if (creatorSpecialties == null) return false;
        
        String specialties = creatorSpecialties.toLowerCase();
        
        switch (brandIndustry) {
            case FITNESS_EQUIPMENT:
            case SPORTSWEAR:
                return specialties.contains("strength") || specialties.contains("cardio") || 
                       specialties.contains("equipment");
            case SUPPLEMENTS:
                return specialties.contains("nutrition") || specialties.contains("supplements");
            case HEALTHY_FOOD:
                return specialties.contains("nutrition") || specialties.contains("cooking") ||
                       specialties.contains("diet");
            case WELLNESS:
                return specialties.contains("wellness") || specialties.contains("mindfulness") ||
                       specialties.contains("yoga");
            default:
                return true; // Generic match for other industries
        }
    }
    
    private BigDecimal calculateFollowerBudgetMatch(Long followers, BigDecimal budget) {
        // Simple algorithm: optimal ratio is $1 per 100 followers
        BigDecimal optimalBudget = BigDecimal.valueOf(followers).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal ratio = budget.divide(optimalBudget.max(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
        
        // Score is highest when ratio is close to 1
        if (ratio.compareTo(BigDecimal.valueOf(0.5)) >= 0 && ratio.compareTo(BigDecimal.valueOf(2.0)) <= 0) {
            return BigDecimal.valueOf(100).subtract(ratio.subtract(BigDecimal.ONE).abs().multiply(BigDecimal.valueOf(50)));
        } else {
            return BigDecimal.valueOf(50); // Lower score for extreme ratios
        }
    }
    
    private BigDecimal calculateCollaborationHistory(Brand brand, Creator creator) {
        List<BrandCollaboration> pastCollaborations = collaborationRepository.findByBrandAndCreator(brand, creator);
        
        if (pastCollaborations.isEmpty()) {
            return BigDecimal.valueOf(5); // Neutral score for no history
        }
        
        long successfulCount = pastCollaborations.stream()
                .filter(collab -> collab.getBrandRating() != null && collab.getBrandRating() >= 4)
                .count();
        
        double successRate = (double) successfulCount / pastCollaborations.size();
        return BigDecimal.valueOf(successRate * 10); // Scale to 10 points max
    }
}