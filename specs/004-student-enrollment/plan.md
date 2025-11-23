# Implementation Plan: Student Class Enrollment Management

**Branch**: `004-student-enrollment` | **Date**: 2025-11-23 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/004-student-enrollment/spec.md`

## Summary

This feature implements three REST API endpoints for managing student class enrollments in the student-service:
1. **GET /api/students/{id}/enrollment-history** - Retrieve complete enrollment history
2. **POST /api/students/{id}/enroll** - Enroll student in a class
3. **POST /api/students/{id}/transfer** - Transfer student to a new class

**Technical Approach**: Extend existing `student_class_enrollments` table schema with status tracking, implement service layer with atomic transaction support for transfers, and provide RESTful endpoints following the project's API response conventions.

## Technical Context

**Language/Version**: Java 21+
**Framework**: Spring Boot 3.5.7
**Primary Dependencies**: Spring Data JPA, Spring Web, Lombok, Hibernate Validator, Flyway
**Storage**: PostgreSQL 15+ (student_db database)
**Testing**: JUnit 5, Spring Boot Test, Testcontainers
**Target Platform**: Docker container (Linux server)
**Project Type**: Microservice (student-service)
**Performance Goals**: <2 seconds for enrollment history retrieval, <1 second for enrollment/transfer operations
**Constraints**: Atomic transactions for transfer operations, class capacity enforcement, duplicate enrollment prevention
**Scale/Scope**: Support 10,000+ students, 1,000+ concurrent enrollments per academic year

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### I. Microservices-First ✅

- **Independently deployable**: Feature contained within `student-service` only
- **Loosely coupled**: No direct dependencies on other services; auth handled via JWT validation
- **Single responsibility**: Enrollment management is within student domain boundary
- **Failure-isolated**: All operations local to student-service database

**Status**: PASS - Feature respects service boundaries

### II. Security-First ✅

- **Authentication**: All endpoints require valid JWT tokens (enforced by existing SecurityConfig)
- **Authorization**: RBAC enforced at service layer (admin-only operations)
- **Data protection**: Student enrollment data protected via existing security filters
- **Audit logging**: Enrollment operations will log user identity (created_by, updated_by fields exist)
- **Secrets management**: No new secrets required

**Status**: PASS - Leverages existing security infrastructure

### III. Simplicity (YAGNI) ✅

- **No premature optimization**: Using standard JPA queries, will optimize only if performance metrics show need
- **No speculative features**: Building exactly what spec requires (3 endpoints, no extras)
- **Minimal dependencies**: No new libraries needed (using existing Spring stack)
- **Clear code over clever code**: Standard service layer pattern with clear method names

**Status**: PASS - Minimal complexity, extends existing patterns

### IV. Observability ✅

- **Health endpoints**: Using existing `/actuator/health` from student-service
- **Structured logging**: Will use existing SLF4J logging with correlation IDs
- **Metrics**: Operation latency will be measurable via Spring Boot Actuator
- **Alerting thresholds**: Success criteria define acceptable limits (<1s, <2s)

**Status**: PASS - Leverages existing observability stack

### V. Test Discipline ✅

- **Unit tests**: Business logic in service layer will have unit tests
- **Integration tests**: Enrollment operations and capacity validation will have integration tests
- **Contract tests**: API endpoints will have contract tests validating schemas
- **Test independence**: Tests will use in-memory database or test containers

**Status**: PASS - Full test coverage planned

### VI. Backend API Conventions ✅

- **Response wrapper**: All endpoints return `ApiResponse<T>` with `errorCode` and `data`
- **Error codes**: Using `StudentErrorCode` enum (UPPER_SNAKE_CASE)
- **Status codes**: Proper HTTP status mapping (200, 400, 404, 409, 500)
- **Internationalization separation**: Backend returns only error codes
- **Global exception handling**: Using existing `GlobalExceptionHandler`

**Status**: PASS - Follows established API conventions

**Overall Constitution Status**: ✅ **ALL CHECKS PASSED** - No violations, no complexity justification needed

## Project Structure

### Documentation (this feature)

```text
specs/004-student-enrollment/
├── plan.md              # This file
├── research.md          # Phase 0: Database schema analysis
├── data-model.md        # Phase 1: Entity and DTO design
├── quickstart.md        # Phase 1: Developer setup guide
├── contracts/           # Phase 1: API contracts
│   └── enrollment-api.md
└── tasks.md             # Phase 2: Implementation tasks (created by /speckit.tasks)
```

### Source Code (repository root)

```text
student-service/
├── src/main/java/com/sms/student/
│   ├── controller/
│   │   └── EnrollmentController.java      # NEW: Three enrollment endpoints
│   ├── dto/
│   │   ├── EnrollmentRequest.java         # NEW: Enroll student DTO
│   │   ├── TransferRequest.java           # NEW: Transfer student DTO
│   │   └── EnrollmentHistoryResponse.java # NEW: History response DTO
│   ├── exception/
│   │   ├── DuplicateEnrollmentException.java   # NEW
│   │   ├── ClassCapacityExceededException.java # EXISTING (reuse)
│   │   └── EnrollmentNotFoundException.java    # NEW
│   ├── model/
│   │   └── Enrollment.java                # UPDATE: Add status field
│   ├── repository/
│   │   └── EnrollmentRepository.java      # NEW: Spring Data JPA repository
│   ├── service/
│   │   ├── EnrollmentService.java         # NEW: Interface
│   │   └── EnrollmentServiceImpl.java     # NEW: Implementation
│   └── validation/
│       └── EnrollmentValidator.java       # NEW: Custom business rules
├── src/main/resources/db/migration/
│   └── V6__add_enrollment_status_field.sql # NEW: Schema migration
└── src/test/java/com/sms/student/
    ├── controller/
    │   └── EnrollmentControllerTest.java  # NEW: Integration tests
    ├── service/
    │   └── EnrollmentServiceTest.java     # NEW: Unit tests
    └── repository/
        └── EnrollmentRepositoryTest.java  # NEW: Repository tests
