# Quickstart Guide: Teacher Registration & Login

**Feature**: 001-teacher-auth | **Branch**: `001-teacher-auth` | **Date**: 2025-11-20

## Overview

This guide helps developers implement the teacher authentication feature in the auth-service microservice. The feature adds two REST endpoints for teacher registration and login, with support for Cambodia phone validation, password strength enforcement, rate limiting, and bilingual error messages.

**What You'll Build**:
- POST `/api/auth/register` - Create teacher accounts
- POST `/api/auth/login` - Authenticate teachers with email or phone
- Database schema with 3 tables (users, login_attempts, sessions)
- JWT token generation with 24-hour expiration
- Rate limiting (5 failed attempts per 15 minutes)
- Bilingual error messages (Khmer/English)

**Estimated Time**: 8-12 hours for complete implementation + testing

---

## Prerequisites

### Required Knowledge
- Java 21 features (records, pattern matching)
- Spring Boot 3.5.7 (Spring Security, Spring Data JPA)
- PostgreSQL 15+ (SQL, Flyway migrations)
- JWT authentication (JJWT library)
- REST API design (OpenAPI/Swagger)
- JUnit 5 + Mockito for testing

### Development Environment
- **IDE**: IntelliJ IDEA 2024+ or Eclipse with Java 21 support
- **Java**: OpenJDK 21 (Eclipse Temurin recommended)
- **Docker**: Docker Desktop 4.x+ with Compose V2
- **Database**: PostgreSQL 15+ (via Docker Compose)
- **Git**: For branch management

### Review Required Documents
1. **[spec.md](./spec.md)** - Feature specification with functional requirements
2. **[data-model.md](./data-model.md)** - Database schema and entity design
3. **[research.md](./research.md)** - Technical decisions and rationale
4. **[contracts/register-api.yaml](./contracts/register-api.yaml)** - Registration endpoint OpenAPI spec
5. **[contracts/login-api.yaml](./contracts/login-api.yaml)** - Login endpoint OpenAPI spec

---

## Step 1: Set Up Development Branch

```bash
# Ensure you're on main with latest changes
git checkout main
git pull origin main

# Branch should already exist from /speckit.specify command
git checkout 001-teacher-auth

# Verify branch
git status
# Should show: On branch 001-teacher-auth
```

---

## Step 2: Database Setup (Flyway Migrations)

### 2.1 Create Migration Files

All migrations go in `auth-service/src/main/resources/db/migration/`

**File**: `V1__create_users_table.sql`
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

**File**: `V2__create_login_attempts_table.sql`
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

**File**: `V3__create_sessions_table.sql`
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

### 2.2 Test Migrations Locally

```bash
# Start PostgreSQL via Docker Compose
docker-compose up -d postgres

# Run auth-service to trigger Flyway migration
cd auth-service
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# Verify tables created
docker exec -it sms-postgres psql -U smsadmin -d auth_db -c "\dt"
# Should show: users, login_attempts, sessions, flyway_schema_history
```

---

## Step 3: Implement JPA Entities

### 3.1 User Entity

**File**: `auth-service/src/main/java/com/sms/auth/model/User.java`

```java
package com.sms.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Pattern(regexp = "^\\+855[1-9]\\d{7,8}$", message = "Invalid Cambodia phone format")
    @Column(name = "phone_number", unique = true, nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "preferred_language", length = 2)
    private String preferredLanguage = "en"; // 'en' or 'km'

    @Column(name = "account_status", length = 20)
    private String accountStatus = "active"; // 'active', 'inactive', 'locked'

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

### 3.2 LoginAttempt Entity

**File**: `auth-service/src/main/java/com/sms/auth/model/LoginAttempt.java`

```java
package com.sms.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String identifier; // email or phone

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private Boolean success;

    @Column(name = "failure_reason", length = 50)
    private String failureReason; // e.g., "INVALID_PASSWORD", "RATE_LIMITED"

    @CreationTimestamp
    @Column(name = "attempted_at", nullable = false, updatable = false)
    private LocalDateTime attemptedAt;
}
```

### 3.3 Session Entity

**File**: `auth-service/src/main/java/com/sms/auth/model/Session.java`

```java
package com.sms.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_jti", unique = true, nullable = false, length = 255)
    private String tokenJti; // JWT ID claim

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_activity_at", nullable = false)
    private LocalDateTime lastActivityAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
}
```

---

## Step 4: Create JPA Repositories

**File**: `auth-service/src/main/java/com/sms/auth/repository/UserRepository.java`

```java
package com.sms.auth.repository;

