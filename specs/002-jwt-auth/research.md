# Research: JWT Authentication, Authorization and User Profile Management

**Feature**: 002-jwt-auth | **Date**: 2025-11-20 | **Phase**: 0 (Research)

## Purpose

This document consolidates research findings and technical decisions for implementing JWT refresh tokens, token lifecycle management, user profile CRUD operations, and profile photo uploads within the existing auth-service.

## Research Areas

### 1. Refresh Token Storage Strategy

**Decision**: Dual storage - Redis (cache) + PostgreSQL (persistence)

**Rationale**:
- **Redis**: Fast lookup for token validation (< 10ms), automatic TTL expiration, handles high concurrent requests
- **PostgreSQL**: Permanent record for audit trails, survives Redis restarts, enables admin review of active sessions
- **Dual-write pattern**: Write to both on token creation, invalidate in both on logout
- **Consistency**: PostgreSQL is source of truth; Redis is performance cache

**Implementation**:
```java
// Token creation flow
1. Generate refresh token UUID
2. Hash token with BCrypt
3. Write to PostgreSQL (refresh_tokens table)
4. Write to Redis (key: "refresh_token:{userId}:{tokenId}", TTL: 30 days)
5. Return plain token to client (only time it's visible)

// Token validation flow
1. Check Redis first (fast path)
2. If Redis miss, check PostgreSQL (cold start recovery)
3. Warm Redis cache on PostgreSQL hit
4. Validate token hash with BCrypt
```

**Alternatives Considered**:
- **Redis only**: Rejected - tokens lost on Redis restart, no audit trail
- **PostgreSQL only**: Rejected - slower lookup (50-100ms), can't handle 1000 req/s
- **JWT refresh tokens (no storage)**: Rejected - can't invalidate on logout/password change

**Spring Dependencies**: Already present (spring-boot-starter-data-redis, spring-boot-starter-data-jpa)

---

### 2. Token Replay Prevention

**Decision**: One-time refresh token with rotation

**Rationale**:
- **Security**: Each refresh token can only be used once
- **Replay Attack Prevention**: Reusing a refresh token invalidates all sessions (possible compromise)
- **User Experience**: Transparent rotation - client receives new refresh token with each use

**Implementation**:
```java
// Token refresh flow
1. Client sends refresh token
2. Validate token exists and not expired
3. Check if token already used (has_been_used flag)
4. If used -> InvalidateAllUserSessions() and throw SecurityException
5. Mark current token as used
6. Generate new access token (24h) + new refresh token (30d)
7. Store new refresh token (dual write)
8. Delete old refresh token from Redis/DB
9. Return both tokens to client
```

**Alternatives Considered**:
- **Reusable refresh tokens**: Rejected - vulnerable to replay attacks
- **Token families (RFC 6819)**: Rejected - added complexity for minimal benefit in our use case
- **Sliding window (allow N uses)**: Rejected - still vulnerable within window

**Database Schema**:
```sql
ALTER TABLE refresh_tokens ADD COLUMN has_been_used BOOLEAN DEFAULT FALSE;
ALTER TABLE refresh_tokens ADD COLUMN used_at TIMESTAMP;
```

---

### 3. Profile Photo Storage

**Decision**: Local filesystem with database metadata

**Rationale**:
- **Simplicity**: No cloud provider dependencies, no additional costs
- **Performance**: Direct filesystem access faster than database BLOBs
- **Scalability**: 10k users × 2MB = 20GB (manageable)
- **Docker Volume**: Mount persistent volume for uploads directory
- **Sufficient for MVP**: Can migrate to S3 later if needed

**Implementation**:
```java
// Storage structure
uploads/
└── profile-photos/
    └── {userId}/
        └── profile.jpg  # Latest photo (old ones deleted)

// Database schema
ALTER TABLE users ADD COLUMN profile_photo_url VARCHAR(500);
ALTER TABLE users ADD COLUMN profile_photo_uploaded_at TIMESTAMP;
```

**File Handling**:
- **Validation**: Check MIME type (image/jpeg, image/png), file size (< 5MB)
- **Sanitization**: Use Apache Tika for content-type detection, reject suspicious files
- **Naming**: Use userId + timestamp to prevent conflicts
- **Cleanup**: Delete old photo when new one uploaded (one photo per user)

