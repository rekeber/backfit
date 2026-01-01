package com.fitlife.controller;

import com.fitlife.entity.Creator;
import com.fitlife.entity.CreatorContent;
import com.fitlife.service.CreatorEconomyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/creators")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CreatorController {
    
    private final CreatorEconomyService creatorEconomyService;
    
    // Creator Application
    @PostMapping("/apply")
    public ResponseEntity<?> applyToBeCreator(
            @RequestBody CreatorApplicationRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            Creator creator = creatorEconomyService.applyToBeCreator(
                    userEmail, 
                    request.getCreatorHandle(), 
                    request.getBio(), 
                    request.getSpecialties()
            );
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Creator application submitted successfully",
                    "creator", creator
            ));
        } catch (Exception e) {
            log.error("Error applying to be creator: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // Content Management
    @PostMapping("/content")
    public ResponseEntity<?> createContent(
            @RequestBody CreatorContentRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            CreatorContent content = CreatorContent.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .contentType(request.getContentType())
                    .category(request.getCategory())
                    .mediaUrl(request.getMediaUrl())
                    .thumbnailUrl(request.getThumbnailUrl())
                    .durationSeconds(request.getDurationSeconds())
                    .difficultyLevel(request.getDifficultyLevel())
                    .tags(String.join(",", request.getTags()))
                    .isPremium(request.getIsPremium())
                    .premiumPrice(request.getPremiumPrice())
                    .targetAudience(request.getTargetAudience())
                    .build();
            
            CreatorContent createdContent = creatorEconomyService.createContent(userEmail, content);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Content created successfully",
                    "content", createdContent
            ));
        } catch (Exception e) {
            log.error("Error creating content: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/content/{contentId}/publish")
    public ResponseEntity<?> publishContent(
            @PathVariable Long contentId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            CreatorContent content = creatorEconomyService.publishContent(contentId, userEmail);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Content published successfully",
                    "content", content
            ));
        } catch (Exception e) {
            log.error("Error publishing content: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // Content Interaction
    @PostMapping("/content/{contentId}/like")
    public ResponseEntity<?> likeContent(
            @PathVariable Long contentId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            creatorEconomyService.likeContent(contentId, userEmail);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Content liked successfully"
            ));
        } catch (Exception e) {
            log.error("Error liking content: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/content/{contentId}/view")
    public ResponseEntity<?> viewContent(
            @PathVariable Long contentId,
            @RequestBody ViewContentRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            creatorEconomyService.viewContent(contentId, userEmail, request.getViewDurationSeconds());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Content view recorded"
            ));
        } catch (Exception e) {
            log.error("Error recording content view: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // Creator Discovery
    @GetMapping("/discover")
    public ResponseEntity<?> discoverCreators(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String minTier,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Creator.CreatorTier tier = minTier != null ? Creator.CreatorTier.valueOf(minTier) : null;
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Creator> creators = creatorEconomyService.discoverCreators(specialty, tier, pageable);
            
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
    
    @GetMapping("/content/trending")
    public ResponseEntity<?> getTrendingContent(
            @RequestParam(defaultValue = "24") int hours) {
        try {
            List<CreatorContent> trendingContent = creatorEconomyService.getTrendingContent(hours);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "content", trendingContent
            ));
        } catch (Exception e) {
            log.error("Error getting trending content: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/content/personalized")
    public ResponseEntity<?> getPersonalizedContent(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            List<CreatorContent> personalizedContent = creatorEconomyService.getPersonalizedContent(userEmail);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "content", personalizedContent
            ));
        } catch (Exception e) {
            log.error("Error getting personalized content: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // Analytics
    @GetMapping("/analytics")
    public ResponseEntity<?> getCreatorAnalytics(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            CreatorEconomyService.CreatorAnalytics analytics = creatorEconomyService.getCreatorAnalytics(userEmail);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "analytics", analytics
            ));
        } catch (Exception e) {
            log.error("Error getting creator analytics: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // DTOs
    @lombok.Data
    public static class CreatorApplicationRequest {
        private String creatorHandle;
        private String bio;
        private List<String> specialties;
    }
    
    @lombok.Data
    public static class CreatorContentRequest {
        private String title;
        private String description;
        private CreatorContent.ContentType contentType;
        private CreatorContent.ContentCategory category;
        private String mediaUrl;
        private String thumbnailUrl;
        private Integer durationSeconds;
        private CreatorContent.DifficultyLevel difficultyLevel;
        private List<String> tags;
        private Boolean isPremium = false;
        private java.math.BigDecimal premiumPrice;
        private String targetAudience;
    }
    
    @lombok.Data
    public static class ViewContentRequest {
        private Integer viewDurationSeconds;
    }
}