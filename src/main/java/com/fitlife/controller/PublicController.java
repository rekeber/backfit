package com.fitlife.controller;

import com.fitlife.entity.Food;
import com.fitlife.service.NutritionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador público para endpoints que no requieren autenticación
 */
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@Slf4j
public class PublicController {
    
    private final NutritionService nutritionService;
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Public endpoint working!");
    }
    
    @GetMapping("/foods/search")
    public ResponseEntity<Page<Food>> searchFoods(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Searching foods with query: {}", query);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Food> foods = nutritionService.searchFoods(query, pageable);
        
        log.info("Found {} foods", foods.getTotalElements());
        
        return ResponseEntity.ok(foods);
    }
    
    @GetMapping("/foods/{id}")
    public ResponseEntity<Food> getFoodById(@PathVariable Long id) {
        log.info("Getting food by id: {}", id);
        
        return nutritionService.getFoodById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/foods")
    public ResponseEntity<List<Food>> getAllFoods(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        log.info("Getting all foods, page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Food> foods = nutritionService.getAllFoods(pageable);
        
        return ResponseEntity.ok(foods.getContent());
    }
}