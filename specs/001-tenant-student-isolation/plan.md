# Implementation Plan: Teacher-Based Student Data Isolation

**Branch**: `001-tenant-student-isolation` | **Date**: 2025-12-07 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-tenant-student-isolation/spec.md`
**User Input**: "in student table add new column called teacher_id to indicate that student belongs to teacher, implement student cache and user able to reload cache"

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

This feature implements teacher-based data isolation where each teacher can only access students they created. The core implementation involves:

1. **Database Schema**: Add `teacher_id` column to the `students` table to track ownership
2. **Authorization Layer**: Enforce teacher ownership validation on all CRUD operations
3. **Caching Layer**: Implement Redis caching for student data with manual cache reload capability
4. **API Layer**: Update all student endpoints to filter by authenticated teacher's ID

**Key Technical Approach**:
- Extract teacher_id from JWT token (already available as `subject` in existing auth system)
- Add teacher_id filter to all JPA repository queries
- Implement Spring Cache with Redis for performance
- Provide cache eviction endpoint for teachers to force reload their student data

## Technical Context

**Language/Version**: Java 21 with Spring Boot 3.5.7
**Primary Dependencies**: Spring Data JPA, Spring Data Redis, Spring Cache, Hibernate, jjwt (0.12.5), PostgreSQL Driver
**Storage**: PostgreSQL 15+ (student_db database), Redis 7+ (for caching)
**Testing**: JUnit 5, Spring Boot Test, Testcontainers (for integration tests)
**Target Platform**: Docker container (Linux server)
**Project Type**: Microservice (student-service within monorepo architecture)
**Performance Goals**: Student list queries < 2 seconds (per SC-001), support 50+ concurrent teachers (per SC-004)
**Constraints**: 100% unauthorized access blocking (per SC-002), zero cross-teacher data leakage (per SC-006)
**Scale/Scope**: ~1000+ students per teacher, CRUD operations on Student entity, cache reload functionality

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Principle I: Microservices-First
- ✅ **PASS**: Feature is implemented within existing `student-service` boundary
- ✅ **PASS**: No new service created (modification to existing service)
- ✅ **PASS**: Service isolation maintained (teacher isolation is internal to student-service)

### Principle II: Security-First
- ✅ **PASS**: JWT authentication already enforced on all student endpoints
- ✅ **PASS**: Authorization enhanced with teacher ownership validation
- ✅ **PASS**: Student PII remains encrypted (no changes to encryption)
- ✅ **PASS**: Audit logging preserved (createdBy/updatedBy fields exist)
- ✅ **PASS**: No new secrets required (uses existing JWT infrastructure)

### Principle III: Simplicity (YAGNI)
- ✅ **PASS**: Minimal complexity - single column addition + query filters
- ✅ **PASS**: Uses existing Spring Cache + Redis (already in stack)
- ✅ **PASS**: No new abstractions - standard JPA repository patterns
- ✅ **PASS**: No premature optimization - cache added per user requirement

### Principle IV: Observability
- ✅ **PASS**: Health endpoints unaffected (existing `/actuator/health`)
- ✅ **PASS**: Logging maintained (Spring Boot structured logging)
- ⚠️ **ACTION**: Add metrics for unauthorized access attempts (count blocked requests)

### Principle V: Test Discipline
- ✅ **PASS**: Requires unit tests for service layer teacher validation
- ✅ **PASS**: Requires integration tests for repository queries with teacher_id filter
- ✅ **PASS**: Requires contract tests for unauthorized access scenarios

### Principle VI: Backend API Conventions
- ✅ **PASS**: Returns `ApiResponse<T>` wrapper (existing pattern in student-service)
- ✅ **PASS**: Error codes: `UNAUTHORIZED_ACCESS`, `STUDENT_NOT_FOUND` (machine-readable)
- ✅ **PASS**: Global exception handler already exists (`@RestControllerAdvice`)

**GATE RESULT**: ✅ **PASSED** (1 action item for observability - non-blocking)

## Project Structure

### Documentation (this feature)

```text
specs/001-tenant-student-isolation/
├── spec.md              # Feature specification (completed)
├── plan.md              # This file (in progress)
├── research.md          # Phase 0 output (to be generated)
├── data-model.md        # Phase 1 output (to be generated)
├── quickstart.md        # Phase 1 output (to be generated)
├── contracts/           # Phase 1 output (to be generated)
│   └── endpoints.md     # API contract specifications
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (student-service)

