package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ContentComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private CreatorContent content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String commentText;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private ContentComment parentComment; // For nested comments/replies
    
    @Column(name = "like_count")
    @Builder.Default
    private Long likeCount = 0L;
    
    @Column(name = "is_pinned")
    @Builder.Default
    private Boolean isPinned = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Business methods
    public void incrementLike() {
        this.likeCount++;
    }
    
    public void decrementLike() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
    
    public boolean isReply() {
        return parentComment != null;
    }
    
    public void pin() {
        this.isPinned = true;
    }
    
    public void unpin() {
        this.isPinned = false;
    }
}