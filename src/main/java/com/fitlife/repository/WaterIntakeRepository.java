package com.fitlife.repository;

import com.fitlife.entity.WaterIntake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WaterIntakeRepository extends JpaRepository<WaterIntake, Long> {
    
    Optional<WaterIntake> findByUserIdAndDate(Long userId, LocalDate date);
    
    @Query("SELECT COALESCE(SUM(w.glasses), 0) FROM WaterIntake w WHERE w.user.id = :userId AND w.date = :date")
    Integer getTotalGlassesByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COALESCE(AVG(w.glasses), 0) FROM WaterIntake w WHERE w.user.id = :userId AND w.date >= :startDate")
    Double getAverageGlassesByUserSince(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}