# Tasks: Teacher School Setup

**Input**: Design documents from `/specs/009-teacher-school-setup/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Not explicitly requested in specification - tests marked as OPTIONAL

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

This is a **monorepo web application** with:
- Backend: `auth-service/`, `student-service/` (Spring Boot microservices)
- Frontend: `frontend/` (React 19 + Vite)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and database schema foundations

- [X] T001 Create Flyway migration V12 for provinces and districts tables in student-service/src/main/resources/db/migration/V12__create_provinces_and_districts_tables.sql
- [X] T002 Create Flyway migration V13 to populate provinces/districts from existing data in student-service/src/main/resources/db/migration/V13__populate_provinces_and_districts_from_existing_data.sql
- [X] T003 Create Flyway migration V14 to add province_id and district_id foreign keys to schools table in student-service/src/main/resources/db/migration/V14__add_province_and_district_foreign_keys_to_schools.sql
- [X] T004 Create Flyway migration V5 for teacher_school table in auth-service/src/main/resources/db/migration/V5__create_teacher_school_table.sql
- [X] T005 Run all database migrations to verify schema changes (./mvnw flyway:migrate in both services)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core entities, repositories, and error code enums that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### student-service Foundational Tasks

- [X] T006 [P] Create Province entity in student-service/src/main/java/com/sms/student/model/Province.java
- [X] T007 [P] Create District entity in student-service/src/main/java/com/sms/student/model/District.java
- [X] T008 [P] Update School entity to add provinceId and districtId fields in student-service/src/main/java/com/sms/student/model/School.java
- [X] T009 [P] Create ProvinceRepository interface in student-service/src/main/java/com/sms/student/repository/ProvinceRepository.java
- [X] T010 [P] Create DistrictRepository interface in student-service/src/main/java/com/sms/student/repository/DistrictRepository.java
- [X] T011 [P] Update SchoolRepository with findByDistrictId method in student-service/src/main/java/com/sms/student/repository/SchoolRepository.java
- [X] T012 [P] Add error codes (PROVINCE_NOT_FOUND, DISTRICT_NOT_FOUND, DUPLICATE_SCHOOL_NAME) to student-service ErrorCode enum

### auth-service Foundational Tasks

- [X] T013 [P] Create TeacherSchool entity in auth-service/src/main/java/com/sms/auth/model/TeacherSchool.java
- [X] T014 [P] Create TeacherSchoolRepository interface in auth-service/src/main/java/com/sms/auth/repository/TeacherSchoolRepository.java
- [X] T015 [P] Add error codes (TEACHER_ALREADY_ASSIGNED, SCHOOL_NOT_FOUND, INVALID_PRINCIPAL_DATA) to auth-service ErrorCode enum

### DTOs (Data Transfer Objects)

- [X] T016 [P] Create ProvinceResponse DTO in student-service/src/main/java/com/sms/student/dto/ProvinceResponse.java
- [X] T017 [P] Create DistrictResponse DTO in student-service/src/main/java/com/sms/student/dto/DistrictResponse.java
- [X] T018 [P] Create SchoolRequest DTO in student-service/src/main/java/com/sms/student/dto/SchoolRequest.java
- [X] T019 [P] Create SchoolResponse DTO in student-service/src/main/java/com/sms/student/dto/SchoolResponse.java
- [X] T020 [P] Create TeacherSchoolRequest DTO in auth-service/src/main/java/com/sms/auth/dto/TeacherSchoolRequest.java
- [X] T021 [P] Create TeacherSchoolResponse DTO in auth-service/src/main/java/com/sms/auth/dto/TeacherSchoolResponse.java

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Select Existing School (Priority: P1) 🎯 MVP

**Goal**: Enable teachers to select an existing school from database through hierarchical location flow (Province → District → School) and associate their account.

**Independent Test**: Register a new teacher account, verify redirect to school setup page, select province → district → school, confirm teacher-school association persisted in database.

### Backend Implementation for User Story 1

#### student-service - Location APIs

- [ ] T022 [P] [US1] Create IProvinceService interface in student-service/src/main/java/com/sms/student/service/interfaces/IProvinceService.java
- [ ] T023 [P] [US1] Implement ProvinceService in student-service/src/main/java/com/sms/student/service/ProvinceService.java
- [ ] T024 [P] [US1] Create IDistrictService interface in student-service/src/main/java/com/sms/student/service/interfaces/IDistrictService.java
- [ ] T025 [P] [US1] Implement DistrictService in student-service/src/main/java/com/sms/student/service/DistrictService.java
- [ ] T026 [US1] Update ISchoolService to add getSchoolsByDistrict method in student-service/src/main/java/com/sms/student/service/interfaces/ISchoolService.java
- [ ] T027 [US1] Implement getSchoolsByDistrict in SchoolService in student-service/src/main/java/com/sms/student/service/SchoolService.java
- [ ] T028 [P] [US1] Create ProvinceController with GET /api/provinces endpoint in student-service/src/main/java/com/sms/student/controller/ProvinceController.java
- [ ] T029 [P] [US1] Create DistrictController with GET /api/districts endpoint in student-service/src/main/java/com/sms/student/controller/DistrictController.java
- [ ] T030 [US1] Add GET /api/schools?districtId={uuid} endpoint to SchoolController in student-service/src/main/java/com/sms/student/controller/SchoolController.java

#### auth-service - Teacher-School Association

- [ ] T031 [P] [US1] Create ITeacherSchoolService interface in auth-service/src/main/java/com/sms/auth/service/interfaces/ITeacherSchoolService.java
- [ ] T032 [US1] Implement TeacherSchoolService with createOrUpdate and get methods in auth-service/src/main/java/com/sms/auth/service/TeacherSchoolService.java
- [ ] T033 [US1] Add cross-service validation to check school_id exists via HTTP call to student-service in TeacherSchoolService
- [ ] T034 [P] [US1] Create TeacherSchoolController with POST /api/teacher-school endpoint in auth-service/src/main/java/com/sms/auth/controller/TeacherSchoolController.java
- [ ] T035 [P] [US1] Add GET /api/teacher-school endpoint to TeacherSchoolController in auth-service/src/main/java/com/sms/auth/controller/TeacherSchoolController.java

### Frontend Implementation for User Story 1

- [ ] T036 [P] [US1] Create Zod validation schema for teacher-school in frontend/src/lib/validations/school-setup.ts
- [ ] T037 [P] [US1] Create API service functions for provinces, districts, schools in frontend/src/services/location.ts
- [ ] T038 [P] [US1] Create API service functions for teacher-school association in frontend/src/services/school.ts
- [ ] T039 [P] [US1] Create Zustand store for school setup state in frontend/src/store/school-setup-store.ts
- [ ] T040 [P] [US1] Create ProvinceSelector component in frontend/src/features/school-setup/components/province-selector.tsx
- [ ] T041 [P] [US1] Create DistrictSelector component in frontend/src/features/school-setup/components/district-selector.tsx
- [ ] T042 [P] [US1] Create SchoolTable component with ClientDataTable in frontend/src/features/school-setup/components/school-table.tsx
- [ ] T043 [US1] Create school setup page with cascading selectors in frontend/src/features/school-setup/index.tsx
- [ ] T044 [US1] Add school setup route in frontend/src/routes/_authenticated/school-setup.tsx
- [ ] T045 [US1] Implement TanStack Router beforeLoad guard to check teacher-school association in frontend/src/routes/_authenticated.tsx
- [ ] T046 [US1] Add i18n translation keys for location selection UI in frontend/src/lib/i18n/locales/en.json and km.json

### Integration & Logging for User Story 1

- [ ] T047 [US1] Add logging for school setup completion events in TeacherSchoolService
- [ ] T048 [US1] Test complete flow: register → redirect to school setup → select province/district/school → verify association → redirect to main app

**Checkpoint**: At this point, User Story 1 should be fully functional - teachers can select existing schools and complete setup independently

---

## Phase 4: User Story 2 - Add New School (Priority: P2)

**Goal**: Enable teachers to add a new school under selected province/district when their school doesn't exist in database.

**Independent Test**: Register a new teacher, navigate to Province → District, confirm no matching school, click "Add New School", submit valid school form, verify new school appears in table and can be selected.

### Backend Implementation for User Story 2

#### student-service - School Creation

- [ ] T049 [US2] Add createSchool method to ISchoolService interface in student-service/src/main/java/com/sms/student/service/interfaces/ISchoolService.java
- [ ] T050 [US2] Implement createSchool with validation (province/district exist, unique name per district) in SchoolService in student-service/src/main/java/com/sms/student/service/SchoolService.java
- [ ] T051 [US2] Add POST /api/schools endpoint to SchoolController in student-service/src/main/java/com/sms/student/controller/SchoolController.java
- [ ] T052 [US2] Add GlobalExceptionHandler mapping for DUPLICATE_SCHOOL_NAME error in student-service/src/main/java/com/sms/student/exception/GlobalExceptionHandler.java

### Frontend Implementation for User Story 2

- [ ] T053 [P] [US2] Create Zod validation schema for school creation form in frontend/src/lib/validations/school-setup.ts
- [ ] T054 [P] [US2] Add createSchool API service function in frontend/src/services/location.ts
- [ ] T055 [US2] Create AddSchoolModal component with react-hook-form in frontend/src/features/school-setup/components/add-school-modal.tsx
- [ ] T056 [US2] Integrate AddSchoolModal into school setup page with "Add New School" button in frontend/src/features/school-setup/index.tsx
- [ ] T057 [US2] Implement form submission, error handling, and table refresh after school creation
- [ ] T058 [US2] Add i18n translation keys for school creation form and error messages in frontend/src/lib/i18n/locales/en.json and km.json

### Integration & Logging for User Story 2

- [ ] T059 [US2] Add logging for new school creation events in SchoolService
- [ ] T060 [US2] Test flow: school setup → district with no matching school → add new school → verify school appears → select and complete association

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently - teachers can select OR add schools

---

## Phase 5: User Story 3 - Edit Selection Before Confirmation (Priority: P3)

**Goal**: Allow teachers to change province/district selections before final confirmation with appropriate UI state resets.

**Independent Test**: Start school setup, select province, change to different province, verify district dropdown resets; select district, change district, verify schools table refreshes.

### Frontend Implementation for User Story 3

- [ ] T061 [US3] Add province change handler to reset district and schools state in Zustand store in frontend/src/store/school-setup-store.ts
- [ ] T062 [US3] Add district change handler to reset schools state in Zustand store
- [ ] T063 [US3] Implement useEffect hooks in school setup page to handle selection resets in frontend/src/features/school-setup/index.tsx
- [ ] T064 [US3] Add visual feedback (loading states, empty states) for dropdown resets in ProvinceSelector and DistrictSelector components
- [ ] T065 [US3] Test edge cases: rapid province switching, browser back button, refresh during selection

**Checkpoint**: All user stories (US1, US2, US3) should now be independently functional - complete selection flow with editing support

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements, error handling, and validation across all user stories

### Backend Enhancements

- [ ] T066 [P] Add OpenAPI/Swagger documentation annotations to all new controllers in auth-service and student-service
- [ ] T067 [P] Add unit tests for ProvinceService, DistrictService, SchoolService in student-service/src/test/java/com/sms/student/service/ (OPTIONAL)
- [ ] T068 [P] Add unit tests for TeacherSchoolService in auth-service/src/test/java/com/sms/auth/service/ (OPTIONAL)
- [ ] T069 [P] Add integration tests for location endpoints (GET provinces, districts, schools) in student-service/src/test/java/com/sms/student/controller/ (OPTIONAL)
- [ ] T070 [P] Add integration tests for teacher-school endpoints in auth-service/src/test/java/com/sms/auth/controller/ (OPTIONAL)
- [ ] T071 Add GlobalExceptionHandler improvements for all new error codes in both services

### Frontend Enhancements

- [ ] T072 [P] Add component tests for ProvinceSelector, DistrictSelector, SchoolTable in frontend/src/features/school-setup/ (OPTIONAL)
- [ ] T073 [P] Add integration test for complete school setup flow using Vitest + React Testing Library (OPTIONAL)
- [ ] T074 Add error boundary for school setup feature in frontend/src/features/school-setup/
- [ ] T075 Implement toast notifications for success/error states (school creation, association)
- [ ] T076 Add loading skeletons for dropdown and table components
- [ ] T077 [P] Add accessibility improvements (ARIA labels, keyboard navigation) to school setup components

### Edge Case Handling

- [ ] T078 Handle empty states: province with no districts, district with no schools
- [ ] T079 Add validation for duplicate school names with user-friendly error messages
- [ ] T080 Implement session persistence: restore school setup state on page refresh
- [ ] T081 Add rate limiting protection for POST /api/schools endpoint (10 req/min)
- [ ] T082 Test cross-browser compatibility (Chrome, Firefox, Safari, Edge)

### Documentation & Deployment

- [ ] T083 [P] Update quickstart.md with database seed script for test provinces/districts
- [ ] T084 [P] Add API endpoint documentation to project README
- [ ] T085 [P] Create migration rollback procedures documentation
- [ ] T086 Verify all Flyway migrations run successfully in Docker environment
- [ ] T087 Run complete quickstart.md validation workflow
- [ ] T088 Performance testing: verify API response times < 500ms p95

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
  - Creates database migrations only
- **Foundational (Phase 2)**: Depends on Phase 1 (migrations must exist) - BLOCKS all user stories
  - Creates all entities, repositories, DTOs
- **User Stories (Phase 3-5)**: All depend on Foundational phase completion
  - User Story 1 (P1): Can start after Foundational - No dependencies on other stories
  - User Story 2 (P2): Can start after Foundational - Extends US1 but independently testable
  - User Story 3 (P3): Can start after Foundational - Enhances US1/US2 but independently testable
- **Polish (Phase 6)**: Depends on all user stories being complete

### User Story Dependencies

```
Foundational (Phase 2)
       ↓
   ┌───┴───┬───────┐
   ↓       ↓       ↓
  US1     US2     US3  (All can run in parallel after foundation)
  (P1)    (P2)    (P3)
