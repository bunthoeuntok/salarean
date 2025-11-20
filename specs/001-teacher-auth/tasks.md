# Implementation Tasks: Teacher Registration & Login

**Feature**: 001-teacher-auth | **Branch**: `001-teacher-auth` | **Created**: 2025-11-20

**Status**: Ready for Implementation

---

## Overview

This document provides dependency-ordered implementation tasks for the teacher authentication feature. Tasks are organized by user story to enable independent, incremental delivery.

**Total Estimated Tasks**: 47 tasks across 6 phases

**Implementation Strategy**:
- **MVP**: Phase 3 (User Story 1 - Registration) delivers immediate value
- **Incremental**: Each user story is independently testable
- **Parallel**: Tasks marked [P] can run concurrently within a phase

---

## Dependencies & Execution Order

```
Phase 1: Setup (T001-T003)
    ↓
Phase 2: Foundational (T004-T013) - MUST complete before user stories
    ↓
Phase 3: User Story 1 [P1] - Registration (T014-T022) ← MVP
    ↓
Phase 4: User Story 2 [P1] - Login (T023-T030)
    ↓
Phase 5: User Story 3 [P2] - Session Management (T031-T037)
    ↓
Phase 6: Polish & Cross-Cutting (T038-T047)

Note: User Story 4 (Multilingual) is handled in frontend, not backend
```

**Parallelization Opportunities**:
- Within Phase 2: T005-T008 (JPA entities) can run in parallel
- Within Phase 3: T015-T017 (DTOs), T018-T019 (validators) can run in parallel
- Within Phase 4: T024-T026 can run in parallel with T027-T029
- Cross-phase: After Phase 2 completes, Phase 3 and 4 tasks can partially overlap

---

## Phase 1: Project Setup

**Goal**: Initialize database schema and verify Spring Boot configuration

**Duration**: ~30 minutes

### Tasks

- [X] T001 Create Flyway migration V1__create_users_table.sql in auth-service/src/main/resources/db/migration/
- [X] T002 Create Flyway migration V2__create_login_attempts_table.sql in auth-service/src/main/resources/db/migration/
- [X] T003 Create Flyway migration V3__create_sessions_table.sql in auth-service/src/main/resources/db/migration/

**Verification**: Run `mvn spring-boot:run -Dspring-boot.run.profiles=docker` and verify all 3 tables created in auth_db

**Files Created**: 3 SQL migration files

---

## Phase 2: Foundational Components

**Goal**: Implement core infrastructure needed by all user stories

**Duration**: ~2-3 hours

**Critical Path**: These tasks MUST complete before any user story implementation

### Tasks

#### JPA Entities (Parallel Group)

- [X] T004 [P] Create User entity in auth-service/src/main/java/com/sms/auth/model/User.java
- [X] T005 [P] Create LoginAttempt entity in auth-service/src/main/java/com/sms/auth/model/LoginAttempt.java
- [X] T006 [P] Create Session entity in auth-service/src/main/java/com/sms/auth/model/Session.java

#### JPA Repositories (Parallel Group)

- [X] T007 [P] Create UserRepository in auth-service/src/main/java/com/sms/auth/repository/UserRepository.java
- [X] T008 [P] Create LoginAttemptRepository in auth-service/src/main/java/com/sms/auth/repository/LoginAttemptRepository.java
- [X] T009 [P] Create SessionRepository in auth-service/src/main/java/com/sms/auth/repository/SessionRepository.java

#### Security Infrastructure

- [X] T010 Configure BCrypt PasswordEncoder (cost factor 12) in auth-service/src/main/java/com/sms/auth/security/PasswordEncoderConfig.java
- [X] T011 Implement JwtTokenProvider in auth-service/src/main/java/com/sms/auth/security/JwtTokenProvider.java
- [X] T012 Implement RateLimitService in auth-service/src/main/java/com/sms/auth/service/RateLimitService.java

**Verification**:
- All entities persist correctly to database
- BCrypt hashing works (cost factor 12)
- JWT generation/validation works
- Rate limiting query returns correct count

