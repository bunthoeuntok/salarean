<!--
SYNC IMPACT REPORT
==================
Version change: 1.3.0 → 1.4.0 (Minor - Frontend structure standardization)
Modified principles:
  - None (existing principles unchanged)
Modified sections:
  - Project Structure > Frontend Structure: Updated to reflect actual feature-based architecture
  - Added Frontend Implementation Standards section (NEW)
Added sections:
  - Frontend Implementation Standards (comprehensive frontend architecture guidance)
Removed sections: None
Templates requiring updates:
  - .specify/templates/plan-template.md ✅ Compatible (technology-agnostic)
  - .specify/templates/spec-template.md ✅ Compatible (technology-agnostic)
  - .specify/templates/tasks-template.md ✅ Compatible (technology-agnostic)
External documents updated:
  - README.md ⚠ No update needed (already reflects actual structure)
  - CLAUDE.md ⚠ No update needed (already reflects actual structure)
Follow-up TODOs: None
==================
-->

# Student Management System (SMS) Constitution

## Core Principles

### I. Microservices-First

Every feature MUST be implemented within appropriate service boundaries. Services MUST be:

- **Independently deployable**: Each service has its own Docker container, database, and deployment lifecycle
- **Loosely coupled**: Services communicate via well-defined APIs (REST) and message queues (RabbitMQ)
- **Single responsibility**: Each service owns one bounded context (Auth, Students, Grades, Reports, etc.)
- **Failure-isolated**: Service failures MUST NOT cascade; implement circuit breakers and graceful degradation

**Rationale**: The SMS architecture depends on service isolation for scalability, maintainability, and independent team workflows. Violating boundaries creates deployment bottlenecks and cascading failures.

### II. Security-First

All development MUST prioritize data protection and compliance with Cambodia Ministry of Education, Youth and Sport (MoEYS) standards.

- **Authentication**: All API endpoints (except public health checks) MUST require valid JWT tokens
- **Authorization**: Role-based access control (RBAC) MUST be enforced at service boundaries
- **Data protection**: Student PII MUST be encrypted at rest and in transit
- **Audit logging**: All data mutations MUST be logged with user identity and timestamp
- **Secrets management**: No credentials in code; all secrets via environment variables or secure vaults

**Rationale**: SMS handles sensitive student data. Security breaches compromise student privacy and violate educational data protection regulations.

### III. Simplicity (YAGNI)

Start with the simplest solution that meets requirements. Complexity MUST be justified.

- **No premature optimization**: Optimize only when metrics prove necessity
- **No speculative features**: Build only what the current requirement demands
- **Minimal dependencies**: Each added library MUST solve a documented problem
- **Clear code over clever code**: Prefer readable, maintainable implementations

**Complexity justification required for**:
- Adding new services beyond the core eight
- Introducing new infrastructure components
- Creating abstraction layers or patterns not already established

**Rationale**: Each complexity addition increases maintenance burden, onboarding time, and bug surface area. Cambodia school administrators need a reliable, maintainable system.

### IV. Observability

All services MUST provide visibility into their health, performance, and behavior.

- **Health endpoints**: Every service exposes `/actuator/health` for container orchestration
- **Structured logging**: JSON-formatted logs with correlation IDs for distributed tracing
- **Metrics**: Key performance indicators (latency, throughput, error rates) MUST be measurable
- **Alerting thresholds**: Define and document acceptable limits for critical operations

**Rationale**: Microservices require observability to diagnose issues across service boundaries. Without it, debugging distributed failures becomes exponentially harder.

### V. Test Discipline

All code MUST have appropriate test coverage to ensure reliability and enable safe refactoring.

- **Unit tests**: Business logic MUST have unit tests with meaningful assertions
- **Integration tests**: Service interactions and database operations MUST be tested
- **Contract tests**: API endpoints MUST have contract tests validating request/response schemas
- **Test independence**: Tests MUST NOT depend on external services or shared state

**Exceptions**: Test coverage requirements may be relaxed for:
- Proof-of-concept implementations (clearly labeled, time-boxed)
- Generated code (DTOs, mappers) where source templates are tested

