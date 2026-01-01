package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "friend_requests")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;
    
    @Column(length = 500)
    private String message;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    public enum Status {
        PENDING, ACCEPTED, REJECTED, CANCELLED
    }
    
    // Métodos de negocio
    public void accept() {
        this.status = Status.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
        
        // Agregar como amigos
        sender.addFriend(receiver);
    }
    
    public void reject() {
        this.status = Status.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        this.status = Status.CANCELLED;
        this.respondedAt = LocalDateTime.now();
    }
}