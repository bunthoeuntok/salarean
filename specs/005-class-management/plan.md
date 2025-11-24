# Implementation Plan: Class Management API

**Branch**: `005-class-management` | **Date**: 2025-11-24 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/005-class-management/spec.md`

## Summary

This feature extends the existing student-service with class management capabilities, enabling teachers to manage their academic classes through 7 RESTful endpoints. The implementation adds class CRUD operations (list, view details, create, update, archive), student roster views, and read-only access to enrollment history. Redis caching optimizes performance for frequently accessed class lists and student rosters. By integrating into student-service rather than creating a new microservice, we eliminate cross-service calls for roster queries, enable atomic enrollment transactions within a single database, and reduce operational complexity. Class and student data coexist in the student_db database, leveraging the established Spring Boot 3.5.7 architecture.

## Technical Context

**Language/Version**: Java 21 with Spring Boot 3.5.7 (existing student-service stack)
**Primary Dependencies**:
- Spring Boot Starter Web (REST API) - EXISTING
- Spring Boot Starter Data JPA (Database access) - EXISTING
- Spring Boot Starter Data Redis (Caching layer) - **NEW**
- Spring Boot Starter Security (Authorization) - EXISTING
- Spring Boot Starter Validation (Input validation) - EXISTING
- Netflix Eureka Client (Service discovery) - EXISTING
- sms-common 1.0.0 (Shared utilities, ApiResponse, ErrorCode) - EXISTING, **WILL EXTEND**
- PostgreSQL Driver (Database connectivity) - EXISTING
- Lombok 1.18.36 (Boilerplate reduction) - EXISTING
- Flyway (Database migrations) - EXISTING

**Storage**: PostgreSQL 15+ (existing `student_db` database; **NEW tables**: classes, enrollment_history)
**Caching**: Redis 7+ (class lists with 30min TTL, class details with 15min TTL) - **NEW dependency for student-service**
**Testing**: JUnit 5, Spring Boot Test, Mockito (unit tests), TestContainers (integration tests) - EXISTING
**Target Platform**: Docker containers orchestrated via Docker Compose, deployed on Linux servers - EXISTING
**Project Type**: Service extension (adding class management package to existing student-service microservice)
**Performance Goals**:
- Class list retrieval: <2s (first load), <500ms (cached)
- Class details with student roster: <2s
- Cache hit rate: >70% after initial usage
- Support 100 concurrent teachers without degradation

**Constraints**:
- Must follow project microservice standards (package structure, JWT auth, ApiResponse format)
- Must integrate with existing auth-service (JWT validation)
- Must use sms-common base cache abstraction for cache operations (**NEW sms-common enhancement**)
- Must handle Redis failures gracefully (cache-aside pattern with fallback to database)
- All timestamps in Cambodia timezone (Asia/Phnom_Penh)
- **No schedule data** - deferred to future schedule service (per clarification #3)
- Enrollment history is **read-only** - created by separate enrollment feature (per clarification #5)

**Scale/Scope**:
- Estimated 50-200 teachers per school
- 10-50 classes per teacher
- 20-40 students per class
- ~1000-5000 total classes in production
- 7 REST endpoints (**3 GET read-only for history, 4 CRUD for classes**)
- **Extends existing student-service** (no new service)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### I. Microservices-First ✅ PASS (with clarification)

**Service Boundary**: Class management extends student-service bounded context rather than creating a new microservice.

- ✅ **Independently deployable**: student-service remains independently deploy able with its own Docker container, PostgreSQL database (student_db), port (8082), and deployment lifecycle
- ✅ **Loosely coupled**: student-service communicates with other services (auth-service for JWT) via REST APIs through API Gateway; **internal coupling between classes and students is acceptable within single bounded context**
- ✅ **Single responsibility**: student-service owns student AND class management domains - both are part of the "student records and class enrollment" bounded context. Classes cannot exist without students; student enrollment cannot occur without classes.
- ✅ **Failure-isolated**: Redis cache failures degrade to database reads (no cascade); auth-service unavailability only affects new requests (existing JWTs still validate locally)

**Justification for Service Colocation** (Per Clarification #1):
- Classes and students are **tightly coupled** in the education domain - student enrollment links both entities
- Eliminates cross-service HTTP calls for roster queries (no network latency, no Feign client overhead)
- Enables **atomic transactions** for enrollment operations within single database
- Reduces operational complexity (no additional service to deploy/monitor/scale)
- Follows Domain-Driven Design principle: "classes" and "students" are subdomains within the "Student Records Management" bounded context

**Architecture Decision**: This is an **acceptable pragmatic variation** of Principle I. The constitution states "Each service owns one bounded context" - student-service owns "Student Records Management" which includes both student profiles AND the classes they enroll in. This is a **single logical bounded context**, not a violation.

###II. Security-First ✅ PASS

- ✅ **Authentication**: All class management endpoints require valid JWT tokens from auth-service
- ✅ **Authorization**: RBAC implemented via JWT claims; teacher can only access their own classes, administrators have broader permissions
- ✅ **Data protection**: Class data encrypted in transit (HTTPS via nginx); student PII remains in student tables (not duplicated in class tables)
- ✅ **Audit logging**: All class mutations (create, update, archive) logged with teacher ID and timestamp via SLF4J
- ✅ **Secrets management**: Database credentials, JWT secret, Redis password via environment variables; no hardcoded secrets

**Note**: Student enrollment history records are created by enrollment operations (separate feature) with proper audit trails. Class management only provides read access.

### III. Simplicity (YAGNI) ✅ PASS

- ✅ **No premature optimization**: Basic CRUD with standard JPA repositories; Redis cache added only for documented performance requirement (SC-001, SC-002)
- ✅ **No speculative features**: Implements only the 7 required endpoints; explicitly excludes scheduling (future service), bulk operations, class cloning
- ✅ **Minimal dependencies**: Uses established student-service stack; **ONE new dependency**: Spring Data Redis (justified by caching requirement FR-003, FR-006)
- ✅ **Clear code over clever code**: Standard Spring Boot REST controller pattern, service layer, JPA repositories

**Complexity additions**:
1. **Base caching framework in sms-common**: Justified by requirement to standardize cache operations across future services and this feature's multi-cache needs (class list, class details, enrollment history)
2. **Redis caching layer in student-service**: Justified by explicit performance requirements (SC-001, SC-002, SC-007) and user specification ("please consider to use redis cache")

### IV. Observability ✅ PASS

- ✅ **Health endpoints**: Existing `/actuator/health` in student-service; **will add** Redis connection health indicator
- ✅ **Structured logging**: JSON-formatted logs with SLF4J + Logback (existing pattern); correlation IDs from JWT for distributed tracing
- ✅ **Metrics**: Spring Boot Actuator metrics for endpoint latency, **new**: cache hit/miss rates, database query times for class operations
- ✅ **Alerting thresholds**:
  - Cache hit rate <50% (investigate cache TTL or key strategy)
  - Class endpoint p95 latency >3s (performance degradation)
  - Redis connection failures (infrastructure issue)

### V. Test Discipline ✅ PASS

- ✅ **Unit tests**: Service layer business logic (class validation, cache invalidation, authorization checks) with JUnit 5 + Mockito
- ✅ **Integration tests**:
  - Repository layer with TestContainers PostgreSQL (existing pattern)
  - **NEW**: Cache operations with TestContainers Redis
  - Controller layer with MockMvc for contract validation
- ✅ **Contract tests**: OpenAPI schema validation for all 7 endpoints (request/response DTOs)
- ✅ **Test independence**: Tests use TestContainers for isolated database/cache; no shared state between test classes

**Coverage goals**:
- Service layer: 90% code coverage
- Repository layer: 80% coverage (standard CRUD operations)
- Controller layer: 100% endpoint coverage

### VI. Backend API Conventions ✅ PASS

- ✅ **Response wrapper**: All endpoints return `ApiResponse<T>` from sms-common (existing pattern)
- ✅ **Error codes**: **NEW** error codes added to student-service `ErrorCode` enum:
  - `CLASS_NOT_FOUND` (404)
  - `UNAUTHORIZED_CLASS_ACCESS` (403)
  - `DUPLICATE_CLASS_NAME` (400)
  - `INVALID_CLASS_DATA` (400)
  - `INVALID_ACADEMIC_YEAR_FORMAT` (400)
  - Plus existing SUCCESS, VALIDATION_FAILED from sms-common
- ✅ **Status codes**: Standard HTTP codes (200, 400, 401, 403, 404, 500)
- ✅ **Internationalization separation**: Backend returns only error codes; frontend translates to Khmer/English
- ✅ **Global exception handling**: Extend existing `@RestControllerAdvice` in student-service to handle class-specific exceptions

**Example Response**:
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "id": "uuid-123",
    "name": "Mathematics Grade 10A",
    "gradeLevel": "GRADE_10",
    "subject": "MATHEMATICS",
    "academicYear": "2024-2025",
    "capacity": 40,
    "currentEnrollment": 35,
    "teacherId": "uuid-456",
    "archived": false
  }
}
```