**Files Created**: 10 Java files

---

## Phase 3: User Story 1 - Teacher Registration [P1]

**User Story**: A new teacher creates an account to access the student management system for the first time.

**Goal**: Teachers can register with email, Cambodia phone (+855 format), and password. System validates all inputs and creates their account.

**Duration**: ~4-5 hours

**Independent Test Criteria**:
✅ Navigate to registration page
✅ Enter valid email, Cambodia phone (+855 XX XXX XXX), and strong password
✅ Account created successfully
✅ Receive confirmation with JWT token
✅ Duplicate email rejected with errorCode: DUPLICATE_EMAIL
✅ Duplicate phone rejected with errorCode: DUPLICATE_PHONE
✅ Weak password rejected with errorCode: INVALID_PASSWORD
✅ Invalid phone format rejected with errorCode: INVALID_PHONE_FORMAT

### Tasks

#### DTOs & Response Format (Parallel Group)

- [X] T014 [P] [US1] Create ErrorCode enum in auth-service/src/main/java/com/sms/auth/dto/ErrorCode.java
- [X] T015 [P] [US1] Create BaseResponse wrapper in auth-service/src/main/java/com/sms/auth/dto/BaseResponse.java
- [X] T016 [P] [US1] Create RegisterRequest DTO in auth-service/src/main/java/com/sms/auth/dto/RegisterRequest.java
- [X] T017 [P] [US1] Create AuthResponse DTO in auth-service/src/main/java/com/sms/auth/dto/AuthResponse.java

#### Validators (Parallel Group)

- [X] T018 [P] [US1] Implement PasswordValidator in auth-service/src/main/java/com/sms/auth/validation/PasswordValidator.java
- [X] T019 [P] [US1] Implement CambodiaPhoneValidator in auth-service/src/main/java/com/sms/auth/validation/CambodiaPhoneValidator.java

#### Business Logic

- [X] T020 [US1] Implement AuthService.register() method in auth-service/src/main/java/com/sms/auth/service/AuthService.java

#### API Layer

- [X] T021 [US1] Implement POST /api/auth/register endpoint in auth-service/src/main/java/com/sms/auth/controller/AuthController.java
- [X] T022 [US1] Implement GlobalExceptionHandler in auth-service/src/main/java/com/sms/auth/exception/GlobalExceptionHandler.java

**Verification**:
```bash
# Test successful registration
curl -X POST http://localhost:8080/auth-service/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teacher@school.edu.kh",
    "phoneNumber": "+85512345678",
    "password": "SecurePass123!",
    "preferredLanguage": "en"
  }'

# Expected: {"errorCode":"SUCCESS","data":{"userId":"...","token":"..."}}
```

**Files Created**: 9 Java files
**Story Completion**: User Story 1 fully functional and independently testable

---

## Phase 4: User Story 2 - Teacher Login [P1]

**User Story**: A registered teacher logs into the system using either their email address or phone number along with their password.

**Goal**: Teachers can authenticate with email OR phone + password. System creates session and returns JWT token.

**Duration**: ~3-4 hours

**Independent Test Criteria**:
✅ Login with email + password succeeds
✅ Login with phone + password succeeds
✅ Receive JWT token valid for 24 hours
✅ Invalid credentials return errorCode: INVALID_CREDENTIALS
✅ Rate limiting triggers after 5 failed attempts (errorCode: RATE_LIMIT_EXCEEDED)
✅ Session record created in database

### Tasks

#### DTOs

- [X] T023 [P] [US2] Create LoginRequest DTO in auth-service/src/main/java/com/sms/auth/dto/LoginRequest.java

#### Exception Handling (Parallel Group)

