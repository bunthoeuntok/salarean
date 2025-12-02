# Implementation Plan: Class Detail View

**Branch**: `007-class-view` | **Date**: 2025-12-02 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/007-class-view/spec.md`

## Summary

This feature implements a class detail view page accessible from the class list. The primary requirement is to display enrolled students in a tabbed interface with lazy-loading support for future tabs (Schedule, Attendance, Grades). The Students tab will show a paginated, searchable, filterable list of students with real-time search (300ms debounce), alphabetical sorting by default, and WCAG 2.1 Level AA accessibility compliance.

**Technical approach**: Frontend-only feature using React 19 + TanStack Router for routing, TanStack Query for data fetching with caching, shadcn/ui Tabs component for tab navigation, and existing student-service API endpoint `/api/classes/{id}/students` for fetching enrollment data.

## Technical Context

**Language/Version**: TypeScript 5.x with React 19, Java 21 (for API endpoint if modifications needed)
**Primary Dependencies**:
- Frontend: TanStack Router v1.x, TanStack Query v5.x, TanStack Table v8.x, shadcn/ui, Zod v3.x, react-hook-form v7.x
- Backend: Spring Boot 3.5.7, Spring Data JPA (existing student-service)

**Storage**: PostgreSQL 15+ (existing `student_db` with `students` and `student_class_enrollments` tables)
**Testing**: Vitest (frontend unit), React Testing Library (component), Playwright (E2E accessibility)
**Target Platform**: Web browsers (Chrome 90+, Firefox 88+, Safari 14+, Edge 90+)
**Project Type**: Web application (frontend feature + backend API verification)
**Performance Goals**:
- Page load: <2 seconds for classes with up to 100 students
- Search response: <300ms after debounce
- Tab switching: <100ms for cached content
- Pagination navigation: <500ms

**Constraints**:
- WCAG 2.1 Level AA compliance required
- Must work on mobile/tablet (responsive design)
- Real-time search with 300ms debouncing
- URL-based tab routing for bookmarking
- Lazy loading for tab content

**Scale/Scope**:
- Expected class sizes: 10-100 students (extreme: up to 200)
- Pagination: 20 students per page
- Concurrent users: 50-100 teachers viewing classes simultaneously

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Principle I: Microservices-First ✅ PASS

- **Service boundary**: Uses existing student-service API, no new service required
- **Loosely coupled**: Frontend communicates via REST API only
- **Single responsibility**: Class detail view is a frontend concern; student data remains in student-service
- **Failure isolation**: Frontend handles API errors gracefully with user-friendly messages

### Principle II: Security-First ✅ PASS

- **Authentication**: Uses existing JWT-based authentication (class detail page requires login)
- **Authorization**: Relies on student-service RBAC (users can only view classes they have permission to access)
- **Data protection**: No PII stored in frontend; all data fetched from secured API
- **Audit logging**: Student data access logged by student-service (existing)
- **Secrets management**: N/A for frontend; API uses existing JWT secret management

### Principle III: Simplicity (YAGNI) ✅ PASS

- **No premature optimization**: Uses standard pagination (20/page), no complex caching beyond TanStack Query defaults
- **No speculative features**: Only Students tab implemented; other tabs show "Coming Soon" placeholders
- **Minimal dependencies**: Reuses existing shadcn/ui components, TanStack libraries already in project
- **Clear code**: Feature-based architecture (features/classes/[id].tsx), no custom abstractions

**Complexity additions**: None. Tab navigation uses standard shadcn/ui Tabs component, routing uses existing TanStack Router patterns.

### Principle IV: Observability ✅ PASS

- **Health endpoints**: Frontend: N/A (static assets); Backend: existing `/actuator/health`
- **Structured logging**: Frontend errors logged to console (dev) and error tracking service (production, existing setup)
- **Metrics**: TanStack Query DevTools for cache inspection, React DevTools for component profiling
- **Alerting thresholds**: Reuses existing frontend error monitoring (e.g., Sentry integration if configured)

### Principle V: Test Discipline ⚠️ PARTIAL

- **Unit tests**: Component logic tested with Vitest + React Testing Library
- **Integration tests**: TanStack Query hooks tested with mock API responses
- **Contract tests**: API endpoint contract validated (schema verification)
- **Test independence**: Tests use mock data, no external API calls

**Note**: E2E accessibility testing (WCAG 2.1 AA) added as new requirement. Will use Playwright + axe-core for automated checks.

### Principle VI: Backend API Conventions ✅ PASS

- **Response wrapper**: API returns `ApiResponse<PagedStudentEnrollmentResponse>` with `errorCode` and `data`
- **Error codes**: Uses existing error codes (`CLASS_NOT_FOUND`, `UNAUTHORIZED`, etc.)
- **Status codes**: Standard HTTP codes (200, 404, 401, 500)
- **Internationalization**: Frontend maps error codes to translated messages (Khmer/English)
- **Global exception handling**: Existing `@RestControllerAdvice` in student-service

**Conclusion**: All constitution principles satisfied. No complexity violations to justify.

## Project Structure

### Documentation (this feature)

```text
specs/007-class-view/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0: Technology research
├── data-model.md        # Phase 1: Entity schema
├── quickstart.md        # Phase 1: Development setup
├── contracts/           # Phase 1: API contracts
│   └── get-class-students.md
├── checklists/          # Validation checklists
│   └── requirements.md
└── tasks.md             # Phase 2: NOT created by /speckit.plan
```

### Source Code (repository root)

This feature follows the **Web application** structure with frontend-heavy implementation:

```text
frontend/src/
├── features/classes/
│   ├── [id].tsx         # NEW: Class detail page route
│   └── components/
│       ├── class-header.tsx           # NEW: Class info header
│       ├── students-tab.tsx           # NEW: Students tab content
│       ├── student-list.tsx           # NEW: Student table with search/filter
│       ├── student-list-item.tsx      # NEW: Student row component
│       ├── coming-soon-tab.tsx        # NEW: Placeholder for future tabs
│       └── student-search.tsx         # NEW: Search/filter controls
├── routes/_authenticated/
│   └── classes.$id.tsx  # NEW: Route file (dynamic class ID parameter)
├── services/
│   └── classes.ts       # MODIFIED: Add getClassStudents() function
├── types/
│   └── class.ts         # MODIFIED: Add ClassDetailResponse, StudentEnrollmentItem types
├── hooks/
│   ├── use-class-students.ts  # NEW: TanStack Query hook for fetching students
│   └── use-debounce.ts        # NEW: Debounce hook for search
└── lib/validations/
    └── class-filters.ts       # NEW: Zod schema for search/filter params

