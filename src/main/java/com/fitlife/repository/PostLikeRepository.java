package com.fitlife.repository;

import com.fitlife.entity.Post;
import com.fitlife.entity.PostLike;
import com.fitlife.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    
    Optional<PostLike> findByPostAndUser(Post post, User user);
    
    boolean existsByPostAndUser(Post post, User user);
    
    void deleteByPostAndUser(Post post, User user);
    
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post = :post")
    Long countByPost(@Param("post") Post post);
    
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.user = :user")
    Long countByUser(@Param("user") User user);
    
    // Métodos adicionales requeridos por SocialService
    List<PostLike> findByPostOrderByCreatedAtDesc(Post post);
    
    Long countByPostUser(User user);
}