# Implementation Plan: Batch Student Transfer with Undo

**Branch**: `008-batch-student-transfer` | **Date**: 2025-12-04 | **Spec**: [spec.md](./spec.md)

## Summary

This feature implements batch student transfer functionality with a 5-minute undo window. Teachers and administrators can select multiple students from a class roster, transfer them to another class with the same grade level in a single operation, and undo the transfer within 5 minutes if needed. The implementation extends the existing Student Service with new batch transfer endpoints, adds UI checkboxes and a floating action button to the class detail page, and implements session-based undo state management with a countdown timer toast notification.

**Key Technical Approach**:
- Backend: New REST endpoints in student-service following existing ApiResponse<T> pattern
- Frontend: Zustand store with session storage for undo state, shadcn/ui Toast for countdown
- Database: Extend enrollment_history table with transfer tracking columns
- Validation: Server-side checks for grade matching, capacity, conflict detection
- User Experience: ClientDataTableWithUrl for student list, Material Design floating action button

---

## Technical Context

**Language/Version**: TypeScript 5.x (frontend), Java 21 (backend)
**Primary Dependencies**: React 19, Vite 7.x, TanStack Router/Query, Zustand, shadcn/ui, Tailwind CSS 4.x (frontend); Spring Boot 3.5.7, Spring Data JPA, PostgreSQL driver, Flyway (backend)
**Storage**: PostgreSQL 15+ (student_db database, enrollment_history table extension)
**Testing**: Vitest + React Testing Library (frontend), JUnit 5 + MockMVC (backend)
**Target Platform**: Web application (Chrome/Firefox/Safari latest, mobile responsive)
**Project Type**: Web (microservices architecture with React SPA frontend)
**Performance Goals**: Transfer <5s for 20 students, Toast appears <500ms, Undo completes <3s
**Constraints**: 5-minute undo window, session-based undo only, same-grade transfers only, max 100 students per batch
**Scale/Scope**: Typical class size <100 students, support for concurrent transfers by multiple users

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Microservices-First ✅ PASS
- Feature implemented within student-service (appropriate service boundary)
- No cross-service dependencies introduced (self-contained)
- REST API follows existing patterns, no new infrastructure needed
- **Rationale**: All transfer logic belongs to student enrollment domain

### Security-First ✅ PASS
- All endpoints require JWT authentication (via API Gateway)
- Authorization check: User must have TRANSFER_STUDENTS permission
- Undo restricted to user who performed original transfer (user ID tracking)
- Audit trail maintained in enrollment_history (transfer_id, performed_by_user_id)
- No PII exposed in error messages (error codes only)
- **Rationale**: Follows existing RBAC pattern, no new security infrastructure required

### Simplicity (YAGNI) ✅ PASS
- Reuses existing enrollment_history table (3 new columns, not a new table)
- Session-based undo (no server-side persistence for 5-minute window)
- Uses established UI patterns (shadcn/ui Toast, ClientDataTableWithUrl)
- No premature optimization (batch size limited to 100 students)
- **Rationale**: Simplest solution that meets requirements without over-engineering

### Observability ✅ PASS
- Health endpoints already exist (/actuator/health)
- Structured logging via Spring Boot (transfer success/failure events)
- Metrics: Transfer count, undo count, failure reasons (trackable via logs)
- **Rationale**: Uses existing observability infrastructure, no new tools required

### Test Discipline ✅ PASS
- Unit tests required for: StudentTransferService, undo validation logic, conflict detection
- Integration tests required for: ClassStudentController endpoints, database transactions
- Contract tests required for: API request/response schemas
- **Rationale**: Critical business logic (transfers, undos) must be thoroughly tested

### Backend API Conventions ✅ PASS
- All endpoints return ApiResponse<T> with errorCode and data
- Error codes are UPPER_SNAKE_CASE enums (SUCCESS, UNDO_CONFLICT, CAPACITY_EXCEEDED)
- No human-readable messages in responses (i18n handled by frontend)
- HTTP status codes mapped appropriately (200, 400, 403, 404, 409)
- **Rationale**: Follows existing API standards, consistent with auth-service and student-service patterns

**Result**: All constitution principles satisfied. No violations requiring justification.

**Post-Phase 1 Re-Check**: ✅ PASS (no design changes introduced violations)

---

## Project Structure

### Documentation (this feature)

