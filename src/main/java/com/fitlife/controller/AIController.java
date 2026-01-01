package com.fitlife.controller;

import com.fitlife.entity.AICoachingSession;
import com.fitlife.entity.AIRecommendation;
import com.fitlife.entity.FoodImageAnalysis;
import com.fitlife.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AIController {
    
    private final AIService aiService;
    
    // Food Image Analysis
    @PostMapping("/analyze-food-image")
    public ResponseEntity<FoodImageAnalysis> analyzeFoodImage(
            @RequestParam("image") MultipartFile imageFile,
            Authentication auth) {
        try {
            String email = auth.getName();
            FoodImageAnalysis analysis = aiService.analyzeFoodImage(email, imageFile);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // AI Coaching Chat
    @PostMapping("/chat")
    public ResponseEntity<AICoachingSession> chatWithAI(
            @RequestBody ChatRequest request,
            Authentication auth) {
        try {
            String email = auth.getName();
            AICoachingSession session = aiService.chatWithAI(
                email, 
                request.getMessage(), 
                request.getSessionType()
            );
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Generate Meal Recommendations
    @PostMapping("/recommendations/meals")
    public ResponseEntity<List<AIRecommendation>> generateMealRecommendations(Authentication auth) {
        try {
            String email = auth.getName();
            List<AIRecommendation> recommendations = aiService.generateMealRecommendations(email);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Generate Motivational Message
    @PostMapping("/recommendations/motivation")
    public ResponseEntity<AIRecommendation> generateMotivationalMessage(Authentication auth) {
        try {
            String email = auth.getName();
            AIRecommendation message = aiService.generateMotivationalMessage(email);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Get User Recommendations
    @GetMapping("/recommendations")
    public ResponseEntity<List<AIRecommendation>> getUserRecommendations(Authentication auth) {
        String email = auth.getName();
        List<AIRecommendation> recommendations = aiService.getUserRecommendations(email);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/recommendations/paginated")
    public ResponseEntity<Page<AIRecommendation>> getUserRecommendationsPaginated(
            Authentication auth,
            Pageable pageable) {
        String email = auth.getName();
        Page<AIRecommendation> recommendations = aiService.getUserRecommendations(email, pageable);
        return ResponseEntity.ok(recommendations);
    }
    
    // Mark recommendation as read
    @PutMapping("/recommendations/{id}/read")
    public ResponseEntity<AIRecommendation> markRecommendationAsRead(
            @PathVariable Long id,
            Authentication auth) {
        try {
            String email = auth.getName();
            AIRecommendation recommendation = aiService.markRecommendationAsRead(email, id);
            return ResponseEntity.ok(recommendation);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Get AI Analytics
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAIAnalytics(Authentication auth) {
        String email = auth.getName();
        Map<String, Object> analytics = aiService.getAIAnalytics(email);
        return ResponseEntity.ok(analytics);
    }
    
    // Rate coaching session
    @PutMapping("/sessions/{id}/rate")
    public ResponseEntity<Void> rateCoachingSession(
            @PathVariable Long id,
            @RequestBody RatingRequest request,
            Authentication auth) {
        // TODO: Implement session rating
        return ResponseEntity.ok().build();
    }
    
    // DTOs
    public static class ChatRequest {
        private String message;
        private AICoachingSession.SessionType sessionType = AICoachingSession.SessionType.GENERAL_CHAT;
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public AICoachingSession.SessionType getSessionType() { return sessionType; }
        public void setSessionType(AICoachingSession.SessionType sessionType) { this.sessionType = sessionType; }
    }
    
    public static class RatingRequest {
        private Integer rating;
        
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
    }
}