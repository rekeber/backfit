package com.fitlife.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    @NotBlank(message = "Nombre del logro es obligatorio")
    @Size(max = 100, message = "Nombre no puede exceder 100 caracteres")
    private String name;
    
    @Column(length = 500)
    @Size(max = 500, message = "Descripción no puede exceder 500 caracteres")
    private String description;
    
    @Column(name = "icon_url")
    private String iconUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Category category = Category.GENERAL;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Difficulty difficulty = Difficulty.EASY;
    
    @Column(nullable = false)
    @Min(value = 1, message = "Los puntos deben ser al menos 1")
    @Max(value = 1000, message = "Los puntos no pueden exceder 1000")
    @Builder.Default
    private Integer points = 10;
    
    @Column(name = "target_value")
    private Double targetValue; // Valor objetivo para logros cuantificables
    
    @Column(name = "target_unit")
    private String targetUnit; // Unidad del valor objetivo (kg, días, etc.)
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "is_repeatable", nullable = false)
    @Builder.Default
    private Boolean isRepeatable = false;
    
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
    
    // Auditoría
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enums
    public enum Category {
        GENERAL, FITNESS, NUTRITION, SOCIAL, STREAK, WEIGHT_LOSS, MUSCLE_GAIN
    }
    
    public enum Difficulty {
        EASY, MEDIUM, HARD, LEGENDARY
    }
    
    // Métodos de negocio
    public String getDisplayName() {
        return name + " (" + points + " pts)";
    }
    
    public boolean isQuantifiable() {
        return targetValue != null && targetValue > 0;
    }
    
    public String getTitle() {
        return name;
    }
    
    public Double getRequiredValue() {
        return targetValue != null ? targetValue : 1.0;
    }
}