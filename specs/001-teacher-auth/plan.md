# Implementation Plan: Teacher Registration & Login

**Branch**: `001-teacher-auth` | **Date**: 2025-11-20 | **Spec**: [spec.md](./spec.md)

**Status**: ✅ Planning Complete - Ready for implementation

---

## Summary

This feature implements teacher authentication for the Student Management System. Teachers can register with email, Cambodia phone number (+855 format), and password, then log in using either credential. The system enforces password strength, validates inputs, manages sessions (24-hour timeout), and provides error codes for frontend localization (Khmer/English). Rate limiting prevents brute-force attacks (5 attempts per 15 minutes).

The implementation extends the existing auth-service microservice with new REST API endpoints (`POST /api/auth/register`, `POST /api/auth/login`) and stores teacher accounts in the auth_db PostgreSQL database. All API responses follow the standardized format: `{errorCode: ErrorEnum, data: T}`.

**Key Architecture Decision**: Internationalization (i18n) is handled in the **frontend**, not the backend. Backend returns only machine-readable error codes (ErrorCode enum) - no error messages. Frontend translates error codes to Khmer/English based on user preference. This maximally simplifies backend implementation and provides frontend flexibility. See [architecture-decisions.md](./architecture-decisions.md) for full rationale.

---

## Planning Artifacts

All planning documents have been generated and are ready for implementation:

### Phase 0: Research ✅
- **[research.md](./research.md)** - 8 key technical decisions:
  - BCrypt password hashing (cost factor 12)
  - JWT implementation (JJWT 0.12.5, HS256, 24h expiration)
  - Database-backed rate limiting (sliding window)
  - Cambodia phone validation regex: `^\+855[1-9]\d{7,8}$`
  - **Frontend i18n** (English error messages from backend, frontend translates)
  - BaseResponse<T> wrapper with ErrorCode enum
  - 3-table database schema (users, login_attempts, sessions)
  - Hybrid JWT + DB session management

### Phase 1: Design & Contracts ✅
- **[data-model.md](./data-model.md)** - Complete database schema:
  - users table (UUID, email, phone, password_hash, preferred_language)
  - login_attempts table (rate limiting tracker)
  - sessions table (JWT session tracking, 24-hour expiration)
  - Flyway migration scripts (V1, V2, V3)
  - All indexes defined for performance

- **[contracts/register-api.yaml](./contracts/register-api.yaml)** - OpenAPI 3.0 spec for POST /api/auth/register
- **[contracts/login-api.yaml](./contracts/login-api.yaml)** - OpenAPI 3.0 spec for POST /api/auth/login
- **[quickstart.md](./quickstart.md)** - 12-step developer implementation guide with complete code examples

- **[architecture-decisions.md](./architecture-decisions.md)** - Key architectural choices:
  - Decision 1: Frontend i18n vs Backend i18n (Frontend chosen)
  - Decision 2: BaseResponse in services vs API Gateway (Services chosen)

---

## Technical Context

**Language/Version**: Java 21 (Spring Boot 3.5.7)
**Primary Dependencies**: Spring Boot, Spring Security, Spring Data JPA, JWT (jjwt 0.12.5), BCrypt for password hashing
**Storage**: PostgreSQL 15+ (auth_db database)
**Testing**: JUnit 5, Mockito, Spring Boot Test, Testcontainers for integration tests
**Target Platform**: Docker containers (Linux), deployed via docker-compose
**Project Type**: Microservices - auth-service (existing service, extending with new endpoints)
**Performance Goals**: Handle 100 concurrent registration/login requests, login completes in <5 seconds, registration in <3 minutes
**Constraints**: JWT token expiry 24 hours, session timeout 24 hours inactivity, rate limiting 5 failed logins per 15 minutes
**Scale/Scope**: Educational platform supporting multiple schools, estimated 1000+ teachers total

---

## Constitution Check

*All gates verified and PASSED ✅*

### ✅ I. Microservices-First
- Feature implemented within existing **auth-service** boundary
- Independent deployment (Docker, separate auth_db database)
- REST API communication, no database coupling

### ✅ II. Security-First
- JWT authentication required
- BCrypt password hashing (cost factor 12)
- Rate limiting prevents brute-force (5 attempts/15 min)
- JWT_SECRET from environment variable
- Audit logging via login_attempts table

### ✅ III. Simplicity (YAGNI)
- No new services or infrastructure
- Standard Spring Boot patterns
- No backend i18n complexity (frontend handles translation)
- No complexity justification needed

### ✅ IV. Observability
- Spring Boot Actuator already configured
- Structured logging
- Metrics for latency, throughput, error rates
- Can monitor login failure rates, session creation

### ✅ V. Test Discipline
- Unit tests: Password validation, email/phone validation, BCrypt hashing
- Integration tests: Registration, login, session persistence, rate limiting
- Contract tests: OpenAPI specs for both endpoints
- Test independence: Testcontainers for PostgreSQL

**Overall Status**: ✅ ALL GATES PASSED - No violations, feature ready for implementation

---

## Next Steps

1. **Start Implementation**: Follow the step-by-step guide in [quickstart.md](./quickstart.md)

2. **Generate Tasks** (optional):
   ```bash
   /speckit.tasks
   ```
   Creates `tasks.md` with dependency-ordered implementation tasks

3. **Convert to GitHub Issues** (optional):
   ```bash
   /speckit.taskstoissues
   ```
   Creates GitHub issues from tasks.md

---

## Project Structure

