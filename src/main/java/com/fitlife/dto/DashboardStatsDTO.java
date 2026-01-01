package com.fitlife.dto;

import java.math.BigDecimal;

public class DashboardStatsDTO {
    private BigDecimal currentWeight;
    private BigDecimal bmi;
    private Integer currentStreak;
    private BigDecimal fitCoinsBalance;
    private Integer totalAchievements;
    private Integer unlockedAchievements;
    private Integer globalRanking;
    private Integer totalWorkouts;
    private BigDecimal totalCaloriesBurned;
    private Integer longestStreak;

    // Constructors
    public DashboardStatsDTO() {}

    public DashboardStatsDTO(BigDecimal currentWeight, BigDecimal bmi, Integer currentStreak, 
                           BigDecimal fitCoinsBalance, Integer totalAchievements, 
                           Integer unlockedAchievements, Integer globalRanking, 
                           Integer totalWorkouts, BigDecimal totalCaloriesBurned, 
                           Integer longestStreak) {
        this.currentWeight = currentWeight;
        this.bmi = bmi;
        this.currentStreak = currentStreak;
        this.fitCoinsBalance = fitCoinsBalance;
        this.totalAchievements = totalAchievements;
        this.unlockedAchievements = unlockedAchievements;
        this.globalRanking = globalRanking;
        this.totalWorkouts = totalWorkouts;
        this.totalCaloriesBurned = totalCaloriesBurned;
        this.longestStreak = longestStreak;
    }

    // Getters and Setters
    public BigDecimal getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(BigDecimal currentWeight) { this.currentWeight = currentWeight; }

    public BigDecimal getBmi() { return bmi; }
    public void setBmi(BigDecimal bmi) { this.bmi = bmi; }

    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }

    public BigDecimal getFitCoinsBalance() { return fitCoinsBalance; }
    public void setFitCoinsBalance(BigDecimal fitCoinsBalance) { this.fitCoinsBalance = fitCoinsBalance; }

    public Integer getTotalAchievements() { return totalAchievements; }
    public void setTotalAchievements(Integer totalAchievements) { this.totalAchievements = totalAchievements; }

    public Integer getUnlockedAchievements() { return unlockedAchievements; }
    public void setUnlockedAchievements(Integer unlockedAchievements) { this.unlockedAchievements = unlockedAchievements; }

    public Integer getGlobalRanking() { return globalRanking; }
    public void setGlobalRanking(Integer globalRanking) { this.globalRanking = globalRanking; }

    public Integer getTotalWorkouts() { return totalWorkouts; }
    public void setTotalWorkouts(Integer totalWorkouts) { this.totalWorkouts = totalWorkouts; }

    public BigDecimal getTotalCaloriesBurned() { return totalCaloriesBurned; }
    public void setTotalCaloriesBurned(BigDecimal totalCaloriesBurned) { this.totalCaloriesBurned = totalCaloriesBurned; }

    public Integer getLongestStreak() { return longestStreak; }
    public void setLongestStreak(Integer longestStreak) { this.longestStreak = longestStreak; }
}