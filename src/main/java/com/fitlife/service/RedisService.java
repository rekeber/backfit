package com.fitlife.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "spring.data.redis.repositories.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String PASSWORD_RESET_PREFIX = "password_reset:";
    private static final String EMAIL_VERIFICATION_PREFIX = "email_verification:";
    
    public void saveRefreshToken(String email, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofDays(7));
        log.debug("Refresh token guardado para: {}", email);
    }
    
    public String getRefreshToken(String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        return redisTemplate.opsForValue().get(key);
    }
    
    public void deleteRefreshToken(String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.delete(key);
        log.debug("Refresh token eliminado para: {}", email);
    }
    
    public void blacklistToken(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofHours(24));
        log.debug("Token agregado a blacklist");
    }
    
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(key);
    }
    
    public void savePasswordResetToken(String email, String token) {
        String key = PASSWORD_RESET_PREFIX + token;
        redisTemplate.opsForValue().set(key, email, Duration.ofHours(1));
        log.debug("Token de restablecimiento guardado para: {}", email);
    }
    
    public String getEmailByPasswordResetToken(String token) {
        String key = PASSWORD_RESET_PREFIX + token;
        return redisTemplate.opsForValue().get(key);
    }
    
    public void deletePasswordResetToken(String token) {
        String key = PASSWORD_RESET_PREFIX + token;
        redisTemplate.delete(key);
        log.debug("Token de restablecimiento eliminado");
    }
    
    public void saveVerificationToken(String email) {
        String token = UUID.randomUUID().toString();
        String key = EMAIL_VERIFICATION_PREFIX + token;
        redisTemplate.opsForValue().set(key, email, Duration.ofHours(24));
        log.debug("Token de verificación guardado para: {}", email);
    }
    
    public String getEmailByVerificationToken(String token) {
        String key = EMAIL_VERIFICATION_PREFIX + token;
        return redisTemplate.opsForValue().get(key);
    }
    
    public void deleteVerificationToken(String token) {
        String key = EMAIL_VERIFICATION_PREFIX + token;
        redisTemplate.delete(key);
        log.debug("Token de verificación eliminado");
    }
}