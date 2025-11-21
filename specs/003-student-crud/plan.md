# Implementation Plan: Student CRUD Operations

**Branch**: `003-student-crud` | **Date**: 2025-11-22 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-student-crud/spec.md`

## Summary

Implement comprehensive student information management system enabling school administrators and teachers to create, read, update, and soft-delete student records. The feature includes bilingual (Khmer/English) data entry, photo uploads (max 5MB), parent/guardian contact management, class-based organization, and complete audit trail. Built as a new `student-service` microservice with PostgreSQL database, integrating with existing `auth-service` for authentication and supporting up to 10,000 students with sub-3-second performance targets.

## Technical Context

**Language/Version**: Java 21+ (Spring Boot 3.5.7)
**Primary Dependencies**:
- Spring Boot 3.5.7 (Web, Data JPA, Security, Validation)
- PostgreSQL 15+ JDBC Driver
- Lombok for boilerplate reduction
- springdoc-openapi for API documentation
- Flyway for database migrations
- Netflix Eureka Client for service discovery

**Storage**:
- PostgreSQL 15+ (`student_db` database)
- File storage for student photos (local `uploads/` directory, cloud-ready)
- Redis 7+ (optional, for caching class rosters)

**Testing**:
- JUnit 5 for unit tests
- Spring Boot Test for integration tests
- Testcontainers for database integration tests
- MockMVC for controller tests

**Target Platform**: Docker containers on Linux (Ubuntu/Alpine)
**Project Type**: Microservice (student-service)
**Performance Goals**:
- Create student profile: < 3 seconds
- Update operations visible to all users: < 5 seconds
- Search/list results: < 3 seconds for up to 50 students
- Photo upload processing: < 15 seconds for 5MB files

**Constraints**:
- Photo storage: ~5GB for 10,000 students (500KB avg)
- Database: Support 10,000 students + 20,000 parent contacts
- API rate limiting: 100 requests/minute per user (inherited from gateway)
- Concurrent users: 20+ simultaneous editors without contention

**Scale/Scope**:
- Initial deployment: Single school, 1,000-5,000 students
- MVP scope: 7 REST endpoints, 4 JPA entities, ~15 source files

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### I. Microservices-First ✅ PASS

**Compliance**:
- ✅ New `student-service` with independent deployment
- ✅ Separate `student_db` PostgreSQL database
- ✅ Service discovery via Eureka registration
- ✅ REST API boundaries for inter-service communication
- ✅ No shared database access with other services
- ✅ Failure isolation: Student service failures don't affect auth/grade services

**Service Boundaries**:
- **Student Service owns**: Student records, parent contacts, class enrollments, photo storage
- **Auth Service owns**: User authentication, JWT validation (dependency)
- **Future integrations**: Grade service (read-only student lookup), Attendance service (read-only roster)

### II. Security-First ✅ PASS

**Compliance**:
- ✅ All endpoints require JWT authentication (except health checks)
- ✅ Role-based access: Administrators (full CRUD), Teachers (read + update assigned classes only)
- ✅ Student PII encrypted in transit (HTTPS via nginx)
- ✅ Audit logging: All mutations tracked with `created_by`, `updated_by`, timestamps
- ✅ No credentials in code: Database config via environment variables
- ✅ Photo upload validation: File type, size, content inspection

**Security Implementation**:
- JWT validation filter using `auth-service` public key
- `@PreAuthorize` annotations on controller methods
- Hibernate audit fields: `@CreationTimestamp`, `@UpdateTimestamp`
- Photo sanitization before storage (strip EXIF metadata)

### III. Simplicity (YAGNI) ✅ PASS

**Compliance**:
- ✅ No premature optimization: Standard JPA queries without caching layer initially
- ✅ No speculative features: Only 7 functional requirements from spec implemented
- ✅ Minimal dependencies: Reusing existing Spring Boot ecosystem
- ✅ Clear code: Standard REST controller → service → repository pattern

**Justified Complexity**:
- **Soft delete pattern**: Required by spec for 7-year data retention compliance
- **Photo storage abstraction**: Enables future migration to cloud storage (S3/Azure Blob)
- **Enrollment history tracking**: `student_class_enrollments` table tracks complete class transfer history

**Rejected Complexity**:
- ❌ No GraphQL: REST sufficient for CRUD operations
- ❌ No event sourcing: Audit fields sufficient for current compliance needs
- ❌ No CQRS: Read/write patterns not complex enough to justify separation

### IV. Observability ✅ PASS

**Compliance**:
- ✅ Health endpoint: `/actuator/health` for Kubernetes probes
- ✅ Structured logging: SLF4J with JSON formatter, correlation IDs via Spring Cloud Sleuth
- ✅ Metrics: Micrometer integration exposing JVM, HTTP, database connection pool metrics
- ✅ Alerting thresholds:
  - Error rate > 5% triggers alert
  - p95 latency > 5 seconds triggers alert
  - Database connection pool exhaustion triggers alert

**Observability Implementation**:
- Spring Boot Actuator endpoints: health, metrics, info
- Request/response logging at INFO level (excluding sensitive fields)
- Exception logging with stack traces at ERROR level

### V. Test Discipline ✅ PASS

**Compliance**:
- ✅ Unit tests: Service layer business logic (student creation validation, soft delete, photo processing)
- ✅ Integration tests: Repository layer with Testcontainers PostgreSQL
- ✅ Contract tests: Controller endpoints with MockMVC validating request/response schemas
- ✅ Test independence: All tests use isolated database instances (Testcontainers)

**Test Coverage Targets**:
- Service layer: 80%+ line coverage
- Controller layer: 100% endpoint coverage
- Repository layer: Key queries tested (complex joins, soft delete filtering)

**Exceptions**:
- DTOs: Generated Lombok code not tested (getters/setters/builders)
- Configuration classes: Simple bean definitions not tested

### VI. Backend API Conventions ✅ PASS

**Compliance**:
- ✅ Response wrapper: All endpoints return `ApiResponse<T>` with `errorCode` and `data`
- ✅ Error codes: Machine-readable enums (e.g., `INVALID_PHONE_FORMAT`, `STUDENT_NOT_FOUND`)
- ✅ HTTP status codes: 200 (success), 400 (validation), 401 (auth), 404 (not found), 500 (internal)
- ✅ No human-readable messages: Frontend handles Khmer/English translation
- ✅ Global exception handler: `@RestControllerAdvice` class maps exceptions to error codes

**Error Code Strategy**:
```java
public enum ErrorCode {
    SUCCESS,