- [X] T024 [P] [US2] Create DuplicateEmailException in auth-service/src/main/java/com/sms/auth/exception/DuplicateEmailException.java
- [X] T025 [P] [US2] Create DuplicatePhoneException in auth-service/src/main/java/com/sms/auth/exception/DuplicatePhoneException.java
- [X] T026 [P] [US2] Create InvalidPasswordException in auth-service/src/main/java/com/sms/auth/exception/InvalidPasswordException.java
- [X] T027 [P] [US2] Create InvalidCredentialsException in auth-service/src/main/java/com/sms/auth/exception/InvalidCredentialsException.java
- [X] T028 [P] [US2] Create RateLimitExceededException in auth-service/src/main/java/com/sms/auth/exception/RateLimitExceededException.java

#### Business Logic

- [X] T029 [US2] Implement AuthService.login() method in auth-service/src/main/java/com/sms/auth/service/AuthService.java

#### API Layer

- [X] T030 [US2] Implement POST /api/auth/login endpoint in auth-service/src/main/java/com/sms/auth/controller/AuthController.java

**Verification**:
```bash
# Test login with email
curl -X POST http://localhost:8080/auth-service/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrPhone": "teacher@school.edu.kh",
    "password": "SecurePass123!"
  }'

# Test login with phone
curl -X POST http://localhost:8080/auth-service/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrPhone": "+85512345678",
    "password": "SecurePass123!"
  }'

# Test rate limiting (run 6 times with wrong password)
for i in {1..6}; do
  curl -X POST http://localhost:8080/auth-service/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"emailOrPhone":"teacher@school.edu.kh","password":"wrong"}'
  echo "\nAttempt $i"
done

# Expected: First 5 return INVALID_CREDENTIALS, 6th returns RATE_LIMIT_EXCEEDED
```

**Files Created**: 8 Java files
**Story Completion**: User Story 2 fully functional and independently testable

---

## Phase 5: User Story 3 - Session Management [P2]

**User Story**: A logged-in teacher's session remains active across page refreshes and browser restarts (within timeout period).

**Goal**: JWT tokens persist for 24 hours. Session metadata tracked in database for audit trail.

**Duration**: ~2-3 hours

**Independent Test Criteria**:
✅ Session persists across page refreshes
✅ Session persists across browser restarts (within 24 hours)
✅ Session expires after 24 hours of inactivity
✅ Session metadata (IP, user agent) recorded in database
✅ Multiple concurrent sessions allowed from different devices

### Tasks

#### Session Management Enhancement

- [X] T031 [US3] Add session creation logic to AuthService.register() (update existing method)
- [X] T032 [US3] Add session creation logic to AuthService.login() (update existing method)
- [X] T033 [US3] Implement session validation in JwtTokenProvider (update existing class)

#### Cleanup & Maintenance

- [X] T034 [P] [US3] Create SessionCleanupService in auth-service/src/main/java/com/sms/auth/service/SessionCleanupService.java
- [X] T035 [US3] Add @Scheduled cleanup job for expired sessions in SessionCleanupService
- [X] T036 [US3] Add @Scheduled cleanup job for old login attempts (7-year retention) in SessionCleanupService

#### Configuration

- [X] T037 [US3] Enable Spring @Scheduled in auth-service/src/main/java/com/sms/auth/AuthServiceApplication.java

**Verification**:
```bash
# 1. Login and get token
TOKEN=$(curl -X POST http://localhost:8080/auth-service/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"emailOrPhone":"teacher@school.edu.kh","password":"SecurePass123!"}' \
  | jq -r '.data.token')

# 2. Use token in subsequent request (simulates page refresh)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/auth-service/api/some-protected-endpoint

# 3. Check session in database
docker exec -it sms-postgres psql -U smsadmin -d auth_db \
  -c "SELECT id, user_id, created_at, expires_at FROM sessions ORDER BY created_at DESC LIMIT 1;"

# Expected: Session record exists with 24-hour expiration
```

**Files Created**: 1 Java file (SessionCleanupService)
**Files Updated**: 3 Java files
**Story Completion**: User Story 3 fully functional and independently testable

---

## Phase 6: Polish & Cross-Cutting Concerns

**Goal**: Production readiness, testing, documentation, and deployment preparation

**Duration**: ~4-6 hours

### Testing Tasks

#### Unit Tests

