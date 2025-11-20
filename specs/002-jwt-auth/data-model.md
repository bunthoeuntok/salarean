# Data Model: JWT Authentication, Authorization and User Profile Management

**Feature**: 002-jwt-auth | **Date**: 2025-11-20 | **Phase**: 1 (Design)

## Overview

This document defines the data entities, relationships, and validation rules for JWT refresh token management and user profile features. The model extends the existing User and Session entities from 001-teacher-auth.

## Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────┐
│                         User                            │
├─────────────────────────────────────────────────────────┤
│ PK: id (UUID)                                           │
│     email (unique, not null)                            │
│     phoneNumber (unique, not null)                      │
│     passwordHash (not null)                             │
│     name (NEW - nullable)                               │
│     preferredLanguage (default: 'en')                   │
│     profilePhotoUrl (NEW - nullable)                    │
│     profilePhotoUploadedAt (NEW - nullable)             │
│     accountStatus (default: 'active')                   │
│     createdAt                                           │
│     updatedAt                                           │
└─────────────────────────────────────────────────────────┘
         │                                     │
         │ 1                                   │ 1
         │                                     │
         │ *                                   │ *
┌────────┴───────────────────┐   ┌────────────┴─────────────────┐
│       Session              │   │      RefreshToken            │
├────────────────────────────┤   ├──────────────────────────────┤
│ PK: id (UUID)              │   │ PK: id (UUID)                │
│ FK: userId (User.id)       │   │ FK: userId (User.id)         │
│     tokenJti (unique)      │   │     tokenHash (not null)     │
│     createdAt              │   │     expiresAt (not null)     │
│     lastActivityAt         │   │     hasBeenUsed (default: F) │
│     expiresAt              │   │     usedAt (nullable)        │
│     ipAddress              │   │     ipAddress (nullable)     │
│     userAgent              │   │     userAgent (nullable)     │
└────────────────────────────┘   │     createdAt                │
                                 └──────────────────────────────┘
