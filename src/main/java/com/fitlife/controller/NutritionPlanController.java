package com.fitlife.controller;

import com.fitlife.dto.NutritionPlanDTO;
import com.fitlife.entity.User;
import com.fitlife.service.NutritionCalculatorService;
import com.fitlife.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para planes nutricionales personalizados
 */
@RestController
@RequestMapping("/nutrition-plan")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@Slf4j
public class NutritionPlanController {
    
    private final NutritionCalculatorService nutritionCalculatorService;
    private final UserService userService;
    
    /**
     * Obtiene el plan nutricional personalizado del usuario
     */
    @GetMapping("/current")
    public ResponseEntity<NutritionPlanDTO> getCurrentNutritionPlan(Authentication auth) {
        log.info("Obteniendo plan nutricional para usuario: {}", auth.getName());
        
        User user = userService.getUserByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
        NutritionPlanDTO plan = nutritionCalculatorService.calculateNutritionPlan(user);
        
        return ResponseEntity.ok(plan);
    }
    
    /**
     * Recalcula el plan nutricional con nuevos parámetros
     */
    @PostMapping("/recalculate")
    public ResponseEntity<NutritionPlanDTO> recalculateNutritionPlan(
            Authentication auth,
            @RequestParam(required = false) Double newWeight,
            @RequestParam(required = false) String newGoal,
            @RequestParam(required = false) String newActivityLevel) {
        
        log.info("Recalculando plan nutricional para usuario: {}", auth.getName());
        
        User user = userService.getUserByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Actualizar parámetros temporalmente para el cálculo
        User tempUser = User.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .age(user.getAge())
            .height(user.getHeight())
            .currentWeight(newWeight != null ? newWeight : user.getCurrentWeight())
            .targetWeight(user.getTargetWeight())
            .goal(newGoal != null ? User.Goal.valueOf(newGoal) : user.getGoal())
            .activityLevel(newActivityLevel != null ? User.ActivityLevel.valueOf(newActivityLevel) : user.getActivityLevel())
            .build();
            
        NutritionPlanDTO plan = nutritionCalculatorService.calculateNutritionPlan(tempUser);
        
        return ResponseEntity.ok(plan);
    }
    
    /**
     * Obtiene recomendaciones específicas según el progreso
     */
    @GetMapping("/recommendations")
    public ResponseEntity<String> getNutritionRecommendations(Authentication auth) {
        log.info("Obteniendo recomendaciones nutricionales para usuario: {}", auth.getName());
        
        User user = userService.getUserByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        String recommendations = generateRecommendations(user);
        
        return ResponseEntity.ok(recommendations);
    }
    
    private String generateRecommendations(User user) {
        StringBuilder recommendations = new StringBuilder();
        
        // Recomendaciones basadas en objetivo
        switch (user.getGoal()) {
            case PERDER_PESO -> {
                recommendations.append("🎯 PÉRDIDA DE PESO:\n");
                recommendations.append("• Mantén un déficit calórico moderado (300-500 cal)\n");
                recommendations.append("• Prioriza proteína para preservar músculo\n");
                recommendations.append("• Come más vegetales para saciedad\n");
                recommendations.append("• Hidrátate bien antes de las comidas\n");
            }
            case GANAR_MUSCULO -> {
                recommendations.append("💪 GANANCIA MUSCULAR:\n");
                recommendations.append("• Mantén superávit calórico moderado (200-400 cal)\n");
                recommendations.append("• Consume 2.0-2.2g proteína por kg de peso\n");
                recommendations.append("• Carbohidratos alrededor del entrenamiento\n");
                recommendations.append("• Duerme 7-9 horas para recuperación\n");
            }
            case MANTENER -> {
                recommendations.append("⚖️ MANTENIMIENTO:\n");
                recommendations.append("• Equilibra calorías consumidas vs gastadas\n");
                recommendations.append("• Distribución balanceada de macronutrientes\n");
                recommendations.append("• Enfócate en alimentos nutritivos\n");
                recommendations.append("• Mantén rutina de ejercicio regular\n");
            }
        }
        
        // Recomendaciones basadas en actividad
        recommendations.append("\n🏃 SEGÚN TU ACTIVIDAD (").append(user.getActivityLevel()).append("):\n");
        switch (user.getActivityLevel()) {
            case SEDENTARIO -> recommendations.append("• Considera agregar caminatas diarias\n• Reduce carbohidratos simples\n");
            case LIGERO -> recommendations.append("• Aumenta gradualmente la actividad\n• Carbohidratos moderados\n");
            case MODERADO -> recommendations.append("• Mantén consistencia en el ejercicio\n• Timing de carbohidratos pre/post entreno\n");
            case ACTIVO, MUY_ACTIVO -> recommendations.append("• Asegura recuperación adecuada\n• Carbohidratos suficientes para energía\n");
        }
        
        return recommendations.toString();
    }
}