package com.fitlife.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "spring.data.redis.repositories.enabled", havingValue = "false")
@Slf4j
public class MockRedisService extends RedisService {
    
    private final Map<String, String> mockStorage = new ConcurrentHashMap<>();
    
    public MockRedisService() {
        super(null); // No RedisTemplate needed for mock
        log.info("Usando MockRedisService para desarrollo (Redis deshabilitado)");
    }
    
    @Override
    public void saveRefreshToken(String email, String refreshToken) {
        String key = "refresh_token:" + email;
        mockStorage.put(key, refreshToken);
        log.debug("Mock: Refresh token guardado para: {}", email);
    }
    
    @Override
    public String getRefreshToken(String email) {
        String key = "refresh_token:" + email;
        return mockStorage.get(key);
    }
    
    @Override
    public void deleteRefreshToken(String email) {
        String key = "refresh_token:" + email;
        mockStorage.remove(key);
        log.debug("Mock: Refresh token eliminado para: {}", email);
    }
    
    @Override
    public void blacklistToken(String token) {
        String key = "blacklist:" + token;
        mockStorage.put(key, "blacklisted");
        log.debug("Mock: Token agregado a blacklist");
    }
    
    @Override
    public boolean isTokenBlacklisted(String token) {
        String key = "blacklist:" + token;
        return mockStorage.containsKey(key);
    }
    
    @Override
    public void savePasswordResetToken(String email, String token) {
        String key = "password_reset:" + token;
        mockStorage.put(key, email);
        log.debug("Mock: Token de restablecimiento guardado para: {}", email);
    }
    
    @Override
    public String getEmailByPasswordResetToken(String token) {
        String key = "password_reset:" + token;
        return mockStorage.get(key);
    }
    
    @Override
    public void deletePasswordResetToken(String token) {
        String key = "password_reset:" + token;
        mockStorage.remove(key);
        log.debug("Mock: Token de restablecimiento eliminado");
    }
    
    @Override
    public void saveVerificationToken(String email) {
        String token = UUID.randomUUID().toString();
        String key = "email_verification:" + token;
        mockStorage.put(key, email);
        log.debug("Mock: Token de verificación guardado para: {}", email);
    }
    
    @Override
    public String getEmailByVerificationToken(String token) {
        String key = "email_verification:" + token;
        return mockStorage.get(key);
    }
    
    @Override
    public void deleteVerificationToken(String token) {
        String key = "email_verification:" + token;
        mockStorage.remove(key);
        log.debug("Mock: Token de verificación eliminado");
    }
}