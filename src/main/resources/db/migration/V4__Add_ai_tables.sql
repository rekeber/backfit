-- AI Integration Tables for FitLife

-- Food Image Analysis Table
CREATE TABLE food_image_analyses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    original_filename VARCHAR(255),
    detected_foods TEXT,
    confidence_score DECIMAL(3,2),
    estimated_calories INTEGER,
    estimated_protein DECIMAL(8,2),
    estimated_carbs DECIMAL(8,2),
    estimated_fat DECIMAL(8,2),
    ai_analysis TEXT,
    processing_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- AI Recommendations Table
CREATE TABLE ai_recommendations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    coaching_session_id BIGINT REFERENCES ai_coaching_sessions(id) ON DELETE SET NULL,
    recommendation_type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    confidence_score DECIMAL(3,2),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_applied BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- AI Coaching Sessions Table
CREATE TABLE ai_coaching_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_type VARCHAR(20) NOT NULL,
    user_message TEXT,
    ai_response TEXT,
    context_data TEXT,
    sentiment_score DECIMAL(3,2),
    satisfaction_rating INTEGER CHECK (satisfaction_rating >= 1 AND satisfaction_rating <= 5),
    session_duration_seconds INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add image_analysis_id to food_entries table
ALTER TABLE food_entries 
ADD COLUMN image_analysis_id BIGINT REFERENCES food_image_analyses(id) ON DELETE SET NULL;

-- Create indexes for better performance
CREATE INDEX idx_food_image_analyses_user_id ON food_image_analyses(user_id);
CREATE INDEX idx_food_image_analyses_status ON food_image_analyses(processing_status);
CREATE INDEX idx_food_image_analyses_created_at ON food_image_analyses(created_at);

CREATE INDEX idx_ai_recommendations_user_id ON ai_recommendations(user_id);
CREATE INDEX idx_ai_recommendations_coaching_session_id ON ai_recommendations(coaching_session_id);
CREATE INDEX idx_ai_recommendations_type ON ai_recommendations(recommendation_type);
CREATE INDEX idx_ai_recommendations_priority ON ai_recommendations(priority);
CREATE INDEX idx_ai_recommendations_is_read ON ai_recommendations(is_read);
CREATE INDEX idx_ai_recommendations_expires_at ON ai_recommendations(expires_at);
CREATE INDEX idx_ai_recommendations_created_at ON ai_recommendations(created_at);

CREATE INDEX idx_ai_coaching_sessions_user_id ON ai_coaching_sessions(user_id);
CREATE INDEX idx_ai_coaching_sessions_type ON ai_coaching_sessions(session_type);
CREATE INDEX idx_ai_coaching_sessions_created_at ON ai_coaching_sessions(created_at);

CREATE INDEX idx_food_entries_image_analysis_id ON food_entries(image_analysis_id);

-- Add some sample AI recommendations for existing users
INSERT INTO ai_recommendations (user_id, recommendation_type, title, content, priority, confidence_score, expires_at)
SELECT 
    u.id,
    'WELLNESS_MOTIVATION',
    '¡Bienvenido a FitLife AI! 🤖',
    '¡Hola! Soy tu nuevo coach de IA personalizado. Estoy aquí para ayudarte a alcanzar tus objetivos de pérdida de peso de manera inteligente y personalizada. 

🍽️ **Análisis de Comidas**: Toma fotos de tus comidas y te daré análisis nutricional instantáneo
🤖 **Chat Inteligente**: Pregúntame cualquier cosa sobre nutrición y fitness
📊 **Recomendaciones Personalizadas**: Recibirás consejos adaptados a tu progreso
💪 **Motivación Diaria**: Te mantendré motivado con mensajes personalizados

¡Empecemos este viaje juntos hacia una versión más saludable de ti! 🌟',
    'HIGH',
    0.95,
    CURRENT_TIMESTAMP + INTERVAL '7 days'
FROM users u
WHERE u.id <= 5; -- Solo para los primeros 5 usuarios como ejemplo

-- Add trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_food_image_analyses_updated_at 
    BEFORE UPDATE ON food_image_analyses 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ai_recommendations_updated_at 
    BEFORE UPDATE ON ai_recommendations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ai_coaching_sessions_updated_at 
    BEFORE UPDATE ON ai_coaching_sessions 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();