# Research: Teacher Registration & Login

**Feature**: 001-teacher-auth
**Date**: 2025-11-20
**Status**: Complete

## Research Objectives

This research phase addresses best practices and design decisions for implementing teacher authentication in a Spring Boot microservices architecture. Key areas investigated:

1. Password hashing with BCrypt (cost factor optimization)
2. JWT token implementation for session management
3. Rate limiting strategies for brute-force protection
4. Cambodia phone number validation patterns
5. Internationalization (i18n) for bilingual error messages
6. Standardized API response format design
7. Database schema design for auth tables

## 1. BCrypt Password Hashing

### Decision: Use BCrypt with cost factor 12

**Rationale**:
- BCrypt is industry standard for password hashing (OWASP recommended)
- Cost factor 12 provides strong security (2^12 = 4096 rounds) while maintaining acceptable performance
- Spring Security provides built-in BCryptPasswordEncoder
- Constitution principle II (Security-First) mandates secure password storage

**Implementation**:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

**Performance**: Cost factor 12 takes ~100-200ms to hash on modern hardware, acceptable for registration and login flows (spec allows <5 seconds for login).

**Alternatives Considered**:
- **Argon2**: More modern but requires external dependency (not Spring Boot default)
- **PBKDF2**: Supported but less resistant to GPU attacks than BCrypt
- **SCrypt**: Memory-hard algorithm but not Spring Security default

**References**: OWASP Password Storage Cheat Sheet, Spring Security documentation

---

## 2. JWT Token Management

### Decision: Use JJWT 0.12.5 with HS256 algorithm, 24-hour expiration

**Rationale**:
- JJWT is robust, well-maintained Java JWT library (already in project dependencies)
- HS256 (HMAC with SHA-256) provides symmetric signing (faster than RSA for single-service auth)
- 24-hour expiration matches spec requirement (FR-010)
- Tokens stored client-side (stateless), session metadata in database/Redis for tracking

**Implementation Strategy**:
```java
String token = Jwts.builder()
    .setSubject(user.getId().toString())
    .setIssuedAt(new Date())
    .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
    .signWith(SignatureAlgorithm.HS256, jwtSecret)
    .compact();
```

**Token Claims**:
- `sub`: User ID
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp (24 hours)
- `roles`: User roles (future-proofing for RBAC)
- `lang`: Preferred language (Khmer/English)

**Alternatives Considered**:
- **OAuth2/OpenID Connect**: Overkill for initial teacher-only auth, adds complexity
- **Session cookies**: Stateful approach conflicts with microservices scalability
- **RS256 (RSA)**: Public/private key approach unnecessary for single auth-service

**Security Notes**:
- JWT_SECRET must be strong (minimum 256 bits, from environment variable)
- Tokens cannot be revoked (by design); use short expiration + refresh tokens in future
- HTTPS required to prevent token interception (nginx already configured)

**References**: RFC 7519 (JWT Standard), JJWT documentation

---

## 3. Rate Limiting for Brute-Force Protection

### Decision: Database-backed rate limiting with sliding window (5 attempts per 15 minutes)

**Rationale**:
- Spec FR-015 requires 5 failed attempts per 15 minutes before lockout
- Database approach allows rate limit tracking across service restarts
- Sliding window is more precise than fixed window (prevents burst attacks at window boundaries)
- Simple implementation using PostgreSQL timestamps

**Implementation Strategy**:
```sql
CREATE TABLE login_attempts (
    id BIGSERIAL PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL,  -- email or phone
    ip_address VARCHAR(45),             -- IPv4/IPv6
    success BOOLEAN NOT NULL,
    attempted_at TIMESTAMP NOT NULL DEFAULT NOW(),
    INDEX idx_identifier_time (identifier, attempted_at)
);
```

Algorithm:
1. On login attempt, count failed attempts for identifier in last 15 minutes
2. If count >= 5, reject with "too many attempts" error
3. On successful login, optionally clear old attempts (or leave for audit trail)

**Query**:
```sql
SELECT COUNT(*) FROM login_attempts
WHERE identifier = ?
  AND success = false
  AND attempted_at > (NOW() - INTERVAL '15 minutes');
```

**Alternatives Considered**:
- **Redis-based rate limiting**: Faster but adds Redis dependency (Redis already in stack for caching, could migrate later)
- **Bucket4j library**: External dependency, overkill for simple 5-attempt limit
- **Spring Security's built-in rate limiting**: Less flexible, harder to customize

**Edge Cases**:
- Lockout per identifier (email/phone), not per IP (teachers may share networks)
- Temporary lockout (15-minute window rolls, no permanent ban)
- Audit trail: Keep all attempts for MoEYS compliance (7-year retention)

**References**: OWASP Authentication Cheat Sheet (Brute-Force Prevention)

---

## 4. Cambodia Phone Number Validation

### Decision: Regex pattern `^\+855[1-9]\d{7,8}$`

