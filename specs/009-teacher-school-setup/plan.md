# Implementation Plan: Teacher School Setup

**Branch**: `009-teacher-school-setup` | **Date**: 2025-12-10 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/009-teacher-school-setup/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

This feature enables newly registered teachers to complete their profile by selecting or adding their school through a hierarchical location flow (Province → District → School). The implementation introduces a new `teacher_school` relationship table to associate teachers with schools while storing school-specific metadata (principal name and gender). Frontend will use a cascading dropdown pattern with a schools table, and backend will provide location-filtered APIs for provinces, districts, and schools.

## Technical Context

**Language/Version**: Java 21 (Spring Boot 3.5.7) for backend, TypeScript 5.x with React 19 for frontend
**Primary Dependencies**: Spring Boot, Spring Data JPA, Spring Security, TanStack Router, TanStack Query, shadcn/ui, Zustand
**Storage**: PostgreSQL 15+ (existing databases: `auth_db` for teacher authentication, new location tables or shared database for provinces/districts/schools; `student_db` or separate database for teacher_school relationship)
**Testing**: JUnit 5 + Mockito (backend), Vitest + React Testing Library (frontend)
**Target Platform**: Web application (browser-based SPA + REST API microservices)
**Project Type**: Web (existing monorepo with separate frontend/ and backend microservices)
**Performance Goals**: Page load < 2 seconds, API response < 500ms, dropdown population < 1 second
**Constraints**: Must integrate with existing auth-service teacher registration flow; must prevent main app access until school setup complete; must support adding new schools without admin approval
**Scale/Scope**: ~1000 schools initially, ~10,000 teachers, province/district selectors with <100 options each

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### ✅ Microservices-First (Principle I)

- **Status**: PASS
- **Rationale**: Feature spans multiple services (auth-service for teacher management, potential new location-service or extending student-service for school/location data). Service boundaries respected.
- **Decision**: Location data (provinces, districts, schools) will be managed by extending student-service (schools already exist there per spec assumptions). Teacher-school association (`teacher_school` table) will be in auth-service database alongside teachers table. APIs exposed through API Gateway.

### ✅ Security-First (Principle II)

- **Status**: PASS
- **Rationale**: School setup requires authentication (JWT token from registration). No PII exposure in location dropdowns. Teacher-school association is protected data.
- **Requirements**:
  - School setup page requires valid JWT token
  - Teacher can only create/update their own school association
  - Validation prevents SQL injection in school creation form
  - Audit logging for new school additions

### ✅ Simplicity/YAGNI (Principle III)

- **Status**: PASS
- **Rationale**: Minimal viable implementation - simple dropdown cascade, direct table association, no complex approval workflows
- **Avoided Complexity**:
  - No admin approval for new schools (added immediately)
  - No complex state machine for setup status
  - No separate location-service microservice (extend existing student-service)
  - No caching for location data initially (premature optimization)

### ✅ Observability (Principle IV)

