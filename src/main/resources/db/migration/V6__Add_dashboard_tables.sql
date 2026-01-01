-- Add water intake tracking table
CREATE TABLE water_intake (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    glasses INTEGER NOT NULL DEFAULT 0,
    goal_glasses INTEGER DEFAULT 8,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_date (user_id, date)
);

-- Add user activities tracking table
CREATE TABLE user_activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    icon_type VARCHAR(50),
    reference_id BIGINT,
    reference_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_activities_user_created (user_id, created_at),
    INDEX idx_user_activities_type (activity_type),
    INDEX idx_user_activities_reference (reference_type, reference_id)
);

-- Add indexes for better performance
CREATE INDEX idx_water_intake_user_date ON water_intake(user_id, date);
CREATE INDEX idx_user_activities_user_recent ON user_activities(user_id, created_at DESC);