```

- **User Story 1 (P1)**: Foundational only - fully independent
- **User Story 2 (P2)**: Foundational only - extends US1 but can be tested independently
- **User Story 3 (P3)**: Foundational only - enhances US1/US2 but can be tested independently

### Within Each User Story

**User Story 1 flow**:
1. Backend services (T022-T027) → Backend controllers (T028-T035) - can run in parallel
2. Frontend services/store (T036-T039) → Frontend components (T040-T042) → Page integration (T043-T046)
3. Integration & logging (T047-T048)

**User Story 2 flow**:
1. Backend service/controller (T049-T052)
2. Frontend validation/service (T053-T054) → Frontend components (T055-T058)
3. Integration & logging (T059-T060)

**User Story 3 flow**:
1. State management updates (T061-T062)
2. UI integration (T063-T064)
3. Edge case testing (T065)

### Parallel Opportunities

**Phase 1 (Setup)**: All migration files can be created in parallel (T001-T004)

**Phase 2 (Foundational)**: High parallelism
- All student-service entities (T006-T008) in parallel
- All student-service repositories (T009-T011) in parallel
- All auth-service entities/repositories (T013-T014) in parallel
- All error code additions (T012, T015) in parallel
- All DTOs (T016-T021) in parallel

**User Story 1 (MVP)**: Moderate parallelism
- Backend: Services (T022-T027) can run in parallel, then controllers (T028-T035)
- Frontend: Services/store (T036-T039) can run in parallel, then components (T040-T042)

**User Story 2**: Low parallelism (mostly sequential)
- Frontend validation/service (T053-T054) can run in parallel
- Other tasks depend on previous completions

**User Story 3**: Low parallelism (mostly sequential)

**Phase 6 (Polish)**: High parallelism
- All unit/integration tests (T067-T070, T072-T073) can run in parallel
- All documentation tasks (T083-T085) can run in parallel
- Most enhancements can run in parallel

---

## Parallel Example: User Story 1 (MVP)

```bash
# Parallel Group 1: Backend Services
Task: "Create IProvinceService interface in student-service/src/main/java/com/sms/student/service/interfaces/IProvinceService.java"
Task: "Implement ProvinceService in student-service/src/main/java/com/sms/student/service/ProvinceService.java"
Task: "Create IDistrictService interface in student-service/src/main/java/com/sms/student/service/interfaces/IDistrictService.java"
Task: "Implement DistrictService in student-service/src/main/java/com/sms/student/service/DistrictService.java"

