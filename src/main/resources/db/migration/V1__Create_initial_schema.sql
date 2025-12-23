-- Crear extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla de usuarios
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    profile_image_url VARCHAR(500),
    age INTEGER NOT NULL CHECK (age >= 13 AND age <= 120),
    height DECIMAL(5,2) NOT NULL CHECK (height >= 100.0 AND height <= 250.0),
    current_weight DECIMAL(5,2) NOT NULL CHECK (current_weight >= 30.0 AND current_weight <= 300.0),
    target_weight DECIMAL(5,2) NOT NULL CHECK (target_weight >= 30.0 AND target_weight <= 300.0),
    activity_level VARCHAR(20) NOT NULL CHECK (activity_level IN ('SEDENTARIO', 'LIGERO', 'MODERADO', 'ACTIVO', 'MUY_ACTIVO')),
    goal VARCHAR(20) NOT NULL CHECK (goal IN ('PERDER_PESO', 'MANTENER', 'GANAR_MUSCULO')),
    streak_days INTEGER DEFAULT 0 NOT NULL,
    total_weight_lost DECIMAL(5,2) DEFAULT 0.0 NOT NULL,
    total_points INTEGER DEFAULT 0 NOT NULL,
    last_login TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE NOT NULL,
    role VARCHAR(20) DEFAULT 'USER' NOT NULL CHECK (role IN ('USER', 'ADMIN', 'NUTRITIONIST', 'TRAINER')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Tabla de restricciones dietéticas del usuario
CREATE TABLE user_dietary_restrictions (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    restriction VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, restriction)
);

-- Tabla de alergias del usuario
CREATE TABLE user_allergies (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    allergy VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, allergy)
);

-- Tabla de alimentos
CREATE TABLE foods (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    calories_per_100g DECIMAL(6,2) NOT NULL CHECK (calories_per_100g >= 0 AND calories_per_100g <= 1000),
    protein_per_100g DECIMAL(5,2) NOT NULL CHECK (protein_per_100g >= 0 AND protein_per_100g <= 100),
    carbs_per_100g DECIMAL(5,2) NOT NULL CHECK (carbs_per_100g >= 0 AND carbs_per_100g <= 100),
    fat_per_100g DECIMAL(5,2) NOT NULL CHECK (fat_per_100g >= 0 AND fat_per_100g <= 100),
    fiber_per_100g DECIMAL(5,2) NOT NULL CHECK (fiber_per_100g >= 0 AND fiber_per_100g <= 100),
    image_url VARCHAR(500),
    is_healthy BOOLEAN DEFAULT TRUE NOT NULL,
    glycemic_index DECIMAL(5,2) CHECK (glycemic_index >= 0 AND glycemic_index <= 100),
    barcode VARCHAR(50),
    brand VARCHAR(100),
    serving_size VARCHAR(50),
    verification_status VARCHAR(20) DEFAULT 'PENDING' CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    verified_by BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT REFERENCES users(id)
);

-- Tabla de categorías de alimentos
CREATE TABLE food_categories (
    food_id BIGINT NOT NULL REFERENCES foods(id) ON DELETE CASCADE,
    category VARCHAR(100) NOT NULL,
    PRIMARY KEY (food_id, category)
);

-- Tabla de alérgenos de alimentos
CREATE TABLE food_allergens (
    food_id BIGINT NOT NULL REFERENCES foods(id) ON DELETE CASCADE,
    allergen VARCHAR(100) NOT NULL,
    PRIMARY KEY (food_id, allergen)
);

-- Tabla de vitaminas de alimentos
CREATE TABLE food_vitamins (
    food_id BIGINT NOT NULL REFERENCES foods(id) ON DELETE CASCADE,
    vitamin_name VARCHAR(50) NOT NULL,
    amount_per_100g DECIMAL(10,4) NOT NULL,
    unit VARCHAR(10) NOT NULL,
    PRIMARY KEY (food_id, vitamin_name)
);

-- Tabla de minerales de alimentos
CREATE TABLE food_minerals (
    food_id BIGINT NOT NULL REFERENCES foods(id) ON DELETE CASCADE,
    mineral_name VARCHAR(50) NOT NULL,
    amount_per_100g DECIMAL(10,4) NOT NULL,
    unit VARCHAR(10) NOT NULL,
    PRIMARY KEY (food_id, mineral_name)
);