import com.sms.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
```

**File**: `auth-service/src/main/java/com/sms/auth/repository/LoginAttemptRepository.java`

```java
package com.sms.auth.repository;

import com.sms.auth.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    @Query("SELECT COUNT(la) FROM LoginAttempt la " +
           "WHERE la.identifier = :identifier " +
           "AND la.success = false " +
           "AND la.attemptedAt > :since")
    long countFailedAttemptsSince(@Param("identifier") String identifier,
                                   @Param("since") LocalDateTime since);
}
```

**File**: `auth-service/src/main/java/com/sms/auth/repository/SessionRepository.java`

```java
package com.sms.auth.repository;

import com.sms.auth.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByTokenJti(String tokenJti);

    void deleteByTokenJti(String tokenJti);
}
```

---

## Step 5: Implement DTOs and Response Format

### 5.1 Base Response Wrapper

**File**: `auth-service/src/main/java/com/sms/auth/dto/BaseResponse.java`

```java
package com.sms.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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
```

### 5.2 Error Code Enum

**File**: `auth-service/src/main/java/com/sms/auth/dto/ErrorCode.java`

```java
package com.sms.auth.dto;

public enum ErrorCode {
    SUCCESS,
    DUPLICATE_EMAIL,
    DUPLICATE_PHONE,
    INVALID_PASSWORD,
    INVALID_PHONE_FORMAT,
    INVALID_EMAIL_FORMAT,
    INVALID_CREDENTIALS,
    INVALID_LANGUAGE,
    RATE_LIMIT_EXCEEDED,
    ACCOUNT_LOCKED,
    SESSION_EXPIRED,
    INTERNAL_ERROR
}
```

### 5.3 Request/Response DTOs

**File**: `auth-service/src/main/java/com/sms/auth/dto/RegisterRequest.java`

```java
package com.sms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+855[1-9]\\d{7,8}$",
             message = "Phone must be Cambodia format (+855 XX XXX XXX)")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;

    @Pattern(regexp = "^(en|km)$", message = "Language must be 'en' or 'km'")
    private String preferredLanguage = "en";
}
```

**File**: `auth-service/src/main/java/com/sms/auth/dto/LoginRequest.java`

```java
package com.sms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email or phone is required")
    private String emailOrPhone;

    @NotBlank(message = "Password is required")
    private String password;
}
```

**File**: `auth-service/src/main/java/com/sms/auth/dto/AuthResponse.java`

```java
package com.sms.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private UUID userId;
    private String email;
    private String phoneNumber;
    private String preferredLanguage;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
```

---

## Step 6: Implement Core Services

### 6.1 Password Validation Service

**File**: `auth-service/src/main/java/com/sms/auth/validation/PasswordValidator.java`

```java
package com.sms.auth.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    // Min 8 chars, 1 upper, 1 lower, 1 number, 1 special
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!*()_\\-]).{8,}$"
    );

    public boolean isValid(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public String getRequirementsMessage() {
        return "Password must be at least 8 characters with uppercase, " +
               "lowercase, number, and special character";
    }
}
```

### 6.2 Rate Limiting Service

**File**: `auth-service/src/main/java/com/sms/auth/service/RateLimitService.java`

```java
package com.sms.auth.service;

import com.sms.auth.model.LoginAttempt;
import com.sms.auth.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final LoginAttemptRepository loginAttemptRepository;

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    @Transactional(readOnly = true)
    public boolean isRateLimited(String identifier) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(LOCKOUT_MINUTES);
        long failedAttempts = loginAttemptRepository.countFailedAttemptsSince(identifier, since);
        return failedAttempts >= MAX_ATTEMPTS;
    }

    @Transactional
    public void recordAttempt(String identifier, String ipAddress, boolean success, String failureReason) {
        LoginAttempt attempt = LoginAttempt.builder()
            .identifier(identifier)
            .ipAddress(ipAddress)
            .success(success)
            .failureReason(failureReason)
            .build();
        loginAttemptRepository.save(attempt);
    }
}
```

### 6.3 JWT Token Provider

**File**: `auth-service/src/main/java/com/sms/auth/security/JwtTokenProvider.java`

```java
package com.sms.auth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final long EXPIRATION_MS = 86400000; // 24 hours

    public String generateToken(UUID userId, String language) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_MS);
        String jti = UUID.randomUUID().toString();

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
            .setSubject(userId.toString())
            .setId(jti)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("lang", language)
            .claim("roles", new String[]{"TEACHER"})
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public String getJtiFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getId();
    }
}
```

---

## Step 7: Implement Auth Service

**File**: `auth-service/src/main/java/com/sms/auth/service/AuthService.java`

```java
package com.sms.auth.service;

