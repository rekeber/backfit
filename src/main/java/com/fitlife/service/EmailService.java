package com.fitlife.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    @Async
    public void sendVerificationEmail(String email, String name) {
        log.info("Enviando email de verificación a: {}", email);
        // TODO: Implementar envío real de email
        // Por ahora solo simulamos el envío
        try {
            Thread.sleep(1000); // Simular tiempo de envío
            log.info("Email de verificación enviado exitosamente a: {}", email);
        } catch (InterruptedException e) {
            log.error("Error simulando envío de email", e);
            Thread.currentThread().interrupt();
        }
    }
    
    @Async
    public void sendPasswordResetEmail(String email, String name, String resetToken) {
        log.info("Enviando email de restablecimiento de contraseña a: {}", email);
        // TODO: Implementar envío real de email
        // Por ahora solo simulamos el envío
        try {
            Thread.sleep(1000); // Simular tiempo de envío
            log.info("Email de restablecimiento enviado exitosamente a: {}", email);
        } catch (InterruptedException e) {
            log.error("Error simulando envío de email", e);
            Thread.currentThread().interrupt();
        }
    }
    
    @Async
    public void sendWelcomeEmail(String email, String name) {
        log.info("Enviando email de bienvenida a: {}", email);
        // TODO: Implementar envío real de email
        try {
            Thread.sleep(1000); // Simular tiempo de envío
            log.info("Email de bienvenida enviado exitosamente a: {}", email);
        } catch (InterruptedException e) {
            log.error("Error simulando envío de email", e);
            Thread.currentThread().interrupt();
        }
    }
}