```text
specs/008-batch-student-transfer/
├── plan.md                    # This file (/speckit.plan command output)
├── spec.md                    # Feature specification (user stories, requirements)
├── research.md                # Phase 0 output (technical decisions)
├── data-model.md              # Phase 1 output (database schema, entities)
├── quickstart.md              # Phase 1 output (developer setup guide)
├── contracts/                 # Phase 1 output (API contracts)
│   └── api-endpoints.md       # REST endpoint specifications
├── checklists/                # Quality validation checklists
│   └── requirements.md        # Spec quality checklist (completed)
└── tasks.md                   # Phase 2 output (/speckit.tasks command - NOT created yet)
```

### Source Code (repository root)

**Backend** (`student-service/`):
```text
student-service/
├── src/
│   ├── main/
│   │   ├── java/com/sms/student/
│   │   │   ├── config/                      # Existing configuration
│   │   │   ├── controller/
│   │   │   │   ├── ClassStudentController.java  # NEW: Batch transfer endpoints
│   │   │   │   └── ... (existing controllers)
│   │   │   ├── dto/
│   │   │   │   ├── BatchTransferRequest.java    # NEW: Transfer request DTO
│   │   │   │   ├── BatchTransferResponse.java   # NEW: Transfer response DTO
│   │   │   │   ├── UndoTransferResponse.java    # NEW: Undo response DTO
│   │   │   │   ├── EligibleClassResponse.java   # NEW: Destination class DTO
│   │   │   │   └── ... (existing DTOs)
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java  # UPDATED: Add transfer error handling
│   │   │   │   └── ... (existing exceptions)
│   │   │   ├── model/
│   │   │   │   ├── EnrollmentHistory.java       # UPDATED: Add transfer columns
│   │   │   │   └── ... (existing entities)
│   │   │   ├── repository/
│   │   │   │   ├── EnrollmentHistoryRepository.java  # UPDATED: Add transfer queries
│   │   │   │   └── ... (existing repositories)
│   │   │   ├── service/
│   │   │   │   ├── interfaces/
│   │   │   │   │   ├── IStudentTransferService.java  # NEW: Transfer service interface
│   │   │   │   │   └── ... (existing interfaces)
│   │   │   │   ├── StudentTransferService.java       # NEW: Transfer service implementation
│   │   │   │   └── ... (existing services)
│   │   │   └── validation/                  # Existing validators
│   │   └── resources/
│   │       ├── application.yml              # Existing config
│   │       └── db/migration/
│   │           ├── V8__add_transfer_undo_support.sql  # NEW: Migration
│   │           └── ... (existing migrations)
│   └── test/
│       ├── java/com/sms/student/
│       │   ├── controller/
│       │   │   └── ClassStudentControllerTest.java         # NEW: Controller tests
│       │   ├── service/
│       │   │   └── StudentTransferServiceTest.java         # NEW: Service tests
│       │   └── integration/
│       │       └── BatchTransferIntegrationTest.java       # NEW: Integration tests
│       └── resources/
│           └── application-test.yml
```

**Frontend** (`frontend/`):
```text
frontend/src/
├── routes/
│   └── _authenticated/
│       └── classes/
│           └── $classId.tsx                # UPDATED: Add class detail route (if not exists)
├── features/classes/
│   ├── components/
│   │   ├── batch-transfer-dialog.tsx       # NEW: Transfer confirmation dialog
│   │   ├── undo-toast.tsx                  # NEW: Toast with countdown & undo button
│   │   └── ... (existing class components)
│   ├── columns.tsx                         # UPDATED: Add checkbox column
│   ├── index.tsx                           # UPDATED: Integrate batch transfer UI
│   └── types.ts                            # NEW: Transfer-specific types
├── components/
│   ├── ui/                                 # Existing shadcn/ui components
│   └── data-table/                         # Existing DataTable components
├── store/
│   ├── undo-store.ts                       # NEW: Zustand store for undo state
│   └── ... (existing stores)
├── services/
│   ├── class.ts                            # UPDATED: Add transfer/undo API calls
│   └── ... (existing services)
├── types/
│   ├── transfer.ts                         # NEW: Transfer API types
│   └── ... (existing types)
├── hooks/
│   ├── use-countdown.ts                    # NEW: Countdown timer hook
│   └── ... (existing hooks)
└── lib/i18n/locales/
    ├── en.json                             # UPDATED: Add transfer error messages
    └── km.json                             # UPDATED: Add Khmer translations
```

**Structure Decision**:
This is a web application following the project's monorepo microservices architecture. The implementation touches:
1. **Backend**: student-service only (no new services)
2. **Frontend**: React SPA with feature-based organization
3. **Database**: PostgreSQL student_db (schema extension, not a new database)

---

## Complexity Tracking

> **No violations - table left empty as per template instructions**

---

## Phase 0: Research & Technical Decisions