**Rationale**:
- Cambodia country code: +855
- Mobile numbers: 8-9 digits after country code (e.g., +855 12 345 678 or +855 96 123 4567)
- First digit after +855 is 1-9 (never 0)
- Spec FR-003 specifies +855 XX XXX XXX format

**Implementation**:
```java
@Pattern(regexp = "^\\+855[1-9]\\d{7,8}$",
         message = "Phone number must be in Cambodia format (+855 XX XXX XXX)")
private String phoneNumber;
```

**Validation Steps**:
1. Client-side: Format input with spaces for readability (+855 12 345 678)
2. Server-side: Strip spaces, validate regex, normalize to E.164 format (+85512345678)
3. Store normalized format in database (easier for uniqueness checks)

**Test Cases**:
- ✅ Valid: `+85512345678`, `+855961234567`
- ❌ Invalid: `855123456` (missing +), `+8550012345` (starts with 0), `+8551234` (too short)

**Alternatives Considered**:
- **libphonenumber (Google)**: Comprehensive library but heavy dependency for single-country validation
- **Manual parsing**: Error-prone, regex is sufficient for Cambodia-only

**References**: Cambodia telecom number structure (Cellcard, Smart, Metfone)

---

## 5. Internationalization (i18n) for Bilingual Errors

### Decision: Frontend-handled i18n with English-only error codes from backend

**Rationale**:
- **Backend returns machine-readable error codes** (ErrorCode enum) - language-agnostic
- **Frontend handles all translations** (Khmer/English) based on user preference
- Simplifies backend implementation (no locale parsing, no message files)
- Provides frontend flexibility to add languages without backend changes
- Follows modern SPA best practices (Next.js/React i18n libraries handle translations)

**Backend Implementation**:
```java
// Backend returns error codes only (no error messages)
return BaseResponse.error(ErrorCode.DUPLICATE_EMAIL);
```

**Frontend Implementation** (example with React i18next):
```javascript
// Frontend maps error codes to translations
const errorMessages = {
  en: {
    DUPLICATE_EMAIL: "This email is already registered",
    DUPLICATE_PHONE: "This phone number is already registered",
    INVALID_PASSWORD: "Password must be at least 8 characters with uppercase, lowercase, number, and special character"
  },
  km: {
    DUPLICATE_EMAIL: "អ៊ីមែលនេះត្រូវបានចុះឈ្មោះរួចហើយ",
    DUPLICATE_PHONE: "លេខទូរស័ព្ទនេះត្រូវបានចុះឈ្មោះរួចហើយ",
    INVALID_PASSWORD: "ពាក្យសម្ងាត់ត្រូវមានយ៉ាងហោចណាស់ 8 តួអក្សរ..."
  }
};

// Frontend uses error code to display localized message
const localizedMessage = errorMessages[userLanguage][response.errorCode];
```

**Benefits**:
- Backend codebase remains language-agnostic
- No Accept-Language header parsing needed
- Frontend can A/B test different phrasings without backend deploy
- Easier to add new languages (only frontend changes)
- Error codes provide machine-readable integration for API clients

**Alternatives Considered**:
- **Backend i18n (Spring MessageSource)**: Adds complexity, couples translations to backend deployments
- **Database-stored translations**: Requires additional infrastructure, overkill for error messages

**References**: Modern SPA architecture patterns, REST API best practices

---

## 6. Standardized API Response Format

### Decision: Generic BaseResponse<T> wrapper with error enums

**Rationale**:
- User input specifies: `{errorCode: ErrorEnum, data: T}`
- Consistent response format across all API endpoints improves client integration
- Error codes enable machine-readable error handling (e.g., frontend can switch on error code)
- No errorMessage field needed - frontend handles all translations based on error code

**Implementation**:
```java
public class BaseResponse<T> {
    private ErrorCode errorCode;
    private T data;

    // Success response
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ErrorCode.SUCCESS, data);
    }

    // Error response
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode, null);
    }
}

public enum ErrorCode {
    SUCCESS,
    DUPLICATE_EMAIL,
    DUPLICATE_PHONE,
    INVALID_PASSWORD,
    INVALID_PHONE_FORMAT,
    INVALID_EMAIL_FORMAT,
    INVALID_CREDENTIALS,
    RATE_LIMIT_EXCEEDED,
    SESSION_EXPIRED,
    INTERNAL_ERROR
}
```

**Example Responses**:

Success (registration):
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "email": "teacher@school.edu.kh",
    "phone": "+85512345678",
    "language": "km",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

Error (duplicate email):
```json
{
  "errorCode": "DUPLICATE_EMAIL",
  "data": null
}
```

**Benefits**:
- Frontend can check `errorCode === 'SUCCESS'` for success detection
- Error codes provide machine-readable integration for API clients
- Frontend translates error codes to localized messages (Khmer/English)
- Type-safe data payload (`BaseResponse<AuthResponse>`)
- Minimal response size (no redundant error message strings)

