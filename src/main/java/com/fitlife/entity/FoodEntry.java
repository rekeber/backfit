package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "food_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;
    
    @Column(nullable = false)
    private Double quantity; // en gramos
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealType mealType;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private Double totalCalories;
    
    @Column(nullable = false)
    private Double totalProtein;
    
    @Column(nullable = false)
    private Double totalCarbs;
    
    @Column(nullable = false)
    private Double totalFat;
    
    @Column(nullable = false)
    private Double totalFiber;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_analysis_id")
    private FoodImageAnalysis imageAnalysis;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum MealType {
        DESAYUNO, ALMUERZO, CENA, SNACK
    }
    
    @PrePersist
    public void calculateTotals() {
        if (food != null && quantity != null) {
            this.totalCalories = food.getCaloriesForQuantity(quantity);
            this.totalProtein = food.getProteinForQuantity(quantity);
            this.totalCarbs = food.getCarbsForQuantity(quantity);
            this.totalFat = food.getFatForQuantity(quantity);
            this.totalFiber = (food.getFiberPer100g() * quantity) / 100.0;
        }
    }
}