# Implementation Plan: Microservice Architecture Standardization

**Branch**: `001-service-standards` | **Date**: 2025-11-22 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-service-standards/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

This feature standardizes microservice architecture across all SMS services based on comprehensive analysis in SERVICE_COMPARISON_ANALYSIS.md. The goal is to establish auth-service as the architectural template, eliminate configuration inconsistencies (profiles, environment variables, package structures), and create reusable standards for future services. The standardization focuses on improving developer onboarding, deployment reliability, and maintenance efficiency without disrupting production or requiring immediate migration of existing services.

**Technical Approach**: Document-driven standardization using compliance checklists, automated validation scripts, and template-based service creation. Standards are enforced through code review, pre-commit hooks, and CI/CD gates. Migration is phased: enforce standards for new services immediately, defer existing service migration until core features are complete.

## Technical Context

**Language/Version**: Java 21 (Spring Boot 3.5.7), Bash scripting for automation
**Primary Dependencies**: Spring Boot, Spring Security, Spring Data JPA, Netflix Eureka, Docker Compose
**Storage**: N/A (this is a standardization/documentation feature, not a data feature)
**Testing**: Compliance validation scripts, structure verification tests, deployment smoke tests
**Target Platform**: Docker-based microservices running on Linux containers
**Project Type**: Multi-service monorepo (microservices architecture)
**Performance Goals**: Service creation time < 2 hours, developer navigation time < 30 minutes
**Constraints**: No breaking changes to existing production services, backward compatibility during migration
**Scale/Scope**: 8 planned microservices (2 existing: auth, student; 6 future: attendance, grade, report, notification, api-gateway, eureka-server)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Principle Compliance

| Principle | Compliance Status | Notes |
|-----------|------------------|-------|
| **I. Microservices-First** | ✅ **PASS** | This feature enforces service boundary standards and configuration isolation. Does not violate service independence. |
| **II. Security-First** | ✅ **PASS** | Standardizes JWT architecture (separate Filter + Provider), enforces security configuration classes (SecurityConfig, CorsConfig). Improves security posture through consistency. |
| **III. Simplicity (YAGNI)** | ✅ **PASS** | Simplifies by reducing configuration variations. Uses existing Spring Boot conventions instead of custom patterns. No new infrastructure or services added. |
| **IV. Observability** | ✅ **PASS** | Standardizes health endpoint configuration and logging patterns. Does not reduce observability. |
| **V. Test Discipline** | ✅ **PASS** | Standardization enables better test reuse. Compliance validation tests will be added. |
| **VI. Backend API Conventions** | ✅ **PASS** | This feature does not change API response formats. Existing ApiResponse<T> pattern is maintained. |

### Project Structure Alignment

| Requirement | Compliance Status | Notes |
|------------|------------------|-------|
| **Monorepo layout** | ✅ **PASS** | Feature operates within existing monorepo structure, affects service subdirectories only. |
| **Service independence** | ✅ **PASS** | Each service remains independently deployable. Standardization applies to structure, not shared code. |
| **Feature numbering** | ✅ **PASS** | This feature is `001-service-standards`, follows zero-padded 3-digit convention. |
| **Backend package structure** | ✅ **ENFORCES** | This feature defines and enforces the standard package structure: config/, controller/, dto/, exception/, model/, repository/, security/, service/, validation/. |

### Technology Stack Compliance

| Stack Component | Compliance Status | Notes |
|----------------|------------------|-------|
| **Spring Boot 3.5+** | ✅ **PASS** | Standards based on Spring Boot 3.5.7 conventions. |
| **Java 21+** | ✅ **PASS** | All services use Java 21. |
| **PostgreSQL per service** | ✅ **PASS** | Database isolation maintained, standardizes connection configuration. |
| **Docker Compose** | ✅ **PASS** | Standardizes Docker environment variable naming. |
| **Netflix Eureka** | ✅ **PASS** | Enforces hostname-based registration (prefer-ip-address: false). |

### Overall Gate Status: ✅ **PASS - Proceed to Phase 0**

**Rationale**: This feature aligns with all constitution principles. It enforces existing standards rather than introducing complexity. No violations to justify.

## Project Structure

### Documentation (this feature)

