package com.fitlife.repository;

import com.fitlife.entity.Post;
import com.fitlife.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<Post> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    Page<Post> findByTypeOrderByCreatedAtDesc(Post.PostType type, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.user IN :users ORDER BY p.createdAt DESC")
    Page<Post> findByUsersOrderByCreatedAtDesc(@Param("users") List<User> users, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.user = :user AND p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<Post> findByUserSince(@Param("user") User user, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(p) FROM Post p WHERE p.user = :user")
    Long countByUser(@Param("user") User user);
    
    @Query("SELECT p FROM Post p WHERE " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.achievement) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> searchPosts(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.achievement IS NOT NULL ORDER BY p.createdAt DESC")
    Page<Post> findAchievementPosts(Pageable pageable);
}