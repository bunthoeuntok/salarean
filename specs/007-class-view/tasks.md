# Tasks: Class Detail View

**Input**: Design documents from `/specs/007-class-view/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/get-class-students.md, quickstart.md

**Tests**: Not explicitly requested in specification - test tasks omitted.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Backend**: `student-service/src/main/java/com/sms/student/`
- **Frontend**: `frontend/src/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Verify project structure and dependencies are in place

- [x] T001 Verify TanStack Table v8 is installed in frontend/package.json (add if missing: `pnpm add @tanstack/react-table`)
- [x] T002 [P] Verify shadcn/ui Tabs component is available (add if missing: `pnpm dlx shadcn@latest add tabs`)
- [x] T003 [P] Generate TanStack Router types with `pnpm exec tsr generate` in frontend/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Backend API and frontend types that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Backend API Implementation

- [x] T004 [P] Create StudentEnrollmentItem DTO in student-service/src/main/java/com/sms/student/dto/StudentEnrollmentItem.java
- [x] T005 [P] Create StudentEnrollmentListResponse DTO in student-service/src/main/java/com/sms/student/dto/StudentEnrollmentListResponse.java
- [x] T006 Add findByClassId query method to StudentClassEnrollmentRepository in student-service/src/main/java/com/sms/student/repository/StudentClassEnrollmentRepository.java
- [x] T007 Add getStudentsByClass method signature to IClassService interface in student-service/src/main/java/com/sms/student/service/interfaces/IClassService.java
- [x] T008 Implement getStudentsByClass method in ClassService in student-service/src/main/java/com/sms/student/service/ClassService.java
- [x] T009 Add GET /{classId}/students endpoint to ClassController in student-service/src/main/java/com/sms/student/controller/ClassController.java
- [x] T010 Rebuild student-service with `./mvnw clean compile` and restart Docker container

### Frontend Types and API Service

- [x] T011 [P] Add StudentEnrollmentListResponse and StudentEnrollmentItem interfaces to frontend/src/types/class.types.ts
- [x] T012 [P] Add StudentFilters interface to frontend/src/types/class.types.ts
- [x] T013 Add getClassStudents function to frontend/src/services/class.service.ts
- [x] T014 Create useClassStudents TanStack Query hook in frontend/src/hooks/useClassStudents.ts

**Checkpoint**: Foundation ready - API endpoint working, frontend can fetch data. User story implementation can now begin.

---

## Phase 3: User Story 1 - View Students in Class (Priority: P1) 🎯 MVP

**Goal**: Teachers can navigate from class list to class detail page and see all enrolled students with their names, codes, and enrollment status.

**Independent Test**: Navigate to `/classes/{id}`, verify student list displays with correct data. Test with class that has 0 students (empty state) and class with multiple students.

### Implementation for User Story 1

- [x] T015 Create route file frontend/src/routes/_authenticated/classes.$id.tsx with basic route definition
- [x] T016 Create class detail page component in frontend/src/features/classes/class-detail/index.tsx with basic structure
- [x] T017 [P] [US1] Create ClassHeader component in frontend/src/features/classes/class-detail/components/class-header.tsx displaying class name, code, grade level, academic year
- [x] T018 [P] [US1] Create StudentListItem component in frontend/src/features/classes/class-detail/components/student-list-item.tsx with photo, name, code, enrollment date, status badge
- [x] T019 [US1] Create StudentList component in frontend/src/features/classes/class-detail/components/student-list.tsx using semantic HTML table with students from useClassStudents hook
- [x] T020 [US1] Create EmptyState component in frontend/src/features/classes/class-detail/components/empty-state.tsx for "No students enrolled in this class" message
- [x] T021 [US1] Add back navigation (breadcrumb or back button) to class detail page in frontend/src/features/classes/class-detail/index.tsx
- [x] T022 [US1] Connect ClassHeader to class detail page, fetching class info (if separate endpoint needed)
- [x] T023 [US1] Integrate StudentList with useClassStudents hook, display loading skeleton while fetching

**Checkpoint**: At this point, User Story 1 should be fully functional - teachers can view class roster from class list.

---

## Phase 4: User Story 2 - Navigate Between Class Information Tabs (Priority: P2)

**Goal**: Establish tab navigation UI with Students tab active by default, and Coming Soon placeholders for future tabs (Schedule, Attendance, Grades).

**Independent Test**: Verify tabs are visible, Students tab shows student list, clicking other tabs shows "Coming Soon" message. Refresh page and verify tab state persists via URL.

### Implementation for User Story 2

- [ ] T024 [P] [US2] Create ComingSoonTab placeholder component in frontend/src/features/classes/components/coming-soon-tab.tsx
- [ ] T025 [US2] Add Zod search params schema for tab routing (tab: students|schedule|attendance|grades) to route file frontend/src/routes/_authenticated/classes.$id.tsx
- [ ] T026 [US2] Implement shadcn/ui Tabs structure in frontend/src/features/classes/[id].tsx with 4 tabs (Students, Schedule, Attendance, Grades)
- [ ] T027 [US2] Connect tab selection to URL search params using Route.useSearch() and Route.useNavigate() in frontend/src/features/classes/[id].tsx
- [ ] T028 [US2] Wrap StudentsTab content with React.lazy and Suspense for lazy loading in frontend/src/features/classes/[id].tsx
- [ ] T029 [US2] Create StudentsTab wrapper component in frontend/src/features/classes/components/students-tab.tsx that contains StudentList and related components
- [ ] T030 [US2] Add ARIA attributes to tabs (role="tablist", role="tab", role="tabpanel", aria-selected, aria-controls) in frontend/src/features/classes/[id].tsx

