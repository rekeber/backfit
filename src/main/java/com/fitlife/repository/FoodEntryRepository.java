package com.fitlife.repository;

import com.fitlife.entity.FoodEntry;
import com.fitlife.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FoodEntryRepository extends JpaRepository<FoodEntry, Long> {
    
    List<FoodEntry> findByUserAndDateOrderByCreatedAtDesc(User user, LocalDate date);
    
    List<FoodEntry> findByUserAndDateAndMealTypeOrderByCreatedAtDesc(User user, LocalDate date, FoodEntry.MealType mealType);
    
    Page<FoodEntry> findByUserOrderByDateDescCreatedAtDesc(User user, Pageable pageable);
    
    @Query("SELECT fe FROM FoodEntry fe WHERE fe.user = :user AND fe.date BETWEEN :startDate AND :endDate ORDER BY fe.date DESC, fe.createdAt DESC")
    List<FoodEntry> findByUserAndDateRange(@Param("user") User user, 
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(fe.totalCalories) FROM FoodEntry fe WHERE fe.user = :user AND fe.date = :date")
    Double sumCaloriesByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(fe.totalProtein) FROM FoodEntry fe WHERE fe.user = :user AND fe.date = :date")
    Double sumProteinByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(fe.totalCarbs) FROM FoodEntry fe WHERE fe.user = :user AND fe.date = :date")
    Double sumCarbsByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(fe.totalFat) FROM FoodEntry fe WHERE fe.user = :user AND fe.date = :date")
    Double sumFatByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(fe.totalFiber) FROM FoodEntry fe WHERE fe.user = :user AND fe.date = :date")
    Double sumFiberByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(fe.totalCalories) FROM FoodEntry fe WHERE fe.user = :user AND fe.date = :date AND fe.mealType = :mealType")
    Double sumCaloriesByUserDateAndMealType(@Param("user") User user, 
                                           @Param("date") LocalDate date, 
                                           @Param("mealType") FoodEntry.MealType mealType);
    
    @Query("SELECT COUNT(DISTINCT fe.date) FROM FoodEntry fe WHERE fe.user = :user AND fe.date >= :startDate")
    Long countDistinctDaysByUserSince(@Param("user") User user, @Param("startDate") LocalDate startDate);
    
    // Dashboard methods
    @Query("SELECT fe FROM FoodEntry fe WHERE fe.user.id = :userId AND fe.date = :date ORDER BY fe.createdAt DESC")
    List<FoodEntry> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}