# Parallel Group 2: Backend Controllers (after Group 1)
Task: "Create ProvinceController with GET /api/provinces endpoint in student-service/src/main/java/com/sms/student/controller/ProvinceController.java"
Task: "Create DistrictController with GET /api/districts endpoint in student-service/src/main/java/com/sms/student/controller/DistrictController.java"
Task: "Create TeacherSchoolController with POST /api/teacher-school endpoint in auth-service/src/main/java/com/sms/auth/controller/TeacherSchoolController.java"

# Parallel Group 3: Frontend Services/Store
Task: "Create Zod validation schema for teacher-school in frontend/src/lib/validations/school-setup.ts"
Task: "Create API service functions for provinces, districts, schools in frontend/src/services/location.ts"
Task: "Create API service functions for teacher-school association in frontend/src/services/school.ts"
Task: "Create Zustand store for school setup state in frontend/src/store/school-setup-store.ts"

# Parallel Group 4: Frontend Components
Task: "Create ProvinceSelector component in frontend/src/features/school-setup/components/province-selector.tsx"
Task: "Create DistrictSelector component in frontend/src/features/school-setup/components/district-selector.tsx"
Task: "Create SchoolTable component with ClientDataTable in frontend/src/features/school-setup/components/school-table.tsx"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

**Target**: Teachers can select existing schools and complete registration

