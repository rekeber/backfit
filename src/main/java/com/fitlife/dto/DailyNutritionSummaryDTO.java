package com.fitlife.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DailyNutritionSummaryDTO {
    private LocalDate date;
    private BigDecimal totalCalories;
    private BigDecimal calorieGoal;
    private BigDecimal totalCarbs;
    private BigDecimal totalProtein;
    private BigDecimal totalFat;
    private BigDecimal totalFiber;
    private BigDecimal proteinGoal;
    private BigDecimal carbGoal;
    private BigDecimal fatGoal;
    private List<MealSummaryDTO> meals;
    private Integer waterGlasses;
    private Integer waterGoal;

    // Constructors
    public DailyNutritionSummaryDTO() {}

    // Getters and Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getTotalCalories() { return totalCalories; }
    public void setTotalCalories(BigDecimal totalCalories) { this.totalCalories = totalCalories; }

    public BigDecimal getCalorieGoal() { return calorieGoal; }
    public void setCalorieGoal(BigDecimal calorieGoal) { this.calorieGoal = calorieGoal; }

    public BigDecimal getTotalCarbs() { return totalCarbs; }
    public void setTotalCarbs(BigDecimal totalCarbs) { this.totalCarbs = totalCarbs; }

    public BigDecimal getTotalProtein() { return totalProtein; }
    public void setTotalProtein(BigDecimal totalProtein) { this.totalProtein = totalProtein; }

    public BigDecimal getTotalFat() { return totalFat; }
    public void setTotalFat(BigDecimal totalFat) { this.totalFat = totalFat; }

    public BigDecimal getTotalFiber() { return totalFiber; }
    public void setTotalFiber(BigDecimal totalFiber) { this.totalFiber = totalFiber; }

    public BigDecimal getProteinGoal() { return proteinGoal; }
    public void setProteinGoal(BigDecimal proteinGoal) { this.proteinGoal = proteinGoal; }

    public BigDecimal getCarbGoal() { return carbGoal; }
    public void setCarbGoal(BigDecimal carbGoal) { this.carbGoal = carbGoal; }

    public BigDecimal getFatGoal() { return fatGoal; }
    public void setFatGoal(BigDecimal fatGoal) { this.fatGoal = fatGoal; }

    public List<MealSummaryDTO> getMeals() { return meals; }
    public void setMeals(List<MealSummaryDTO> meals) { this.meals = meals; }

    public Integer getWaterGlasses() { return waterGlasses; }
    public void setWaterGlasses(Integer waterGlasses) { this.waterGlasses = waterGlasses; }

    public Integer getWaterGoal() { return waterGoal; }
    public void setWaterGoal(Integer waterGoal) { this.waterGoal = waterGoal; }

    // Helper methods
    public BigDecimal getCalorieProgress() {
        if (calorieGoal == null || calorieGoal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalCalories.divide(calorieGoal, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getRemainingCalories() {
        if (calorieGoal == null) return BigDecimal.ZERO;
        return calorieGoal.subtract(totalCalories != null ? totalCalories : BigDecimal.ZERO);
    }

    public static class MealSummaryDTO {
        private String mealType; // BREAKFAST, LUNCH, DINNER, SNACK
        private BigDecimal calories;
        private Integer foodCount;

        public MealSummaryDTO() {}

        public MealSummaryDTO(String mealType, BigDecimal calories, Integer foodCount) {
            this.mealType = mealType;
            this.calories = calories;
            this.foodCount = foodCount;
        }

        public String getMealType() { return mealType; }
        public void setMealType(String mealType) { this.mealType = mealType; }

        public BigDecimal getCalories() { return calories; }
        public void setCalories(BigDecimal calories) { this.calories = calories; }

        public Integer getFoodCount() { return foodCount; }
        public void setFoodCount(Integer foodCount) { this.foodCount = foodCount; }
    }
}