```text
specs/001-service-standards/
├── spec.md                      # Feature specification (COMPLETE)
├── plan.md                      # This implementation plan (IN PROGRESS)
├── research.md                  # Phase 0: Standards research and best practices (PENDING)
├── data-model.md                # Phase 1: Standardization artifact models (PENDING)
├── quickstart.md                # Phase 1: Developer guide for creating standardized services (PENDING)
├── contracts/                   # Phase 1: Compliance validation contracts (PENDING)
│   ├── compliance-checklist.md # Service compliance verification checklist
│   ├── validation-script.sh    # Automated compliance validation script
│   └── service-template.md     # Template service structure documentation
└── tasks.md                     # Phase 2: Task breakdown (generated by /speckit.tasks)
```

### Source Code (repository root)

This feature affects the following parts of the monorepo:

```text
salarean/
├── auth-service/                # TEMPLATE SERVICE (reference implementation)
│   ├── src/main/java/com/sms/auth/
│   │   ├── config/              # ✅ Standard: CorsConfig, OpenAPIConfig, SecurityConfig
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── exception/
│   │   ├── model/               # ✅ Standard package name (not 'entity')
│   │   ├── repository/
│   │   ├── security/            # ✅ Standard: JWT classes in security/ package
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── JwtTokenProvider.java
│   │   ├── service/             # ✅ Standard: Implementations in service/ (not service/impl/)
│   │   └── validation/
│   └── src/main/resources/
│       ├── application.yml      # ✅ Standard: Default profile only
│       └── application-docker.yml # ✅ Standard: Docker profile only
│
├── student-service/             # NEEDS MIGRATION (deferred)
│   ├── src/main/java/com/sms/student/
│   │   ├── config/
│   │   │   └── JwtAuthenticationFilter.java  # ❌ Should be in security/
│   │   ├── entity/              # ❌ Should be 'model/'
│   │   ├── service/impl/        # ❌ Should be flattened into service/
│   │   └── ...
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-dev.yml  # ❌ Extra profile, should be removed
│       ├── application-docker.yml # ⚠️ Exists but not used
│       └── application-prod.yml # ❌ Should be application-docker.yml
│
├── {future-service}/            # NEW SERVICES (must follow standards from day 1)
│   └── [Copy structure from auth-service template]
│
├── docker-compose.yml           # NEEDS UPDATES
│   # Standardize environment variable naming:
│   # - SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
│   # - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
│   # - Remove EUREKA_INSTANCE_* env vars (move to YAML)
│
├── .standards/                  # NEW: Standardization artifacts directory
│   ├── templates/
│   │   └── service-template/    # Cookiecutter or manual template
│   ├── scripts/
│   │   ├── validate-service-structure.sh  # Automated compliance checker
│   │   └── create-service.sh    # Service scaffolding script
│   └── checklists/
│       └── service-compliance.md # Compliance checklist template
│
└── SERVICE_COMPARISON_ANALYSIS.md # ✅ Authoritative reference document
```

**Structure Decision**: This feature operates at the **repository root level**, affecting multiple microservices. It introduces a new `.standards/` directory for templates, validation scripts, and checklists. The standardization is enforced through documentation, automation, and code review rather than shared code libraries (maintaining microservice independence per Constitution Principle I).

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

*No violations - this table is empty.*

This feature reduces complexity by eliminating configuration variations and standardizing patterns. It does not introduce new services, infrastructure components, or abstraction layers.

## Phase 0: Research & Standards Definition

**Prerequisites**: Constitution Check passed ✅

**Objective**: Research Spring Boot best practices, validate standardization decisions from SERVICE_COMPARISON_ANALYSIS.md, and document rationale for each standard.

### Research Tasks

1. **Spring Boot Configuration Best Practices**
   - Research: Official Spring Boot documentation on profile usage
   - Research: Spring Boot property naming conventions (SPRING_DATASOURCE_* vs custom)
   - Decision needed: Confirm 2-profile standard (default, docker) vs multi-environment profiles

2. **Eureka Service Discovery Patterns**
   - Research: Netflix Eureka documentation on hostname vs IP-based registration
   - Research: Docker networking best practices with Eureka
   - Decision needed: Validate `prefer-ip-address: false` for Docker deployments

3. **Package Structure Standards**
   - Research: Spring Boot official examples and conventions
   - Research: Domain-Driven Design package organization
   - Decision needed: Confirm `model/` vs `entity/` package naming
   - Decision needed: Confirm `security/` package for JWT classes vs `config/`

4. **JWT Security Architecture**
   - Research: Spring Security best practices for JWT filters
   - Research: Separation of concerns in authentication
   - Decision needed: Validate Filter + Provider separation pattern

