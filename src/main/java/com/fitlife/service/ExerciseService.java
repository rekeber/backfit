package com.fitlife.service;

import com.fitlife.entity.Exercise;
import com.fitlife.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExerciseService {
    
    private final ExerciseRepository exerciseRepository;
    
    public List<Exercise> getAllExercises() {
        return exerciseRepository.findAll();
    }
    
    public Optional<Exercise> getExerciseById(Long id) {
        return exerciseRepository.findById(id);
    }
    
    public List<Exercise> getExercisesByCategory(String category) {
        return exerciseRepository.findByCategory(category);
    }
    
    public List<Exercise> getExercisesByDifficulty(String difficulty) {
        return exerciseRepository.findByDifficulty(difficulty);
    }
    
    public List<String> getAllCategories() {
        return exerciseRepository.findAllCategories();
    }
    
    public Page<Exercise> searchExercises(String searchTerm, Pageable pageable) {
        return exerciseRepository.searchExercises(searchTerm, pageable);
    }
    
    public List<Exercise> getExercisesByCategoryAndDifficulty(String category, String difficulty) {
        return exerciseRepository.findByCategoryAndDifficulty(category, difficulty);
    }
    
    public List<Exercise> getExercisesByCalorieRange(Integer minCalories, Integer maxCalories) {
        return exerciseRepository.findByCalorieRange(minCalories, maxCalories);
    }
    
    public List<Exercise> getExercisesByDurationRange(Integer minDuration, Integer maxDuration) {
        return exerciseRepository.findByDurationRange(minDuration, maxDuration);
    }
    
    public Exercise createExercise(Exercise exercise) {
        log.info("Creating new exercise: {}", exercise.getName());
        return exerciseRepository.save(exercise);
    }
    
    public Exercise updateExercise(Long id, Exercise exerciseDetails) {
        return exerciseRepository.findById(id)
                .map(exercise -> {
                    exercise.setName(exerciseDetails.getName());
                    exercise.setCategory(exerciseDetails.getCategory());
                    exercise.setDuration(exerciseDetails.getDuration());
                    exercise.setCaloriesBurned(exerciseDetails.getCaloriesBurned());
                    exercise.setDifficulty(exerciseDetails.getDifficulty());
                    exercise.setDescription(exerciseDetails.getDescription());
                    exercise.setImageUrl(exerciseDetails.getImageUrl());
                    exercise.setMuscleGroups(exerciseDetails.getMuscleGroups());
                    
                    log.info("Updated exercise: {}", exercise.getName());
                    return exerciseRepository.save(exercise);
                })
                .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + id));
    }
    
    public void deleteExercise(Long id) {
        if (exerciseRepository.existsById(id)) {
            exerciseRepository.deleteById(id);
            log.info("Deleted exercise with id: {}", id);
        } else {
            throw new RuntimeException("Exercise not found with id: " + id);
        }
    }
    
    public List<Exercise> getRecommendedExercises(String userLevel, String preferredCategory) {
        if (preferredCategory != null && !preferredCategory.isEmpty()) {
            return exerciseRepository.findByCategoryAndDifficulty(preferredCategory, userLevel);
        }
        return exerciseRepository.findByDifficulty(userLevel);
    }
}