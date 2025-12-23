package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "friend_requests")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"sender", "receiver"})
@ToString(exclude = {"sender", "receiver"})
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
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum Status {
        PENDING, ACCEPTED, REJECTED
    }
    
    // Métodos de negocio
    public void accept() {
        this.status = Status.ACCEPTED;
        // Agregar como amigos mutuamente
        sender.addFriend(receiver);
    }
    
    public void reject() {
        this.status = Status.REJECTED;
    }
    
    public boolean isPending() {
        return this.status == Status.PENDING;
    }
    
    public boolean isAccepted() {
        return this.status == Status.ACCEPTED;
    }
    
    public boolean isRejected() {
        return this.status == Status.REJECTED;
    }
}