-- Tabla de ejercicios
CREATE TABLE exercises (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL CHECK (category IN ('CARDIO', 'FUERZA', 'FLEXIBILIDAD', 'DEPORTES', 'HIIT')),
    muscle_group VARCHAR(50) NOT NULL,
    difficulty VARCHAR(20) NOT NULL CHECK (difficulty IN ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO')),
    calories_per_minute DECIMAL(5,2) NOT NULL CHECK (calories_per_minute >= 0),
    image_url VARCHAR(500),
    video_url VARCHAR(500),
    is_bodyweight BOOLEAN DEFAULT TRUE NOT NULL,
    verification_status VARCHAR(20) DEFAULT 'PENDING' CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    verified_by BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT REFERENCES users(id)
);

-- Tabla de equipamiento necesario para ejercicios
CREATE TABLE exercise_equipment (
    exercise_id BIGINT NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    equipment VARCHAR(100) NOT NULL,
    PRIMARY KEY (exercise_id, equipment)
);

-- Tabla de instrucciones de ejercicios
CREATE TABLE exercise_instructions (
    id BIGSERIAL PRIMARY KEY,
    exercise_id BIGINT NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    step_number INTEGER NOT NULL,
    instruction TEXT NOT NULL,
    UNIQUE (exercise_id, step_number)
);

-- Tabla de consejos de ejercicios
CREATE TABLE exercise_tips (
    id BIGSERIAL PRIMARY KEY,
    exercise_id BIGINT NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    tip TEXT NOT NULL
);

-- Tabla de rutinas de ejercicio
CREATE TABLE workouts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    difficulty VARCHAR(20) NOT NULL CHECK (difficulty IN ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO')),
    estimated_duration INTEGER NOT NULL CHECK (estimated_duration > 0),
    category VARCHAR(50) NOT NULL,
    image_url VARCHAR(500),
    rating DECIMAL(3,2) DEFAULT 0.0 CHECK (rating >= 0 AND rating <= 5),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT NOT NULL REFERENCES users(id)
);

-- Tabla de músculos objetivo de rutinas
CREATE TABLE workout_target_muscles (
    workout_id BIGINT NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    muscle_group VARCHAR(50) NOT NULL,
    PRIMARY KEY (workout_id, muscle_group)
);

-- Tabla de ejercicios en rutinas
CREATE TABLE workout_exercises (
    id BIGSERIAL PRIMARY KEY,
    workout_id BIGINT NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercises(id),
    order_index INTEGER NOT NULL,
    sets INTEGER DEFAULT 1 NOT NULL CHECK (sets > 0),
    reps INTEGER DEFAULT 1 NOT NULL CHECK (reps > 0),
    weight DECIMAL(5,2) DEFAULT 0.0 NOT NULL CHECK (weight >= 0),
    duration_minutes INTEGER DEFAULT 1 NOT NULL CHECK (duration_minutes > 0),
    rest_seconds INTEGER DEFAULT 60 NOT NULL CHECK (rest_seconds >= 0),
    notes TEXT,
    UNIQUE (workout_id, order_index)
);

-- Tabla de comidas/recetas
CREATE TABLE meals (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    meal_type VARCHAR(20) NOT NULL CHECK (meal_type IN ('DESAYUNO', 'ALMUERZO', 'CENA', 'SNACK')),
    image_url VARCHAR(500),
    preparation_time INTEGER DEFAULT 0 CHECK (preparation_time >= 0),
    difficulty VARCHAR(20) DEFAULT 'FACIL' CHECK (difficulty IN ('FACIL', 'MEDIO', 'DIFICIL')),
    rating DECIMAL(3,2) DEFAULT 0.0 CHECK (rating >= 0 AND rating <= 5),
    servings INTEGER DEFAULT 1 CHECK (servings > 0),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT NOT NULL REFERENCES users(id)
);

-- Tabla de ingredientes en comidas
CREATE TABLE meal_ingredients (
    id BIGSERIAL PRIMARY KEY,
    meal_id BIGINT NOT NULL REFERENCES meals(id) ON DELETE CASCADE,
    food_id BIGINT NOT NULL REFERENCES foods(id),
    quantity DECIMAL(8,2) NOT NULL CHECK (quantity > 0),
    unit VARCHAR(20) DEFAULT 'gramos' NOT NULL
);

-- Tabla de instrucciones de preparación de comidas
CREATE TABLE meal_instructions (
    id BIGSERIAL PRIMARY KEY,
    meal_id BIGINT NOT NULL REFERENCES meals(id) ON DELETE CASCADE,
    step_number INTEGER NOT NULL,
    instruction TEXT NOT NULL,
    UNIQUE (meal_id, step_number)
);

-- Tabla de etiquetas de comidas
CREATE TABLE meal_tags (
    meal_id BIGINT NOT NULL REFERENCES meals(id) ON DELETE CASCADE,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (meal_id, tag)
);

-- Tabla de solicitudes de amistad
CREATE TABLE friend_requests (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (sender_id, receiver_id),
    CHECK (sender_id != receiver_id)
);

-- Tabla de amistades (muchos a muchos)
CREATE TABLE user_friends (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    friend_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, friend_id),
    CHECK (user_id != friend_id)
);

-- Tabla de publicaciones sociales
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    image_url VARCHAR(500),
    post_type VARCHAR(20) DEFAULT 'TEXT' CHECK (post_type IN ('TEXT', 'IMAGE', 'WORKOUT', 'MEAL', 'ACHIEVEMENT')),
    related_id BIGINT, -- ID relacionado según el tipo (workout_id, meal_id, etc.)
    likes_count INTEGER DEFAULT 0 NOT NULL CHECK (likes_count >= 0),
    comments_count INTEGER DEFAULT 0 NOT NULL CHECK (comments_count >= 0),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Tabla de likes en publicaciones
