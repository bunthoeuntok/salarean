# Implementation Plan: JWT Authentication, Authorization and User Profile Management

**Branch**: `002-jwt-auth` | **Date**: 2025-11-20 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-jwt-auth/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

This feature extends the existing authentication system (001-teacher-auth) to implement JWT-based session management with refresh tokens, user profile management capabilities, and profile photo upload functionality. The implementation will add refresh token storage in Redis and PostgreSQL, create new endpoints for token refresh/logout, profile management (view/update), password changes, language preferences, and profile photo uploads. This builds on the existing JWT infrastructure while adding the token lifecycle management and user self-service capabilities required for a complete authentication and profile management system.

## Technical Context

**Language/Version**: Java 21 (Spring Boot 3.5.7)
**Primary Dependencies**: Spring Boot, Spring Security, Spring Data JPA, Spring Data Redis, JWT (jjwt 0.12.5), BCrypt, Hibernate Validator, Lombok
**Storage**: PostgreSQL 15+ (auth-service database), Redis 7+ (refresh token cache)
**Testing**: JUnit 5, Mockito, Spring Boot Test, Testcontainers
**Target Platform**: Docker containerized microservice (Linux)
**Project Type**: Microservices architecture (extending existing auth-service)
**Performance Goals**: Token refresh < 500ms, Profile updates < 200ms, Photo upload < 3s (2MB), Support 1000 concurrent requests
**Constraints**: Refresh tokens stored in both Redis (fast lookup) and PostgreSQL (persistence), Token replay prevention, Session isolation per device, Image validation and sanitization
**Scale/Scope**: 10k+ concurrent users, 30-day token lifecycle, Multi-device session support, Image storage integration

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Principle I: Microservices-First ✅ PASS

**Status**: COMPLIANT - Feature extends existing auth-service boundary

- **Service Boundary**: All authentication and user profile features belong to the auth-service bounded context
- **No new services**: Extends existing auth-service (already established in 001-teacher-auth)
- **Data Ownership**: auth-service owns user credentials, profiles, sessions, and tokens
- **API Contracts**: REST endpoints follow existing patterns in auth-service
- **Failure Isolation**: Token refresh failures don't cascade; clients retry with existing tokens

**Justification**: This feature naturally fits within the authentication service boundary. Profile management is closely coupled with authentication (password changes invalidate sessions, language preferences affect error messages, etc.). Creating a separate "profile service" would introduce unnecessary cross-service transactions.

### Principle II: Security-First ✅ PASS

**Status**: COMPLIANT - Security is central to feature design

- **Authentication**: All endpoints require valid JWT (except public health checks)
- **Authorization**: Users can only access/modify their own profiles
- **Data Protection**:
  - Refresh tokens hashed before storage (BCrypt)
  - Profile photos sanitized and validated
  - Password changes require current password verification
- **Audit Logging**: All profile modifications logged with user ID and timestamp
- **Token Security**:
  - Access tokens (24h) + Refresh tokens (30d)
  - Token replay prevention via JTI tracking
  - All sessions invalidated on password change
  - Logout invalidates both token types

**Compliance**: Aligns with MoEYS data protection requirements for student data systems (teacher credentials are similarly sensitive).

### Principle III: Simplicity (YAGNI) ✅ PASS

**Status**: COMPLIANT - Minimal, requirement-driven implementation

- **No Premature Optimization**: Using existing Redis instance (no new infrastructure)
- **No Speculative Features**: Only implementing specified requirements (no "nice to have" features)
- **Existing Patterns**: Reusing established patterns from 001-teacher-auth
- **Minimal Dependencies**: All required dependencies already in place (jjwt, Redis, PostgreSQL)
- **No New Abstractions**: Using Spring Data JPA repositories, standard Spring Security patterns

**Avoided Complexity**:
- ❌ Separate token service microservice
- ❌ Complex token rotation strategies (one-time refresh tokens sufficient)
- ❌ Advanced image processing libraries (basic validation sufficient)
- ❌ Distributed session management (Redis + DB dual storage sufficient)

### Principle IV: Observability ✅ PASS

**Status**: COMPLIANT - Existing observability extended

- **Health Endpoints**: Existing `/actuator/health` covers service
- **Structured Logging**: JSON logs with correlation IDs (via Spring Boot Actuator)
- **Metrics**: Key operations logged:
  - Token refresh success/failure rates
  - Profile update operations
  - Photo upload sizes and durations
  - Session invalidation events
