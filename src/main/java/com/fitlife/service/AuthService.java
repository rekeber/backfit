package com.fitlife.service;

import com.fitlife.dto.auth.*;
import com.fitlife.entity.User;
import com.fitlife.exception.AuthenticationException;
import com.fitlife.exception.ResourceNotFoundException;
import com.fitlife.exception.ValidationException;
import com.fitlife.repository.UserRepository;
import com.fitlife.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final RedisService redisService;
    
    public AuthResponse register(RegisterRequest request) {
        log.info("Iniciando registro para email: {}", request.getEmail());
        
        // Verificar si el email ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("El email ya está registrado");
        }
        
        // Crear nuevo usuario
        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .name(request.getName())
            .age(request.getAge())
            .height(request.getHeight())
            .currentWeight(request.getCurrentWeight())
            .targetWeight(request.getTargetWeight())
            .activityLevel(request.getActivityLevelEnum()) // Usar método helper
            .goal(request.getGoalEnum()) // Usar método helper
            .dietaryRestrictions(request.getDietaryRestrictions() != null ? 
                               request.getDietaryRestrictions() : new HashSet<>())
            .allergies(request.getAllergies() != null ? 
                      request.getAllergies() : new HashSet<>())
            .role(User.Role.USER)
            .isActive(true)
            .emailVerified(false)
            .streakDays(0)
            .totalWeightLost(0.0)
            .totalPoints(0)
            .build();
        
        user = userRepository.save(user);
        log.info("Usuario creado con ID: {}", user.getId());
        
        // Generar tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        
        // Guardar refresh token en Redis
        redisService.saveRefreshToken(user.getEmail(), refreshToken);
        
        // Enviar email de verificación (asíncrono)
        emailService.sendVerificationEmail(user.getEmail(), user.getName());
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
            .user(mapToUserResponse(user))
            .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        log.info("Iniciando login para email: {}", request.getEmail());
        
        // Autenticar usuario
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );
        
        // Buscar usuario
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new AuthenticationException("Credenciales inválidas"));
        
        // Verificar si la cuenta está activa
        if (!user.getIsActive()) {
            throw new AuthenticationException("Cuenta desactivada");
        }
        
        // Actualizar último login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // Generar tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        
        // Guardar refresh token en Redis
        redisService.saveRefreshToken(user.getEmail(), refreshToken);
        
        log.info("Login exitoso para usuario ID: {}", user.getId());
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
            .user(mapToUserResponse(user))
            .build();
    }
    
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Renovando token");
        
        // Validar refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthenticationException("Refresh token inválido");
        }
        
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        
        // Verificar que el refresh token existe en Redis
        String storedToken = redisService.getRefreshToken(email);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new AuthenticationException("Refresh token no válido");
        }
        
        // Buscar usuario
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticationException("Usuario no encontrado"));
        
        // Generar nuevos tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(email);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);
        
        // Actualizar refresh token en Redis
        redisService.saveRefreshToken(email, newRefreshToken);
        
        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
            .user(mapToUserResponse(user))
            .build();
    }
    
    @CacheEvict(value = "users", key = "#email")
    public void logout(String token) {
        log.info("Procesando logout");
        
        String email = jwtTokenProvider.getEmailFromToken(token);
        
        // Eliminar refresh token de Redis
        redisService.deleteRefreshToken(email);
        
        // Agregar token a blacklist
        redisService.blacklistToken(token);
        
        log.info("Logout completado para: {}", email);
    }
    
    public void forgotPassword(String email) {
        log.info("Solicitud de restablecimiento de contraseña para: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElse(null);
        
        if (user != null) {
            // Generar token de restablecimiento
            String resetToken = UUID.randomUUID().toString();
            
            // Guardar token en Redis con expiración de 1 hora
            redisService.savePasswordResetToken(email, resetToken);
            
            // Enviar email (asíncrono)
            emailService.sendPasswordResetEmail(email, user.getName(), resetToken);
        }
        
        // Siempre devolver éxito por seguridad
        log.info("Proceso de restablecimiento iniciado para: {}", email);
    }
    
    public void resetPassword(String token, String newPassword) {
        log.info("Restableciendo contraseña");
        
        // Buscar email asociado al token
        String email = redisService.getEmailByPasswordResetToken(token);
        if (email == null) {
            throw new ValidationException("Token de restablecimiento inválido o expirado");
        }
        
        // Buscar usuario
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Eliminar token de Redis
        redisService.deletePasswordResetToken(token);
        
        // Invalidar todas las sesiones del usuario
        redisService.deleteRefreshToken(email);
        
        log.info("Contraseña restablecida para usuario ID: {}", user.getId());
    }
    
    public void verifyEmail(String token) {
        log.info("Verificando email");
        
        String email = redisService.getEmailByVerificationToken(token);
        if (email == null) {
            throw new ValidationException("Token de verificación inválido o expirado");
        }
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        user.setEmailVerified(true);
        userRepository.save(user);
        
        // Eliminar token de Redis
        redisService.deleteVerificationToken(token);
        
        log.info("Email verificado para usuario ID: {}", user.getId());
    }
    
    public void resendVerificationEmail(String email) {
        log.info("Reenviando verificación para: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        if (user.getEmailVerified()) {
            throw new ValidationException("El email ya está verificado");
        }
        
        // Enviar nuevo email de verificación
        emailService.sendVerificationEmail(email, user.getName());
    }
    
    @Cacheable(value = "tokenValidation", key = "#token")
    public boolean validateToken(String token) {
        // Verificar si el token está en blacklist
        if (redisService.isTokenBlacklisted(token)) {
            return false;
        }
        
        return jwtTokenProvider.validateToken(token);
    }
    
    public UserResponse mapToUserResponse(User user) {
        log.info("=== MAPPING USER TO RESPONSE ===");
        log.info("User ID: {}", user.getId());
        log.info("User email: {}", user.getEmail());
        log.info("User name: {}", user.getName());
        log.info("User height: {}", user.getHeight());
        log.info("User currentWeight: {}", user.getCurrentWeight());
        log.info("User targetWeight: {}", user.getTargetWeight());
        log.info("User age: {}", user.getAge());
        log.info("================================");
        
        UserResponse response = UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .profileImageUrl(user.getProfileImageUrl())
            .age(user.getAge())
            .height(user.getHeight())
            .currentWeight(user.getCurrentWeight())
            .targetWeight(user.getTargetWeight())
            .activityLevel(user.getActivityLevel())
            .goal(user.getGoal())
            .dietaryRestrictions(user.getDietaryRestrictions())
            .allergies(user.getAllergies())
            .streakDays(user.getStreakDays())
            .totalWeightLost(user.getTotalWeightLost())
            .totalPoints(user.getTotalPoints())
            .emailVerified(user.getEmailVerified())
            .createdAt(user.getCreatedAt())
            .lastLogin(user.getLastLogin())
            .bmi(user.getBmi())
            .dailyCalories(user.getDailyCalories())
            .build();
            
        log.info("Response height: {}", response.getHeight());
        log.info("Response currentWeight: {}", response.getCurrentWeight());
        return response;
    }
}