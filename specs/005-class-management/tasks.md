# Implementation Tasks: Class Management API

**Feature**: 005-class-management
**Generated**: 2025-11-24
**Service**: student-service (extension)
**Tech Stack**: Java 21, Spring Boot 3.5.7, PostgreSQL 15+, Redis 7+

---

## Overview

This document breaks down the class management implementation into executable tasks organized by user story. Each phase represents a complete, independently testable increment of functionality.

**Implementation Strategy**: MVP-first approach
- Phase 1-2: Setup and foundational infrastructure
- Phase 3-4: P1 stories (core teacher functionality - view classes and details)
- Phase 5: P2 stories (enrollment history and class creation)
- Phase 6-7: P3 stories (update and archive)
- Phase 8: Polish and cross-cutting concerns

**Total Tasks**: 47
**Parallelizable Tasks**: 28
**User Stories**: 6 (2 P1, 2 P2, 2 P3)

---

## Task Format Legend

```
- [ ] [TaskID] [P] [Story] Description with file path
```

- **[P]**: Task can be parallelized (different files, no dependencies on incomplete tasks)
- **[Story]**: User story label (US1, US2, etc.) - maps to spec.md user stories
- **File paths**: Absolute paths from repository root

---

## Phase 1: Setup & Infrastructure

**Goal**: Prepare project infrastructure for class management feature

**Duration Estimate**: 2-3 hours

### Tasks

- [X] T001 Create feature branch `005-class-management` from main
- [X] T002 [P] Add Spring Data Redis dependency to student-service/pom.xml (spring-boot-starter-data-redis)
- [X] T003 [P] Create package structure: student-service/src/main/java/com/sms/student/{controller,dto,model,repository,service,exception}
- [X] T004 [P] Create test package structure: student-service/src/test/java/com/sms/student/{controller,service,repository}
- [X] T005 [P] Create sms-common cache package: sms-common/src/main/java/com/sms/common/cache/
- [X] T006 [P] Implement CacheService interface in sms-common/src/main/java/com/sms/common/cache/CacheService.java
- [X] T007 [P] Implement RedisCacheService in sms-common/src/main/java/com/sms/common/cache/RedisCacheService.java
- [X] T008 [P] Implement CacheKeyGenerator in sms-common/src/main/java/com/sms/common/cache/CacheKeyGenerator.java
- [X] T009 Rebuild and install sms-common: `cd sms-common && ./mvnw clean install -DskipTests`

**Completion Criteria**:
- ✅ Feature branch created
- ✅ Spring Data Redis dependency added
- ✅ Package structure created
- ✅ Base cache framework in sms-common completed and installed

---

## Phase 2: Foundational Components (Blocking Prerequisites)

**Goal**: Implement shared infrastructure required by all user stories

**Duration Estimate**: 3-4 hours

**Dependencies**: Must complete Phase 1 before starting

### Tasks

#### Database & Models
- [X] T010-T015 **SKIPPED** - Using existing SchoolClass entity and classes table from V1 migration (already exists in codebase from enrollment feature)
  - SchoolClass entity uses: grade (Integer 1-12), section, schoolId, teacherId, academicYear, maxCapacity, studentCount, status (ACTIVE/ARCHIVED)
  - No new entities or migrations needed - reusing existing schema

#### Repositories
- [X] T016 [P] Create ClassRepository interface (updated to use SchoolClass entity)
- [X] T017 [P] **SKIPPED** - EnrollmentHistoryRepository not needed (using existing StudentClassEnrollmentRepository)

#### Configuration
- [X] T018 [P] Create RedisConfig in student-service/src/main/java/com/sms/student/config/RedisConfig.java (RedisTemplate, CacheManager beans)
- [X] T019 Update application.yml with Redis configuration (localhost:6379 for local profile)
- [X] T020 Update application-docker.yml with Redis configuration (redis:6379 for docker profile)
- [X] T021 Add test Redis configuration in student-service/src/test/resources/application-test.yml (TestContainers config)