- [X] T038 [P] Write PasswordValidator unit tests in auth-service/src/test/java/com/sms/auth/validation/PasswordValidatorTest.java
- [X] T039 [P] Write CambodiaPhoneValidator unit tests in auth-service/src/test/java/com/sms/auth/validation/CambodiaPhoneValidatorTest.java
- [X] T040 [P] Write AuthService unit tests in auth-service/src/test/java/com/sms/auth/service/AuthServiceTest.java
- [X] T041 [P] Write RateLimitService unit tests in auth-service/src/test/java/com/sms/auth/service/RateLimitServiceTest.java

#### Integration Tests

- [X] T042 Write AuthController integration tests in auth-service/src/test/java/com/sms/auth/controller/AuthControllerTest.java
- [X] T043 Write end-to-end registration + login flow test in auth-service/src/test/java/com/sms/auth/integration/AuthFlowIntegrationTest.java

### Configuration & Deployment

- [X] T044 Update application.yml with JWT configuration (if not already done)
- [X] T045 Verify docker-compose.yml has JWT_SECRET environment variable (already done - just verify)

### Documentation

- [X] T047 Update README.md with API documentation links (specs/001-teacher-auth/contracts/*.yaml)

**Verification**:
```bash
# Run all tests
cd auth-service
mvn test

# Run integration tests with Testcontainers
mvn verify -Dspring.profiles.active=test

# Expected: All tests pass, coverage >85%
```

**Files Created**: 6 test files
**Files Updated**: 3 configuration/documentation files

---

## Task Execution Guide

### Sequential Execution (Safest)

Execute tasks in order T001 → T047. Each task builds on the previous.

```bash
# Example workflow
# 1. Database migrations
# Create V1__create_users_table.sql
# Create V2__create_login_attempts_table.sql
# Create V3__create_sessions_table.sql
# Run: mvn spring-boot:run

# 2. Foundational components (T004-T013)
# Create all entities, repositories, security classes

# 3. User Story 1 (T014-T022)
# Implement registration endpoint
# Test: curl POST /api/auth/register

# 4. User Story 2 (T023-T030)
# Implement login endpoint
# Test: curl POST /api/auth/login

# 5. User Story 3 (T031-T037)
# Add session management
# Test: verify session persistence

# 6. Polish (T038-T047)
# Write tests, update docs
# Test: mvn test
```

### Parallel Execution (Faster)

Within each phase, execute [P] marked tasks concurrently:

**Phase 2 Parallelization**:
```
Worker 1: T004, T007, T010
Worker 2: T005, T008, T011
Worker 3: T006, T009, T012
Then: T013 (depends on T010-T012)
```

**Phase 3 Parallelization**:
```
Worker 1: T014, T016, T018, T020
Worker 2: T015, T017, T019, T021
Then: T022 (depends on all DTOs)
```

**Phase 4 Parallelization**:
```
Worker 1: T023, T024, T026, T028
Worker 2: T025, T027, T029
Then: T030 (depends on T029)
```

**Phase 6 Parallelization**:
```
Worker 1: T038, T040, T042
Worker 2: T039, T041, T043
Worker 3: T044, T045, T046, T047
```

### MVP Delivery (Phase 3 Only)

For fastest time-to-value, implement only:
- Phase 1: Setup (T001-T003)
- Phase 2: Foundational (T004-T013)
- Phase 3: User Story 1 Registration (T014-T022)

This delivers a working registration endpoint in ~6-8 hours.

Then iterate:
- Sprint 2: Add Phase 4 (Login)
- Sprint 3: Add Phase 5 (Session Management)
- Sprint 4: Add Phase 6 (Testing & Polish)

---

## Task Summary

| Phase | Tasks | Duration | Parallel Tasks | User Story | Files Created |
|-------|-------|----------|----------------|------------|---------------|
| Phase 1: Setup | T001-T003 | 30 min | 0 | - | 3 SQL files |
| Phase 2: Foundational | T004-T013 | 2-3 hrs | 6 | - | 10 Java files |
| Phase 3: US1 Registration | T014-T022 | 4-5 hrs | 5 | P1 | 9 Java files |
| Phase 4: US2 Login | T023-T030 | 3-4 hrs | 5 | P1 | 8 Java files |
| Phase 5: US3 Session Mgmt | T031-T037 | 2-3 hrs | 1 | P2 | 1 Java file |
| Phase 6: Polish | T038-T047 | 4-6 hrs | 4 | - | 6 test files |
| **TOTAL** | **47 tasks** | **16-22 hrs** | **21 parallel** | **3 stories** | **37 files** |

**Note on User Story 4 (Multilingual)**: Handled entirely in frontend - no backend tasks required.

---

## Acceptance Criteria Verification

After completing all phases, verify these acceptance criteria from spec.md:

### User Story 1 - Registration
- [x] Valid email, phone (+855), password → account created
- [x] Duplicate email → errorCode: DUPLICATE_EMAIL
- [x] Duplicate phone → errorCode: DUPLICATE_PHONE
- [x] Weak password → errorCode: INVALID_PASSWORD
- [x] Invalid phone format → errorCode: INVALID_PHONE_FORMAT

### User Story 2 - Login
- [x] Login with email + password → success
- [x] Login with phone + password → success
- [x] Invalid credentials → errorCode: INVALID_CREDENTIALS
- [x] Unregistered email/phone → errorCode: INVALID_CREDENTIALS
- [x] 5 failed attempts → errorCode: RATE_LIMIT_EXCEEDED

### User Story 3 - Session Management
- [x] Page refresh → session persists
- [x] Browser restart within 24 hrs → session persists
- [x] Inactive for 24 hrs → session expired, login required
- [x] Session metadata (IP, user agent) recorded

### Success Criteria (from spec.md)
- [x] SC-001: Registration completes in <3 minutes
- [x] SC-003: Login completes in <5 seconds
- [x] SC-004: Zero logouts on page refresh (active session)
- [x] SC-006: 100% weak password validation
- [x] SC-007: 100% invalid email/phone validation
- [x] SC-008: Zero duplicate accounts
- [x] SC-009: Handle 100 concurrent requests
- [x] SC-010: Rate limiting blocks brute-force (5 attempts/15 min)

---

## Implementation Notes

### Response Format
All endpoints return: `{errorCode: ErrorEnum, data: T}`

**Success Example**:
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "teacher@school.edu.kh",
    "phoneNumber": "+85512345678",
    "preferredLanguage": "en",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "createdAt": "2025-11-20T10:30:00Z"
  }
}
```

**Error Example**:
```json
{
  "errorCode": "DUPLICATE_EMAIL",
  "data": null
}
```

### No Backend i18n
Backend returns only error codes. Frontend handles all translation to Khmer/English.

### Key Dependencies
- BCrypt cost factor: 12
- JWT expiration: 24 hours (86400000 ms)
- Rate limiting: 5 attempts per 15 minutes
- Session timeout: 24 hours inactivity
- Phone validation: `^\+855[1-9]\d{7,8}$`
- Password validation: Min 8 chars, 1 upper, 1 lower, 1 number, 1 special

---

## References

- **Feature Specification**: [spec.md](./spec.md)
- **Implementation Plan**: [plan.md](./plan.md)
- **Database Design**: [data-model.md](./data-model.md)
- **Developer Guide**: [quickstart.md](./quickstart.md)
- **API Contracts**: [contracts/register-api.yaml](./contracts/register-api.yaml), [contracts/login-api.yaml](./contracts/login-api.yaml)
- **Architecture Decisions**: [architecture-decisions.md](./architecture-decisions.md)

---

## Next Steps

1. **Start Implementation**: Begin with Phase 1 (T001-T003)
2. **Create GitHub Issues** (optional): Run `/speckit.taskstoissues` to convert tasks to issues
3. **Track Progress**: Update task checkboxes as you complete each task
4. **Test Incrementally**: Verify each phase before moving to the next

**Suggested First Task**: T001 - Create users table migration

Good luck with implementation! 🚀
