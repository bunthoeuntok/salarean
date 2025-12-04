# Tasks: Batch Student Transfer with Undo

**Input**: Design documents from `/specs/008-batch-student-transfer/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: This feature includes comprehensive testing. All test tasks are marked with test types.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions

- **Backend**: `student-service/src/main/java/com/sms/student/` (Java)
- **Frontend**: `frontend/src/` (TypeScript/React)
- **Tests**: `student-service/src/test/java/com/sms/student/` (Backend), `frontend/src/` (Frontend)
- **Database**: `student-service/src/main/resources/db/migration/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and database schema changes

- [x] T001 Create Flyway migration script V8__add_transfer_undo_support.sql in student-service/src/main/resources/db/migration/
- [x] T002 [P] Add transfer-related DTOs to backend in student-service/src/main/java/com/sms/student/dto/
- [x] T003 [P] Create frontend transfer types in frontend/src/types/transfer.ts
- [x] T004 [P] Add transfer error codes to StudentErrorCode enum in student-service/src/main/java/com/sms/student/dto/StudentErrorCode.java

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T005 Update EnrollmentHistory entity with transfer columns in student-service/src/main/java/com/sms/student/model/EnrollmentHistory.java
- [ ] T006 Add transfer query methods to EnrollmentHistoryRepository in student-service/src/main/java/com/sms/student/repository/EnrollmentHistoryRepository.java
- [ ] T007 Create IStudentTransferService interface in student-service/src/main/java/com/sms/student/service/interfaces/IStudentTransferService.java
- [ ] T008 Implement StudentTransferService skeleton in student-service/src/main/java/com/sms/student/service/StudentTransferService.java
- [ ] T009 [P] Create undo Zustand store in frontend/src/store/undo-store.ts
- [ ] T010 [P] Create useCountdown hook in frontend/src/hooks/use-countdown.ts
- [ ] T011 Run database migration and verify schema changes with quickstart.md step 1

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Select Multiple Students for Transfer (Priority: P1) 🎯 MVP

**Goal**: Enable users to select multiple students via checkboxes and see a floating action button with selection count

**Independent Test**: Display checkboxes in class student list, select multiple students, verify floating action button appears with correct count and disappears when all unchecked

### Implementation for User Story 1

- [ ] T012 [P] [US1] Add checkbox column to student table columns in frontend/src/features/classes/columns.tsx
- [ ] T013 [P] [US1] Create StudentSelectionStore in frontend/src/features/classes/store/selection-store.ts
- [ ] T014 [US1] Update class detail page to integrate selection state in frontend/src/routes/_authenticated/classes/$classId.tsx
- [ ] T015 [US1] Create FloatingActionButton component in frontend/src/features/classes/components/floating-action-button.tsx
- [ ] T016 [US1] Integrate FloatingActionButton with selection state in class detail page
- [ ] T017 [P] [US1] Write component test for FloatingActionButton in frontend/src/features/classes/components/floating-action-button.test.tsx
- [ ] T018 [P] [US1] Add i18n translations for selection UI in frontend/src/lib/i18n/locales/en.json and km.json

**Checkpoint**: At this point, User Story 1 should be fully functional - users can select students and see floating button

---

## Phase 4: User Story 2 - Confirm Transfer Destination (Priority: P1)

**Goal**: Display transfer confirmation dialog with selected students list and destination class dropdown

**Independent Test**: Click floating action button, verify dialog opens with selected students, select destination class from dropdown showing only same-grade active classes, verify "Confirm Transfer" button state

### Implementation for User Story 2

- [ ] T019 [P] [US2] Create EligibleClassResponse DTO in student-service/src/main/java/com/sms/student/dto/EligibleClassResponse.java
- [ ] T020 [US2] Implement getEligibleDestinations method in StudentTransferService
- [ ] T021 [US2] Add getEligibleDestinations endpoint to ClassStudentController in student-service/src/main/java/com/sms/student/controller/ClassStudentController.java
- [ ] T022 [P] [US2] Create BatchTransferDialog component in frontend/src/features/classes/components/batch-transfer-dialog.tsx
- [ ] T023 [P] [US2] Add getEligibleDestinations API call to class service in frontend/src/services/class.ts
- [ ] T024 [US2] Integrate dialog with FloatingActionButton click handler
- [ ] T025 [P] [US2] Write unit test for getEligibleDestinations in student-service/src/test/java/com/sms/student/service/StudentTransferServiceTest.java
- [ ] T026 [P] [US2] Write component test for BatchTransferDialog in frontend/src/features/classes/components/batch-transfer-dialog.test.tsx
- [ ] T027 [P] [US2] Add i18n translations for transfer dialog in frontend/src/lib/i18n/locales/en.json and km.json