5. **OpenAPI/Swagger Configuration**
   - Research: Springdoc OpenAPI best practices
   - Research: API Gateway integration patterns with OpenAPI
   - Decision needed: Confirm server URL should point to API Gateway

6. **Service Template Creation**
   - Research: Cookiecutter, Spring Initializr, or manual templates
   - Research: Service scaffolding best practices
   - Decision needed: Choose templating approach for new services

7. **Compliance Validation Automation**
   - Research: Shell scripting for directory structure validation
   - Research: CI/CD integration for compliance checks
   - Decision needed: Design automated validation script architecture

### Deliverable: research.md

Document all findings in `research.md` with the following structure:

```markdown
# Standardization Research: Microservice Architecture

## Decision 1: Spring Profile Strategy

**Question**: How many profiles should each service have?

**Research Findings**:
- Spring Boot documentation recommends...
- Industry best practices show...
- SERVICE_COMPARISON_ANALYSIS.md shows auth-service uses...

**Decision**: Use exactly 2 profiles (default, docker)

**Rationale**: [Explain why]

**Alternatives Considered**:
- Multi-environment profiles (dev, test, prod) - Rejected because...
- Single profile with all env vars - Rejected because...

## Decision 2: Environment Variable Naming
[Continue for all research areas...]
```

## Phase 1: Design & Documentation

**Prerequisites**: `research.md` complete with all decisions documented

**Objective**: Create standardization artifacts (data models, contracts, templates) and developer guides.

### 1.1 Data Model Design

**File**: `data-model.md`

While this feature doesn't involve database entities, we need to model the standardization artifacts:

```markdown
# Standardization Artifact Models

## Service Template Structure

**Description**: Reference structure that all services must follow

**Attributes**:
- Package structure (config/, controller/, dto/, exception/, model/, repository/, security/, service/, validation/)
- Configuration files (application.yml, application-docker.yml)
- Required configuration classes (CorsConfig, OpenAPIConfig, SecurityConfig)
- JWT architecture (JwtAuthenticationFilter, JwtTokenProvider)

**Validation Rules**:
- Exactly 2 profile files
- JWT classes in security/ package
- Entities in model/ package (not entity/)
- Service implementations in service/ package (not service/impl/)

## Configuration Profile Model

**Description**: Standard environment-specific configuration structure

**Attributes**:
- Profile name (default, docker)
- Environment variable references (SPRING_DATASOURCE_*, EUREKA_CLIENT_SERVICEURL_DEFAULTZONE)
- Eureka instance configuration (hostname, prefer-ip-address: false)

**Validation Rules**:
- No custom variable names (DB_*, EUREKA_CLIENT_SERVICE_URL)
- Eureka instance config in YAML, not env vars

## Compliance Checklist Model

**Description**: Validation criteria for service compliance

**Attributes**:
- Profile count (must be 2)
- Package structure correctness
- Required configuration class presence
- Environment variable naming compliance
- JWT architecture correctness

**Pass Criteria**: All checks must pass for 100% compliance
```

### 1.2 Contracts & Validation

**Directory**: `contracts/`

Create compliance validation contracts:

#### `contracts/compliance-checklist.md`

```markdown
# Service Compliance Checklist

## Profile Configuration
- [ ] Exactly 2 profiles (application.yml, application-docker.yml)
- [ ] No extra profiles (application-dev.yml, application-prod.yml, application-test.yml)
- [ ] docker-compose.yml uses SPRING_PROFILES_ACTIVE=docker

## Environment Variable Naming
- [ ] Database: SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
- [ ] Eureka: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
- [ ] No custom variable names (DB_USERNAME, DB_PASSWORD, EUREKA_CLIENT_SERVICE_URL)

## Package Structure
- [ ] Entities in model/ package (not entity/)
- [ ] JWT classes in security/ package (not config/)
- [ ] Service implementations in service/ package (not service/impl/)
- [ ] All standard packages present: config/, controller/, dto/, exception/, model/, repository/, security/, service/

## JWT Architecture
- [ ] JwtAuthenticationFilter in security/ package
- [ ] JwtTokenProvider in security/ package
- [ ] Filter extends OncePerRequestFilter
- [ ] Provider handles token generation, parsing, validation

## Required Configuration Classes
- [ ] CorsConfig.java present
- [ ] OpenAPIConfig.java present (correct capitalization)
- [ ] SecurityConfig.java present

## OpenAPI Configuration
- [ ] Server URL points to API Gateway (http://localhost:8080)
- [ ] Server description is "API Gateway"

## Eureka Configuration
- [ ] prefer-ip-address: false
- [ ] hostname set to service name
- [ ] Configuration in YAML, not environment variables
```

