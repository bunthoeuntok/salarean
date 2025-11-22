# SMS Microservice Architecture Standards

**Version**: 1.0.0
**Last Updated**: 2025-11-22
**Status**: Active

---

## Overview

This directory contains the official architectural standards, templates, validation tools, and documentation for all microservices in the Student Management System (SMS) project.

**Purpose**: Ensure consistency, maintainability, and quality across all microservices by providing:
- **Standards Documentation**: Clear guidelines for package structure, naming conventions, and configuration patterns
- **Reusable Templates**: Production-ready code templates for configuration classes and service structure
- **Validation Tools**: Automated scripts to verify service compliance with architectural standards
- **Developer Guides**: Step-by-step instructions for creating, maintaining, and migrating services

---

## Directory Structure

```
.standards/
├── README.md                    # This file - overview and usage guide
├── CHANGELOG.md                 # Decision history and rationale
├── checklists/                  # Compliance checklists
│   └── service-compliance.md    # Service compliance verification checklist
├── scripts/                     # Automation scripts
│   ├── validate-service-structure.sh    # Automated compliance validation
│   ├── validate-all-services.sh         # Batch validation for all services
│   ├── create-service.sh                # Service creation automation
│   ├── find-component.sh                # Component locator helper
│   └── smoke-test-deployment.sh         # Deployment testing
├── templates/                   # Service templates and code examples
│   ├── README.md                        # Template usage guide
│   ├── service-template.md              # Complete service structure documentation
│   ├── application.yml                  # Configuration template (default profile)
│   ├── application-docker.yml           # Configuration template (docker profile)
│   ├── docker-compose-service.yml       # Docker Compose service definition
│   ├── Dockerfile                       # Container image template
│   ├── pom-template.xml                 # Maven dependencies template
│   └── java/                            # Java class templates
│       ├── CorsConfig.java
│       ├── OpenAPIConfig.java
│       ├── SecurityConfig.java
│       ├── JwtAuthenticationFilter.java
│       └── JwtTokenProvider.java
├── docs/                        # Comprehensive documentation
│   ├── package-structure.md             # Standard package layout
│   ├── naming-conventions.md            # Naming standards
│   ├── configuration-patterns.md        # Configuration classes guide
│   ├── jwt-architecture.md              # JWT Filter + Provider pattern
│   ├── navigation-guide.md              # Service navigation guide
│   ├── environment-variables.md         # Environment variable standards
│   ├── profile-strategy.md              # Spring profile strategy
│   ├── eureka-configuration.md          # Service discovery configuration
│   ├── reusable-components.md           # Reusable component guide
│   ├── common-locations.md              # Component location mapping
│   ├── quickstart-service-creation.md   # Service creation quickstart
│   ├── maintenance-metrics.md           # Maintenance tracking
│   ├── migration-guide.md               # Migration guide for existing services
│   ├── troubleshooting.md               # Common issues and solutions
│   ├── FAQ.md                           # Frequently asked questions
│   └── version-history.md               # Standards version history
├── validation-reports/          # Compliance validation results
│   ├── auth-service.md
│   └── student-service.md
└── reports/                     # Summary reports
    └── compliance-summary-2025-11-22.md
```

---

## Quick Start

### For New Developers

**Goal**: Understand and navigate any microservice in the codebase within 30 minutes

1. **Read the Navigation Guide**: Start with [`docs/navigation-guide.md`](docs/navigation-guide.md) to understand the standard service structure
2. **Review Package Structure**: Read [`docs/package-structure.md`](docs/package-structure.md) to learn where different components live
3. **Study Naming Conventions**: Check [`docs/naming-conventions.md`](docs/naming-conventions.md) for class and file naming patterns
4. **Explore a Real Service**: Navigate through `auth-service` (the reference template) using the documentation

**Success Metric**: You should be able to locate equivalent components across any service within 5 minutes.

### For Creating a New Service

**Goal**: Create a compliant microservice in under 2 hours

1. **Read the Quickstart Guide**: Follow [`docs/quickstart-service-creation.md`](docs/quickstart-service-creation.md) step-by-step
2. **Use the Automation Script** (recommended):
   ```bash
   .standards/scripts/create-service.sh --name attendance --port 8084
   ```
3. **Or Copy Manually** from the template service:
   ```bash
   cp -r auth-service new-service
   # Follow manual setup steps in docs/quickstart-service-creation.md
   ```
4. **Validate Compliance**:
   ```bash
   .standards/scripts/validate-service-structure.sh new-service
   ```

**Success Metric**: Service passes 100% of compliance checks on first validation run.

### For Deploying Services

