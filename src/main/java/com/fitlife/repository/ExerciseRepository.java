package com.fitlife.repository;

import com.fitlife.entity.Exercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    
    List<Exercise> findByCategory(String category);
    
    List<Exercise> findByDifficulty(String difficulty);
    
    @Query("SELECT DISTINCT e.category FROM Exercise e ORDER BY e.category")
    List<String> findAllCategories();
    
    @Query("SELECT e FROM Exercise e WHERE " +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.category) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Exercise> searchExercises(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT e FROM Exercise e WHERE e.category = :category AND e.difficulty = :difficulty")
    List<Exercise> findByCategoryAndDifficulty(@Param("category") String category, 
                                              @Param("difficulty") String difficulty);
    
    @Query("SELECT e FROM Exercise e WHERE e.caloriesBurned BETWEEN :minCalories AND :maxCalories")
    List<Exercise> findByCalorieRange(@Param("minCalories") Integer minCalories, 
                                     @Param("maxCalories") Integer maxCalories);
    
    @Query("SELECT e FROM Exercise e WHERE e.duration BETWEEN :minDuration AND :maxDuration")
    List<Exercise> findByDurationRange(@Param("minDuration") Integer minDuration, 
                                      @Param("maxDuration") Integer maxDuration);
}