package com.fitlife.service;

import com.fitlife.entity.*;
import com.fitlife.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NutritionService {
    
    private final FoodRepository foodRepository;
    private final FoodEntryRepository foodEntryRepository;
    private final NutritionGoalRepository nutritionGoalRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    
    // Food management
    public List<Food> getAllFoods() {
        return foodRepository.findByIsActiveTrueOrderByNameAsc();
    }
    
    public Page<Food> getAllFoods(Pageable pageable) {
        return foodRepository.findAllActive(pageable);
    }
    
    public Optional<Food> getFoodById(Long id) {
        return foodRepository.findById(id);
    }
    
    public Page<Food> searchFoods(String searchTerm, Pageable pageable) {
        return foodRepository.searchByName(searchTerm, pageable);
    }
    
    public List<Food> getFoodsByCategory(String category) {
        return foodRepository.findByCategoryAndIsActiveTrueOrderByNameAsc(category);
    }
    
    public Page<Food> getFoodsByCategory(String category, Pageable pageable) {
        return foodRepository.findByCategory(category, pageable);
    }
    
    public List<Food> getHealthyFoods() {
        return foodRepository.findByIsHealthyTrueAndIsActiveTrueOrderByNameAsc();
    }
    
    public Page<Food> getHealthyFoods(Pageable pageable) {
        return foodRepository.findHealthyFoods(pageable);
    }
    
    public Food createFood(Food food) {
        log.info("Creating new food: {}", food.getName());
        return foodRepository.save(food);
    }
    
    public List<String> getAllFoodCategories() {
        return foodRepository.findAllCategories();
    }
    
    public List<Food> getFoodsByBarcode(String barcode) {
        return foodRepository.findByBarcode(barcode);
    }
    
    public Page<Food> getHighProteinFoods(Double minProtein, Pageable pageable) {
        return foodRepository.findHighProteinFoods(minProtein, pageable);
    }
    
    // Food entries management
    public List<FoodEntry> getUserFoodEntries(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return foodEntryRepository.findByUserOrderByDateDescCreatedAtDesc(user, Pageable.unpaged()).getContent();
    }
    
    public List<FoodEntry> getUserFoodEntriesByDate(String email, LocalDate date) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return foodEntryRepository.findByUserAndDateOrderByCreatedAtDesc(user, date);
    }
    
    public List<FoodEntry> getUserFoodEntriesByMealType(String email, LocalDate date, String mealType) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        FoodEntry.MealType meal = FoodEntry.MealType.valueOf(mealType.toUpperCase());
        return foodEntryRepository.findByUserAndDateAndMealTypeOrderByCreatedAtDesc(user, date, meal);
    }
    
    public FoodEntry createFoodEntry(String email, FoodEntry foodEntry) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        foodEntry.setUser(user);
        if (foodEntry.getDate() == null) {
            foodEntry.setDate(LocalDate.now());
        }
        
        // Calculate totals
        foodEntry.calculateTotals();
        
        // Give points to user
        user.setTotalPoints(user.getTotalPoints() + 5);
        userRepository.save(user);
        
        log.info("Created food entry for user {}: {}", user.getEmail(), foodEntry.getFood().getName());
        return foodEntryRepository.save(foodEntry);
    }
    
    public FoodEntry updateFoodEntry(String email, Long id, FoodEntry foodEntryDetails) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        FoodEntry foodEntry = foodEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food entry not found with id: " + id));
        
        if (!foodEntry.getUser().equals(user)) {
            throw new RuntimeException("User not authorized to update this food entry");
        }
        
        foodEntry.setQuantity(foodEntryDetails.getQuantity());
        foodEntry.setMealType(foodEntryDetails.getMealType());
        foodEntry.setNotes(foodEntryDetails.getNotes());
        foodEntry.calculateTotals();
        
        log.info("Updated food entry for user {}: {}", user.getEmail(), foodEntry.getFood().getName());
        return foodEntryRepository.save(foodEntry);
    }
    
    public void deleteFoodEntry(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        FoodEntry foodEntry = foodEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food entry not found with id: " + id));
        
        if (!foodEntry.getUser().equals(user)) {
            throw new RuntimeException("User not authorized to delete this food entry");
        }
        
        foodEntryRepository.delete(foodEntry);
        log.info("Deleted food entry for user {}: {}", user.getEmail(), id);
    }
    
    public List<FoodEntry> getDailyFoodEntries(User user, LocalDate date) {
        return foodEntryRepository.findByUserAndDateOrderByCreatedAtDesc(user, date);
    }
    
    public List<FoodEntry> getMealEntries(User user, LocalDate date, FoodEntry.MealType mealType) {
        return foodEntryRepository.findByUserAndDateAndMealTypeOrderByCreatedAtDesc(user, date, mealType);
    }
    
    public List<FoodEntry> getFoodEntriesByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return foodEntryRepository.findByUserAndDateRange(user, startDate, endDate);
    }
    
    public void deleteFoodEntry(Long entryId, User user) {
        FoodEntry entry = foodEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Food entry not found with id: " + entryId));
        
        if (!entry.getUser().equals(user)) {
            throw new RuntimeException("User not authorized to delete this food entry");
        }
        
        foodEntryRepository.delete(entry);
        log.info("Deleted food entry with id: {}", entryId);
    }
    
    // Nutrition goals management
    public Optional<NutritionGoal> getUserNutritionGoals(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return nutritionGoalRepository.findByUser(user);
    }
    
    public NutritionGoal createOrUpdateNutritionGoals(String email, NutritionGoal nutritionGoal) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        NutritionGoal existingGoals = nutritionGoalRepository.findByUser(user)
                .orElse(NutritionGoal.builder().user(user).build());
        
        existingGoals.setDailyCalories(nutritionGoal.getDailyCalories());
        existingGoals.setProtein(nutritionGoal.getProtein());
        existingGoals.setCarbs(nutritionGoal.getCarbs());
        existingGoals.setFat(nutritionGoal.getFat());
        existingGoals.setFiber(nutritionGoal.getFiber());
        existingGoals.setWater(nutritionGoal.getWater());
        existingGoals.setActivityLevel(nutritionGoal.getActivityLevel());
        existingGoals.setGoal(nutritionGoal.getGoal());
        
        log.info("Updated nutrition goals for user {}", user.getEmail());
        return nutritionGoalRepository.save(existingGoals);
    }
    
    // Analytics methods
    public Map<String, Object> getDailyNutritionSummary(String email, LocalDate date) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        Double totalCalories = foodEntryRepository.sumCaloriesByUserAndDate(user, date);
        Double totalProtein = foodEntryRepository.sumProteinByUserAndDate(user, date);
        Double totalCarbs = foodEntryRepository.sumCarbsByUserAndDate(user, date);
        Double totalFat = foodEntryRepository.sumFatByUserAndDate(user, date);
        Double totalFiber = foodEntryRepository.sumFiberByUserAndDate(user, date);
        
        NutritionGoal goals = nutritionGoalRepository.findByUser(user).orElse(null);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("date", date);
        summary.put("totalCalories", totalCalories != null ? totalCalories.intValue() : 0);
        summary.put("totalProtein", totalProtein != null ? totalProtein : 0.0);
        summary.put("totalCarbs", totalCarbs != null ? totalCarbs : 0.0);
        summary.put("totalFat", totalFat != null ? totalFat : 0.0);
        summary.put("totalFiber", totalFiber != null ? totalFiber : 0.0);
        summary.put("goals", goals);
        
        return summary;
    }
    
    public Map<String, Object> getWeeklyNutritionSummary(String email, LocalDate startDate) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        LocalDate endDate = startDate.plusDays(6);
        List<FoodEntry> weekEntries = foodEntryRepository.findByUserAndDateRange(user, startDate, endDate);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);
        summary.put("totalEntries", weekEntries.size());
        summary.put("averageCalories", weekEntries.stream().mapToDouble(FoodEntry::getTotalCalories).average().orElse(0.0));
        summary.put("averageProtein", weekEntries.stream().mapToDouble(FoodEntry::getTotalProtein).average().orElse(0.0));
        
        return summary;
    }
    
    public Map<String, Object> getNutritionProgress(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        
        Long consistentDays = foodEntryRepository.countDistinctDaysByUserSince(user, weekAgo);
        List<FoodEntry> recentEntries = foodEntryRepository.findByUserAndDateRange(user, weekAgo, today);
        
        Map<String, Object> progress = new HashMap<>();
        progress.put("consistentDays", consistentDays);
        progress.put("totalEntries", recentEntries.size());
        progress.put("averageDailyCalories", recentEntries.stream().mapToDouble(FoodEntry::getTotalCalories).average().orElse(0.0));
        
        return progress;
    }
    
    private NutritionGoal createDefaultNutritionGoals(User user) {
        NutritionGoal defaultGoals = NutritionGoal.builder()
                .user(user)
                .dailyCalories(2000)
                .protein(150.0)
                .carbs(250.0)
                .fat(67.0)
                .fiber(25.0)
                .water(2.0)
                .activityLevel(NutritionGoal.ActivityLevel.MODERATE)
                .goal(NutritionGoal.Goal.MAINTAIN)
                .build();
        
        return nutritionGoalRepository.save(defaultGoals);
    }
}