- **Status**: PASS
- **Requirements**:
  - Health endpoint already exists in all services
  - Log school setup completion events (teacher_id, school_id, timestamp)
  - Log new school creation events (teacher_id, school details, timestamp)
  - Track setup abandonment metric (teachers who don't complete setup)

### ✅ Test Discipline (Principle V)

- **Status**: PASS
- **Requirements**:
  - Unit tests for service layer (location filtering, school creation, teacher-school association)
  - Integration tests for API endpoints (GET provinces, GET districts, POST schools, POST teacher-school)
  - Contract tests for API responses
  - Frontend component tests for dropdown interactions and form submission

### ✅ Backend API Conventions (Principle VI)

- **Status**: PASS
- **Requirements**:
  - All endpoints return `ApiResponse<T>` wrapper with `errorCode` and `data`
  - Error codes: `SUCCESS`, `INVALID_INPUT`, `SCHOOL_NOT_FOUND`, `PROVINCE_NOT_FOUND`, `DISTRICT_NOT_FOUND`, `UNAUTHORIZED`, `TEACHER_ALREADY_ASSIGNED`
  - Frontend handles all translation of error codes to Khmer/English
  - No human-readable messages in backend responses

### ✅ Frontend Implementation Standards

- **Status**: PASS
- **Requirements**:
  - Feature module in `frontend/src/features/school-setup/`
  - Protected route in `routes/_authenticated/school-setup.tsx` (or redirect logic after registration)
  - TanStack Query for API calls
  - Zustand store for local form state (selected province/district)
  - Zod validation for new school form
  - shadcn/ui components (Select, Table, Dialog, Form, Button)

### 🚨 Gate Decision: PROCEED TO PHASE 0

All constitution checks passed. No complexity violations to justify.

## Project Structure

### Documentation (this feature)

```text
specs/009-teacher-school-setup/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   ├── endpoints.md     # API endpoint specifications
│   └── error-codes.md   # Error code definitions
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
# Backend (extending existing services)
auth-service/
├── src/main/java/com/sms/auth/
│   ├── controller/
│   │   └── TeacherSchoolController.java      # NEW: POST /teacher-school
│   ├── dto/
│   │   ├── TeacherSchoolRequest.java         # NEW: principal name, gender
│   │   └── TeacherSchoolResponse.java        # NEW: association confirmation
│   ├── model/
│   │   └── TeacherSchool.java                # NEW: teacher_school entity
│   ├── repository/
│   │   └── TeacherSchoolRepository.java      # NEW: JPA repository
│   ├── service/
│   │   ├── interfaces/
│   │   │   └── ITeacherSchoolService.java    # NEW: service interface
│   │   └── TeacherSchoolService.java         # NEW: association logic
│   └── resources/db/migration/
│       └── V##__create_teacher_school.sql    # NEW: Flyway migration
└── ...

student-service/ (or separate location data service)
├── src/main/java/com/sms/student/
│   ├── controller/
│   │   ├── ProvinceController.java           # NEW: GET /provinces
│   │   ├── DistrictController.java           # NEW: GET /districts?provinceId={id}
│   │   └── SchoolController.java             # EXTEND: GET /schools?districtId={id}, POST /schools
│   ├── dto/
│   │   ├── ProvinceResponse.java             # NEW: province list
│   │   ├── DistrictResponse.java             # NEW: district list
│   │   └── SchoolRequest.java                # NEW: create school form
│   ├── model/
│   │   ├── Province.java                     # NEW or EXISTING: province entity
│   │   ├── District.java                     # NEW or EXISTING: district entity
│   │   └── School.java                       # EXISTING: school entity (per spec assumptions)
│   ├── repository/
│   │   ├── ProvinceRepository.java           # NEW: JPA repository
│   │   ├── DistrictRepository.java           # NEW: JPA repository
│   │   └── SchoolRepository.java             # EXTEND: add filtered queries
│   └── service/
│       ├── interfaces/
│       │   ├── IProvinceService.java         # NEW: service interface
│       │   ├── IDistrictService.java         # NEW: service interface
│       │   └── ISchoolService.java           # EXTEND: add creation logic
│       ├── ProvinceService.java              # NEW: province logic
│       ├── DistrictService.java              # NEW: district logic
│       └── SchoolService.java                # EXTEND: school CRUD
└── ...

# Frontend
frontend/
├── src/
│   ├── features/
│   │   └── school-setup/                     # NEW: feature module
│   │       ├── components/
│   │       │   ├── province-selector.tsx     # NEW: province dropdown
│   │       │   ├── district-selector.tsx     # NEW: district dropdown
│   │       │   ├── school-table.tsx          # NEW: schools table
│   │       │   └── add-school-modal.tsx      # NEW: new school form
│   │       └── index.tsx                     # NEW: school setup page
│   ├── routes/
│   │   └── _authenticated/
│   │       └── school-setup.tsx              # NEW: route (or redirect after registration)
│   ├── services/
│   │   ├── location.ts                       # NEW: API calls for provinces/districts
│   │   └── school.ts                         # NEW: API calls for schools/teacher-school
│   └── lib/
│       └── validations/
│           └── school-setup.ts               # NEW: Zod schemas for school form
└── ...
```

**Structure Decision**: Web application structure maintained. Backend extends existing `auth-service` for teacher-school association and `student-service` for location/school data (per spec assumption that schools table already exists). Frontend adds new feature module under `features/school-setup/` following feature-based architecture pattern.

## Complexity Tracking

No constitution violations. No complexity justifications required.

---

## Post-Design Constitution Re-Check

*All constitution principles re-evaluated after Phase 1 design completion*

### ✅ Microservices-First (Principle I) - REAFFIRMED

**Design Decisions**:
- Location data (provinces, districts, schools) managed in student-service
- Teacher-school associations managed in auth-service
- Cross-service communication via REST APIs (teacher_school validation)
- No shared databases (school_id validated at application layer)

**Verdict**: PASS - Service boundaries maintained, no monolith tendencies

---

### ✅ Security-First (Principle II) - REAFFIRMED

**Design Decisions**:
- All endpoints require JWT authentication
- User ID extracted from JWT (not request body) for teacher-school associations
- Input validation via Jakarta Bean Validation and Zod schemas
- Cross-service school_id validation prevents dangling references
- Audit logging for school creation and associations

**Verdict**: PASS - Security requirements met

---

### ✅ Simplicity/YAGNI (Principle III) - REAFFIRMED

**Design Decisions**:
- No caching in MVP (TanStack Query default sufficient)
- No approval workflow for new schools (immediate availability)
- No pagination initially (~1000 schools, ~25 provinces, ~200 districts)
- Simple UPSERT logic for teacher-school (no complex state machine)
- Retained old VARCHAR columns for backward compatibility (removed in future)

**Avoided Complexity**:
- No separate location-service microservice
- No Redis caching for location data
- No admin approval workflows
- No complex migration rollback (migrations are additive)

**Verdict**: PASS - Minimal viable implementation achieved

---

### ✅ Observability (Principle IV) - REAFFIRMED

**Design Decisions**:
- Existing health endpoints unchanged
- Log events: school creation, teacher-school association, setup completion
- Metrics: track setup abandonment rate (future analytics)
- Error codes provide clear debugging signals

**Verdict**: PASS - Observability maintained

---

### ✅ Test Discipline (Principle V) - REAFFIRMED

**Design Decisions**:
- Unit tests for service layer (filtering, validation, CRUD)
- Integration tests for API endpoints (7 endpoints across 2 services)
- Contract tests for ApiResponse<T> schemas
- Frontend component tests for school-setup feature

**Verdict**: PASS - Test requirements met

---

### ✅ Backend API Conventions (Principle VI) - REAFFIRMED

**Design Decisions**:
- All endpoints return ApiResponse<T> with errorCode and data
- Error codes: service-specific enums (PROVINCE_NOT_FOUND, TEACHER_ALREADY_ASSIGNED, etc.)
- Frontend handles all i18n translation
- GlobalExceptionHandler maps exceptions to error codes
- HTTP status codes: 200 (success), 201 (created), 400 (validation), 404 (not found), 409 (conflict), 401 (unauthorized)

**Verdict**: PASS - API conventions followed

---

### ✅ Frontend Implementation Standards - REAFFIRMED

**Design Decisions**:
- Feature module: `features/school-setup/`
- TanStack Query for server state (provinces, districts, schools)
- Zustand for local UI state (selected province/district)
- Zod validation for school creation form
- react-hook-form for form state management
- shadcn/ui components (Select, Table, Dialog, Button, Form)
- ClientDataTable for schools table (no URL persistence needed)
- TanStack Router guard for school setup completion check

**Verdict**: PASS - Frontend standards followed

---

## Final Gate Decision: ✅ READY FOR IMPLEMENTATION

All constitution principles satisfied. No violations. No complexity debt incurred.

**Phase 1 Complete**:
- ✅ research.md generated
- ✅ data-model.md generated (teacher_school table + provinces/districts normalization)
- ✅ contracts/endpoints.md generated (7 REST endpoints)
- ✅ quickstart.md generated
- ✅ Agent context updated (CLAUDE.md)

**Next Step**: Run `/speckit.tasks` to generate implementation task breakdown.
