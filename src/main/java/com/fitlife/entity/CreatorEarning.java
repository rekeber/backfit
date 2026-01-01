package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "creator_earnings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CreatorEarning {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator creator;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaboration_id")
    private BrandCollaboration collaboration;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private CreatorContent content;
    
    @Column(name = "earning_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EarningType earningType;
    
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "currency")
    @Builder.Default
    private String currency = "USD";
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Column(name = "tax_withheld", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxWithheld = BigDecimal.ZERO;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum EarningType {
        COLLABORATION_FEE,      // Pago por colaboración con marca
        CONTENT_MONETIZATION,   // Monetización de contenido
        AFFILIATE_COMMISSION,   // Comisión por afiliados
        SUBSCRIPTION_REVENUE,   // Ingresos por suscripciones
        TIP_DONATION,          // Propinas/donaciones
        MERCHANDISE_SALE,      // Venta de merchandise
        LIVE_STREAM_REVENUE,   // Ingresos por live streams
        COURSE_SALE,           // Venta de cursos
        CONSULTATION_FEE,      // Honorarios por consultoría
        PERFORMANCE_BONUS,     // Bonus por rendimiento
        REFERRAL_BONUS,        // Bonus por referidos
        PLATFORM_BONUS         // Bonus de la plataforma
    }
    
    public enum PaymentStatus {
        PENDING,        // Pendiente de pago
        PROCESSING,     // Procesando pago
        PAID,          // Pagado
        FAILED,        // Falló el pago
        CANCELLED,     // Cancelado
        REFUNDED,      // Reembolsado
        ON_HOLD        // En espera (por verificación)
    }
    
    // Business methods
    public BigDecimal getNetAmount() {
        return amount.subtract(taxWithheld);
    }
    
    public void markAsPaid(String paymentMethod, String transactionId) {
        this.status = PaymentStatus.PAID;
        this.paymentDate = LocalDateTime.now();
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
    }
    
    public void markAsFailed() {
        this.status = PaymentStatus.FAILED;
    }
    
    public void markAsProcessing() {
        this.status = PaymentStatus.PROCESSING;
    }
    
    public void cancel() {
        if (status == PaymentStatus.PENDING || status == PaymentStatus.PROCESSING) {
            this.status = PaymentStatus.CANCELLED;
        }
    }
    
    public void refund() {
        if (status == PaymentStatus.PAID) {
            this.status = PaymentStatus.REFUNDED;
        }
    }
    
    public void putOnHold() {
        if (status == PaymentStatus.PENDING) {
            this.status = PaymentStatus.ON_HOLD;
        }
    }
    
    public boolean canBePaid() {
        return status == PaymentStatus.PENDING && amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isPaid() {
        return status == PaymentStatus.PAID;
    }
    
    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }
    
    public void calculateTax(BigDecimal taxRate) {
        this.taxWithheld = amount.multiply(taxRate).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}