**Goal**: Zero deployment failures from configuration inconsistencies

1. **Review Configuration Standards**: Read [`docs/environment-variables.md`](docs/environment-variables.md) and [`docs/profile-strategy.md`](docs/profile-strategy.md)
2. **Use Standard Templates**: Copy configuration from [`templates/application-docker.yml`](templates/application-docker.yml)
3. **Follow Profile Strategy**: Ensure exactly 2 profiles (default, docker) exist
4. **Run Smoke Test**:
   ```bash
   .standards/scripts/smoke-test-deployment.sh
   ```

**Success Metric**: All services start successfully and register with Eureka.

### For Maintaining Services

**Goal**: Reduce cross-service maintenance time by 50%

1. **Locate Components Quickly**:
   ```bash
   .standards/scripts/find-component.sh JwtTokenProvider
   ```
2. **Use Common Locations Guide**: Reference [`docs/common-locations.md`](docs/common-locations.md) to find where specific features are implemented
3. **Follow Refactoring Checklist**: Use [`docs/refactoring-checklist.md`](docs/refactoring-checklist.md) when making changes across services
4. **Validate After Changes**:
   ```bash
   .standards/scripts/validate-all-services.sh
   ```

**Success Metric**: Locate and fix the same bug across all services in half the time.

---

## Core Standards

### 1. Package Structure Standard

All services MUST follow this package layout:

```
com.sms.{service-name}/
├── config/          # Spring configuration beans (CORS, OpenAPI, Security)
├── controller/      # REST API endpoints (@RestController)
├── dto/             # Data Transfer Objects (request/response)
├── exception/       # Custom exceptions and global exception handler
├── model/           # JPA entities (@Entity) - NOT "entity" package
├── repository/      # Spring Data JPA repositories
├── security/        # Authentication/authorization (JWT Filter, JWT Provider)
├── service/         # Business logic (@Service) - implementations directly in service/
└── validation/      # Custom validators (optional)
```

**Reference**: [`docs/package-structure.md`](docs/package-structure.md)

### 2. Configuration Profile Strategy

All services MUST have exactly **2 Spring profiles**:

1. **default** (`application.yml`) - Local development
2. **docker** (`application-docker.yml`) - Containerized deployment

**Docker Compose** MUST use: `SPRING_PROFILES_ACTIVE=docker`

**Reference**: [`docs/profile-strategy.md`](docs/profile-strategy.md)

### 3. Environment Variable Naming

All services MUST use Spring Boot standard variable names:

- **Database**: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- **Eureka**: `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`
- **Security**: `JWT_SECRET`

❌ **Forbidden**: Custom variable names like `DB_USERNAME`, `DB_PASSWORD`, `EUREKA_CLIENT_SERVICE_URL`

**Reference**: [`docs/environment-variables.md`](docs/environment-variables.md)

### 4. Eureka Service Discovery

All services MUST configure Eureka with:

