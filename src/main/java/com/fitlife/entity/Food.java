package com.fitlife.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "foods")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @NotBlank(message = "Nombre del alimento es obligatorio")
    @Size(max = 200, message = "Nombre no puede exceder 200 caracteres")
    private String name;
    
    @Column(length = 1000)
    @Size(max = 1000, message = "Descripción no puede exceder 1000 caracteres")
    private String description;
    
    @Column(name = "calories_per_100g", nullable = false)
    @DecimalMin(value = "0.0", message = "Las calorías no pueden ser negativas")
    @DecimalMax(value = "1000.0", message = "Las calorías por 100g no pueden exceder 1000")
    private Double caloriesPer100g;
    
    @Column(name = "protein_per_100g", nullable = false)
    @DecimalMin(value = "0.0", message = "Las proteínas no pueden ser negativas")
    @DecimalMax(value = "100.0", message = "Las proteínas por 100g no pueden exceder 100g")
    private Double proteinPer100g;
    
    @Column(name = "carbs_per_100g", nullable = false)
    @DecimalMin(value = "0.0", message = "Los carbohidratos no pueden ser negativos")
    @DecimalMax(value = "100.0", message = "Los carbohidratos por 100g no pueden exceder 100g")
    private Double carbsPer100g;
    
    @Column(name = "fat_per_100g", nullable = false)
    @DecimalMin(value = "0.0", message = "Las grasas no pueden ser negativas")
    @DecimalMax(value = "100.0", message = "Las grasas por 100g no pueden exceder 100g")
    private Double fatPer100g;
    
    @Column(name = "fiber_per_100g", nullable = false)
    @DecimalMin(value = "0.0", message = "La fibra no puede ser negativa")
    @DecimalMax(value = "100.0", message = "La fibra por 100g no puede exceder 100g")
    private Double fiberPer100g;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "food_categories", 
                    joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "category")
    @Builder.Default
    private Set<String> categories = new HashSet<>();
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "food_allergens", 
                    joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "allergen")
    @Builder.Default
    private Set<String> allergens = new HashSet<>();
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "is_healthy", nullable = false)
    @Builder.Default
    private Boolean isHealthy = true;
    
    @Column(name = "glycemic_index")
    @DecimalMin(value = "0.0", message = "El índice glucémico no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El índice glucémico no puede exceder 100")
    private Double glycemicIndex;
    
    @Column(name = "barcode")
    private String barcode;
    
    @Column(name = "brand")
    private String brand;
    
    @Column(name = "serving_size")
    private String servingSize;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    
    @Column(name = "verified_by")
    private Long verifiedBy; // ID del usuario que verificó
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    // Vitaminas y minerales como JSON
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "food_vitamins", 
                    joinColumns = @JoinColumn(name = "food_id"))
    @MapKeyColumn(name = "vitamin_name")
    @Column(name = "amount_per_100g")
    @Builder.Default
    private Set<NutrientInfo> vitamins = new HashSet<>();
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "food_minerals", 
                    joinColumns = @JoinColumn(name = "food_id"))
    @MapKeyColumn(name = "mineral_name")
    @Column(name = "amount_per_100g")
    @Builder.Default
    private Set<NutrientInfo> minerals = new HashSet<>();
    
    // Auditoría
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy; // ID del usuario que creó el alimento
    
    // Enums
    public enum VerificationStatus {
        PENDING, VERIFIED, REJECTED
    }
    
    // Clase embebida para nutrientes
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutrientInfo {
        @Column(name = "nutrient_name")
        private String name;
        
        @Column(name = "amount")
        private Double amount;
        
        @Column(name = "unit")
        private String unit; // mg, mcg, IU, etc.
    }
    
    // Métodos de negocio
    public Double getCaloriesForQuantity(Double grams) {
        if (grams == null || caloriesPer100g == null) return 0.0;
        return (caloriesPer100g * grams) / 100.0;
    }
    
    public Double getProteinForQuantity(Double grams) {
        if (grams == null || proteinPer100g == null) return 0.0;
        return (proteinPer100g * grams) / 100.0;
    }
    
    public Double getCarbsForQuantity(Double grams) {
        if (grams == null || carbsPer100g == null) return 0.0;
        return (carbsPer100g * grams) / 100.0;
    }
    
    public Double getFatForQuantity(Double grams) {
        if (grams == null || fatPer100g == null) return 0.0;
        return (fatPer100g * grams) / 100.0;
    }
    
    public boolean hasAllergen(String allergen) {
        return allergens.contains(allergen.toLowerCase());
    }
    
    public boolean isInCategory(String category) {
        return categories.contains(category.toLowerCase());
    }
}