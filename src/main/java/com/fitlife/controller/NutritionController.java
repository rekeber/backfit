package com.fitlife.controller;

import com.fitlife.entity.Food;
import com.fitlife.entity.FoodEntry;
import com.fitlife.entity.NutritionGoal;
import com.fitlife.service.AIService;
import com.fitlife.service.NutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/nutrition")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class NutritionController {
    
    private final NutritionService nutritionService;
    private final AIService aiService;
    
    // Food endpoints
    @GetMapping("/foods")
    public ResponseEntity<List<Food>> getAllFoods() {
        return ResponseEntity.ok(nutritionService.getAllFoods());
    }
    
    @GetMapping("/foods/{id}")
    public ResponseEntity<Food> getFoodById(@PathVariable Long id) {
        return nutritionService.getFoodById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/foods/search")
    public ResponseEntity<Page<Food>> searchFoods(
            @RequestParam String q,
            Pageable pageable) {
        return ResponseEntity.ok(nutritionService.searchFoods(q, pageable));
    }
    
    @GetMapping("/foods/category/{category}")
    public ResponseEntity<List<Food>> getFoodsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(nutritionService.getFoodsByCategory(category));
    }
    
    @GetMapping("/foods/healthy")
    public ResponseEntity<List<Food>> getHealthyFoods() {
        return ResponseEntity.ok(nutritionService.getHealthyFoods());
    }
    
    @PostMapping("/foods")
    public ResponseEntity<Food> createFood(@RequestBody Food food) {
        return ResponseEntity.ok(nutritionService.createFood(food));
    }
    
    // Food entries endpoints
    @GetMapping("/entries")
    public ResponseEntity<List<FoodEntry>> getUserFoodEntries(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(nutritionService.getUserFoodEntries(email));
    }
    
    @GetMapping("/entries/date/{date}")
    public ResponseEntity<List<FoodEntry>> getUserFoodEntriesByDate(
            @PathVariable LocalDate date,
            Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(nutritionService.getUserFoodEntriesByDate(email, date));
    }
    
    @GetMapping("/entries/meal/{mealType}")
    public ResponseEntity<List<FoodEntry>> getUserFoodEntriesByMeal(
            @PathVariable String mealType,
            @RequestParam(required = false) LocalDate date,
            Authentication auth) {
        String email = auth.getName();
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(nutritionService.getUserFoodEntriesByMealType(email, targetDate, mealType));
    }
    
    @PostMapping("/entries")
    public ResponseEntity<FoodEntry> createFoodEntry(@RequestBody FoodEntry foodEntry, Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(nutritionService.createFoodEntry(email, foodEntry));
    }
    
    @PutMapping("/entries/{id}")
    public ResponseEntity<FoodEntry> updateFoodEntry(
            @PathVariable Long id,
            @RequestBody FoodEntry foodEntry,
            Authentication auth) {
        String email = auth.getName();
        try {
            return ResponseEntity.ok(nutritionService.updateFoodEntry(email, id, foodEntry));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/entries/{id}")
    public ResponseEntity<Void> deleteFoodEntry(@PathVariable Long id, Authentication auth) {
        String email = auth.getName();
        try {
            nutritionService.deleteFoodEntry(email, id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Nutrition goals endpoints
    @GetMapping("/goals")
    public ResponseEntity<NutritionGoal> getUserNutritionGoals(Authentication auth) {
        String email = auth.getName();
        return nutritionService.getUserNutritionGoals(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/goals")
    public ResponseEntity<NutritionGoal> createOrUpdateNutritionGoals(
            @RequestBody NutritionGoal nutritionGoal,
            Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(nutritionService.createOrUpdateNutritionGoals(email, nutritionGoal));
    }
    
    // Analytics endpoints
    @GetMapping("/analytics/daily")
    public ResponseEntity<Map<String, Object>> getDailyNutritionSummary(
            @RequestParam(required = false) LocalDate date,
            Authentication auth) {
        String email = auth.getName();
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(nutritionService.getDailyNutritionSummary(email, targetDate));
    }
    
    @GetMapping("/analytics/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyNutritionSummary(
            @RequestParam(required = false) LocalDate startDate,
            Authentication auth) {
        String email = auth.getName();
        LocalDate weekStart = startDate != null ? startDate : LocalDate.now().minusDays(6);
        return ResponseEntity.ok(nutritionService.getWeeklyNutritionSummary(email, weekStart));
    }
    
    @GetMapping("/analytics/progress")
    public ResponseEntity<Map<String, Object>> getNutritionProgress(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(nutritionService.getNutritionProgress(email));
    }
    
    // AI-Enhanced Nutrition Endpoints
    @GetMapping("/ai/recommendations")
    public ResponseEntity<Map<String, Object>> getAIEnhancedSummary(
            @RequestParam(required = false) LocalDate date,
            Authentication auth) {
        String email = auth.getName();
        LocalDate targetDate = date != null ? date : LocalDate.now();
        
        // Get regular nutrition summary
        Map<String, Object> summary = nutritionService.getDailyNutritionSummary(email, targetDate);
        
        // Add AI recommendations
        var aiRecommendations = aiService.getUserRecommendations(email);
        summary.put("aiRecommendations", aiRecommendations);
        
        return ResponseEntity.ok(summary);
    }
    
    @PostMapping("/ai/ask-nutrition")
    public ResponseEntity<Map<String, Object>> askNutritionQuestion(
            @RequestBody NutritionQuestionRequest request,
            Authentication auth) {
        try {
            String email = auth.getName();
            var session = aiService.chatWithAI(email, request.getQuestion(), 
                com.fitlife.entity.AICoachingSession.SessionType.NUTRITION_QUERY);
            
            Map<String, Object> response = Map.of(
                "question", request.getQuestion(),
                "answer", session.getAiResponse(),
                "sessionId", session.getId()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // DTO for nutrition questions
    public static class NutritionQuestionRequest {
        private String question;
        
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
    }
}