### Constitution Compliance Summary

✅ **ALL GATES PASSED** - Feature complies with all six constitution principles.

**Architectural Justification**: Extending student-service with class management is **more aligned** with Principle I (single bounded context) than creating a separate service would be. "Student Records Management" logically includes both student profiles and the classes students enroll in - these are inseparable in the education domain.

## Project Structure

### Documentation (this feature)

```text
specs/005-class-management/
├── spec.md              # Feature specification (COMPLETED with clarifications)
├── plan.md              # This file (IN PROGRESS - /speckit.plan output)
├── research.md          # Phase 0 output (PENDING - will generate)
├── data-model.md        # Phase 1 output (PENDING - will generate)
├── quickstart.md        # Phase 1 output (PENDING - will generate)
├── contracts/           # Phase 1 output (PENDING - will generate)
│   └── openapi.yml      # OpenAPI 3.0 specification for all 7 endpoints
├── checklists/
│   └── requirements.md  # Specification quality checklist (COMPLETED)
└── tasks.md             # Phase 2 output (PENDING - /speckit.tasks command)
```

### Source Code (repository root)

**Extends existing student-service** (no new service directory):

```text
student-service/                        # EXISTING SERVICE - WILL EXTEND
├── src/
│   ├── main/
│   │   ├── java/com/sms/student/
│   │   │   ├── StudentServiceApplication.java  # EXISTING
│   │   │   ├── classmanagement/        # NEW PACKAGE for class features
│   │   │   │   ├── controller/
│   │   │   │   │   └── ClassController.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── ClassListResponse.java
│   │   │   │   │   ├── ClassDetailResponse.java
│   │   │   │   │   ├── ClassCreateRequest.java
│   │   │   │   │   ├── ClassUpdateRequest.java
│   │   │   │   │   └── EnrollmentHistoryResponse.java
│   │   │   │   ├── model/
│   │   │   │   │   ├── ClassEntity.java
│   │   │   │   │   ├── EnrollmentHistory.java
│   │   │   │   │   └── GradeLevel.java  # Enum (GRADE_1 to GRADE_12)
│   │   │   │   ├── repository/
│   │   │   │   │   ├── ClassRepository.java
│   │   │   │   │   └── EnrollmentHistoryRepository.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── ClassService.java
│   │   │   │   │   ├── ClassServiceImpl.java
│   │   │   │   │   └── ClassCacheService.java
│   │   │   │   └── exception/
│   │   │   │       ├── ClassNotFoundException.java
│   │   │   │       ├── UnauthorizedClassAccessException.java
│   │   │   │       └── DuplicateClassException.java
│   │   │   ├── config/              # EXISTING - WILL MODIFY
│   │   │   │   ├── RedisConfig.java     # NEW - Redis configuration
│   │   │   │   ├── OpenAPIConfig.java   # MODIFY - add class endpoints
│   │   │   │   └── SecurityConfig.java  # EXISTING - no changes needed
│   │   │   ├── exception/           # EXISTING - WILL EXTEND
│   │   │   │   ├── ErrorCode.java       # MODIFY - add class error codes
│   │   │   │   └── GlobalExceptionHandler.java  # MODIFY - handle class exceptions
│   │   │   ├── model/               # EXISTING student entities
│   │   │   ├── repository/          # EXISTING student repositories
│   │   │   └── service/             # EXISTING student services
│   │   └── resources/
│   │       ├── application.yml          # MODIFY - add Redis config
│   │       ├── application-docker.yml   # MODIFY - add Redis config
│   │       └── db/migration/            # EXISTING - WILL ADD
│   │           ├── V5__create_classes_table.sql          # NEW
│   │           └── V6__create_enrollment_history_table.sql  # NEW
│   └── test/
│       ├── java/com/sms/student/
│       │   ├── classmanagement/         # NEW - class management tests
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   └── repository/
│       │   └── integration/             # EXISTING - may add class tests
│       └── resources/
│           └── application-test.yml     # MODIFY - add test Redis config

sms-common/                             # EXISTING - ENHANCEMENTS NEEDED
├── src/main/java/com/sms/common/
│   ├── cache/                          # NEW - Base caching framework
│   │   ├── CacheService.java           # Abstract base cache interface
│   │   ├── RedisCacheService.java      # Redis implementation
│   │   └── CacheKeyGenerator.java      # Standardized cache key generation
│   ├── dto/                            # EXISTING
│   │   └── ApiResponse.java            # Already exists
│   └── constant/
│       └── CommonConstants.java        # EXISTING - may add grade level constants

docker-compose.yml                      # MODIFY - Update student-service to include Redis dependency
.env                                    # MODIFY - Add Redis configuration for student-service
```

