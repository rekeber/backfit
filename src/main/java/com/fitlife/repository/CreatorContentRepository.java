package com.fitlife.repository;

import com.fitlife.entity.Creator;
import com.fitlife.entity.CreatorContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CreatorContentRepository extends JpaRepository<CreatorContent, Long> {
    
    List<CreatorContent> findByCreatorAndIsPublishedTrueOrderByCreatedAtDesc(Creator creator);
    
    Page<CreatorContent> findByIsPublishedTrueOrderByViewCountDesc(Pageable pageable);
    
    Page<CreatorContent> findByIsPublishedTrueOrderByCreatedAtDesc(Pageable pageable);
    
    Page<CreatorContent> findByCategoryAndIsPublishedTrueOrderByViewCountDesc(
            CreatorContent.ContentCategory category, Pageable pageable);
    
    Page<CreatorContent> findByContentTypeAndIsPublishedTrueOrderByViewCountDesc(
            CreatorContent.ContentType contentType, Pageable pageable);
    
    @Query("SELECT c FROM CreatorContent c WHERE c.isPublished = true AND c.difficultyLevel = :level ORDER BY c.likeCount DESC")
    Page<CreatorContent> findByDifficultyLevel(@Param("level") CreatorContent.DifficultyLevel level, Pageable pageable);
    
    @Query("SELECT c FROM CreatorContent c WHERE c.isPublished = true AND c.tags LIKE %:tag% ORDER BY c.viewCount DESC")
    Page<CreatorContent> findByTag(@Param("tag") String tag, Pageable pageable);
    
    @Query("SELECT c FROM CreatorContent c WHERE c.isPublished = true AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY c.viewCount DESC")
    Page<CreatorContent> searchContent(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT c FROM CreatorContent c WHERE c.creator = :creator AND c.isPublished = true ORDER BY c.viewCount DESC")
    List<CreatorContent> findTopContentByCreator(@Param("creator") Creator creator, Pageable pageable);
    
    @Query("SELECT c FROM CreatorContent c WHERE c.isPublished = true AND c.createdAt >= :since ORDER BY c.viewCount DESC")
    List<CreatorContent> findTrendingContent(@Param("since") LocalDateTime since, Pageable pageable);
    
    @Query("SELECT c FROM CreatorContent c WHERE c.moderationStatus = :status ORDER BY c.createdAt ASC")
    List<CreatorContent> findByModerationStatus(@Param("status") CreatorContent.ModerationStatus status);
    
    @Query("SELECT COUNT(c) FROM CreatorContent c WHERE c.creator = :creator AND c.isPublished = true")
    Long countPublishedByCreator(@Param("creator") Creator creator);
    
    @Query("SELECT SUM(c.viewCount) FROM CreatorContent c WHERE c.creator = :creator AND c.isPublished = true")
    Long getTotalViewsByCreator(@Param("creator") Creator creator);
    
    @Query("SELECT c FROM CreatorContent c WHERE c.isMonetized = true AND c.isPublished = true ORDER BY c.earnings DESC")
    List<CreatorContent> findTopEarningContent(Pageable pageable);
    
    @Query("SELECT c FROM CreatorContent c WHERE c.isPremium = true AND c.isPublished = true ORDER BY c.createdAt DESC")
    Page<CreatorContent> findPremiumContent(Pageable pageable);
    
    @Query("SELECT c FROM CreatorContent c WHERE c.isPublished = true AND " +
           "c.viewCount > 0 AND (c.likeCount + c.commentCount + c.shareCount + c.saveCount) / c.viewCount >= :minEngagementRate " +
           "ORDER BY (c.likeCount + c.commentCount + c.shareCount + c.saveCount) / c.viewCount DESC")
    List<CreatorContent> findHighEngagementContent(@Param("minEngagementRate") Double minEngagementRate, Pageable pageable);
}