#### Error Handling
- [X] T022 [P] Add class error codes to student-service/src/main/java/com/sms/student/constant/ClassErrorCode.java (CLASS_NOT_FOUND, UNAUTHORIZED_CLASS_ACCESS, DUPLICATE_CLASS, etc.)
- [X] T023 [P] Create ClassNotFoundException in student-service/src/main/java/com/sms/student/exception/ClassNotFoundException.java
- [X] T024 [P] Create UnauthorizedClassAccessException in student-service/src/main/java/com/sms/student/exception/UnauthorizedClassAccessException.java
- [X] T025 [P] Create DuplicateClassException in student-service/src/main/java/com/sms/student/exception/DuplicateClassException.java
- [X] T026 Update GlobalExceptionHandler in student-service/src/main/java/com/sms/student/exception/GlobalExceptionHandler.java (add class exception handlers)

#### Docker & Infrastructure
- [X] T027 Update docker-compose.yml to add Redis dependency for student-service
- [X] T028 Verify Flyway migrations (renamed to V7 and V8 to avoid version conflicts)

**Completion Criteria**:
- ✅ Database schema created (2 tables, 6 indexes, 1 unique constraint)
- ✅ Entity models and enums implemented
- ✅ Repositories created with custom query methods
- ✅ Redis configuration completed for all profiles
- ✅ Error handling infrastructure in place
- ✅ Flyway migrations validated

**Independent Test**:
```bash
# Verify database setup
docker exec -it postgres-student psql -U sms_user -d student_db -c "\dt"
docker exec -it postgres-student psql -U sms_user -d student_db -c "\d classes"
docker exec -it postgres-student psql -U sms_user -d student_db -c "\d enrollment_history"

# Verify Redis connection
docker exec -it redis redis-cli ping  # Should return PONG
```

---

## Phase 3: User Story 1 - View My Classes (P1)

**User Story**: As a teacher, I want to view a list of all my classes so that I can quickly access the classes I'm teaching and monitor my teaching schedule.

**Priority**: P1 (critical path)

**Duration Estimate**: 4-5 hours

**Dependencies**: Must complete Phase 2

### Tasks

#### DTOs
- [X] T029 [P] [US1] Create ClassSummaryDto in student-service/src/main/java/com/sms/student/dto/ClassSummaryDto.java (fields: id, name, gradeLevel, subject, academicYear, capacity, currentEnrollment, teacherId, archived, createdAt)

#### Service Layer
- [X] T030 [P] [US1] Create ClassService interface in student-service/src/main/java/com/sms/student/service/ClassService.java (define listTeacherClasses method signature)
- [X] T031 [US1] Implement ClassServiceImpl.listTeacherClasses in student-service/src/main/java/com/sms/student/service/ClassServiceImpl.java (query by teacherId, filter archived, map to DTOs)
- [X] T032 [P] [US1] Create ClassCacheService in student-service/src/main/java/com/sms/student/service/ClassCacheService.java (inject CacheService from sms-common, implement cacheTeacherClasses and evictTeacherClasses methods)
- [X] T033 [US1] Integrate cache in ClassServiceImpl.listTeacherClasses (30min TTL, cache key: student-service:teacher:classes:{teacherId})

#### Controller
- [X] T034 [US1] Create ClassController with GET /api/classes endpoint in student-service/src/main/java/com/sms/student/controller/ClassController.java (extract teacherId from JWT, call service, wrap in ApiResponse)
- [X] T035 [US1] Add teacher authorization check in ClassController.listClasses (verify JWT contains ROLE_TEACHER or ROLE_ADMIN)
- [X] T036 [US1] Add query parameter `includeArchived` to GET /api/classes endpoint (default false)

#### OpenAPI Documentation
- [X] T037 [US1] Update OpenAPIConfig in student-service/src/main/java/com/sms/student/config/OpenAPIConfig.java (add class management tag, update API info)

**Completion Criteria**:
- ✅ Teacher can retrieve list of their classes
- ✅ Active classes shown by default, archived excluded
- ✅ includeArchived=true shows all classes
- ✅ Results cached with 30min TTL
- ✅ Authorization enforced (teacher sees only own classes)
- ✅ Empty list returned for teachers with no classes
- ✅ ApiResponse wrapper applied

