package com.fitlife.controller;

import com.fitlife.entity.User;
import com.fitlife.entity.WorkoutSession;
import com.fitlife.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workouts")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class WorkoutController {
    
    private final WorkoutService workoutService;
    
    @GetMapping
    public ResponseEntity<Page<WorkoutSession>> getUserWorkouts(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        return ResponseEntity.ok(workoutService.getUserWorkouts(user, pageable));
    }
    
    @GetMapping("/recent")
    public ResponseEntity<List<WorkoutSession>> getRecentWorkouts(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(workoutService.getRecentWorkouts(user, limit));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutSession> getWorkoutById(@PathVariable Long id) {
        return workoutService.getWorkoutById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/start")
    public ResponseEntity<WorkoutSession> startWorkout(
            @AuthenticationPrincipal User user,
            @RequestBody StartWorkoutRequest request) {
        WorkoutSession workout = workoutService.startWorkout(user, request.getExerciseId(), request.getNotes());
        return ResponseEntity.ok(workout);
    }
    
    @PutMapping("/{id}/finish")
    public ResponseEntity<WorkoutSession> finishWorkout(
            @PathVariable Long id,
            @RequestBody FinishWorkoutRequest request) {
        try {
            WorkoutSession workout = workoutService.finishWorkout(id, request.getAdditionalNotes());
            return ResponseEntity.ok(workout);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/quick")
    public ResponseEntity<WorkoutSession> createQuickWorkout(
            @AuthenticationPrincipal User user,
            @RequestBody QuickWorkoutRequest request) {
        WorkoutSession workout = workoutService.createQuickWorkout(
                user, 
                request.getExerciseId(), 
                request.getDuration(), 
                request.getNotes()
        );
        return ResponseEntity.ok(workout);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        try {
            workoutService.deleteWorkout(id, user);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<WorkoutStats> getWorkoutStats(@AuthenticationPrincipal User user) {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        
        WorkoutStats stats = WorkoutStats.builder()
                .weeklyWorkouts(workoutService.getWorkoutCountSince(user, weekAgo))
                .monthlyWorkouts(workoutService.getWorkoutCountSince(user, monthAgo))
                .todayCalories(workoutService.getTodayCaloriesBurned(user))
                .weeklyCalories(workoutService.getTotalCaloriesBurnedSince(user, weekAgo))
                .monthlyCalories(workoutService.getTotalCaloriesBurnedSince(user, monthAgo))
                .weeklyTime(workoutService.getTotalWorkoutTimeSince(user, weekAgo))
                .monthlyTime(workoutService.getTotalWorkoutTimeSince(user, monthAgo))
                .build();
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/range")
    public ResponseEntity<List<WorkoutSession>> getWorkoutsByDateRange(
            @AuthenticationPrincipal User user,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        return ResponseEntity.ok(workoutService.getUserWorkoutsByDateRange(user, start, end));
    }
    
    // DTOs
    public static class StartWorkoutRequest {
        private Long exerciseId;
        private String notes;
        
        public Long getExerciseId() { return exerciseId; }
        public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
    
    public static class FinishWorkoutRequest {
        private String additionalNotes;
        
        public String getAdditionalNotes() { return additionalNotes; }
        public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
    }
    
    public static class QuickWorkoutRequest {
        private Long exerciseId;
        private Integer duration;
        private String notes;
        
        public Long getExerciseId() { return exerciseId; }
        public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }
        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
    
    public static class WorkoutStats {
        private Long weeklyWorkouts;
        private Long monthlyWorkouts;
        private Integer todayCalories;
        private Integer weeklyCalories;
        private Integer monthlyCalories;
        private Integer weeklyTime;
        private Integer monthlyTime;
        
        public static WorkoutStatsBuilder builder() { return new WorkoutStatsBuilder(); }
        
        // Getters and setters
        public Long getWeeklyWorkouts() { return weeklyWorkouts; }
        public void setWeeklyWorkouts(Long weeklyWorkouts) { this.weeklyWorkouts = weeklyWorkouts; }
        public Long getMonthlyWorkouts() { return monthlyWorkouts; }
        public void setMonthlyWorkouts(Long monthlyWorkouts) { this.monthlyWorkouts = monthlyWorkouts; }
        public Integer getTodayCalories() { return todayCalories; }
        public void setTodayCalories(Integer todayCalories) { this.todayCalories = todayCalories; }
        public Integer getWeeklyCalories() { return weeklyCalories; }
        public void setWeeklyCalories(Integer weeklyCalories) { this.weeklyCalories = weeklyCalories; }
        public Integer getMonthlyCalories() { return monthlyCalories; }
        public void setMonthlyCalories(Integer monthlyCalories) { this.monthlyCalories = monthlyCalories; }
        public Integer getWeeklyTime() { return weeklyTime; }
        public void setWeeklyTime(Integer weeklyTime) { this.weeklyTime = weeklyTime; }
        public Integer getMonthlyTime() { return monthlyTime; }
        public void setMonthlyTime(Integer monthlyTime) { this.monthlyTime = monthlyTime; }
        
        public static class WorkoutStatsBuilder {
            private Long weeklyWorkouts;
            private Long monthlyWorkouts;
            private Integer todayCalories;
            private Integer weeklyCalories;
            private Integer monthlyCalories;
            private Integer weeklyTime;
            private Integer monthlyTime;
            
            public WorkoutStatsBuilder weeklyWorkouts(Long weeklyWorkouts) { this.weeklyWorkouts = weeklyWorkouts; return this; }
            public WorkoutStatsBuilder monthlyWorkouts(Long monthlyWorkouts) { this.monthlyWorkouts = monthlyWorkouts; return this; }
            public WorkoutStatsBuilder todayCalories(Integer todayCalories) { this.todayCalories = todayCalories; return this; }
            public WorkoutStatsBuilder weeklyCalories(Integer weeklyCalories) { this.weeklyCalories = weeklyCalories; return this; }
            public WorkoutStatsBuilder monthlyCalories(Integer monthlyCalories) { this.monthlyCalories = monthlyCalories; return this; }
            public WorkoutStatsBuilder weeklyTime(Integer weeklyTime) { this.weeklyTime = weeklyTime; return this; }
            public WorkoutStatsBuilder monthlyTime(Integer monthlyTime) { this.monthlyTime = monthlyTime; return this; }
            
            public WorkoutStats build() {
                WorkoutStats stats = new WorkoutStats();
                stats.setWeeklyWorkouts(weeklyWorkouts);
                stats.setMonthlyWorkouts(monthlyWorkouts);
                stats.setTodayCalories(todayCalories);
                stats.setWeeklyCalories(weeklyCalories);
                stats.setMonthlyCalories(monthlyCalories);
                stats.setWeeklyTime(weeklyTime);
                stats.setMonthlyTime(monthlyTime);
                return stats;
            }
        }
    }
}