**Alternatives Considered**:
- **Database BLOBs**: Rejected - poor performance for 5MB files, bloats database backups
- **AWS S3**: Rejected - premature complexity, adds cloud dependency and costs
- **MinIO (self-hosted S3)**: Rejected - additional infrastructure for small-scale needs

**Spring Dependencies**:
- `spring-boot-starter-web` (already present - handles multipart uploads)
- `org.apache.tika:tika-core:2.9.1` (NEW - MIME type detection)

---

### 4. Password Strength Requirements

**Decision**: OWASP-aligned password policy with Spring Security

**Rationale**:
- **Industry Standard**: OWASP provides well-tested recommendations
- **User-Friendly**: Avoids overly restrictive rules that frustrate users
- **Security**: Balances strength with memorability

**Policy**:
```
- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 digit
- At least 1 special character (!@#$%^&*()_+-=[]{}|;:,.<>?)
- Not in common password dictionary (top 10k)
```

**Implementation**:
```java
// Custom validator
@Component
public class PasswordStrengthValidator {
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]");

    public ValidationResult validate(String password) {
        if (password.length() < 8) return invalid("At least 8 characters required");
        if (!UPPERCASE.matcher(password).find()) return invalid("At least 1 uppercase letter");
        // ... other checks
        if (isCommonPassword(password)) return invalid("Password too common");
        return valid();
    }
}
```

**Alternatives Considered**:
- **Passay library**: Rejected - adds dependency for simple regex checks
- **Minimum 12 characters**: Rejected - too restrictive for users, 8 with complexity sufficient
- **Password entropy calculation**: Rejected - overly complex for user understanding

**Spring Dependencies**: None required (java.util.regex.Pattern built-in)

---

### 5. Session Invalidation Strategy

**Decision**: Cascading invalidation on password change