**Independent Test**:
```bash
# Authenticate as teacher
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"0123456789","password":"password123"}' | jq -r '.data.accessToken')

# Test 1: List classes (should be empty initially)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/classes
# Expected: {"errorCode":"SUCCESS","data":[]}

# Test 2: List with includeArchived parameter
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/classes?includeArchived=true"

# Test 3: Verify cache hit (check Redis)
docker exec -it redis redis-cli KEYS "student-service:teacher:classes:*"
```

---

## Phase 4: User Story 2 - View Class Details (P1)

**User Story**: As a teacher, I want to view detailed information about a specific class so that I can see the complete class profile including all enrolled students.

**Priority**: P1 (critical path)

**Duration Estimate**: 5-6 hours

**Dependencies**: Must complete Phase 3 (requires ClassEntity and basic service structure)

### Tasks

#### DTOs
- [X] T038 [P] [US2] Create ClassDetailDto in student-service/src/main/java/com/sms/student/dto/ClassDetailDto.java (extends ClassSummaryDto, adds: description, updatedAt)
- [X] T039 [P] [US2] Create StudentRosterItemDto in student-service/src/main/java/com/sms/student/dto/StudentRosterItemDto.java (fields: studentId, studentIdNumber, firstNameLatin, lastNameLatin, gradeLevel, enrollmentDate, status)

#### Service Layer
- [X] T040 [US2] Add getClassDetails method to ClassService interface
- [X] T041 [US2] Implement ClassServiceImpl.getClassDetails in student-service/src/main/java/com/sms/student/service/ClassServiceImpl.java (query by classId, verify teacher authorization, throw ClassNotFoundException if not found)
- [X] T042 [US2] Add getClassStudents method to ClassService interface
- [X] T043 [US2] Implement ClassServiceImpl.getClassStudents (query enrollment records, fetch student details, map to StudentRosterItemDto)
- [X] T044 [US2] Add cache methods in ClassCacheService (cacheClassDetails with 15min TTL, evictClassDetails, cache key: student-service:class:{classId})
- [X] T045 [US2] Integrate cache in ClassServiceImpl.getClassDetails and getClassStudents (cache combined response)

#### Controller
- [X] T046 [US2] Add GET /api/classes/{id} endpoint in ClassController (extract teacherId from JWT, verify authorization, return ClassDetailDto)
- [X] T047 [US2] Add GET /api/classes/{id}/students endpoint in ClassController (extract teacherId from JWT, verify authorization, return StudentRosterItemDto list)
- [X] T048 [US2] Add teacher authorization check (verify class belongs to teacher before returning data)

