package com.fitlife.repository;

import com.fitlife.entity.User;
import com.fitlife.entity.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {
    
    Optional<UserFollow> findByFollowerAndFollowing(User follower, User following);
    
    boolean existsByFollowerAndFollowing(User follower, User following);
    
    void deleteByFollowerAndFollowing(User follower, User following);
    
    @Query("SELECT uf.following FROM UserFollow uf WHERE uf.follower = :user")
    Page<User> findFollowingByUser(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT uf.follower FROM UserFollow uf WHERE uf.following = :user")
    Page<User> findFollowersByUser(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.follower = :user")
    Long countFollowingByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.following = :user")
    Long countFollowersByUser(@Param("user") User user);
    
    @Query("SELECT uf.following FROM UserFollow uf WHERE uf.follower = :user")
    List<User> findFollowingUsers(@Param("user") User user);
    
    @Query("SELECT u FROM User u WHERE u != :user AND u NOT IN " +
           "(SELECT uf.following FROM UserFollow uf WHERE uf.follower = :user) " +
           "ORDER BY u.totalPoints DESC")
    Page<User> findSuggestedUsers(@Param("user") User user, Pageable pageable);
    
    // Métodos adicionales requeridos por SocialService
    List<UserFollow> findByFollowingOrderByCreatedAtDesc(User following);
    
    List<UserFollow> findByFollowerOrderByCreatedAtDesc(User follower);
    
    Long countByFollowing(User following);
    
    Long countByFollower(User follower);
}