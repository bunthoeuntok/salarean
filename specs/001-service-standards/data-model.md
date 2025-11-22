# Standardization Artifact Models

**Feature**: Microservice Architecture Standardization
**Date**: 2025-11-22
**Purpose**: Define the conceptual models for standardization artifacts used across all microservices

---

## Overview

This document models the standardization artifacts that will be used to ensure consistency across all SMS microservices. Unlike traditional data models (database entities), these models represent structural and configurational patterns that services must conform to.

---

## 1. Service Template Structure

**Description**: The canonical reference structure that all SMS microservices must follow. This is embodied by `auth-service` and documented for replication.

**Attributes**:

| Attribute | Type | Description | Validation Rule |
|-----------|------|-------------|-----------------|
| `packageStructure` | Structure | Java package organization | Must include: config/, controller/, dto/, exception/, model/, repository/, security/, service/, validation/ |
| `profileFiles` | File[] | Spring configuration profiles | Exactly 2 files: application.yml, application-docker.yml |
| `requiredConfigs` | Class[] | Mandatory configuration classes | Must include: CorsConfig, OpenAPIConfig, SecurityConfig |
| `jwtArchitecture` | Structure | JWT authentication components | Must have: JwtAuthenticationFilter, JwtTokenProvider in security/ package |
| `serviceImplementations` | Location | Where service implementations reside | Must be in service/ package, NOT service/impl/ |
| `entityPackage` | String | Package name for JPA entities | Must be "model", NOT "entity" |

**Validation Rules**:

1. **Profile Count**: Exactly 2 YAML files in `src/main/resources/`
   - `application.yml` (default profile for local development)
   - `application-docker.yml` (docker profile for containerized deployment)

2. **Package Structure Completeness**: All standard packages must exist:
   ```
   com.sms.{service}/
   ├── config/       ✓ Required
   ├── controller/   ✓ Required
   ├── dto/          ✓ Required
   ├── exception/    ✓ Required
   ├── model/        ✓ Required (NOT 'entity')
   ├── repository/   ✓ Required
   ├── security/     ✓ Required (if service handles auth)
   ├── service/      ✓ Required
   └── validation/   ○ Optional (but recommended)
   ```

3. **JWT Class Placement**: If service implements JWT authentication:
   - `JwtAuthenticationFilter.java` MUST be in `security/` package
   - `JwtTokenProvider.java` MUST be in `security/` package
   - Both classes MUST NOT be in `config/` package

4. **Configuration Class Requirements**:
   - `CorsConfig.java` in `config/` package
   - `OpenAPIConfig.java` in `config/` package (note capitalization: "API" not "Api")
   - `SecurityConfig.java` in `config/` package

**Relationships**:
- Template is embodied by `auth-service` (1:1)
- Template is replicated by all new services (1:N)
- Template defines structure for Compliance Checklist validation (1:1)

---

## 2. Configuration Profile Model

**Description**: Standard structure for environment-specific configuration in Spring Boot microservices.

**Attributes**:

| Attribute | Type | Description | Validation Rule |
|-----------|------|-------------|-----------------|
| `profileName` | Enum | Profile identifier | Must be one of: "default", "docker" |
| `environmentVariables` | Map<String, String> | External configuration references | Must use Spring Boot standard names |
| `eurekaConfig` | Object | Service discovery settings | Instance config in YAML, NOT env vars |
| `datasourceConfig` | Object | Database connection settings | Must reference SPRING_DATASOURCE_* env vars |

**Profile Types**:

### Default Profile (`application.yml`)

**Purpose**: Local development configuration

**Required Properties**:
```yaml
spring:
  application:
    name: {service-name}
  datasource:
    url: jdbc:postgresql://localhost:5432/{service}_db
    username: ${DB_USERNAME:sms_user}
    password: ${DB_PASSWORD:password}

server:
  port: {service-specific-port}

eureka:
  client:
    enabled: false  # Disable Eureka for local dev
```

### Docker Profile (`application-docker.yml`)

**Purpose**: Containerized deployment configuration

**Required Properties**:
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

eureka:
  instance:
    hostname: {service-name}
    prefer-ip-address: false  # MUST be false
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

**Validation Rules**:

1. **Environment Variable Naming**:
   - ✅ **ALLOWED**: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
   - ✅ **ALLOWED**: `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`
   - ✅ **ALLOWED**: `JWT_SECRET`, `SPRING_REDIS_HOST`
   - ❌ **FORBIDDEN**: `DB_USERNAME`, `DB_PASSWORD`, `EUREKA_CLIENT_SERVICE_URL`

2. **Eureka Configuration Location**:
   - Instance properties (`hostname`, `prefer-ip-address`) MUST be in YAML
   - Only `defaultZone` URL should reference environment variable
   - ❌ **FORBIDDEN**: Setting `EUREKA_INSTANCE_HOSTNAME` or `EUREKA_INSTANCE_PREFER_IP_ADDRESS` as env vars

3. **Profile Activation**:
   - In `docker-compose.yml`, MUST use: `SPRING_PROFILES_ACTIVE=docker`
   - NOT "prod", "production", "docker-prod", or any other variant