**Rationale**: Without tests, changes become risky and velocity decreases over time. Test discipline enables confident refactoring and catches regressions early.

### VI. Backend API Conventions

All backend services MUST follow standardized response formats and error handling patterns for consistency across the system.

- **Response wrapper**: All API endpoints MUST return `ApiResponse<T>` with `errorCode` and `data` fields
- **Error codes**: Use machine-readable enum values (UPPER_SNAKE_CASE) - NO human-readable messages in responses
- **Status codes**: Map error types to appropriate HTTP status codes (400, 401, 404, 429, 500)
- **Internationalization separation**: Backend returns only error codes; frontend handles all translation to Khmer/English
- **Global exception handling**: Use `@RestControllerAdvice` to centralize error mapping

**Response Structure**:
```json
{
  "errorCode": "SUCCESS",
  "data": { "id": "123", "name": "John" }
}
```

**Error Code Requirements**:
- Self-documenting names (e.g., `INVALID_PHONE_FORMAT` not `ERR_001`)
- Consistent across all services
- Documented in feature specifications
- Defined in service-specific `ErrorCode` enum

**Rationale**: Separating error codes from messages enables dynamic frontend translation without backend deployments, simplifies API contracts, and provides clear debugging signals across service boundaries.

## Project Structure

The SMS project follows a **microservices monorepo** architecture with clear separation of concerns.

### Repository Layout

```text
salarean/
├── frontend/                    # React 19 SPA with Vite
├── auth-service/                # Authentication & authorization microservice
├── student-service/             # Student records management microservice
├── grade-service/               # Grade calculation & management microservice
├── attendance-service/          # Attendance tracking microservice
├── report-service/              # Report generation microservice
├── notification-service/        # Email/SMS notification microservice
├── api-gateway/                 # API Gateway (routing, rate limiting)
├── eureka-server/               # Service discovery (Netflix Eureka)
├── nginx/                       # Reverse proxy configuration
├── specs/                       # Feature specifications (by number)
│   ├── 001-feature-name/
│   │   ├── spec.md              # Feature specification
│   │   ├── plan.md              # Implementation plan
│   │   ├── tasks.md             # Task breakdown
│   │   └── ...
│   └── 002-another-feature/
├── .specify/                    # Constitution & templates
│   ├── memory/
│   │   └── constitution.md      # This document
│   └── templates/               # Feature & task templates
├── docker-compose.yml           # Multi-service orchestration
├── CLAUDE.md                    # Auto-generated dev guidelines
└── README.md                    # Project setup & quick start
```

### Frontend Structure (React 19 + Vite SPA)

```text
frontend/
├── src/
│   ├── routes/                  # TanStack Router route definitions
│   │   ├── __root.tsx           # Root layout with providers
│   │   ├── index.tsx            # Home/landing page
│   │   ├── (auth)/              # Public auth routes (login, register)
│   │   └── _authenticated/      # Protected routes (requires authentication)
│   ├── features/                # Feature-based organization
│   │   ├── auth/                # Authentication feature module
│   │   ├── students/            # Student management feature module
│   │   │   ├── components/      # Feature-specific components
│   │   │   ├── columns.tsx      # Table column definitions
│   │   │   └── index.tsx        # Feature entry point
│   │   ├── classes/             # Class management feature module
│   │   ├── dashboard/           # Dashboard feature module
│   │   ├── settings/            # Settings feature module
│   │   └── errors/              # Error pages (404, 500, etc.)
│   ├── components/              # Shared React components
│   │   ├── ui/                  # shadcn/ui base components
│   │   ├── layout/              # Layout components (Header, Sidebar, etc.)
│   │   └── data-table/          # Reusable data table components
│   ├── context/                 # React context providers
│   │   ├── theme-provider.tsx   # Theme (light/dark mode)
│   │   ├── language-provider.tsx # i18n language context
│   │   ├── direction-provider.tsx # RTL/LTR direction
│   │   ├── layout-provider.tsx  # Layout state (sidebar, etc.)
│   │   └── search-provider.tsx  # Global search context
│   ├── hooks/                   # Custom React hooks
│   ├── lib/                     # Utilities & helpers
│   │   ├── api.ts               # Axios client configuration
│   │   ├── cookies.ts           # Cookie utilities
│   │   ├── handle-server-error.ts # Error handling utilities
│   │   ├── utils.ts             # General utility functions
│   │   ├── i18n/                # Internationalization setup
│   │   ├── utils/               # Additional utilities
│   │   └── validations/         # Zod validation schemas
│   ├── services/                # API service functions (domain-specific)
│   ├── store/                   # Zustand stores (client state)
│   ├── types/                   # TypeScript type definitions
│   ├── assets/                  # Static assets
│   │   └── icons/               # SVG icons and images
│   ├── styles/                  # Global styles and CSS
│   └── main.tsx                 # Application entry point
├── public/                      # Static public assets
├── index.html                   # HTML entry point
├── vite.config.ts               # Vite configuration
├── tailwind.config.ts           # Tailwind CSS configuration
├── tsconfig.json                # TypeScript configuration
└── package.json
```