**Completion Criteria**:
- ✅ Teacher can view detailed class information
- ✅ Class details include all fields (name, grade, subject, capacity, currentEnrollment, description)
- ✅ Student roster shows all enrolled students with basic info
- ✅ Results cached with 15min TTL
- ✅ Authorization enforced (403 if accessing another teacher's class)
- ✅ 404 error returned for non-existent classes
- ✅ Empty student list returned for classes with no enrollments

**Independent Test**:
```bash
# Prerequisites: Create a class first (Phase 5 task, or manual DB insert for testing)
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"0123456789","password":"password123"}' | jq -r '.data.accessToken')

# Assume classId from previous creation
CLASS_ID="550e8400-e29b-41d4-a716-446655440001"

# Test 1: Get class details
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/classes/$CLASS_ID
# Expected: {"errorCode":"SUCCESS","data":{...class details...}}

# Test 2: Get class students
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/classes/$CLASS_ID/students
# Expected: {"errorCode":"SUCCESS","data":[...student roster...]}

# Test 3: Unauthorized access (different teacher's class)
# Login as different teacher, try to access class - should return 403

# Test 4: Non-existent class
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/classes/00000000-0000-0000-0000-000000000000
# Expected: {"errorCode":"CLASS_NOT_FOUND","data":null}

# Test 5: Verify cache
docker exec -it redis redis-cli KEYS "student-service:class:*"
docker exec -it redis redis-cli TTL "student-service:class:$CLASS_ID"
```

---

## Phase 5: User Story 3 & 4 - Enrollment History & Create Class (P2)

**User Stories**:
- US3: As a teacher, I want to view the enrollment history for my class
- US4: As a school administrator, I want to create a new class

**Priority**: P2

**Duration Estimate**: 6-7 hours

**Dependencies**: Must complete Phase 4

### User Story 3 Tasks: Review Enrollment History

#### DTOs
- [ ] T049 [P] [US3] Create EnrollmentHistoryDto in student-service/src/main/java/com/sms/student/dto/EnrollmentHistoryDto.java (fields: id, classId, studentId, studentName, eventType, eventTimestamp, sourceClassId, sourceClassName, destinationClassId, destinationClassName, performedBy, performedByName, notes)

#### Service Layer
- [ ] T050 [US3] Add getEnrollmentHistory method to ClassService interface
- [ ] T051 [US3] Implement ClassServiceImpl.getEnrollmentHistory (query EnrollmentHistoryRepository by classId, order by timestamp DESC, map to DTOs)
- [ ] T052 [US3] Add cache method in ClassCacheService (cacheEnrollmentHistory with 60min TTL, cache key: student-service:class:{classId}:history)
- [ ] T053 [US3] Integrate cache in ClassServiceImpl.getEnrollmentHistory

#### Controller
- [ ] T054 [US3] Add GET /api/classes/{id}/history endpoint in ClassController (verify teacher authorization, return EnrollmentHistoryDto list)

**US3 Completion Criteria**:
- ✅ Teacher can view enrollment history for their classes
- ✅ Events sorted by timestamp DESC (newest first)
- ✅ All event types shown (ENROLLED, TRANSFERRED_OUT, WITHDRAWN, TRANSFERRED_IN)
- ✅ Transfer events show source/destination class info
- ✅ Results cached with 60min TTL
- ✅ Authorization enforced (403 if not teacher's class)
- ✅ Empty list returned for classes with no history

**US3 Independent Test**:
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"0123456789","password":"password123"}' | jq -r '.data.accessToken')

CLASS_ID="550e8400-e29b-41d4-a716-446655440001"

# Test 1: Get enrollment history
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/classes/$CLASS_ID/history
# Expected: {"errorCode":"SUCCESS","data":[...history events sorted DESC...]}

# Test 2: Verify cache
docker exec -it redis redis-cli GET "student-service:class:$CLASS_ID:history"
docker exec -it redis redis-cli TTL "student-service:class:$CLASS_ID:history"
```

### User Story 4 Tasks: Create New Class

#### DTOs
- [ ] T055 [P] [US4] Create ClassCreateRequest in student-service/src/main/java/com/sms/student/dto/ClassCreateRequest.java (fields: name, gradeLevel, subject, academicYear, capacity, description; validation: @NotBlank, @NotNull, @Min, @Max, @Pattern for academicYear)

#### Service Layer
- [ ] T056 [US4] Add createClass method to ClassService interface
- [ ] T057 [US4] Implement ClassServiceImpl.createClass (validate academic year format "YYYY-YYYY", check duplicate via repository.existsByNameAndGradeLevelAndSubjectAndAcademicYear, save entity, return ClassDetailDto)
- [ ] T058 [US4] Add academic year validation helper method in ClassServiceImpl (validate second year = first year + 1)
- [ ] T059 [US4] Implement cache invalidation in createClass (evict teacher's class list cache)

#### Controller
- [ ] T060 [US4] Add POST /api/classes endpoint in ClassController (extract teacherId from JWT, validate request body, call service, return 201 Created)
- [ ] T061 [US4] Add administrator authorization check in ClassController.createClass (verify JWT contains ROLE_ADMIN or special create permission)
- [ ] T062 [US4] Add validation error handling (return 400 with VALIDATION_FAILED error code)
- [ ] T063 [US4] Add duplicate class error handling (return 400 with DUPLICATE_CLASS_NAME error code)
- [ ] T064 [US4] Add invalid academic year error handling (return 400 with INVALID_ACADEMIC_YEAR_FORMAT error code)

**US4 Completion Criteria**:
- ✅ Administrator can create new classes
- ✅ All required fields validated (@NotBlank, @NotNull, capacity 5-60)
- ✅ Academic year format validated ("YYYY-YYYY", second year = first year + 1)
- ✅ Duplicate class detection (same name, grade, subject, year)
- ✅ Teacher's class list cache invalidated after creation
- ✅ Created class appears in teacher's class list
- ✅ 201 Created response with class details
- ✅ Proper error codes for validation failures

**US4 Independent Test**:
```bash
# Login as admin
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"0987654321","password":"admin123"}' | jq -r '.data.accessToken')

