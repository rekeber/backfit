package com.fitlife.service;

import com.fitlife.dto.DashboardStatsDTO;
import com.fitlife.dto.DailyNutritionSummaryDTO;
import com.fitlife.dto.RecentActivityDTO;
import com.fitlife.entity.*;
import com.fitlife.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class DashboardService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FitCoinRepository fitCoinRepository;
    
    @Autowired
    private UserAchievementRepository userAchievementRepository;
    
    @Autowired
    private AchievementRepository achievementRepository;
    
    @Autowired
    private WorkoutSessionRepository workoutSessionRepository;
    
    @Autowired
    private FoodEntryRepository foodEntryRepository;
    
    @Autowired
    private WaterIntakeRepository waterIntakeRepository;
    
    @Autowired
    private UserActivityRepository userActivityRepository;
    
    @Autowired
    private NutritionGoalRepository nutritionGoalRepository;

    public DashboardStatsDTO getDashboardStats(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("=== DASHBOARD STATS DEBUG ===");
        log.info("User ID: {}", userId);
        log.info("User email: {}", user.getEmail());
        log.info("User height: {}", user.getHeight());
        log.info("User current weight: {}", user.getCurrentWeight());
        log.info("User target weight: {}", user.getTargetWeight());
        log.info("==============================");

        // Get FitCoins balance
        BigDecimal fitCoinsBalance = fitCoinRepository.getCurrentBalance(userId);
        
        // Get achievements
        Long totalAchievements = achievementRepository.count();
        Long unlockedAchievements = userAchievementRepository.countByUserId(userId);
        
        // Calculate current streak (simplified - days with any activity)
        Integer currentStreak = calculateCurrentStreak(userId);
        
        // Get workout stats
        Integer totalWorkouts = workoutSessionRepository.countByUserId(userId).intValue();
        BigDecimal totalCaloriesBurned = workoutSessionRepository.getTotalCaloriesBurnedByUser(userId);
        
        // Calculate BMI if height and weight are available
        BigDecimal bmi = null;
        if (user.getHeight() != null && user.getCurrentWeight() != null) {
            // BMI = weight(kg) / (height(m))^2
            BigDecimal heightInMeters = BigDecimal.valueOf(user.getHeight()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            bmi = BigDecimal.valueOf(user.getCurrentWeight()).divide(
                heightInMeters.multiply(heightInMeters), 2, RoundingMode.HALF_UP);
            log.info("BMI calculated: {}", bmi);
        } else {
            log.info("Cannot calculate BMI - height: {}, weight: {}", user.getHeight(), user.getCurrentWeight());
        }
        
        // Calculate global ranking (simplified - based on FitCoins)
        Integer globalRanking = calculateGlobalRanking(userId, fitCoinsBalance);
        
        DashboardStatsDTO stats = new DashboardStatsDTO(
            user.getCurrentWeight() != null ? BigDecimal.valueOf(user.getCurrentWeight()) : BigDecimal.ZERO,
            bmi,
            currentStreak,
            fitCoinsBalance,
            totalAchievements.intValue(),
            unlockedAchievements.intValue(),
            globalRanking,
            totalWorkouts,
            totalCaloriesBurned != null ? totalCaloriesBurned : BigDecimal.ZERO,
            currentStreak // For now, longest streak = current streak
        );
        
        log.info("Dashboard stats created: currentWeight={}, bmi={}", stats.getCurrentWeight(), stats.getBmi());
        return stats;
    }

    public DailyNutritionSummaryDTO getDailyNutritionSummary(Long userId, LocalDate date) {
        DailyNutritionSummaryDTO summary = new DailyNutritionSummaryDTO();
        summary.setDate(date);
        
        // Get nutrition goal
        Optional<NutritionGoal> goalOpt = nutritionGoalRepository.findByUser(userRepository.findById(userId).orElse(null));
        if (goalOpt.isPresent()) {
            NutritionGoal goal = goalOpt.get();
            summary.setCalorieGoal(BigDecimal.valueOf(goal.getDailyCalories()));
            summary.setProteinGoal(BigDecimal.valueOf(goal.getProtein()));
            summary.setCarbGoal(BigDecimal.valueOf(goal.getCarbs()));
            summary.setFatGoal(BigDecimal.valueOf(goal.getFat()));
        }
        
        // Get food entries for the day
        List<FoodEntry> foodEntries = foodEntryRepository.findByUserIdAndDate(userId, date);
        
        // Calculate totals
        BigDecimal totalCalories = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        BigDecimal totalFiber = BigDecimal.ZERO;
        
        for (FoodEntry entry : foodEntries) {
            if (entry.getTotalCalories() != null) totalCalories = totalCalories.add(BigDecimal.valueOf(entry.getTotalCalories()));
            if (entry.getTotalCarbs() != null) totalCarbs = totalCarbs.add(BigDecimal.valueOf(entry.getTotalCarbs()));
            if (entry.getTotalProtein() != null) totalProtein = totalProtein.add(BigDecimal.valueOf(entry.getTotalProtein()));
            if (entry.getTotalFat() != null) totalFat = totalFat.add(BigDecimal.valueOf(entry.getTotalFat()));
            if (entry.getTotalFiber() != null) totalFiber = totalFiber.add(BigDecimal.valueOf(entry.getTotalFiber()));
        }
        
        summary.setTotalCalories(totalCalories);
        summary.setTotalCarbs(totalCarbs);
        summary.setTotalProtein(totalProtein);
        summary.setTotalFat(totalFat);
        summary.setTotalFiber(totalFiber);
        
        // Group by meal type
        List<DailyNutritionSummaryDTO.MealSummaryDTO> meals = foodEntries.stream()
            .collect(Collectors.groupingBy(entry -> entry.getMealType().name()))
            .entrySet().stream()
            .map(entry -> {
                String mealType = entry.getKey();
                List<FoodEntry> mealEntries = entry.getValue();
                BigDecimal mealCalories = mealEntries.stream()
                    .map(FoodEntry::getTotalCalories)
                    .filter(cal -> cal != null)
                    .map(BigDecimal::valueOf)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                return new DailyNutritionSummaryDTO.MealSummaryDTO(mealType, mealCalories, mealEntries.size());
            })
            .collect(Collectors.toList());
        
        summary.setMeals(meals);
        
        // Get water intake
        Integer waterGlasses = waterIntakeRepository.getTotalGlassesByUserAndDate(userId, date);
        summary.setWaterGlasses(waterGlasses);
        summary.setWaterGoal(8); // Default goal
        
        return summary;
    }

    public List<RecentActivityDTO> getRecentActivities(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusDays(7); // Last 7 days
        List<UserActivity> activities = userActivityRepository.findRecentActivitiesByUser(
            userId, since, PageRequest.of(0, 10));
        
        return activities.stream()
            .map(activity -> new RecentActivityDTO(
                activity.getActivityType(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getIconType(),
                activity.getCreatedAt()
            ))
            .collect(Collectors.toList());
    }

    public void logWaterIntake(Long userId, int glasses) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        LocalDate today = LocalDate.now();
        Optional<WaterIntake> existingIntake = waterIntakeRepository.findByUserIdAndDate(userId, today);
        
        WaterIntake waterIntake;
        if (existingIntake.isPresent()) {
            waterIntake = existingIntake.get();
            waterIntake.setGlasses(glasses);
        } else {
            waterIntake = new WaterIntake(user, today, glasses);
        }
        
        waterIntakeRepository.save(waterIntake);
        
        // Log activity
        logActivity(user, "WATER_LOGGED", "Agua registrada", 
                   glasses + " vasos de agua", "WATER");
    }

    public Integer getTodayWaterIntake(Long userId) {
        Integer glasses = waterIntakeRepository.getTotalGlassesByUserAndDate(userId, LocalDate.now());
        log.info("=== WATER INTAKE DEBUG ===");
        log.info("User ID: {}", userId);
        log.info("Date: {}", LocalDate.now());
        log.info("Glasses found: {}", glasses);
        log.info("==========================");
        return glasses;
    }

    public void logActivity(User user, String activityType, String title, String description, String iconType) {
        UserActivity activity = new UserActivity(user, activityType, title, description, iconType);
        userActivityRepository.save(activity);
    }

    private Integer calculateCurrentStreak(Long userId) {
        // Simplified streak calculation - count consecutive days with activities
        LocalDate today = LocalDate.now();
        int streak = 0;
        
        for (int i = 0; i < 365; i++) { // Max 365 days check
            LocalDate checkDate = today.minusDays(i);
            LocalDateTime startOfDay = checkDate.atStartOfDay();
            LocalDateTime endOfDay = checkDate.atTime(23, 59, 59);
            
            List<UserActivity> dayActivities = userActivityRepository.findRecentActivitiesByUser(
                userId, startOfDay, PageRequest.of(0, 1));
            
            if (!dayActivities.isEmpty()) {
                streak++;
            } else if (i > 0) { // Don't break on first day (today) if no activities yet
                break;
            }
        }
        
        return streak;
    }

    private Integer calculateGlobalRanking(Long userId, BigDecimal fitCoinsBalance) {
        // Simplified ranking based on FitCoins
        Long usersWithMoreCoins = fitCoinRepository.countUsersWithMoreCoins(fitCoinsBalance);
        return usersWithMoreCoins.intValue() + 1;
    }
}