**Rationale**:
- **Security**: Compromised credentials should invalidate all access
- **User Control**: User explicitly changing password signals security concern
- **Exception**: Keep current session active (user changing password shouldn't log themselves out)

**Implementation**:
```java
// Password change flow
1. Validate current password
2. Hash new password with BCrypt
3. Update user.passwordHash
4. Find all sessions for user (current_user_id)
5. Delete all sessions EXCEPT current session (by token JTI)
6. Delete all refresh tokens EXCEPT current refresh token
7. Invalidate Redis cache for deleted tokens
8. Log audit event (password_changed, all_sessions_invalidated)
```

**Database Queries**:
```sql
-- Delete other sessions
DELETE FROM sessions
WHERE user_id = ? AND token_jti != ?;

-- Delete other refresh tokens
DELETE FROM refresh_tokens
WHERE user_id = ? AND id != ?;
```

**Alternatives Considered**:
- **Invalidate all sessions (including current)**: Rejected - poor UX (user logs themselves out)
- **Keep all sessions**: Rejected - security risk if password changed due to compromise
- **Session versioning**: Rejected - added complexity for same security outcome

---

### 6. JWT Claims Structure

**Decision**: Minimal claims with user identity and roles

**Rationale**:
- **Small Token Size**: Less data transmitted per request
- **Stateless**: All authorization info in token (no database lookup per request)
- **Standard Claims**: Follow JWT best practices (sub, iat, exp, jti)

**Access Token Claims** (24h expiry):
```json
{
  "sub": "user-uuid",           // User ID (subject)
  "jti": "token-jti-uuid",      // JWT ID (unique token identifier)
  "iat": 1700000000,            // Issued at (Unix timestamp)
  "exp": 1700086400,            // Expiration (Unix timestamp)
  "roles": ["TEACHER"],         // User roles for authorization
  "lang": "en"                  // Language preference (for error messages)
}
```

**Refresh Token** (30d expiry):
- Separate entity in database (not JWT)
- UUID-based random token
- Stored hashed (BCrypt) for security

**Alternatives Considered**:
- **Include profile data in JWT**: Rejected - increases token size, stale data after profile updates
- **Include permissions array**: Rejected - premature (roles sufficient for current requirements)
- **Nested JWT (refresh as JWT)**: Rejected - storage/invalidation requirement conflicts with JWT statelessness

**Spring Dependencies**: `io.jsonwebtoken:jjwt-api:0.12.5` (already present)

---

### 7. Cambodia Phone Validation

**Decision**: Extend existing regex pattern with stricter validation

**Rationale**:
- **Existing Pattern**: Already validates Cambodia format in User.java (`^\\+855[1-9]\\d{7,8}$`)
- **Coverage**: Matches all Cambodia mobile formats (+855 XX XXX XXX)
- **Server-side Only**: Client-side validation is UX enhancement, server validation is security requirement

**Current Implementation** (from User.java):
```java
@Pattern(regexp = "^\\+855[1-9]\\d{7,8}$", message = "Invalid Cambodia phone format")
private String phoneNumber;
```

**Validation Details**:
- `^\\+855`: Must start with +855 (Cambodia country code)
- `[1-9]`: First digit 1-9 (mobile operators: 1=Cellcard, 6=Smart, 7=Metfone, 9=Seatel, etc.)
- `\\d{7,8}$`: 7-8 additional digits (total 8-9 digits after country code)

**No Changes Required**: Pattern already correct and implemented in 001-teacher-auth

---

### 8. Internationalization (i18n) for Error Messages

**Decision**: Spring MessageSource with Khmer/English resource bundles

**Rationale**:
- **Spring Built-in**: No additional dependencies required
- **Language from JWT**: User's preferred language stored in JWT claims (`lang`)
- **Fallback**: Default to English if translation missing

**Implementation**:
```java
// messages_en.properties
error.token.invalid=Invalid or expired token
error.token.replay=Token has already been used
error.profile.phone.duplicate=Phone number already registered
error.password.weak=Password does not meet strength requirements
error.photo.size=Photo size exceeds 5MB limit

// messages_km.properties (Khmer)
error.token.invalid=តូខឹនមិនត្រឹមត្រូវ ឬផុតកំណត់
error.token.replay=តូខឹននេះត្រូវបានប្រើរួចហើយ
error.profile.phone.duplicate=លេខទូរស័ព្ទនេះត្រូវបានចុះឈ្មោះរួចហើយ
error.password.weak=ពាក្យសម្ងាត់មិនបំពេញតាមតម្រូវការ
error.photo.size=រូបភាពលើសពី 5MB
```

**Usage in Controllers**:
```java
@RestController
public class ProfileController {
    @Autowired
    private MessageSource messageSource;

    private String getMessage(String key, String lang) {
        Locale locale = "km".equals(lang) ? new Locale("km") : Locale.ENGLISH;
        return messageSource.getMessage(key, null, locale);
    }
}
```

**Alternatives Considered**:
- **Accept-Language header**: Rejected - language preference should persist in user profile
- **Client-side only i18n**: Rejected - server validation errors must be localized
- **Third-party i18n library**: Rejected - Spring built-in sufficient

**Spring Dependencies**: `spring-context` (already present via spring-boot-starter)

---

## Summary of Technical Decisions

| Area | Decision | New Dependencies |
|------|----------|------------------|
| Refresh Token Storage | Redis + PostgreSQL dual storage | None (already present) |
| Token Replay Prevention | One-time refresh token with rotation | None |
| Profile Photo Storage | Local filesystem with DB metadata | org.apache.tika:tika-core:2.9.1 |
| Password Strength | OWASP-aligned policy with custom validator | None |
| Session Invalidation | Cascade invalidation except current session | None |
| JWT Claims | Minimal claims (sub, jti, iat, exp, roles, lang) | None (jjwt already present) |
| Phone Validation | Existing regex (no changes) | None |
| i18n Error Messages | Spring MessageSource (Khmer/English) | None (spring-context present) |

## Dependencies to Add

Only one new dependency required:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-core</artifactId>
    <version>2.9.1</version>
</dependency>
```

All other dependencies already present from 001-teacher-auth feature.

## Next Steps

Phase 1 will produce:
1. **data-model.md**: Entity schemas (RefreshToken, updated User model)
2. **contracts/api-contracts.yaml**: OpenAPI spec for 6 endpoints
3. **quickstart.md**: Developer setup guide for testing new endpoints
