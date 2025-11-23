# Tasks: Student Class Enrollment Management

**Input**: Design documents from `/specs/004-student-enrollment/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are NOT explicitly requested in the feature specification, so test tasks are omitted. Focus on implementation tasks only.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Microservice**: `student-service/src/main/java/com/sms/student/`
- **Resources**: `student-service/src/main/resources/`
- **Tests**: `student-service/src/test/java/com/sms/student/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Verify student-service project structure matches plan.md requirements
- [ ] T002 Verify existing dependencies in student-service/pom.xml (Spring Boot 3.5.7, Spring Data JPA, Lombok, Hibernate Validator, Flyway)
- [ ] T003 [P] Verify existing security configuration in student-service/src/main/java/com/sms/student/security/
- [ ] T004 [P] Verify existing ApiResponse wrapper in sms-common module

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T005 Create EnrollmentStatus enum in student-service/src/main/java/com/sms/student/model/EnrollmentStatus.java
- [ ] T006 [P] Add enrollment-specific error codes to StudentErrorCode enum in student-service/src/main/java/com/sms/student/exception/StudentErrorCode.java (DUPLICATE_ENROLLMENT, ENROLLMENT_NOT_FOUND, INVALID_ENROLLMENT_STATUS)
- [ ] T007 Create V6__add_enrollment_status_field.sql migration in student-service/src/main/resources/db/migration/ (adds status, transfer_date, transfer_reason fields with indexes and constraints)
- [ ] T008 Update Enrollment entity in student-service/src/main/java/com/sms/student/model/Enrollment.java (add status, transferDate, transferReason fields)
- [ ] T009 Create EnrollmentRepository interface in student-service/src/main/java/com/sms/student/repository/EnrollmentRepository.java (extends JpaRepository with custom query for enrollment history)
- [ ] T010 [P] Create EnrollmentRequest DTO in student-service/src/main/java/com/sms/student/dto/EnrollmentRequest.java
- [ ] T011 [P] Create TransferRequest DTO in student-service/src/main/java/com/sms/student/dto/TransferRequest.java
- [ ] T012 [P] Create EnrollmentResponse DTO in student-service/src/main/java/com/sms/student/dto/EnrollmentResponse.java
- [ ] T013 [P] Create EnrollmentHistoryResponse DTO in student-service/src/main/java/com/sms/student/dto/EnrollmentHistoryResponse.java
- [ ] T014 [P] Create DuplicateEnrollmentException in student-service/src/main/java/com/sms/student/exception/DuplicateEnrollmentException.java
- [ ] T015 [P] Create EnrollmentNotFoundException in student-service/src/main/java/com/sms/student/exception/EnrollmentNotFoundException.java
- [ ] T016 Update GlobalExceptionHandler in student-service/src/main/java/com/sms/student/exception/GlobalExceptionHandler.java to handle new enrollment exceptions
- [ ] T017 Run Flyway migration and verify V6 migration applied successfully (check flyway_schema_history and student_class_enrollments table structure)

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - View Student Enrollment History (Priority: P1) 🎯 MVP

**Goal**: Enable administrators to retrieve and view complete enrollment history for any student, showing all past and current class enrollments with dates, statuses, and transfer information.

**Independent Test**: Query a student's enrollment history via GET /api/students/{id}/enrollment-history and verify:
- All enrollments returned in chronological order (most recent first)
- Response includes enrollment ID, class name, school name, dates, status, transfer info
- Empty list returned for students with no enrollments (not an error)
- Active enrollments distinguished from historical ones
- Transfer dates and reasons displayed when applicable

### Implementation for User Story 1

- [ ] T018 [US1] Create EnrollmentService interface in student-service/src/main/java/com/sms/student/service/EnrollmentService.java (define getEnrollmentHistory method)
- [ ] T019 [US1] Implement getEnrollmentHistory method in student-service/src/main/java/com/sms/student/service/EnrollmentServiceImpl.java (uses JOIN FETCH to load class and school, maps to EnrollmentHistoryResponse with status counts)
- [ ] T020 [US1] Create EnrollmentController in student-service/src/main/java/com/sms/student/controller/EnrollmentController.java with GET /api/students/{id}/enrollment-history endpoint
- [ ] T021 [US1] Add OpenAPI annotations to EnrollmentController.getEnrollmentHistory method (@Operation, @ApiResponses for 200, 404)
- [ ] T022 [US1] Add entity-to-DTO mapping method in EnrollmentServiceImpl (mapToResponse and mapToHistoryResponse helpers with denormalized className and schoolName)
- [ ] T023 [US1] Verify endpoint responds with 404 STUDENT_NOT_FOUND when student ID doesn't exist
- [ ] T024 [US1] Verify endpoint responds with 200 and empty enrollments list when student has no enrollment history
- [ ] T025 [US1] Test endpoint with student having multiple enrollments (verify ordering, status counts, denormalized fields)