**Relationships**:
- Profile Model is implemented by Service Template (2:1)
- Profile Model defines environment variables for Docker Compose (1:1)

---

## 3. Compliance Checklist Model

**Description**: Validation criteria and rules for verifying service compliance with architectural standards.

**Attributes**:

| Attribute | Type | Description | Validation Rule |
|-----------|------|-------------|-----------------|
| `checkItems` | CheckItem[] | Individual compliance checks | All must pass for 100% compliance |
| `validationScript` | Script | Automated validation executable | Must return exit code 0 for pass |
| `manualReview` | Checklist | Human-verifiable items | Used in code review process |

**Check Item Structure**:

```typescript
interface CheckItem {
  id: string;              // e.g., "PROFILE-001"
  category: string;        // e.g., "Profile Configuration"
  description: string;     // e.g., "Exactly 2 profiles"
  automated: boolean;      // Can be checked by script?
  severity: "CRITICAL" | "HIGH" | "MEDIUM" | "LOW";
  passCondition: string;   // How to verify compliance
}
```

**Check Categories**:

1. **Profile Configuration** (3 items)
   - Profile count validation
   - Profile naming validation
   - Docker compose activation validation

2. **Environment Variable Naming** (3 items)
   - Database variable validation
   - Eureka variable validation
   - Forbidden custom variable detection

3. **Package Structure** (4 items)
   - Entity package naming (model/ not entity/)
   - JWT class placement (security/ not config/)
   - Service implementation location (service/ not service/impl/)
   - Standard package presence validation

4. **JWT Architecture** (4 items)
   - JwtAuthenticationFilter presence and location
   - JwtTokenProvider presence and location
   - Filter class inheritance validation
   - Provider responsibility validation

5. **Required Configuration Classes** (3 items)
   - CorsConfig presence
   - OpenAPIConfig presence and naming
   - SecurityConfig presence

6. **OpenAPI Configuration** (2 items)
   - Server URL validation (must point to API Gateway)
   - Server description validation

7. **Eureka Configuration** (3 items)
   - prefer-ip-address value validation
   - hostname configuration validation
   - Configuration location validation

**Validation Rules**:

1. **Pass Criteria**: Service MUST pass ALL checks to be considered compliant
2. **Critical Failures**: PROFILE, ENVIRONMENT_VAR, and JWT_ARCHITECTURE categories are critical
3. **Automated vs Manual**: 80% of checks should be automated, 20% require human review
4. **Enforcement**: Compliance check MUST run in CI/CD before merge to main

**Relationships**:
- Compliance Checklist validates Service Template (1:1)
- Compliance Checklist is executed by Validation Script (1:1)
- Compliance Checklist is used in Code Review Process (1:N)

---

## 4. Docker Environment Configuration Model

**Description**: Standardized environment variable configuration for docker-compose.yml service definitions.

**Attributes**:

| Attribute | Type | Description | Validation Rule |
|-----------|------|-------------|-----------------|
| `profileActivation` | String | Active Spring profile | Must be "docker" |
| `datasourceVars` | Object | Database connection env vars | Must use SPRING_DATASOURCE_* naming |
| `eurekaVars` | Object | Service discovery env vars | Must use EUREKA_CLIENT_SERVICEURL_DEFAULTZONE |
| `securityVars` | Object | Security-related env vars | Must include JWT_SECRET |
| `serviceSpecificVars` | Object | Optional service-specific vars | e.g., UPLOAD_DIR, SPRING_REDIS_HOST |

**Standard Docker Compose Service Block**:

```yaml
{service-name}:
  build: ./{service-name}
  container_name: {service-name}
  environment:
    - SPRING_PROFILES_ACTIVE=docker                                # Profile activation
    - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-{service}:5432/{service}_db
    - SPRING_DATASOURCE_USERNAME=sms_user
    - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}                    # From .env file
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    - JWT_SECRET=${JWT_SECRET}                                     # From .env file
    # Optional service-specific variables:
    - SPRING_REDIS_HOST=redis                                      # If service uses Redis
    - UPLOAD_DIR=/app/uploads/{service}                            # If service handles uploads
  networks:
    - backend-network
    - database-network
  depends_on:
    - postgres-{service}
    - eureka-server
  ports:
    - "{external-port}:{internal-port}"
```

**Validation Rules**:

1. **Mandatory Variables** (all services):
   - `SPRING_PROFILES_ACTIVE=docker`
   - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
   - `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`
   - `JWT_SECRET`

2. **Forbidden Variables**:
   - ❌ `DB_USERNAME`, `DB_PASSWORD`
   - ❌ `EUREKA_CLIENT_SERVICE_URL`
   - ❌ `EUREKA_INSTANCE_HOSTNAME`, `EUREKA_INSTANCE_PREFER_IP_ADDRESS`

3. **Network Configuration**:
   - All services MUST connect to `backend-network`
   - Services with databases MUST connect to `database-network`

**Relationships**:
- Docker Environment Configuration implements Configuration Profile Model (N:1)
- Docker Environment Configuration is validated by Compliance Checklist (N:1)

