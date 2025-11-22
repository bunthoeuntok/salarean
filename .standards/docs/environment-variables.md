# Environment Variable Standards

**Version**: 1.0.0
**Last Updated**: 2025-11-22
**Compliance**: MANDATORY for all microservices

---

## Overview

All microservices MUST use **Spring Boot standard environment variable names**. This ensures:

- ✅ **Zero deployment failures** from configuration inconsistencies
- ✅ **Consistent configuration** across all services
- ✅ **Clear documentation** for operations teams
- ✅ **Compatibility** with Spring Boot ecosystem tools

**Critical Rule**: NEVER use custom variable names when Spring Boot provides a standard equivalent.

---

## Table of Contents

1. [Standard Variable Names](#standard-variable-names)
2. [Required Variables](#required-variables)
3. [Optional Variables](#optional-variables)
4. [Forbidden Variable Names](#forbidden-variable-names)
5. [Docker Compose Configuration](#docker-compose-configuration)
6. [Environment-Specific Values](#environment-specific-values)

---

## Standard Variable Names

### Database Configuration

**Spring Boot Standard Names** (MANDATORY):

| Variable | Purpose | Example Value |
|----------|---------|---------------|
| `SPRING_DATASOURCE_URL` | JDBC connection URL | `jdbc:postgresql://postgres-auth:5432/auth_db` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `sms_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `${DB_PASSWORD}` (from secret) |

**Usage in application-docker.yml**:
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
```

**Forbidden Alternatives**:
```bash
❌ DB_URL                          # Use SPRING_DATASOURCE_URL
❌ DB_USERNAME                     # Use SPRING_DATASOURCE_USERNAME
❌ DB_PASSWORD                     # Use SPRING_DATASOURCE_PASSWORD
❌ DATABASE_URL                    # Use SPRING_DATASOURCE_URL
❌ POSTGRES_URL                    # Use SPRING_DATASOURCE_URL
```

---

### Eureka Service Discovery

**Spring Boot Standard Names** (MANDATORY):

| Variable | Purpose | Example Value |
|----------|---------|---------------|
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka server URL | `http://eureka-server:8761/eureka/` |

**Usage in application-docker.yml**:
```yaml
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

**Forbidden Alternatives**:
```bash
❌ EUREKA_CLIENT_SERVICE_URL       # Wrong: use SERVICEURL (no underscore)
❌ EUREKA_URL                      # Too vague
❌ EUREKA_SERVER_URL               # Non-standard
❌ EUREKA_DEFAULTZONE              # Missing CLIENT_SERVICEURL prefix
```

**Critical**: The variable name is `SERVICEURL` (no underscore between SERVICE and URL).

---

### Redis Configuration

**Spring Boot Standard Names** (MANDATORY):

| Variable | Purpose | Example Value |
|----------|---------|---------------|
| `SPRING_REDIS_HOST` | Redis server hostname | `redis` |
| `SPRING_REDIS_PORT` | Redis server port | `6379` |
| `SPRING_REDIS_PASSWORD` | Redis password (if auth enabled) | `${REDIS_PASSWORD}` |

**Usage in application-docker.yml**:
```yaml
spring:
  data:
    redis:
      host: ${SPRING_REDIS_HOST:redis}
      port: ${SPRING_REDIS_PORT:6379}
      password: ${SPRING_REDIS_PASSWORD:}
```

**Forbidden Alternatives**:
```bash
❌ REDIS_HOST                      # Use SPRING_REDIS_HOST
❌ REDIS_URL                       # Use SPRING_REDIS_HOST + SPRING_REDIS_PORT
❌ CACHE_HOST                      # Use SPRING_REDIS_HOST
```

---

### JWT Configuration

**Custom Application Variable** (REQUIRED):

| Variable | Purpose | Example Value |
|----------|---------|---------------|
| `JWT_SECRET` | JWT signing secret (256-bit minimum) | `your-256-bit-secret-key-here-minimum-32-chars` |

**Why Custom**: JWT secret is application-specific, not a Spring Boot framework feature.

**Usage in application-docker.yml**:
```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000              # 24 hours (can be hardcoded)
  refresh-expiration: 2592000000    # 30 days (can be hardcoded)
```

**Security Requirements**:
- ✅ Minimum 256 bits (32 characters)
- ✅ Stored in environment variables or secret management system
- ✅ NEVER committed to version control
- ✅ Different per environment (dev, staging, prod)

**Generate Strong Secret**:
```bash
openssl rand -base64 32
```

---

### Spring Profiles

**Spring Boot Standard Name** (MANDATORY):

| Variable | Purpose | Example Value |
|----------|---------|---------------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |

**Usage in docker-compose.yml**:
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
```

**Allowed Values**:
- `default` (implicit when not set) - Local development
- `docker` - Containerized deployment

**Forbidden Alternatives**:
```bash
❌ PROFILE                         # Use SPRING_PROFILES_ACTIVE
❌ ENV                             # Use SPRING_PROFILES_ACTIVE
❌ ENVIRONMENT                     # Use SPRING_PROFILES_ACTIVE
❌ SPRING_PROFILE                  # Missing _ACTIVE suffix
```

---

## Required Variables

### All Services Must Define

**In docker-compose.yml for EVERY service**:

```yaml
environment:
  # Profile (MANDATORY)
  - SPRING_PROFILES_ACTIVE=docker

  # Database (MANDATORY if using database)
  - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-{service}:5432/{db_name}
  - SPRING_DATASOURCE_USERNAME=sms_user
  - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

  # Eureka (MANDATORY for all services)
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

  # JWT (MANDATORY for services using JWT)
  - JWT_SECRET=${JWT_SECRET}
```

**Example: auth-service**:
```yaml
auth-service:
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-auth:5432/auth_db
    - SPRING_DATASOURCE_USERNAME=sms_user
    - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    - SPRING_REDIS_HOST=redis
    - SPRING_REDIS_PORT=6379
    - JWT_SECRET=${JWT_SECRET}
```

---

## Optional Variables

### Service-Specific Variables

**Redis** (if service uses caching):
```yaml
- SPRING_REDIS_HOST=redis
- SPRING_REDIS_PORT=6379
- SPRING_REDIS_PASSWORD=${REDIS_PASSWORD}  # If auth enabled
```

**File Upload** (if service handles uploads):
```yaml
- FILE_UPLOAD_BASE_PATH=/app/uploads
- FILE_UPLOAD_MAX_SIZE=10485760  # 10MB in bytes
```

**CORS** (if custom origins needed):
```yaml
- CORS_ALLOWED_ORIGINS=http://localhost:3000,https://sms.example.com
```

**Logging**:
```yaml
- SPRING_LOGGING_LEVEL_ROOT=INFO
- SPRING_LOGGING_LEVEL_COM_SMS=${SERVICE_NAME}=DEBUG
```

---

## Forbidden Variable Names

### Common Mistakes

**Database Variables**:
```bash
❌ DB_URL                          → ✅ SPRING_DATASOURCE_URL
❌ DB_USERNAME                     → ✅ SPRING_DATASOURCE_USERNAME
❌ DB_PASSWORD                     → ✅ SPRING_DATASOURCE_PASSWORD
❌ DATABASE_URL                    → ✅ SPRING_DATASOURCE_URL
❌ JDBC_URL                        → ✅ SPRING_DATASOURCE_URL
❌ POSTGRES_USER                   → ✅ SPRING_DATASOURCE_USERNAME
❌ MYSQL_PASSWORD                  → ✅ SPRING_DATASOURCE_PASSWORD
```

**Eureka Variables**:
```bash
❌ EUREKA_CLIENT_SERVICE_URL       → ✅ EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
❌ EUREKA_URL                      → ✅ EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
❌ EUREKA_SERVER_URL               → ✅ EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
❌ EUREKA_INSTANCE_HOSTNAME        → Configure in YAML, not env var
❌ EUREKA_INSTANCE_PREFER_IP       → Configure in YAML, not env var
```

**Redis Variables**:
```bash
❌ REDIS_HOST                      → ✅ SPRING_REDIS_HOST
❌ REDIS_PORT                      → ✅ SPRING_REDIS_PORT
❌ CACHE_HOST                      → ✅ SPRING_REDIS_HOST
❌ REDIS_URL                       → ✅ Use SPRING_REDIS_HOST + SPRING_REDIS_PORT
```

**Profile Variables**:
```bash
❌ PROFILE                         → ✅ SPRING_PROFILES_ACTIVE
❌ ENV                             → ✅ SPRING_PROFILES_ACTIVE
❌ ENVIRONMENT                     → ✅ SPRING_PROFILES_ACTIVE
❌ SPRING_PROFILE                  → ✅ SPRING_PROFILES_ACTIVE (with _ACTIVE)
```

---

## Docker Compose Configuration

### Standard Service Definition

**Template for any microservice**:

```yaml
{service-name}:
  build:
    context: ./{service-name}
    dockerfile: Dockerfile
  container_name: sms-{service-name}
  ports:
    - "{port}:{port}"
  environment:
    # Profile
    - SPRING_PROFILES_ACTIVE=docker

    # Database
    - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-{service}:5432/{db_name}
    - SPRING_DATASOURCE_USERNAME=sms_user
    - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

    # Eureka
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

    # JWT
    - JWT_SECRET=${JWT_SECRET}

    # Redis (optional)
    - SPRING_REDIS_HOST=redis
    - SPRING_REDIS_PORT=6379
  networks:
    - backend-network
    - database-network
  depends_on:
    - eureka-server
    - postgres-{service}
  restart: unless-stopped
```

### Real Example: auth-service

```yaml
auth-service:
  build:
    context: ./auth-service
    dockerfile: Dockerfile
  container_name: sms-auth-service
  ports:
    - "8081:8081"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-auth:5432/auth_db
    - SPRING_DATASOURCE_USERNAME=sms_user
    - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    - SPRING_REDIS_HOST=redis
    - SPRING_REDIS_PORT=6379
    - JWT_SECRET=${JWT_SECRET}
  networks:
    - backend-network
    - database-network
    - cache-network
  depends_on:
    - eureka-server
    - postgres-auth
    - redis
  restart: unless-stopped
```

---

## Environment-Specific Values

### Development (Local)

**In application.yml** (default profile):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db
    username: sms_user
    password: ${DB_PASSWORD:password}  # Default for local dev

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here-minimum-32-chars}

spring:
  data:
    redis:
      host: localhost
      port: 6379
```

**Defaults** (`${VAR:default}`):
- Provide sensible defaults for local development
- Make it easy to run services without Docker
- Still allow override via environment variables

---

### Docker (Containerized)

**In application-docker.yml**:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}

jwt:
  secret: ${JWT_SECRET}

spring:
  data:
    redis:
      host: ${SPRING_REDIS_HOST:redis}
      port: ${SPRING_REDIS_PORT:6379}
```

**No Defaults**:
- All values come from environment variables
- Fail fast if required variables are missing
- Clear error messages for misconfiguration

---

### Production

**Environment Variables** (passed via deployment platform):

```bash
# Secrets (from secret management)
export DB_PASSWORD="<strong-database-password>"
export JWT_SECRET="<256-bit-jwt-secret>"
export REDIS_PASSWORD="<redis-password>"

# Configuration (from deployment config)
export SPRING_DATASOURCE_URL="jdbc:postgresql://prod-db-host:5432/auth_db"
export SPRING_DATASOURCE_USERNAME="auth_svc_user"
export EUREKA_CLIENT_SERVICEURL_DEFAULTZONE="http://eureka-prod:8761/eureka/"
export SPRING_REDIS_HOST="redis-prod.example.com"
```

**Best Practices**:
- ✅ Use secret management systems (AWS Secrets Manager, HashiCorp Vault, Kubernetes Secrets)
- ✅ Rotate secrets regularly
- ✅ Use least-privilege database users
- ✅ Enable SSL/TLS for database connections
- ✅ Use strong, unique passwords per environment

---

## Validation

### Automated Validation

**Check environment variables in docker-compose.yml**:

```bash
.standards/scripts/validate-service-structure.sh <service-name>
```

**Validation Checks**:
- ✅ Uses `SPRING_DATASOURCE_*` (not `DB_*`)
- ✅ Uses `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` (not `EUREKA_CLIENT_SERVICE_URL`)
- ✅ Uses `SPRING_REDIS_HOST` (not `REDIS_HOST`)
- ✅ Has `SPRING_PROFILES_ACTIVE=docker`

**Expected Output**:
```
✅ PASS: Environment variable naming (Spring Boot standard names)
```

---

### Manual Validation

**Checklist for docker-compose.yml**:

- [ ] `SPRING_PROFILES_ACTIVE=docker` is set
- [ ] Database uses `SPRING_DATASOURCE_*` variables
- [ ] Eureka uses `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`
- [ ] Redis uses `SPRING_REDIS_*` variables (if applicable)
- [ ] JWT uses `JWT_SECRET` variable
- [ ] No custom variable names (DB_*, EUREKA_URL, etc.)

---

## Migration Guide

### Migrating from Custom Variable Names

**Step 1**: Identify non-standard variables

```bash
# In docker-compose.yml, search for:
grep -E "DB_USERNAME|DB_PASSWORD|DB_URL|EUREKA_CLIENT_SERVICE_URL" docker-compose.yml
```

**Step 2**: Update docker-compose.yml

**Before**:
```yaml
environment:
  - DB_URL=jdbc:postgresql://postgres-student:5432/student_db
  - DB_USERNAME=sms_user
  - DB_PASSWORD=${DB_PASSWORD}
  - EUREKA_CLIENT_SERVICE_URL=http://eureka-server:8761/eureka/
```

**After**:
```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-student:5432/student_db
  - SPRING_DATASOURCE_USERNAME=sms_user
  - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
```

**Step 3**: Update application-docker.yml

**Before**:
```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

**After**:
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
```

**Step 4**: Test

```bash
docker-compose up auth-service
```

Check logs for successful startup and Eureka registration.

---

## Quick Reference

### Standard Variable Names Cheat Sheet

| Configuration | Standard Variable | Example |
|---------------|-------------------|---------|
| **Database URL** | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres-auth:5432/auth_db` |
| **Database Username** | `SPRING_DATASOURCE_USERNAME` | `sms_user` |
| **Database Password** | `SPRING_DATASOURCE_PASSWORD` | `${DB_PASSWORD}` |
| **Eureka URL** | `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | `http://eureka-server:8761/eureka/` |
| **Redis Host** | `SPRING_REDIS_HOST` | `redis` |
| **Redis Port** | `SPRING_REDIS_PORT` | `6379` |
| **JWT Secret** | `JWT_SECRET` | `<256-bit-secret>` |
| **Spring Profile** | `SPRING_PROFILES_ACTIVE` | `docker` |

---

## Related Documentation

- **Profile Strategy**: `.standards/docs/profile-strategy.md` - 2-profile standard
- **Eureka Configuration**: `.standards/docs/eureka-configuration.md` - Service discovery setup
- **Configuration Patterns**: `.standards/docs/configuration-patterns.md` - Configuration classes
- **Docker Compose Template**: `.standards/templates/docker-compose-service.yml`

---

## Version History

| Version | Date       | Changes                           |
|---------|------------|-----------------------------------|
| 1.0.0   | 2025-11-22 | Initial environment variable standards |

---

## Support

For questions about environment variables:

1. Check this document for standard variable names
2. Review auth-service docker-compose.yml as reference
3. Run validation script to identify non-standard variables
4. Consult Spring Boot documentation for additional properties