**Checkpoint**: At this point, User Stories 1 AND 2 should both work - users can select students, open dialog, and see eligible destination classes

---

## Phase 5: User Story 3 - Execute Batch Transfer (Priority: P1)

**Goal**: Transfer all selected students to destination class in a single operation with validation and success feedback

**Independent Test**: Complete transfer flow (select students, choose destination, confirm), verify students removed from source class, added to destination class, enrollment history updated, success toast appears

### Tests for User Story 3

- [ ] T028 [P] [US3] Write unit test for batch transfer validation in student-service/src/test/java/com/sms/student/service/StudentTransferServiceTest.java
- [ ] T029 [P] [US3] Write integration test for batch transfer endpoint in student-service/src/test/java/com/sms/student/controller/ClassStudentControllerTest.java
- [ ] T030 [P] [US3] Write contract test for batch transfer API in student-service/src/test/java/com/sms/student/integration/BatchTransferIntegrationTest.java

### Implementation for User Story 3

- [ ] T031 [P] [US3] Create BatchTransferRequest DTO in student-service/src/main/java/com/sms/student/dto/BatchTransferRequest.java
- [ ] T032 [P] [US3] Create BatchTransferResponse DTO in student-service/src/main/java/com/sms/student/dto/BatchTransferResponse.java
- [ ] T033 [US3] Implement batchTransfer method in StudentTransferService with full validation logic
- [ ] T034 [US3] Add batchTransferStudents endpoint to ClassStudentController
- [ ] T035 [US3] Add error handling to GlobalExceptionHandler for transfer errors in student-service/src/main/java/com/sms/student/exception/GlobalExceptionHandler.java
- [ ] T036 [P] [US3] Add batchTransfer API call to class service in frontend/src/services/class.ts
- [ ] T037 [US3] Implement transfer execution in BatchTransferDialog component
- [ ] T038 [US3] Add TanStack Query cache invalidation for class students after transfer
- [ ] T039 [US3] Create success toast notification component in frontend/src/features/classes/components/transfer-success-toast.tsx
- [ ] T040 [US3] Integrate success toast with transfer completion
- [ ] T041 [P] [US3] Add i18n translations for transfer success and errors in frontend/src/lib/i18n/locales/en.json and km.json

**Checkpoint**: At this point, User Stories 1, 2, AND 3 should work - users can complete full transfer flow with validation and success feedback

---

## Phase 6: User Story 4 - Undo Batch Transfer (Priority: P1)

**Goal**: Allow users to reverse a batch transfer within 5 minutes via toast notification with countdown timer and undo button

**Independent Test**: Complete transfer, verify success toast appears with undo button and countdown timer, click undo within 5 minutes, verify students returned to source class, undo toast disappears after 5 minutes

### Tests for User Story 4

- [ ] T042 [P] [US4] Write unit test for undo validation (timestamp, conflicts) in student-service/src/test/java/com/sms/student/service/StudentTransferServiceTest.java
- [ ] T043 [P] [US4] Write integration test for undo endpoint in student-service/src/test/java/com/sms/student/controller/ClassStudentControllerTest.java
- [ ] T044 [P] [US4] Write test for undo conflict detection in student-service/src/test/java/com/sms/student/service/StudentTransferServiceTest.java

### Implementation for User Story 4

- [ ] T045 [P] [US4] Create UndoTransferResponse DTO in student-service/src/main/java/com/sms/student/dto/UndoTransferResponse.java
- [ ] T046 [US4] Implement undoTransfer method in StudentTransferService with conflict detection
- [ ] T047 [US4] Add undoTransfer endpoint to ClassStudentController
- [ ] T048 [P] [US4] Add undoTransfer API call to class service in frontend/src/services/class.ts
- [ ] T049 [P] [US4] Update success toast to include undo button and countdown timer in frontend/src/features/classes/components/transfer-success-toast.tsx
- [ ] T050 [US4] Implement undo state persistence in undo-store.ts
- [ ] T051 [US4] Integrate useCountdown hook with toast component
- [ ] T052 [US4] Implement undo button click handler with API call and cache invalidation
- [ ] T053 [US4] Add auto-dismiss logic for toast after 5 minutes
- [ ] T054 [US4] Add undo button disable logic while undo in progress
- [ ] T055 [P] [US4] Write component test for countdown timer in frontend/src/hooks/use-countdown.test.ts
- [ ] T056 [P] [US4] Write component test for undo toast with countdown in frontend/src/features/classes/components/transfer-success-toast.test.tsx
- [ ] T057 [P] [US4] Add i18n translations for undo UI and errors in frontend/src/lib/i18n/locales/en.json and km.json