**Checkpoint**: At this point, User Story 1 should be fully functional - administrators can view complete enrollment history for any student

---

## Phase 4: User Story 2 - Enroll Student in Class (Priority: P2)

**Goal**: Enable administrators to enroll students in classes with automatic capacity validation, duplicate prevention, and enrollment history tracking.

**Independent Test**: Enroll a student in a class via POST /api/students/{id}/enroll and verify:
- Enrollment record created with correct student ID, class ID, enrollment date (today), status ACTIVE, reason NEW
- Response includes enrollment details with denormalized class name and school name
- Duplicate enrollment prevented (409 DUPLICATE_ENROLLMENT) when student already enrolled in same class
- Capacity enforcement (409 CLASS_CAPACITY_EXCEEDED) when class is full
- Student and class existence validated (404 errors returned appropriately)
- Class student_count incremented by 1

### Implementation for User Story 2

- [ ] T026 [US2] Add enrollStudent method to EnrollmentService interface in student-service/src/main/java/com/sms/student/service/EnrollmentService.java
- [ ] T027 [US2] Implement enrollStudent method in EnrollmentServiceImpl with @Transactional annotation (validates student exists, class exists, no duplicate enrollment, capacity available, creates enrollment with status ACTIVE and reason NEW, increments class student_count)
- [ ] T028 [US2] Add POST /api/students/{id}/enroll endpoint to EnrollmentController with @Valid request body
- [ ] T029 [US2] Add OpenAPI annotations to EnrollmentController.enrollStudent method (@Operation, @ApiResponses for 200, 400, 404, 409)
- [ ] T030 [US2] Add duplicate enrollment check in EnrollmentServiceImpl (query for active enrollment with same student_id and class_id, throw DuplicateEnrollmentException if exists)
- [ ] T031 [US2] Add class capacity validation in EnrollmentServiceImpl (check if studentCount >= maxCapacity, throw ClassCapacityExceededException if full, use optimistic locking with version field)
- [ ] T032 [US2] Add class student_count increment logic in EnrollmentServiceImpl (classEntity.setStudentCount(count + 1), save class entity)
- [ ] T033 [US2] Verify endpoint creates enrollment successfully with 200 response when all validations pass
- [ ] T034 [US2] Verify endpoint returns 404 STUDENT_NOT_FOUND when student doesn't exist
- [ ] T035 [US2] Verify endpoint returns 404 CLASS_NOT_FOUND when class doesn't exist
- [ ] T036 [US2] Verify endpoint returns 409 DUPLICATE_ENROLLMENT when student already enrolled in class
- [ ] T037 [US2] Verify endpoint returns 409 CLASS_CAPACITY_EXCEEDED when class is full
- [ ] T038 [US2] Verify endpoint returns 400 VALIDATION_ERROR when classId is null or notes exceed 500 characters
- [ ] T039 [US2] Verify enrollment appears in student's history after successful enrollment (test integration with User Story 1)

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently - administrators can view history and enroll students with proper validation

---

## Phase 5: User Story 3 - Transfer Student to New Class (Priority: P3)

**Goal**: Enable administrators to transfer students between classes while maintaining complete audit trail, ensuring atomicity, and preserving historical records.

**Independent Test**: Transfer a student from one class to another via POST /api/students/{id}/transfer and verify:
- Old enrollment marked as TRANSFERRED with transfer_date (today) and transfer_reason from request
- Old enrollment end_date set to today
- New enrollment created with status ACTIVE, reason TRANSFER, enrollment_date (today)
- Both operations succeed atomically (transaction rollback on any failure)
- Source class student_count decremented by 1
- Target class student_count incremented by 1
- Target class capacity validated before transfer
- Transfer reason recorded and visible in enrollment history
- Student existence and active enrollment validated (404 errors returned appropriately)

### Implementation for User Story 3