```

**Structure Decision**: Feature extends existing student-service microservice. All code follows established package structure (`controller/`, `dto/`, `service/`, `model/`, `repository/`). Database migration adds missing `status` field to existing `student_class_enrollments` table.

## Complexity Tracking

> **No violations - table not needed**

All constitution checks passed. No complexity justification required.

---

## Phase 0: Research & Schema Analysis

### Research Tasks

1. **Database Schema Review**
   - Analyze existing `student_class_enrollments` table (V3 migration)
   - Identify gap: Missing `status` field (spec requires: active, completed, transferred, withdrawn)
   - Current schema has `reason` enum but no explicit status field
   - Plan: Add status field via new migration V6

2. **JPA Entity Pattern Review**
   - Review existing Student, Class entities in codebase
   - Identify pattern: UUID primary keys, Lombok annotations, audit fields
   - Ensure Enrollment entity follows same pattern

3. **Transaction Management Research**
   - Transfer operation requires atomic updates (close old enrollment + create new)
   - Use `@Transactional` annotation on service method
   - Leverage Spring's declarative transaction management

4. **Capacity Validation Pattern**
   - Classes table has `max_capacity` and `student_count` fields
   - Identify pattern: Use optimistic locking (`version` field) to prevent race conditions
   - Decision: Use `@Lock(LockModeType.OPTIMISTIC)` on Class entity updates

### Research Findings

**Database Schema Decision**:
- **Existing**: `student_class_enrollments` table with `reason` field (NEW, TRANSFER, PROMOTION, etc.)
- **Gap**: No explicit `status` field for lifecycle management
- **Migration Plan**: Add `status` ENUM column with values (ACTIVE, COMPLETED, TRANSFERRED, WITHDRAWN)
- **Rationale**: Separation of concerns - `reason` explains WHY enrollment was created, `status` tracks current STATE

**API Response Pattern**:
- **Existing**: `ApiResponse<T>` wrapper, `StudentErrorCode` enum
- **Pattern**: All endpoints return `ApiResponse` with `errorCode` and `data`
- **Error Codes**: Add enrollment-specific codes to `StudentErrorCode` enum

**Transaction Atomicity**:
- **Pattern**: `@Transactional` on service methods with multiple database operations
- **Transfer Logic**: Single transaction wrapping:
  1. Mark old enrollment as TRANSFERRED
  2. Create new enrollment as ACTIVE
  3. Update class student counts
- **Rollback**: Any failure rolls back entire transaction

---

## Phase 1: Design & Contracts

### Data Model Design

See [data-model.md](./data-model.md) for complete entity specifications.

**Key Entities**:

1. **Enrollment** (JPA Entity - extends existing)
   - Maps to `student_class_enrollments` table
   - Add `status` field (EnrollmentStatus enum)
   - Relationships: `@ManyToOne` to Student and Class

2. **EnrollmentStatus** (Java Enum)
   - Values: ACTIVE, COMPLETED, TRANSFERRED, WITHDRAWN
   - Used for lifecycle tracking

3. **DTOs**:
   - `EnrollmentRequest` - {classId, notes}
   - `TransferRequest` - {targetClassId, reason}
   - `EnrollmentHistoryResponse` - {enrollments[...], totalCount}

### API Contracts

See [contracts/enrollment-api.md](./contracts/enrollment-api.md) for OpenAPI specifications.

**Endpoints**:

1. **GET /api/students/{id}/enrollment-history**
   - Response: `ApiResponse<EnrollmentHistoryResponse>`
   - Returns all enrollments ordered by date DESC

2. **POST /api/students/{id}/enroll**
   - Request: `EnrollmentRequest`
   - Response: `ApiResponse<EnrollmentResponse>`
   - Validates student existence, class capacity, prevents duplicates

3. **POST /api/students/{id}/transfer**
   - Request: `TransferRequest`
   - Response: `ApiResponse<EnrollmentResponse>`
   - Atomic operation: close old + create new enrollment

### Quickstart Guide

See [quickstart.md](./quickstart.md) for developer setup instructions.

**Prerequisites**: Java 21, Maven 3.9+, Docker
**Run locally**: `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`
**Run tests**: `./mvnw test`
**Database**: PostgreSQL via Docker Compose

---

## Implementation Notes

### Error Handling

Add to `StudentErrorCode` enum:
- `DUPLICATE_ENROLLMENT` - Student already enrolled in class
- `ENROLLMENT_NOT_FOUND` - No active enrollment found for transfer
- `CLASS_CAPACITY_EXCEEDED` - Class is full (existing error, reuse)
- `INVALID_ENROLLMENT_STATUS` - Enrollment status transition not allowed

### Validation Rules

1. **Enroll Student**:
   - Student must exist (FR-007)
   - Class must exist (FR-008)
   - Class must have available capacity (FR-009)
   - No active enrollment for same student+class (FR-006)

2. **Transfer Student**:
   - Student must have active enrollment in source class (FR-015)
   - Target class must have available capacity (FR-016)
   - Transfer reason must be provided (FR-017)
   - Both operations must succeed or fail together (FR-018)

### Security

- All endpoints secured via existing JWT filter
- User ID extracted from JWT for audit fields (created_by, updated_by)
- RBAC check: Only users with ADMIN role can enroll/transfer students

### Performance Optimization

- Index already exists: `idx_enrollment_current` for active enrollments
- Index already exists: `idx_enrollment_student` for history queries
- Query optimization: Use JOIN FETCH for class details in history endpoint
- Capacity check: Use `SELECT FOR UPDATE` to prevent race conditions

---

## Next Steps

1. ✅ Phase 0 completed - Research and schema analysis done
2. ➡️ Create `research.md` documenting findings
3. ➡️ Create `data-model.md` with entity specifications
4. ➡️ Create `contracts/enrollment-api.md` with OpenAPI specs
5. ➡️ Create `quickstart.md` with setup instructions
6. ➡️ Run `/speckit.tasks` to generate implementation tasks