### Microservice Structure (Spring Boot)

Each `*-service/` directory follows this standard layout:

```text
{service-name}-service/
├── src/
│   ├── main/
│   │   ├── java/com/sms/{service}/
│   │   │   ├── config/          # Spring configuration beans
│   │   │   ├── controller/      # REST API endpoints
│   │   │   ├── dto/             # Request/response objects
│   │   │   ├── exception/       # Custom exceptions + handler
│   │   │   ├── model/           # JPA entities
│   │   │   ├── repository/      # Spring Data repositories
│   │   │   ├── security/        # Auth filters (if needed)
│   │   │   ├── service/         # Business logic
│   │   │   │   ├── interfaces/  # Service interfaces (I-prefixed)
│   │   │   │   └── *.java       # Service implementations
│   │   │   └── validation/      # Custom validators
│   │   └── resources/
│   │       ├── application.yml  # Service configuration
│   │       └── db/migration/    # Flyway migrations
│   └── test/
│       ├── java/...             # Unit & integration tests
│       └── resources/
│           └── application-test.yml
├── uploads/                     # Local file storage (if needed)
├── target/                      # Maven build output (gitignored)
├── pom.xml                      # Maven dependencies
├── Dockerfile                   # Container image definition
└── README.md                    # Service-specific docs
```

### Feature Specification Structure

Each feature in `specs/###-feature-name/` MUST contain:

```text
specs/###-feature-name/
├── spec.md                      # User stories & requirements
├── plan.md                      # Implementation plan
├── tasks.md                     # Task breakdown
├── research.md                  # Technical research (Phase 0)
├── data-model.md                # Entity design (Phase 1)
├── quickstart.md                # Developer setup (Phase 1)
└── contracts/                   # API contracts (Phase 1)
    ├── endpoints.md
    └── ...
```

### Structure Rules

**Monorepo Conventions**:
- Each microservice MUST be independently deployable
- Services MUST NOT share code via filesystem (use Maven artifacts if needed)
- Each service MUST have its own `pom.xml` and dependencies
- Frontend MUST communicate only through API Gateway (no direct service calls)

**Service Naming**:
- Pattern: `{domain}-service` (e.g., `auth-service`, `student-service`)
- Java package: `com.sms.{domain}` (e.g., `com.sms.auth`)
- Database: `{domain}_db` (e.g., `auth_db`, `student_db`)

**Feature Numbering**:
- Use zero-padded 3-digit numbers: `001-`, `002-`, `003-`, etc.
- Feature names in kebab-case: `001-teacher-auth`, `002-jwt-refresh`

**Rationale**: The monorepo structure enables shared tooling and atomic cross-service changes while maintaining microservice independence. Standardized layouts reduce cognitive load and enable automation.

## Technology Stack

The following technologies are mandated for the SMS project:

### Frontend
- **Framework**: React 19 with Vite 7.x (SPA architecture)
- **Language**: TypeScript 5.x (strict mode)
- **Routing**: TanStack Router for type-safe file-based routing
- **Styling**: Tailwind CSS 4.x with shadcn/ui components
- **State Management**: Zustand for client state, TanStack Query for server state
- **Forms**: react-hook-form with Zod validation
- **API Client**: Axios for HTTP requests
- **Internationalization**: i18next for Khmer/English translations

