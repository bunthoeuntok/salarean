<!--
SYNC IMPACT REPORT
==================
Version change: N/A → 1.0.0 (Initial constitution)
Modified principles: None (new document)
Added sections:
  - Core Principles (5 principles)
  - Technology Stack
  - Security & Compliance
  - Code Quality
  - Deployment
  - Governance
Removed sections: None
Templates requiring updates:
  - .specify/templates/plan-template.md ✅ Compatible (Constitution Check section present)
  - .specify/templates/spec-template.md ✅ Compatible (Requirements align with principles)
  - .specify/templates/tasks-template.md ✅ Compatible (Phase structure supports principles)
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

## Technology Stack

The following technologies are mandated for the SMS project:

### Frontend
- **Framework**: Next.js 14 with React Server Components
- **Language**: TypeScript (strict mode)
- **Styling**: TailwindCSS
- **State**: Zustand for client state, React Query for server state

### Backend
- **Framework**: Spring Boot 3.2+
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

- All passwords hashed with bcrypt (cost factor 12+)
- JWT tokens expire within 24 hours; refresh tokens within 7 days
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

**Version**: 1.0.0 | **Ratified**: 2025-11-20 | **Last Amended**: 2025-11-20
