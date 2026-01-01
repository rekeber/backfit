package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "brand_collaborations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BrandCollaboration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator creator;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private CreatorContent content; // Contenido resultado de la colaboración
    
    @Column(name = "collaboration_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CollaborationType collaborationType;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CollaborationStatus status = CollaborationStatus.PROPOSED;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements; // JSON con requerimientos específicos
    
    @Column(name = "deliverables", columnDefinition = "TEXT")
    private String deliverables; // JSON con entregables esperados
    
    @Column(name = "budget", precision = 10, scale = 2, nullable = false)
    private BigDecimal budget;
    
    @Column(name = "creator_fee", precision = 10, scale = 2)
    private BigDecimal creatorFee; // Lo que recibe el creator
    
    @Column(name = "platform_fee", precision = 10, scale = 2)
    private BigDecimal platformFee; // Comisión de FitLife
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "deadline")
    private LocalDateTime deadline;
    
    @Column(name = "target_metrics", columnDefinition = "TEXT")
    private String targetMetrics; // JSON con métricas objetivo
    
    @Column(name = "actual_metrics", columnDefinition = "TEXT")
    private String actualMetrics; // JSON con métricas reales
    
    @Column(name = "brand_rating")
    private Integer brandRating; // 1-5 rating del creator hacia la marca
    
    @Column(name = "creator_rating")
    private Integer creatorRating; // 1-5 rating de la marca hacia el creator
    
    @Column(name = "brand_feedback", columnDefinition = "TEXT")
    private String brandFeedback;
    
    @Column(name = "creator_feedback", columnDefinition = "TEXT")
    private String creatorFeedback;
    
    @Column(name = "contract_terms", columnDefinition = "TEXT")
    private String contractTerms; // JSON con términos del contrato
    
    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(name = "ai_match_score", precision = 5, scale = 2)
    private BigDecimal aiMatchScore; // Score de compatibilidad IA
    
    @Column(name = "performance_bonus", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal performanceBonus = BigDecimal.ZERO;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum CollaborationType {
        SPONSORED_POST,
        PRODUCT_REVIEW,
        BRAND_AMBASSADOR,
        AFFILIATE_MARKETING,
        EVENT_PROMOTION,
        RECIPE_CREATION,
        WORKOUT_CREATION,
        LIVE_STREAM,
        STORY_MENTION,
        LONG_TERM_PARTNERSHIP
    }
    
    public enum CollaborationStatus {
        PROPOSED,      // Marca propone colaboración
        NEGOTIATING,   // En negociación
        ACCEPTED,      // Creator acepta
        IN_PROGRESS,   // En desarrollo
        SUBMITTED,     // Creator entrega contenido
        REVIEWING,     // Marca revisa contenido
        APPROVED,      // Marca aprueba contenido
        PUBLISHED,     // Contenido publicado
        COMPLETED,     // Colaboración completada
        CANCELLED,     // Cancelada por cualquier parte
        DISPUTED       // En disputa
    }
    
    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        PAID,
        FAILED,
        REFUNDED,
        DISPUTED
    }
    
    // Business methods
    public void calculateFees() {
        // FitLife toma 15% de comisión
        BigDecimal platformFeeRate = new BigDecimal("0.15");
        this.platformFee = budget.multiply(platformFeeRate);
        this.creatorFee = budget.subtract(platformFee);
    }
    
    public boolean isOverdue() {
        return deadline != null && 
               LocalDateTime.now().isAfter(deadline) && 
               status != CollaborationStatus.COMPLETED &&
               status != CollaborationStatus.CANCELLED;
    }
    
    public boolean canBePaid() {
        return status == CollaborationStatus.COMPLETED &&
               paymentStatus == PaymentStatus.PENDING &&
               creatorRating != null && creatorRating >= 3;
    }
    
    public BigDecimal getTotalPayout() {
        return creatorFee.add(performanceBonus);
    }
    
    public void completeCollaboration(Integer brandRating, Integer creatorRating) {
        this.status = CollaborationStatus.COMPLETED;
        this.brandRating = brandRating;
        this.creatorRating = creatorRating;
        
        // Bonus por performance excepcional
        if (creatorRating != null && creatorRating == 5) {
            this.performanceBonus = creatorFee.multiply(new BigDecimal("0.10")); // 10% bonus
        }
    }
}