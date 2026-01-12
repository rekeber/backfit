package com.fitlife.controller;

import com.fitlife.entity.Food;
import com.fitlife.service.NutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador público para búsqueda de alimentos (sin autenticación)
 */
@RestController
@RequestMapping("/foods")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class PublicFoodController {
    
    private final NutritionService nutritionService;
    
    @GetMapping("/search")
    public ResponseEntity<Page<Food>> searchFoods(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Food> foods = nutritionService.searchFoods(query, pageable);
        return ResponseEntity.ok(foods);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Food> getFoodById(@PathVariable Long id) {
        return nutritionService.getFoodById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<Food>> getAllFoods(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Food> foods = nutritionService.getAllFoods(pageable);
        return ResponseEntity.ok(foods.getContent());
    }
}