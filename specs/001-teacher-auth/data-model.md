# Data Model: Teacher Registration & Login

**Feature**: 001-teacher-auth
**Date**: 2025-11-20
**Database**: auth_db (PostgreSQL 15+)

## Overview

This feature introduces three new tables to the auth-service database:

1. **users** - Teacher account information (email, phone, password, preferences)
2. **login_attempts** - Login attempt tracking for rate limiting and audit trail
3. **sessions** - Active session tracking for audit and future revocation features

All tables support the functional requirements from spec.md (FR-001 through FR-015) and align with MoEYS compliance requirements for audit logging.

---

## Entity: User (Teacher Account)

**Table**: `users`
**Purpose**: Stores registered teacher accounts with authentication credentials and preferences

### Fields

| Field | Type | Constraints | Description | Maps to Spec |
|-------|------|-------------|-------------|--------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique teacher identifier | All FRs (core entity) |
| email | VARCHAR(255) | UNIQUE NOT NULL | Teacher's email address (RFC 5322 compliant) | FR-001, FR-002, FR-005 |
| phone_number | VARCHAR(20) | UNIQUE NOT NULL | Cambodia phone number (+855 format) | FR-001, FR-003, FR-006 |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt hash of password (cost factor 12) | FR-001, FR-004, FR-008 |
| preferred_language | VARCHAR(2) | DEFAULT 'en', CHECK IN ('en', 'km') | User's language preference (English/Khmer) | FR-011, FR-012, FR-013 |
| account_status | VARCHAR(20) | DEFAULT 'active', CHECK IN ('active', 'inactive', 'locked') | Account state (active, inactive, locked) | Future feature, supports admin actions |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Account creation timestamp | Audit requirement |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Last update timestamp | Audit requirement |

### Indexes

```sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone_number);
```

**Rationale**:
- Email and phone used for login (FR-007), need fast lookup
- Both columns have UNIQUE constraints, indexes improve uniqueness check performance

### Validation Rules

| Field | Validation | Error Code | Spec Reference |
|-------|------------|------------|----------------|
| email | RFC 5322 compliant regex | INVALID_EMAIL_FORMAT | FR-002 |
| phone_number | Regex: `^\+855[1-9]\d{7,8}$` | INVALID_PHONE_FORMAT | FR-003 |
| password (pre-hash) | Min 8 chars, 1 upper, 1 lower, 1 number, 1 special | INVALID_PASSWORD | FR-004 |
| email uniqueness | No duplicates allowed | DUPLICATE_EMAIL | FR-005 |
| phone_number uniqueness | No duplicates allowed | DUPLICATE_PHONE | FR-006 |
| preferred_language | Must be 'en' or 'km' | INVALID_LANGUAGE | FR-013 |

### State Transitions

```
[New Registration] → active
active → inactive (admin action, future feature)
active → locked (excessive failed logins, future feature)
inactive → active (admin reactivation, future feature)
```

**Current Implementation**: Only `active` state used for initial release. Other states reserved for future admin features.

---

## Entity: LoginAttempt

**Table**: `login_attempts`
**Purpose**: Tracks all login attempts (successful and failed) for rate limiting and audit trail

### Fields

| Field | Type | Constraints | Description | Maps to Spec |
|-------|------|-------------|-------------|--------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-incrementing attempt ID | System-generated |
| identifier | VARCHAR(255) | NOT NULL | Email or phone used in login attempt | FR-007 (login with either) |
| ip_address | VARCHAR(45) | NULL | IPv4 or IPv6 address of requester | Audit requirement, future: IP-based rate limiting |
| success | BOOLEAN | NOT NULL | True if login succeeded, false if failed | FR-015 (rate limiting logic) |
| failure_reason | VARCHAR(50) | NULL | Reason for failure (e.g., 'INVALID_PASSWORD', 'RATE_LIMITED') | Audit/debugging |
| attempted_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Timestamp of login attempt | FR-015 (15-minute window) |

### Indexes

```sql
CREATE INDEX idx_login_attempts_identifier_time ON login_attempts(identifier, attempted_at);
CREATE INDEX idx_login_attempts_attempted_at ON login_attempts(attempted_at);
```

**Rationale**:
- `idx_login_attempts_identifier_time`: Composite index for rate limiting query (count failed attempts for identifier in last 15 minutes)
- `idx_login_attempts_attempted_at`: Supports cleanup jobs (delete attempts older than 7 years for compliance)

### Rate Limiting Query