**Structure Decision**:

This feature follows **Service Extension** pattern (not Option 1, 2, or 3 from template). Class management is integrated as a **new package** (`classmanagement/`) within the existing student-service directory structure.

Key structural decisions:
1. **Package organization**: `com.sms.student.classmanagement.*` - clear separation from student package while maintaining service cohesion
2. **Shared database**: Classes stored in existing `student_db` PostgreSQL database alongside student tables
3. **Shared configuration**: Extends existing student-service Spring Boot configuration (application.yml) with Redis settings
4. **Base cache in sms-common**: New `cache/` package provides abstract `CacheService` interface with Redis implementation
5. **Flyway migration numbering**: Continues from existing student-service migrations (V5__, V6__ instead of V1__, V2__)

**Rationale**: Package-based separation (`classmanagement/*`) provides clear module boundaries within the monolithic service, making future extraction to a separate microservice easier if needed. This follows the "modular monolith" pattern recommended for bounded subdomains.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

**STATUS**: No violations - all constitution checks passed. This table is empty.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| N/A       | N/A        | N/A                                 |

**Complexity Justification Notes**:

While there are no constitution violations, two complexity additions warrant documentation per Principle III:

1. **Base Caching Framework (sms-common/cache/)**:
   - **What**: Abstract `CacheService` interface + Redis implementation in sms-common
   - **Why**: Standardizes cache operations across student-service (class lists + class details) and future services needing caching patterns
   - **Alternative rejected**: Implementing Redis directly in student-service would work for this feature but creates technical debt when other services need caching (code duplication, inconsistent cache key formats)
   - **Complexity cost**: ~3 additional classes in sms-common, but saves 5+ classes per service that needs caching