1. ✅ Complete Phase 1: Setup (migrations) - ~1 hour
2. ✅ Complete Phase 2: Foundational (entities, repos, DTOs) - ~3 hours
3. ✅ Complete Phase 3: User Story 1 (location APIs + teacher-school association + frontend) - ~8 hours
4. **STOP and VALIDATE**: Test User Story 1 independently
   - Register new teacher → Redirect to setup → Select province/district/school → Verify association
   - Check database: teacher_school record exists
   - Check redirect: main app accessible after setup
5. ✅ Deploy/demo if ready

**Total MVP Time**: ~12 hours (assuming single developer, sequential execution)

### Incremental Delivery

1. **Foundation Release** (Phase 1 + 2): Database ready, APIs stubbed → ~4 hours
2. **MVP Release** (+ Phase 3): User Story 1 working → Test independently → Deploy (~12 hours total)
3. **Enhancement 1** (+ Phase 4): User Story 2 (add new school) → Test independently → Deploy (~16 hours total)
4. **Enhancement 2** (+ Phase 5): User Story 3 (edit selections) → Test independently → Deploy (~18 hours total)
5. **Production Ready** (+ Phase 6): Polish, tests, documentation → Final deploy (~24 hours total)

Each release adds value without breaking previous functionality.

### Parallel Team Strategy

