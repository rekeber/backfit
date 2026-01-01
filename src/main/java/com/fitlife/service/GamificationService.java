package com.fitlife.service;

import com.fitlife.entity.*;
import com.fitlife.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GamificationService {
    
    private final FitCoinRepository fitCoinRepository;
    private final LootBoxRepository lootBoxRepository;
    private final UserAvatarRepository userAvatarRepository;
    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final Random random = new Random();
    
    // FitCoins Management
    public FitCoin awardFitCoins(User user, BigDecimal amount, FitCoin.CoinSource source, String description) {
        BigDecimal currentBalance = getCurrentBalance(user);
        BigDecimal newBalance = currentBalance.add(amount);
        
        // Apply multipliers
        BigDecimal finalAmount = applyMultipliers(user, amount, source);
        BigDecimal finalBalance = currentBalance.add(finalAmount);
        
        FitCoin transaction = FitCoin.builder()
                .user(user)
                .transactionType(FitCoin.TransactionType.EARNED)
                .amount(finalAmount)
                .balanceAfter(finalBalance)
                .source(source)
                .description(description)
                .build();
        
        transaction = fitCoinRepository.save(transaction);
        
        // Update user total points
        user.setTotalPoints(user.getTotalPoints() + finalAmount.intValue());
        userRepository.save(user);
        
        // Check for achievements
        checkCoinAchievements(user, finalBalance);
        
        log.info("Awarded {} FitCoins to user {} for {}", finalAmount, user.getEmail(), description);
        return transaction;
    }
    
    public FitCoin spendFitCoins(User user, BigDecimal amount, String description, String referenceType, Long referenceId) {
        BigDecimal currentBalance = getCurrentBalance(user);
        
        if (currentBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient FitCoins balance");
        }
        
        BigDecimal newBalance = currentBalance.subtract(amount);
        
        FitCoin transaction = FitCoin.builder()
                .user(user)
                .transactionType(FitCoin.TransactionType.SPENT)
                .amount(amount.negate())
                .balanceAfter(newBalance)
                .source(FitCoin.CoinSource.PURCHASE_REFUND) // Generic spending source
                .description(description)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();
        
        transaction = fitCoinRepository.save(transaction);
        
        log.info("User {} spent {} FitCoins for {}", user.getEmail(), amount, description);
        return transaction;
    }
    
    public BigDecimal getCurrentBalance(User user) {
        BigDecimal balance = fitCoinRepository.getCurrentBalance(user);
        return balance != null ? balance : BigDecimal.ZERO;
    }
    
    // Loot Box System
    public LootBox createDailyLootBox(User user) {
        // Check if user already has today's daily box
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        List<LootBox> todayBoxes = lootBoxRepository.findByUserAndBoxTypeAndCreatedAtAfter(
                user, LootBox.BoxType.DAILY, todayStart);
        
        if (!todayBoxes.isEmpty()) {
            throw new RuntimeException("Daily loot box already claimed today");
        }
        
        LootBox dailyBox = LootBox.createDailyBox(user);
        dailyBox = lootBoxRepository.save(dailyBox);
        
        log.info("Created daily loot box for user {}", user.getEmail());
        return dailyBox;
    }
    
    public LootBox createWorkoutLootBox(User user, int workoutIntensity) {
        LootBox workoutBox = LootBox.createWorkoutBox(user, workoutIntensity);
        workoutBox = lootBoxRepository.save(workoutBox);
        
        log.info("Created workout loot box for user {} with intensity {}", user.getEmail(), workoutIntensity);
        return workoutBox;
    }
    
    public LootBox createStreakLootBox(User user, int streakDays) {
        LootBox streakBox = LootBox.createStreakBox(user, streakDays);
        streakBox = lootBoxRepository.save(streakBox);
        
        log.info("Created streak loot box for user {} with {} days streak", user.getEmail(), streakDays);
        return streakBox;
    }
    
    public LootBoxRewards openLootBox(Long lootBoxId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        LootBox lootBox = lootBoxRepository.findById(lootBoxId)
                .orElseThrow(() -> new RuntimeException("Loot box not found"));
        
        if (!lootBox.getUser().equals(user)) {
            throw new RuntimeException("User not authorized to open this loot box");
        }
        
        if (lootBox.getIsOpened()) {
            throw new RuntimeException("Loot box already opened");
        }
        
        // Open the box and generate contents
        lootBox.openBox();
        lootBox = lootBoxRepository.save(lootBox);
        
        // Award the rewards
        LootBoxRewards rewards = new LootBoxRewards();
        
        if (lootBox.getFitcoinsReward() != null && lootBox.getFitcoinsReward().compareTo(BigDecimal.ZERO) > 0) {
            awardFitCoins(user, lootBox.getFitcoinsReward(), FitCoin.CoinSource.LOOT_BOX, 
                    "Loot box reward: " + lootBox.getBoxType());
            rewards.setFitcoins(lootBox.getFitcoinsReward());
        }
        
        if (lootBox.getFitgemsReward() != null && lootBox.getFitgemsReward() > 0) {
            // TODO: Implement FitGems system
            rewards.setFitgems(lootBox.getFitgemsReward());
        }
        
        // TODO: Award avatar items and special rewards
        
        log.info("User {} opened {} loot box and received {} FitCoins, {} FitGems", 
                user.getEmail(), lootBox.getBoxType(), rewards.getFitcoins(), rewards.getFitgems());
        
        return rewards;
    }
    
    // Avatar System
    public UserAvatar getOrCreateUserAvatar(User user) {
        Optional<UserAvatar> existingAvatar = userAvatarRepository.findByUser(user);
        
        if (existingAvatar.isPresent()) {
            return existingAvatar.get();
        }
        
        // Create default avatar
        UserAvatar avatar = UserAvatar.builder()
                .user(user)
                .avatarName("Avatar_" + user.getId())
                .bodyType(UserAvatar.BodyType.MESOMORPH)
                .skinTone(UserAvatar.SkinTone.MEDIUM)
                .hairStyle(UserAvatar.HairStyle.SHORT)
                .hairColor(UserAvatar.HairColor.BROWN)
                .eyeColor(UserAvatar.EyeColor.BROWN)
                .fitnessLevel(UserAvatar.FitnessLevel.BEGINNER)
                .muscleDefinition(1)
                .transformationProgress(0)
                .build();
        
        avatar = userAvatarRepository.save(avatar);
        
        log.info("Created default avatar for user {}", user.getEmail());
        return avatar;
    }
    
    public UserAvatar updateAvatarProgress(User user, Double currentWeight) {
        UserAvatar avatar = getOrCreateUserAvatar(user);
        
        // Update transformation progress
        avatar.updateTransformationProgress(currentWeight, user.getTargetWeight(), 
                user.getCurrentWeight()); // Assuming we store initial weight
        
        // Update total weight lost
        if (user.getCurrentWeight() != null && currentWeight < user.getCurrentWeight()) {
            double weightLost = user.getCurrentWeight() - currentWeight;
            avatar.setTotalWeightLost(avatar.getTotalWeightLost() + weightLost);
        }
        
        avatar.updateMuscleDefinition();
        
        avatar = userAvatarRepository.save(avatar);
        
        // Check for transformation achievements
        checkTransformationAchievements(user, avatar);
        
        return avatar;
    }
    
    public UserAvatar recordWorkoutCompletion(User user, int caloriesBurned) {
        UserAvatar avatar = getOrCreateUserAvatar(user);
        avatar.completeWorkout(caloriesBurned);
        
        avatar = userAvatarRepository.save(avatar);
        
        // Award FitCoins for workout
        BigDecimal workoutReward = BigDecimal.valueOf(20 + (caloriesBurned / 10)); // Base 20 + calories/10
        awardFitCoins(user, workoutReward, FitCoin.CoinSource.WORKOUT_COMPLETED, 
                "Workout completed: " + caloriesBurned + " calories burned");
        
        // Create workout loot box
        int intensity = Math.min(10, caloriesBurned / 50); // Scale 1-10 based on calories
        createWorkoutLootBox(user, intensity);
        
        // Check for workout achievements
        checkWorkoutAchievements(user, avatar);
        
        return avatar;
    }
    
    // Daily Spin System
    public DailySpinResult performDailySpin(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user already spun today
        // TODO: Implement UserWallet entity to track last spin
        
        // Generate random reward
        DailySpinResult result = generateSpinReward();
        
        // Award the reward
        if (result.getFitcoins().compareTo(BigDecimal.ZERO) > 0) {
            awardFitCoins(user, result.getFitcoins(), FitCoin.CoinSource.DAILY_SPIN, 
                    "Daily spin reward");
        }
        
        // TODO: Award other rewards (FitGems, avatar items, etc.)
        
        log.info("User {} performed daily spin and won {} FitCoins", userEmail, result.getFitcoins());
        return result;
    }
    
    // Achievement System Integration
    private void checkCoinAchievements(User user, BigDecimal totalBalance) {
        // Check for coin milestones
        if (totalBalance.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            unlockAchievement(user, "COIN_COLLECTOR_1000");
        }
        if (totalBalance.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            unlockAchievement(user, "COIN_MASTER_10000");
        }
    }
    
    private void checkWorkoutAchievements(User user, UserAvatar avatar) {
        if (avatar.getTotalWorkoutsCompleted() >= 10) {
            unlockAchievement(user, "WORKOUT_WARRIOR_10");
        }
        if (avatar.getTotalWorkoutsCompleted() >= 100) {
            unlockAchievement(user, "WORKOUT_LEGEND_100");
        }
        if (avatar.getTotalCaloriesBurned() >= 10000) {
            unlockAchievement(user, "CALORIE_CRUSHER_10K");
        }
    }
    
    private void checkTransformationAchievements(User user, UserAvatar avatar) {
        if (avatar.getTransformationProgress() >= 25) {
            unlockAchievement(user, "TRANSFORMATION_QUARTER");
        }
        if (avatar.getTransformationProgress() >= 50) {
            unlockAchievement(user, "TRANSFORMATION_HALFWAY");
        }
        if (avatar.getTransformationProgress() >= 100) {
            unlockAchievement(user, "TRANSFORMATION_COMPLETE");
        }
    }
    
    private void unlockAchievement(User user, String achievementCode) {
        Optional<Achievement> achievement = achievementRepository.findByName(achievementCode);
        if (achievement.isEmpty()) {
            log.warn("Achievement not found: {}", achievementCode);
            return;
        }
        
        // Check if user already has this achievement
        boolean alreadyHas = userAchievementRepository.existsByUserAndAchievement(user, achievement.get());
        if (alreadyHas) {
            return;
        }
        
        // Award achievement
        UserAchievement userAchievement = UserAchievement.builder()
                .user(user)
                .achievement(achievement.get())
                .unlockedAt(LocalDateTime.now())
                .build();
        
        userAchievementRepository.save(userAchievement);
        
        // Award achievement bonus
        awardFitCoins(user, BigDecimal.valueOf(achievement.get().getPoints()), 
                FitCoin.CoinSource.ACHIEVEMENT_UNLOCKED, 
                "Achievement unlocked: " + achievement.get().getName());
        
        log.info("User {} unlocked achievement: {}", user.getEmail(), achievement.get().getName());
    }
    
    // Additional public methods for controller
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
    
    public List<LootBox> getUserLootBoxes(String userEmail, boolean openedOnly) {
        User user = getUserByEmail(userEmail);
        
        if (openedOnly) {
            return lootBoxRepository.findByUserAndIsOpenedTrueOrderByOpenedAtDesc(user);
        } else {
            return lootBoxRepository.findByUserAndIsOpenedFalseOrderByCreatedAtDesc(user);
        }
    }
    
    public UserAvatar customizeAvatar(String userEmail, String avatarName, UserAvatar.BodyType bodyType,
                                    UserAvatar.SkinTone skinTone, UserAvatar.HairStyle hairStyle,
                                    UserAvatar.HairColor hairColor, UserAvatar.EyeColor eyeColor) {
        User user = getUserByEmail(userEmail);
        UserAvatar avatar = getOrCreateUserAvatar(user);
        
        if (avatarName != null) avatar.setAvatarName(avatarName);
        if (bodyType != null) avatar.setBodyType(bodyType);
        if (skinTone != null) avatar.setSkinTone(skinTone);
        if (hairStyle != null) avatar.setHairStyle(hairStyle);
        if (hairColor != null) avatar.setHairColor(hairColor);
        if (eyeColor != null) avatar.setEyeColor(eyeColor);
        
        avatar = userAvatarRepository.save(avatar);
        
        // Award customization bonus
        awardFitCoins(user, BigDecimal.valueOf(10), FitCoin.CoinSource.SOCIAL_INTERACTION, 
                "Avatar customization bonus");
        
        log.info("Avatar customized for user {}", userEmail);
        return avatar;
    }
    
    public List<Object> getUserAchievements(String userEmail) {
        User user = getUserByEmail(userEmail);
        
        List<UserAchievement> userAchievements = userAchievementRepository.findByUserOrderByUnlockedAtDesc(user);
        
        return userAchievements.stream()
                .map(ua -> Map.of(
                        "id", ua.getId(),
                        "achievement", ua.getAchievement(),
                        "unlockedAt", ua.getUnlockedAt(),
                        "progress", 100 // Already unlocked
                ))
                .collect(java.util.stream.Collectors.toList());
    }
    
    public List<Object> getFitCoinsLeaderboard(int limit) {
        // This would require a more complex query to get top users by FitCoins
        // For now, return a simple implementation
        List<User> topUsers = userRepository.findAll().stream()
                .sorted((u1, u2) -> Integer.compare(u2.getTotalPoints(), u1.getTotalPoints()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
        
        return topUsers.stream()
                .map(user -> Map.of(
                        "userId", user.getId(),
                        "name", user.getName(),
                        "totalPoints", user.getTotalPoints(),
                        "currentBalance", getCurrentBalance(user)
                ))
                .collect(java.util.stream.Collectors.toList());
    }
    
    public List<Object> getTransformationLeaderboard(int limit) {
        List<UserAvatar> topAvatars = userAvatarRepository.findTopPerformingAvatars().stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
        
        return topAvatars.stream()
                .map(avatar -> Map.of(
                        "userId", avatar.getUser().getId(),
                        "name", avatar.getUser().getName(),
                        "avatarName", avatar.getAvatarName(),
                        "transformationProgress", avatar.getTransformationProgress(),
                        "totalWeightLost", avatar.getTotalWeightLost(),
                        "totalWorkouts", avatar.getTotalWorkoutsCompleted()
                ))
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Helper methods
    private BigDecimal applyMultipliers(User user, BigDecimal baseAmount, FitCoin.CoinSource source) {
        BigDecimal multiplier = BigDecimal.ONE;
        
        // Streak multiplier
        if (user.getStreakDays() > 0) {
            BigDecimal streakMultiplier = BigDecimal.valueOf(1.0 + (user.getStreakDays() * 0.01)); // 1% per day
            multiplier = multiplier.multiply(streakMultiplier);
        }
        
        // Premium multiplier (if user has premium)
        // TODO: Check if user has premium subscription
        // multiplier = multiplier.multiply(BigDecimal.valueOf(1.5));
        
        // Weekend bonus
        if (LocalDateTime.now().getDayOfWeek().getValue() >= 6) { // Saturday or Sunday
            multiplier = multiplier.multiply(BigDecimal.valueOf(1.2)); // 20% weekend bonus
        }
        
        return baseAmount.multiply(multiplier);
    }
    
    private DailySpinResult generateSpinReward() {
        int roll = random.nextInt(100);
        
        DailySpinResult result = new DailySpinResult();
        
        if (roll < 40) { // 40% - Small FitCoins
            result.setFitcoins(BigDecimal.valueOf(10 + random.nextInt(20))); // 10-30 coins
            result.setRewardType("FITCOINS_SMALL");
        } else if (roll < 70) { // 30% - Medium FitCoins
            result.setFitcoins(BigDecimal.valueOf(30 + random.nextInt(40))); // 30-70 coins
            result.setRewardType("FITCOINS_MEDIUM");
        } else if (roll < 85) { // 15% - Large FitCoins
            result.setFitcoins(BigDecimal.valueOf(70 + random.nextInt(80))); // 70-150 coins
            result.setRewardType("FITCOINS_LARGE");
        } else if (roll < 95) { // 10% - FitGems
            result.setFitgems(1 + random.nextInt(3)); // 1-3 gems
            result.setRewardType("FITGEMS");
        } else { // 5% - Jackpot
            result.setFitcoins(BigDecimal.valueOf(200 + random.nextInt(300))); // 200-500 coins
            result.setFitgems(5 + random.nextInt(10)); // 5-15 gems
            result.setRewardType("JACKPOT");
        }
        
        return result;
    }
    
    // DTOs
    @lombok.Data
    public static class LootBoxRewards {
        private BigDecimal fitcoins = BigDecimal.ZERO;
        private Integer fitgems = 0;
        private List<String> avatarItems;
        private List<String> specialRewards;
    }
    
    @lombok.Data
    public static class DailySpinResult {
        private String rewardType;
        private BigDecimal fitcoins = BigDecimal.ZERO;
        private Integer fitgems = 0;
        private String specialReward;
    }
}