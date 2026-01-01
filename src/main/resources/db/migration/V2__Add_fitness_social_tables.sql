-- V2__Add_fitness_social_tables.sql
-- Add new tables for fitness and social features

-- Update users table with new fields
ALTER TABLE users 
ADD COLUMN bio VARCHAR(500),
ADD COLUMN date_of_birth DATE,
ADD COLUMN gender VARCHAR(10) DEFAULT 'OTHER',
ADD COLUMN current_streak INT NOT NULL DEFAULT 0,
ADD COLUMN longest_streak INT NOT NULL DEFAULT 0;

-- Exercises table
CREATE TABLE exercises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(100) NOT NULL,
    duration INT NOT NULL,
    calories_burned INT NOT NULL,
    difficulty VARCHAR(50) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_category (category),
    INDEX idx_difficulty (difficulty),
    INDEX idx_calories (calories_burned),
    INDEX idx_duration (duration)
);

-- Exercise muscle groups
CREATE TABLE exercise_muscle_groups (
    exercise_id BIGINT NOT NULL,
    muscle_group VARCHAR(100) NOT NULL,
    PRIMARY KEY (exercise_id, muscle_group),
    FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
);

-- Workout sessions
CREATE TABLE workout_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    duration INT NOT NULL,
    calories_burned INT NOT NULL,
    notes TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE RESTRICT,
    INDEX idx_user_date (user_id, start_time),
    INDEX idx_start_time (start_time)
);

-- Food entries
CREATE TABLE food_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    quantity DOUBLE NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    date DATE NOT NULL,
    total_calories DOUBLE NOT NULL,
    total_protein DOUBLE NOT NULL,
    total_carbs DOUBLE NOT NULL,
    total_fat DOUBLE NOT NULL,
    total_fiber DOUBLE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE RESTRICT,
    INDEX idx_user_date (user_id, date),
    INDEX idx_user_meal (user_id, date, meal_type),
    INDEX idx_date (date)
);

-- Nutrition goals
CREATE TABLE nutrition_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    daily_calories INT NOT NULL DEFAULT 2000,
    protein DOUBLE NOT NULL DEFAULT 150.0,
    carbs DOUBLE NOT NULL DEFAULT 250.0,
    fat DOUBLE NOT NULL DEFAULT 67.0,
    fiber DOUBLE NOT NULL DEFAULT 25.0,
    water DOUBLE NOT NULL DEFAULT 2.0,
    activity_level VARCHAR(20) NOT NULL DEFAULT 'MODERATE',
    goal VARCHAR(20) NOT NULL DEFAULT 'MAINTAIN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Posts
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(500),
    type VARCHAR(20) NOT NULL DEFAULT 'GENERAL',
    likes INT NOT NULL DEFAULT 0,
    comments INT NOT NULL DEFAULT 0,
    achievement VARCHAR(200),
    workout_session_id BIGINT,
    food_entry_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (workout_session_id) REFERENCES workout_sessions(id) ON DELETE SET NULL,
    FOREIGN KEY (food_entry_id) REFERENCES food_entries(id) ON DELETE SET NULL,
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
);

-- Post likes
CREATE TABLE post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_post_like (post_id, user_id),
    INDEX idx_post (post_id),
    INDEX idx_user (user_id)
);

-- Comments
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_post_created (post_id, created_at),
    INDEX idx_user (user_id)
);

-- User follows
CREATE TABLE user_follows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_follow (follower_id, following_id),
    CONSTRAINT chk_no_self_follow CHECK (follower_id != following_id),
    INDEX idx_follower (follower_id),
    INDEX idx_following (following_id)
);

-- Achievements
CREATE TABLE achievements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    icon_url VARCHAR(500),
    category VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'MILESTONE',
    required_value INT NOT NULL DEFAULT 1,
    points INT NOT NULL DEFAULT 10,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_category (category),
    INDEX idx_type (type),
    INDEX idx_is_active (is_active)
);

-- User achievements
CREATE TABLE user_achievements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    achievement_id BIGINT NOT NULL,
    unlocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    progress INT NOT NULL DEFAULT 0,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_achievement (user_id, achievement_id),
    INDEX idx_user_unlocked (user_id, unlocked_at),
    INDEX idx_user_completed (user_id, is_completed)
);