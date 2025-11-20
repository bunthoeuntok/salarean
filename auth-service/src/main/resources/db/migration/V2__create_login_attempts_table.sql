CREATE TABLE login_attempts (
    id BIGSERIAL PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45),
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(50),
    attempted_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_login_attempts_identifier_time ON login_attempts(identifier, attempted_at);
CREATE INDEX idx_login_attempts_attempted_at ON login_attempts(attempted_at);
