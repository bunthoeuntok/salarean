# Implementation Plan: Frontend Authentication Integration

**Branch**: `006-frontend-auth` | **Date**: 2025-11-25 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/006-frontend-auth/spec.md`

## Summary

Build a React frontend application that integrates with the existing auth-service backend, providing teacher sign-in, registration, session management, sign-out, and forgot password flows. The frontend uses the shadcn-admin project as a reference for structure, styling, and component patterns. Authentication tokens will be stored in HTTP-only cookies for security, and the UI will support bilingual (English/Khmer) error messages with browser language detection.

## Technical Context

**Language/Version**: TypeScript 5.x with React 18 (Next.js 14)
**Primary Dependencies**: Next.js 14 (App Router), TanStack Query, Zustand, Tailwind CSS 3.x, shadcn/ui, Axios, Zod, react-hook-form, i18next
**Storage**: HTTP-only cookies (tokens), Zustand (client state)
**Testing**: Jest/React Testing Library for unit tests, Playwright for E2E tests
**Target Platform**: Modern web browsers (Chrome, Firefox, Safari, Edge - latest 2 versions)
**Project Type**: Web application (Next.js App Router with SSR/CSR hybrid)
**Performance Goals**: Form validation feedback under 200ms, sign-in completion under 10 seconds
**Constraints**: Must integrate with existing auth-service API via API Gateway (localhost:8080), HTTP-only cookies require backend cookie-setting support
**Scale/Scope**: Next.js application with ~10 routes, supporting bilingual UI

**Note**: Originally planned for Vite + TanStack Router, but existing frontend uses Next.js 14 App Router. Implementation adapted to Next.js patterns while preserving shadcn-admin styling reference.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Evidence |
|-----------|--------|----------|
| I. Microservices-First | PASS | Frontend communicates only through API Gateway; no direct service calls |
| II. Security-First | PASS | HTTP-only cookies for tokens, HTTPS in production, no credentials in code |
| III. Simplicity (YAGNI) | PASS | Using established shadcn-admin patterns; no premature abstractions |
| IV. Observability | PASS | Structured error logging, toast notifications for user feedback |
| V. Test Discipline | PASS | Unit tests for validation logic, E2E tests for auth flows planned |
| VI. Backend API Conventions | PASS | Frontend maps errorCode from ApiResponse to localized messages |

**Gate Status**: PASS - Proceeding to Phase 0

## Project Structure

### Documentation (this feature)

```text
specs/006-frontend-auth/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── endpoints.md     # API contracts documentation
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
frontend/
├── src/
│   ├── assets/              # Static assets, brand icons
│   ├── components/          # Reusable components
│   │   ├── ui/              # shadcn/ui base components
│   │   ├── layout/          # Layout components (header, sidebar, etc.)
│   │   └── password-input.tsx
│   ├── config/              # Application configuration
│   ├── context/             # React context providers
│   │   ├── theme-provider.tsx
│   │   └── direction-provider.tsx
│   ├── features/            # Feature-based modules
│   │   ├── auth/            # Authentication feature
│   │   │   ├── sign-in/     # Sign-in page and form
│   │   │   ├── sign-up/     # Registration page and form
│   │   │   ├── forgot-password/
│   │   │   ├── reset-password/
│   │   │   └── auth-layout.tsx
│   │   └── dashboard/       # Dashboard (post-login landing)
│   ├── hooks/               # Custom React hooks
│   │   └── use-auth.ts      # Auth state hook
│   ├── lib/                 # Utilities and helpers
│   │   ├── api-client.ts    # Axios instance with interceptors
│   │   ├── cookies.ts       # Cookie utilities
│   │   ├── handle-server-error.ts
│   │   ├── utils.ts
│   │   └── i18n/            # Internationalization
│   │       ├── translations.ts
│   │       └── error-codes.ts
│   ├── routes/              # TanStack Router file-based routes
│   │   ├── __root.tsx
│   │   ├── (auth)/          # Auth route group (public)
│   │   │   ├── sign-in.tsx
│   │   │   ├── sign-up.tsx
│   │   │   ├── forgot-password.tsx
│   │   │   └── reset-password.tsx
│   │   └── _authenticated/  # Protected route group
│   │       ├── route.tsx    # Auth guard
│   │       └── index.tsx    # Dashboard
│   ├── services/            # API service functions
│   │   └── auth.service.ts  # Auth API calls
│   ├── stores/              # Zustand state stores
│   │   └── auth-store.ts    # Auth state management
│   ├── types/               # TypeScript type definitions
│   │   └── auth.types.ts
│   ├── styles/              # Global styles
│   │   └── index.css
│   └── main.tsx             # Application entry point
├── public/                  # Static public assets
├── tests/                   # Test files
│   ├── unit/                # Unit tests
│   └── e2e/                 # End-to-end tests
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── tailwind.config.ts
├── components.json          # shadcn/ui config
└── Dockerfile
```

**Structure Decision**: Web application structure following shadcn-admin patterns with feature-based organization. Authentication flows are contained within `features/auth/` with shared components in `components/`. TanStack Router file-based routing with `(auth)` public group and `_authenticated` protected group.

## Complexity Tracking

> No complexity violations identified. Implementation follows established patterns from shadcn-admin reference.

## Post-Design Constitution Re-Check

*Re-evaluated after Phase 1 design completion.*

| Principle | Status | Evidence |
|-----------|--------|----------|
| I. Microservices-First | PASS | Frontend in separate `/frontend` directory; communicates only via API Gateway |
| II. Security-First | PASS | HTTP-only cookies, Zod validation, no credentials in code, XSS prevention via React |
| III. Simplicity (YAGNI) | PASS | No custom abstractions; using established libraries (TanStack, Zustand, shadcn/ui) |
| IV. Observability | PASS | Error logging, toast notifications, TanStack Query devtools in dev |
| V. Test Discipline | PASS | Vitest for unit tests, Playwright for E2E tests, Zod schemas testable |
| VI. Backend API Conventions | PASS | Frontend consumes ApiResponse format, maps errorCode to i18n translations |

**Post-Design Gate Status**: PASS - Ready for `/speckit.tasks`

## Open Items for Implementation

1. **Backend Cookie Support**: Auth-service needs modification to set HTTP-only cookies instead of returning tokens in response body. See `research.md` for implementation details.

2. **New Endpoint Required**: `/api/auth/me` endpoint needed for session validation on app load. Proposed in `contracts/endpoints.md`.

3. **CORS Configuration**: Backend CORS must allow credentials from `http://localhost:5173` (Vite dev server).
