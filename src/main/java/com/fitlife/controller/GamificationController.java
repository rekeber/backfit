package com.fitlife.controller;

import com.fitlife.entity.LootBox;
import com.fitlife.entity.UserAvatar;
import com.fitlife.service.GamificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gamification")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GamificationController {
    
    private final GamificationService gamificationService;
    
    // FitCoins Management
    @GetMapping("/fitcoins/balance")
    public ResponseEntity<?> getFitCoinsBalance(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            BigDecimal balance = gamificationService.getCurrentBalance(
                    gamificationService.getUserByEmail(userEmail));
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "balance", balance
            ));
        } catch (Exception e) {
            log.error("Error getting FitCoins balance: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // Loot Box System
    @PostMapping("/lootbox/daily")
    public ResponseEntity<?> claimDailyLootBox(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            LootBox dailyBox = gamificationService.createDailyLootBox(
                    gamificationService.getUserByEmail(userEmail));
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Daily loot box claimed successfully",
                    "lootBox", dailyBox
            ));
        } catch (Exception e) {
            log.error("Error claiming daily loot box: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/lootbox/{lootBoxId}/open")
    public ResponseEntity<?> openLootBox(
            @PathVariable Long lootBoxId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            GamificationService.LootBoxRewards rewards = gamificationService.openLootBox(lootBoxId, userEmail);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Loot box opened successfully",
                    "rewards", rewards
            ));
        } catch (Exception e) {
            log.error("Error opening loot box: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/lootbox/my")
    public ResponseEntity<?> getMyLootBoxes(
            @RequestParam(defaultValue = "false") boolean openedOnly,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            List<LootBox> lootBoxes = gamificationService.getUserLootBoxes(userEmail, openedOnly);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "lootBoxes", lootBoxes
            ));
        } catch (Exception e) {
            log.error("Error getting user loot boxes: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // Avatar System
    @GetMapping("/avatar")
    public ResponseEntity<?> getUserAvatar(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            UserAvatar avatar = gamificationService.getOrCreateUserAvatar(
                    gamificationService.getUserByEmail(userEmail));
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "avatar", avatar
            ));
        } catch (Exception e) {
            log.error("Error getting user avatar: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/avatar/update-progress")
    public ResponseEntity<?> updateAvatarProgress(
            @RequestBody UpdateProgressRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            UserAvatar avatar = gamificationService.updateAvatarProgress(
                    gamificationService.getUserByEmail(userEmail), 
                    request.getCurrentWeight());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Avatar progress updated successfully",
                    "avatar", avatar
            ));
        } catch (Exception e) {
            log.error("Error updating avatar progress: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/avatar/workout-completed")
    public ResponseEntity<?> recordWorkoutCompletion(
            @RequestBody WorkoutCompletionRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            UserAvatar avatar = gamificationService.recordWorkoutCompletion(
                    gamificationService.getUserByEmail(userEmail), 
                    request.getCaloriesBurned());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Workout completion recorded successfully",
                    "avatar", avatar
            ));
        } catch (Exception e) {
            log.error("Error recording workout completion: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/avatar/customize")
    public ResponseEntity<?> customizeAvatar(
            @RequestBody AvatarCustomizationRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            UserAvatar avatar = gamificationService.customizeAvatar(
                    userEmail,
                    request.getAvatarName(),
                    request.getBodyType(),
                    request.getSkinTone(),
                    request.getHairStyle(),
                    request.getHairColor(),
                    request.getEyeColor()
            );
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Avatar customized successfully",
                    "avatar", avatar
            ));
        } catch (Exception e) {
            log.error("Error customizing avatar: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // Daily Spin System
    @PostMapping("/daily-spin")
    public ResponseEntity<?> performDailySpin(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            GamificationService.DailySpinResult result = gamificationService.performDailySpin(userEmail);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Daily spin completed successfully",
                    "result", result
            ));
        } catch (Exception e) {
            log.error("Error performing daily spin: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // Achievements
    @GetMapping("/achievements")
    public ResponseEntity<?> getUserAchievements(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            List<Object> achievements = gamificationService.getUserAchievements(userEmail);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "achievements", achievements
            ));
        } catch (Exception e) {
            log.error("Error getting user achievements: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // Leaderboards
    @GetMapping("/leaderboard/fitcoins")
    public ResponseEntity<?> getFitCoinsLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Object> leaderboard = gamificationService.getFitCoinsLeaderboard(limit);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "leaderboard", leaderboard
            ));
        } catch (Exception e) {
            log.error("Error getting FitCoins leaderboard: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/leaderboard/transformation")
    public ResponseEntity<?> getTransformationLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Object> leaderboard = gamificationService.getTransformationLeaderboard(limit);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "leaderboard", leaderboard
            ));
        } catch (Exception e) {
            log.error("Error getting transformation leaderboard: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // DTOs
    @lombok.Data
    public static class UpdateProgressRequest {
        private Double currentWeight;
    }
    
    @lombok.Data
    public static class WorkoutCompletionRequest {
        private Integer caloriesBurned;
    }
    
    @lombok.Data
    public static class AvatarCustomizationRequest {
        private String avatarName;
        private UserAvatar.BodyType bodyType;
        private UserAvatar.SkinTone skinTone;
        private UserAvatar.HairStyle hairStyle;
        private UserAvatar.HairColor hairColor;
        private UserAvatar.EyeColor eyeColor;
    }
}