#### `contracts/validation-script.sh`

```bash
#!/bin/bash
# Service compliance validation script
# Usage: ./validation-script.sh <service-directory>

SERVICE_DIR=$1
ERRORS=0

echo "Validating service: $SERVICE_DIR"

# Check profile count
PROFILE_COUNT=$(find "$SERVICE_DIR/src/main/resources" -name "application*.yml" | wc -l)
if [ "$PROFILE_COUNT" -ne 2 ]; then
  echo "❌ Profile count: Expected 2, found $PROFILE_COUNT"
  ERRORS=$((ERRORS+1))
fi

# Check package structure
if [ ! -d "$SERVICE_DIR/src/main/java/com/sms/*/model" ]; then
  echo "❌ Package structure: model/ package not found"
  ERRORS=$((ERRORS+1))
fi

if [ ! -d "$SERVICE_DIR/src/main/java/com/sms/*/security" ]; then
  echo "❌ Package structure: security/ package not found"
  ERRORS=$((ERRORS+1))
fi

# Check JWT classes
if ! find "$SERVICE_DIR/src/main/java/com/sms/*/security" -name "JwtAuthenticationFilter.java" | grep -q .; then
  echo "❌ JWT architecture: JwtAuthenticationFilter.java not in security/ package"
  ERRORS=$((ERRORS+1))
fi

if ! find "$SERVICE_DIR/src/main/java/com/sms/*/security" -name "JwtTokenProvider.java" | grep -q .; then
  echo "❌ JWT architecture: JwtTokenProvider.java not in security/ package"
  ERRORS=$((ERRORS+1))
fi

# Check required config classes
if ! find "$SERVICE_DIR/src/main/java/com/sms/*/config" -name "CorsConfig.java" | grep -q .; then
  echo "❌ Configuration classes: CorsConfig.java not found"
  ERRORS=$((ERRORS+1))
fi

if ! find "$SERVICE_DIR/src/main/java/com/sms/*/config" -name "OpenAPIConfig.java" | grep -q .; then
  echo "❌ Configuration classes: OpenAPIConfig.java not found"
  ERRORS=$((ERRORS+1))
fi

if [ "$ERRORS" -eq 0 ]; then
  echo "✅ All checks passed"
  exit 0
else
  echo "❌ $ERRORS checks failed"
  exit 1
fi
```

#### `contracts/service-template.md`

```markdown
# Service Template Structure

This document defines the exact structure that all SMS microservices must follow.

## Directory Structure

[Full directory tree with explanations]

## Required Files Checklist

[List of mandatory files with descriptions]

## Configuration File Templates

[Example application.yml and application-docker.yml with explanations]

## Java Class Templates

[Example configuration classes with annotations and patterns]
```

### 1.3 Developer Quickstart Guide

**File**: `quickstart.md`

```markdown
# Quickstart: Creating a Standardized Microservice

## Prerequisites
- Java 21+ installed
- Maven 3.8+ installed
- Docker and Docker Compose installed
- auth-service template available

## Step 1: Copy Template Service

bash
cp -r auth-service new-service
cd new-service


## Step 2: Rename Package

bash
# Replace all occurrences of com.sms.auth with com.sms.newservice
find . -type f -name "*.java" -exec sed -i '' 's/com.sms.auth/com.sms.newservice/g' {} +


## Step 3: Update Configuration

1. Edit `src/main/resources/application.yml`:
   - Update spring.application.name
   - Update server.port
   - Update database connection (if applicable)

2. Edit `src/main/resources/application-docker.yml`:
   - Update datasource URL
   - Update eureka.instance.hostname

3. Edit `pom.xml`:
   - Update artifactId, name, description

## Step 4: Update Docker Compose

Add service to `docker-compose.yml`:

yaml
new-service:
  build: ./new-service
  container_name: new-service
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-new:5432/new_db
    - SPRING_DATASOURCE_USERNAME=sms_user
    - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    - JWT_SECRET=${JWT_SECRET}


## Step 5: Validate Compliance

bash
.standards/scripts/validate-service-structure.sh new-service


## Step 6: Build and Test

bash
cd new-service
./mvnw clean package
docker-compose up new-service


## Compliance Checklist

Use `.standards/checklists/service-compliance.md` to verify all standards are met.
```

### 1.4 Agent Context Update