### Backend
- **Framework**: Spring Boot 3.5+
- **Language**: Java 21+
- **Build**: Maven with wrapper (mvnw)
- **Database**: PostgreSQL 15+ (one database per service)
- **Cache**: Redis 7+ for session and performance caching
- **Messaging**: RabbitMQ 3+ for async communication

### Infrastructure
- **Containers**: Docker with Docker Compose for local development
- **Proxy**: Nginx for routing and TLS termination
- **Discovery**: Netflix Eureka for service registration

**Rationale**: Standardizing the stack ensures team consistency, reduces context switching, and enables shared tooling and knowledge.

### Frontend Implementation Standards

All React frontend applications MUST adhere to the following architectural patterns:

**Feature-Based Architecture**:

The frontend follows a feature-based (vertical slice) architecture where each feature module is self-contained:

```text
features/{feature-name}/
├── components/          # Feature-specific components
├── hooks/               # Feature-specific hooks (optional)
├── types/               # Feature-specific types (optional)
├── columns.tsx          # Table column definitions (if applicable)
└── index.tsx            # Feature entry point/main page
```

**Rules**:
- Each feature module MUST be independently understandable
- Feature-specific components MUST NOT be imported by other features
- Shared components MUST be placed in `components/`
- Feature modules MAY have their own hooks, types, and utilities

**Routing Conventions**:

- **Public routes**: Place in `routes/(auth)/` for unauthenticated pages (login, register)
- **Protected routes**: Place in `routes/_authenticated/` for authenticated-only pages
- **Root layout**: `routes/__root.tsx` MUST include all context providers
- **File naming**: Use kebab-case for route files (e.g., `student-list.tsx`)

**Context Providers Pattern**:

All global state providers MUST be organized in `context/` directory:

- `theme-provider.tsx` - Light/dark mode theming
- `language-provider.tsx` - i18n language selection (Khmer/English)
- `direction-provider.tsx` - Text direction (LTR/RTL)
- `layout-provider.tsx` - Layout state (sidebar collapse, etc.)
- `search-provider.tsx` - Global search functionality

**Provider composition** in `__root.tsx`:
```tsx
<ThemeProvider>
  <DirectionProvider>
    <LanguageProvider>
      <LayoutProvider>
        <SearchProvider>
          <Outlet />
        </SearchProvider>
      </LayoutProvider>
    </LanguageProvider>
  </DirectionProvider>
</ThemeProvider>
```

**Component Organization**:

- `components/ui/` - shadcn/ui base components (Button, Input, Dialog, etc.)
- `components/layout/` - Layout components (Header, Sidebar, Footer, etc.)
- `components/data-table/` - Reusable data table components with pagination, sorting, filtering
- Feature-specific components MUST stay in `features/{name}/components/`

**State Management**:

- **Server state**: Use TanStack Query for all API data fetching and caching
- **Client state**: Use Zustand for local UI state (theme, sidebar, filters, etc.)
- **Form state**: Use react-hook-form with Zod validation schemas
- **Context**: Use React Context only for cross-cutting concerns (theme, language, layout)

**API Integration**:

- API client configuration in `lib/api.ts` (Axios instance with interceptors)
- API service functions in `services/{domain}.ts` (e.g., `services/students.ts`)
- Error handling in `lib/handle-server-error.ts`
- Cookie utilities in `lib/cookies.ts`

**Validation**:

- All validation schemas MUST be defined in `lib/validations/`
- Use Zod for runtime type validation
- Integrate with react-hook-form via `@hookform/resolvers/zod`

**Internationalization**:

- i18n setup in `lib/i18n/`
- Translation keys in `lib/i18n/locales/{en,km}.json`
- Use `useTranslation()` hook from react-i18next
- Backend error codes MUST be mapped to translated messages in frontend

**TypeScript Standards**:

- Strict mode MUST be enabled in `tsconfig.json`
- All API responses MUST have typed interfaces in `types/`
- Use `type` for object shapes, `interface` for extensible contracts
- Avoid `any` - use `unknown` when type is truly unknown

**Styling Standards**:

- Use Tailwind CSS utility classes for styling
- Component-specific styles in component files (no separate CSS modules)
- Global styles in `styles/` directory
- Follow shadcn/ui component patterns for consistency

**Example Feature Module**:

```text
features/students/
├── components/
│   ├── add-student-modal.tsx
│   ├── edit-student-modal.tsx
│   ├── view-student-modal.tsx
│   ├── enroll-student-modal.tsx
│   └── transfer-student-modal.tsx
├── columns.tsx              # TanStack Table column definitions
└── index.tsx                # Student list page with data table
```

**Rationale**:
- Feature-based architecture promotes modularity and independent development
- Context providers centralize cross-cutting concerns (theme, i18n, layout)
- TanStack Router + Query provide type-safe routing and data fetching
- Zod validation ensures runtime type safety at API boundaries
- Tailwind + shadcn/ui enable rapid, consistent UI development

### Backend Implementation Standards

All Spring Boot services MUST adhere to the following architectural patterns:

**Package Structure** (per service):
```text
com.sms.{service-name}/
├── config/           # Spring configuration beans (SecurityConfig, OpenAPIConfig, etc.)
├── controller/       # REST API endpoints (@RestController)
├── dto/              # Data Transfer Objects (request/response models)
├── exception/        # Custom exceptions and GlobalExceptionHandler
├── model/            # JPA entities (@Entity)
├── repository/       # Spring Data JPA repositories
├── security/         # Authentication/authorization filters
├── service/          # Business logic layer (@Service)
│   ├── interfaces/   # Service interface definitions (I-prefixed)
│   │   ├── IStudentService.java
│   │   ├── IEnrollmentService.java
│   │   └── I{Domain}Service.java
│   ├── StudentService.java       # Implementation (no "Impl" suffix)
│   ├── EnrollmentService.java
│   └── {Domain}Service.java
└── validation/       # Custom validators (@Component)
```

**Service Layer Architecture**:

All services MUST follow this interface-implementation pattern:

1. **Interface Definition** (in `service/interfaces/`):
   - Prefix interface name with `I` (e.g., `IStudentService`, `IEnrollmentService`)
   - Define all public service methods
   - Include comprehensive JavaDoc with parameter descriptions, return types, and exceptions
   - Located in `service/interfaces/` subdirectory

2. **Implementation** (in `service/`):
   - Implementation class name matches domain without suffix (e.g., `StudentService`, NOT `StudentServiceImpl`)
   - Implements the corresponding interface (e.g., `StudentService implements IStudentService`)
   - Annotated with `@Service`
   - Located directly in `service/` directory (NOT in a separate `impl/` subdirectory)

**Example**:
```java
// service/interfaces/IStudentService.java
public interface IStudentService {
    /**
     * Create a new student profile.
     * @param request Student creation request
     * @return Created student response
     * @throws InvalidStudentDataException if validation fails
     */
    StudentResponse createStudent(StudentRequest request);
}

// service/StudentService.java
@Service
@RequiredArgsConstructor
public class StudentService implements IStudentService {
    @Override
    public StudentResponse createStudent(StudentRequest request) {
        // Implementation
    }
}
```

**Rationale**:
- I-prefix interfaces clearly distinguish contracts from implementations
- Removing "Impl" suffix reduces verbosity while maintaining clarity
- Flat structure in `service/` avoids unnecessary nesting
- Interfaces in subdirectory keep contract definitions organized separately

**Dependency Injection**:
- Use constructor injection with `@RequiredArgsConstructor` (Lombok)
- NEVER use field injection (`@Autowired` on fields)

**Entity Design**:
- Use Lombok annotations: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- Primary keys: UUID with `@GeneratedValue(strategy = GenerationType.UUID)`
- Timestamps: `@CreationTimestamp` and `@UpdateTimestamp` for audit fields
- Table names: snake_case plural (e.g., `users`, `login_attempts`)

