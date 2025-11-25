# Tasks: Frontend Authentication Integration

**Input**: Design documents from `/specs/006-frontend-auth/`
**Prerequisites**: plan.md, spec.md, data-model.md, contracts/endpoints.md, research.md, quickstart.md

**Tests**: Not explicitly requested - test tasks not included. Add if needed.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Frontend SPA**: `frontend/src/` at repository root
- Structure follows shadcn-admin reference project

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Frontend project initialization and core configuration

**Note**: Project uses Next.js 14 (not Vite). Tasks adapted accordingly.

- [x] T001 Frontend already exists with Next.js 14 - verified setup in frontend/
- [x] T002 Install sonner for toasts (core dependencies already present) in frontend/package.json
- [x] T003 [P] Initialize and configure shadcn/ui with New York style in frontend/components.json
- [x] T004 [P] SKIPPED - Next.js used instead of Vite (already configured)
- [x] T005 [P] TypeScript already configured with strict mode and path aliases in frontend/tsconfig.json
- [x] T006 [P] Tailwind CSS already configured in frontend/tailwind.config.ts
- [x] T007 Add required shadcn/ui components (button, card, form, input, label, separator, toast, sonner) via CLI
- [x] T008 Create base directory structure (features/auth/*, context/, lib/validations/, lib/i18n/)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**CRITICAL**: No user story work can begin until this phase is complete

### Type Definitions

- [ ] T009 [P] Create API response types (ApiResponse, PagedResponse) in frontend/src/types/api.types.ts
- [ ] T010 [P] Create auth types (AuthUser, LoginRequest, RegisterRequest, AuthResponse, etc.) in frontend/src/types/auth.types.ts
- [ ] T011 [P] Create error code types (CommonErrorCode, AuthErrorCode) in frontend/src/types/error-codes.types.ts

### Validation Schemas

- [ ] T012 [P] Create Zod validation schemas (loginSchema, registerSchema, passwordSchema, khmerPhoneRegex) in frontend/src/lib/validations/auth.schema.ts

### i18n Infrastructure

- [ ] T013 [P] Create i18n type definitions in frontend/src/lib/i18n/types.ts
- [ ] T014 [P] Create translations file with English and Khmer error code mappings in frontend/src/lib/i18n/translations.ts
- [ ] T015 Create language detection and translation helper functions in frontend/src/lib/i18n/index.ts

### API Client Infrastructure

- [ ] T016 Create Axios instance with baseURL, withCredentials, and response interceptor in frontend/src/lib/api-client.ts
- [ ] T017 [P] Create error handling utilities (mapErrorCode to i18n message) in frontend/src/lib/handle-server-error.ts
- [ ] T018 [P] Create utility functions (cn, sleep) in frontend/src/lib/utils.ts

### State Management

- [ ] T019 Create Zustand auth store with AuthState interface (isAuthenticated, user, preferredLanguage, actions) in frontend/src/stores/auth-store.ts

### Routing Infrastructure

- [ ] T020 Create root route with providers (QueryClientProvider, ThemeProvider, Toaster) in frontend/src/routes/__root.tsx
- [ ] T021 Create main.tsx entry point with QueryClient and Router configuration in frontend/src/main.tsx

### Shared UI Components

- [ ] T022 [P] Create PasswordInput component with show/hide toggle in frontend/src/components/password-input.tsx
- [ ] T023 [P] Create auth layout component (centered card with logo) in frontend/src/features/auth/auth-layout.tsx
- [ ] T024 [P] Create theme provider context in frontend/src/context/theme-provider.tsx

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Teacher Sign In (Priority: P1) MVP

**Goal**: Teachers can sign in with email/phone and password to access the dashboard

**Independent Test**: Enter valid credentials → Verify redirect to dashboard with session active

### API Service

- [ ] T025 [US1] Create auth service with login function (POST /api/auth/login) in frontend/src/services/auth.service.ts
- [ ] T026 [US1] Add getCurrentUser function (GET /api/auth/me) to auth service in frontend/src/services/auth.service.ts

### Sign-In Feature

- [ ] T027 [P] [US1] Create sign-in form component with email/phone and password fields in frontend/src/features/auth/sign-in/components/sign-in-form.tsx
- [ ] T028 [US1] Implement sign-in form submission with validation and API call in frontend/src/features/auth/sign-in/components/sign-in-form.tsx
- [ ] T029 [US1] Add error handling with i18n error messages to sign-in form in frontend/src/features/auth/sign-in/components/sign-in-form.tsx
- [ ] T030 [P] [US1] Create sign-in page component in frontend/src/features/auth/sign-in/index.tsx
- [ ] T031 [US1] Create sign-in route with redirect search param handling in frontend/src/routes/(auth)/sign-in.tsx

### Protected Routes

- [ ] T032 [US1] Create authenticated layout route with beforeLoad auth check in frontend/src/routes/_authenticated/route.tsx
- [ ] T033 [P] [US1] Create authenticated layout component with sidebar placeholder in frontend/src/components/layout/authenticated-layout.tsx
- [ ] T034 [US1] Create dashboard index route (post-login landing page) in frontend/src/routes/_authenticated/index.tsx
- [ ] T035 [P] [US1] Create simple dashboard component in frontend/src/features/dashboard/index.tsx

### Session Management

- [ ] T036 [US1] Implement session check on app load (call /api/auth/me and hydrate store) in frontend/src/main.tsx
- [ ] T037 [US1] Add redirect to sign-in for already-authenticated users accessing /sign-in in frontend/src/routes/(auth)/sign-in.tsx

**Checkpoint**: User Story 1 complete - Teachers can sign in and access dashboard

---

## Phase 4: User Story 2 - Teacher Registration (Priority: P2)

**Goal**: New teachers can create an account with email, phone, and password

**Independent Test**: Fill registration form → Verify account created and auto-signed in

### API Service Extension

- [ ] T038 [US2] Add register function (POST /api/auth/register) to auth service in frontend/src/services/auth.service.ts

### Registration Feature

- [ ] T039 [P] [US2] Create password strength indicator component in frontend/src/features/auth/sign-up/components/password-strength.tsx
- [ ] T040 [P] [US2] Create registration form component with email, phone, password, confirm password fields in frontend/src/features/auth/sign-up/components/sign-up-form.tsx
- [ ] T041 [US2] Implement real-time password strength feedback in registration form in frontend/src/features/auth/sign-up/components/sign-up-form.tsx
- [ ] T042 [US2] Implement Cambodia phone validation with format hint in registration form in frontend/src/features/auth/sign-up/components/sign-up-form.tsx
- [ ] T043 [US2] Implement registration form submission with validation and API call in frontend/src/features/auth/sign-up/components/sign-up-form.tsx
- [ ] T044 [US2] Add error handling for duplicate email/phone errors with i18n in frontend/src/features/auth/sign-up/components/sign-up-form.tsx
- [ ] T045 [P] [US2] Create sign-up page component in frontend/src/features/auth/sign-up/index.tsx
- [ ] T046 [US2] Create sign-up route in frontend/src/routes/(auth)/sign-up.tsx

### Navigation

- [ ] T047 [US2] Add link from sign-in to sign-up page in frontend/src/features/auth/sign-in/components/sign-in-form.tsx
- [ ] T048 [US2] Add link from sign-up to sign-in page in frontend/src/features/auth/sign-up/components/sign-up-form.tsx

**Checkpoint**: User Story 2 complete - New teachers can register

---

## Phase 5: User Story 3 - Session Persistence (Priority: P3)

**Goal**: Sessions persist across browser restarts with automatic token refresh

**Independent Test**: Sign in → Close browser → Reopen → Verify still authenticated

### Token Refresh

- [ ] T049 [US3] Add refresh function (POST /api/auth/refresh) to auth service in frontend/src/services/auth.service.ts
- [ ] T050 [US3] Implement 401 response interceptor with automatic token refresh in frontend/src/lib/api-client.ts
- [ ] T051 [US3] Add retry logic after successful refresh in API client interceptor in frontend/src/lib/api-client.ts
- [ ] T052 [US3] Implement redirect to sign-in on refresh failure in API client interceptor in frontend/src/lib/api-client.ts

### Session Validation

- [ ] T053 [US3] Add session expired toast message on auth failure in frontend/src/lib/api-client.ts
- [ ] T054 [US3] Ensure auth store resets properly on session expiry in frontend/src/stores/auth-store.ts

**Checkpoint**: User Story 3 complete - Sessions persist and auto-refresh

---

## Phase 6: User Story 4 - Teacher Sign Out (Priority: P4)

**Goal**: Teachers can sign out, invalidating their session

**Independent Test**: Sign in → Click sign out → Verify redirected to sign-in and cannot access protected routes

### API Service Extension

- [ ] T055 [US4] Add logout function (POST /api/auth/logout) to auth service in frontend/src/services/auth.service.ts

### Sign Out Feature

- [ ] T056 [P] [US4] Create user menu component with sign out button in frontend/src/components/layout/user-menu.tsx
- [ ] T057 [US4] Implement sign out handler (call API, reset store, redirect) in frontend/src/components/layout/user-menu.tsx
- [ ] T058 [US4] Add user menu to authenticated layout header in frontend/src/components/layout/authenticated-layout.tsx

**Checkpoint**: User Story 4 complete - Teachers can sign out

---

## Phase 7: User Story 5 - Forgot Password (Priority: P5)

**Goal**: Teachers can reset their password via email

**Independent Test**: Request password reset → Enter new password with token → Verify can sign in with new password

### API Service Extension

- [ ] T059 [US5] Add forgotPassword function (POST /api/auth/forgot-password) to auth service in frontend/src/services/auth.service.ts
- [ ] T060 [US5] Add resetPassword function (POST /api/auth/reset-password) to auth service in frontend/src/services/auth.service.ts

### Forgot Password Feature

- [ ] T061 [P] [US5] Create forgot password form component in frontend/src/features/auth/forgot-password/components/forgot-password-form.tsx
- [ ] T062 [US5] Implement forgot password submission with email validation in frontend/src/features/auth/forgot-password/components/forgot-password-form.tsx
- [ ] T063 [US5] Add success message display (always show success for security) in frontend/src/features/auth/forgot-password/components/forgot-password-form.tsx
- [ ] T064 [P] [US5] Create forgot password page component in frontend/src/features/auth/forgot-password/index.tsx
- [ ] T065 [US5] Create forgot password route in frontend/src/routes/(auth)/forgot-password.tsx

### Reset Password Feature

- [ ] T066 [P] [US5] Create reset password form component with new password and confirm fields in frontend/src/features/auth/reset-password/components/reset-password-form.tsx
- [ ] T067 [US5] Implement reset password submission with token from URL in frontend/src/features/auth/reset-password/components/reset-password-form.tsx
- [ ] T068 [US5] Add error handling for invalid/expired token with retry option in frontend/src/features/auth/reset-password/components/reset-password-form.tsx
- [ ] T069 [P] [US5] Create reset password page component in frontend/src/features/auth/reset-password/index.tsx
- [ ] T070 [US5] Create reset password route with token query param in frontend/src/routes/(auth)/reset-password.tsx

### Navigation

- [ ] T071 [US5] Add forgot password link to sign-in form in frontend/src/features/auth/sign-in/components/sign-in-form.tsx

**Checkpoint**: User Story 5 complete - Password reset flow works

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

### Error Handling

- [ ] T072 [P] Create generic error page component in frontend/src/features/errors/general-error.tsx
- [ ] T073 [P] Create 404 not found page component in frontend/src/features/errors/not-found-error.tsx
- [ ] T074 Add error boundary to root route in frontend/src/routes/__root.tsx

### Network Error Handling

- [ ] T075 Add network error handling with retry option in frontend/src/lib/api-client.ts
- [ ] T076 Add service unavailable message for backend errors in frontend/src/lib/handle-server-error.ts

### UI Polish

- [ ] T077 [P] Create navigation progress bar component in frontend/src/components/navigation-progress.tsx
- [ ] T078 Add loading states to all forms during submission
- [ ] T079 [P] Create app logo component in frontend/src/assets/logo.tsx

### Docker

- [ ] T080 Create Dockerfile for frontend production build in frontend/Dockerfile
- [ ] T081 Add frontend service to docker-compose.yml at repository root

### Documentation

- [ ] T082 Update frontend README with setup and development instructions in frontend/README.md
- [ ] T083 Run quickstart.md validation to ensure setup steps work

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Foundational phase completion
  - User stories can proceed in priority order (P1 → P2 → P3 → P4 → P5)
  - Or in parallel if staffed (after Phase 2)
- **Polish (Phase 8)**: Depends on at least US1 being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories - **MVP**
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Independent of US1
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - Builds on API client from Phase 2
- **User Story 4 (P4)**: Can start after US1 (needs authenticated layout) - Extends authenticated UI
- **User Story 5 (P5)**: Can start after Foundational (Phase 2) - Independent auth flow

### Within Each User Story

- API service functions before UI components that use them
- Form components before page components
- Page components before route files
- Core functionality before polish features

### Parallel Opportunities

**Phase 1 (Setup)**:
- T003, T004, T005, T006 can run in parallel (different config files)

**Phase 2 (Foundational)**:
- T009, T010, T011 can run in parallel (different type files)
- T013, T014 can run in parallel (different i18n files)
- T017, T018 can run in parallel (different lib files)
- T022, T023, T024 can run in parallel (different components)

**User Stories**:
- Once Phase 2 completes: US1, US2, US3, US5 can start in parallel (US4 waits for US1)
- Within each story: Tasks marked [P] can run in parallel

---

## Parallel Example: Foundational Phase

```bash
# Launch type definitions in parallel:
Task: "Create API response types in frontend/src/types/api.types.ts"
Task: "Create auth types in frontend/src/types/auth.types.ts"
Task: "Create error code types in frontend/src/types/error-codes.types.ts"

# Launch i18n files in parallel:
Task: "Create i18n type definitions in frontend/src/lib/i18n/types.ts"
Task: "Create translations file in frontend/src/lib/i18n/translations.ts"

# Launch shared components in parallel:
Task: "Create PasswordInput component in frontend/src/components/password-input.tsx"
Task: "Create auth layout component in frontend/src/features/auth/auth-layout.tsx"
Task: "Create theme provider in frontend/src/context/theme-provider.tsx"
```

---

## Parallel Example: User Story 1

```bash
# Launch form and page components in parallel (after T025, T026):
Task: "Create sign-in form component in frontend/src/features/auth/sign-in/components/sign-in-form.tsx"
Task: "Create sign-in page component in frontend/src/features/auth/sign-in/index.tsx"

# Launch route components in parallel (after T030, T033):
Task: "Create authenticated layout component in frontend/src/components/layout/authenticated-layout.tsx"
Task: "Create dashboard component in frontend/src/features/dashboard/index.tsx"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 - Teacher Sign In
4. **STOP and VALIDATE**: Test sign-in flow independently
5. Deploy/demo if ready - Teachers can sign in!

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 (Sign In) → Test → Deploy (MVP!)
3. Add User Story 2 (Registration) → Test → Deploy
4. Add User Story 3 (Session Persistence) → Test → Deploy
5. Add User Story 4 (Sign Out) → Test → Deploy
6. Add User Story 5 (Forgot Password) → Test → Deploy
7. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers after Phase 2:

- Developer A: User Story 1 (Sign In) - MVP priority
- Developer B: User Story 2 (Registration)
- Developer C: User Story 5 (Forgot Password)

Then:
- Developer A: User Story 4 (Sign Out) - needs US1
- Developer B: User Story 3 (Session Persistence)

---

## Notes

- [P] tasks = different files, no dependencies on other in-progress tasks
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Backend modifications required (HTTP-only cookies, /api/auth/me endpoint) - coordinate with backend team
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