# Test 1: Create valid class
curl -X POST http://localhost:8080/api/classes \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mathematics Grade 10A",
    "gradeLevel": "GRADE_10",
    "subject": "Mathematics",
    "academicYear": "2024-2025",
    "capacity": 40,
    "description": "Advanced mathematics"
  }'
# Expected: 201 Created with class details

# Test 2: Create duplicate class (should fail)
# Repeat same request - Expected: {"errorCode":"DUPLICATE_CLASS_NAME","data":null}

# Test 3: Invalid academic year
curl -X POST http://localhost:8080/api/classes \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Physics Grade 11A",
    "gradeLevel": "GRADE_11",
    "subject": "Physics",
    "academicYear": "2024-2026",
    "capacity": 35
  }'
# Expected: {"errorCode":"INVALID_ACADEMIC_YEAR_FORMAT","data":null}

# Test 4: Verify cache invalidation
# List classes before and after creation - cache should be cleared
```

---

## Phase 6: User Story 5 - Update Class Information (P3)

**User Story**: As a school administrator, I want to update class information so that I can correct errors or adjust class details as needed.

**Priority**: P3

**Duration Estimate**: 3-4 hours

**Dependencies**: Must complete Phase 5

### Tasks

#### DTOs
- [ ] T065 [P] [US5] Create ClassUpdateRequest in student-service/src/main/java/com/sms/student/dto/ClassUpdateRequest.java (partial update DTO: capacity, description; all fields optional)

#### Service Layer
- [ ] T066 [US5] Add updateClass method to ClassService interface
- [ ] T067 [US5] Implement ClassServiceImpl.updateClass (fetch existing class, verify teacher authorization, apply updates only for non-null fields, handle optimistic locking, save and return ClassDetailDto)
- [ ] T068 [US5] Implement cache invalidation in updateClass (evict both class list and class details caches)

#### Controller
- [ ] T069 [US5] Add PUT /api/classes/{id} endpoint in ClassController (extract teacherId from JWT, verify authorization, validate request, call service, return 200 OK)
- [ ] T070 [US5] Add authorization check (verify teacher owns class or is admin)
- [ ] T071 [US5] Add optimistic locking conflict handling (return 409 CONCURRENT_UPDATE_CONFLICT if version mismatch)

**Completion Criteria**:
- ✅ Administrator/teacher can update class capacity and description
- ✅ Partial updates supported (only provided fields updated)
- ✅ Optimistic locking prevents concurrent update conflicts
- ✅ Authorization enforced (teacher can only update own classes)
- ✅ Both class list and details caches invalidated
- ✅ Updated class immediately visible in views
- ✅ 409 error for concurrent updates

**Independent Test**:
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"0123456789","password":"password123"}' | jq -r '.data.accessToken')

CLASS_ID="550e8400-e29b-41d4-a716-446655440001"

# Test 1: Update capacity
curl -X PUT http://localhost:8080/api/classes/$CLASS_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "capacity": 45,
    "description": "Increased capacity for high enrollment"
  }'
# Expected: {"errorCode":"SUCCESS","data":{...updated class...}}

# Test 2: Partial update (description only)
curl -X PUT http://localhost:8080/api/classes/$CLASS_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"description": "Updated description"}'

# Test 3: Unauthorized update (different teacher)
# Expected: {"errorCode":"UNAUTHORIZED_CLASS_ACCESS","data":null}

# Test 4: Verify cache invalidation
docker exec -it redis redis-cli KEYS "student-service:teacher:classes:*"
docker exec -it redis redis-cli KEYS "student-service:class:$CLASS_ID"
# Both should be empty after update
```

