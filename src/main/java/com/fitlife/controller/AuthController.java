package com.fitlife.controller;

import com.fitlife.dto.auth.*;
import com.fitlife.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; 

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para autenticación y registro de usuarios")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una nueva cuenta de usuario")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Intento de registro para email: {}", request.getEmail());
        
        AuthResponse response = authService.register(request);
        
        log.info("Usuario registrado exitosamente: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve tokens JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Intento de login para email: {}", request.getEmail());
        
        AuthResponse response = authService.login(request);
        
        log.info("Login exitoso para email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Renovar token", description = "Renueva el token de acceso usando el refresh token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Intento de renovación de token");
        
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        
        log.info("Token renovado exitosamente");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Invalida el token de acceso actual")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        log.info("Intento de logout");
        
        // Extraer el token sin el prefijo "Bearer "
        String jwtToken = token.substring(7);
        authService.logout(jwtToken);
        
        log.info("Logout exitoso");
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar restablecimiento de contraseña", 
               description = "Envía un email para restablecer la contraseña")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Solicitud de restablecimiento de contraseña para: {}", request.getEmail());
        
        authService.forgotPassword(request.getEmail());
        
        MessageResponse response = MessageResponse.builder()
            .message("Si el email existe, se ha enviado un enlace de restablecimiento")
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña", 
               description = "Restablece la contraseña usando el token enviado por email")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Intento de restablecimiento de contraseña");
        
        authService.resetPassword(request.getToken(), request.getNewPassword());
        
        MessageResponse response = MessageResponse.builder()
            .message("Contraseña restablecida exitosamente")
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify-email")
    @Operation(summary = "Verificar email", description = "Verifica el email del usuario")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        log.info("Intento de verificación de email");
        
        authService.verifyEmail(token);
        
        MessageResponse response = MessageResponse.builder()
            .message("Email verificado exitosamente")
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/resend-verification")
    @Operation(summary = "Reenviar verificación", 
               description = "Reenvía el email de verificación")
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        log.info("Reenvío de verificación para: {}", request.getEmail());
        
        authService.resendVerificationEmail(request.getEmail());
        
        MessageResponse response = MessageResponse.builder()
            .message("Email de verificación reenviado")
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/validate-token")
    @Operation(summary = "Validar token", description = "Valida si un token JWT es válido")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestHeader("Authorization") String token) {
        log.info("Validación de token");
        
        // Extraer el token sin el prefijo "Bearer "
        String jwtToken = token.substring(7);
        boolean isValid = authService.validateToken(jwtToken);
        
        TokenValidationResponse response = TokenValidationResponse.builder()
            .valid(isValid)
            .build();
        
        return ResponseEntity.ok(response);
    }
}