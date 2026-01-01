package com.fitlife.controller;

import com.fitlife.entity.Exercise;
import com.fitlife.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exercises")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ExerciseController {
    
    private final ExerciseService exerciseService;
    
    @GetMapping
    public ResponseEntity<List<Exercise>> getAllExercises() {
        return ResponseEntity.ok(exerciseService.getAllExercises());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Exercise> getExerciseById(@PathVariable Long id) {
        return exerciseService.getExerciseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<Exercise>> searchExercises(
            @RequestParam String q,
            Pageable pageable) {
        return ResponseEntity.ok(exerciseService.searchExercises(q, pageable));
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Exercise>> getExercisesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(exerciseService.getExercisesByCategory(category));
    }
    
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<Exercise>> getExercisesByDifficulty(@PathVariable String difficulty) {
        return ResponseEntity.ok(exerciseService.getExercisesByDifficulty(difficulty));
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(exerciseService.getAllCategories());
    }
    
    @GetMapping("/filter")
    public ResponseEntity<List<Exercise>> filterExercises(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Integer minCalories,
            @RequestParam(required = false) Integer maxCalories,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration) {
        
        if (category != null && difficulty != null) {
            return ResponseEntity.ok(exerciseService.getExercisesByCategoryAndDifficulty(category, difficulty));
        }
        
        if (minCalories != null && maxCalories != null) {
            return ResponseEntity.ok(exerciseService.getExercisesByCalorieRange(minCalories, maxCalories));
        }
        
        if (minDuration != null && maxDuration != null) {
            return ResponseEntity.ok(exerciseService.getExercisesByDurationRange(minDuration, maxDuration));
        }
        
        return ResponseEntity.ok(exerciseService.getAllExercises());
    }
    
    @GetMapping("/recommended")
    public ResponseEntity<List<Exercise>> getRecommendedExercises(
            @RequestParam(defaultValue = "Principiante") String level,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(exerciseService.getRecommendedExercises(level, category));
    }
    
    @PostMapping
    public ResponseEntity<Exercise> createExercise(@RequestBody Exercise exercise) {
        return ResponseEntity.ok(exerciseService.createExercise(exercise));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Exercise> updateExercise(@PathVariable Long id, @RequestBody Exercise exercise) {
        try {
            return ResponseEntity.ok(exerciseService.updateExercise(id, exercise));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExercise(@PathVariable Long id) {
        try {
            exerciseService.deleteExercise(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}