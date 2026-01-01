package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PostType type = PostType.GENERAL;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer likes = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer comments = 0;
    
    private String achievement;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_session_id")
    private WorkoutSession workoutSession;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_entry_id")
    private FoodEntry foodEntry;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<PostLike> postLikes = new HashSet<>();
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Comment> postComments = new HashSet<>();
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum PostType {
        WORKOUT, NUTRITION, ACHIEVEMENT, GENERAL
    }
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void incrementLikes() {
        this.likes++;
    }
    
    public void decrementLikes() {
        if (this.likes > 0) {
            this.likes--;
        }
    }
    
    public void incrementComments() {
        this.comments++;
    }
    
    public void decrementComments() {
        if (this.comments > 0) {
            this.comments--;
        }
    }
}