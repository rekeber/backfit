-- Creator Economy & Advanced Gamification Tables for FitLife

-- Creators Table
CREATE TABLE creators (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    creator_handle VARCHAR(50) UNIQUE NOT NULL,
    tier VARCHAR(20) NOT NULL DEFAULT 'BRONZE',
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    follower_count BIGINT NOT NULL DEFAULT 0,
    total_views BIGINT NOT NULL DEFAULT 0,
    total_earnings DECIMAL(10,2) DEFAULT 0.00,
    monthly_earnings DECIMAL(10,2) DEFAULT 0.00,
    revenue_share_percentage DECIMAL(5,2) DEFAULT 70.00,
    specialties TEXT,
    bio TEXT,
    website_url VARCHAR(500),
    instagram_handle VARCHAR(100),
    youtube_channel VARCHAR(100),
    tiktok_handle VARCHAR(100),
    average_rating DECIMAL(3,2) DEFAULT 0.00,
    total_ratings BIGINT NOT NULL DEFAULT 0,
    content_count BIGINT NOT NULL DEFAULT 0,
    engagement_rate DECIMAL(5,2) DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    application_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    application_date TIMESTAMP,
    approval_date TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Creator Content Table
CREATE TABLE creator_contents (
    id BIGSERIAL PRIMARY KEY,
    creator_id BIGINT NOT NULL REFERENCES creators(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    content_type VARCHAR(20) NOT NULL,
    category VARCHAR(20) NOT NULL,
    media_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    duration_seconds INTEGER,
    difficulty_level VARCHAR(20),
    tags TEXT,
    view_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    comment_count BIGINT NOT NULL DEFAULT 0,
    share_count BIGINT NOT NULL DEFAULT 0,
    save_count BIGINT NOT NULL DEFAULT 0,
    earnings DECIMAL(10,2) DEFAULT 0.00,
    is_monetized BOOLEAN NOT NULL DEFAULT FALSE,
    is_premium BOOLEAN NOT NULL DEFAULT FALSE,
    premium_price DECIMAL(8,2),
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP,
    moderation_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    moderation_notes TEXT,
    ai_analysis TEXT,
    target_audience TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Brands Table
CREATE TABLE brands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    brand_handle VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    logo_url VARCHAR(500),
    website_url VARCHAR(500),
    industry VARCHAR(30) NOT NULL,
    company_size VARCHAR(20),
    contact_email VARCHAR(200) NOT NULL,
    contact_phone VARCHAR(50),
    contact_person VARCHAR(200),
    monthly_budget DECIMAL(12,2),
    total_spent DECIMAL(12,2) DEFAULT 0.00,
    target_demographics TEXT,
    brand_values TEXT,
    preferred_content_types TEXT,
    blacklisted_keywords TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    account_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verification_date TIMESTAMP,
    total_campaigns BIGINT NOT NULL DEFAULT 0,
    successful_campaigns BIGINT NOT NULL DEFAULT 0,
    average_roi DECIMAL(5,2) DEFAULT 0.00,
    brand_rating DECIMAL(3,2) DEFAULT 0.00,
    total_ratings BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Brand Collaborations Table
CREATE TABLE brand_collaborations (
    id BIGSERIAL PRIMARY KEY,
    brand_id BIGINT NOT NULL REFERENCES brands(id) ON DELETE CASCADE,
    creator_id BIGINT NOT NULL REFERENCES creators(id) ON DELETE CASCADE,
    content_id BIGINT REFERENCES creator_contents(id) ON DELETE SET NULL,
    collaboration_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PROPOSED',
    title VARCHAR(200) NOT NULL,
    description TEXT,
    requirements TEXT,
    deliverables TEXT,
    budget DECIMAL(10,2) NOT NULL,
    creator_fee DECIMAL(10,2),
    platform_fee DECIMAL(10,2),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    deadline TIMESTAMP,
    target_metrics TEXT,
    actual_metrics TEXT,
    brand_rating INTEGER CHECK (brand_rating >= 1 AND brand_rating <= 5),
    creator_rating INTEGER CHECK (creator_rating >= 1 AND creator_rating <= 5),
    brand_feedback TEXT,
    creator_feedback TEXT,
    contract_terms TEXT,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_date TIMESTAMP,
    ai_match_score DECIMAL(5,2),
    performance_bonus DECIMAL(8,2) DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- FitCoins Table (Virtual Currency)
CREATE TABLE fitcoins (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    balance_after DECIMAL(10,2) NOT NULL,
    source VARCHAR(30) NOT NULL,
    description VARCHAR(500),
    reference_id BIGINT,
    reference_type VARCHAR(50),
    multiplier DECIMAL(3,2) DEFAULT 1.00,
    expires_at TIMESTAMP,
    is_bonus BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- User Avatars Table
CREATE TABLE user_avatars (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    avatar_name VARCHAR(100),
    body_type VARCHAR(20),
    skin_tone VARCHAR(20),
    hair_style VARCHAR(20),
    hair_color VARCHAR(20),
    eye_color VARCHAR(20),
    current_outfit TEXT,
    unlocked_outfits TEXT,
    unlocked_accessories TEXT,
    current_accessories TEXT,
    fitness_level VARCHAR(20) NOT NULL DEFAULT 'BEGINNER',
    muscle_definition INTEGER DEFAULT 1 CHECK (muscle_definition >= 1 AND muscle_definition <= 10),
    body_fat_percentage DECIMAL(5,2),
    avatar_3d_model_url VARCHAR(500),
    future_projection_url VARCHAR(500),
    transformation_progress INTEGER DEFAULT 0 CHECK (transformation_progress >= 0 AND transformation_progress <= 100),
    total_workouts_completed BIGINT DEFAULT 0,
    total_calories_burned BIGINT DEFAULT 0,
    total_weight_lost DECIMAL(5,2) DEFAULT 0.0,
    achievements_displayed TEXT,
    last_body_scan_date TIMESTAMP,
    next_evolution_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Loot Boxes Table
CREATE TABLE loot_boxes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    box_type VARCHAR(20) NOT NULL,
    rarity VARCHAR(20) NOT NULL,
    is_opened BOOLEAN NOT NULL DEFAULT FALSE,
    opened_at TIMESTAMP,
    earned_from VARCHAR(30),
    contents TEXT,
    fitcoins_reward DECIMAL(10,2),
    fitgems_reward INTEGER,
    avatar_items TEXT,
    special_rewards TEXT,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Content Interactions Tables
CREATE TABLE content_likes (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES creator_contents(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(content_id, user_id)
);

CREATE TABLE content_comments (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES creator_contents(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    comment_text TEXT NOT NULL,
    parent_comment_id BIGINT REFERENCES content_comments(id) ON DELETE CASCADE,
    like_count BIGINT DEFAULT 0,
    is_pinned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE content_views (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES creator_contents(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    view_duration_seconds INTEGER,
    completion_percentage DECIMAL(5,2),
    device_type VARCHAR(20),
    ip_address INET,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Creator Earnings Table
CREATE TABLE creator_earnings (
    id BIGSERIAL PRIMARY KEY,
    creator_id BIGINT NOT NULL REFERENCES creators(id) ON DELETE CASCADE,
    collaboration_id BIGINT REFERENCES brand_collaborations(id) ON DELETE SET NULL,
    content_id BIGINT REFERENCES creator_contents(id) ON DELETE SET NULL,
    earning_type VARCHAR(30) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_date TIMESTAMP,
    payment_method VARCHAR(50),
    transaction_id VARCHAR(100),
    tax_withheld DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Ad Campaigns Table (for brands)
CREATE TABLE ad_campaigns (
    id BIGSERIAL PRIMARY KEY,
    brand_id BIGINT NOT NULL REFERENCES brands(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    campaign_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    budget DECIMAL(12,2) NOT NULL,
    spent_amount DECIMAL(12,2) DEFAULT 0.00,
    target_demographics TEXT,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    impressions BIGINT DEFAULT 0,
    clicks BIGINT DEFAULT 0,
    conversions BIGINT DEFAULT 0,
    ctr DECIMAL(5,4) DEFAULT 0.0000,
    cpc DECIMAL(8,4) DEFAULT 0.0000,
    cpm DECIMAL(8,4) DEFAULT 0.0000,
    roi DECIMAL(8,4) DEFAULT 0.0000,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- User Wallet Table (for FitCoins and FitGems)
CREATE TABLE user_wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    fitcoins_balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    fitgems_balance INTEGER NOT NULL DEFAULT 0,
    lifetime_fitcoins_earned DECIMAL(12,2) DEFAULT 0.00,
    lifetime_fitcoins_spent DECIMAL(12,2) DEFAULT 0.00,
    lifetime_fitgems_earned INTEGER DEFAULT 0,
    lifetime_fitgems_spent INTEGER DEFAULT 0,
    last_daily_spin TIMESTAMP,
    daily_spin_streak INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_creators_user_id ON creators(user_id);
CREATE INDEX idx_creators_handle ON creators(creator_handle);
CREATE INDEX idx_creators_tier ON creators(tier);
CREATE INDEX idx_creators_verified ON creators(is_verified);
CREATE INDEX idx_creators_follower_count ON creators(follower_count);

CREATE INDEX idx_creator_contents_creator_id ON creator_contents(creator_id);
CREATE INDEX idx_creator_contents_type ON creator_contents(content_type);
CREATE INDEX idx_creator_contents_category ON creator_contents(category);
CREATE INDEX idx_creator_contents_published ON creator_contents(is_published);
CREATE INDEX idx_creator_contents_views ON creator_contents(view_count);

CREATE INDEX idx_brands_handle ON brands(brand_handle);
CREATE INDEX idx_brands_industry ON brands(industry);
CREATE INDEX idx_brands_verified ON brands(is_verified);
CREATE INDEX idx_brands_status ON brands(account_status);

CREATE INDEX idx_brand_collaborations_brand_id ON brand_collaborations(brand_id);
CREATE INDEX idx_brand_collaborations_creator_id ON brand_collaborations(creator_id);
CREATE INDEX idx_brand_collaborations_status ON brand_collaborations(status);
CREATE INDEX idx_brand_collaborations_payment_status ON brand_collaborations(payment_status);

CREATE INDEX idx_fitcoins_user_id ON fitcoins(user_id);
CREATE INDEX idx_fitcoins_transaction_type ON fitcoins(transaction_type);
CREATE INDEX idx_fitcoins_source ON fitcoins(source);
CREATE INDEX idx_fitcoins_created_at ON fitcoins(created_at);

CREATE INDEX idx_user_avatars_user_id ON user_avatars(user_id);
CREATE INDEX idx_user_avatars_fitness_level ON user_avatars(fitness_level);

CREATE INDEX idx_loot_boxes_user_id ON loot_boxes(user_id);
CREATE INDEX idx_loot_boxes_type ON loot_boxes(box_type);
CREATE INDEX idx_loot_boxes_opened ON loot_boxes(is_opened);
CREATE INDEX idx_loot_boxes_expires_at ON loot_boxes(expires_at);

CREATE INDEX idx_content_likes_content_id ON content_likes(content_id);
CREATE INDEX idx_content_likes_user_id ON content_likes(user_id);

CREATE INDEX idx_content_comments_content_id ON content_comments(content_id);
CREATE INDEX idx_content_comments_user_id ON content_comments(user_id);

CREATE INDEX idx_content_views_content_id ON content_views(content_id);
CREATE INDEX idx_content_views_user_id ON content_views(user_id);

CREATE INDEX idx_creator_earnings_creator_id ON creator_earnings(creator_id);
CREATE INDEX idx_creator_earnings_status ON creator_earnings(status);

CREATE INDEX idx_user_wallets_user_id ON user_wallets(user_id);

-- Add triggers for updated_at timestamps
CREATE TRIGGER update_creators_updated_at 
    BEFORE UPDATE ON creators 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_creator_contents_updated_at 
    BEFORE UPDATE ON creator_contents 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_brands_updated_at 
    BEFORE UPDATE ON brands 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_brand_collaborations_updated_at 
    BEFORE UPDATE ON brand_collaborations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_avatars_updated_at 
    BEFORE UPDATE ON user_avatars 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_content_comments_updated_at 
    BEFORE UPDATE ON content_comments 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ad_campaigns_updated_at 
    BEFORE UPDATE ON ad_campaigns 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_wallets_updated_at 
    BEFORE UPDATE ON user_wallets 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Initialize user wallets and avatars for existing users
INSERT INTO user_wallets (user_id, fitcoins_balance, fitgems_balance)
SELECT id, 100.00, 5 FROM users 
WHERE id NOT IN (SELECT user_id FROM user_wallets);

INSERT INTO user_avatars (user_id, avatar_name, fitness_level)
SELECT id, CONCAT('Avatar_', id), 'BEGINNER' FROM users 
WHERE id NOT IN (SELECT user_id FROM user_avatars);

-- Sample creator data
INSERT INTO creators (user_id, creator_handle, tier, is_verified, follower_count, bio, specialties)
SELECT 
    u.id,
    CONCAT('@', LOWER(REPLACE(u.name, ' ', '_'))),
    'BRONZE',
    FALSE,
    FLOOR(RANDOM() * 1000),
    CONCAT('Fitness enthusiast helping others achieve their goals! 💪'),
    '["nutrition", "strength_training"]'
FROM users u
WHERE u.id <= 3; -- Solo para los primeros 3 usuarios como ejemplo

-- Sample loot boxes for users
INSERT INTO loot_boxes (user_id, box_type, rarity, earned_from, fitcoins_reward, fitgems_reward, expires_at)
SELECT 
    u.id,
    'DAILY',
    'COMMON',
    'DAILY_LOGIN',
    25.00,
    1,
    CURRENT_TIMESTAMP + INTERVAL '1 day'
FROM users u
WHERE u.id <= 5;