---

## Phase 7: User Story 6 - Archive Completed Class (P3)

**User Story**: As a school administrator, I want to archive classes that are no longer active so that I can keep my class list focused on current classes while preserving historical records.

**Priority**: P3

**Duration Estimate**: 2-3 hours

**Dependencies**: Must complete Phase 6

### Tasks

#### Service Layer
- [ ] T072 [US6] Add archiveClass method to ClassService interface
- [ ] T073 [US6] Implement ClassServiceImpl.archiveClass (fetch class, verify teacher authorization, set archived=true, save, return success response)
- [ ] T074 [US6] Implement cache invalidation in archiveClass (evict teacher's class list cache)

#### Controller
- [ ] T075 [US6] Add DELETE /api/classes/{id} endpoint in ClassController (extract teacherId from JWT, verify authorization, call service, return 200 OK)
- [ ] T076 [US6] Add authorization check (verify teacher owns class or is admin)
- [ ] T077 [US6] Add OpenAPI documentation note explaining DELETE is soft delete (archived=true)

**Completion Criteria**:
- ✅ Administrator/teacher can archive classes
- ✅ Archived class no longer appears in default class list
- ✅ Archived class accessible with includeArchived=true parameter
- ✅ Students remain enrolled in archived class (no cascade delete)
- ✅ Authorization enforced (teacher can only archive own classes)
- ✅ Class list cache invalidated
- ✅ Historical data preserved (soft delete)

**Independent Test**:
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"0123456789","password":"password123"}' | jq -r '.data.accessToken')

CLASS_ID="550e8400-e29b-41d4-a716-446655440001"

# Test 1: Archive class
curl -X DELETE http://localhost:8080/api/classes/$CLASS_ID \
  -H "Authorization: Bearer $TOKEN"
# Expected: {"errorCode":"SUCCESS","data":{"message":"Class archived successfully"}}

# Test 2: Verify class no longer in default list
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/classes
# Expected: Archived class not in list

# Test 3: Verify class accessible with includeArchived=true
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/classes?includeArchived=true"
# Expected: Archived class appears with archived=true

# Test 4: Verify class details still accessible
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/classes/$CLASS_ID
# Expected: Class details with archived=true

# Test 5: Verify database (soft delete, not hard delete)
docker exec -it postgres-student psql -U sms_user -d student_db \
  -c "SELECT id, name, archived FROM classes WHERE id='$CLASS_ID';"
# Expected: Row exists with archived=true
```

---

## Phase 8: Polish & Cross-Cutting Concerns

**Goal**: Add production-ready features, observability, and integration improvements

**Duration Estimate**: 4-5 hours

**Dependencies**: Must complete all user story phases (3-7)

### Tasks

#### Observability & Monitoring
- [ ] T078 [P] Add Redis health indicator in student-service/src/main/java/com/sms/student/config/RedisConfig.java (custom HealthIndicator bean)
- [ ] T079 [P] Add cache metrics in ClassCacheService (cache hit/miss counters using Micrometer)
- [ ] T080 [P] Add structured logging in ClassServiceImpl (log class creation, updates, archival with correlation IDs)

#### Performance Optimization
- [ ] T081 [P] Add database connection pool tuning in application.yml (HikariCP settings for 100 concurrent users)
- [ ] T082 [P] Optimize ClassRepository queries with @Query hints for frequently accessed data

#### Documentation
- [ ] T083 [P] Update OpenAPIConfig with complete class management API documentation (all 7 endpoints, request/response examples)
- [ ] T084 [P] Add API usage examples in quickstart.md (curl commands for all endpoints)

#### Docker & Deployment
- [ ] T085 Update docker-compose.yml with health checks for student-service and Redis
- [ ] T086 Add environment variable documentation in .env.example for Redis configuration
- [ ] T087 Create docker-compose override for development (docker-compose.override.yml with Redis Commander for cache inspection)

#### Error Handling Improvements
- [ ] T088 [P] Add graceful degradation for Redis failures in RedisCacheService (log warning, return Optional.empty(), continue with database)
- [ ] T089 [P] Add circuit breaker pattern for cache operations (using Resilience4j)

#### Code Quality
- [ ] T090 [P] Add Lombok configuration validation (ensure @Builder works with @Entity)
- [ ] T091 Run code formatter: `cd student-service && ./mvnw fmt:format`
- [ ] T092 Run checkstyle: `cd student-service && ./mvnw checkstyle:check`

**Completion Criteria**:
- ✅ Redis health check added to /actuator/health
- ✅ Cache metrics available in /actuator/metrics
- ✅ Structured logging with correlation IDs
- ✅ Connection pool optimized for 100 concurrent users
- ✅ Complete OpenAPI documentation
- ✅ Docker health checks configured
- ✅ Graceful degradation for Redis failures
- ✅ Code formatted and passes checkstyle

**Independent Test**:
```bash
# Test 1: Health check includes Redis
curl http://localhost:8082/actuator/health
# Expected: {"status":"UP","components":{"db":{"status":"UP"},"redis":{"status":"UP"}}}