**Status**: ✅ COMPLETE

**Output**: `research.md` (10 technical decisions documented)

**Key Decisions Summary**:
1. **Undo State Management**: Zustand + session storage (not local storage, not server-side)
2. **Toast Notification**: shadcn/ui Toast with custom countdown hook
3. **API Design**: Batch transfer endpoint with partial success support
4. **Undo Endpoint**: POST /api/transfers/{id}/undo with conflict detection
5. **Enrollment History**: Extend existing table with transfer_id columns
6. **Cache Invalidation**: TanStack Query invalidateQueries on transfer/undo
7. **DataTable Selection**: ClientDataTableWithUrl per constitution standards
8. **Permission Model**: Reuse existing RBAC, track performed_by_user_id
9. **Floating Action Button**: Fixed position with conditional rendering
10. **Error Handling**: Centralized error code mapping with i18n

**Research Findings**:
- No new infrastructure dependencies required
- All patterns align with existing project standards
- Performance targets achievable with current architecture
- 5-minute undo window sufficient for most error recovery scenarios

---

## Phase 1: Design Artifacts

**Status**: ✅ COMPLETE

### 1. Data Model (`data-model.md`)

**Database Changes**:
- Added 3 columns to `enrollment_history` table:
  - `transfer_id UUID` (nullable, indexed)
  - `undo_of_transfer_id UUID` (nullable, indexed)
  - `performed_by_user_id UUID NOT NULL` (backfilled with system UUID)
- Added 3 indexes for efficient transfer/undo queries
- Flyway migration: `V8__add_transfer_undo_support.sql`

**Backend DTOs**:
- `BatchTransferRequest` (destinationClassId, studentIds[])
- `BatchTransferResponse` (transferId, successfulTransfers, failedTransfers[])
- `UndoTransferResponse` (transferId, undoneStudents, sourceClassId)
- `EligibleClassResponse` (id, name, gradeLevel, capacity, currentEnrollment)

**Frontend Types**:
- `UndoState` interface for Zustand store (transferId, expiresAt, studentIds)
- `TransferDialogState` for UI state management
- `StudentRow` for DataTable with checkbox selection

**Validation Rules**:
- 7 pre-transfer checks (student enrollment, capacity, grade match, duplicates)
- 5 pre-undo checks (timestamp, permissions, conflicts, source class exists)

### 2. API Contracts (`contracts/api-endpoints.md`)

**Endpoints Defined**:
1. `POST /api/classes/{classId}/students/batch-transfer` - Main transfer operation
2. `POST /api/transfers/{transferId}/undo` - Undo operation
3. `GET /api/classes/{classId}/eligible-destination-classes` - Destination dropdown

**Error Codes**: 15 total
- `SUCCESS`, `PARTIAL_SUCCESS` (200)
- `CAPACITY_EXCEEDED`, `GRADE_MISMATCH`, `ALREADY_ENROLLED` (400)
- `UNDO_EXPIRED`, `UNDO_CONFLICT` (409)
- `UNDO_UNAUTHORIZED` (403)
- `TRANSFER_NOT_FOUND`, `SOURCE_CLASS_NOT_FOUND` (404)

**Controller Pattern**:
- `ClassStudentController` with `@RestController` and `@RequiredArgsConstructor`
- `ApiResponse<T>` wrapper for all responses
- `@AuthenticationPrincipal JwtUser` for current user injection
- OpenAPI annotations for Swagger documentation

### 3. Developer Quickstart (`quickstart.md`)

**Setup Steps**:
1. Run Flyway migration (add transfer columns)
2. Verify backend service is running
3. Install frontend dependencies
4. Seed test data (2 classes, 5 students)
5. Manual testing guide (transfer, undo, expiration, conflict)
6. API testing with cURL examples
7. Verify enrollment history audit trail

**Troubleshooting**:
- Common errors and solutions documented
- Database verification queries provided
- Session storage debugging tips included

**Agent Context Update**: ✅ COMPLETE
- Updated `CLAUDE.md` with PostgreSQL student_db database
- Recent Changes section updated with feature 008

---

## Phase 2: Task Breakdown

**Status**: ⏳ PENDING

**Next Step**: Run `/speckit.tasks` to generate tasks.md

**Expected Task Categories**:
1. **Database Migration** (1-2 tasks)
   - Create Flyway migration script
   - Test migration on local database

2. **Backend Implementation** (8-10 tasks)
   - Create DTOs (BatchTransferRequest, Response, UndoResponse)
   - Implement IStudentTransferService interface
   - Implement StudentTransferService with business logic
   - Create ClassStudentController endpoints
   - Add validation logic (capacity, grade, conflicts)
   - Update EnrollmentHistory entity
   - Add repository methods for transfer queries
   - Write unit tests for service layer
   - Write integration tests for API endpoints

