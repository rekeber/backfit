package com.fitlife.repository;

import com.fitlife.entity.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    
    @Query("SELECT f FROM Food f WHERE f.isActive = true")
    Page<Food> findAllActive(Pageable pageable);
    
    @Query("SELECT f FROM Food f WHERE f.isActive = true AND " +
           "LOWER(f.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Food> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT f FROM Food f JOIN f.categories c WHERE f.isActive = true AND c = :category")
    Page<Food> findByCategory(@Param("category") String category, Pageable pageable);
    
    @Query("SELECT DISTINCT c FROM Food f JOIN f.categories c WHERE f.isActive = true ORDER BY c")
    List<String> findAllCategories();
    
    @Query("SELECT f FROM Food f WHERE f.isActive = true AND f.barcode = :barcode")
    List<Food> findByBarcode(@Param("barcode") String barcode);
    
    @Query("SELECT f FROM Food f WHERE f.isActive = true AND f.brand = :brand")
    Page<Food> findByBrand(@Param("brand") String brand, Pageable pageable);
    
    @Query("SELECT f FROM Food f WHERE f.isActive = true AND f.verificationStatus = :status")
    Page<Food> findByVerificationStatus(@Param("status") Food.VerificationStatus status, Pageable pageable);
    
    @Query("SELECT f FROM Food f WHERE f.isActive = true AND " +
           "f.caloriesPer100g BETWEEN :minCalories AND :maxCalories")
    Page<Food> findByCalorieRange(@Param("minCalories") Double minCalories, 
                                 @Param("maxCalories") Double maxCalories, 
                                 Pageable pageable);
    
    @Query("SELECT f FROM Food f WHERE f.isActive = true AND " +
           "f.proteinPer100g >= :minProtein")
    Page<Food> findHighProteinFoods(@Param("minProtein") Double minProtein, Pageable pageable);
    
    @Query("SELECT f FROM Food f WHERE f.isActive = true AND " +
           "f.isHealthy = true ORDER BY f.caloriesPer100g ASC")
    Page<Food> findHealthyFoods(Pageable pageable);
    
    // Métodos adicionales requeridos por NutritionService
    List<Food> findByIsActiveTrueOrderByNameAsc();
    
    @Query("SELECT f FROM Food f JOIN f.categories c WHERE f.isActive = true AND c = :category ORDER BY f.name ASC")
    List<Food> findByCategoryAndIsActiveTrueOrderByNameAsc(@Param("category") String category);
    
    List<Food> findByIsHealthyTrueAndIsActiveTrueOrderByNameAsc();
}