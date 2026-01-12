package com.fitlife.service;

import com.fitlife.entity.User;
import com.fitlife.dto.NutritionPlanDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio avanzado para cálculo de necesidades nutricionales
 * Basado en ciencia deportiva y fórmulas validadas científicamente
 */
@Service
@Slf4j
public class NutritionCalculatorService {

    /**
     * Calcula el plan nutricional completo para un usuario
     */
    public NutritionPlanDTO calculateNutritionPlan(User user) {
        log.info("Calculando plan nutricional para usuario: {}", user.getEmail());
        
        // 1. Calcular BMR (Basal Metabolic Rate)
        double bmr = calculateBMR(user);
        
        // 2. Calcular TDEE (Total Daily Energy Expenditure)
        double tdee = calculateTDEE(bmr, user.getActivityLevel());
        
        // 3. Ajustar calorías según objetivo
        double targetCalories = adjustCaloriesForGoal(tdee, user.getGoal(), user);
        
        // 4. Calcular macronutrientes
        MacronutrientBreakdown macros = calculateMacronutrients(targetCalories, user.getGoal(), user);
        
        // 5. Calcular hidratación
        double waterIntake = calculateWaterIntake(user);
        
        NutritionPlanDTO plan = NutritionPlanDTO.builder()
            .bmr(round(bmr))
            .tdee(round(tdee))
            .targetCalories(round(targetCalories))
            .protein(round(macros.protein))
            .carbs(round(macros.carbs))
            .fat(round(macros.fat))
            .fiber(round(macros.fiber))
            .waterIntake(round(waterIntake))
            .mealsPerDay(calculateOptimalMeals(user.getGoal()))
            .calorieDistribution(calculateMealDistribution(user.getGoal()))
            .build();
            
        log.info("Plan nutricional calculado: BMR={}, TDEE={}, Target={} cal", 
                 plan.getBmr(), plan.getTdee(), plan.getTargetCalories());
                 
        return plan;
    }

    /**
     * Calcula BMR usando múltiples fórmulas y promedia para mayor precisión
     */
    private double calculateBMR(User user) {
        if (user.getCurrentWeight() == null || user.getHeight() == null || user.getAge() == null) {
            return 0.0;
        }
        
        double weight = user.getCurrentWeight();
        double height = user.getHeight();
        int age = user.getAge();
        
        // Asumimos masculino por defecto (se puede agregar campo género después)
        boolean isMale = true; // TODO: Agregar campo género a User
        
        // Fórmula Mifflin-St Jeor (más precisa para población general)
        double mifflinBMR = isMale ? 
            (10 * weight) + (6.25 * height) - (5 * age) + 5 :
            (10 * weight) + (6.25 * height) - (5 * age) - 161;
            
        // Fórmula Harris-Benedict revisada
        double harrisBMR = isMale ?
            88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age) :
            447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
            
        // Promedio de ambas fórmulas para mayor precisión
        double averageBMR = (mifflinBMR + harrisBMR) / 2;
        
        log.debug("BMR calculado: Mifflin={}, Harris={}, Promedio={}", 
                  mifflinBMR, harrisBMR, averageBMR);
                  