import com.sms.auth.dto.*;
import com.sms.auth.exception.*;
import com.sms.auth.model.Session;
import com.sms.auth.model.User;
import com.sms.auth.repository.SessionRepository;
import com.sms.auth.repository.UserRepository;
import com.sms.auth.security.JwtTokenProvider;
import com.sms.auth.validation.PasswordValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final JwtTokenProvider jwtTokenProvider;
    private final RateLimitService rateLimitService;

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        // Validate password strength
        if (!passwordValidator.isValid(request.getPassword())) {
            throw new InvalidPasswordException(passwordValidator.getRequirementsMessage());
        }

        // Check duplicates
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("This email is already registered");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicatePhoneException("This phone number is already registered");
        }

        // Create user
        User user = User.builder()
            .email(request.getEmail())
            .phoneNumber(request.getPhoneNumber())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .preferredLanguage(request.getPreferredLanguage())
            .accountStatus("active")
            .build();
        user = userRepository.save(user);

        // Generate JWT
        String token = jwtTokenProvider.generateToken(user.getId(), user.getPreferredLanguage());
        String jti = jwtTokenProvider.getJtiFromToken(token);

        // Create session
        Session session = Session.builder()
            .user(user)
            .tokenJti(jti)
            .lastActivityAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(24))
            .ipAddress(getClientIp(httpRequest))
            .userAgent(httpRequest.getHeader("User-Agent"))
            .build();
        sessionRepository.save(session);

        return buildAuthResponse(user, token, user.getCreatedAt());
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String identifier = request.getEmailOrPhone();
        String clientIp = getClientIp(httpRequest);

        // Check rate limiting
        if (rateLimitService.isRateLimited(identifier)) {
            throw new RateLimitExceededException(
                "Too many failed login attempts. Please try again in 15 minutes."
            );
        }

        // Find user by email or phone
        User user = userRepository.findByEmail(identifier)
            .or(() -> userRepository.findByPhoneNumber(identifier))
            .orElse(null);

        // Verify credentials
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            rateLimitService.recordAttempt(identifier, clientIp, false, "INVALID_PASSWORD");
            throw new InvalidCredentialsException("Invalid email/phone or password");
        }

        // Record successful attempt
        rateLimitService.recordAttempt(identifier, clientIp, true, null);

        // Generate JWT
        String token = jwtTokenProvider.generateToken(user.getId(), user.getPreferredLanguage());
        String jti = jwtTokenProvider.getJtiFromToken(token);

        // Create session
        Session session = Session.builder()
            .user(user)
            .tokenJti(jti)
            .lastActivityAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(24))
            .ipAddress(clientIp)
            .userAgent(httpRequest.getHeader("User-Agent"))
            .build();
        sessionRepository.save(session);

        return buildAuthResponse(user, token, LocalDateTime.now());
    }

    private AuthResponse buildAuthResponse(User user, String token, LocalDateTime loginTime) {
        return AuthResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .preferredLanguage(user.getPreferredLanguage())
            .token(token)
            .createdAt(user.getCreatedAt())
            .lastLoginAt(loginTime)
            .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

---

## Step 8: Implement REST Controller

**File**: `auth-service/src/main/java/com/sms/auth/controller/AuthController.java`

```java
package com.sms.auth.controller;

import com.sms.auth.dto.*;
import com.sms.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.register(request, httpRequest);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
```

---

## Step 9: Global Exception Handling

**File**: `auth-service/src/main/java/com/sms/auth/exception/GlobalExceptionHandler.java`

```java
package com.sms.auth.exception;

import com.sms.auth.dto.BaseResponse;
import com.sms.auth.dto.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for auth-service.
 * Returns error codes only - frontend handles translation to Khmer/English.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<BaseResponse<Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.badRequest()
            .body(BaseResponse.error(ErrorCode.DUPLICATE_EMAIL));
    }

    @ExceptionHandler(DuplicatePhoneException.class)
    public ResponseEntity<BaseResponse<Object>> handleDuplicatePhone(DuplicatePhoneException ex) {
        return ResponseEntity.badRequest()
            .body(BaseResponse.error(ErrorCode.DUPLICATE_PHONE));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<BaseResponse<Object>> handleInvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity.badRequest()
            .body(BaseResponse.error(ErrorCode.INVALID_PASSWORD));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<BaseResponse<Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(BaseResponse.error(ErrorCode.INVALID_CREDENTIALS));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<BaseResponse<Object>> handleRateLimit(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(BaseResponse.error(ErrorCode.RATE_LIMIT_EXCEEDED));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        // Determine error code based on field
        ErrorCode errorCode = ErrorCode.INVALID_EMAIL_FORMAT; // default
        if (!ex.getBindingResult().getFieldErrors().isEmpty()) {
            String field = ex.getBindingResult().getFieldErrors().get(0).getField();
            if ("phoneNumber".equals(field)) {
                errorCode = ErrorCode.INVALID_PHONE_FORMAT;
            } else if ("password".equals(field)) {
                errorCode = ErrorCode.INVALID_PASSWORD;
            }
        }

        return ResponseEntity.badRequest()
            .body(BaseResponse.error(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleGeneric(Exception ex) {
        // Log full exception for debugging
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(BaseResponse.error(ErrorCode.INTERNAL_ERROR));
    }
}
```

---

## Step 10: Error Messages (Frontend i18n)

**Architecture Decision**: Internationalization is handled in the **frontend**, not the backend.

### Backend Responsibility

The backend returns:
1. **Machine-readable error codes** (ErrorCode enum) only
2. **No error messages** in the response

**Example**:
```java
// Backend throws exception
throw new DuplicateEmailException("Email already registered");

// Exception handler creates response with error code only:
return BaseResponse.error(ErrorCode.DUPLICATE_EMAIL);

// Response: {"errorCode": "DUPLICATE_EMAIL", "data": null}
```

### Frontend Responsibility

The frontend:
1. Receives error code from backend (e.g., `"DUPLICATE_EMAIL"`)
2. Maps error code to localized message based on user's language preference
3. Displays translated message to user

**Example Frontend Implementation** (React/Next.js with i18next):

```javascript
// Frontend translation files
const errorTranslations = {
  en: {
    DUPLICATE_EMAIL: "This email is already registered",
    DUPLICATE_PHONE: "This phone number is already registered",
    INVALID_PASSWORD: "Password must be at least 8 characters with uppercase, lowercase, number, and special character",
    INVALID_CREDENTIALS: "Invalid email/phone or password",
    RATE_LIMIT_EXCEEDED: "Too many failed login attempts. Please try again in 15 minutes."
  },
  km: {
    DUPLICATE_EMAIL: "អ៊ីមែលនេះត្រូវបានចុះឈ្មោះរួចហើយ",
    DUPLICATE_PHONE: "លេខទូរស័ព្ទនេះត្រូវបានចុះឈ្មោះរួចហើយ",
    INVALID_PASSWORD: "ពាក្យសម្ងាត់ត្រូវមានយ៉ាងហោចណាស់ 8 តួអក្សរ រួមមានអក្សរធំ អក្សរតូច លេខ និងតួអក្សរពិសេស",
    INVALID_CREDENTIALS: "អ៊ីមែល/លេខទូរស័ព្ទ ឬពាក្យសម្ងាត់មិនត្រឹមត្រូវ",
    RATE_LIMIT_EXCEEDED: "មានការព្យាយាមចូលបរាជ័យច្រើនពេក។ សូមព្យាយាមម្តងទៀតក្នុងរយៈពេល 15 នាទី។"
  }
};

// Usage in component
const localizedMessage = errorTranslations[userLanguage][response.errorCode];
```

### Benefits of Frontend i18n

✅ **Simpler backend** - No locale parsing, no MessageSource configuration
✅ **Faster iteration** - Change translations without backend deployment
✅ **Better UX** - Frontend can add contextual details (e.g., "The email **user@example.com** is already registered")
✅ **Language-agnostic API** - Error codes work for any language
✅ **Future-proof** - Easy to add new languages (e.g., Vietnamese, Thai)

---

## Step 11: Testing

### 11.1 Unit Test Example

**File**: `auth-service/src/test/java/com/sms/auth/service/AuthServiceTest.java`

```java
@SpringBootTest
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordValidator passwordValidator;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_withValidRequest_shouldCreateUser() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@school.edu.kh");
        request.setPhoneNumber("+85512345678");
        request.setPassword("Test1234!");
        request.setPreferredLanguage("en");

        when(passwordValidator.isValid(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> authService.register(request, mockHttpRequest()));
    }

    @Test
    void register_withDuplicateEmail_shouldThrowException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("duplicate@school.edu.kh");

        when(passwordValidator.isValid(anyString())).thenReturn(true);
        when(userRepository.existsByEmail("duplicate@school.edu.kh")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateEmailException.class,
            () -> authService.register(request, mockHttpRequest()));
    }
}
```

### 11.2 Integration Test with Testcontainers

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("auth_db_test");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void registerAndLogin_endToEnd() {
        // Register
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail("integration@test.com");
        registerReq.setPhoneNumber("+85512345678");
        registerReq.setPassword("Test1234!");

        ResponseEntity<BaseResponse> registerRes = restTemplate.postForEntity(
            "/api/auth/register", registerReq, BaseResponse.class);

        assertEquals(HttpStatus.OK, registerRes.getStatusCode());
        assertEquals(ErrorCode.SUCCESS, registerRes.getBody().getError());

        // Login
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmailOrPhone("integration@test.com");
        loginReq.setPassword("Test1234!");

        ResponseEntity<BaseResponse> loginRes = restTemplate.postForEntity(
            "/api/auth/login", loginReq, BaseResponse.class);

        assertEquals(HttpStatus.OK, loginRes.getStatusCode());
        assertNotNull(((AuthResponse) loginRes.getBody().getData()).getToken());
    }
}
```

---

## Step 12: Manual Testing with curl

```bash
# 1. Register a new teacher
curl -X POST http://localhost:8080/auth-service/api/auth/register \
  -H "Content-Type: application/json" \
  -H "Accept-Language: en" \
  -d '{
    "email": "teacher@school.edu.kh",
    "phoneNumber": "+85512345678",
    "password": "SecurePass123!",
    "preferredLanguage": "en"
  }'