```text
student-service/
├── src/main/java/com/sms/student/
│   ├── config/
│   │   ├── CorsConfig.java
│   │   ├── OpenAPIConfig.java
│   │   ├── SecurityConfig.java
│   │   └── RedisConfig.java              # [NEW] Redis cache configuration
│   ├── controller/
│   │   ├── StudentController.java        # [MODIFY] Add cache reload endpoint
│   │   └── CacheController.java          # [NEW] Cache management endpoints
│   ├── dto/
│   │   ├── StudentRequest.java           # [EXISTING] No changes
│   │   ├── StudentResponse.java          # [EXISTING] No changes
│   │   └── CacheReloadResponse.java      # [NEW] Cache reload status
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java   # [MODIFY] Add unauthorized access handling
│   │   └── UnauthorizedAccessException.java # [NEW] Custom exception
│   ├── model/
│   │   ├── Student.java                  # [MODIFY] Add teacher_id field
│   │   └── ParentContact.java            # [EXISTING] No changes
│   ├── repository/
│   │   ├── StudentRepository.java        # [MODIFY] Add teacher_id filters to queries
│   │   └── ParentContactRepository.java  # [EXISTING] No changes
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java  # [EXISTING] No changes
│   │   └── TeacherContextHolder.java     # [NEW] Thread-local teacher ID storage
│   ├── service/
│   │   ├── interfaces/
│   │   │   ├── IStudentService.java      # [MODIFY] Add cache methods
│   │   │   └── ICacheService.java        # [NEW] Cache management interface
│   │   ├── StudentService.java           # [MODIFY] Add teacher validation + caching
│   │   └── CacheService.java             # [NEW] Cache operations implementation
│   └── validation/
│       └── KhmerPhoneValidator.java      # [EXISTING] No changes
├── src/main/resources/
│   ├── application.yml                   # [MODIFY] Add Redis cache config
│   ├── application-docker.yml            # [MODIFY] Add Redis connection
│   └── db/migration/
│       └── V6__add_teacher_id_to_students.sql # [NEW] Migration script
└── src/test/java/com/sms/student/
    ├── service/
    │   ├── StudentServiceTest.java       # [MODIFY] Add teacher isolation tests
    │   └── CacheServiceTest.java         # [NEW] Cache functionality tests
    └── repository/
        └── StudentRepositoryTest.java    # [MODIFY] Add teacher_id query tests
```

**Structure Decision**: This is a microservice modification within the existing monorepo architecture. All changes are scoped to the `student-service` directory. No new microservices are created (adheres to Principle I: Microservices-First).

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

**No violations** - All constitution checks passed. No complexity justifications required.

---

## Post-Design Constitution Re-Check

*Re-evaluation after Phase 1 design completion*

### Principle I: Microservices-First
- ✅ **PASS**: Implementation remains within student-service boundary
- ✅ **PASS**: No new services created
- ✅ **PASS**: Service isolation maintained

### Principle II: Security-First
- ✅ **PASS**: JWT authentication enforced on all endpoints
- ✅ **PASS**: Teacher ownership validation on every operation
- ✅ **PASS**: Unauthorized access returns 401 with error code
- ✅ **PASS**: No SQL injection (parameterized queries)
- ✅ **PASS**: Cache keys scoped by teacher_id (no cross-teacher pollution)

### Principle III: Simplicity (YAGNI)
- ✅ **PASS**: Single column addition + query filters
- ✅ **PASS**: Standard Spring Cache patterns (no custom abstractions)
- ✅ **PASS**: ThreadLocal context holder (proven pattern, minimal code)
- ✅ **PASS**: No over-engineering (cache TTL + manual eviction, simple)

### Principle IV: Observability
- ✅ **PASS**: Health endpoints unchanged
- ✅ **PASS**: Spring Boot structured logging maintained
- ✅ **ACTION COMPLETED**: Added logging for unauthorized access attempts (in GlobalExceptionHandler)

### Principle V: Test Discipline
- ✅ **PASS**: Unit tests planned for service layer (StudentServiceTest)
- ✅ **PASS**: Integration tests planned for repository (StudentRepositoryTest)
- ✅ **PASS**: Contract tests planned for unauthorized scenarios
- ✅ **PASS**: Test checklist in quickstart.md

### Principle VI: Backend API Conventions
- ✅ **PASS**: All endpoints return `ApiResponse<T>`
- ✅ **PASS**: Error codes are machine-readable (UNAUTHORIZED_ACCESS, STUDENT_NOT_FOUND)
- ✅ **PASS**: Global exception handler updated
- ✅ **PASS**: No i18n messages in backend (error codes only)

**FINAL GATE RESULT**: ✅ **PASSED** - All principles satisfied. Ready for implementation.
