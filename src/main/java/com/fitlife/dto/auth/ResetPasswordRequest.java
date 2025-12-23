package com.fitlife.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    
    @NotBlank(message = "Token es obligatorio")
    private String token;
    
    @NotBlank(message = "Nueva contraseña es obligatoria")
    @Size(min = 8, message = "Contraseña debe tener al menos 8 caracteres")
    private String newPassword;
}