package com.fitlife.repository;

import com.fitlife.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByIsActiveTrue();
    
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name% AND u.isActive = true")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT u FROM User u WHERE u.id IN :ids AND u.isActive = true")
    List<User> findByIdInAndIsActiveTrue(@Param("ids") List<Long> ids);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();
}