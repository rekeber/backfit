package com.fitlife.dto.auth;

import com.fitlife.entity.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Email es obligatorio")
    @Email(message = "Email debe ser válido")
    private String email;
    
    @NotBlank(message = "Contraseña es obligatoria")
    @Size(min = 8, message = "Contraseña debe tener al menos 8 caracteres")
    private String password;
    
    @NotBlank(message = "Nombre es obligatorio")
    @Size(min = 2, max = 100, message = "Nombre debe tener entre 2 y 100 caracteres")
    private String name;
    
    @NotNull(message = "Edad es obligatoria")
    @Min(value = 13, message = "Edad mínima es 13 años")
    @Max(value = 120, message = "Edad máxima es 120 años")
    private Integer age;
    
    @NotNull(message = "Altura es obligatoria")
    @DecimalMin(value = "100.0", message = "Altura mínima es 100 cm")
    @DecimalMax(value = "250.0", message = "Altura máxima es 250 cm")
    private Double height;
    
    @NotNull(message = "Peso actual es obligatorio")
    @DecimalMin(value = "30.0", message = "Peso mínimo es 30 kg")
    @DecimalMax(value = "300.0", message = "Peso máximo es 300 kg")
    private Double currentWeight;
    
    @NotNull(message = "Peso objetivo es obligatorio")
    @DecimalMin(value = "30.0", message = "Peso objetivo mínimo es 30 kg")
    @DecimalMax(value = "300.0", message = "Peso objetivo máximo es 300 kg")
    private Double targetWeight;
    
    @NotNull(message = "Nivel de actividad es obligatorio")
    private User.ActivityLevel activityLevel;
    
    @NotNull(message = "Objetivo es obligatorio")
    private User.Goal goal;
    
    private Set<String> dietaryRestrictions;
    private Set<String> allergies;
}