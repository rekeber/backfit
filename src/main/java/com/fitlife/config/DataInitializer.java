package com.fitlife.config;

import com.fitlife.entity.User;
import com.fitlife.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;

@Component
@Profile("dev") // Solo en desarrollo
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Inicializando datos de prueba en PostgreSQL...");
        
        // Crear usuario de prueba si no existe
        if (!userRepository.existsByEmail("rekeber@gmail.com")) {
            User testUser = User.builder()
                .email("rekeber@gmail.com")
                .password(passwordEncoder.encode("password123"))
                .name("Usuario de Prueba")
                .age(30)
                .height(175.0) // 175 cm
                .currentWeight(75.0) // 75 kg
                .targetWeight(70.0) // 70 kg
                .activityLevel(User.ActivityLevel.MODERADO)
                .goal(User.Goal.PERDER_PESO)
                .dietaryRestrictions(new HashSet<>())
                .allergies(new HashSet<>())
                .role(User.Role.USER)
                .isActive(true)
                .emailVerified(true)
                .streakDays(5)
                .totalWeightLost(2.0)
                .totalPoints(150)
                .lastLogin(LocalDateTime.now())
                .build();
            
            userRepository.save(testUser);
            log.info("Usuario de prueba creado en PostgreSQL: rekeber@gmail.com / password123");
            log.info("Datos del usuario: Peso={}kg, Altura={}cm, IMC se calculará automáticamente", 
                    testUser.getCurrentWeight(), testUser.getHeight());
        } else {
            User existingUser = userRepository.findByEmail("rekeber@gmail.com").orElse(null);
            if (existingUser != null) {
                log.info("Usuario de prueba ya existe en PostgreSQL: rekeber@gmail.com");
                log.info("Datos existentes: Peso={}kg, Altura={}cm", 
                        existingUser.getCurrentWeight(), existingUser.getHeight());
            }
        }
    }
}