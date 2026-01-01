package com.fitlife.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleVisionService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${ai.google.api-key:}")
    private String apiKey;
    
    @Value("${ai.google.project-id:}")
    private String projectId;
    
    public FoodDetectionResult analyzeFood(byte[] imageData) {
        try {
            if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-api-key")) {
                log.warn("Google Vision API key not configured, returning mock response");
                return generateMockFoodDetection();
            }
            
            // TODO: Implement actual Google Vision API call
            // For now, return mock data
            return generateMockFoodDetection();
            
        } catch (Exception e) {
            log.error("Error analyzing food image with Google Vision", e);
            return FoodDetectionResult.builder()
                .success(false)
                .errorMessage("Error al analizar la imagen")
                .build();
        }
    }
    
    public FoodDetectionResult analyzeFood(String imageUrl) {
        try {
            if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-api-key")) {
                log.warn("Google Vision API key not configured, returning mock response");
                return generateMockFoodDetection();
            }
            
            // TODO: Implement actual Google Vision API call with image URL
            // For now, return mock data
            return generateMockFoodDetection();
            
        } catch (Exception e) {
            log.error("Error analyzing food image from URL with Google Vision", e);
            return FoodDetectionResult.builder()
                .success(false)
                .errorMessage("Error al analizar la imagen desde URL")
                .build();
        }
    }
    
    private FoodDetectionResult generateMockFoodDetection() {
        // Mock food detection for development
        List<DetectedFood> detectedFoods = Arrays.asList(
            DetectedFood.builder()
                .name("Pollo a la plancha")
                .confidence(0.92)
                .estimatedWeight(150.0)
                .calories(165)
                .protein(31.0)
                .carbs(0.0)
                .fat(3.6)
                .build(),
            DetectedFood.builder()
                .name("Arroz blanco")
                .confidence(0.88)
                .estimatedWeight(100.0)
                .calories(130)
                .protein(2.7)
                .carbs(28.0)
                .fat(0.3)
                .build(),
            DetectedFood.builder()
                .name("Brócoli")
                .confidence(0.85)
                .estimatedWeight(80.0)
                .calories(27)
                .protein(3.0)
                .carbs(5.1)
                .fat(0.4)
                .build()
        );
        
        return FoodDetectionResult.builder()
            .success(true)
            .detectedFoods(detectedFoods)
            .overallConfidence(0.88)
            .totalCalories(322)
            .totalProtein(36.7)
            .totalCarbs(33.1)
            .totalFat(4.3)
            .analysis("Comida balanceada con buena proporción de proteínas, carbohidratos y vegetales. Excelente opción para pérdida de peso.")
            .build();
    }
    
    public static class FoodDetectionResult {
        private boolean success;
        private List<DetectedFood> detectedFoods;
        private double overallConfidence;
        private int totalCalories;
        private double totalProtein;
        private double totalCarbs;
        private double totalFat;
        private String analysis;
        private String errorMessage;
        
        public static FoodDetectionResultBuilder builder() {
            return new FoodDetectionResultBuilder();
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public List<DetectedFood> getDetectedFoods() { return detectedFoods; }
        public double getOverallConfidence() { return overallConfidence; }
        public int getTotalCalories() { return totalCalories; }
        public double getTotalProtein() { return totalProtein; }
        public double getTotalCarbs() { return totalCarbs; }
        public double getTotalFat() { return totalFat; }
        public String getAnalysis() { return analysis; }
        public String getErrorMessage() { return errorMessage; }
        
        public static class FoodDetectionResultBuilder {
            private boolean success;
            private List<DetectedFood> detectedFoods;
            private double overallConfidence;
            private int totalCalories;
            private double totalProtein;
            private double totalCarbs;
            private double totalFat;
            private String analysis;
            private String errorMessage;
            
            public FoodDetectionResultBuilder success(boolean success) {
                this.success = success;
                return this;
            }
            
            public FoodDetectionResultBuilder detectedFoods(List<DetectedFood> detectedFoods) {
                this.detectedFoods = detectedFoods;
                return this;
            }
            
            public FoodDetectionResultBuilder overallConfidence(double overallConfidence) {
                this.overallConfidence = overallConfidence;
                return this;
            }
            
            public FoodDetectionResultBuilder totalCalories(int totalCalories) {
                this.totalCalories = totalCalories;
                return this;
            }
            
            public FoodDetectionResultBuilder totalProtein(double totalProtein) {
                this.totalProtein = totalProtein;
                return this;
            }
            
            public FoodDetectionResultBuilder totalCarbs(double totalCarbs) {
                this.totalCarbs = totalCarbs;
                return this;
            }
            
            public FoodDetectionResultBuilder totalFat(double totalFat) {
                this.totalFat = totalFat;
                return this;
            }
            
            public FoodDetectionResultBuilder analysis(String analysis) {
                this.analysis = analysis;
                return this;
            }
            
            public FoodDetectionResultBuilder errorMessage(String errorMessage) {
                this.errorMessage = errorMessage;
                return this;
            }
            
            public FoodDetectionResult build() {
                FoodDetectionResult result = new FoodDetectionResult();
                result.success = this.success;
                result.detectedFoods = this.detectedFoods;
                result.overallConfidence = this.overallConfidence;
                result.totalCalories = this.totalCalories;
                result.totalProtein = this.totalProtein;
                result.totalCarbs = this.totalCarbs;
                result.totalFat = this.totalFat;
                result.analysis = this.analysis;
                result.errorMessage = this.errorMessage;
                return result;
            }
        }
    }
    
    public static class DetectedFood {
        private String name;
        private double confidence;
        private double estimatedWeight;
        private int calories;
        private double protein;
        private double carbs;
        private double fat;
        
        public static DetectedFoodBuilder builder() {
            return new DetectedFoodBuilder();
        }
        
        // Getters
        public String getName() { return name; }
        public double getConfidence() { return confidence; }
        public double getEstimatedWeight() { return estimatedWeight; }
        public int getCalories() { return calories; }
        public double getProtein() { return protein; }
        public double getCarbs() { return carbs; }
        public double getFat() { return fat; }
        
        public static class DetectedFoodBuilder {
            private String name;
            private double confidence;
            private double estimatedWeight;
            private int calories;
            private double protein;
            private double carbs;
            private double fat;
            
            public DetectedFoodBuilder name(String name) {
                this.name = name;
                return this;
            }
            
            public DetectedFoodBuilder confidence(double confidence) {
                this.confidence = confidence;
                return this;
            }
            
            public DetectedFoodBuilder estimatedWeight(double estimatedWeight) {
                this.estimatedWeight = estimatedWeight;
                return this;
            }
            
            public DetectedFoodBuilder calories(int calories) {
                this.calories = calories;
                return this;
            }
            
            public DetectedFoodBuilder protein(double protein) {
                this.protein = protein;
                return this;
            }
            
            public DetectedFoodBuilder carbs(double carbs) {
                this.carbs = carbs;
                return this;
            }
            
            public DetectedFoodBuilder fat(double fat) {
                this.fat = fat;
                return this;
            }
            
            public DetectedFood build() {
                DetectedFood food = new DetectedFood();
                food.name = this.name;
                food.confidence = this.confidence;
                food.estimatedWeight = this.estimatedWeight;
                food.calories = this.calories;
                food.protein = this.protein;
                food.carbs = this.carbs;
                food.fat = this.fat;
                return food;
            }
        }
    }
}