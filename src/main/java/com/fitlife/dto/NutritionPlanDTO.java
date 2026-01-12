package com.fitlife.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para plan nutricional personalizado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionPlanDTO {
    
    // Metabolismo basal
    private Double bmr;
    
    // Gasto energético total diario
    private Double tdee;
    
    // Calorías objetivo
    private Double targetCalories;
    
    // Macronutrientes (en gramos)
    private Double protein;
    private Double carbs;
    private Double fat;
    private Double fiber;
    
    // Hidratación (vasos de agua)
    private Double waterIntake;
    
    // Estructura de comidas
    private Integer mealsPerDay;
    private String calorieDistribution;
    
    // Información adicional
    private String goalDescription;
    private String recommendations;
    
    // Rangos de macronutrientes (para flexibilidad)
    private MacroRange proteinRange;
    private MacroRange carbRange;
    private MacroRange fatRange;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MacroRange {
        private Double min;
        private Double max;
        private Double optimal;
        private String unit; // "g" para gramos, "%" para porcentaje
    }
}