```

## Entity Definitions

### 1. User (Extended)

**Changes**: Add name, profilePhotoUrl, profilePhotoUploadedAt fields

**Table**: `users`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK, NOT NULL | User unique identifier |
| email | VARCHAR(255) | UNIQUE, NOT NULL | User email address |
| phone_number | VARCHAR(20) | UNIQUE, NOT NULL | Cambodia phone (+855...) |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| name | VARCHAR(255) | NULLABLE | **NEW**: User full name |
| preferred_language | VARCHAR(2) | NOT NULL, DEFAULT 'en' | 'en' or 'km' |
| profile_photo_url | VARCHAR(500) | NULLABLE | **NEW**: Photo file path |
| profile_photo_uploaded_at | TIMESTAMP | NULLABLE | **NEW**: Photo upload timestamp |
| account_status | VARCHAR(20) | NOT NULL, DEFAULT 'active' | 'active', 'inactive', 'locked' |
| created_at | TIMESTAMP | NOT NULL | Account creation timestamp |
| updated_at | TIMESTAMP | NOT NULL | Last update timestamp |

**Indexes**:
- PRIMARY KEY: `id`
- UNIQUE INDEX: `email`
- UNIQUE INDEX: `phone_number`
- INDEX: `account_status` (for filtering active users)

**Validation Rules**:
- `email`: Must be valid email format (Jakarta Bean Validation `@Email`)
- `phone_number`: Must match Cambodia format `^\\+855[1-9]\\d{7,8}$`
- `password_hash`: BCrypt hash (60 characters)
- `name`: Optional, max 255 characters, trimmed
- `preferred_language`: Must be 'en' or 'km'
- `profile_photo_url`: Must be valid relative path if present (e.g., `/uploads/profile-photos/{userId}/profile.jpg`)
- `account_status`: Must be one of: 'active', 'inactive', 'locked'

**State Transitions**:
```
active -> locked (after N failed login attempts)
locked -> active (admin unlock or time-based)
active -> inactive (soft delete)
```

---

### 2. Session (No Changes)

**Table**: `sessions`

This entity already exists from 001-teacher-auth and doesn't require modifications for this feature.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK, NOT NULL | Session unique identifier |
| user_id | UUID | FK(users.id), NOT NULL | Owner user |
| token_jti | VARCHAR(255) | UNIQUE, NOT NULL | JWT ID claim (for invalidation) |
| created_at | TIMESTAMP | NOT NULL | Session start timestamp |
| last_activity_at | TIMESTAMP | NOT NULL | Last request timestamp |
| expires_at | TIMESTAMP | NOT NULL | Session expiration (24h from creation) |
| ip_address | VARCHAR(45) | NULLABLE | Client IP address (IPv4 or IPv6) |
| user_agent | TEXT | NULLABLE | Client user agent string |

**Indexes**:
- PRIMARY KEY: `id`
- UNIQUE INDEX: `token_jti`
- INDEX: `user_id` (for finding user sessions)
- INDEX: `expires_at` (for cleanup jobs)

**Relationships**:
- `user_id` → `users.id` (Many-to-One: Many sessions per user)

---

### 3. RefreshToken (New)

**Purpose**: Store refresh tokens for long-lived authentication (30 days)

**Table**: `refresh_tokens`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK, NOT NULL | Refresh token unique identifier |
| user_id | UUID | FK(users.id), NOT NULL | Owner user |
| token_hash | VARCHAR(255) | NOT NULL | BCrypt hashed token value |
| expires_at | TIMESTAMP | NOT NULL | Token expiration (30 days from creation) |
| has_been_used | BOOLEAN | NOT NULL, DEFAULT FALSE | Token rotation flag |
| used_at | TIMESTAMP | NULLABLE | When token was consumed |
| ip_address | VARCHAR(45) | NULLABLE | Client IP at token creation |
| user_agent | TEXT | NULLABLE | Client user agent at creation |
| created_at | TIMESTAMP | NOT NULL | Token creation timestamp |

**Indexes**:
- PRIMARY KEY: `id`
- INDEX: `user_id` (for finding user refresh tokens)
- INDEX: `expires_at` (for cleanup jobs)
- INDEX: `has_been_used` (for detecting replay attacks)

**Relationships**:
- `user_id` → `users.id` (Many-to-One: Many refresh tokens per user, one per device/session)

**Validation Rules**:
- `token_hash`: Must be BCrypt hash of the token UUID
- `expires_at`: Must be 30 days after `created_at`
- `has_been_used`: Once TRUE, token is invalid (replay detection)
- `used_at`: Must be set when `has_been_used` becomes TRUE

**State Transitions**:
```
created (has_been_used=FALSE) -> used (has_been_used=TRUE, used_at=NOW)
created -> expired (expires_at < NOW)
created -> deleted (logout, password change)
```

**Lifecycle**:
1. **Creation**: Generate UUID token, hash with BCrypt, store in DB + Redis
2. **Usage**: Mark `has_been_used=TRUE`, set `used_at`, create new refresh token
3. **Expiration**: Cleanup job deletes tokens where `expires_at < NOW`
4. **Invalidation**: Delete on logout or password change

---

## Redis Cache Schema

**Purpose**: Fast lookup cache for refresh token validation (avoid DB query on every token refresh)

### Key Structure

```
Key Pattern: "refresh_token:{userId}:{tokenId}"
Value: JSON object
TTL: 30 days (auto-expire with token)
```

### Example Entry

```json
{
  "tokenId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "tokenHash": "$2a$12$...",
  "expiresAt": "2025-12-20T10:30:00Z",
  "hasBeenUsed": false
}
```

### Operations

**Write** (on token creation):
```java
String key = String.format("refresh_token:%s:%s", userId, tokenId);
redisTemplate.opsForValue().set(key, tokenData, 30, TimeUnit.DAYS);
```

**Read** (on token validation):
```java
String key = String.format("refresh_token:%s:%s", userId, tokenId);
RefreshTokenData data = redisTemplate.opsForValue().get(key);
if (data == null) {
    // Fallback to PostgreSQL
    data = refreshTokenRepository.findById(tokenId);
}
```

**Delete** (on logout/invalidation):
```java
String key = String.format("refresh_token:%s:%s", userId, tokenId);
redisTemplate.delete(key);
```

---

## Database Migration Script

**File**: `auth-service/src/main/resources/db/migration/V2__add_refresh_tokens_and_profile_fields.sql`

```sql
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
```

---

## Validation Summary

### User Profile Updates

| Field | Validation | Error Message (EN) | Error Message (KM) |
|-------|------------|-------------------|-------------------|
| name | Max 255 chars, trimmed | Name too long | ឈ្មោះវែងពេក |
| phone_number | Cambodia format, unique | Invalid phone or already registered | លេខទូរស័ព្ទមិនត្រឹមត្រូវ ឬត្រូវបានចុះឈ្មោះរួចហើយ |
| preferred_language | 'en' or 'km' | Invalid language code | កូដភាសាមិនត្រឹមត្រូវ |

### Password Change

| Rule | Validation | Error Message (EN) | Error Message (KM) |
|------|------------|-------------------|-------------------|
| Length | >= 8 characters | At least 8 characters required | តម្រូវឱ្យយ៉ាងតិច 8 តួអក្សរ |
| Uppercase | >= 1 uppercase letter | At least 1 uppercase letter | យ៉ាងតិច 1 អក្សរធំ |
| Lowercase | >= 1 lowercase letter | At least 1 lowercase letter | យ៉ាងតិច 1 អក្សរតូច |
| Digit | >= 1 digit | At least 1 digit | យ៉ាងតិច 1 លេខ |
| Special | >= 1 special char | At least 1 special character | យ៉ាងតិច 1 តួអក្សរពិសេស |
| Common | Not in top 10k list | Password too common | ពាក្យសម្ងាត់ធម្មតាពេក |

### Profile Photo Upload

| Rule | Validation | Error Message (EN) | Error Message (KM) |
|------|------------|-------------------|-------------------|
| Size | <= 5MB | Photo exceeds 5MB limit | រូបភាពលើសពី 5MB |
| Format | JPEG or PNG | Only JPG/PNG allowed | អនុញ្ញាតតែ JPG/PNG |
| Content | MIME type verified | Invalid or corrupted image | រូបភាពមិនត្រឹមត្រូវ ឬខូច |

---

## Data Integrity Rules

### Cascading Deletes

```sql
-- When user deleted, cascade to related entities
ON DELETE CASCADE:
  - sessions (user_id -> users.id)
  - refresh_tokens (user_id -> users.id)