**Alternatives Considered**:
- **HTTP status codes only**: Less descriptive for client-side error handling
- **Problem Details RFC 7807**: Overly complex for simple auth errors
- **GraphQL errors**: Different paradigm, not REST

**References**: Industry best practices for REST API design

---

## 7. Database Schema Design

### Decision: Three tables - users, login_attempts, sessions

**Rationale**:
- **users**: Core teacher account data (email, phone, hashed password, preferences)
- **login_attempts**: Rate limiting and audit trail (FR-015, MoEYS compliance)
- **sessions**: Optional session tracking (could use JWT-only for stateless, or track for audit/revocation)

**Schema**:

```sql
-- users table (teachers)
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

-- login_attempts table (rate limiting + audit)
CREATE TABLE login_attempts (
    id BIGSERIAL PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL,  -- email or phone
    ip_address VARCHAR(45),
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(50),         -- e.g., 'INVALID_PASSWORD', 'RATE_LIMITED'
    attempted_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_login_attempts_identifier_time ON login_attempts(identifier, attempted_at);
CREATE INDEX idx_login_attempts_attempted_at ON login_attempts(attempted_at);  -- For cleanup jobs

-- sessions table (optional, for tracking active sessions)
CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_jti VARCHAR(255) UNIQUE NOT NULL,  -- JWT ID (jti claim)
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_activity_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT
);

CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_expires_at ON sessions(expires_at);  -- For cleanup jobs
```

**Design Decisions**:

1. **UUID for user IDs**: Prevents enumeration attacks, better for distributed systems
2. **Separate email/phone columns**: Unique constraints on both, easier to query for login
3. **password_hash VARCHAR(255)**: BCrypt produces 60-char hash, 255 allows for future algorithm changes
4. **preferred_language**: Defaults to English, supports Khmer
5. **account_status**: Supports future features (admin can lock accounts, soft delete)
6. **login_attempts.identifier**: Stores email OR phone (whichever was used for login attempt)
7. **sessions.token_jti**: JWT ID claim for potential token revocation (future feature)

**Migration Strategy**:
- Use Flyway for versioned migrations (Spring Boot standard)
- V1: Create users table
- V2: Create login_attempts table
- V3: Create sessions table

**Alternatives Considered**:
- **Single auth_events table**: Mixing users and login attempts is less normalized
- **Redis for sessions**: Faster but less durable (acceptable for stateless JWT-only approach)
- **MongoDB**: NoSQL unnecessary for structured auth data

**References**: PostgreSQL documentation, database normalization best practices

---

## 8. Session Management Strategy

### Decision: Hybrid approach - JWT tokens + database session tracking

**Rationale**:
- **JWT tokens**: Stateless, client-side storage, fast validation (no database lookup per request)
- **Database session tracking**: Enables session revocation, audit trail, concurrent session limits (future features)
- Balances performance (stateless JWT) with control (session tracking)

**Flow**:
1. **Login**: Create JWT token + insert session record in database
2. **Request validation**: Validate JWT signature/expiration (no DB lookup)
3. **Session tracking**: Update `last_activity_at` on authenticated requests (async, non-blocking)
4. **Logout**: Mark session as expired in database (future feature)
5. **Cleanup**: Periodic job deletes expired sessions from database

**Advantages**:
- JWT enables stateless request validation (fast)
- Database tracking enables admin features (view active sessions, force logout)
- Meets MoEYS audit requirements (track when teachers accessed system)

**Alternatives Considered**:
- **Pure JWT (no database)**: Faster but cannot revoke tokens, no audit trail
- **Pure session cookies**: Stateful, requires Redis/database lookup per request (slower)

**References**: JWT best practices, session management patterns

---

## Summary

All research objectives addressed. Key decisions:

| Component | Decision | Rationale |
|-----------|----------|-----------|
| Password Hashing | BCrypt cost 12 | Industry standard, Spring Boot default, adequate performance |
| JWT | JJWT 0.12.5, HS256, 24h expiration | Spec compliant, stateless, well-supported library |
| Rate Limiting | Database sliding window (5/15min) | Simple, persistent across restarts, auditable |
| Phone Validation | Regex `^\+855[1-9]\d{7,8}$` | Cambodia format, no heavy dependencies |
| i18n | Spring MessageSource + .properties | Built-in Spring support, simple maintenance |
| Response Format | BaseResponse<T> with ErrorCode enum | User-specified, machine-readable errors |
| Database Schema | 3 tables (users, login_attempts, sessions) | Normalized, scalable, supports audit requirements |
| Session Management | Hybrid JWT + DB tracking | Balances stateless performance with control |

**No clarifications needed from user** - all technical decisions resolved using Spring Boot best practices and project constitution principles.

**Next Phase**: Phase 1 - Design & Contracts (data-model.md, OpenAPI contracts, quickstart.md)
