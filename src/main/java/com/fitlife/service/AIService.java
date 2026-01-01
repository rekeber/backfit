package com.fitlife.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.entity.*;
import com.fitlife.repository.*;
import com.fitlife.service.ai.GoogleVisionService;
import com.fitlife.service.ai.OpenAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AIService {
    
    private final OpenAIService openAIService;
    private final GoogleVisionService googleVisionService;
    private final FoodImageAnalysisRepository foodImageAnalysisRepository;
    private final AIRecommendationRepository aiRecommendationRepository;
    private final AICoachingSessionRepository aiCoachingSessionRepository;
    private final UserRepository userRepository;
    private final NutritionGoalRepository nutritionGoalRepository;
    private final FoodEntryRepository foodEntryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Food Image Analysis
    public FoodImageAnalysis analyzeFoodImage(String userEmail, MultipartFile imageFile) {
        try {
            User user = getUserByEmail(userEmail);
            
            // Create initial analysis record
            FoodImageAnalysis analysis = FoodImageAnalysis.builder()
                .user(user)
                .originalFilename(imageFile.getOriginalFilename())
                .imageUrl("temp-url") // TODO: Implement file storage
                .processingStatus(FoodImageAnalysis.ProcessingStatus.PROCESSING)
                .build();
            
            analysis = foodImageAnalysisRepository.save(analysis);
            
            // Analyze image with Google Vision
            GoogleVisionService.FoodDetectionResult result = googleVisionService.analyzeFood(imageFile.getBytes());
            
            if (result.isSuccess()) {
                // Update analysis with results
                analysis.setDetectedFoods(objectMapper.writeValueAsString(result.getDetectedFoods()));
                analysis.setConfidenceScore(result.getOverallConfidence());
                analysis.setEstimatedCalories(result.getTotalCalories());
                analysis.setEstimatedProtein(result.getTotalProtein());
                analysis.setEstimatedCarbs(result.getTotalCarbs());
                analysis.setEstimatedFat(result.getTotalFat());
                analysis.setAiAnalysis(result.getAnalysis());
                analysis.setProcessingStatus(FoodImageAnalysis.ProcessingStatus.COMPLETED);
                
                // Generate AI recommendations based on the analysis
                generateFoodRecommendations(user, result);
                
            } else {
                analysis.setProcessingStatus(FoodImageAnalysis.ProcessingStatus.FAILED);
                analysis.setErrorMessage(result.getErrorMessage());
            }
            
            return foodImageAnalysisRepository.save(analysis);
            
        } catch (Exception e) {
            log.error("Error analyzing food image for user: " + userEmail, e);
            throw new RuntimeException("Error al analizar la imagen de comida", e);
        }
    }
    
    // AI Coaching Chat
    public AICoachingSession chatWithAI(String userEmail, String message, AICoachingSession.SessionType sessionType) {
        try {
            User user = getUserByEmail(userEmail);
            
            // Build user context
            String userContext = buildUserContext(user);
            
            // Get AI response
            String aiResponse = openAIService.generateNutritionAdvice(userContext, message);
            
            // Create coaching session
            AICoachingSession session = AICoachingSession.builder()
                .user(user)
                .sessionType(sessionType)
                .userMessage(message)
                .aiResponse(aiResponse)
                .contextData(userContext)
                .build();
            
            session = aiCoachingSessionRepository.save(session);
            
            // Generate recommendations if applicable
            if (sessionType == AICoachingSession.SessionType.NUTRITION_QUERY || 
                sessionType == AICoachingSession.SessionType.MEAL_PLANNING) {
                generatePersonalizedRecommendations(user, message, aiResponse);
            }
            
            return session;
            
        } catch (Exception e) {
            log.error("Error in AI coaching session for user: " + userEmail, e);
            throw new RuntimeException("Error en la sesión de coaching con IA", e);
        }
    }
    
    // Generate Meal Recommendations
    public List<AIRecommendation> generateMealRecommendations(String userEmail) {
        try {
            User user = getUserByEmail(userEmail);
            String userProfile = buildUserProfile(user);
            String preferences = getUserPreferences(user);
            String restrictions = getUserRestrictions(user);
            
            String recommendations = openAIService.generateMealRecommendations(userProfile, preferences, restrictions);
            
            AIRecommendation recommendation = AIRecommendation.builder()
                .user(user)
                .type(AIRecommendation.RecommendationType.NUTRITION_MEAL)
                .title("Recomendaciones de Comidas Personalizadas")
                .content(recommendations)
                .priority(AIRecommendation.Priority.MEDIUM)
                .confidenceScore(0.85)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
            
            aiRecommendationRepository.save(recommendation);
            return List.of(recommendation);
            
        } catch (Exception e) {
            log.error("Error generating meal recommendations for user: " + userEmail, e);
            throw new RuntimeException("Error al generar recomendaciones de comidas", e);
        }
    }
    
    // Generate Daily Motivational Message
    public AIRecommendation generateMotivationalMessage(String userEmail) {
        try {
            User user = getUserByEmail(userEmail);
            String userProgress = getUserProgress(user);
            String challenges = getUserChallenges(user);
            
            String motivationalMessage = openAIService.generateMotivationalMessage(userProgress, challenges);
            
            AIRecommendation recommendation = AIRecommendation.builder()
                .user(user)
                .type(AIRecommendation.RecommendationType.WELLNESS_MOTIVATION)
                .title("Mensaje Motivacional del Día")
                .content(motivationalMessage)
                .priority(AIRecommendation.Priority.HIGH)
                .confidenceScore(0.90)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
            
            return aiRecommendationRepository.save(recommendation);
            
        } catch (Exception e) {
            log.error("Error generating motivational message for user: " + userEmail, e);
            throw new RuntimeException("Error al generar mensaje motivacional", e);
        }
    }
    
    // Get User Recommendations
    public List<AIRecommendation> getUserRecommendations(String userEmail) {
        User user = getUserByEmail(userEmail);
        return aiRecommendationRepository.findActiveRecommendations(user, LocalDateTime.now());
    }
    
    public Page<AIRecommendation> getUserRecommendations(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        return aiRecommendationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    // Mark recommendation as read
    public AIRecommendation markRecommendationAsRead(String userEmail, Long recommendationId) {
        User user = getUserByEmail(userEmail);
        AIRecommendation recommendation = aiRecommendationRepository.findById(recommendationId)
            .orElseThrow(() -> new RuntimeException("Recomendación no encontrada"));
        
        if (!recommendation.getUser().equals(user)) {
            throw new RuntimeException("No autorizado para acceder a esta recomendación");
        }
        
        recommendation.setIsRead(true);
        return aiRecommendationRepository.save(recommendation);
    }
    
    // Get AI Analytics
    public Map<String, Object> getAIAnalytics(String userEmail) {
        User user = getUserByEmail(userEmail);
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Image analysis stats
        Long totalAnalyses = foodImageAnalysisRepository.countByUserAndStatus(user, FoodImageAnalysis.ProcessingStatus.COMPLETED);
        Double avgConfidence = foodImageAnalysisRepository.getAverageConfidenceScoreByUser(user);
        
        // Coaching session stats
        Long totalSessions = (long) aiCoachingSessionRepository.findByUserOrderByCreatedAtDesc(user).size();
        Double avgSatisfaction = aiCoachingSessionRepository.getAverageSatisfactionByUser(user);
        
        // Recommendation stats
        Long unreadRecommendations = aiRecommendationRepository.countUnreadByUser(user);
        
        analytics.put("totalImageAnalyses", totalAnalyses);
        analytics.put("averageConfidenceScore", avgConfidence != null ? avgConfidence : 0.0);
        analytics.put("totalCoachingSessions", totalSessions);
        analytics.put("averageSatisfaction", avgSatisfaction != null ? avgSatisfaction : 0.0);
        analytics.put("unreadRecommendations", unreadRecommendations);
        
        return analytics;
    }
    
    // Helper methods
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }
    
    private String buildUserContext(User user) {
        try {
            Map<String, Object> context = new HashMap<>();
            context.put("name", user.getName());
            context.put("age", user.getAge());
            context.put("currentWeight", user.getCurrentWeight());
            context.put("targetWeight", user.getTargetWeight());
            context.put("height", user.getHeight());
            context.put("activityLevel", user.getActivityLevel());
            context.put("goal", user.getGoal());
            
            // Add nutrition goals if available
            Optional<NutritionGoal> nutritionGoal = nutritionGoalRepository.findByUser(user);
            nutritionGoal.ifPresent(goal -> {
                context.put("dailyCalories", goal.getDailyCalories());
                context.put("nutritionGoal", goal.getGoal());
            });
            
            return objectMapper.writeValueAsString(context);
        } catch (Exception e) {
            log.error("Error building user context", e);
            return "{}";
        }
    }
    
    private String buildUserProfile(User user) {
        return String.format("Usuario: %s, %d años, %.1f kg (actual), %.1f kg (objetivo), %.1f cm, Nivel de actividad: %s, Objetivo: %s",
            user.getName(), user.getAge(), user.getCurrentWeight(), user.getTargetWeight(), user.getHeight(), 
            user.getActivityLevel(), user.getGoal());
    }
    
    private String getUserPreferences(User user) {
        // TODO: Implement user preferences system
        return "Prefiere comidas balanceadas, le gustan los vegetales y proteínas magras";
    }
    
    private String getUserRestrictions(User user) {
        // TODO: Implement user dietary restrictions system
        return "Sin restricciones dietéticas específicas";
    }
    
    private String getUserProgress(User user) {
        // TODO: Implement progress tracking
        return String.format("Peso actual: %.1f kg, Peso objetivo: %.1f kg, Puntos totales: %d", 
            user.getCurrentWeight(), user.getTargetWeight(), user.getTotalPoints());
    }
    
    private String getUserChallenges(User user) {
        // TODO: Implement challenge detection based on user behavior
        return "Mantener consistencia en la alimentación saludable";
    }
    
    private void generateFoodRecommendations(User user, GoogleVisionService.FoodDetectionResult result) {
        try {
            String userGoals = buildUserProfile(user);
            String analysis = openAIService.analyzeFoodImage(result.getAnalysis(), userGoals);
            
            AIRecommendation recommendation = AIRecommendation.builder()
                .user(user)
                .type(AIRecommendation.RecommendationType.NUTRITION_MEAL)
                .title("Análisis de tu Comida")
                .content(analysis)
                .priority(AIRecommendation.Priority.MEDIUM)
                .confidenceScore(result.getOverallConfidence())
                .expiresAt(LocalDateTime.now().plusHours(6))
                .build();
            
            aiRecommendationRepository.save(recommendation);
        } catch (Exception e) {
            log.error("Error generating food recommendations", e);
        }
    }
    
    private void generatePersonalizedRecommendations(User user, String userMessage, String aiResponse) {
        try {
            AIRecommendation recommendation = AIRecommendation.builder()
                .user(user)
                .type(AIRecommendation.RecommendationType.GENERAL_TIP)
                .title("Consejo Personalizado")
                .content(aiResponse)
                .priority(AIRecommendation.Priority.MEDIUM)
                .confidenceScore(0.80)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
            
            aiRecommendationRepository.save(recommendation);
        } catch (Exception e) {
            log.error("Error generating personalized recommendations", e);
        }
    }
}