```

### Orphan Prevention

- **Session cleanup**: Job runs hourly, deletes sessions where `expires_at < NOW()`
- **Refresh token cleanup**: Job runs daily, deletes tokens where `expires_at < NOW()` OR `has_been_used = TRUE AND used_at < NOW() - INTERVAL '7 days'`

### Uniqueness Constraints

- `users.email`: UNIQUE across all users
- `users.phone_number`: UNIQUE across all users
- `sessions.token_jti`: UNIQUE across all sessions
- No uniqueness on `refresh_tokens` (multiple tokens per user allowed, one per device)

---

## Query Patterns

### Find Active Sessions for User

```sql
SELECT * FROM sessions
WHERE user_id = ?
  AND expires_at > NOW()
ORDER BY last_activity_at DESC;
```

### Find Valid Refresh Tokens for User

```sql
SELECT * FROM refresh_tokens
WHERE user_id = ?
  AND expires_at > NOW()
  AND has_been_used = FALSE
ORDER BY created_at DESC;
```

### Invalidate User Sessions (Password Change)

```sql
-- Delete all sessions except current
DELETE FROM sessions
WHERE user_id = ?
  AND token_jti != ?;

-- Delete all refresh tokens except current
DELETE FROM refresh_tokens
WHERE user_id = ?
  AND id != ?;
```

### Detect Token Replay Attack

```sql
SELECT has_been_used FROM refresh_tokens
WHERE id = ?
  AND user_id = ?;

-- If has_been_used = TRUE, possible compromise:
DELETE FROM sessions WHERE user_id = ?;
DELETE FROM refresh_tokens WHERE user_id = ?;
```

---

## Performance Considerations

### Expected Query Performance

| Query | Expected Latency | Optimization |
|-------|-----------------|--------------|
| Refresh token lookup (Redis) | < 10ms | TTL-based expiration |
| Refresh token lookup (PostgreSQL) | < 50ms | Index on `user_id` |
| Profile photo fetch | < 20ms | Filesystem read (not DB) |
| Session invalidation | < 100ms | Index on `user_id`, bulk delete |
| User profile update | < 50ms | Single row update with index |

### Scalability Limits

- **Concurrent token refreshes**: 1000 req/s (Redis bottleneck)
- **User profiles**: 100k+ users (PostgreSQL capacity)
- **Refresh tokens per user**: 10 (typical: 2-3 devices)
- **Photo storage**: 20GB for 10k users @ 2MB average

---

## Next Steps

Phase 1 will also produce:
1. ✅ **data-model.md**: Complete (this document)
2. ⏭️ **contracts/api-contracts.yaml**: OpenAPI spec for 6 endpoints
3. ⏭️ **quickstart.md**: Developer setup guide