**Spec Requirement**: FR-015 - Maximum 5 failed attempts within 15 minutes before lockout

```sql
SELECT COUNT(*)
FROM login_attempts
WHERE identifier = :emailOrPhone
  AND success = false
  AND attempted_at > (NOW() - INTERVAL '15 minutes');
```

**Logic**:
- If count >= 5 → reject login attempt with `RATE_LIMIT_EXCEEDED` error
- If count < 5 → allow login attempt, insert new record

**Edge Cases**:
- Rate limit per identifier (email/phone), not per IP address (teachers may share school networks)
- Successful login does NOT reset failed attempt count (sliding window continues for audit trail)
- Window is rolling (each new attempt checks last 15 minutes from current time)

---

## Entity: Session

**Table**: `sessions`
**Purpose**: Tracks active JWT sessions for audit trail and future revocation features

### Fields

| Field | Type | Constraints | Description | Maps to Spec |
|-------|------|-------------|-------------|--------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique session identifier | System-generated |
| user_id | UUID | NOT NULL, REFERENCES users(id) ON DELETE CASCADE | Teacher who owns this session | Links to users table |
| token_jti | VARCHAR(255) | UNIQUE NOT NULL | JWT ID (jti claim) for token identification | Enables token revocation (future) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Session creation timestamp | FR-009 (session creation) |
| last_activity_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Last time this session was used | FR-010 (24-hour inactivity timeout) |
| expires_at | TIMESTAMP | NOT NULL | Session expiration timestamp (24 hours from creation) | FR-010 (24-hour timeout) |
| ip_address | VARCHAR(45) | NULL | IP address of session creation | Audit requirement |
| user_agent | TEXT | NULL | Browser/client user agent string | Audit requirement, supports "active sessions" UI |

### Indexes

```sql
CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_expires_at ON sessions(expires_at);
CREATE INDEX idx_sessions_token_jti ON sessions(token_jti);
```

**Rationale**:
- `idx_sessions_user_id`: List all sessions for a teacher (admin feature: "show active sessions")
- `idx_sessions_expires_at`: Cleanup job to delete expired sessions
- `idx_sessions_token_jti`: Token revocation lookup (future feature: logout endpoint)

### Session Lifecycle

```
[Login Success] → CREATE session (expires_at = now + 24 hours)
[API Request] → UPDATE last_activity_at (if within 24 hours)
[Inactivity Timeout] → Session marked expired (cleanup job deletes)
[Explicit Logout] → DELETE session (future feature)
```

**Current Implementation**:
- Session created on successful login
- `last_activity_at` updated on authenticated requests (async, non-blocking)
- JWT validation is stateless (no database lookup), session table used only for audit/tracking

**Future Features** (out of scope for this release):
- Logout endpoint: DELETE session by token_jti
- Admin feature: View all active sessions for a teacher
- Admin feature: Force logout (revoke specific session)

---

## Relationships

```
users (1) ──────< (many) sessions
       └─────────< (many) login_attempts (implicit, via identifier)
```

**Notes**:
- `sessions.user_id` has foreign key constraint (CASCADE delete)
- `login_attempts.identifier` does NOT have foreign key (stores email/phone even if user doesn't exist, for tracking invalid login attempts)

---

## Flyway Migrations

### V1__create_users_table.sql

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    preferred_language VARCHAR(2) DEFAULT 'en' CHECK (preferred_language IN ('en', 'km')),
    account_status VARCHAR(20) DEFAULT 'active' CHECK (account_status IN ('active', 'inactive', 'locked')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone_number);

-- Trigger to auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
```

### V2__create_login_attempts_table.sql

```sql
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
```

### V3__create_sessions_table.sql

```sql
CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_jti VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_activity_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT
);

CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_expires_at ON sessions(expires_at);
CREATE INDEX idx_sessions_token_jti ON sessions(token_jti);
```

---

## Data Retention & Cleanup

**MoEYS Compliance**: Audit records must be retained for 7 years

### Cleanup Jobs (Future Implementation)

```sql
-- Delete login attempts older than 7 years
DELETE FROM login_attempts
WHERE attempted_at < (NOW() - INTERVAL '7 years');

-- Delete expired sessions
DELETE FROM sessions
WHERE expires_at < NOW();
```

**Execution**: Periodic cron job or Spring @Scheduled task (daily at 2 AM)

---

## Sample Data (Development/Testing)

```sql
-- Test teacher account
INSERT INTO users (id, email, phone_number, password_hash, preferred_language)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'test.teacher@school.edu.kh',
    '+85512345678',
    '$2a$12$K.7eXXV.b3Zf0g7F9h4J3O7yF4ZqJxG0L8V2Qz0Yf3H8L4X2V1N0S',  -- BCrypt hash of "Test1234!"
    'en'
);