**Checkpoint**: All P1 user stories should now be independently functional - full transfer and undo flow working

---

## Phase 7: User Story 5 - Handle Transfer Errors (Priority: P2)

**Goal**: Display clear, localized error messages for all transfer failure scenarios (validation errors, capacity, conflicts, partial success)

**Independent Test**: Trigger various error scenarios (capacity exceeded, grade mismatch, student already enrolled, undo conflict), verify appropriate error messages displayed in user's language

### Implementation for User Story 5

- [ ] T058 [P] [US5] Add detailed error messages for all transfer error codes in frontend/src/lib/i18n/locales/en.json
- [ ] T059 [P] [US5] Add Khmer translations for all transfer errors in frontend/src/lib/i18n/locales/km.json
- [ ] T060 [US5] Implement error code mapping in BatchTransferDialog component
- [ ] T061 [US5] Add partial success handling to show which students failed and why
- [ ] T062 [US5] Implement error toast for transfer failures
- [ ] T063 [US5] Add error handling for undo failures (expired, conflict, unauthorized)
- [ ] T064 [P] [US5] Write integration test for partial success scenario in student-service/src/test/java/com/sms/student/integration/BatchTransferIntegrationTest.java
- [ ] T065 [P] [US5] Write integration test for error handling in frontend integration tests

**Checkpoint**: All user stories (P1 and P2) should now be complete with comprehensive error handling

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T066 [P] Update Swagger/OpenAPI documentation for new endpoints in student-service
- [ ] T067 [P] Add API examples to Postman collection for transfer and undo endpoints
- [ ] T068 [P] Verify all logging statements are in place for transfer operations
- [ ] T069 [P] Performance test: Transfer 20 students in <5 seconds
- [ ] T070 [P] Performance test: Undo completes in <3 seconds
- [ ] T071 [P] Performance test: Toast appears in <500ms after transfer
- [ ] T072 [P] Accessibility test: Verify WCAG 2.1 Level AA compliance for transfer UI
- [ ] T073 [P] Mobile responsive test: Verify checkboxes and floating button work on tablet/phone
- [ ] T074 Run complete quickstart.md validation with another developer
- [ ] T075 [P] Code review: Verify all constitution principles followed
- [ ] T076 [P] Security review: Verify authorization checks and audit trail
- [ ] T077 Update project documentation if needed

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Foundational phase completion
  - User Story 1 (P1): Can start after Foundational - No dependencies on other stories
  - User Story 2 (P1): Can start after Foundational - Integrates with US1 but independently testable
  - User Story 3 (P1): Can start after Foundational - Integrates with US1 and US2
  - User Story 4 (P1): Depends on US3 completion (needs transfer to exist before undo)
  - User Story 5 (P2): Can start after Foundational - Enhances all other stories
- **Polish (Phase 8)**: Depends on all desired user stories being complete

### User Story Dependencies

**Critical Path** (must be sequential):
1. User Story 1 → User Story 2 → User Story 3 → User Story 4

**Reasoning**:
- US2 requires US1 (needs selected students from checkboxes)
- US3 requires US2 (needs destination class from dialog)
- US4 requires US3 (needs successful transfer to undo)
- US5 can be worked on in parallel with any other story (error handling is additive)

### Within Each User Story

- Tests (where present) can run in parallel before implementation
- Backend DTOs can be created in parallel
- Frontend components can be created in parallel
- Services depend on DTOs
- Controllers depend on services
- Frontend integration depends on backend endpoints
- Tests can be updated in parallel after implementation

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel (4 tasks)
- Foundational phase: T009, T010 can run in parallel after T005-T008
- Within each user story: All tasks marked [P] can run in parallel
- User Story 5 can be worked on in parallel with any other story once Foundational is complete

---

## Parallel Example: User Story 3

```bash
# Write all tests for User Story 3 together (after DTOs exist):
Task: "Write unit test for batch transfer validation in StudentTransferServiceTest"
Task: "Write integration test for batch transfer endpoint in ClassStudentControllerTest"
Task: "Write contract test for batch transfer API in BatchTransferIntegrationTest"

# Create all DTOs for User Story 3 together:
Task: "Create BatchTransferRequest DTO"
Task: "Create BatchTransferResponse DTO"

# Create frontend components in parallel (after API exists):
Task: "Add batchTransfer API call to class service"
Task: "Create success toast notification component"
Task: "Add i18n translations for transfer success and errors"
```

