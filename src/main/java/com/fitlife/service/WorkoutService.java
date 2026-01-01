package com.fitlife.service;

import com.fitlife.entity.Exercise;
import com.fitlife.entity.User;
import com.fitlife.entity.WorkoutSession;
import com.fitlife.repository.ExerciseRepository;
import com.fitlife.repository.UserRepository;
import com.fitlife.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WorkoutService {
    
    private final WorkoutSessionRepository workoutSessionRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    
    public List<WorkoutSession> getUserWorkouts(User user) {
        return workoutSessionRepository.findByUserOrderByStartTimeDesc(user);
    }
    
    public Page<WorkoutSession> getUserWorkouts(User user, Pageable pageable) {
        return workoutSessionRepository.findByUserOrderByStartTimeDesc(user, pageable);
    }
    
    public List<WorkoutSession> getRecentWorkouts(User user, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return workoutSessionRepository.findRecentByUser(user, pageable);
    }
    
    public List<WorkoutSession> getUserWorkoutsByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return workoutSessionRepository.findByUserAndDateRange(user, startDate, endDate);
    }
    
    public WorkoutSession startWorkout(User user, Long exerciseId, String notes) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));
        
        WorkoutSession workout = WorkoutSession.builder()
                .user(user)
                .exercise(exercise)
                .duration(0) // Se actualizará al finalizar
                .caloriesBurned(0) // Se calculará al finalizar
                .notes(notes)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now()) // Se actualizará al finalizar
                .build();
        
        log.info("Started workout for user {} with exercise {}", user.getEmail(), exercise.getName());
        return workoutSessionRepository.save(workout);
    }
    
    public WorkoutSession finishWorkout(Long workoutId, String additionalNotes) {
        WorkoutSession workout = workoutSessionRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout session not found with id: " + workoutId));
        
        LocalDateTime endTime = LocalDateTime.now();
        long durationMinutes = ChronoUnit.MINUTES.between(workout.getStartTime(), endTime);
        
        // Calcular calorías basado en la duración real y el ejercicio
        int caloriesPerMinute = workout.getExercise().getCaloriesBurned() / workout.getExercise().getDuration();
        int totalCalories = (int) (caloriesPerMinute * durationMinutes);
        
        workout.setEndTime(endTime);
        workout.setDuration((int) durationMinutes);
        workout.setCaloriesBurned(totalCalories);
        
        if (additionalNotes != null && !additionalNotes.isEmpty()) {
            String currentNotes = workout.getNotes() != null ? workout.getNotes() : "";
            workout.setNotes(currentNotes + "\n" + additionalNotes);
        }
        
        // Actualizar puntos del usuario
        User user = workout.getUser();
        user.setTotalPoints(user.getTotalPoints() + (totalCalories / 10)); // 1 punto por cada 10 calorías
        userRepository.save(user);
        
        log.info("Finished workout for user {} - Duration: {} min, Calories: {}", 
                workout.getUser().getEmail(), durationMinutes, totalCalories);
        
        return workoutSessionRepository.save(workout);
    }
    
    public WorkoutSession createQuickWorkout(User user, Long exerciseId, Integer duration, String notes) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));
        
        // Calcular calorías basado en la duración proporcionada
        int caloriesPerMinute = exercise.getCaloriesBurned() / exercise.getDuration();
        int totalCalories = caloriesPerMinute * duration;
        
        LocalDateTime now = LocalDateTime.now();
        WorkoutSession workout = WorkoutSession.builder()
                .user(user)
                .exercise(exercise)
                .duration(duration)
                .caloriesBurned(totalCalories)
                .notes(notes)
                .startTime(now.minusMinutes(duration))
                .endTime(now)
                .build();
        
        // Actualizar puntos del usuario
        user.setTotalPoints(user.getTotalPoints() + (totalCalories / 10));
        userRepository.save(user);
        
        log.info("Created quick workout for user {} - Exercise: {}, Duration: {} min", 
                user.getEmail(), exercise.getName(), duration);
        
        return workoutSessionRepository.save(workout);
    }
    
    public Optional<WorkoutSession> getWorkoutById(Long id) {
        return workoutSessionRepository.findById(id);
    }
    
    public void deleteWorkout(Long id, User user) {
        WorkoutSession workout = workoutSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workout session not found with id: " + id));
        
        if (!workout.getUser().equals(user)) {
            throw new RuntimeException("User not authorized to delete this workout");
        }
        
        workoutSessionRepository.delete(workout);
        log.info("Deleted workout session with id: {}", id);
    }
    
    // Estadísticas
    public Long getWorkoutCountSince(User user, LocalDateTime since) {
        return workoutSessionRepository.countByUserSince(user, since);
    }
    
    public Integer getTotalCaloriesBurnedSince(User user, LocalDateTime since) {
        Integer calories = workoutSessionRepository.sumCaloriesBurnedByUserSince(user, since);
        return calories != null ? calories : 0;
    }
    
    public Integer getTotalWorkoutTimeSince(User user, LocalDateTime since) {
        Integer duration = workoutSessionRepository.sumDurationByUserSince(user, since);
        return duration != null ? duration : 0;
    }
    
    public Long getWeeklyWorkoutCount(User user) {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        return getWorkoutCountSince(user, weekAgo);
    }
    
    public Integer getTodayCaloriesBurned(User user) {
        LocalDateTime startOfDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        return getTotalCaloriesBurnedSince(user, startOfDay);
    }
}