2. **Redis Caching Layer in student-service**:
   - **What**: Spring Data Redis integration with cache-aside pattern
   - **Why**: Explicit performance requirement (SC-001: <500ms cached response, SC-007: >70% hit rate)
   - **Alternative rejected**: Database-only approach would violate success criteria; class list queries may join students table making sub-second response difficult at scale
   - **Complexity cost**: Redis container dependency in Docker Compose, Redis config class, cache invalidation logic in service layer
   - **Benefit**: 4x performance improvement (2s → 500ms), reduces database load

**Decision**: Both complexity additions are justified by documented requirements and long-term maintainability. Proceed with implementation.

---

## Phase 0: Research & Technical Decisions

**Status**: ✅ COMPLETE

The research phase resolved:
1. ✅ Cache key naming conventions and TTL strategies
2. ✅ Academic year validation patterns
3. ✅ Enrollment history query optimization (read-only joins with student data)
4. ✅ Redis configuration for student-service
5. ✅ Flyway migration best practices for existing service extension
6. ✅ Grade level enumeration (GRADE_1 to GRADE_12)
7. ✅ Schedule data exclusion (deferred to future service)
8. ✅ Base cache framework design for sms-common

See [research.md](./research.md) for detailed findings.

---

## Phase 1: Design Artifacts

**Status**: ✅ COMPLETE

Phase 1 deliverables:
- ✅ **data-model.md**: Entity design for ClassEntity, EnrollmentHistory (read-only), relationships with existing Student entity, indexes
  - ClassEntity with 12 fields + optimistic locking
  - EnrollmentHistory (read-only) with 8 fields
  - GradeLevel enum (GRADE_1 to GRADE_12)
  - EnrollmentEventType enum (4 values)
  - 2 Flyway migrations (V5__, V6__)
  - 6 database indexes for performance
