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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreatorEconomyService {
    
    private final CreatorRepository creatorRepository;
    private final CreatorContentRepository contentRepository;
    private final BrandCollaborationRepository collaborationRepository;
    private final UserRepository userRepository;
    private final FitCoinRepository fitCoinRepository;
    private final GamificationService gamificationService;
    
    // Creator Management
    public Creator applyToBeCreator(String userEmail, String creatorHandle, String bio, List<String> specialties) {
        User user = getUserByEmail(userEmail);
        
        // Check if user is already a creator
        Optional<Creator> existingCreator = creatorRepository.findByUser(user);
        if (existingCreator.isPresent()) {
            throw new RuntimeException("User is already a creator");
        }
        
        // Check if handle is available
        if (creatorRepository.findByCreatorHandle(creatorHandle).isPresent()) {
            throw new RuntimeException("Creator handle already taken");
        }
        
        Creator creator = Creator.builder()
                .user(user)
                .creatorHandle(creatorHandle)
                .bio(bio)
                .specialties(String.join(",", specialties))
                .applicationDate(LocalDateTime.now())
                .applicationStatus(Creator.ApplicationStatus.PENDING)
                .build();
        
        creator = creatorRepository.save(creator);
        
        // Award FitCoins for applying
        gamificationService.awardFitCoins(user, BigDecimal.valueOf(50), 
                FitCoin.CoinSource.CREATOR_EARNINGS, "Creator application bonus");
        
        log.info("User {} applied to be creator with handle {}", userEmail, creatorHandle);
        return creator;
    }
    
    public Creator approveCreatorApplication(Long creatorId, String adminEmail) {
        Creator creator = creatorRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));
        
        if (creator.getApplicationStatus() != Creator.ApplicationStatus.PENDING) {
            throw new RuntimeException("Creator application is not pending");
        }
        
        creator.setApplicationStatus(Creator.ApplicationStatus.APPROVED);
        creator.setApprovalDate(LocalDateTime.now());
        creator.setIsActive(true);
        
        creator = creatorRepository.save(creator);
        
        // Award welcome bonus
        gamificationService.awardFitCoins(creator.getUser(), BigDecimal.valueOf(200), 
                FitCoin.CoinSource.CREATOR_EARNINGS, "Creator approval bonus");
        
        log.info("Creator {} approved by admin {}", creator.getCreatorHandle(), adminEmail);
        return creator;
    }
    
    // Content Management
    public CreatorContent createContent(String userEmail, CreatorContent content) {
        User user = getUserByEmail(userEmail);
        Creator creator = creatorRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User is not a creator"));
        
        if (!creator.getIsActive() || creator.getApplicationStatus() != Creator.ApplicationStatus.APPROVED) {
            throw new RuntimeException("Creator account is not active");
        }
        
        content.setCreator(creator);
        content.setModerationStatus(CreatorContent.ModerationStatus.PENDING);
        
        content = contentRepository.save(content);
        
        // Update creator stats
        creator.setContentCount(creator.getContentCount() + 1);
        creatorRepository.save(creator);
        
        // Award FitCoins for content creation
        gamificationService.awardFitCoins(user, BigDecimal.valueOf(25), 
                FitCoin.CoinSource.CONTENT_CREATED, "Content creation reward");
        
        log.info("Creator {} created new content: {}", creator.getCreatorHandle(), content.getTitle());
        return content;
    }
    
    public CreatorContent publishContent(Long contentId, String userEmail) {
        User user = getUserByEmail(userEmail);
        CreatorContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));
        
        if (!content.getCreator().getUser().equals(user)) {
            throw new RuntimeException("User not authorized to publish this content");
        }
        
        if (content.getModerationStatus() != CreatorContent.ModerationStatus.APPROVED) {
            throw new RuntimeException("Content must be approved before publishing");
        }
        
        content.setIsPublished(true);
        content.setPublishedAt(LocalDateTime.now());
        
        // Check if eligible for monetization
        if (content.isEligibleForMonetization()) {
            content.setIsMonetized(true);
        }
        
        content = contentRepository.save(content);
        
        // Award publishing bonus
        gamificationService.awardFitCoins(user, BigDecimal.valueOf(50), 
                FitCoin.CoinSource.CONTENT_CREATED, "Content publishing bonus");
        
        log.info("Content published: {} by {}", content.getTitle(), content.getCreator().getCreatorHandle());
        return content;
    }
    
    // Content Interaction
    public void likeContent(Long contentId, String userEmail) {
        User user = getUserByEmail(userEmail);
        CreatorContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));
        
        // TODO: Check if user already liked (implement ContentLike entity)
        
        content.incrementLike();
        contentRepository.save(content);
        
        // Award FitCoins to content creator
        Creator creator = content.getCreator();
        gamificationService.awardFitCoins(creator.getUser(), BigDecimal.valueOf(2), 
                FitCoin.CoinSource.CONTENT_LIKED, "Content like reward");
        
        // Award small amount to liker for engagement
        gamificationService.awardFitCoins(user, BigDecimal.valueOf(1), 
                FitCoin.CoinSource.SOCIAL_INTERACTION, "Content interaction reward");
        
        // Update creator engagement rate
        updateCreatorEngagement(creator);
    }
    
    public void viewContent(Long contentId, String userEmail, Integer viewDurationSeconds) {
        User user = getUserByEmail(userEmail);
        CreatorContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));
        
        content.incrementView();
        contentRepository.save(content);
        
        // Award view rewards based on watch time
        if (viewDurationSeconds != null && content.getDurationSeconds() != null) {
            double completionRate = (double) viewDurationSeconds / content.getDurationSeconds();
            
            if (completionRate >= 0.8) { // 80% completion
                gamificationService.awardFitCoins(content.getCreator().getUser(), BigDecimal.valueOf(5), 
                        FitCoin.CoinSource.CONTENT_LIKED, "High engagement view reward");
            } else if (completionRate >= 0.5) { // 50% completion
                gamificationService.awardFitCoins(content.getCreator().getUser(), BigDecimal.valueOf(2), 
                        FitCoin.CoinSource.CONTENT_LIKED, "Medium engagement view reward");
            }
        }
        
        // Update creator stats
        Creator creator = content.getCreator();
        creator.setTotalViews(creator.getTotalViews() + 1);
        updateCreatorEngagement(creator);
    }
    
    // Creator Discovery
    public Page<Creator> discoverCreators(String specialty, Creator.CreatorTier minTier, Pageable pageable) {
        if (specialty != null) {
            return creatorRepository.findBySpecialty(specialty).stream()
                    .filter(c -> minTier == null || c.getTier().ordinal() >= minTier.ordinal())
                    .collect(java.util.stream.Collectors.toList())
                    .stream()
                    .skip(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .collect(java.util.stream.Collectors.toList())
                    .stream()
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())
                    ));
        }
        
        return creatorRepository.findByIsActiveTrueOrderByFollowerCountDesc(pageable);
    }
    
    public List<CreatorContent> getTrendingContent(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return contentRepository.findTrendingContent(since, 
                org.springframework.data.domain.PageRequest.of(0, 20));
    }
    
    public List<CreatorContent> getPersonalizedContent(String userEmail) {
        User user = getUserByEmail(userEmail);
        
        // TODO: Implement AI-based personalization
        // For now, return popular content
        return contentRepository.findTrendingContent(LocalDateTime.now().minusDays(7), 
                org.springframework.data.domain.PageRequest.of(0, 10));
    }
    
    // Analytics
    public CreatorAnalytics getCreatorAnalytics(String userEmail) {
        User user = getUserByEmail(userEmail);
        Creator creator = creatorRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User is not a creator"));
        
        Long totalViews = contentRepository.getTotalViewsByCreator(creator);
        Long totalContent = contentRepository.countPublishedByCreator(creator);
        
        return CreatorAnalytics.builder()
                .creatorId(creator.getId())
                .totalFollowers(creator.getFollowerCount())
                .totalViews(totalViews != null ? totalViews : 0L)
                .totalContent(totalContent != null ? totalContent : 0L)
                .totalEarnings(creator.getTotalEarnings())
                .monthlyEarnings(creator.getMonthlyEarnings())
                .engagementRate(creator.getEngagementRate())
                .averageRating(creator.getAverageRating())
                .tier(creator.getTier())
                .build();
    }
    
    // Helper methods
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
    
    private void updateCreatorEngagement(Creator creator) {
        // Calculate engagement rate based on recent content performance
        List<CreatorContent> recentContent = contentRepository.findByCreatorAndIsPublishedTrueOrderByCreatedAtDesc(creator)
                .stream()
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
        
        if (!recentContent.isEmpty()) {
            double totalEngagementRate = recentContent.stream()
                    .mapToDouble(content -> content.getEngagementRate().doubleValue())
                    .average()
                    .orElse(0.0);
            
            creator.setEngagementRate(BigDecimal.valueOf(totalEngagementRate));
            creatorRepository.save(creator);
        }
    }
    
    // DTO for analytics
    @lombok.Data
    @lombok.Builder
    public static class CreatorAnalytics {
        private Long creatorId;
        private Long totalFollowers;
        private Long totalViews;
        private Long totalContent;
        private BigDecimal totalEarnings;
        private BigDecimal monthlyEarnings;
        private BigDecimal engagementRate;
        private BigDecimal averageRating;
        private Creator.CreatorTier tier;
    }
}