# Expected: 200 OK with JWT token

# 2. Login with email
curl -X POST http://localhost:8080/auth-service/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrPhone": "teacher@school.edu.kh",
    "password": "SecurePass123!"
  }'

# 3. Login with phone
curl -X POST http://localhost:8080/auth-service/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrPhone": "+85512345678",
    "password": "SecurePass123!"
  }'

# 4. Test rate limiting (run 6 times with wrong password)
for i in {1..6}; do
  curl -X POST http://localhost:8080/auth-service/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{
      "emailOrPhone": "teacher@school.edu.kh",
      "password": "WrongPassword"
    }'
  echo "\nAttempt $i"
done

# Expected: First 5 return 401 INVALID_CREDENTIALS, 6th returns 429 RATE_LIMIT_EXCEEDED
```

---

## Common Issues & Solutions

### Issue 1: Flyway Migration Fails
**Symptom**: "Flyway validation failed" or "Table already exists"
**Solution**:
```bash
# Clean database and restart
docker-compose down -v
docker-compose up -d postgres
# Restart auth-service to re-run migrations
```

### Issue 2: JWT_SECRET Not Found
**Symptom**: "Could not resolve placeholder 'JWT_SECRET'"
**Solution**: Ensure `.env` file exists with `JWT_SECRET=your-secret-key-here` (minimum 256 bits)

### Issue 3: Password Always Invalid
**Symptom**: Login always returns "Invalid credentials" even with correct password
**Solution**: Check BCrypt cost factor matches between registration and login (should be 12)

---

## Next Steps

After completing implementation:

1. **Run `/speckit.tasks`** - Generate implementation tasks from this plan
2. **Create Pull Request** - Follow PR template for code review
3. **Update Documentation** - Add API docs to project wiki
4. **Frontend Integration** - Share OpenAPI contracts with frontend team

---

## Resources

- **OpenAPI Specs**: [register-api.yaml](./contracts/register-api.yaml), [login-api.yaml](./contracts/login-api.yaml)
- **Data Model**: [data-model.md](./data-model.md)
- **Technical Decisions**: [research.md](./research.md)
- **Feature Spec**: [spec.md](./spec.md)
- **Spring Security Docs**: https://docs.spring.io/spring-security/reference/
- **JJWT Documentation**: https://github.com/jwtk/jjwt#readme