---

## Parallel Example: User Story 4

```bash
# Write all tests for User Story 4 together:
Task: "Write unit test for undo validation in StudentTransferServiceTest"
Task: "Write integration test for undo endpoint in ClassStudentControllerTest"
Task: "Write test for undo conflict detection in StudentTransferServiceTest"

# Create frontend components in parallel:
Task: "Add undoTransfer API call to class service"
Task: "Update success toast to include undo button"
Task: "Write component test for countdown timer"
Task: "Add i18n translations for undo UI and errors"
```

---

## Implementation Strategy

### MVP First (User Stories 1-3 Only)

**Minimum Viable Product**: Basic batch transfer without undo

1. Complete Phase 1: Setup (T001-T004)
2. Complete Phase 2: Foundational (T005-T011) - CRITICAL checkpoint
3. Complete Phase 3: User Story 1 (T012-T018) - Selection UI
4. Complete Phase 4: User Story 2 (T019-T027) - Confirmation dialog
5. Complete Phase 5: User Story 3 (T028-T041) - Execute transfer
6. **STOP and VALIDATE**: Test end-to-end transfer flow independently
7. Deploy/demo if ready

**Total MVP Tasks**: 41 tasks
**Estimated Time**: 2-3 days

### Full Feature (All P1 User Stories)

**Complete Feature**: Batch transfer with undo capability

1. Complete MVP (Phases 1-5)
2. Complete Phase 6: User Story 4 (T042-T057) - Undo functionality
3. **STOP and VALIDATE**: Test full transfer and undo flow
4. Deploy/demo if ready

**Total P1 Tasks**: 57 tasks
**Estimated Time**: 3-4 days

### Complete Feature (P1 + P2)

**Production-Ready**: Full feature with comprehensive error handling

1. Complete Full Feature (Phases 1-6)
2. Complete Phase 7: User Story 5 (T058-T065) - Error handling
3. Complete Phase 8: Polish (T066-T077) - Cross-cutting concerns
4. **FINAL VALIDATION**: Complete quickstart.md validation
5. Production deployment

**Total Tasks**: 77 tasks
**Estimated Time**: 4-5 days

### Parallel Team Strategy

With 2-3 developers:

**Phase 1-2 (Setup + Foundational)**: All developers work together (T001-T011)

**After Foundational completes**:
- **Developer A**: User Story 1 + 2 (Frontend UI) - T012-T027
- **Developer B**: User Story 3 (Backend transfer logic) - T028-T041
- **Developer C**: User Story 4 (Undo logic) - T042-T057 (can start after US3 backend done)

**Final Phase**: All developers work on User Story 5 + Polish together

**Estimated Time with Team**: 2-3 days

---

## Task Summary

**Total Tasks**: 77 tasks
- **Phase 1 (Setup)**: 4 tasks
- **Phase 2 (Foundational)**: 7 tasks (CRITICAL - blocks all stories)
- **Phase 3 (User Story 1 - Selection)**: 7 tasks
- **Phase 4 (User Story 2 - Confirmation)**: 9 tasks
- **Phase 5 (User Story 3 - Execute Transfer)**: 14 tasks
- **Phase 6 (User Story 4 - Undo)**: 13 tasks
- **Phase 7 (User Story 5 - Error Handling)**: 8 tasks
- **Phase 8 (Polish)**: 12 tasks

**Parallel Tasks**: 43 tasks marked [P] can run in parallel (56% of total)

**Test Tasks**: 15 tasks (unit, integration, contract, component tests)

**Independent Test Criteria**:
- **US1**: Checkboxes appear, selection count shows in floating button
- **US2**: Dialog opens with selected students, destination dropdown populated
- **US3**: Transfer executes, students moved, enrollment history updated
- **US4**: Undo button appears, countdown accurate, undo reverses transfer
- **US5**: Error messages clear and localized for all failure scenarios

**MVP Scope**: User Stories 1-3 (41 tasks) = Basic batch transfer without undo
**Full Feature Scope**: User Stories 1-4 (57 tasks) = Batch transfer with undo
**Production-Ready Scope**: All user stories + Polish (77 tasks) = Complete feature

---

## Notes

- [P] tasks = different files, no dependencies, can run in parallel
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Tests are written before implementation where marked
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Critical path: US1 → US2 → US3 → US4 (must be sequential)
- US5 can run in parallel with other stories (error handling is additive)
- All tasks follow project standards: ApiResponse<T>, error codes, i18n, constitution compliance
- File paths use exact project structure from plan.md
