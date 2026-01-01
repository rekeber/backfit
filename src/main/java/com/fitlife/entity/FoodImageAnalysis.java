package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "food_image_analyses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FoodImageAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "image_url", nullable = false)
    private String imageUrl;
    
    @Column(name = "original_filename")
    private String originalFilename;
    
    @Column(name = "detected_foods", columnDefinition = "TEXT")
    private String detectedFoods; // JSON string of detected foods
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "estimated_calories")
    private Integer estimatedCalories;
    
    @Column(name = "estimated_protein")
    private Double estimatedProtein;
    
    @Column(name = "estimated_carbs")
    private Double estimatedCarbs;
    
    @Column(name = "estimated_fat")
    private Double estimatedFat;
    
    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis; // Full AI analysis response
    
    @Column(name = "processing_status")
    @Enumerated(EnumType.STRING)
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @OneToMany(mappedBy = "imageAnalysis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FoodEntry> generatedEntries;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum ProcessingStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}