- [ ] T040 [US3] Add transferStudent method to EnrollmentService interface in student-service/src/main/java/com/sms/student/service/EnrollmentService.java
- [ ] T041 [US3] Implement transferStudent method in EnrollmentServiceImpl with @Transactional annotation (validates student exists, finds active enrollment, validates target class exists and has capacity, marks old enrollment as TRANSFERRED, creates new enrollment with reason TRANSFER, updates both class student_counts)
- [ ] T042 [US3] Add POST /api/students/{id}/transfer endpoint to EnrollmentController with @Valid request body
- [ ] T043 [US3] Add OpenAPI annotations to EnrollmentController.transferStudent method (@Operation, @ApiResponses for 200, 400, 404, 409)
- [ ] T044 [US3] Add active enrollment lookup in EnrollmentServiceImpl (query for enrollment with student_id and status ACTIVE, throw EnrollmentNotFoundException if not found or multiple found)
- [ ] T045 [US3] Add old enrollment closure logic in EnrollmentServiceImpl (set status=TRANSFERRED, endDate=today, transferDate=today, transferReason=request.reason)
- [ ] T046 [US3] Add new enrollment creation logic in EnrollmentServiceImpl (create enrollment with student, targetClass, enrollmentDate=today, reason=TRANSFER, status=ACTIVE, notes=request.reason)
- [ ] T047 [US3] Add class student count update logic in EnrollmentServiceImpl (decrement old class count, increment new class count, save both)
- [ ] T048 [US3] Add target class capacity validation in EnrollmentServiceImpl (reuse capacity check from User Story 2, use optimistic locking)
- [ ] T049 [US3] Verify endpoint transfers student successfully with 200 response when all validations pass
- [ ] T050 [US3] Verify endpoint returns 404 STUDENT_NOT_FOUND when student doesn't exist
- [ ] T051 [US3] Verify endpoint returns 404 ENROLLMENT_NOT_FOUND when student has no active enrollment
- [ ] T052 [US3] Verify endpoint returns 404 CLASS_NOT_FOUND when target class doesn't exist
- [ ] T053 [US3] Verify endpoint returns 409 CLASS_CAPACITY_EXCEEDED when target class is full
- [ ] T054 [US3] Verify endpoint returns 400 VALIDATION_ERROR when targetClassId is null or reason is blank/exceeds 500 characters
- [ ] T055 [US3] Verify transaction atomicity by simulating failure scenarios (e.g., invalid target class after marking old enrollment - entire operation should rollback)
- [ ] T056 [US3] Verify both old and new enrollments appear in student's history after successful transfer (test integration with User Story 1)
- [ ] T057 [US3] Verify class student counts are correctly updated for both source and target classes

**Checkpoint**: All user stories should now be independently functional - complete enrollment management workflow available

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T058 [P] Add comprehensive logging for enrollment operations in EnrollmentServiceImpl (log student ID, class ID, operation type, success/failure)
- [ ] T059 [P] Add audit field population in EnrollmentServiceImpl (set createdBy and updatedBy from JWT token user ID)
- [ ] T060 [P] Verify all endpoints require JWT authentication (test with missing/invalid token, expect 401 UNAUTHORIZED)
- [ ] T061 [P] Verify all endpoints enforce ADMIN or TEACHER role authorization (test with insufficient role, expect 403 FORBIDDEN)
- [ ] T062 [P] Update OpenAPIConfig in student-service/src/main/java/com/sms/student/config/OpenAPIConfig.java to include enrollment endpoints in API documentation
- [ ] T063 [P] Verify Swagger UI displays all enrollment endpoints correctly at http://localhost:8080/swagger-ui.html (via API Gateway)
- [ ] T064 [P] Test performance of enrollment history endpoint with student having 100+ enrollments (verify < 2 seconds per SC-001)
- [ ] T065 [P] Test performance of enroll and transfer operations under normal load (verify < 1 second per SC-002)
- [ ] T066 [P] Test concurrent enrollment scenarios (multiple admins enrolling different students in same class near capacity, verify no over-enrollment)
- [ ] T067 [P] Add database indexes verification (confirm idx_enrollment_status and idx_enrollment_status_date exist and improve query performance)
- [ ] T068 Code cleanup and refactoring (remove unused imports, apply consistent formatting, ensure all classes follow project conventions)
- [ ] T069 Run quickstart.md validation (follow developer setup guide, test all three endpoints with cURL commands)
- [ ] T070 Document any deviations from plan.md or data-model.md in implementation notes

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User Story 1 (P1): Can start after Foundational (Phase 2) - No dependencies on other stories
  - User Story 2 (P2): Can start after Foundational (Phase 2) - No dependencies on other stories
  - User Story 3 (P3): Can start after Foundational (Phase 2) - No dependencies on other stories (though logically builds on US1 and US2 concepts)
- **Polish (Phase 6)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Independently testable, integrates with US1 for history visibility
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - Independently testable, integrates with US1 for history visibility

**Note**: While US2 and US3 integrate with US1 (enrollment history), they can be implemented independently. Integration testing comes after each story's core functionality is complete.

### Within Each User Story

- Service interface before implementation
- Service implementation before controller
- Controller before OpenAPI annotations
- Core implementation before validation tests
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (T010-T016: DTOs and exceptions)
- Once Foundational phase completes, all user stories CAN be worked in parallel by different developers
- All Polish tasks marked [P] can run in parallel

---