CREATE TABLE post_likes (
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (post_id, user_id)
);

-- Tabla de comentarios en publicaciones
CREATE TABLE post_comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    parent_comment_id BIGINT REFERENCES post_comments(id) ON DELETE CASCADE,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Tabla de desafíos
CREATE TABLE challenges (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    challenge_type VARCHAR(50) NOT NULL CHECK (challenge_type IN ('WORKOUT', 'NUTRITION', 'WEIGHT_LOSS', 'STREAK', 'STEPS')),
    target_value DECIMAL(10,2) NOT NULL,
    target_unit VARCHAR(20) NOT NULL,
    duration_days INTEGER NOT NULL CHECK (duration_days > 0),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reward_points INTEGER DEFAULT 0 CHECK (reward_points >= 0),
    max_participants INTEGER,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT NOT NULL REFERENCES users(id),
    CHECK (end_date > start_date)
);

-- Tabla de participación en desafíos
CREATE TABLE challenge_participants (
    challenge_id BIGINT NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    current_progress DECIMAL(10,2) DEFAULT 0.0 NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    PRIMARY KEY (challenge_id, user_id)
);

-- Tabla de logros
CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    icon VARCHAR(10) NOT NULL,
    category VARCHAR(50) NOT NULL,
    points INTEGER NOT NULL CHECK (points > 0),
    requirement_type VARCHAR(50) NOT NULL,
    requirement_value DECIMAL(10,2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Tabla de logros desbloqueados por usuarios
CREATE TABLE user_achievements (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_id BIGINT NOT NULL REFERENCES achievements(id) ON DELETE CASCADE,
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, achievement_id)
);

-- Tabla de progreso de logros
CREATE TABLE achievement_progress (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_id BIGINT NOT NULL REFERENCES achievements(id) ON DELETE CASCADE,
    current_progress DECIMAL(10,2) DEFAULT 0.0 NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, achievement_id)
);

-- Tabla de sesiones de entrenamiento
CREATE TABLE workout_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    workout_id BIGINT NOT NULL REFERENCES workouts(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    calories_burned DECIMAL(6,2) DEFAULT 0.0 CHECK (calories_burned >= 0),
    status VARCHAR(20) DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Tabla de datos de ejercicios en sesiones
CREATE TABLE workout_session_exercises (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES workout_sessions(id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercises(id),
    sets_completed INTEGER DEFAULT 0 CHECK (sets_completed >= 0),
    reps_completed INTEGER DEFAULT 0 CHECK (reps_completed >= 0),
    weight_used DECIMAL(5,2) DEFAULT 0.0 CHECK (weight_used >= 0),
    duration_minutes INTEGER DEFAULT 0 CHECK (duration_minutes >= 0),
    notes TEXT
);

-- Tabla de registro diario de comidas
CREATE TABLE daily_food_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    food_id BIGINT NOT NULL REFERENCES foods(id),
    meal_type VARCHAR(20) NOT NULL CHECK (meal_type IN ('DESAYUNO', 'ALMUERZO', 'CENA', 'SNACK')),
    quantity DECIMAL(8,2) NOT NULL CHECK (quantity > 0),
    unit VARCHAR(20) DEFAULT 'gramos' NOT NULL,
    logged_date DATE NOT NULL,
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Tabla de registro de peso
CREATE TABLE weight_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    weight DECIMAL(5,2) NOT NULL CHECK (weight > 0),
    logged_date DATE NOT NULL,
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    notes TEXT,
    UNIQUE (user_id, logged_date)
);

-- Tabla de registro de hidratación
CREATE TABLE hydration_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    glasses_count INTEGER NOT NULL CHECK (glasses_count > 0),
    logged_date DATE NOT NULL,
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (user_id, logged_date)
);

-- Crear índices para mejorar rendimiento
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_foods_name ON foods(name);
CREATE INDEX idx_foods_active ON foods(is_active);
CREATE INDEX idx_exercises_category ON exercises(category);
CREATE INDEX idx_exercises_muscle_group ON exercises(muscle_group);
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX idx_friend_requests_receiver ON friend_requests(receiver_id);
CREATE INDEX idx_workout_sessions_user_date ON workout_sessions(user_id, start_time);
CREATE INDEX idx_daily_food_logs_user_date ON daily_food_logs(user_id, logged_date);
CREATE INDEX idx_weight_logs_user_date ON weight_logs(user_id, logged_date);

-- Crear función para actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Crear triggers para actualizar updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_foods_updated_at BEFORE UPDATE ON foods FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_exercises_updated_at BEFORE UPDATE ON exercises FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_workouts_updated_at BEFORE UPDATE ON workouts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_meals_updated_at BEFORE UPDATE ON meals FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_posts_updated_at BEFORE UPDATE ON posts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_challenges_updated_at BEFORE UPDATE ON challenges FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_workout_sessions_updated_at BEFORE UPDATE ON workout_sessions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();