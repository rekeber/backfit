package com.fitlife.service;

import com.fitlife.entity.*;
import com.fitlife.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SocialService {
    
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final UserFollowRepository userFollowRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    
    // Posts management
    public Page<Post> getFeedPosts(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // Get posts from user and followed users
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    public Page<Post> getUserPosts(User user, Pageable pageable) {
        return postRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    public Page<Post> getMyPosts(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return postRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }
    
    public Post createPost(String email, Post post) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        post.setUser(user);
        Post savedPost = postRepository.save(post);
        
        // Give points to user for creating post
        user.setTotalPoints(user.getTotalPoints() + 5);
        userRepository.save(user);
        
        log.info("Created post for user {}: {}", user.getEmail(), post.getContent().substring(0, Math.min(50, post.getContent().length())));
        return savedPost;
    }
    
    public Post updatePost(String email, Long id, Post postDetails) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        
        if (!post.getUser().equals(user)) {
            throw new RuntimeException("User not authorized to update this post");
        }
        
        post.setContent(postDetails.getContent());
        post.setImageUrl(postDetails.getImageUrl());
        post.setType(postDetails.getType());
        
        log.info("Updated post for user {}: {}", user.getEmail(), id);
        return postRepository.save(post);
    }
    
    public void deletePost(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        
        if (!post.getUser().equals(user)) {
            throw new RuntimeException("User not authorized to delete this post");
        }
        
        postRepository.delete(post);
        log.info("Deleted post for user {}: {}", user.getEmail(), id);
    }
    
    // Likes management
    public PostLike likePost(String email, Long postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        
        // Check if already liked
        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);
        if (existingLike.isPresent()) {
            return existingLike.get();
        }
        
        PostLike like = PostLike.builder()
                .post(post)
                .user(user)
                .build();
        
        PostLike savedLike = postLikeRepository.save(like);
        
        // Update post likes count
        post.setLikes(post.getLikes() + 1);
        postRepository.save(post);
        
        // Give points to post owner
        User postOwner = post.getUser();
        postOwner.setTotalPoints(postOwner.getTotalPoints() + 2);
        userRepository.save(postOwner);
        
        log.info("User {} liked post {}", user.getEmail(), postId);
        return savedLike;
    }
    
    public void unlikePost(String email, Long postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        
        Optional<PostLike> like = postLikeRepository.findByPostAndUser(post, user);
        if (like.isPresent()) {
            postLikeRepository.delete(like.get());
            
            // Update post likes count
            post.setLikes(Math.max(0, post.getLikes() - 1));
            postRepository.save(post);
            
            log.info("User {} unliked post {}", user.getEmail(), postId);
        }
    }
    
    public List<PostLike> getPostLikes(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        return postLikeRepository.findByPostOrderByCreatedAtDesc(post);
    }
    
    // Comments management
    public List<Comment> getPostComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        return commentRepository.findByPostOrderByCreatedAtAsc(post);
    }
    
    public Comment createComment(String email, Long postId, Comment comment) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        
        comment.setUser(user);
        comment.setPost(post);
        Comment savedComment = commentRepository.save(comment);
        
        // Update post comments count
        post.setComments(post.getComments() + 1);
        postRepository.save(post);
        
        // Give points to commenter
        user.setTotalPoints(user.getTotalPoints() + 3);
        userRepository.save(user);
        
        // Give points to post owner
        User postOwner = post.getUser();
        postOwner.setTotalPoints(postOwner.getTotalPoints() + 1);
        userRepository.save(postOwner);
        
        log.info("User {} commented on post {}", user.getEmail(), postId);
        return savedComment;
    }
    
    public Comment updateComment(String email, Long id, Comment commentDetails) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        
        if (!comment.getUser().equals(user)) {
            throw new RuntimeException("User not authorized to update this comment");
        }
        
        comment.setContent(commentDetails.getContent());
        
        log.info("Updated comment for user {}: {}", user.getEmail(), id);
        return commentRepository.save(comment);
    }
    
    public void deleteComment(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        
        if (!comment.getUser().equals(user)) {
            throw new RuntimeException("User not authorized to delete this comment");
        }
        
        Post post = comment.getPost();
        commentRepository.delete(comment);
        
        // Update post comments count
        post.setComments(Math.max(0, post.getComments() - 1));
        postRepository.save(post);
        
        log.info("Deleted comment for user {}: {}", user.getEmail(), id);
    }
    
    // Follow management
    public UserFollow followUser(String email, Long userId) {
        User follower = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        User following = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (follower.equals(following)) {
            throw new RuntimeException("User cannot follow themselves");
        }
        
        // Check if already following
        Optional<UserFollow> existingFollow = userFollowRepository.findByFollowerAndFollowing(follower, following);
        if (existingFollow.isPresent()) {
            return existingFollow.get();
        }
        
        UserFollow follow = UserFollow.builder()
                .follower(follower)
                .following(following)
                .build();
        
        UserFollow savedFollow = userFollowRepository.save(follow);
        
        // Give points to followed user
        following.setTotalPoints(following.getTotalPoints() + 5);
        userRepository.save(following);
        
        log.info("User {} followed user {}", follower.getEmail(), following.getEmail());
        return savedFollow;
    }
    
    public void unfollowUser(String email, Long userId) {
        User follower = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        User following = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Optional<UserFollow> follow = userFollowRepository.findByFollowerAndFollowing(follower, following);
        if (follow.isPresent()) {
            userFollowRepository.delete(follow.get());
            log.info("User {} unfollowed user {}", follower.getEmail(), following.getEmail());
        }
    }
    
    public List<UserFollow> getUserFollowers(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return userFollowRepository.findByFollowingOrderByCreatedAtDesc(user);
    }
    
    public List<UserFollow> getUserFollowing(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return userFollowRepository.findByFollowerOrderByCreatedAtDesc(user);
    }
    
    public List<UserFollow> getUserFollowersById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return userFollowRepository.findByFollowingOrderByCreatedAtDesc(user);
    }
    
    public List<UserFollow> getUserFollowingById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return userFollowRepository.findByFollowerOrderByCreatedAtDesc(user);
    }
    
    // Discovery
    public List<Map<String, Object>> discoverUsers(String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        List<User> suggestedUsers = userRepository.findTop10ByIsActiveTrueAndIdNotOrderByTotalPointsDesc(currentUser.getId());
        
        return suggestedUsers.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("name", user.getName());
            userMap.put("email", user.getEmail());
            userMap.put("totalPoints", user.getTotalPoints());
            userMap.put("profileImageUrl", user.getProfileImageUrl());
            return userMap;
        }).toList();
    }
    
    public Page<Post> discoverPosts(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    // Statistics
    public Map<String, Object> getUserSocialStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        Long postsCount = postRepository.countByUser(user);
        Long followersCount = userFollowRepository.countByFollowing(user);
        Long followingCount = userFollowRepository.countByFollower(user);
        Long totalLikes = postLikeRepository.countByPostUser(user);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("postsCount", postsCount);
        stats.put("followersCount", followersCount);
        stats.put("followingCount", followingCount);
        stats.put("totalLikes", totalLikes);
        
        return stats;
    }
    
    // Achievements management
    public List<Achievement> getAllAchievements() {
        return achievementRepository.findByIsActiveTrueOrderByCategoryAscNameAsc();
    }
    
    public List<Achievement> getAchievementsByCategory(Achievement.Category category) {
        return achievementRepository.findByCategoryAndIsActiveTrueOrderByNameAsc(category);
    }
    
    public List<UserAchievement> getUserAchievements(User user) {
        return userAchievementRepository.findByUserOrderByUnlockedAtDesc(user);
    }
    
    public List<UserAchievement> getRecentAchievements(User user, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return userAchievementRepository.findByUserAndUnlockedAtAfterOrderByUnlockedAtDesc(user, since);
    }
}