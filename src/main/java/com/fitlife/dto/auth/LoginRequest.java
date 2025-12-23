package com.fitlife.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Email es obligatorio")
    @Email(message = "Email debe ser válido")
    private String email;
    
    @NotBlank(message = "Contraseña es obligatoria")
    private String password;
}