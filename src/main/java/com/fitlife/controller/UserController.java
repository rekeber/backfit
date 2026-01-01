package com.fitlife.controller;

import com.fitlife.entity.User;
import com.fitlife.entity.Achievement;
import com.fitlife.entity.UserAchievement;
import com.fitlife.service.UserService;
import com.fitlife.service.AuthService;
import com.fitlife.dto.auth.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class UserController {
    
    private final UserService userService;
    private final AuthService authService;
    
    // User profile endpoints
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getCurrentUserProfile(Authentication auth) {
        String email = auth.getName();
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok(authService.mapToUserResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody User userDetails, Authentication auth) {
        String email = auth.getName();
        try {
            return ResponseEntity.ok(userService.updateUserProfile(email, userDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/profile/image")
    public ResponseEntity<User> updateProfileImage(@RequestParam String imageUrl, Authentication auth) {
        String email = auth.getName();
        try {
            return ResponseEntity.ok(userService.updateProfileImage(email, imageUrl));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Search endpoints
    @GetMapping("/search")
    public ResponseEntity<Page<User>> searchUsers(@RequestParam String q, Pageable pageable) {
        return ResponseEntity.ok(userService.searchUsers(q, pageable));
    }
    
    @GetMapping("/leaderboard")
    public ResponseEntity<List<User>> getLeaderboard(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(userService.getTopUsersByPoints(limit));
    }
    
    // Achievements endpoints
    @GetMapping("/achievements")
    public ResponseEntity<List<Achievement>> getAllAchievements() {
        return ResponseEntity.ok(userService.getAllAchievements());
    }
    
    @GetMapping("/achievements/my")
    public ResponseEntity<List<UserAchievement>> getMyAchievements(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(userService.getUserAchievements(email));
    }
    
    @GetMapping("/achievements/available")
    public ResponseEntity<List<Achievement>> getAvailableAchievements(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(userService.getAvailableAchievements(email));
    }
    
    @PostMapping("/achievements/{achievementId}/unlock")
    public ResponseEntity<UserAchievement> unlockAchievement(
            @PathVariable Long achievementId,
            Authentication auth) {
        String email = auth.getName();
        try {
            return ResponseEntity.ok(userService.unlockAchievement(email, achievementId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Statistics endpoints
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(userService.getUserStats(email));
    }
    
    @GetMapping("/stats/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(userService.getDashboardStats(email));
    }
    
    @GetMapping("/progress")
    public ResponseEntity<Map<String, Object>> getUserProgress(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(userService.getUserProgress(email));
    }
    
    // Activity endpoints
    @PostMapping("/activity/checkin")
    public ResponseEntity<Map<String, Object>> dailyCheckIn(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(userService.performDailyCheckIn(email));
    }
    
    @PostMapping("/points/add")
    public ResponseEntity<User> addPoints(@RequestParam int points, Authentication auth) {
        String email = auth.getName();
        try {
            return ResponseEntity.ok(userService.addPoints(email, points));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Weight tracking endpoints
    @PostMapping("/weight")
    public ResponseEntity<User> updateWeight(@RequestParam Double weight, Authentication auth) {
        String email = auth.getName();
        try {
            return ResponseEntity.ok(userService.updateWeight(email, weight));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/weight/history")
    public ResponseEntity<List<Map<String, Object>>> getWeightHistory(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(userService.getWeightHistory(email));
    }
    
    // Account management
    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(Authentication auth) {
        String email = auth.getName();
        try {
            userService.deleteAccount(email);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/account/deactivate")
    public ResponseEntity<Void> deactivateAccount(Authentication auth) {
        String email = auth.getName();
        try {
            userService.deactivateAccount(email);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/account/activate")
    public ResponseEntity<Void> activateAccount(Authentication auth) {
        String email = auth.getName();
        try {
            userService.activateAccount(email);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}