3. **Frontend Implementation** (10-12 tasks)
   - Create undo-store.ts with Zustand
   - Add checkbox column to student columns
   - Create batch-transfer-dialog.tsx component
   - Create undo-toast.tsx component
   - Create use-countdown.ts hook
   - Update class detail page with selection state
   - Add floating action button component
   - Update class.ts service (transfer/undo API calls)
   - Add transfer types to types/transfer.ts
   - Add i18n translations (en.json, km.json)
   - Write component tests (dialog, toast)
   - Write integration tests (full transfer flow)

4. **Documentation & Testing** (3-4 tasks)
   - Update Swagger/OpenAPI documentation
   - Add API examples to Postman collection
   - Update project README if needed
   - Perform end-to-end testing on staging

**Total Estimated Tasks**: 22-28 tasks

---

## Implementation Notes

### Backend Implementation Strategy

**Service Layer**:
```java
// Interface first (service/interfaces/IStudentTransferService.java)
public interface IStudentTransferService {
    BatchTransferResponse batchTransfer(UUID sourceClassId, BatchTransferRequest request, UUID userId);
    UndoTransferResponse undoTransfer(UUID transferId, UUID userId);
    List<EligibleClassResponse> getEligibleDestinations(UUID sourceClassId);
}

// Implementation (service/StudentTransferService.java)
@Service
@RequiredArgsConstructor
@Transactional
public class StudentTransferService implements IStudentTransferService {
    // Implement business logic with proper transaction management
    // Use constructor injection for dependencies
}
```

**Transaction Boundaries**:
- `@Transactional` on service methods modifying data
- Atomic batch transfer (all-or-nothing per student with partial success support)
- Undo operation must be atomic (all students or none)

**Error Handling**:
- Use custom exceptions that map to error codes
- GlobalExceptionHandler catches and wraps in ApiResponse
- No stack traces in production responses

### Frontend Implementation Strategy

**State Management**:
```typescript
// Undo store (store/undo-store.ts)
const useUndoStore = create<UndoStore>()(
  persist(
    (set, get) => ({
      undoState: null,
      setUndoState: (state) => set({ undoState: state }),
      clearUndoState: () => set({ undoState: null }),
      isUndoAvailable: () => {
        const state = get().undoState;
        if (!state) return false;
        return Date.now() < state.expiresAt;
      },
    }),
    {
      name: 'batch-transfer-undo',
      storage: createJSONStorage(() => sessionStorage),
    }
  )
);
```

**Component Hierarchy**:
```
ClassDetailPage
├── ClientDataTableWithUrl (with checkbox column)
├── BatchTransferDialog (conditional render)
├── UndoToast (conditional render based on undoState)
└── FloatingActionButton (conditional render based on selection)
```

**Data Fetching**:
- TanStack Query for all API calls
- Cache invalidation on transfer/undo success
- Optimistic updates NOT used (wait for server confirmation)

### Testing Strategy

**Backend Tests**:
1. **Unit Tests** (StudentTransferServiceTest):
   - Test validation logic (capacity, grade, enrollment)
   - Test conflict detection (student already in destination)
   - Test undo validation (timestamp, permissions, conflicts)
   - Mock repository calls

2. **Integration Tests** (ClassStudentControllerTest):
   - Test full endpoint flows with real database (H2 test DB)
   - Test error scenarios (unauthorized, not found, expired)
   - Test concurrent transfer scenarios

**Frontend Tests**:
1. **Component Tests**:
   - BatchTransferDialog: selection display, dropdown, validation
   - UndoToast: countdown timer, undo button, auto-dismiss
   - FloatingActionButton: visibility, count display

2. **Integration Tests** (with MSW mocking):
   - Full transfer flow: select → confirm → success toast → undo
   - Error handling: capacity exceeded, conflict, unauthorized
   - Expiration: countdown reaches zero, undo disabled

### Performance Considerations

**Backend**:
- Batch size limited to 100 students (prevents long-running transactions)
- Database indexes on transfer_id and performed_at for efficient queries
- Undo conflict check optimized with single query (not N+1)

**Frontend**:
- Debounced search input (300ms) to prevent excessive renders
- Checkbox selection state managed efficiently (Map<studentId, boolean>)
- Countdown timer updates every 1 second (not every render)
- Toast auto-dismiss at expiration (no manual cleanup needed)

### Security Considerations

