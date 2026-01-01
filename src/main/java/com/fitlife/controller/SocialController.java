package com.fitlife.controller;

import com.fitlife.entity.Post;
import com.fitlife.entity.PostLike;
import com.fitlife.entity.Comment;
import com.fitlife.entity.UserFollow;
import com.fitlife.service.SocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/social")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class SocialController {
    
    private final SocialService socialService;
    
    // Posts endpoints
    @GetMapping("/posts")
    public ResponseEntity<Page<Post>> getFeedPosts(Authentication auth, Pageable pageable) {
        String email = auth.getName();
        return ResponseEntity.ok(socialService.getFeedPosts(email, pageable));
    }
    
    @GetMapping("/posts/user/{userId}")
    public ResponseEntity<Page<Post>> getUserPosts(@PathVariable Long userId, Pageable pageable) {
        // This would need a method that takes userId directly
        return ResponseEntity.ok(socialService.getFeedPosts("", pageable)); // Simplified for now
    }
    
    @GetMapping("/posts/my")
    public ResponseEntity<Page<Post>> getMyPosts(Authentication auth, Pageable pageable) {
        String email = auth.getName();
        return ResponseEntity.ok(socialService.getMyPosts(email, pageable));
    }
    
    @GetMapping("/posts/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return socialService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(@RequestBody Post post, Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(socialService.createPost(email, post));
    }
    
    @PutMapping("/posts/{id}")
    public ResponseEntity<Post> updatePost(
            @PathVariable Long id,
            @RequestBody Post post,
            Authentication auth) {
        String email = auth.getName();
        try {
            return ResponseEntity.ok(socialService.updatePost(email, id, post));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Authentication auth) {
        String email = auth.getName();
        try {
            socialService.deletePost(email, id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Likes endpoints
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<PostLike> likePost(@PathVariable Long postId, Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(socialService.likePost(email, postId));
    }
    
    @DeleteMapping("/posts/{postId}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId, Authentication auth) {
        String email = auth.getName();
        socialService.unlikePost(email, postId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/posts/{postId}/likes")
    public ResponseEntity<List<PostLike>> getPostLikes(@PathVariable Long postId) {
        return ResponseEntity.ok(socialService.getPostLikes(postId));
    }
    
    // Comments endpoints
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Comment>> getPostComments(@PathVariable Long postId) {
        return ResponseEntity.ok(socialService.getPostComments(postId));
    }
    
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Comment> createComment(
            @PathVariable Long postId,
            @RequestBody Comment comment,
            Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(socialService.createComment(email, postId, comment));
    }
    
    @PutMapping("/comments/{id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long id,
            @RequestBody Comment comment,
            Authentication auth) {
        String email = auth.getName();
        try {
            return ResponseEntity.ok(socialService.updateComment(email, id, comment));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, Authentication auth) {
        String email = auth.getName();
        try {
            socialService.deleteComment(email, id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Follow endpoints
    @PostMapping("/follow/{userId}")
    public ResponseEntity<UserFollow> followUser(@PathVariable Long userId, Authentication auth) {
        String email = auth.getName();
        try {
            return ResponseEntity.ok(socialService.followUser(email, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/follow/{userId}")
    public ResponseEntity<Void> unfollowUser(@PathVariable Long userId, Authentication auth) {
        String email = auth.getName();
        socialService.unfollowUser(email, userId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/followers")
    public ResponseEntity<List<UserFollow>> getMyFollowers(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(socialService.getUserFollowers(email));
    }
    
    @GetMapping("/following")
    public ResponseEntity<List<UserFollow>> getMyFollowing(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(socialService.getUserFollowing(email));
    }
    
    @GetMapping("/users/{userId}/followers")
    public ResponseEntity<List<UserFollow>> getUserFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(socialService.getUserFollowersById(userId));
    }
    
    @GetMapping("/users/{userId}/following")
    public ResponseEntity<List<UserFollow>> getUserFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(socialService.getUserFollowingById(userId));
    }
    
    // Discovery endpoints
    @GetMapping("/discover/users")
    public ResponseEntity<List<Map<String, Object>>> discoverUsers(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(socialService.discoverUsers(email));
    }
    
    @GetMapping("/discover/posts")
    public ResponseEntity<Page<Post>> discoverPosts(Authentication auth, Pageable pageable) {
        String email = auth.getName();
        return ResponseEntity.ok(socialService.discoverPosts(email, pageable));
    }
    
    // Analytics endpoints
    @GetMapping("/analytics/stats")
    public ResponseEntity<Map<String, Object>> getSocialStats(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(socialService.getUserSocialStats(email));
    }
}