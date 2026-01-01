package com.fitlife.controller;

import com.fitlife.service.DashboardService;
import com.fitlife.service.CustomUserDetailsService;
import com.fitlife.dto.DashboardStatsDTO;
import com.fitlife.dto.DailyNutritionSummaryDTO;
import com.fitlife.dto.RecentActivityDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
@Slf4j
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
            CustomUserDetailsService.CustomUserPrincipal principal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            return principal.getUser().getId();
        }
        throw new RuntimeException("User not authenticated properly");
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(Authentication authentication) {
        log.info("=== DASHBOARD STATS ENDPOINT CALLED ===");
        log.info("Authentication object: {}", authentication);
        log.info("Authentication name: {}", authentication != null ? authentication.getName() : "null");
        log.info("Authentication principal: {}", authentication != null ? authentication.getPrincipal() : "null");
        
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Extracted user ID: {}", userId);
        
        DashboardStatsDTO stats = dashboardService.getDashboardStats(userId);
        log.info("Dashboard stats returned: {}", stats);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/nutrition/daily")
    public ResponseEntity<DailyNutritionSummaryDTO> getDailyNutrition(
            Authentication authentication,
            @RequestParam(required = false) String date) {
        log.info("Getting daily nutrition for user: {}", authentication.getName());
        Long userId = getUserIdFromAuthentication(authentication);
        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        DailyNutritionSummaryDTO nutrition = dashboardService.getDailyNutritionSummary(userId, targetDate);
        return ResponseEntity.ok(nutrition);
    }

    @GetMapping("/activities/recent")
    public ResponseEntity<List<RecentActivityDTO>> getRecentActivities(Authentication authentication) {
        log.info("Getting recent activities for user: {}", authentication.getName());
        Long userId = getUserIdFromAuthentication(authentication);
        List<RecentActivityDTO> activities = dashboardService.getRecentActivities(userId);
        return ResponseEntity.ok(activities);
    }

    @PostMapping("/hydration")
    public ResponseEntity<Void> logWaterIntake(
            Authentication authentication,
            @RequestParam int glasses) {
        log.info("Logging water intake for user: {}, glasses: {}", authentication.getName(), glasses);
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Extracted user ID: {}", userId);
        dashboardService.logWaterIntake(userId, glasses);
        log.info("Water intake logged successfully");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hydration/today")
    public ResponseEntity<Integer> getTodayWaterIntake(Authentication authentication) {
        log.info("Getting today's water intake for user: {}", authentication.getName());
        Long userId = getUserIdFromAuthentication(authentication);
        int glasses = dashboardService.getTodayWaterIntake(userId);
        log.info("Today's water intake: {} glasses", glasses);
        return ResponseEntity.ok(glasses);
    }
}