With 3 developers working simultaneously:

**Phase 1-2: Foundation (4 hours)**
- All developers work together on setup and foundational tasks

**Phase 3-5: User Stories (6 hours)**
- Developer A: User Story 1 (MVP) - Full stack
- Developer B: User Story 2 (Add School) - Full stack
- Developer C: User Story 3 (Edit Selections) - Full stack
- Stories complete and integrate independently

**Phase 6: Polish (2 hours)**
- Developer A: Backend tests + documentation
- Developer B: Frontend tests + accessibility
- Developer C: Edge cases + deployment validation

**Total Team Time**: ~12 hours (50% time savings with 3 developers)

---

## Task Summary

**Total Tasks**: 88 tasks

**Tasks per User Story**:
- Setup (Phase 1): 5 tasks (migrations)
- Foundational (Phase 2): 16 tasks (entities, repos, DTOs)
- User Story 1 (P1 - MVP): 27 tasks (backend + frontend + integration)
- User Story 2 (P2): 12 tasks (school creation)
- User Story 3 (P3): 5 tasks (selection editing)
- Polish (Phase 6): 23 tasks (tests, docs, enhancements)

**Parallel Opportunities**:
- Phase 1: 4 parallel tasks (migration files)
- Phase 2: 21 parallel tasks (all entities, repos, DTOs can be created simultaneously)
- User Story 1: 12 parallel tasks (services, controllers, components)
- User Story 2: 4 parallel tasks (validation, service, components)
- Phase 6: 18 parallel tasks (tests, documentation)

**MVP Scope** (Recommended for first release):
- Phase 1: Setup (5 tasks)
- Phase 2: Foundational (16 tasks)
- Phase 3: User Story 1 only (27 tasks)
- **Total MVP**: 48 tasks (~12-16 hours single developer)

**Format Validation**: ✅ All tasks follow checklist format with:
- Checkbox prefix `- [ ]`
- Sequential Task ID (T001-T088)
- [P] marker for parallelizable tasks
- [Story] label for user story tasks (US1, US2, US3)
- Clear descriptions with exact file paths

---

## Notes

- **[P] tasks**: Different files, no dependencies - can execute in parallel
- **[Story] label**: Maps task to specific user story for traceability and independent testing
- **Each user story** should be independently completable and testable
- **Commit strategy**: Commit after each task or logical group (e.g., all DTOs)
- **Stop at checkpoints**: Validate each story independently before moving to next priority
- **Avoid**: Vague tasks, same file conflicts, cross-story dependencies that break independence
- **Tests are OPTIONAL**: Only execute test tasks (T067-T070, T072-T073) if TDD approach requested
- **Database migrations**: Run Phase 1 migrations first, verify with `./mvnw flyway:info` before proceeding