**Action**: Run `.specify/scripts/bash/update-agent-context.sh claude`

This will update the CLAUDE.md file with standardization patterns and guidelines extracted from this feature.

**Expected Update**:
- Add microservice architecture standards section
- Add service template creation guide
- Add compliance validation instructions
- Add reference to SERVICE_COMPARISON_ANALYSIS.md

## Phase 2: Implementation Planning (Post-Design)

**Note**: This phase is completed by the `/speckit.tasks` command, not `/speckit.plan`. The plan command stops here.

Phase 2 will include:
- Creating `.standards/` directory structure
- Writing validation scripts
- Documenting compliance checklists
- Creating service template
- Updating CLAUDE.md with standards
- (Optional) Migrating student-service as validation of standards

## Constitution Check (Post-Design)

*Re-evaluating after Phase 1 design artifacts are complete*

| Principle | Post-Design Status | Changes |
|-----------|-------------------|---------|
| **I. Microservices-First** | ✅ **PASS** | Design maintains service independence. Template approach prevents code sharing violations. |
| **II. Security-First** | ✅ **PASS** | Design enforces security configuration classes and JWT architecture separation. |
| **III. Simplicity (YAGNI)** | ✅ **PASS** | Design uses simple bash scripts and markdown checklists. No complex tooling added. |
| **IV. Observability** | ✅ **PASS** | Design maintains health endpoint standards. |
| **V. Test Discipline** | ✅ **PASS** | Validation script acts as automated test for compliance. |
| **VI. Backend API Conventions** | ✅ **PASS** | Design does not affect API response formats. |

**Overall Status**: ✅ **PASS - Ready for Phase 2 (tasks.md generation)**

## Success Metrics

These metrics will be used to validate the standardization feature:

1. **Service Creation Time**: New service from template to deployment < 2 hours
   - Measure: Time-track creating a new service using template

2. **Compliance Pass Rate**: 100% of new services pass compliance checklist
   - Measure: Run validation script on all new services

3. **Developer Navigation Time**: < 30 minutes to locate code in unfamiliar service
   - Measure: Developer study with navigation tasks

4. **Deployment Consistency**: Zero configuration-related deployment failures
   - Measure: Deployment success rate for standardized services

5. **Code Review Efficiency**: 75% reduction in architecture-related review comments
   - Measure: Compare review comments before/after standardization

## Migration Strategy

**Immediate (Phase 2 - tasks.md execution)**:
1. Create `.standards/` directory with templates and scripts
2. Document compliance checklist
3. Validate auth-service as reference template
4. Update CLAUDE.md with standardization guidelines

**Short-term (New services)**:
- All new services MUST use template and pass validation
- Enforce via code review and CI/CD gates

**Long-term (Existing services)**:
- student-service migration: After core features complete
- Detailed migration plan in separate feature specification

## Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Validation script false positives | Medium | Low | Thorough testing, manual override option |
| Template becomes outdated | Medium | Medium | Designate template owner, regular reviews |
| Developer non-compliance | Low | Medium | Code review enforcement, CI/CD gates |
| Spring Boot version updates break patterns | Low | High | Document Spring Boot version in standards |

## Dependencies

**External**:
- SERVICE_COMPARISON_ANALYSIS.md (authoritative reference)
- auth-service (template service must remain stable)
- Spring Boot 3.5.7 conventions

**Internal**:
- Constitution compliance
- Existing docker-compose.yml structure
- Git branching workflow

## Deliverables Summary

| Phase | Deliverable | Status |
|-------|------------|--------|
| Phase 0 | research.md | PENDING |
| Phase 1 | data-model.md | PENDING |
| Phase 1 | contracts/compliance-checklist.md | PENDING |
| Phase 1 | contracts/validation-script.sh | PENDING |
| Phase 1 | contracts/service-template.md | PENDING |
| Phase 1 | quickstart.md | PENDING |
| Phase 1 | CLAUDE.md update (via script) | PENDING |
| Phase 2 | tasks.md (via /speckit.tasks) | PENDING |
| Phase 2 | .standards/ directory implementation | PENDING |

## Next Steps

1. **Complete Phase 0**: Execute research tasks and generate `research.md`
2. **Complete Phase 1**: Generate all design artifacts (data-model.md, contracts/, quickstart.md)
3. **Update agent context**: Run update-agent-context.sh script
4. **Generate tasks**: Run `/speckit.tasks` to create tasks.md
5. **Execute implementation**: Follow tasks.md to build standardization artifacts
