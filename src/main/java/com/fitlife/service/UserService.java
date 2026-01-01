package com.fitlife.service;

import com.fitlife.entity.User;
import com.fitlife.entity.Achievement;
import com.fitlife.entity.UserAchievement;
import com.fitlife.repository.UserRepository;
import com.fitlife.repository.AchievementRepository;
import com.fitlife.repository.UserAchievementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .accountExpired(false)
                .accountLocked(!user.getIsActive())
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public User createUser(String email, String name, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User already exists with email: " + email);
        }
        
        User user = User.builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(password))
                .role(User.Role.USER)
                .isActive(true)
                .emailVerified(false)
                .totalPoints(0)
                .streakDays(0)
                .totalWeightLost(0.0)
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("Created new user: {}", savedUser.getEmail());
        return savedUser;
    }
    
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    public User updateUserProfile(String email, User userDetails) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        user.setName(userDetails.getName());
        user.setAge(userDetails.getAge());
        user.setHeight(userDetails.getHeight());
        user.setCurrentWeight(userDetails.getCurrentWeight());
        user.setTargetWeight(userDetails.getTargetWeight());
        user.setActivityLevel(userDetails.getActivityLevel());
        user.setGoal(userDetails.getGoal());
        user.setDietaryRestrictions(userDetails.getDietaryRestrictions());
        user.setAllergies(userDetails.getAllergies());
        
        User updatedUser = userRepository.save(user);
        log.info("Updated profile for user: {}", updatedUser.getEmail());
        return updatedUser;
    }
    
    public User updateProfileImage(String email, String imageUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        user.setProfileImageUrl(imageUrl);
        User updatedUser = userRepository.save(user);
        log.info("Updated profile image for user: {}", updatedUser.getEmail());
        return updatedUser;
    }
    
    public void updateLastLogin(User user) {
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }
    
    public User addPoints(String email, int points) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        user.setTotalPoints(user.getTotalPoints() + points);
        User updatedUser = userRepository.save(user);
        log.info("Added {} points to user {}. Total: {}", points, user.getEmail(), user.getTotalPoints());
        return updatedUser;
    }
    
    public User updateWeight(String email, Double weight) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        Double previousWeight = user.getCurrentWeight();
        user.setCurrentWeight(weight);
        
        if (previousWeight != null && weight < previousWeight) {
            user.setTotalWeightLost(user.getTotalWeightLost() + (previousWeight - weight));
        }
        
        User updatedUser = userRepository.save(user);
        log.info("Updated weight for user: {} from {} to {}", user.getEmail(), previousWeight, weight);
        return updatedUser;
    }
    
    public Map<String, Object> performDailyCheckIn(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastLogin = user.getLastLogin();
        
        Map<String, Object> result = new HashMap<>();
        
        if (lastLogin == null || !lastLogin.toLocalDate().equals(now.toLocalDate())) {
            // First check-in today
            if (lastLogin != null && lastLogin.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
                // Consecutive day
                user.setStreakDays(user.getStreakDays() + 1);
            } else if (lastLogin == null || !lastLogin.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
                // Reset streak
                user.setStreakDays(1);
            }
            
            user.setLastLogin(now);
            int pointsEarned = 10 + (user.getStreakDays() * 2); // Bonus points for streak
            user.setTotalPoints(user.getTotalPoints() + pointsEarned);
            
            userRepository.save(user);
            
            result.put("success", true);
            result.put("pointsEarned", pointsEarned);
            result.put("currentStreak", user.getStreakDays());
            result.put("message", "Daily check-in completed!");
        } else {
            result.put("success", false);
            result.put("message", "Already checked in today");
        }
        
        return result;
    }
    
    public void verifyEmail(User user) {
        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Verified email for user: {}", user.getEmail());
    }
    
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        userRepository.delete(user);
        log.info("Deleted account for user: {}", email);
    }
    
    public void deactivateAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        user.setIsActive(false);
        userRepository.save(user);
        log.info("Deactivated account for user: {}", email);
    }
    
    public void activateAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        user.setIsActive(true);
        userRepository.save(user);
        log.info("Activated account for user: {}", email);
    }
    
    public boolean changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Changed password for user: {}", user.getEmail());
        return true;
    }
    
    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(searchTerm, pageable);
    }
    
    public Page<User> getAllActiveUsers(Pageable pageable) {
        return userRepository.findByIsActiveTrueOrderByTotalPointsDesc(pageable);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public void deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            log.info("Deleted user with id: {}", userId);
        } else {
            throw new RuntimeException("User not found with id: " + userId);
        }
    }
    
    // Statistics methods
    public Long getTotalUsersCount() {
        return userRepository.count();
    }
    
    public Long getActiveUsersCount() {
        return userRepository.countByIsActiveTrue();
    }
    
    public List<User> getTopUsersByPoints(int limit) {
        return userRepository.findByIsActiveTrueOrderByTotalPointsDesc()
                .stream()
                .limit(limit)
                .toList();
    }
    
    // Achievement methods
    public List<Achievement> getAllAchievements() {
        return achievementRepository.findByIsActiveTrueOrderByCategoryAscNameAsc();
    }
    
    public List<UserAchievement> getUserAchievements(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        return userAchievementRepository.findByUserOrderByUnlockedAtDesc(user);
    }
    
    public List<Achievement> getAvailableAchievements(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        List<Long> userAchievementIds = userAchievementRepository.findByUser(user)
                .stream()
                .map(ua -> ua.getAchievement().getId())
                .toList();
        
        return achievementRepository.findByIsActiveTrueAndIdNotIn(userAchievementIds);
    }
    
    public UserAchievement unlockAchievement(String email, Long achievementId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new RuntimeException("Achievement not found with id: " + achievementId));
        
        // Check if user already has this achievement
        if (userAchievementRepository.existsByUserAndAchievement(user, achievement)) {
            throw new RuntimeException("User already has this achievement");
        }
        
        UserAchievement userAchievement = UserAchievement.builder()
                .user(user)
                .achievement(achievement)
                .unlockedAt(LocalDateTime.now())
                .progress(achievement.getRequiredValue())
                .isCompleted(true)
                .build();
        
        // Add points to user
        user.setTotalPoints(user.getTotalPoints() + achievement.getPoints());
        userRepository.save(user);
        
        UserAchievement saved = userAchievementRepository.save(userAchievement);
        log.info("Unlocked achievement {} for user {}", achievement.getTitle(), user.getEmail());
        return saved;
    }
    
    // Statistics methods
    public Map<String, Object> getUserStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPoints", user.getTotalPoints());
        stats.put("currentStreak", user.getStreakDays());
        stats.put("totalWeightLost", user.getTotalWeightLost());
        stats.put("bmi", user.getBmi());
        stats.put("dailyCalories", user.getDailyCalories());
        stats.put("achievementsCount", userAchievementRepository.countByUserAndIsCompletedTrue(user));
        
        return stats;
    }
    
    public Map<String, Object> getDashboardStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("welcomeMessage", "¡Hola " + user.getName() + "!");
        stats.put("currentWeight", user.getCurrentWeight());
        stats.put("targetWeight", user.getTargetWeight());
        stats.put("weightProgress", calculateWeightProgress(user));
        stats.put("streakDays", user.getStreakDays());
        stats.put("totalPoints", user.getTotalPoints());
        stats.put("bmi", user.getBmi());
        stats.put("dailyCalorieGoal", user.getDailyCalories());
        
        return stats;
    }
    
    public Map<String, Object> getUserProgress(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        Map<String, Object> progress = new HashMap<>();
        progress.put("weightProgress", calculateWeightProgress(user));
        progress.put("streakProgress", user.getStreakDays());
        progress.put("pointsProgress", user.getTotalPoints());
        progress.put("achievementsProgress", userAchievementRepository.countByUserAndIsCompletedTrue(user));
        
        return progress;
    }
    
    public List<Map<String, Object>> getWeightHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // For now, return current weight as single entry
        // In a real app, you'd have a separate WeightEntry entity
        List<Map<String, Object>> history = new ArrayList<>();
        Map<String, Object> entry = new HashMap<>();
        entry.put("date", LocalDateTime.now());
        entry.put("weight", user.getCurrentWeight());
        history.add(entry);
        
        return history;
    }
    
    private double calculateWeightProgress(User user) {
        if (user.getCurrentWeight() == null || user.getTargetWeight() == null) {
            return 0.0;
        }
        
        double startWeight = user.getCurrentWeight() + user.getTotalWeightLost();
        double totalWeightToLose = startWeight - user.getTargetWeight();
        
        if (totalWeightToLose <= 0) {
            return 100.0; // Already at or below target
        }
        
        return (user.getTotalWeightLost() / totalWeightToLose) * 100.0;
    }
}