**Validation**:
- Use Jakarta Bean Validation on DTOs (`@Valid` in controllers)
- Custom validators in `validation/` package implementing `ConstraintValidator`
- Field-level annotations: `@NotBlank`, `@Email`, `@Pattern`, etc.

**Transaction Management**:
- Service methods modifying data MUST have `@Transactional`
- Read-only operations SHOULD use `@Transactional(readOnly = true)` for optimization

**API Documentation**:
- Use Swagger/OpenAPI annotations: `@Tag`, `@Operation` on controllers
- Expose docs at `/swagger-ui.html` via springdoc-openapi

## Security & Compliance

### Data Classification

| Level | Examples | Requirements |
|-------|----------|--------------|
| **Confidential** | Student grades, attendance records, contact info | Encrypted at rest, access logging, role-restricted |
| **Internal** | Class schedules, teacher assignments | Authentication required, audit trail |
| **Public** | School name, academic calendar | No restrictions |

### Compliance Requirements

- **MoEYS Standards**: Grade calculations and report formats MUST follow Cambodia Ministry guidelines
- **Data Retention**: Student records MUST be retained per MoEYS requirements
- **Access Audit**: Admin actions on student data MUST be auditable for 7 years

### Security Practices

- All passwords hashed with BCrypt (cost factor 12+)
- JWT access tokens expire within 24 hours; refresh tokens within 30 days
- Refresh token rotation on each use (one-time use tokens)
- API rate limiting: 100 requests/minute per user
- SQL injection prevention via parameterized queries (JPA/Hibernate)
- XSS prevention via React's default escaping and CSP headers

## Code Quality

### Review Requirements

- All code changes MUST be reviewed before merge
- Reviewers MUST verify:
  - Constitution principle compliance
  - Test coverage for new functionality
  - No hardcoded secrets or credentials
  - Appropriate error handling and logging

### Quality Gates

- **Compilation**: Code MUST compile without errors
- **Tests**: All tests MUST pass
- **Linting**: Code MUST pass ESLint (frontend) and Checkstyle (backend)
- **Security scan**: No high/critical vulnerabilities in dependencies

### Documentation

- Public APIs MUST have OpenAPI/Swagger documentation
- Complex business logic MUST have inline comments explaining "why"
- README files MUST be updated when setup procedures change

## Deployment

### Container Requirements

- Every service MUST have a Dockerfile
- Images MUST be built from official base images (eclipse-temurin for Java, node for frontend)
- Production images MUST NOT include development dependencies
- Health checks MUST be configured in Docker Compose

### Environment Management

- Configuration via environment variables (not hardcoded)
- Separate configurations for: development, staging, production
- Secrets MUST NOT be committed to version control

### Deployment Process

- All deployments via Docker Compose or equivalent orchestration
- Database migrations MUST be backward compatible (no breaking changes without migration plan)
- Rollback procedure MUST be documented and tested

### Monitoring

- All production services MUST expose health endpoints
- Log aggregation MUST be configured for distributed debugging
- Alerting MUST be configured for service downtime and error rate spikes

## Governance

### Constitution Authority

This constitution supersedes all other development practices for the SMS project. When conflicts arise between this document and other guidelines, this constitution takes precedence.

### Amendment Process

1. Propose change via pull request to this document
2. Document rationale and impact assessment
3. Review by project maintainers
4. Update version according to semantic versioning:
   - **MAJOR**: Removing or fundamentally changing a principle
   - **MINOR**: Adding new principles or sections
   - **PATCH**: Clarifications, typo fixes, non-behavioral changes
5. Update all dependent templates and documentation

### Compliance Verification

- All PRs MUST pass the Constitution Check in the implementation plan
- Complexity additions MUST be documented in the Complexity Tracking table
- Periodic audits SHOULD verify ongoing compliance

### Guidance Documents

For runtime development guidance, refer to:
- `README.md` - Project setup and quick start
- `.specify/` - Feature specifications and implementation plans
- `CLAUDE.md` - Auto-generated development guidelines (updated per feature)

**Version**: 1.4.0 | **Ratified**: 2025-11-20 | **Last Amended**: 2025-12-02