- **Alerting**: Existing monitoring covers:
  - Redis connectivity failures
  - Database connection pool exhaustion
  - Token generation errors

### Principle V: Test Discipline ✅ PASS

**Status**: COMPLIANT - Comprehensive test coverage planned

- **Unit Tests**:
  - Token generation and validation logic
  - Profile update validations
  - Password strength checks
  - Image file validation
- **Integration Tests**:
  - Token refresh flow (Redis + DB)
  - Profile CRUD operations
  - Password change session invalidation
  - Photo upload with file storage
- **Contract Tests**:
  - All 6 API endpoints (OpenAPI schema validation)
  - Request/response payload validation
  - Error response formats

**Coverage Target**: 80%+ line coverage for new service methods

## Project Structure

### Documentation (this feature)

```text
specs/002-jwt-auth/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── api-contracts.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/sms/auth/
│   │   │   ├── model/
│   │   │   │   ├── User.java                    # EXTEND: Add name, profilePhotoUrl fields
│   │   │   │   ├── Session.java                 # EXISTS: Already has token tracking
│   │   │   │   └── RefreshToken.java            # NEW: Refresh token entity
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java          # EXISTS: Will extend for profile queries
│   │   │   │   ├── SessionRepository.java       # EXISTS: Will extend for invalidation
│   │   │   │   └── RefreshTokenRepository.java  # NEW: Refresh token persistence
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java             # EXISTS: Will extend for token refresh
│   │   │   │   ├── ProfileService.java          # NEW: Profile CRUD operations
│   │   │   │   ├── PhotoStorageService.java     # NEW: Image upload/storage
│   │   │   │   └── TokenService.java            # NEW: Refresh token management
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java          # EXTEND: Add /refresh, /logout
│   │   │   │   └── ProfileController.java       # NEW: Profile endpoints
│   │   │   ├── dto/
│   │   │   │   ├── RefreshTokenRequest.java     # EXISTS: Already defined
│   │   │   │   ├── RefreshTokenResponse.java    # NEW: Token refresh response
│   │   │   │   ├── ProfileResponse.java         # NEW: Profile view DTO
│   │   │   │   ├── UpdateProfileRequest.java    # NEW: Profile update DTO
│   │   │   │   ├── ChangePasswordRequest.java   # NEW: Password change DTO
│   │   │   │   └── PhotoUploadResponse.java     # NEW: Photo upload response
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java        # EXISTS: Will extend for refresh tokens
│   │   │   │   ├── JwtAuthenticationFilter.java # NEW: JWT validation filter
│   │   │   │   └── SecurityConfig.java          # EXTEND: Add filter chain
│   │   │   └── exception/
│   │   │       ├── InvalidTokenException.java   # NEW: Token validation errors
│   │   │       ├── ProfileUpdateException.java  # NEW: Profile update errors
│   │   │       └── PhotoUploadException.java    # NEW: Photo upload errors
│   │   └── resources/
│   │       ├── application.yml                  # EXTEND: Add photo storage config
│   │       └── db/migration/
│   │           └── V2__add_refresh_tokens_and_profile_fields.sql  # NEW: Schema migration
│   └── test/
│       └── java/com/sms/auth/
│           ├── service/
│           │   ├── ProfileServiceTest.java      # NEW: Profile service tests
│           │   ├── TokenServiceTest.java        # NEW: Token service tests
│           │   └── PhotoStorageServiceTest.java # NEW: Photo storage tests
│           └── controller/
│               └── ProfileControllerTest.java   # NEW: Profile API tests
└── target/

api-gateway/
└── # No changes required - routes already configured for auth-service

# Image storage (to be decided in research phase)
# Option A: Local filesystem (./uploads/profile-photos/)
# Option B: Cloud storage (S3-compatible)
# Option C: Database BLOBs (not recommended for 5MB files)
```

**Structure Decision**: Extending existing microservices architecture. All authentication and profile features live within the auth-service boundary, which is the correct service for user identity and credential management. The auth-service already has Spring Boot, JPA, Redis, and JWT infrastructure from 001-teacher-auth. This feature adds new entities, services, and endpoints to that existing structure.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations detected. All principles passed.
