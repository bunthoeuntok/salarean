-- Add profile fields to users table
ALTER TABLE users
    ADD COLUMN name VARCHAR(255),
    ADD COLUMN profile_photo_url VARCHAR(500),
    ADD COLUMN profile_photo_uploaded_at TIMESTAMP;

-- Create refresh_tokens table
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    has_been_used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Indexes for refresh_tokens
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_has_been_used ON refresh_tokens(has_been_used);

-- Comments for documentation
COMMENT ON COLUMN users.name IS 'User full name (optional, for profile display)';
COMMENT ON COLUMN users.profile_photo_url IS 'Relative path to profile photo file';
COMMENT ON COLUMN users.profile_photo_uploaded_at IS 'Timestamp when current photo was uploaded';

COMMENT ON TABLE refresh_tokens IS 'Long-lived tokens (30d) for obtaining new access tokens';
COMMENT ON COLUMN refresh_tokens.token_hash IS 'BCrypt hash of the actual token UUID (never store plain token)';
COMMENT ON COLUMN refresh_tokens.has_been_used IS 'One-time use flag for token rotation and replay detection';
COMMENT ON COLUMN refresh_tokens.used_at IS 'Timestamp when token was consumed (for audit trail)';