# Test 2: Cache metrics available
curl http://localhost:8082/actuator/metrics/cache.gets
curl http://localhost:8082/actuator/metrics/cache.puts

# Test 3: Graceful degradation - stop Redis and verify app still works
docker-compose stop redis
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/classes
# Expected: Works but slower (database fallback)
docker-compose start redis

# Test 4: Code quality checks pass
cd student-service && ./mvnw checkstyle:check
# Expected: BUILD SUCCESS
```

---

## Task Dependencies & Execution Order

### Critical Path (Must Execute Sequentially)

```
Phase 1 (Setup) → Phase 2 (Foundational) → Phase 3 (US1) → Phase 4 (US2) → Phase 5 (US3+US4) → Phase 6 (US5) → Phase 7 (US6) → Phase 8 (Polish)
```

### User Story Dependencies

```
US1 (View Classes) → US2 (View Details)
                   ↓
US2 (View Details) → US3 (Enrollment History)
                   ↓
US1 (View Classes) → US4 (Create Class)
                   ↓
US4 (Create Class) → US5 (Update Class) → US6 (Archive Class)
```

**Key Insights**:
- US1 and US2 are blocking prerequisites for all other stories
- US3 (enrollment history) is independent and can be implemented anytime after US2
- US4 (create) is required before US5 (update) and US6 (archive)
- US5 and US6 are independent of each other

### Parallel Execution Opportunities

**Phase 1** (9 tasks, 7 parallelizable):
```bash
# Run in parallel:
T002, T003, T004, T005, T006, T007, T008

# Run sequentially after parallel tasks:
T001 → [parallel group] → T009
```

**Phase 2** (19 tasks, 14 parallelizable):
```bash
# Run in parallel group 1 (models, enums):
T010, T011, T012, T013

# Run in parallel group 2 (migrations - depends on models):
T014, T015

# Run in parallel group 3 (repositories - depends on models):
T016, T017

# Run in parallel group 4 (config, exceptions):
T018, T022, T023, T024, T025

# Run sequentially:
T019 → T020 → T021 → T026 → T027 → T028
```

**Phase 3** (9 tasks, 3 parallelizable):
```bash
# Run in parallel:
T029, T030, T032

# Run sequentially:
T031 → T033 → T034 → T035 → T036 → T037
```

**Phase 4** (11 tasks, 2 parallelizable):
```bash
# Run in parallel:
T038, T039

# Run sequentially:
T040 → T041 → T042 → T043 → T044 → T045 → T046 → T047 → T048
```

**Phase 5** (16 tasks, 2 parallelizable):
```bash
# US3 and US4 can be implemented in parallel by different developers

# US3 tasks (parallelizable):
T049 (parallel) → T050 → T051 → T052 → T053 → T054