student-service/src/main/java/com/sms/student/
├── controller/
│   └── ClassController.java   # MODIFIED: Add getStudentsByClass endpoint
├── service/
│   ├── interfaces/
│   │   └── IClassService.java # MODIFIED: Add getStudentsByClass method
│   └── ClassService.java      # MODIFIED: Implement getStudentsByClass
├── dto/
│   ├── PagedStudentEnrollmentResponse.java  # NEW: Paginated response DTO
│   └── StudentEnrollmentItem.java           # NEW: Enrollment item DTO
└── repository/
    └── StudentClassEnrollmentRepository.java # MODIFIED: Add findByClassId query
```

**Structure Decision**: Feature-based frontend architecture (features/classes/[id].tsx) with supporting components in feature subdirectory. Backend modifications minimal (one new endpoint in existing ClassController). This aligns with constitution's frontend implementation standards (feature-based vertical slices).

## Complexity Tracking

> No complexity violations - table intentionally left empty.

---

# Phase 0: Research

## Research Questions

1. **TanStack Table integration with search/filter**: How to implement real-time search with debouncing using TanStack Table's filtering API?
2. **WCAG 2.1 Level AA compliance**: What specific requirements apply to tab navigation, search inputs, and data tables?
3. **URL-based tab routing**: Best practices for preserving tab state in URL with TanStack Router?
4. **Lazy loading strategy**: How to implement lazy tab content loading without affecting accessibility?

## Findings Summary

*(Full details in research.md)*

### 1. TanStack Table + Real-time Search

**Decision**: Use TanStack Table's `globalFilter` with custom debounced state.

**Rationale**: TanStack Table v8 supports client-side filtering via `globalFilter` prop. Combined with React's `useDeferredValue` or custom debounce hook, achieves 300ms debounce requirement without additional libraries.

**Implementation approach**:
```typescript
const [searchTerm, setSearchTerm] = useState('')
const debouncedSearch = useDebouncedValue(searchTerm, 300)
const table = useReactTable({
  data: students,
  columns,
  state: { globalFilter: debouncedSearch },
  onGlobalFilterChange: setSearchTerm,
  globalFilterFn: 'includesString' // Case-insensitive substring match
})
```

**Alternatives considered**:
- Server-side search: Rejected due to unnecessary API calls and complexity
- Lodash debounce: Rejected to avoid extra dependency (React's useDeferredValue sufficient)

### 2. WCAG 2.1 Level AA Requirements

**Decision**: Implement ARIA roles, keyboard navigation, focus management, and 4.5:1 color contrast.

**Rationale**: WCAG 2.1 AA is the legal standard for educational institutions. Key requirements for this feature:
- **Tabs**: Use `role="tablist"`, `role="tab"`, `role="tabpanel"` with arrow key navigation
- **Search**: `aria-label="Search students by name or code"`, live region for result count
- **Table**: Semantic `<table>` with `<caption>`, sortable column headers with `aria-sort`
- **Keyboard**: All interactive elements reachable via Tab, operable with Enter/Space
- **Focus indicators**: Visible outline (2px solid, contrast ratio 3:1 minimum)
- **Color contrast**: Text 4.5:1, large text 3:1, UI components 3:1

**Tools**: axe DevTools (browser extension), Playwright + axe-core (automated testing)

**Alternatives considered**: WCAG AAA (rejected as overly strict for MVP)

### 3. URL-based Tab Routing

**Decision**: Use TanStack Router search params with `useSearch` hook.

**Rationale**: TanStack Router supports type-safe search params. Preserves tab state on refresh and enables direct linking.

**Implementation approach**:
```typescript
// Route: routes/_authenticated/classes.$id.tsx
const searchSchema = z.object({
  tab: z.enum(['students', 'schedule', 'attendance', 'grades']).optional().default('students')
})