**Checkpoint**: At this point, User Stories 1 AND 2 should both work - tab navigation with URL persistence.

---

## Phase 5: User Story 3 - Filter and Search Students in Class (Priority: P3)

**Goal**: Teachers can search students by name/code in real-time (300ms debounce) and filter by enrollment status.

**Independent Test**: Type in search box, verify list filters after 300ms. Select status filter, verify only matching students shown. Clear filters, verify all students return.

### Implementation for User Story 3

- [ ] T031 [P] [US3] Create useDebouncedValue hook in frontend/src/hooks/use-debounce.ts with configurable delay (default 300ms)
- [ ] T032 [P] [US3] Create Zod validation schema for student filters in frontend/src/lib/validations/class-filters.ts
- [ ] T033 [P] [US3] Create StudentSearch component with search input in frontend/src/features/classes/components/student-search.tsx
- [ ] T034 [P] [US3] Create StatusFilter dropdown component in frontend/src/features/classes/components/status-filter.tsx with options (All, Active, Transferred, Graduated, Withdrawn)
- [ ] T035 [US3] Integrate TanStack Table in StudentList with globalFilter for client-side search filtering in frontend/src/features/classes/components/student-list.tsx
- [ ] T036 [US3] Connect search input to debouncedSearch state and TanStack Table globalFilter in frontend/src/features/classes/components/students-tab.tsx
- [ ] T037 [US3] Connect status filter to useClassStudents hook (server-side filtering) in frontend/src/features/classes/components/students-tab.tsx
- [ ] T038 [US3] Add aria-live region for search results count announcement in frontend/src/features/classes/components/student-search.tsx

**Checkpoint**: All user stories should now be independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Accessibility, error handling, and responsive design improvements

### Accessibility (WCAG 2.1 Level AA)

- [ ] T039 [P] Add keyboard navigation (arrow keys) for tab switching in frontend/src/features/classes/[id].tsx
- [ ] T040 [P] Add visible focus indicators to all interactive elements (tabs, search, filters, buttons) via CSS in frontend/src/features/classes/
- [ ] T041 [P] Add semantic table with caption element ("Students enrolled in {className}") in frontend/src/features/classes/components/student-list.tsx
- [ ] T042 [P] Add aria-label to search input ("Search students by name or code") in frontend/src/features/classes/components/student-search.tsx
- [ ] T043 Verify color contrast ratios (4.5:1 for text, 3:1 for UI components) in all components

### Error Handling

- [ ] T044 [P] Add error boundary for class detail page in frontend/src/features/classes/[id].tsx
- [ ] T045 [P] Handle CLASS_NOT_FOUND error - redirect to class list with error message in frontend/src/features/classes/[id].tsx
- [ ] T046 [P] Add error state component for API failures in frontend/src/features/classes/components/error-state.tsx

### Responsive Design

- [ ] T047 [P] Add mobile-responsive styles for tab navigation (stack or horizontal scroll on small screens) in frontend/src/features/classes/
- [ ] T048 [P] Add mobile-responsive styles for student table (horizontal scroll or card layout) in frontend/src/features/classes/components/student-list.tsx

### Final Validation

- [ ] T049 Run frontend build `pnpm build` and fix any TypeScript errors
- [ ] T050 Run axe DevTools accessibility check on class detail page
- [ ] T051 Manual testing on mobile device (tablet and phone screen sizes)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Requires basic page structure from US1 (T015, T016)
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - Requires StudentList component from US1 (T019)

### Within Each User Story

- Models/DTOs before services
- Services before endpoints/components
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks T004, T005 (DTOs) can run in parallel
- Within US1: T017 and T018 can run in parallel (different components)
- Within US2: T024 can run in parallel with US1 work
- Within US3: T031, T032, T033, T034 can all run in parallel (different files)
- Polish tasks marked [P] can run in parallel

---

## Parallel Example: User Story 3

```bash
# Launch all independent tasks for User Story 3 together:
Task: "Create useDebouncedValue hook in frontend/src/hooks/use-debounce.ts"
Task: "Create Zod validation schema in frontend/src/lib/validations/class-filters.ts"
Task: "Create StudentSearch component in frontend/src/features/classes/components/student-search.tsx"
Task: "Create StatusFilter dropdown in frontend/src/features/classes/components/status-filter.tsx"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready - Teachers can view class rosters!

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo (Tab navigation)
4. Add User Story 3 → Test independently → Deploy/Demo (Search/filter)
5. Add Polish → Test accessibility → Final release
6. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (backend verified, frontend components)
   - Developer B: User Story 2 (can start after T015, T016)
   - Developer C: User Story 3 (can start parallel components)
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- No pagination needed - all students displayed in scrollable list
- Search filtering is client-side (TanStack Table globalFilter, 300ms debounce)
- Status filtering is server-side (API query parameter)