### Documentation (this feature)

```text
specs/001-teacher-auth/
├── spec.md                          # Feature specification (from /speckit.specify)
├── plan.md                          # This file
├── research.md                      # Technical research (Phase 0)
├── data-model.md                    # Database schema (Phase 1)
├── quickstart.md                    # Developer guide (Phase 1)
├── architecture-decisions.md        # Key architecture choices
├── contracts/                       # OpenAPI specifications (Phase 1)
│   ├── register-api.yaml           # POST /api/auth/register
│   └── login-api.yaml              # POST /api/auth/login
└── checklists/                      # Quality validation
    └── requirements.md              # Spec quality checklist (all passed)
```

### Source Code (auth-service)

```text
auth-service/
├── src/main/java/com/sms/auth/
│   ├── controller/
│   │   └── AuthController.java              # REST endpoints
│   ├── service/
│   │   ├── AuthService.java                 # Business logic
│   │   └── RateLimitService.java            # Rate limiting
│   ├── repository/
│   │   ├── UserRepository.java              # JPA repository
│   │   ├── LoginAttemptRepository.java
│   │   └── SessionRepository.java
│   ├── model/
│   │   ├── User.java                        # JPA entity
│   │   ├── LoginAttempt.java
│   │   └── Session.java
│   ├── dto/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── AuthResponse.java
│   │   ├── BaseResponse.java                # Standardized wrapper
│   │   └── ErrorCode.java                   # Error code enum
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── DuplicateEmailException.java
│   │   ├── DuplicatePhoneException.java
│   │   ├── InvalidCredentialsException.java
│   │   └── RateLimitExceededException.java
│   ├── validation/
│   │   ├── PasswordValidator.java
│   │   └── CambodiaPhoneValidator.java
│   └── security/
│       ├── JwtTokenProvider.java
│       ├── PasswordEncoder.java             # BCrypt config
│       └── SecurityConfig.java
│
├── src/main/resources/
│   ├── application.yml
│   ├── application-docker.yml
│   └── db/migration/                         # Flyway migrations
│       ├── V1__create_users_table.sql
│       ├── V2__create_login_attempts_table.sql
│       └── V3__create_sessions_table.sql
│
└── src/test/java/com/sms/auth/
    ├── controller/
    │   └── AuthControllerTest.java           # Integration tests
    ├── service/
    │   ├── AuthServiceTest.java              # Unit tests
    │   └── RateLimitServiceTest.java
    └── validation/
        ├── PasswordValidatorTest.java
        └── CambodiaPhoneValidatorTest.java
```

**Structure Decision**: Microservices architecture. This feature extends the existing auth-service microservice (one of eight services in the system). The auth-service maintains single responsibility for authentication, uses PostgreSQL (auth_db) for persistence, and communicates via REST API. No new services created.

---

## Implementation Checklist

- [ ] **Step 1**: Create Flyway migration files (V1, V2, V3) in `auth-service/src/main/resources/db/migration/`
- [ ] **Step 2**: Implement JPA entities (User, LoginAttempt, Session)
- [ ] **Step 3**: Create JPA repositories (UserRepository, LoginAttemptRepository, SessionRepository)
- [ ] **Step 4**: Implement DTOs (RegisterRequest, LoginRequest, AuthResponse, BaseResponse, ErrorCode)
- [ ] **Step 5**: Implement validators (PasswordValidator, CambodiaPhoneValidator)
- [ ] **Step 6**: Implement services (AuthService, RateLimitService, JwtTokenProvider)
- [ ] **Step 7**: Implement REST controller (AuthController)
- [ ] **Step 8**: Implement global exception handler (GlobalExceptionHandler)
- [ ] **Step 9**: Write unit tests (AuthServiceTest, RateLimitServiceTest, validators)
- [ ] **Step 10**: Write integration tests (AuthControllerTest with Testcontainers)
- [ ] **Step 11**: Manual testing with curl (register, login, rate limiting)
- [ ] **Step 12**: Update documentation and create pull request

See [quickstart.md](./quickstart.md) for detailed implementation steps with code examples.

---

## Key Implementation Notes

1. **No backend i18n**: Backend returns only error codes (no error messages). Frontend handles translation to Khmer/English based on error codes.

2. **BaseResponse format**: All endpoints return `{errorCode: ErrorEnum, data: T}` - no errorMessage field

3. **Rate limiting**: Database-backed with sliding window query (5 failed attempts in last 15 minutes)

4. **Session management**: Hybrid approach - JWT for stateless validation, database tracks sessions for audit trail

5. **Phone validation**: Cambodia format only - `^\+855[1-9]\d{7,8}$`

6. **Password strength**: Minimum 8 characters, 1 uppercase, 1 lowercase, 1 number, 1 special character

7. **JWT expiration**: 24 hours (86400000 milliseconds)

8. **BCrypt cost factor**: 12 (balances security and performance)

---

## References

- **Feature Specification**: [spec.md](./spec.md)
- **Technical Research**: [research.md](./research.md)
- **Database Design**: [data-model.md](./data-model.md)
- **Implementation Guide**: [quickstart.md](./quickstart.md)
- **Architecture Decisions**: [architecture-decisions.md](./architecture-decisions.md)
- **API Contracts**: [contracts/register-api.yaml](./contracts/register-api.yaml), [contracts/login-api.yaml](./contracts/login-api.yaml)
- **Project Constitution**: [.specify/memory/constitution.md](../../.specify/memory/constitution.md)