```yaml
eureka:
  instance:
    hostname: {service-name}
    prefer-ip-address: false  # MUST be false for Docker deployments
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

**Reference**: [`docs/eureka-configuration.md`](docs/eureka-configuration.md)

### 5. JWT Architecture

JWT functionality MUST be separated into exactly 2 classes in the `security/` package:

1. **JwtAuthenticationFilter.java** - Request filtering logic
2. **JwtTokenProvider.java** - Token operations (generation, parsing, validation)

**Reference**: [`docs/jwt-architecture.md`](docs/jwt-architecture.md)

### 6. Required Configuration Classes

All services MUST include these configuration classes in the `config/` package:

- **CorsConfig.java** - Cross-origin request handling
- **OpenAPIConfig.java** - Swagger/OpenAPI documentation (note: "OpenAPI" not "OpenApi")
- **SecurityConfig.java** - Spring Security configuration

**Reference**: [`docs/configuration-patterns.md`](docs/configuration-patterns.md)

---

## Validation & Compliance

### Automated Validation

Run automated compliance checks on any service:

```bash
.standards/scripts/validate-service-structure.sh <service-directory>
```

**Example**:
```bash
.standards/scripts/validate-service-structure.sh auth-service
```

**Expected Output**:
```
Validating service: auth-service
✅ Profile count: 2 profiles found
✅ Package structure: model/ package found
✅ Package structure: security/ package found
✅ JWT architecture: JwtAuthenticationFilter.java in security/
✅ JWT architecture: JwtTokenProvider.java in security/
✅ Configuration classes: CorsConfig.java found
✅ Configuration classes: OpenAPIConfig.java found
✅ All checks passed
```

### Manual Checklist

For comprehensive validation, use the manual checklist:

```bash
open .standards/checklists/service-compliance.md
```

This includes 32 verification items across:
- Profile configuration (3 checks)
- Environment variable naming (5 checks)
- Package structure (6 checks)
- JWT architecture (4 checks)
- Required configuration classes (4 checks)
- OpenAPI configuration (3 checks)
- Eureka configuration (3 checks)
- Docker Compose configuration (4 checks)

---

## Reference Services

### Template Service (auth-service)

**Status**: ✅ Fully Compliant (Reference Implementation)

`auth-service` is the canonical template that all new services should follow. It demonstrates:
- Correct package structure (model/, security/, service/)
- 2-profile configuration (default, docker)
- Standard environment variable naming
- JWT architecture (Filter + Provider separation)
- Required configuration classes (CORS, OpenAPI, Security)
- Hostname-based Eureka registration

**Use auth-service as the starting point for all new services.**

### Non-Compliant Services

**student-service**: ⚠️ Requires migration (deferred until core features complete)

Known deviations:
- Uses `entity/` package instead of `model/`
- JWT classes in `config/` instead of `security/`
- Has 4 profiles instead of 2
- Uses custom env var names (DB_USERNAME, DB_PASSWORD)
- Service implementations in `service/impl/` subpackage

**Migration Guide**: [`docs/migration-guide.md`](docs/migration-guide.md)

---

## Contributing to Standards

### Proposing Changes

1. Document proposed change with rationale in a new feature spec
2. Update affected templates and documentation
3. Run validation on all services to assess impact
4. Submit pull request with updated CHANGELOG.md

### Version History

Standards follow semantic versioning (MAJOR.MINOR.PATCH):

- **MAJOR**: Fundamental architectural changes (breaking)
- **MINOR**: New standards or significant clarifications
- **PATCH**: Typo fixes, minor clarifications

Current Version: **1.0.0**

**History**: [`docs/version-history.md`](docs/version-history.md)

---

## Support & Resources

### Documentation

**Core Standards** (User Story 1: Developer Onboarding):
- **Package Structure**: [`docs/package-structure.md`](docs/package-structure.md) - Standard package layout and component organization
- **Naming Conventions**: [`docs/naming-conventions.md`](docs/naming-conventions.md) - Class, file, and variable naming standards
- **Configuration Patterns**: [`docs/configuration-patterns.md`](docs/configuration-patterns.md) - Required configuration classes (CORS, OpenAPI, Security)
- **JWT Architecture**: [`docs/jwt-architecture.md`](docs/jwt-architecture.md) - Filter + Provider pattern with examples

**Quick Start Guides**:
- **Getting Started**: [`docs/navigation-guide.md`](docs/navigation-guide.md)
- **Service Creation**: [`docs/quickstart-service-creation.md`](docs/quickstart-service-creation.md)

**Reference Documentation**:
- **Troubleshooting**: [`docs/troubleshooting.md`](docs/troubleshooting.md)
- **FAQ**: [`docs/FAQ.md`](docs/FAQ.md)

### Authoritative References

- **SERVICE_COMPARISON_ANALYSIS.md**: Detailed analysis of service inconsistencies and standardization decisions
- **Feature Specification**: `specs/001-service-standards/spec.md`
- **Implementation Plan**: `specs/001-service-standards/plan.md`

### Related Documents

- **Project Constitution**: `.specify/memory/constitution.md` - Microservices-First, Security-First, and other core principles
- **Development Guidelines**: `CLAUDE.md` - Auto-generated development standards (includes references to `.standards/`)

---

## Success Metrics

These standards are designed to achieve measurable improvements:

| Metric | Target | Measurement |
|--------|--------|-------------|
| Developer navigation time | < 30 minutes | Time to locate components in unfamiliar service |
| Service creation time | < 2 hours | Time from template to deployment |
| Cross-service bug fix time | -50% reduction | Time to fix same issue across all services |
| Deployment failures | 0 | Configuration-related deployment failures |
| New service compliance | 100% | Percentage passing compliance check |
| Code review comments | -75% reduction | Architecture-related review comments |
| Developer satisfaction | +40% improvement | Survey on codebase maintainability |
| Eureka registration | 100% hostname-based | Successful hostname registration rate |

**Tracking**: [`docs/maintenance-metrics.md`](docs/maintenance-metrics.md)

---

## License

This standardization framework is internal to the SMS project and follows the same license as the main project.

---

**Questions?** See [`docs/FAQ.md`](docs/FAQ.md) or consult the team lead.

**Found an issue?** Document it in `specs/` as a new feature or bug report.
