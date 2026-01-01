package com.fitlife.repository;

import com.fitlife.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    
    List<Achievement> findByIsActiveTrueOrderByCategoryAscNameAsc();
    
    Optional<Achievement> findByName(String name);
    
    List<Achievement> findByCategoryAndIsActiveTrueOrderByNameAsc(Achievement.Category category);
    
    List<Achievement> findByDifficultyAndIsActiveTrueOrderByNameAsc(Achievement.Difficulty difficulty);
    
    @Query("SELECT DISTINCT a.category FROM Achievement a WHERE a.isActive = true ORDER BY a.category")
    List<Achievement.Category> findAllCategories();
    
    List<Achievement> findByIsActiveTrueAndIdNotIn(List<Long> excludeIds);
    
    long countByIsActiveTrue();
}