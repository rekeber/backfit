package com.fitlife.repository;

import com.fitlife.entity.NutritionGoal;
import com.fitlife.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NutritionGoalRepository extends JpaRepository<NutritionGoal, Long> {
    
    Optional<NutritionGoal> findByUser(User user);
    
    boolean existsByUser(User user);
    
    void deleteByUser(User user);
}