        return averageBMR;
    }

    /**
     * Calcula TDEE basado en nivel de actividad
     */
    private double calculateTDEE(double bmr, User.ActivityLevel activityLevel) {
        double activityFactor = switch (activityLevel) {
            case SEDENTARIO -> 1.2;      // Trabajo de oficina, sin ejercicio
            case LIGERO -> 1.375;        // Ejercicio ligero 1-3 días/semana
            case MODERADO -> 1.55;       // Ejercicio moderado 3-5 días/semana
            case ACTIVO -> 1.725;        // Ejercicio intenso 6-7 días/semana
            case MUY_ACTIVO -> 1.9;      // Ejercicio muy intenso, trabajo físico
        };
        
        return bmr * activityFactor;
    }

    /**
     * Ajusta calorías según objetivo específico del usuario
     */
    private double adjustCaloriesForGoal(double tdee, User.Goal goal, User user) {
        return switch (goal) {
            case PERDER_PESO -> calculateWeightLossCalories(tdee, user);
            case GANAR_MUSCULO -> calculateMuscleGainCalories(tdee, user);
            case MANTENER -> tdee; // Mantenimiento
        };
    }

    /**
     * Cálculo específico para pérdida de peso
     */
    private double calculateWeightLossCalories(double tdee, User user) {
        // Déficit agresivo pero seguro basado en peso actual
        double currentWeight = user.getCurrentWeight();
        double targetWeight = user.getTargetWeight();
        
        // Déficit recomendado: 0.5-1kg por semana (3500-7000 cal/semana)
        double weeklyDeficit = Math.min(7000, currentWeight * 50); // Max 50 cal/kg peso
        double dailyDeficit = weeklyDeficit / 7;
        
        // No bajar de 1200 cal para mujeres o 1500 para hombres (asumimos hombre)
        double minCalories = 1500;
        
        return Math.max(minCalories, tdee - dailyDeficit);
    }

    /**
     * Cálculo específico para ganancia muscular
     */
    private double calculateMuscleGainCalories(double tdee, User user) {
        // Superávit moderado para minimizar ganancia de grasa
        double surplus = switch (user.getActivityLevel()) {
            case SEDENTARIO, LIGERO -> 200;      // Superávit conservador
            case MODERADO -> 300;                // Superávit moderado
            case ACTIVO, MUY_ACTIVO -> 400;      // Superávit más agresivo
        };
        
        return tdee + surplus;
    }

    /**
     * Calcula distribución de macronutrientes según objetivo
     */
    private MacronutrientBreakdown calculateMacronutrients(double calories, User.Goal goal, User user) {
        double weight = user.getCurrentWeight();
        
        return switch (goal) {
            case PERDER_PESO -> calculateWeightLossMacros(calories, weight);
            case GANAR_MUSCULO -> calculateMuscleGainMacros(calories, weight);
            case MANTENER -> calculateMaintenanceMacros(calories, weight);
        };
    }

    private MacronutrientBreakdown calculateWeightLossMacros(double calories, double weight) {
        // Proteína alta para preservar músculo: 2.2-2.5g/kg
        double protein = weight * 2.3 * 4; // gramos * 4 cal/g
        
        // Grasa: 25-30% de calorías totales
        double fat = calories * 0.275 * 0.25; // 27.5% * 0.25 (fat has 9 cal/g, not 4)
        
        // Carbohidratos: el resto
        double carbs = calories - protein - (fat * 9 / 4); // Ajustar por diferencia calórica
        
        return new MacronutrientBreakdown(
            protein / 4,    // gramos de proteína
            carbs / 4,      // gramos de carbohidratos  
            fat * 9 / 4 / 9, // gramos de grasa
            weight * 0.035  // fibra: 35g por cada 1000 cal aprox
        );
    }

    private MacronutrientBreakdown calculateMuscleGainMacros(double calories, double weight) {
        // Proteína para construcción muscular: 2.0-2.2g/kg
        double protein = weight * 2.1 * 4;
        
        // Grasa: 20-25% de calorías
        double fat = calories * 0.225;
        
        // Carbohidratos altos para energía: el resto
        double carbs = calories - protein - fat;
        
        return new MacronutrientBreakdown(
            protein / 4,
            carbs / 4,
            fat / 9,
            calories * 0.014 // 14g fibra por 1000 cal
        );
    }

    private MacronutrientBreakdown calculateMaintenanceMacros(double calories, double weight) {
        // Distribución balanceada
        double protein = weight * 1.8 * 4; // 1.8g/kg
        double fat = calories * 0.25;      // 25%
        double carbs = calories - protein - fat;
        
        return new MacronutrientBreakdown(
            protein / 4,
            carbs / 4,
            fat / 9,
            calories * 0.012 // 12g fibra por 1000 cal
        );
    }

    /**
     * Calcula necesidades de hidratación
     */
    private double calculateWaterIntake(User user) {
        double baseWater = user.getCurrentWeight() * 35; // 35ml por kg
        
        // Ajustar por actividad
        double activityBonus = switch (user.getActivityLevel()) {
            case SEDENTARIO -> 0;
            case LIGERO -> 250;
            case MODERADO -> 500;
            case ACTIVO -> 750;
            case MUY_ACTIVO -> 1000;
        };
        
        return (baseWater + activityBonus) / 250; // Convertir a vasos de 250ml
    }

    /**
     * Calcula número óptimo de comidas
     */
    private int calculateOptimalMeals(User.Goal goal) {
        return switch (goal) {
            case PERDER_PESO -> 4;      // Más comidas para controlar hambre
            case GANAR_MUSCULO -> 5;    // Más comidas para maximizar síntesis proteica
            case MANTENER -> 3;         // Comidas tradicionales
        };
    }

    /**
     * Calcula distribución de calorías por comida
     */
    private String calculateMealDistribution(User.Goal goal) {
        return switch (goal) {
            case PERDER_PESO -> "25% desayuno, 30% almuerzo, 25% cena, 20% snacks";
            case GANAR_MUSCULO -> "20% desayuno, 25% almuerzo, 30% cena, 25% snacks";
            case MANTENER -> "30% desayuno, 40% almuerzo, 30% cena";
        };
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
            .setScale(1, RoundingMode.HALF_UP)
            .doubleValue();
    }

    /**
     * Clase interna para breakdown de macronutrientes
     */
    private static class MacronutrientBreakdown {
        final double protein;
        final double carbs;
        final double fat;
        final double fiber;
        
        MacronutrientBreakdown(double protein, double carbs, double fat, double fiber) {
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
            this.fiber = fiber;
        }
    }
}