# US4 tasks (parallelizable):
T055 (parallel) → T056 → T057 → T058 → T059 → T060 → T061 → T062 → T063 → T064
```

**Phase 6** (7 tasks, 1 parallelizable):
```bash
T065 (parallel) → T066 → T067 → T068 → T069 → T070 → T071
```

**Phase 7** (6 tasks, 0 parallelizable):
```bash
T072 → T073 → T074 → T075 → T076 → T077
```

**Phase 8** (15 tasks, 9 parallelizable):
```bash
# Run in parallel:
T078, T079, T080, T081, T082, T083, T084, T088, T089, T090

# Run sequentially after parallel tasks:
T085 → T086 → T087 → T091 → T092
```

---

## Implementation Strategy

### MVP Scope (Minimum Viable Product)

**Recommended MVP**: Phase 1-4 only (US1 + US2)

**Rationale**:
- US1 (View Classes) + US2 (View Details) provide core value to teachers
- Teachers can see their classes and student rosters immediately
- Independent and testable without other features
- Demonstrates Redis caching benefits
- Enables early user feedback

**MVP Deliverables**:
- ✅ Teacher can list their classes
- ✅ Teacher can view class details with student roster
- ✅ Redis caching working with measurable performance improvement
- ✅ Authorization and error handling in place
- ✅ ~35 tasks, 2-3 days of development

### Incremental Delivery Plan

**Iteration 1 (MVP)**: Phase 1-4
- Deploy to staging
- Collect teacher feedback on class viewing experience
- Measure cache hit rates and performance

**Iteration 2 (Administrative Features)**: Phase 5
- Add enrollment history (US3) for record-keeping
- Add class creation (US4) for administrators
- Deploy and validate with admin users

**Iteration 3 (Class Lifecycle Management)**: Phase 6-7
- Add update capability (US5)
- Add archive capability (US6)
- Complete end-to-end class lifecycle

**Iteration 4 (Production Hardening)**: Phase 8
- Add observability and monitoring
- Optimize performance
- Add graceful degradation
- Production deployment

### Testing Strategy

**Unit Tests**: Focus on business logic in service layer
- ClassServiceImpl (90% coverage target)
- ClassCacheService
- Validation methods (academic year format)

**Integration Tests**: Test with real database and Redis via TestContainers
- ClassRepository custom query methods
- Cache integration (hit/miss scenarios)
- Flyway migrations

**Contract Tests**: Validate API contracts match OpenAPI spec
- All 7 endpoints
- Request/response DTOs
- Error responses

**Manual Testing**: Follow quickstart.md test scenarios for each user story

---

## Progress Tracking

Use this checklist to track implementation progress:

- [ ] **Phase 1 Complete**: Setup & Infrastructure (9 tasks)
- [ ] **Phase 2 Complete**: Foundational Components (19 tasks)
- [ ] **Phase 3 Complete**: US1 - View My Classes (9 tasks)
- [ ] **Phase 4 Complete**: US2 - View Class Details (11 tasks)
- [ ] **Phase 5 Complete**: US3 & US4 - Enrollment History & Create (16 tasks)
- [ ] **Phase 6 Complete**: US5 - Update Class (7 tasks)
- [ ] **Phase 7 Complete**: US6 - Archive Class (6 tasks)
- [ ] **Phase 8 Complete**: Polish & Cross-Cutting (15 tasks)

**Total Progress**: 0 / 92 tasks (0%)

---

## Success Metrics

Track these metrics after each phase:

| Metric | Target | How to Measure |
|--------|--------|----------------|
| Class list load time (first) | <2s | Chrome DevTools Network tab |
| Class list load time (cached) | <500ms | Chrome DevTools Network tab |
| Cache hit rate | >70% | /actuator/metrics/cache.gets |
| Concurrent teacher capacity | 100+ | Load testing with JMeter |
| Test coverage (service layer) | 90% | `./mvnw test jacoco:report` |
| API contract compliance | 100% | OpenAPI validator |

---

**Tasks Document Status**: ✅ COMPLETE - Ready for implementation

**Next Step**: Begin Phase 1 execution or run `/speckit.implement` for guided implementation