**Authorization**:
- Transfer endpoint checks TRANSFER_STUDENTS permission for source class
- Undo endpoint validates current user ID matches performed_by_user_id
- All endpoints protected by JWT authentication at API Gateway

**Audit Trail**:
- All transfer events logged in enrollment_history with user ID
- Transfer ID links related enrollment records
- Undo events reference original transfer ID

**Data Integrity**:
- Server-side validation prevents invalid transfers
- Atomic transactions ensure consistency
- Conflict detection prevents cascading undo issues

---

## Definition of Done

**This feature is complete when**:

### Backend
- [ ] Flyway migration runs successfully and creates transfer columns
- [ ] All 3 API endpoints return correct ApiResponse<T> format
- [ ] Unit tests cover all service methods (>80% coverage)
- [ ] Integration tests verify database transactions work correctly
- [ ] Error codes map correctly to HTTP status codes
- [ ] Swagger UI shows all new endpoints with correct schemas
- [ ] Undo operation correctly blocks when conflicts exist

### Frontend
- [ ] Checkboxes appear next to each student in class list
- [ ] Floating action button shows/hides based on selection
- [ ] Transfer dialog displays selected students and destination dropdown
- [ ] Success toast appears within 500ms with undo button and countdown
- [ ] Countdown timer decrements every second accurately
- [ ] Undo button triggers API call and refreshes student lists
- [ ] Toast auto-dismisses after 5 minutes
- [ ] All error messages translated to English and Khmer
- [ ] Component tests pass (>80% coverage)

### Integration
- [ ] End-to-end test: Select → Transfer → Undo → Verify audit trail
- [ ] Concurrent transfer test: Two users transferring same students
- [ ] Capacity test: Transfer fails when destination class is full
- [ ] Grade mismatch test: Transfer fails for different grade levels
- [ ] Undo expiration test: Undo fails after 5 minutes
- [ ] Undo conflict test: Undo fails when student moved again
- [ ] Session test: Undo state lost on page refresh (expected)
- [ ] Mobile test: Feature works on tablet and phone screens

### Documentation
- [ ] Swagger UI updated with new endpoint documentation
- [ ] API examples added to internal Postman collection
- [ ] quickstart.md tested by another developer
- [ ] Code comments explain complex validation logic
- [ ] Migration script includes rollback instructions

### Performance
- [ ] Transfer of 20 students completes in <5 seconds
- [ ] Undo of 20 students completes in <3 seconds
- [ ] Toast notification appears in <500ms
- [ ] Page remains responsive during transfer/undo operations
- [ ] No memory leaks from countdown timer or toast notifications

---

## Risks & Mitigation

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Concurrent transfers cause enrollment conflicts | High | Medium | Use database row-level locking on student_class_enrollments; last-write-wins with clear error |
| Undo state lost on browser crash | Medium | Low | Document session-based limitation; acceptable per spec (5-min window) |
| Large batch transfer (100 students) times out | Medium | Low | Transaction timeout set to 30s; batch size limited; show progress indicator |
| User confused why undo unavailable after page refresh | Medium | Medium | Clear messaging in toast: "Undo available until page refresh or 5 minutes" |
| Clock skew between client and server causes premature expiration | Low | Medium | Use server timestamp for expiration check; client countdown is for UX only |
| Undo conflicts with manual enrollment changes | High | Low | Validate no new enrollments exist for transferred students; block undo with clear message |

---

## Success Metrics

**User Experience**:
- Teachers can transfer 5 students in <2 minutes (vs. 10+ minutes manually)
- 95% of batch transfers complete successfully without errors
- Undo feature used in <10% of transfers (indicates low error rate)

**Technical**:
- API endpoint response time <1 second for transfers up to 20 students
- Zero data integrity issues (no orphaned enrollments)
- Enrollment history provides complete audit trail for all transfers

**Business**:
- Reduces class reorganization time by 80%
- Reduces support tickets related to transfer errors (self-service undo)
- Increases teacher productivity during term transitions

---

## Next Steps

1. **Review this plan** with team/stakeholders for approval
2. **Run `/speckit.tasks`** to generate detailed task breakdown (tasks.md)
3. **Start implementation** following task priorities
4. **Regular check-ins** to verify adherence to constitution principles

**Estimated Implementation Time**: 3-5 days (1-2 days backend, 2-3 days frontend, including testing)

**Phase 2 Command**:
```
/speckit.tasks
```

This will generate the tasks.md file with prioritized, actionable implementation tasks ready for development.

---

**Plan Complete**: All Phase 0 and Phase 1 artifacts generated. Ready for task breakdown.