-- Sample login attempts (failed and successful)
INSERT INTO login_attempts (identifier, ip_address, success, failure_reason, attempted_at)
VALUES
    ('test.teacher@school.edu.kh', '192.168.1.100', false, 'INVALID_PASSWORD', NOW() - INTERVAL '20 minutes'),
    ('test.teacher@school.edu.kh', '192.168.1.100', false, 'INVALID_PASSWORD', NOW() - INTERVAL '10 minutes'),
    ('test.teacher@school.edu.kh', '192.168.1.100', true, NULL, NOW() - INTERVAL '5 minutes');
```

---

## Validation Enforcement

### Application Layer (Spring Boot)

**Entity Validation** (Bean Validation):
```java
@Entity
@Table(name = "users")
public class User {
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;

    @Pattern(regexp = "^\\+855[1-9]\\d{7,8}$", message = "Invalid Cambodia phone format")
    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    // Password validation done pre-hash in service layer
    // ...
}
```

**Service Layer Validation**:
- Password strength (FR-004): Custom validator before hashing
- Duplicate email/phone (FR-005, FR-006): JPA UniqueConstraintException handling
- Rate limiting (FR-015): Query login_attempts before allowing login

---

## Performance Considerations

**Expected Load**: 1000+ teachers total, ~100 concurrent login requests (spec SC-009)

**Query Performance**:
- Email/phone lookups: O(log n) with B-tree indexes, sub-millisecond for 10k users
- Rate limiting query: O(log n) with composite index, sub-millisecond
- Session lookups (future): O(log n) with token_jti index

**Database Sizing** (estimated for 1000 teachers):
- users table: ~1000 rows × ~500 bytes = 500 KB
- login_attempts table: ~10 attempts/teacher/year × 7 years = 70,000 rows × ~200 bytes = 14 MB
- sessions table: ~2 active sessions/teacher average = 2000 rows × ~300 bytes = 600 KB

**Total storage**: < 20 MB (negligible for PostgreSQL)

---

## Security Considerations

1. **Password Storage**: Never store plaintext passwords, only BCrypt hashes (FR-008)
2. **PII Protection**: Email and phone are teacher PII, encrypted in transit via HTTPS
3. **SQL Injection**: Prevented via JPA parameterized queries (Hibernate)
4. **Rate Limiting**: Prevents brute-force attacks via login_attempts tracking (FR-015)
5. **Audit Trail**: All login attempts logged for MoEYS compliance (7-year retention)

---

## Mapping to Spec Requirements

| Spec Requirement | Database Implementation |
|------------------|-------------------------|
| FR-001: Register with email, phone, password | users table with email, phone_number, password_hash columns |
| FR-002: Email validation | Application-layer regex + database UNIQUE constraint |
| FR-003: Cambodia phone validation | Application-layer regex `^\+855[1-9]\d{7,8}$` + UNIQUE constraint |
| FR-004: Password strength | Application-layer validation before BCrypt hashing |
| FR-005: Prevent duplicate emails | users.email UNIQUE constraint |
| FR-006: Prevent duplicate phones | users.phone_number UNIQUE constraint |
| FR-007: Login with email OR phone | Query users by email OR phone_number |
| FR-008: Secure password storage | password_hash column with BCrypt (cost 12) |
| FR-009: Session persistence | sessions table with created_at, expires_at, last_activity_at |
| FR-010: 24-hour session timeout | sessions.expires_at = created_at + 24 hours |
| FR-011: Bilingual errors | users.preferred_language ('en' or 'km') |
| FR-012: Language selection during registration | users.preferred_language set on INSERT |
| FR-013: Default to browser language | Application layer sets preferred_language from Accept-Language header |
| FR-014: Clear error messages | Handled in application layer via i18n |
| FR-015: Rate limiting (5 attempts/15 min) | login_attempts table with sliding window query |

---

## Summary

Three normalized tables support all teacher authentication requirements:

1. **users**: Core teacher accounts (1000+ rows expected)
2. **login_attempts**: Rate limiting and audit trail (70k+ rows over 7 years)
3. **sessions**: Active session tracking (2000 rows average)

All tables indexed for performance, support MoEYS compliance (7-year audit retention), and align with Spring Boot/JPA best practices. No complex queries or joins required for core auth flows.