---

## 5. Validation Script Model

**Description**: Automated compliance validation executable that checks service structure against standards.

**Attributes**:

| Attribute | Type | Description | Validation Rule |
|-----------|------|-------------|-----------------|
| `scriptPath` | String | Location of validation script | `.standards/scripts/validate-service-structure.sh` |
| `inputParameter` | String | Service directory to validate | Must be valid service directory path |
| `exitCode` | Integer | Script execution result | 0 = pass, non-zero = fail |
| `validationChecks` | Function[] | Individual check functions | Each returns pass/fail |
| `outputFormat` | String | Check result display format | Human-readable with emojis (✅/❌) |

**Script Interface**:

```bash
# Usage
./validate-service-structure.sh <service-directory>

# Exit codes
# 0   = All checks passed
# 1   = One or more checks failed
# 2   = Invalid input (service directory not found)

# Output format
# Validating service: {service-name}
# ✅ Check name: Description of passing check
# ❌ Check name: Description of failing check
# Summary: X/Y checks passed
```

**Validation Check Functions**:

1. `check_profile_count()`: Verify exactly 2 YAML profiles exist
2. `check_package_structure()`: Verify model/, security/ packages exist
3. `check_jwt_classes()`: Verify JWT classes in security/ package
4. `check_config_classes()`: Verify required config classes present
5. `check_openapi_naming()`: Verify OpenAPIConfig (not OpenApiConfig)
6. `check_service_impl_location()`: Verify no service/impl/ subdirectory

**Validation Rules**:

1. **Error Accumulation**: Script continues checking even after failures, reports all errors
2. **Non-Destructive**: Script MUST NOT modify any files, only read and validate
3. **Idempotent**: Running multiple times produces same result
4. **Fast Execution**: Should complete in < 5 seconds for typical service

**Relationships**:
- Validation Script implements Compliance Checklist (1:1)
- Validation Script validates Service Template instances (1:N)

---

## 6. Service Creation Workflow Model

**Description**: Step-by-step process for creating a new compliant microservice from the template.

**Workflow Steps**:

| Step | Action | Input | Output | Validation |
|------|--------|-------|--------|------------|
| 1 | Copy template | auth-service directory | new-service directory | Directory exists |
| 2 | Rename packages | Package name pattern | Updated Java files | Grep for old package name |
| 3 | Update configuration | Service-specific values | Updated YAML, POM | Service name matches |
| 4 | Update Docker Compose | Service definition | Updated docker-compose.yml | Valid YAML syntax |
| 5 | Validate compliance | Service directory | Validation report | All checks pass |
| 6 | Build service | Maven command | JAR artifact | Build succeeds |
| 7 | Test locally | Docker Compose | Running container | Health check passes |

**State Transitions**:

```
TEMPLATE_COPIED → PACKAGES_RENAMED → CONFIGURATION_UPDATED →
DOCKER_UPDATED → COMPLIANCE_VALIDATED → BUILT → TESTED → READY
```

**Validation Rules**:

1. **Pre-Conditions**: auth-service must exist and be compliant
2. **Post-Conditions**: New service must pass 100% compliance check
3. **Rollback**: If any step fails, provide clear error message and cleanup instructions
4. **Documentation**: Each step must be documented in quickstart.md

**Relationships**:
- Service Creation Workflow uses Service Template (N:1)
- Service Creation Workflow produces Compliant Service instances (1:N)
- Service Creation Workflow validated by Validation Script (1:1)

---

## Artifact Relationships Diagram

```
┌─────────────────────────┐
│ Service Template        │◄───────────┐
│ (auth-service)          │            │
└───────┬─────────────────┘            │
        │                              │
        │ embodies                     │ validates
        │                              │
        ▼                              │
┌─────────────────────────┐            │
│ Configuration Profile   │            │
│ Model                   │            │
└───────┬─────────────────┘            │
        │                              │
        │ defines                      │
        │                              │
        ▼                       ┌──────┴────────┐
┌─────────────────────────┐    │ Compliance    │
│ Docker Environment      │───►│ Checklist     │
│ Configuration           │    │ Model         │
└─────────────────────────┘    └──────┬────────┘
                                      │
                                      │ executed by
                                      │
                                      ▼
                               ┌──────────────┐
                               │ Validation   │
                               │ Script       │
                               └──────┬───────┘
                                      │
                                      │ used in
                                      │
                                      ▼
                               ┌──────────────┐
                               │ Service      │
                               │ Creation     │
                               │ Workflow     │
                               └──────────────┘
```

---

## Summary

These artifact models define the standardization framework for the SMS microservices architecture. Key characteristics:

1. **Template-Based**: auth-service is the authoritative template
2. **Validation-Driven**: Automated compliance checking ensures consistency
3. **Documented Process**: Clear workflow for creating new services
4. **Enforceable**: Validation script can be integrated into CI/CD

**Next Steps**:
- Implement validation script based on Validation Script Model
- Create compliance checklist based on Compliance Checklist Model
- Document service creation workflow in quickstart.md
- Integrate validation into CI/CD pipeline