## Parallel Example: Foundational Phase

```bash
# These DTOs can be created in parallel (all marked [P]):
Task T010: "Create EnrollmentRequest DTO"
Task T011: "Create TransferRequest DTO"
Task T012: "Create EnrollmentResponse DTO"
Task T013: "Create EnrollmentHistoryResponse DTO"

# These exceptions can be created in parallel (all marked [P]):
Task T014: "Create DuplicateEnrollmentException"
Task T015: "Create EnrollmentNotFoundException"
```

## Parallel Example: Multi-Developer Strategy

```bash
# After Foundational phase completes, assign stories to different developers:

Developer A: User Story 1 (T018-T025) - Enrollment History
Developer B: User Story 2 (T026-T039) - Enroll Student
Developer C: User Story 3 (T040-T057) - Transfer Student

# Each developer works independently, stories integrate after core completion
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T004)
2. Complete Phase 2: Foundational (T005-T017) - CRITICAL - blocks all stories
3. Complete Phase 3: User Story 1 (T018-T025)
4. **STOP and VALIDATE**: Test enrollment history endpoint independently
5. Deploy/demo if ready - delivers value immediately for enrollment tracking

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 (T018-T025) → Test independently → Deploy/Demo (MVP!)
   - **Value delivered**: Administrators can view complete enrollment history
3. Add User Story 2 (T026-T039) → Test independently → Deploy/Demo
   - **Value delivered**: Administrators can enroll students with validation
4. Add User Story 3 (T040-T057) → Test independently → Deploy/Demo
   - **Value delivered**: Administrators can transfer students between classes
5. Add Polish (T058-T070) → Full feature complete
   - **Value delivered**: Production-ready with performance, security, documentation

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together (T001-T017)
2. Once Foundational is done:
   - Developer A: User Story 1 (T018-T025)
   - Developer B: User Story 2 (T026-T039)
   - Developer C: User Story 3 (T040-T057)
3. Stories complete and integrate independently
4. Team tackles Polish together (T058-T070)

---

## Technical Notes

### Database Migration (V6)

The V6 migration adds critical fields to existing `student_class_enrollments` table:
- `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' (with CHECK constraint)
- `transfer_date` DATE NULL
- `transfer_reason` VARCHAR(500) NULL
- Indexes: `idx_enrollment_status`, `idx_enrollment_status_date`
- Constraint: `chk_transfer_fields` (ensures transfer fields populated when status=TRANSFERRED)

### Transaction Management

Transfer operation (User Story 3) uses `@Transactional` to ensure atomicity:
1. Mark old enrollment as TRANSFERRED (status, dates, reason)
2. Create new enrollment (status ACTIVE, reason TRANSFER)
3. Update both class student counts (decrement old, increment new)
4. Any failure rolls back entire transaction

### Concurrency Control

Class capacity enforcement uses optimistic locking:
- Classes table has `version` field (already exists)
- When incrementing student_count, version auto-increments
- If version changed between read and write, `OptimisticLockException` thrown
- Transaction rolls back, preventing over-enrollment

### Performance Requirements

- **Enrollment history**: Must complete in < 2 seconds (SC-001)
  - Uses JOIN FETCH to prevent N+1 queries
  - Indexes on status and enrollment_date optimize queries
- **Enroll/Transfer operations**: Must complete in < 1 second (SC-002)
  - Atomic transactions minimize lock time
  - Optimistic locking avoids pessimistic locks

### Error Handling

New error codes added to `StudentErrorCode` enum:
- `DUPLICATE_ENROLLMENT` (409 Conflict)
- `ENROLLMENT_NOT_FOUND` (404 Not Found)
- `INVALID_ENROLLMENT_STATUS` (400 Bad Request)

Existing error codes reused:
- `STUDENT_NOT_FOUND` (404 Not Found)
- `CLASS_NOT_FOUND` (404 Not Found)
- `CLASS_CAPACITY_EXCEEDED` (409 Conflict)
- `VALIDATION_ERROR` (400 Bad Request)

### API Response Pattern

All endpoints return `ApiResponse<T>` wrapper:
```json
{
  "errorCode": "SUCCESS",
  "data": { ... }
}
```

Error responses:
```json
{
  "errorCode": "STUDENT_NOT_FOUND",
  "data": null
}
```

---

## Notes

- [P] tasks = different files, no dependencies, can run in parallel
- [Story] label maps task to specific user story for traceability (US1, US2, US3)
- Each user story should be independently completable and testable
- Commit after each task or logical group of related tasks
- Stop at any checkpoint to validate story independently
- Database migration V6 must succeed before any enrollment operations
- Verify optimistic locking prevents race conditions during capacity checks
- All endpoints secured via existing JWT filter (enforced by Spring Security)
