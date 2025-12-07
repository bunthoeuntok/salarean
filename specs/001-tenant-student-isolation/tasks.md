# Tasks: Teacher-Based Student Data Isolation

**Input**: Design documents from `/specs/001-tenant-student-isolation/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/endpoints.md

**Tests**: Tests are included in this task list as the feature specification requires comprehensive test coverage (per Constitution Principle V: Test Discipline).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

All paths are relative to `student-service/` directory within the monorepo.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Database migration and configuration setup for teacher isolation

- [ ] T001 Create database migration script `src/main/resources/db/migration/V6__add_teacher_id_to_students.sql`
- [ ] T002 [P] Add Redis cache dependencies to `pom.xml` (spring-boot-starter-cache, spring-boot-starter-data-redis)
- [ ] T003 [P] Configure Redis cache settings in `src/main/resources/application-docker.yml`
- [ ] T004 [P] Configure Redis cache settings in `src/main/resources/application.yml`
- [ ] T005 Apply database migration to add teacher_id column and index

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T006 Create TeacherContextHolder class in `src/main/java/com/sms/student/security/TeacherContextHolder.java`
- [ ] T007 Update JwtAuthenticationFilter to set/clear teacher context in `src/main/java/com/sms/student/security/JwtAuthenticationFilter.java`
- [ ] T008 Create UnauthorizedAccessException class in `src/main/java/com/sms/student/exception/UnauthorizedAccessException.java`
- [ ] T009 [P] Add UNAUTHORIZED_ACCESS and TEACHER_CONTEXT_MISSING to ErrorCode enum
- [ ] T010 Update GlobalExceptionHandler to handle UnauthorizedAccessException in `src/main/java/com/sms/student/exception/GlobalExceptionHandler.java`
- [ ] T011 Create RedisConfig class in `src/main/java/com/sms/student/config/RedisConfig.java`
- [ ] T012 Add teacher_id field to Student entity in `src/main/java/com/sms/student/model/Student.java`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Teacher Views Only Their Own Students (Priority: P1) 🎯 MVP

**Goal**: Teachers can list and view students, seeing only students they own. This is the core security feature ensuring data isolation.

**Independent Test**: Create two teachers with different students, login as each teacher, verify each sees only their own students. Test unauthorized access returns 401.

### Tests for User Story 1

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T013 [P] [US1] Create StudentRepositoryTest for teacher isolation queries in `src/test/java/com/sms/student/repository/StudentRepositoryTest.java`
- [ ] T014 [P] [US1] Create StudentServiceTest for getAllStudents isolation in `src/test/java/com/sms/student/service/StudentServiceTest.java`
- [ ] T015 [P] [US1] Add integration test for GET /api/students endpoint with teacher filtering

### Implementation for User Story 1

- [ ] T016 [P] [US1] Add findAllByTeacherId query to StudentRepository in `src/main/java/com/sms/student/repository/StudentRepository.java`
- [ ] T017 [P] [US1] Add findByIdAndTeacherId query to StudentRepository in `src/main/java/com/sms/student/repository/StudentRepository.java`
- [ ] T018 [US1] Update getAllStudents method in StudentService to filter by teacher_id in `src/main/java/com/sms/student/service/StudentService.java`
- [ ] T019 [US1] Update getStudentById method in StudentService to validate ownership in `src/main/java/com/sms/student/service/StudentService.java`
- [ ] T020 [US1] Add @Cacheable annotation to getAllStudents method with teacher-scoped cache key
- [ ] T021 [US1] Add @Cacheable annotation to getStudentById method with teacher-scoped cache key
- [ ] T022 [US1] Verify StudentController GET /api/students endpoint uses updated service methods
- [ ] T023 [US1] Verify StudentController GET /api/students/{id} endpoint uses updated service methods
- [ ] T024 [US1] Run tests to verify teacher isolation for read operations

**Checkpoint**: At this point, User Story 1 should be fully functional - teachers can view only their students

---

## Phase 4: User Story 2 - Teacher Creates Students Under Their Account (Priority: P2)

**Goal**: Teachers can create new students, with automatic teacher_id assignment ensuring ownership.

**Independent Test**: Create a student as Teacher A, verify student has teacher_id = Teacher A's ID. Verify Teacher B cannot access this student.

### Tests for User Story 2

- [ ] T025 [P] [US2] Add test for createStudent auto-assigns teacher_id in StudentServiceTest
- [ ] T026 [P] [US2] Add test for created student inaccessible to other teachers in StudentServiceTest
- [ ] T027 [P] [US2] Add integration test for POST /api/students with teacher_id validation

### Implementation for User Story 2

- [ ] T028 [US2] Update createStudent method in StudentService to auto-assign teacher_id from context in `src/main/java/com/sms/student/service/StudentService.java`
- [ ] T029 [US2] Update createStudent method to set createdBy field to teacher_id
- [ ] T030 [US2] Add @CacheEvict annotation to createStudent to evict teacher's student list cache
- [ ] T031 [US2] Update findByStudentCodeAndTeacherId query in StudentRepository for duplicate checking
- [ ] T032 [US2] Update existsByStudentCodeAndTeacherId method in StudentRepository to include teacher_id filter
- [ ] T033 [US2] Verify StudentController POST /api/students does not accept teacher_id in request body
- [ ] T034 [US2] Run tests to verify automatic teacher_id assignment

**Checkpoint**: At this point, User Stories 1 AND 2 should both work - teachers can view and create students

---

## Phase 5: User Story 3 - Teacher Updates Only Their Own Students (Priority: P2)

**Goal**: Teachers can update student information, but only for students they own. Unauthorized updates are blocked with 401.

**Independent Test**: Update a student as Teacher A (success), attempt to update Teacher B's student as Teacher A (401 error).

### Tests for User Story 3

- [ ] T035 [P] [US3] Add test for updateStudent validates ownership in StudentServiceTest
- [ ] T036 [P] [US3] Add test for updateStudent rejects cross-teacher access in StudentServiceTest
- [ ] T037 [P] [US3] Add integration test for PUT /api/students/{id} with authorization validation

### Implementation for User Story 3

- [ ] T038 [US3] Update updateStudent method in StudentService to validate ownership using findByIdAndTeacherId in `src/main/java/com/sms/student/service/StudentService.java`
- [ ] T039 [US3] Update updatedBy field to use teacher_id from context in updateStudent method
- [ ] T040 [US3] Add @CacheEvict annotation to updateStudent to invalidate cached student data
- [ ] T041 [US3] Ensure teacher_id field is immutable (cannot be changed via update request)
- [ ] T042 [US3] Verify StudentController PUT /api/students/{id} endpoint handles UnauthorizedAccessException
- [ ] T043 [US3] Run tests to verify update authorization

**Checkpoint**: User Stories 1, 2, AND 3 should all work - teachers can view, create, and update their own students

---

## Phase 6: User Story 4 - Teacher Deletes Only Their Own Students (Priority: P3)

**Goal**: Teachers can soft-delete students, but only students they own. Unauthorized deletes are blocked with 401.

**Independent Test**: Delete a student as Teacher A (success), attempt to delete Teacher B's student as Teacher A (401 error).

### Tests for User Story 4

- [ ] T044 [P] [US4] Add test for deleteStudent validates ownership in StudentServiceTest
- [ ] T045 [P] [US4] Add test for deleteStudent rejects cross-teacher access in StudentServiceTest
- [ ] T046 [P] [US4] Add integration test for DELETE /api/students/{id} with authorization validation

### Implementation for User Story 4

- [ ] T047 [US4] Update deleteStudent method in StudentService to validate ownership using findByIdAndTeacherId in `src/main/java/com/sms/student/service/StudentService.java`
- [ ] T048 [US4] Update deletedBy field to use teacher_id from context in deleteStudent method
- [ ] T049 [US4] Add @CacheEvict annotation to deleteStudent to invalidate cached student data
- [ ] T050 [US4] Verify StudentController DELETE /api/students/{id} endpoint handles UnauthorizedAccessException
- [ ] T051 [US4] Run tests to verify delete authorization

**Checkpoint**: All user stories should now be independently functional - full CRUD with teacher isolation

---

## Phase 7: Cache Reload Feature (Additional Requirement)

**Goal**: Allow teachers to manually reload their cached student data via API endpoint.

**Independent Test**: Cache student data for Teacher A, call reload endpoint, verify next request refetches from database.

### Tests for Cache Reload

- [ ] T052 [P] Add test for clearTeacherCache evicts only teacher's cache in CacheServiceTest
- [ ] T053 [P] Add integration test for POST /api/cache/reload endpoint

### Implementation for Cache Reload

- [ ] T054 [P] Create CacheReloadResponse DTO in `src/main/java/com/sms/student/dto/CacheReloadResponse.java`
- [ ] T055 [P] Create ICacheService interface in `src/main/java/com/sms/student/service/interfaces/ICacheService.java`
- [ ] T056 Create CacheService implementation in `src/main/java/com/sms/student/service/CacheService.java`
- [ ] T057 Add clearTeacherCache method to IStudentService interface in `src/main/java/com/sms/student/service/interfaces/IStudentService.java`
- [ ] T058 Implement clearTeacherCache in StudentService with @CacheEvict annotation
- [ ] T059 Create CacheController with reload endpoint in `src/main/java/com/sms/student/controller/CacheController.java`
- [ ] T060 Run tests to verify cache reload functionality

**Checkpoint**: Cache reload feature is complete and tested

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Final verification, documentation, and cross-story validation

- [ ] T061 [P] Run all unit tests to verify teacher isolation across all CRUD operations
- [ ] T062 [P] Run all integration tests to verify API contract compliance
- [ ] T063 [P] Perform manual testing per quickstart.md validation steps
- [ ] T064 [P] Verify database migration can be rolled back (dev environment only)
- [ ] T065 [P] Update OpenAPI/Swagger documentation for modified endpoints
- [ ] T066 Code review for security vulnerabilities (SQL injection, cache poisoning)
- [ ] T067 Performance test: Verify student list queries complete in <2 seconds (SC-001)
- [ ] T068 Concurrency test: Verify system handles 50+ concurrent teachers (SC-004)
- [ ] T069 Security test: Verify 100% of unauthorized access attempts are blocked (SC-002)
- [ ] T070 [P] Update CLAUDE.md with implementation notes (if applicable)
- [ ] T071 Final validation: Run entire quickstart.md test suite

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-6)**: All depend on Foundational phase completion
  - User stories can proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P2 → P3)
- **Cache Reload (Phase 7)**: Can start after Foundational - independent of user stories
- **Polish (Phase 8)**: Depends on all desired user stories + cache reload being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Independent of US1 but benefits from US1 for testing
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - Depends on US1 for read validation
- **User Story 4 (P3)**: Can start after Foundational (Phase 2) - Depends on US1 for read validation

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Repository queries before service layer
- Service layer before controller endpoints
- Core implementation before cache annotations
- Story complete before moving to next priority

### Parallel Opportunities

**Setup Phase (Phase 1)**:
```bash
# Can run in parallel:
T002 (pom.xml dependencies)
T003 (application-docker.yml)
T004 (application.yml)
```

**Foundational Phase (Phase 2)**:
```bash
# Can run in parallel:
T009 (ErrorCode enum)
```

**User Story 1 - Tests**:
```bash
# Can run in parallel:
T013 (StudentRepositoryTest)
T014 (StudentServiceTest)
T015 (Integration test)
```

**User Story 1 - Implementation**:
```bash
# Can run in parallel:
T016 (findAllByTeacherId)
T017 (findByIdAndTeacherId)
```

**User Story 2 - Tests**:
```bash
# Can run in parallel:
T025 (auto-assign test)
T026 (cross-teacher test)
T027 (integration test)
```

**User Story 3 - Tests**:
```bash
# Can run in parallel:
T035 (ownership validation test)
T036 (cross-teacher test)
T037 (integration test)
```

**User Story 4 - Tests**:
```bash
# Can run in parallel:
T044 (ownership validation test)
T045 (cross-teacher test)
T046 (integration test)
```

**Cache Reload - Tests**:
```bash
# Can run in parallel:
T052 (cache eviction test)
T053 (integration test)
```

**Cache Reload - Implementation**:
```bash
# Can run in parallel:
T054 (CacheReloadResponse DTO)
T055 (ICacheService interface)
```

**Polish Phase**:
```bash
# Can run in parallel:
T061 (unit tests)
T062 (integration tests)
T063 (manual testing)
T064 (migration rollback)
T065 (OpenAPI docs)
T070 (CLAUDE.md update)
```

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Create StudentRepositoryTest for teacher isolation queries"
Task: "Create StudentServiceTest for getAllStudents isolation"
Task: "Add integration test for GET /api/students endpoint"

# Launch repository queries together:
Task: "Add findAllByTeacherId query to StudentRepository"
Task: "Add findByIdAndTeacherId query to StudentRepository"

# After repository queries complete, update service layer
Task: "Update getAllStudents method in StudentService"
Task: "Update getStudentById method in StudentService"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T005) - ~30 minutes
2. Complete Phase 2: Foundational (T006-T012) - ~1 hour
   **CRITICAL CHECKPOINT**: Foundation must be solid
3. Complete Phase 3: User Story 1 (T013-T024) - ~1.5 hours
4. **STOP and VALIDATE**: Test User Story 1 independently
   - Create 2 teachers with students
   - Verify each sees only their own students
   - Test unauthorized access returns 401
5. Deploy/demo if ready (read-only teacher isolation MVP)

**Estimated MVP Time**: 3 hours

### Incremental Delivery

1. **Foundation** (Phase 1-2): Setup + Foundational → ~1.5 hours
2. **MVP** (Phase 3): User Story 1 → Test independently → Deploy/Demo (~1.5 hours)
3. **Create Feature** (Phase 4): User Story 2 → Test independently → Deploy/Demo (~1 hour)
4. **Update Feature** (Phase 5): User Story 3 → Test independently → Deploy/Demo (~1 hour)
5. **Delete Feature** (Phase 6): User Story 4 → Test independently → Deploy/Demo (~45 minutes)
6. **Cache Reload** (Phase 7): Manual cache refresh → Test independently → Deploy/Demo (~45 minutes)
7. **Polish** (Phase 8): Final validation and performance testing (~1 hour)

**Total Estimated Time**: 6-8 hours (matches quickstart.md estimate)

### Parallel Team Strategy

With multiple developers:

1. **Together**: Complete Setup + Foundational (Phase 1-2) → ~1.5 hours
2. **Once Foundational is done**:
   - Developer A: User Story 1 (Phase 3) → Read operations
   - Developer B: User Story 2 (Phase 4) → Create operations
   - Developer C: Cache Reload (Phase 7) → Cache management
3. **Then**:
   - Developer A: User Story 3 (Phase 5) → Update operations
   - Developer B: User Story 4 (Phase 6) → Delete operations
4. **Finally Together**: Polish (Phase 8) → Cross-story validation

**Parallel Estimated Time**: 3-4 hours (with 3 developers)

---

## Task Summary

**Total Tasks**: 71

**Breakdown by Phase**:
- Setup: 5 tasks
- Foundational: 7 tasks (CRITICAL - blocks all stories)
- User Story 1 (P1): 12 tasks (MVP)
- User Story 2 (P2): 10 tasks
- User Story 3 (P2): 9 tasks
- User Story 4 (P3): 8 tasks
- Cache Reload: 9 tasks
- Polish: 11 tasks

**Breakdown by Type**:
- Tests: 18 tasks (25%)
- Repository: 5 tasks
- Service Layer: 16 tasks
- Controllers: 4 tasks
- Configuration: 8 tasks
- DTOs/Models: 3 tasks
- Security: 5 tasks
- Validation/Polish: 12 tasks

**Parallelizable Tasks**: 31 tasks marked with [P] (44%)

**Independent Test Criteria**:
- **US1**: Two teachers, each sees only own students, unauthorized access blocked
- **US2**: Created student has correct teacher_id, inaccessible to other teachers
- **US3**: Can update own students, cannot update other teachers' students
- **US4**: Can delete own students, cannot delete other teachers' students
- **Cache**: Cache reload evicts only teacher's cache, next request refetches

**Suggested MVP Scope**:
- Phase 1: Setup (5 tasks)
- Phase 2: Foundational (7 tasks)
- Phase 3: User Story 1 (12 tasks)
- **Total MVP: 24 tasks** (~3 hours)

**Format Validation**: ✅ All tasks follow checklist format with checkbox, ID, optional [P] and [Story] labels, and file paths

---

## Notes

- [P] tasks = different files, no dependencies within same phase
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Write tests first, verify they fail before implementing
- Commit after each logical task group
- Stop at any checkpoint to validate story independently
- Teacher isolation must be tested at repository, service, and controller levels
- Cache keys must always include teacher_id to prevent cross-teacher pollution
- Performance targets: <2s list queries, 50+ concurrent teachers, 100% unauthorized access blocking
