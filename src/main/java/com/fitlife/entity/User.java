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
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"friends", "sentFriendRequests", "receivedFriendRequests"})
@ToString(exclude = {"friends", "sentFriendRequests", "receivedFriendRequests"})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @Email(message = "Email debe ser válido")
    @NotBlank(message = "Email es obligatorio")
    private String email;
    
    @Column(nullable = false)
    @NotBlank(message = "Contraseña es obligatoria")
    private String password;
    
    @Column(nullable = false)
    @NotBlank(message = "Nombre es obligatorio")
    @Size(min = 2, max = 100, message = "Nombre debe tener entre 2 y 100 caracteres")
    private String name;
    
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    
    @Column(nullable = false)
    @Min(value = 13, message = "Edad mínima es 13 años")
    @Max(value = 120, message = "Edad máxima es 120 años")
    private Integer age;
    
    @Column(nullable = false)
    @DecimalMin(value = "100.0", message = "Altura mínima es 100 cm")
    @DecimalMax(value = "250.0", message = "Altura máxima es 250 cm")
    private Double height; // en cm
    
    @Column(name = "current_weight", nullable = false)
    @DecimalMin(value = "30.0", message = "Peso mínimo es 30 kg")
    @DecimalMax(value = "300.0", message = "Peso máximo es 300 kg")
    private Double currentWeight; // en kg
    
    @Column(name = "target_weight", nullable = false)
    @DecimalMin(value = "30.0", message = "Peso objetivo mínimo es 30 kg")
    @DecimalMax(value = "300.0", message = "Peso objetivo máximo es 300 kg")
    private Double targetWeight; // en kg
    
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false)
    private ActivityLevel activityLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Goal goal;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_dietary_restrictions", 
                    joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "restriction")
    @Builder.Default
    private Set<String> dietaryRestrictions = new HashSet<>();
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_allergies", 
                    joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "allergy")
    @Builder.Default
    private Set<String> allergies = new HashSet<>();
    
    @Column(name = "streak_days", nullable = false)
    @Builder.Default
    private Integer streakDays = 0;
    
    @Column(name = "total_weight_lost", nullable = false)
    @Builder.Default
    private Double totalWeightLost = 0.0;
    
    @Column(name = "total_points", nullable = false)
    @Builder.Default
    private Integer totalPoints = 0;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;
    
    // Relaciones de amistad
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_friends",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    @Builder.Default
    private Set<User> friends = new HashSet<>();
    
    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<FriendRequest> sentFriendRequests = new HashSet<>();
    
    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<FriendRequest> receivedFriendRequests = new HashSet<>();
    
    // Auditoría
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enums
    public enum ActivityLevel {
        SEDENTARIO, LIGERO, MODERADO, ACTIVO, MUY_ACTIVO
    }
    
    public enum Goal {
        PERDER_PESO, MANTENER, GANAR_MUSCULO
    }
    
    public enum Role {
        USER, ADMIN, NUTRITIONIST, TRAINER
    }
    
    // Métodos de negocio
    public Double getBmi() {
        if (currentWeight == null || height == null || height == 0) {
            return 0.0;
        }
        return currentWeight / Math.pow(height / 100, 2);
    }
    
    public Double getDailyCalories() {
        if (currentWeight == null || height == null || age == null) {
            return 0.0;
        }
        
        // Usar cálculo básico para compatibilidad (el servicio avanzado está disponible en /nutrition-plan)
        // Fórmula Mifflin-St Jeor (para hombres, ajustar según género si se agrega)
        double bmr = (10 * currentWeight) + (6.25 * height) - (5 * age) + 5;
        
        // Factor de actividad
        double activityFactor = switch (activityLevel) {
            case SEDENTARIO -> 1.2;
            case LIGERO -> 1.375;
            case MODERADO -> 1.55;
            case ACTIVO -> 1.725;
            case MUY_ACTIVO -> 1.9;
        };
        
        double maintenanceCalories = bmr * activityFactor;
        
        // Ajustar según objetivo con cálculos más precisos
        return switch (goal) {
            case PERDER_PESO -> {
                // Déficit basado en peso actual (más seguro)
                double deficit = Math.min(500, currentWeight * 7); // Max 7 cal/kg
                yield Math.max(1500, maintenanceCalories - deficit); // Min 1500 cal
            }
            case GANAR_MUSCULO -> {
                // Superávit moderado según actividad
                double surplus = switch (activityLevel) {
                    case SEDENTARIO, LIGERO -> 200;
                    case MODERADO -> 300;
                    case ACTIVO, MUY_ACTIVO -> 400;
                };
                yield maintenanceCalories + surplus;
            }
            case MANTENER -> maintenanceCalories;
        };
    }
    
    public void addFriend(User friend) {
        this.friends.add(friend);
        friend.getFriends().add(this);
    }
    
    public void removeFriend(User friend) {
        this.friends.remove(friend);
        friend.getFriends().remove(this);
    }
    
    public boolean isFriendWith(User user) {
        return this.friends.contains(user);
    }
}