function ClassDetailPage() {
  const { tab } = useSearch({ from: '/_authenticated/classes/$id' })
  // Render active tab based on `tab` param
}
```

**Alternatives considered**:
- Hash-based routing (#students): Rejected due to poor SEO and analytics tracking
- Separate routes per tab: Rejected as over-engineering (4 routes for one page)

### 4. Lazy Tab Loading Strategy

**Decision**: Use React.lazy() for tab components with Suspense fallback, maintain tab panel in DOM with `hidden` attribute.

**Rationale**: Accessibility requirement: tab panels must exist in DOM for screen readers to announce count ("4 tabs"). Solution: Render all `<TabPanel>` elements but lazy-load content only when clicked.

**Implementation approach**:
```typescript
const StudentsTab = React.lazy(() => import('./components/students-tab'))
const ScheduleTab = React.lazy(() => import('./components/schedule-tab'))

<TabsContent value="students">
  <Suspense fallback={<Skeleton />}>
    <StudentsTab classId={classId} />
  </Suspense>
</TabsContent>
```

**Alternatives considered**:
- Conditional rendering (remove unselected tabs from DOM): Rejected due to screen reader accessibility
- Preload all tabs: Rejected due to performance impact (unnecessary data fetching)

---

# Phase 1: Design

## Data Model

*(Full details in data-model.md)*

### Frontend Types

**ClassDetailResponse** (from `/api/classes/{id}`)
```typescript
interface ClassDetailResponse {
  id: string
  className: string
  classCode: string
  gradeLevel: 'KINDERGARTEN' | 'PRIMARY_1' | ... | 'SECONDARY_12'
  academicYear: string
  teacherName: string | null
  capacity: number
  currentEnrollment: number
}
```

**PagedStudentEnrollmentResponse** (from `/api/classes/{id}/students`)
```typescript
interface PagedStudentEnrollmentResponse {
  content: StudentEnrollmentItem[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
}

interface StudentEnrollmentItem {
  studentId: string
  studentName: string
  studentCode: string
  photoUrl: string | null
  enrollmentDate: string // ISO 8601
  enrollmentStatus: 'ACTIVE' | 'TRANSFERRED' | 'GRADUATED' | 'WITHDRAWN'
}
```

**Search/Filter State**
```typescript
interface StudentFilters {
  search: string // Name or code
  status: EnrollmentStatus | null
  page: number
  size: number
}
```

### Backend Entities (Existing)

**Student** (existing in `student_db.students`)
- id: UUID (PK)
- student_code: VARCHAR(20) UNIQUE
- full_name: VARCHAR(255)
- photo_url: VARCHAR(500) NULL
- ... (other student fields)

**StudentClassEnrollment** (existing in `student_db.student_class_enrollments`)
- id: UUID (PK)
- student_id: UUID (FK → students)
- class_id: UUID (FK → classes)
- enrollment_date: DATE
- status: ENUM('ACTIVE', 'TRANSFERRED', 'GRADUATED', 'WITHDRAWN')
- created_at: TIMESTAMP
- updated_at: TIMESTAMP

### State Transitions

**StudentEnrollmentStatus**: ACTIVE → (TRANSFERRED | GRADUATED | WITHDRAWN)
- ACTIVE: Currently enrolled, attending class
- TRANSFERRED: Moved to different class/school
- GRADUATED: Completed grade level
- WITHDRAWN: Left school (dropped out, moved)

No transitions out of terminal states (TRANSFERRED, GRADUATED, WITHDRAWN).

## API Contracts

*(Full details in contracts/get-class-students.md)*

### GET /api/classes/{id}/students

**Purpose**: Fetch paginated list of students enrolled in a specific class with search/filter support.

**Authentication**: Required (JWT token)

**Authorization**: User must have permission to view the specified class

**Request**:
```http
GET /api/classes/{classId}/students?page=0&size=20&search=john&status=ACTIVE
Authorization: Bearer {jwt_token}
```

**Path Parameters**:
- `classId` (UUID, required): Class identifier

**Query Parameters**:
- `page` (integer, optional, default: 0): Zero-based page number
- `size` (integer, optional, default: 20, max: 100): Page size
- `search` (string, optional): Search term for student name or code (case-insensitive substring match)
- `status` (enum, optional): Filter by enrollment status (ACTIVE | TRANSFERRED | GRADUATED | WITHDRAWN)
- `sort` (string, optional, default: "studentName,asc"): Sort field and direction

**Response 200 OK**:
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "content": [
      {
        "studentId": "123e4567-e89b-12d3-a456-426614174000",
        "studentName": "John Doe",
        "studentCode": "STU-2024-0001",
        "photoUrl": "https://cdn.example.com/photos/student-123.jpg",
        "enrollmentDate": "2024-09-01",
        "enrollmentStatus": "ACTIVE"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

**Response 404 Not Found**:
```json
{
  "errorCode": "CLASS_NOT_FOUND",
  "data": null
}
```

**Response 401 Unauthorized**:
```json
{
  "errorCode": "UNAUTHORIZED",
  "data": null
}
```

**Response 403 Forbidden**:
```json
{
  "errorCode": "FORBIDDEN",
  "data": null
}
```

**Performance**:
- Database query: <100ms (indexed on class_id, status)
- Total response time: <200ms (includes serialization)

**Caching**: TanStack Query caches response for 5 minutes (staleTime: 300000)

## Development Quickstart

*(Full details in quickstart.md)*

### Prerequisites

- Node.js 20+, pnpm 8+
- Java 21, Maven 3.9+
- Docker & Docker Compose (for backend services)
- Running student-service on port 8082

### Setup Steps

1. **Start backend services** (if not already running):
   ```bash
   cd /Volumes/DATA/my-projects/salarean
   docker-compose up -d postgres-student student-service
   ```

2. **Install frontend dependencies**:
   ```bash
   cd frontend
   pnpm install
   ```

3. **Create feature branch** (if not already created):
   ```bash
   git checkout 007-class-view
   ```

4. **Run frontend dev server**:
   ```bash
   pnpm dev
   # Open http://localhost:5173
   ```

5. **Verify API endpoint**:
   ```bash
   # Get JWT token (login first)
   # Replace {classId} and {token}
   curl -H "Authorization: Bearer {token}" \
     http://localhost:8080/api/classes/{classId}/students?page=0&size=20
   ```

### Development Workflow

1. **Frontend**:
   - Create route: `frontend/src/routes/_authenticated/classes.$id.tsx`
   - Create feature components in `frontend/src/features/classes/`
   - Add API service: `frontend/src/services/classes.ts`
   - Add types: `frontend/src/types/class.ts`
   - Test with React DevTools + TanStack Query DevTools

2. **Backend** (if endpoint doesn't exist):
   - Add endpoint: `student-service/src/main/java/com/sms/student/controller/ClassController.java`
   - Add service method: `student-service/src/main/java/com/sms/student/service/ClassService.java`
   - Add DTOs: `student-service/src/main/java/com/sms/student/dto/`
   - Test with Swagger UI: `http://localhost:8082/swagger-ui.html`

3. **Testing**:
   - Unit tests: `pnpm test` (Vitest)
   - E2E tests: `pnpm test:e2e` (Playwright)
   - Accessibility: `pnpm test:a11y` (Playwright + axe-core)

### Troubleshooting

- **CORS errors**: Ensure API Gateway (port 8080) is running and CORS configured
- **404 on route**: Check TanStack Router devtools, verify route file naming
- **Empty student list**: Verify class has enrolled students in database
- **TypeScript errors**: Run `pnpm exec tsr generate` to regenerate route types

---

## Complexity Tracking

*(Intentionally empty - no complexity violations)*

## Next Steps

After this plan is complete:

1. **Run `/speckit.tasks`** to generate task breakdown from this plan
2. **Begin implementation** following task priorities (P1: Students tab → P2: Tab navigation → P3: Search/filter)
3. **Verify accessibility** using axe DevTools during development
4. **Test on mobile devices** (responsive design requirement)

**Estimated effort**: 2-3 days (frontend-focused, minimal backend changes)
