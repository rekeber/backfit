package com.fitlife.repository;

import com.fitlife.entity.Comment;
import com.fitlife.entity.Post;
import com.fitlife.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);
    
    Page<Comment> findByPostOrderByCreatedAtAsc(Post post, Pageable pageable);
    
    List<Comment> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post = :post")
    Long countByPost(@Param("post") Post post);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user = :user")
    Long countByUser(@Param("user") User user);
}