package com.fitlife.controller;

import com.fitlife.entity.Brand;
import com.fitlife.entity.BrandCollaboration;
import com.fitlife.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BrandController {
    
    private final BrandService brandService;
    
    // Brand Registration
    @PostMapping("/register")
    public ResponseEntity<?> registerBrand(@RequestBody BrandRegistrationRequest request) {
        try {
            Brand brand = Brand.builder()
                    .name(request.getName())
                    .brandHandle(request.getBrandHandle())
                    .description(request.getDescription())
                    .logoUrl(request.getLogoUrl())
                    .websiteUrl(request.getWebsiteUrl())
                    .industry(request.getIndustry())
                    .companySize(request.getCompanySize())
                    .contactEmail(request.getContactEmail())
                    .contactPhone(request.getContactPhone())
                    .contactPerson(request.getContactPerson())
                    .monthlyBudget(request.getMonthlyBudget())
                    .targetDemographics(request.getTargetDemographics())
                    .brandValues(request.getBrandValues())
                    .preferredContentTypes(request.getPreferredContentTypes())
                    .build();
            
            Brand registeredBrand = brandService.registerBrand(brand);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Brand registered successfully",
                    "brand", registeredBrand
            ));
        } catch (Exception e) {
            log.error("Error registering brand: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // Collaboration Management
    @PostMapping("/collaborations")
    public ResponseEntity<?> createCollaboration(
            @RequestBody CollaborationRequest request,
            Authentication authentication) {
        try {
            String brandEmail = authentication.getName();
            
            BrandCollaboration collaboration = brandService.createCollaboration(
                    brandEmail,
                    request.getCreatorId(),
                    request.getCollaborationType(),
                    request.getTitle(),
                    request.getDescription(),
                    request.getRequirements(),
                    request.getDeliverables(),
                    request.getBudget(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getDeadline(),
                    request.getTargetMetrics()
            );
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Collaboration created successfully",
                    "collaboration", collaboration
            ));
        } catch (Exception e) {
            log.error("Error creating collaboration: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/collaborations")
    public ResponseEntity<?> getBrandCollaborations(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        try {
            String brandEmail = authentication.getName();
            Pageable pageable = PageRequest.of(page, size);
            
            BrandCollaboration.CollaborationStatus collaborationStatus = 
                    status != null ? BrandCollaboration.CollaborationStatus.valueOf(status) : null;
            
            Page<BrandCollaboration> collaborations = brandService.getBrandCollaborations(
                    brandEmail, collaborationStatus, pageable);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "collaborations", collaborations.getContent(),
                    "totalElements", collaborations.getTotalElements(),
                    "totalPages", collaborations.getTotalPages(),
                    "currentPage", collaborations.getNumber()
            ));
        } catch (Exception e) {
            log.error("Error getting brand collaborations: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/collaborations/{collaborationId}/approve")
    public ResponseEntity<?> approveCollaboration(
            @PathVariable Long collaborationId,
            Authentication authentication) {
        try {
            String brandEmail = authentication.getName();
            
            BrandCollaboration collaboration = brandService.approveCollaboration(collaborationId, brandEmail);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Collaboration approved successfully",
                    "collaboration", collaboration
            ));
        } catch (Exception e) {
            log.error("Error approving collaboration: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/collaborations/{collaborationId}/complete")
    public ResponseEntity<?> completeCollaboration(
            @PathVariable Long collaborationId,
            @RequestBody CompleteCollaborationRequest request,
            Authentication authentication) {
        try {
            String brandEmail = authentication.getName();
            
            BrandCollaboration collaboration = brandService.completeCollaboration(
                    collaborationId, 
                    brandEmail, 
                    request.getActualMetrics(),
                    request.getBrandRating(),
                    request.getBrandFeedback()
            );
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Collaboration completed successfully",
                    "collaboration", collaboration
            ));
        } catch (Exception e) {
            log.error("Error completing collaboration: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // Creator Discovery for Brands
    @GetMapping("/creators/discover")
    public ResponseEntity<?> discoverCreators(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String minTier,
            @RequestParam(required = false) Long minFollowers,
            @RequestParam(required = false) BigDecimal minEngagementRate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Object> creators = brandService.discoverCreatorsForBrand(
                    specialty, minTier, minFollowers, minEngagementRate, pageable);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "creators", creators.getContent(),
                    "totalElements", creators.getTotalElements(),
                    "totalPages", creators.getTotalPages(),
                    "currentPage", creators.getNumber()
            ));
        } catch (Exception e) {
            log.error("Error discovering creators: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // AI Matching
    @PostMapping("/creators/{creatorId}/match")
    public ResponseEntity<?> getCreatorMatch(
            @PathVariable Long creatorId,
            @RequestBody MatchRequest request,
            Authentication authentication) {
        try {
            String brandEmail = authentication.getName();
            
            BigDecimal matchScore = brandService.calculateCreatorMatch(
                    brandEmail, 
                    creatorId, 
                    request.getCampaignType(),
                    request.getTargetAudience(),
                    request.getBudget()
            );
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "matchScore", matchScore,
                    "recommendation", getMatchRecommendation(matchScore)
            ));
        } catch (Exception e) {
            log.error("Error calculating creator match: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // Analytics
    @GetMapping("/analytics")
    public ResponseEntity<?> getBrandAnalytics(Authentication authentication) {
        try {
            String brandEmail = authentication.getName();
            
            Object analytics = brandService.getBrandAnalytics(brandEmail);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "analytics", analytics
            ));
        } catch (Exception e) {
            log.error("Error getting brand analytics: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // Helper methods
    private String getMatchRecommendation(BigDecimal matchScore) {
        if (matchScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return "Excellent match! Highly recommended for collaboration.";
        } else if (matchScore.compareTo(BigDecimal.valueOf(75)) >= 0) {
            return "Good match! This creator aligns well with your brand.";
        } else if (matchScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "Moderate match. Consider for specific campaigns.";
        } else {
            return "Low match. May not be the best fit for your brand.";
        }
    }
    
    // DTOs
    @lombok.Data
    public static class BrandRegistrationRequest {
        private String name;
        private String brandHandle;
        private String description;
        private String logoUrl;
        private String websiteUrl;
        private Brand.Industry industry;
        private Brand.CompanySize companySize;
        private String contactEmail;
        private String contactPhone;
        private String contactPerson;
        private BigDecimal monthlyBudget;
        private String targetDemographics;
        private String brandValues;
        private String preferredContentTypes;
    }
    
    @lombok.Data
    public static class CollaborationRequest {
        private Long creatorId;
        private BrandCollaboration.CollaborationType collaborationType;
        private String title;
        private String description;
        private String requirements;
        private String deliverables;
        private BigDecimal budget;
        private java.time.LocalDateTime startDate;
        private java.time.LocalDateTime endDate;
        private java.time.LocalDateTime deadline;
        private String targetMetrics;
    }
    
    @lombok.Data
    public static class CompleteCollaborationRequest {
        private String actualMetrics;
        private Integer brandRating;
        private String brandFeedback;
    }
    
    @lombok.Data
    public static class MatchRequest {
        private String campaignType;
        private String targetAudience;
        private BigDecimal budget;
    }
}