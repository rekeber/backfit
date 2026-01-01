package com.fitlife.repository;

import com.fitlife.entity.User;
import com.fitlife.entity.UserAvatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAvatarRepository extends JpaRepository<UserAvatar, Long> {
    
    // Find avatar by user (one-to-one relationship)
    Optional<UserAvatar> findByUser(User user);
    
    // Find avatars by fitness level
    List<UserAvatar> findByFitnessLevelOrderByTransformationProgressDesc(UserAvatar.FitnessLevel fitnessLevel);
    
    // Find avatars by body type
    List<UserAvatar> findByBodyTypeOrderByTransformationProgressDesc(UserAvatar.BodyType bodyType);
    
    // Find avatars with high transformation progress
    @Query("SELECT ua FROM UserAvatar ua WHERE ua.transformationProgress >= :minProgress ORDER BY ua.transformationProgress DESC")
    List<UserAvatar> findByHighTransformationProgress(@Param("minProgress") Integer minProgress);
    
    // Find avatars by muscle definition level
    List<UserAvatar> findByMuscleDefinitionGreaterThanEqualOrderByMuscleDefinitionDesc(Integer minMuscleDefinition);
    
    // Find avatars that need evolution (high progress)
    @Query("SELECT ua FROM UserAvatar ua WHERE ua.transformationProgress >= 25 AND ua.nextEvolutionDate IS NULL")
    List<UserAvatar> findAvatarsNeedingEvolution();
    
    // Find avatars due for evolution
    @Query("SELECT ua FROM UserAvatar ua WHERE ua.nextEvolutionDate IS NOT NULL AND ua.nextEvolutionDate <= :now")
    List<UserAvatar> findAvatarsDueForEvolution(@Param("now") LocalDateTime now);
    
    // Find avatars by total workouts completed range
    @Query("SELECT ua FROM UserAvatar ua WHERE ua.totalWorkoutsCompleted >= :minWorkouts AND ua.totalWorkoutsCompleted <= :maxWorkouts")
    List<UserAvatar> findByWorkoutRange(@Param("minWorkouts") Long minWorkouts, @Param("maxWorkouts") Long maxWorkouts);
    
    // Find avatars by total calories burned range
    @Query("SELECT ua FROM UserAvatar ua WHERE ua.totalCaloriesBurned >= :minCalories AND ua.totalCaloriesBurned <= :maxCalories")
    List<UserAvatar> findByCalorieRange(@Param("minCalories") Long minCalories, @Param("maxCalories") Long maxCalories);
    
    // Find avatars by weight lost range
    @Query("SELECT ua FROM UserAvatar ua WHERE ua.totalWeightLost >= :minWeightLost AND ua.totalWeightLost <= :maxWeightLost")
    List<UserAvatar> findByWeightLostRange(@Param("minWeightLost") Double minWeightLost, @Param("maxWeightLost") Double maxWeightLost);
    
    // Find top performing avatars (by transformation progress)
    @Query("SELECT ua FROM UserAvatar ua ORDER BY ua.transformationProgress DESC, ua.totalWorkoutsCompleted DESC")
    List<UserAvatar> findTopPerformingAvatars();
    
    // Find avatars that had recent body scans
    @Query("SELECT ua FROM UserAvatar ua WHERE ua.lastBodyScanDate >= :since")
    List<UserAvatar> findAvatarsWithRecentBodyScans(@Param("since") LocalDateTime since);
    
    // Find avatars needing body scan update
    @Query("SELECT ua FROM UserAvatar ua WHERE ua.lastBodyScanDate IS NULL OR ua.lastBodyScanDate < :cutoffDate")
    List<UserAvatar> findAvatarsNeedingBodyScan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Count avatars by fitness level
    @Query("SELECT ua.fitnessLevel, COUNT(ua) FROM UserAvatar ua GROUP BY ua.fitnessLevel")
    List<Object[]> countAvatarsByFitnessLevel();
    
    // Count avatars by body type
    @Query("SELECT ua.bodyType, COUNT(ua) FROM UserAvatar ua GROUP BY ua.bodyType")
    List<Object[]> countAvatarsByBodyType();
    
    // Get average transformation progress by fitness level
    @Query("SELECT ua.fitnessLevel, AVG(ua.transformationProgress) FROM UserAvatar ua GROUP BY ua.fitnessLevel")
    List<Object[]> getAverageTransformationProgressByFitnessLevel();
    
    // Find avatars with specific achievements displayed
    @Query("SELECT ua FROM UserAvatar ua WHERE ua.achievementsDisplayed LIKE %:achievementCode%")
    List<UserAvatar> findAvatarsWithAchievement(@Param("achievementCode") String achievementCode);
    
    // Find avatars by skin tone
    List<UserAvatar> findBySkinTone(UserAvatar.SkinTone skinTone);
    
    // Find avatars by hair style
    List<UserAvatar> findByHairStyle(UserAvatar.HairStyle hairStyle);
    
    // Find avatars by hair color
    List<UserAvatar> findByHairColor(UserAvatar.HairColor hairColor);
    
    // Find avatars by eye color
    List<UserAvatar> findByEyeColor(UserAvatar.EyeColor eyeColor);
    
    // Get avatar customization statistics
    @Query("SELECT ua.skinTone, ua.hairStyle, ua.hairColor, ua.eyeColor, COUNT(ua) FROM UserAvatar ua GROUP BY ua.skinTone, ua.hairStyle, ua.hairColor, ua.eyeColor")
    List<Object[]> getAvatarCustomizationStats();
    
    // Find similar avatars (same body type and fitness level)
    @Query("SELECT ua FROM UserAvatar ua WHERE ua.bodyType = :bodyType AND ua.fitnessLevel = :fitnessLevel AND ua.user != :excludeUser")
    List<UserAvatar> findSimilarAvatars(@Param("bodyType") UserAvatar.BodyType bodyType, 
                                       @Param("fitnessLevel") UserAvatar.FitnessLevel fitnessLevel, 
                                       @Param("excludeUser") User excludeUser);
}