    // Student-specific errors
    STUDENT_NOT_FOUND,
    INVALID_STUDENT_DATA,
    DUPLICATE_STUDENT_CODE,
    STUDENT_ALREADY_ENROLLED,

    // Photo errors
    PHOTO_SIZE_EXCEEDED,
    INVALID_PHOTO_FORMAT,
    PHOTO_UPLOAD_FAILED,

    // Parent contact errors
    INVALID_PHONE_FORMAT,
    PARENT_CONTACT_REQUIRED,

    // Class errors
    CLASS_NOT_FOUND,
    CLASS_CAPACITY_EXCEEDED,

    // Generic
    VALIDATION_ERROR,
    UNAUTHORIZED,
    INTERNAL_ERROR
}
```

### Constitution Summary

**Overall Status**: ✅ ALL GATES PASSED

**Rationale**: Student CRUD feature aligns perfectly with established microservices architecture, security standards, and API conventions. No complexity violations require justification. Ready to proceed to Phase 0 research.

## Project Structure

### Documentation (this feature)

```text
specs/003-student-crud/
├── spec.md              # Feature specification (complete)
├── plan.md              # This file (/speckit.plan output)
├── checklists/
│   └── requirements.md  # Quality checklist (validated)
├── research.md          # Phase 0: Technology decisions
├── data-model.md        # Phase 1: Entity design
├── quickstart.md        # Phase 1: Developer setup
├── contracts/           # Phase 1: API contracts
│   └── student-api.yaml # OpenAPI specification
└── tasks.md             # Phase 2: Implementation tasks (/speckit.tasks)
```

### Source Code (student-service)

```text
student-service/
├── src/
│   ├── main/
│   │   ├── java/com/sms/student/
│   │   │   ├── StudentServiceApplication.java  # Spring Boot main class
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java         # JWT validation, RBAC
│   │   │   │   ├── OpenAPIConfig.java          # Swagger/OpenAPI setup
│   │   │   │   └── StorageConfig.java          # File storage configuration
│   │   │   ├── controller/
│   │   │   │   └── StudentController.java      # REST endpoints (7 total)
│   │   │   ├── dto/
│   │   │   │   ├── ApiResponse.java            # Standard response wrapper
│   │   │   │   ├── ErrorCode.java              # Error code enum
│   │   │   │   ├── StudentRequest.java         # Create/update request
│   │   │   │   ├── StudentResponse.java        # Student detail response
│   │   │   │   ├── StudentListResponse.java    # Paginated list response
│   │   │   │   ├── ParentContactRequest.java   # Parent contact DTO
│   │   │   │   └── PhotoUploadResponse.java    # Photo upload result
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java # @RestControllerAdvice
│   │   │   │   ├── StudentNotFoundException.java
│   │   │   │   ├── PhotoUploadException.java
│   │   │   │   └── ClassCapacityException.java
│   │   │   ├── model/
│   │   │   │   ├── Student.java                # JPA entity
│   │   │   │   ├── ParentContact.java          # JPA entity
│   │   │   │   ├── StudentClassEnrollment.java # JPA entity (history)
│   │   │   │   └── Class.java                  # JPA entity (read-only reference)
│   │   │   ├── repository/
│   │   │   │   ├── StudentRepository.java      # Spring Data JPA
│   │   │   │   ├── ParentContactRepository.java
│   │   │   │   ├── EnrollmentRepository.java
│   │   │   │   └── ClassRepository.java
│   │   │   ├── service/
│   │   │   │   ├── StudentService.java         # Business logic
│   │   │   │   ├── PhotoStorageService.java    # File upload handling
│   │   │   │   └── EnrollmentService.java      # Class assignment logic
│   │   │   └── validation/
│   │   │       ├── StudentCodeValidator.java   # Custom @StudentCode
│   │   │       └── CambodiaPhoneValidator.java # Phone format validation
│   │   └── resources/
│   │       ├── application.yml                 # Service configuration
│   │       └── db/migration/
│   │           ├── V1__create_students_table.sql
│   │           ├── V2__create_parent_contacts_table.sql
│   │           ├── V3__create_enrollments_table.sql
│   │           └── V4__create_indexes.sql
│   └── test/
│       ├── java/com/sms/student/
│       │   ├── controller/
│       │   │   └── StudentControllerTest.java  # MockMVC tests
│       │   ├── service/
│       │   │   ├── StudentServiceTest.java     # Unit tests
│       │   │   └── PhotoStorageServiceTest.java
│       │   └── repository/
│       │       └── StudentRepositoryTest.java  # Testcontainers
│       └── resources/
│           └── application-test.yml            # Test configuration
├── uploads/                                     # Local photo storage (gitignored)
├── target/                                      # Maven build output (gitignored)
├── pom.xml                                      # Maven dependencies
├── Dockerfile                                   # Container image
└── README.md                                    # Service documentation
```

**Structure Decision**:
Microservice architecture with standard Spring Boot layered design (controller → service → repository). The `student-service` follows established SMS patterns from `auth-service` and `grade-service` for consistency. Photo storage uses local filesystem initially with abstraction layer enabling future cloud migration (AWS S3, Azure Blob Storage) without code changes.

**Database Schema**: Implemented via Flyway migrations in `db/migration/` following the schema provided in user input (schools, classes, students, student_class_enrollments tables with indexes and utility functions).

## Complexity Tracking

> **No violations to document - all Constitution gates passed.**

---

## Phase 0: Outline & Research

### Research Tasks

The following technical decisions require investigation before implementation:

1. **Photo Storage Strategy**
   - **Question**: Local filesystem vs. cloud storage (S3/Azure Blob) vs. database BLOBs?
   - **Context**: Spec requires 5MB max photos for up to 10,000 students (~5GB total)
   - **Research needed**: Performance implications, backup strategies, cost analysis

2. **Soft Delete Implementation Pattern**
   - **Question**: Status flag vs. deleted_at timestamp vs. separate archive table?
   - **Context**: 7-year retention requirement for compliance, need to filter active vs. inactive efficiently
   - **Research needed**: JPA best practices, query performance with large datasets

3. **Photo Processing Pipeline**
   - **Question**: Resize/optimize photos on upload vs. on-demand vs. background job?
   - **Context**: Standard dimensions (400x400px), thumbnail generation for lists
   - **Research needed**: Java image processing libraries (ImageMagick, Thumbnailator, Java AWT)

4. **Class Assignment Validation**
   - **Question**: Real-time capacity checking vs. eventual consistency?
   - **Context**: Prevent class overcrowding, handle concurrent enrollments
   - **Research needed**: Transaction isolation levels, optimistic vs. pessimistic locking

5. **Bilingual Data Indexing**
   - **Question**: Separate columns (first_name, first_name_km) vs. JSONB vs. full-text search?
   - **Context**: Khmer + English names, search across both languages
   - **Research needed**: PostgreSQL text search capabilities, index types for Unicode

### Research Output

See [research.md](./research.md) for consolidated findings on:
- Photo storage decision (local with S3 migration path)
- Soft delete pattern (status enum + deleted_at)
- Photo processing approach (synchronous resize with Thumbnailator)
- Class validation strategy (optimistic locking + version column)
- Bilingual indexing (separate columns + composite GIN indexes)

---

## Phase 1: Design & Contracts

### Entities & Relationships

See [data-model.md](./data-model.md) for complete entity definitions including:

**Core Entities**:
1. **Student**: Primary entity with demographics, photo URL, audit fields
2. **ParentContact**: One-to-many with Student, stores guardian information
3. **StudentClassEnrollment**: Many-to-many junction tracking complete enrollment history
4. **Class**: Read-only reference entity (owned by separate class-service)

**Entity Relationships**:
- Student 1:N ParentContact (cascade delete)
- Student N:M Class (via StudentClassEnrollment, restrict delete)
- StudentClassEnrollment tracks enrollment_date, end_date, reason

**State Transitions**:
- Student status: ACTIVE → INACTIVE (soft delete) → ACTIVE (restoration)
- Enrollment: NEW → TRANSFER/PROMOTION/DEMOTION → ENDED

### API Contracts

See [contracts/student-api.yaml](./contracts/student-api.yaml) for OpenAPI 3.0 specification including:

**Endpoints** (from user input):
```
GET    /api/students                  - List students (query params: classId, page, size, status)
GET    /api/students/{id}             - Get student details
POST   /api/students                  - Create student
PUT    /api/students/{id}             - Update student
DELETE /api/students/{id}             - Soft delete student
GET    /api/students/class/{classId}  - Get students by class (convenience endpoint)
POST   /api/students/{id}/photo       - Upload student photo
```

**Request/Response Schemas**:
- `StudentRequest`: Create/update payload with validation annotations
- `StudentResponse`: Complete student details with parent contacts
- `StudentListResponse`: Paginated list with metadata (totalElements, totalPages)
- `PhotoUploadResponse`: URL and metadata after successful upload
- `ApiResponse<T>`: Standard wrapper with errorCode and data fields

### Developer Quickstart

See [quickstart.md](./quickstart.md) for step-by-step setup including:
1. Prerequisites (Java 21, Docker, PostgreSQL)
2. Database setup (student_db creation, Flyway migrations)
3. Local development (Maven commands, application.yml configuration)
4. Testing (running unit/integration tests, Testcontainers setup)
5. API documentation access (Swagger UI at http://localhost:8082/swagger-ui.html)

---

## Phase 1: Agent Context Update

After completing research and design artifacts, run:

```bash
.specify/scripts/bash/update-agent-context.sh claude
```

This updates `CLAUDE.md` with:
- Spring Boot 3.5.7 + Java 21 stack for student-service
- PostgreSQL 15+ database schema
- Photo storage patterns
- Soft delete implementation approach
- API contract conventions

---

## Phase 2: Task Breakdown

**Note**: Task generation is handled by the `/speckit.tasks` command (NOT part of `/speckit.plan`).

After completing Phase 0 and Phase 1, run `/speckit.tasks` to generate `tasks.md` with:
- Dependency-ordered implementation tasks
- Estimated effort per task
- Testing requirements per component
- Deployment checklist

---

## Implementation Notes

### Critical Path Dependencies

1. **Database Schema**: Must complete Flyway migrations before repository layer
2. **Entity Models**: Required before service layer implementation
3. **Security Config**: JWT validation must work before controller authorization
4. **Photo Storage**: Abstract interface first, local implementation initially
5. **Integration with auth-service**: Requires auth-service to expose public key endpoint for JWT validation

### Known Risks

1. **Photo Storage Migration**: Local filesystem locks feature to single-instance deployment initially
   - **Mitigation**: Implement `PhotoStorageService` interface from day one, enabling S3 adapter swap later

2. **Concurrent Class Enrollment**: Race condition when multiple students enroll simultaneously
   - **Mitigation**: Optimistic locking on Class entity with retry logic

3. **Large Class Rosters**: Listing 50+ students without pagination may timeout
   - **Mitigation**: Enforce pagination (default 20/page, max 50/page)

4. **Khmer Unicode Rendering**: Font support required in frontend for proper display
   - **Mitigation**: Document font requirements in frontend integration guide

### Performance Optimization Opportunities (Post-MVP)

- **Redis Caching**: Cache frequently accessed class rosters (invalidate on enrollment changes)
- **Photo CDN**: Serve photos via CloudFront/Cloudflare for faster global access
- **Database Indexing**: Add composite indexes on (class_id, status) for filtered queries
- **Batch Operations**: Support bulk student import via CSV upload endpoint

---

## Success Criteria (from spec.md)

Implementation will be considered complete when:

✅ **Quantitative Metrics**:
1. Administrator can create student profile in < 3 minutes
2. Updates visible to all users within 5 seconds
3. Search results returned in < 10 seconds
4. Photo uploads complete in < 15 seconds (5MB files)
5. Class rosters (50 students) load in < 3 seconds
6. 100% of soft-deleted records recoverable
7. System supports 20+ concurrent users

✅ **Qualitative Measures**:
1. 90% of administrators complete first student creation without help
2. 95% of profiles have complete data (photo + contacts)
3. Teachers report improved efficiency vs. previous systems
4. Duplicate records reduced by 80% via validation
5. 85% task completion rate on first attempt

---

## Deliverables Checklist

### Phase 0 Outputs
- [ ] research.md (technology decisions documented)

### Phase 1 Outputs
- [ ] data-model.md (entity definitions complete)
- [ ] contracts/student-api.yaml (OpenAPI spec)
- [ ] quickstart.md (developer setup guide)
- [ ] Agent context updated (CLAUDE.md synced)

### Phase 2 Outputs (via /speckit.tasks)
- [ ] tasks.md (implementation task breakdown)

### Implementation (via /speckit.implement)
- [ ] Database migrations (V1-V4 Flyway scripts)
- [ ] JPA entities (Student, ParentContact, Enrollment, Class)
- [ ] Repository interfaces (Spring Data JPA)
- [ ] Service layer (StudentService, PhotoStorageService, EnrollmentService)
- [ ] Controller (StudentController with 7 endpoints)
- [ ] Exception handling (GlobalExceptionHandler)
- [ ] Validation (custom validators)
- [ ] Unit tests (service layer, 80%+ coverage)
- [ ] Integration tests (repository + controller)
- [ ] Dockerfile + docker-compose.yml entry
- [ ] README.md (service documentation)

---

**Status**: Phase 0 ready to begin
**Next Command**: Begin Phase 0 research tasks
**Blockers**: None
