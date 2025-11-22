# Spring Profile Strategy Standards

**Version**: 1.0.0
**Last Updated**: 2025-11-22
**Compliance**: MANDATORY for all microservices

---

## Overview

All microservices MUST have exactly **2 Spring profiles**:

1. **default** (`application.yml`) - Local development
2. **docker** (`application-docker.yml`) - Containerized deployment

This strategy ensures:

- ✅ **Zero configuration complexity** - Only 2 profiles to manage
- ✅ **Clear separation** - Local vs containerized environments
- ✅ **Consistent deployment** - All services use `docker` profile in containers
- ✅ **Easy debugging** - Developers know which profile is active

**Critical Rule**: Services MUST NOT have more than 2 profiles.

---

## Table of Contents

1. [The 2-Profile Standard](#the-2-profile-standard)
2. [Default Profile (application.yml)](#default-profile-applicationyml)
3. [Docker Profile (application-docker.yml)](#docker-profile-application-dockeryml)
4. [Profile Activation](#profile-activation)
5. [Forbidden Profiles](#forbidden-profiles)
6. [Migration Guide](#migration-guide)

---

## The 2-Profile Standard

### Why Only 2 Profiles?

**Problems with Multiple Profiles**:
- ❌ Confusion about which profile to use
- ❌ Configuration duplication and drift
- ❌ Difficult to maintain consistency
- ❌ Harder to debug configuration issues

**Solution**: 2 Profiles Only

```
✅ CORRECT:
application.yml          (default profile - local development)
application-docker.yml   (docker profile - containerized deployment)

❌ INCORRECT:
application.yml
application-dev.yml      ← Remove this
application-prod.yml     ← Use docker profile instead
application-test.yml     ← Remove this
application-local.yml    ← Use default profile instead
```

---

### Profile File Structure

**Standard structure for EVERY service**:

```
{service-name}/
└── src/main/resources/
    ├── application.yml           # Default profile (no suffix)
    └── application-docker.yml    # Docker profile
```

**File Count Validation**:
```bash
# Should return exactly 2
ls -1 {service}/src/main/resources/application*.yml | wc -l
```

---

## Default Profile (application.yml)

### Purpose

**When Used**: Local development on developer's machine

**Characteristics**:
- ✅ Uses `localhost` for all services
- ✅ Provides sensible defaults
- ✅ Easy to run without Docker
- ✅ Supports hot reloading and debugging

### Standard Template

**File**: `src/main/resources/application.yml`

```yaml
server:
  port: {service-port}  # 8081 for auth, 8082 for student, etc.

spring:
  application:
    name: {service-name}

  # Database Configuration (Local)
  datasource:
    url: jdbc:postgresql://localhost:5432/{db_name}
    username: sms_user
    password: ${DB_PASSWORD:password}  # Default for local dev
    driver-class-name: org.postgresql.Driver

  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: validate  # Never use 'update' or 'create-drop'
    show-sql: false       # Set to true for debugging SQL
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  # Flyway Migration
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    schemas: public

  # Redis (if applicable)
  data:
    redis:
      host: localhost
      port: 6379
      lettuce:
        pool:
          max-active: 10
          max-idle: 5
          min-idle: 2
          max-wait: 2000

# Eureka Configuration (Local)
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true  # OK for local development

# JWT Configuration
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here-minimum-32-chars}
  expiration: 86400000              # 24 hours in milliseconds
  refresh-expiration: 2592000000    # 30 days in milliseconds

# Actuator Endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always

# OpenAPI/Swagger
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
  show-actuator: false
```

### Key Characteristics

**Defaults with Overrides**:
```yaml
password: ${DB_PASSWORD:password}    # Default: password
jwt:
  secret: ${JWT_SECRET:default-secret}  # Default: default-secret
```

**Pattern**: `${VARIABLE_NAME:default_value}`

**Rationale**:
- Developers can run service without setting env vars
- Production values still come from environment
- Clear defaults for local development

---

## Docker Profile (application-docker.yml)

### Purpose

**When Used**: Containerized deployment (Docker Compose, Kubernetes, production)

**Characteristics**:
- ✅ All configuration from environment variables
- ✅ Uses Docker service names for hostnames
- ✅ No hardcoded defaults (fail fast if misconfigured)
- ✅ Production-ready

### Standard Template

**File**: `src/main/resources/application-docker.yml`

```yaml
server:
  port: {service-port}  # Same as default profile

spring:
  application:
    name: {service-name}  # Same as default profile

  # Database Configuration (Docker)
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: validate

  # Flyway Migration
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    schemas: public

  # Redis (if applicable)
  data:
    redis:
      host: ${SPRING_REDIS_HOST:redis}
      port: ${SPRING_REDIS_PORT:6379}

# Eureka Configuration (Docker)
eureka:
  instance:
    hostname: {service-name}       # MUST match service name
    prefer-ip-address: false       # MUST be false for Docker
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}

# JWT Configuration
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
  refresh-expiration: 2592000000
```

### Key Characteristics

**No Defaults for Secrets**:
```yaml
password: ${SPRING_DATASOURCE_PASSWORD}  # No default - MUST be provided
jwt:
  secret: ${JWT_SECRET}                  # No default - MUST be provided
```

**Optional Defaults for Infrastructure**:
```yaml
redis:
  host: ${SPRING_REDIS_HOST:redis}       # Default: 'redis' (Docker service name)
  port: ${SPRING_REDIS_PORT:6379}        # Default: 6379 (standard port)
```

**Rationale**:
- Secrets must be explicitly provided
- Infrastructure defaults use Docker service names
- Fail fast if critical config is missing

---

## Profile Activation

### Local Development (Default Profile)

**No profile specified**:

```bash
# Run Spring Boot application
./mvnw spring-boot:run

# Profile: default (implicit)
# Uses: application.yml
```

**Or explicitly**:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=default
```

---

### Docker Deployment (Docker Profile)

**In docker-compose.yml** (MANDATORY):

```yaml
{service-name}:
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    # ... other environment variables
```

**Complete Example**:

```yaml
auth-service:
  build:
    context: ./auth-service
    dockerfile: Dockerfile
  container_name: sms-auth-service
  ports:
    - "8081:8081"
  environment:
    - SPRING_PROFILES_ACTIVE=docker  # ← MANDATORY
    - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-auth:5432/auth_db
    - SPRING_DATASOURCE_USERNAME=sms_user
    - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    - JWT_SECRET=${JWT_SECRET}
  networks:
    - backend-network
    - database-network
  depends_on:
    - eureka-server
    - postgres-auth
  restart: unless-stopped
```

---

### Kubernetes Deployment

**In Deployment manifest**:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
spec:
  template:
    spec:
      containers:
      - name: auth-service
        image: sms/auth-service:latest
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "docker"  # Same profile for all containerized deployments
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: auth-db-secret
              key: url
        # ... other environment variables
```

---

## Forbidden Profiles

### Do NOT Create These Profiles

```
❌ application-dev.yml       # Use default profile instead
❌ application-prod.yml      # Use docker profile instead
❌ application-test.yml      # Use default profile with test DB
❌ application-local.yml     # Use default profile
❌ application-staging.yml   # Use docker profile
❌ application-uat.yml       # Use docker profile
```

### Why These Are Forbidden

**application-dev.yml**:
- ❌ Duplicates default profile
- ✅ Use `application.yml` (default) for development

**application-prod.yml**:
- ❌ Creates configuration drift
- ✅ Use `application-docker.yml` + production env vars

**application-test.yml**:
- ❌ Test configuration should be in test resources
- ✅ Use `src/test/resources/application.yml` for tests

**Environment-specific profiles** (staging, uat, etc.):
- ❌ Configuration should come from environment variables
- ✅ Use `application-docker.yml` + different env vars per environment

---

## Configuration Differences

### What Goes in Each Profile?

| Configuration | Default Profile | Docker Profile |
|---------------|----------------|----------------|
| **Service Name** | Same | Same |
| **Port** | Same | Same |
| **Database URL** | `localhost:5432` | `${SPRING_DATASOURCE_URL}` |
| **Database Username** | Hardcoded or default | `${SPRING_DATASOURCE_USERNAME}` |
| **Database Password** | Default value | No default (required) |
| **Eureka URL** | `http://localhost:8761/eureka/` | `${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}` |
| **Eureka Hostname** | N/A | `{service-name}` |
| **Eureka prefer-ip** | `true` | `false` |
| **Redis Host** | `localhost` | `${SPRING_REDIS_HOST:redis}` |
| **JWT Secret** | Default value | No default (required) |

---

## Validation

### Automated Validation

**Check profile count**:

```bash
.standards/scripts/validate-service-structure.sh <service-name>
```

**Expected Output**:
```
✅ PASS: Service has exactly 2 profile files (default, docker)
```

**Error Output**:
```
❌ FAIL: Service has 4 profile files (expected 2)
Found: application.yml, application-dev.yml, application-docker.yml, application-prod.yml
```

---

### Manual Validation

**Checklist**:

- [ ] Exactly 2 profile files exist: `application.yml` and `application-docker.yml`
- [ ] No other profile files (`application-*.yml`) exist
- [ ] Default profile uses `localhost` for services
- [ ] Docker profile uses environment variables
- [ ] Docker profile has `hostname: {service-name}` for Eureka
- [ ] Docker profile has `prefer-ip-address: false` for Eureka
- [ ] docker-compose.yml sets `SPRING_PROFILES_ACTIVE=docker`

---

## Migration Guide

### Removing Extra Profiles

**Step 1**: Identify extra profiles

```bash
cd {service-name}/src/main/resources
ls -1 application*.yml

# Example output (WRONG - 4 profiles):
application.yml
application-dev.yml
application-docker.yml
application-prod.yml
```

**Step 2**: Consolidate to 2 profiles

**If you have `application-dev.yml`**:
```bash
# Merge dev settings into application.yml (default profile)
# Then delete application-dev.yml
rm application-dev.yml
```

**If you have `application-prod.yml`**:
```bash
# Rename to application-docker.yml
mv application-prod.yml application-docker.yml
```

**Step 3**: Update docker-compose.yml

**Before**:
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod  # Wrong profile name
```

**After**:
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker  # Correct profile name
```

**Step 4**: Verify

```bash
ls -1 {service}/src/main/resources/application*.yml | wc -l
# Should output: 2
```

---

## Real Examples

### Example 1: auth-service (Compliant)

**Files**:
```
auth-service/src/main/resources/
├── application.yml         ✅
└── application-docker.yml  ✅
```

**Default Profile (application.yml)**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db
    username: sms_user
    password: ${DB_PASSWORD:password}

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

**Docker Profile (application-docker.yml)**:
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

eureka:
  instance:
    hostname: auth-service
    prefer-ip-address: false
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

**Status**: ✅ Compliant (2 profiles)

---

### Example 2: student-service (Non-Compliant - Before Migration)

**Files** (WRONG):
```
student-service/src/main/resources/
├── application.yml         ✅
├── application-dev.yml     ❌ Remove this
├── application-docker.yml  ✅
└── application-prod.yml    ❌ Remove this (or merge into docker)
```

**Status**: ❌ Non-compliant (4 profiles - should be 2)

**Fix**: Consolidate to 2 profiles as shown in migration guide.

---

## Best Practices

### 1. Keep Profiles Identical Where Possible

**Same in Both Profiles**:
```yaml
server:
  port: 8081

spring:
  application:
    name: auth-service

  jpa:
    hibernate:
      ddl-auto: validate
```

**Different Between Profiles**:
```yaml
# Default:
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db

# Docker:
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
```

---

### 2. Use Defaults Wisely

**Default Profile**: Provide defaults for convenience
```yaml
jwt:
  secret: ${JWT_SECRET:default-secret-for-dev-only}
```

**Docker Profile**: No defaults for secrets
```yaml
jwt:
  secret: ${JWT_SECRET}  # Required - no default
```

---

### 3. Document Profile Differences

**In README.md**:
```markdown
## Running Locally (Default Profile)

mvn spring-boot:run

Uses localhost for all services.

## Running with Docker (Docker Profile)

docker-compose up

Uses docker profile with environment variables.
```

---

## Quick Reference

### Profile Checklist

✅ **Correct Setup**:
- [ ] Exactly 2 profile files
- [ ] Files named `application.yml` and `application-docker.yml`
- [ ] Default profile uses `localhost`
- [ ] Docker profile uses environment variables
- [ ] Docker profile has `hostname: {service-name}`
- [ ] Docker profile has `prefer-ip-address: false`
- [ ] docker-compose.yml uses `SPRING_PROFILES_ACTIVE=docker`

❌ **Incorrect Setup**:
- More than 2 profile files
- Profiles named `-dev`, `-prod`, `-test`, etc.
- Hardcoded values in docker profile
- Using IP addresses instead of hostnames

---

## Related Documentation

- **Environment Variables**: `.standards/docs/environment-variables.md` - Standard variable names
- **Eureka Configuration**: `.standards/docs/eureka-configuration.md` - Service discovery setup
- **Configuration Templates**: `.standards/templates/application.yml`, `.standards/templates/application-docker.yml`

---

## Version History

| Version | Date       | Changes                     |
|---------|------------|-----------------------------|
| 1.0.0   | 2025-11-22 | Initial 2-profile standard  |

---

## Support

For questions about Spring profiles:

1. Check this document for the 2-profile standard
2. Review auth-service as reference implementation
3. Run validation script to check profile count
4. Consult migration guide to consolidate profiles