- ✅ **contracts/openapi.yml**: Complete OpenAPI 3.0 specification for all 7 REST endpoints
  - 7 REST endpoints (3 GET read-only, 4 CRUD)
  - All request/response DTOs defined
  - Error codes and responses documented
  - Examples for all operations
  - Schedule data excluded (per clarification #3)
- ✅ **quickstart.md**: Developer setup guide for extending student-service with class management
  - Setup instructions for local and Docker environments
  - API testing examples with curl
  - Database and Redis management commands
  - Troubleshooting common issues
  - Development workflow guidance

See respective files in this directory.

### Post-Design Constitution Check

All 6 constitution principles remain compliant after design completion:
- ✅ Microservices-First: Single bounded context (Student Records Management)
- ✅ Security-First: JWT authentication, RBAC, audit logging
- ✅ Simplicity (YAGNI): No premature optimization, justified Redis caching
- ✅ Observability: Health checks, structured logging, metrics
- ✅ Test Discipline: Unit, integration, contract tests planned
- ✅ Backend API Conventions: ApiResponse wrapper, error codes, i18n separation

**Design artifacts validated and approved for implementation.**

---

## Phase 2: Task Breakdown

**STATUS**: NOT STARTED (requires /speckit.tasks command)

Task generation is handled by the `/speckit.tasks` command and will be output to `tasks.md`.

---

## Planning Summary

**Planning Status**: ✅ COMPLETE (Phases 0-1)

### Artifacts Generated

| Artifact | Path | Status | Lines | Purpose |
|----------|------|--------|-------|---------|
| Specification | [spec.md](./spec.md) | ✅ Complete | 230 | Feature requirements with 5 clarifications |
| Implementation Plan | [plan.md](./plan.md) | ✅ Complete | 365 | This document - technical architecture |
| Research | [research.md](./research.md) | ✅ Complete | 292 | 8 technical decisions documented |
| Data Model | [data-model.md](./data-model.md) | ✅ Complete | 310 | 2 entities, 2 enums, 2 migrations, 6 indexes |
| API Contracts | [contracts/openapi.yml](./contracts/openapi.yml) | ✅ Complete | 825 | OpenAPI 3.0 spec for 7 REST endpoints |
| Quick Start Guide | [quickstart.md](./quickstart.md) | ✅ Complete | 572 | Developer setup and testing guide |
| Agent Context | [CLAUDE.md](../../CLAUDE.md) | ✅ Updated | - | Added tech stack for 005-class-management |

**Total Documentation**: 2,594 lines across 7 files

### Key Architectural Decisions

1. **Service Integration**: Extends existing student-service (NOT new microservice)
   - Rationale: Tightly coupled domain (classes ↔ students), atomic transactions, reduced complexity
   - Package: `com.sms.student.classmanagement.*`
   - Database: Shared `student_db` PostgreSQL database

2. **Caching Strategy**: Redis with cache-aside pattern and graceful degradation
   - Class list: 30min TTL
   - Class details: 15min TTL
   - Enrollment history: 60min TTL
   - New dependency: Spring Data Redis for student-service

3. **Data Model**: 2 entities, 2 enums, 2 Flyway migrations
   - ClassEntity: 12 fields + optimistic locking (version)
   - EnrollmentHistory: READ-ONLY for this feature (writes handled by enrollment feature)
   - GradeLevel: GRADE_1 to GRADE_12 (Cambodia MoEYS 6-3-3 structure)
   - Academic year format: "YYYY-YYYY" with validation

4. **API Design**: 7 RESTful endpoints via API Gateway
   - 3 GET (read-only): class list, class details, enrollment history
   - 4 CRUD: create, update (PUT), archive (DELETE soft-delete)
   - Response format: ApiResponse<T> wrapper (sms-common standard)
   - Error codes: Machine-readable for frontend i18n

5. **Excluded from Scope**: Schedule data
   - Clarification #3: "should have its own service to handle schedule (will implement in the future)"
   - No schedule_json column, no ScheduleSession DTO

### Constitution Compliance

| Principle | Status | Key Compliance Points |
|-----------|--------|----------------------|
| I. Microservices-First | ✅ PASS | Single bounded context (Student Records Management) |
| II. Security-First | ✅ PASS | JWT auth, RBAC, audit logging, encrypted transit |
| III. Simplicity (YAGNI) | ✅ PASS | Minimal dependencies (+1 Redis), no speculative features |
| IV. Observability | ✅ PASS | Health checks, structured logging, cache metrics |
| V. Test Discipline | ✅ PASS | Unit (90%), integration (80%), contract (100%) coverage |
| VI. Backend API Conventions | ✅ PASS | ApiResponse wrapper, error codes, i18n separation |

**All constitution gates PASSED.**

### Performance Targets

| Metric | Target | Strategy |
|--------|--------|----------|
| Class list (first load) | <2s | Database query optimization, composite indexes |
| Class list (cached) | <500ms | Redis cache with 30min TTL |
| Class details | <2s | Joined query optimization, 15min cache |
| Cache hit rate | >70% | Proper TTL tuning, cache warming |
| Concurrent teachers | 100+ | Stateless design, connection pooling |

### Next Steps

**Phase 2: Task Breakdown** (requires `/speckit.tasks` command)
- Generate implementation tasks in dependency order
- Estimate effort and complexity
- Create tasks.md with actionable steps

**Phase 3: Implementation** (requires `/speckit.implement` command)
- Execute tasks from tasks.md
- Write code following microservice standards
- Run tests and verify success criteria

---

**Implementation Plan Status**: ✅ COMPLETE - Ready for task generation (`/speckit.tasks`)
