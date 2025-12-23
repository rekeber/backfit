package com.fitlife.dto.auth;

import com.fitlife.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String profileImageUrl;
    private Integer age;
    private Double height;
    private Double currentWeight;
    private Double targetWeight;
    private User.ActivityLevel activityLevel;
    private User.Goal goal;
    private Set<String> dietaryRestrictions;
    private Set<String> allergies;
    private Integer streakDays;
    private Double totalWeightLost;
    private Integer totalPoints;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private Double bmi;
    private Double dailyCalories;
}