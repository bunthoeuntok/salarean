# Service Creation Post-Creation Verification Checklist

**Version**: 1.0.0
**Date**: 2025-11-22
**Purpose**: Ensure new microservices are 100% compliant with Salarean SMS standards

---

## Overview

This checklist ensures that newly created microservices follow all architectural standards before deployment. Use this after creating a service from the template or script.

**When to use this**:
- ✅ After creating a service from auth-service template
- ✅ After running create-service.sh automation script
- ✅ Before first deployment to Docker
- ✅ Before creating pull request

**How to use this**:
1. Work through each category sequentially
2. Check off each item as you verify it
3. Run automated validation script at the end
4. Fix any failures before proceeding

---

## Quick Verification (5 Minutes)

### Automated Compliance Check

**Run this first** to catch 80% of common issues:

```bash
# Navigate to project root
cd /Volumes/DATA/my-projects/salarean

# Run validation script
.standards/scripts/validate-service-structure.sh {your-service-name}

# Example
.standards/scripts/validate-service-structure.sh attendance-service
```

**If validation passes** (all checks ✅), proceed to manual verification.
**If validation fails** (some checks ❌), review failure details and fix issues.

---

## Category 1: File Structure and Naming

### 1.1 Service Directory Structure

- [ ] Service directory name matches pattern: `{name}-service` (lowercase, hyphen-separated)
- [ ] Directory exists at repository root: `/Volumes/DATA/my-projects/salarean/{service-name}/`

**Verify**:
```bash
ls -la | grep {service-name}
# Example: ls -la | grep attendance-service
```

---

### 1.2 Required Files Present

- [ ] `Dockerfile` exists
- [ ] `pom.xml` exists
- [ ] `README.md` exists
- [ ] `.gitignore` exists (optional but recommended)

**Verify**:
```bash
cd {service-name}
ls -la | grep -E "Dockerfile|pom.xml|README.md|.gitignore"
```

---

### 1.3 Package Structure

**Required packages**:
- [ ] `src/main/java/com/sms/{service}/config/` - Configuration classes
- [ ] `src/main/java/com/sms/{service}/controller/` - REST controllers
- [ ] `src/main/java/com/sms/{service}/dto/request/` - Request DTOs
- [ ] `src/main/java/com/sms/{service}/dto/response/` - Response DTOs
- [ ] `src/main/java/com/sms/{service}/exception/` - Exception handlers
- [ ] `src/main/java/com/sms/{service}/model/` - JPA entities (NOT `entity/`)
- [ ] `src/main/java/com/sms/{service}/repository/` - Data access
- [ ] `src/main/java/com/sms/{service}/security/` - JWT and auth (if applicable)
- [ ] `src/main/java/com/sms/{service}/service/` - Business logic

**Verify**:
```bash
tree src/main/java/com/sms/ -L 2
```

**Critical checks**:
- [ ] ❌ NO `entity/` package (must be `model/`)
- [ ] ❌ NO `service/impl/` package (impl classes go in `service/`)
- [ ] ❌ NO JWT classes in `config/` (must be in `security/`)

---

## Category 2: Configuration Files

### 2.1 Profile Files

- [ ] Exactly 2 profile files exist (no more, no less):
  - [ ] `src/main/resources/application.yml` (default profile)
  - [ ] `src/main/resources/application-docker.yml` (docker profile)

**Verify**:
```bash
find src/main/resources -name "application*.yml" | wc -l
# Expected output: 2
```

**Forbidden profiles**:
- [ ] ❌ NO `application-prod.yml`
- [ ] ❌ NO `application-dev.yml`
- [ ] ❌ NO `application-test.yml` in main resources (test profile should be in src/test/resources/)

---

### 2.2 application.yml (Default Profile)

**Open**: `src/main/resources/application.yml`

- [ ] `spring.application.name` matches service name (e.g., `attendance-service`)
- [ ] `server.port` is unique and not in use by other services
- [ ] `spring.datasource.url` points to local database (e.g., `jdbc:postgresql://localhost:5432/{service}_db`)
- [ ] `eureka.client.enabled: false` for local development

**Port allocation check**:
| Service | Port | Your Port? |
|---------|------|------------|
| API Gateway | 8080 | |
| Auth Service | 8081 | |
| Student Service | 8082 | |
| Attendance Service | 8083 | |
| Grade Service | 8084 | |
| Report Service | 8085 | |
| Notification Service | 8086 | |

---

### 2.3 application-docker.yml (Docker Profile)

**Open**: `src/main/resources/application-docker.yml`

- [ ] `spring.datasource.url` uses Docker container name (e.g., `jdbc:postgresql://postgres-{service}:5432/{service}_db`)
- [ ] Database URL uses `${SPRING_DATASOURCE_URL}` environment variable reference
- [ ] `eureka.instance.hostname` matches service name (e.g., `attendance-service`)
- [ ] `eureka.instance.prefer-ip-address: false` (CRITICAL - must be false)
- [ ] `eureka.client.enabled: true` for Docker deployment
- [ ] `eureka.client.service-url.defaultZone` uses `${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}`

**Example**:
```yaml
eureka:
  instance:
    hostname: attendance-service  # ✅ Matches service name
    prefer-ip-address: false      # ✅ MUST be false
  client:
    enabled: true
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

---

### 2.4 pom.xml

**Open**: `pom.xml`

- [ ] `<groupId>com.sms</groupId>`
- [ ] `<artifactId>{service-name}</artifactId>` (e.g., `attendance-service`)
- [ ] `<name>{service-name}</name>`
- [ ] `<description>` updated (not still "Auth Service")
- [ ] Spring Boot version matches: `<version>3.5.7</version>`
- [ ] Java version: `<java.version>21</java.version>`

**Required dependencies present**:
- [ ] `spring-boot-starter-web`
- [ ] `spring-boot-starter-data-jpa`
- [ ] `spring-boot-starter-security`
- [ ] `spring-boot-starter-validation`
- [ ] `spring-cloud-starter-netflix-eureka-client`
- [ ] `postgresql` driver
- [ ] `lombok`
- [ ] `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (JWT libraries)
- [ ] `springdoc-openapi-starter-webmvc-ui` (Swagger/OpenAPI)

---

## Category 3: Java Source Code

### 3.1 Main Application Class

**File**: `src/main/java/com/sms/{service}/{Service}ServiceApplication.java`

- [ ] File exists and is named correctly (e.g., `AttendanceServiceApplication.java`)
- [ ] Class name matches file name
- [ ] Package declaration: `package com.sms.{service};`
- [ ] Annotations present:
  - [ ] `@SpringBootApplication`
  - [ ] `@EnableEurekaClient` or `@EnableDiscoveryClient`
- [ ] `main` method present with `SpringApplication.run()`

**Example**:
```java
package com.sms.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class AttendanceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AttendanceServiceApplication.class, args);
    }
}
```

---

### 3.2 Required Configuration Classes

**All configuration classes must be in**: `src/main/java/com/sms/{service}/config/`

#### CorsConfig.java

- [ ] File exists: `config/CorsConfig.java`
- [ ] Class annotated with `@Configuration`
- [ ] Bean method: `public CorsConfigurationSource corsConfigurationSource()`
- [ ] Allowed origins configured (localhost + production)

#### OpenAPIConfig.java (exact capitalization)

- [ ] File exists: `config/OpenAPIConfig.java` (NOT `OpenApiConfig.java`)
- [ ] Class annotated with `@Configuration`
- [ ] Bean method returns `OpenAPI`
- [ ] Server URL points to API Gateway: `http://localhost:8080` (NOT service port)
- [ ] Security scheme configured for JWT

**Critical**: Server URL check
```java
Server server = new Server();
server.setUrl("http://localhost:8080");  // ✅ API Gateway
// NOT "http://localhost:8083"           // ❌ Wrong
```

#### SecurityConfig.java

- [ ] File exists: `config/SecurityConfig.java`
- [ ] Class annotated with `@Configuration`, `@EnableWebSecurity`, `@EnableMethodSecurity`
- [ ] Bean method: `public SecurityFilterChain filterChain(HttpSecurity http)`
- [ ] CSRF disabled for stateless API
- [ ] Session management: `SessionCreationPolicy.STATELESS`
- [ ] Public endpoints configured (actuator, swagger)
- [ ] JWT filter added: `addFilterBefore(jwtAuthenticationFilter, ...)`

---

### 3.3 JWT Architecture (if applicable)

**Both classes must be in**: `src/main/java/com/sms/{service}/security/` (NOT `config/`)

#### JwtAuthenticationFilter.java

- [ ] File exists: `security/JwtAuthenticationFilter.java`
- [ ] Class extends `OncePerRequestFilter`
- [ ] Annotated with `@Component`
- [ ] Extracts JWT from Authorization header
- [ ] Delegates validation to `JwtTokenProvider`
- [ ] Sets Spring Security context

#### JwtTokenProvider.java

- [ ] File exists: `security/JwtTokenProvider.java`
- [ ] Class annotated with `@Component`
- [ ] Contains token generation methods
- [ ] Contains token validation methods
- [ ] JWT secret configurable: `@Value("${jwt.secret}")`
- [ ] Token expiration configurable

---

### 3.4 Exception Handling

**File**: `src/main/java/com/sms/{service}/exception/GlobalExceptionHandler.java`

- [ ] File exists
- [ ] Class annotated with `@RestControllerAdvice`
- [ ] Handles validation errors: `@ExceptionHandler(MethodArgumentNotValidException.class)`
- [ ] Handles generic exceptions: `@ExceptionHandler(Exception.class)`
- [ ] Returns `ApiResponse<T>` format with error codes

---

### 3.5 API Response Format

**File**: `src/main/java/com/sms/{service}/dto/response/ApiResponse.java`

- [ ] File exists
- [ ] Generic class: `ApiResponse<T>`
- [ ] Has `errorCode` field (String)
- [ ] Has `data` field (T)
- [ ] Lombok annotations: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`

**Example**:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String errorCode;  // "SUCCESS" or error code
    private T data;            // Response payload or null
}
```

---

## Category 4: Docker Configuration

### 4.1 Dockerfile (Optimized Pattern)

**File**: `Dockerfile` (in service root)

**Required Pattern** (MANDATORY):
- [ ] Stage 1 uses pre-built base image: `FROM sms-common-builder:latest AS common-builder`
- [ ] Stage 2 copies sms-common from base: `COPY --from=common-builder /root/.m2/repository/com/sms/sms-common`
- [ ] Uses Java 21 JDK for build stage: `FROM eclipse-temurin:21-jdk-alpine AS builder`
- [ ] Uses Java 21 JRE for runtime stage: `FROM eclipse-temurin:21-jre-alpine`
- [ ] Exposes correct port: `EXPOSE {your-port}`
- [ ] Layer extraction configured: `RUN java -Djarmode=layertools -jar target/*.jar extract`
- [ ] Non-root user created: `RUN addgroup -S spring && adduser -S spring -G spring`
- [ ] Health check configured: `HEALTHCHECK --interval=30s ...`

**Verify**:
```bash
cat Dockerfile | grep -E "FROM sms-common-builder:latest"
# Should return: FROM sms-common-builder:latest AS common-builder

cat Dockerfile | grep -E "COPY --from=common-builder /root/.m2/repository/com/sms/sms-common"
# Should return the COPY command

cat Dockerfile | grep -E "HEALTHCHECK"
# Should return health check configuration
```

**FORBIDDEN Patterns**:
- [ ] ❌ NO 11-line sms-common build stage in Dockerfile (must use base image)
- [ ] ❌ NO `FROM eclipse-temurin:21-jdk-alpine AS common-builder` followed by sms-common build

**Reference**: See `.standards/docs/docker-build-optimization.md` for complete template

---

### 4.2 docker-compose.yml Entry

**File**: `/Volumes/DATA/my-projects/salarean/docker-compose.yml` (project root)

**sms-common-builder service** (must exist):
- [ ] `sms-common-builder` service defined at top of services section
- [ ] Image name: `image: sms-common-builder:latest`
- [ ] Build context: `context: ./sms-common`
- [ ] Profile: `profiles: - build-only`

**Your service configuration**:
- [ ] Service entry added to docker-compose.yml
- [ ] Service name matches: `{service-name}:`
- [ ] Build context points to ROOT directory: `context: .` (NOT `./{service-name}`)
- [ ] Dockerfile path specified: `dockerfile: ./{service-name}/Dockerfile`
- [ ] Container name matches service

**Required environment variables**:
- [ ] `SPRING_PROFILES_ACTIVE=docker` (NOT `prod`)
- [ ] `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-{service}:5432/{db_name}`
- [ ] `SPRING_DATASOURCE_USERNAME=sms_user`
- [ ] `SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}`
- [ ] `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/`
- [ ] `JWT_SECRET=${JWT_SECRET}`

**Networks**:
- [ ] `backend-network` (for Eureka communication)
- [ ] `database-network` (for database communication)

**Dependencies**:
- [ ] `depends_on` includes `eureka-server`
- [ ] `depends_on` includes service-specific database

**Healthcheck**:
- [ ] Healthcheck configured: `curl -f http://localhost:{port}/actuator/health`

---

### 4.3 Database Container (if needed)

- [ ] Postgres container added: `postgres-{service}:`
- [ ] Database name: `{service}_db`
- [ ] Volume created: `postgres-{service}-data`
- [ ] External port unique (avoid conflicts): e.g., `5433:5432`

---

## Category 5: API Gateway Integration

**File**: `api-gateway/src/main/resources/application.yml`

- [ ] Route added for new service
- [ ] Route ID matches service name
- [ ] URI uses load balancer: `lb://{service-name}`
- [ ] Path predicate correct: `/api/{resource}/**`
- [ ] Authentication filter added (if required)

**Example**:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: attendance-service
          uri: lb://attendance-service
          predicates:
            - Path=/api/attendance/**
          filters:
            - name: AuthenticationFilter
```

---

## Category 6: Code Quality

### 6.1 No Template Artifacts

- [ ] No references to `auth` in code (unless intentional)
- [ ] No hardcoded `auth-service` strings
- [ ] No "Auth Service" in comments/descriptions
- [ ] No `AuthController`, `AuthService`, etc. (unless you're creating auth-related features)

**Verify**:
```bash
grep -r "auth-service" src/ | grep -v "# auth-service template"
# Should return no results or only comments

grep -r "com.sms.auth" src/
# Should return no results
```

---

### 6.2 Package Naming Consistency

**Check all Java files**:
```bash
# Find files with incorrect package declarations
find src/main/java -name "*.java" -exec grep -H "package com.sms.auth" {} \;
# Should return no results
```

---

### 6.3 Lombok Usage

- [ ] Entity classes use `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
- [ ] Configuration classes use `@RequiredArgsConstructor` (if needed)
- [ ] Controllers use `@RequiredArgsConstructor`
- [ ] Services use `@RequiredArgsConstructor`

---

## Category 7: Testing

### 7.1 Test Directory Structure

- [ ] `src/test/java/com/sms/{service}/` exists
- [ ] Test resources: `src/test/resources/application-test.yml` exists (optional)

---

### 7.2 Build and Test

**Verify service builds**:
```bash
cd {service-name}
mvn clean package -DskipTests
```

- [ ] Build succeeds: `[INFO] BUILD SUCCESS`
- [ ] No compilation errors
- [ ] JAR file created: `target/{service-name}-0.0.1-SNAPSHOT.jar`

**Verify tests pass** (if tests exist):
```bash
mvn test
```

- [ ] Tests run successfully
- [ ] No test failures

---

## Category 8: Local Testing

### 8.1 Service Starts Locally

**Start service**:
```bash
cd {service-name}
mvn spring-boot:run
```

**Verify**:
- [ ] Service starts without errors
- [ ] Logs show: `Started {Service}Application in X.XXX seconds`
- [ ] Port binding successful: `Tomcat started on port(s): {port} (http)`

---

### 8.2 Health Endpoint

**Test health endpoint**:
```bash
curl http://localhost:{port}/actuator/health
```

**Expected response**:
```json
{"status":"UP"}
```

- [ ] Health endpoint returns 200 OK
- [ ] Response shows `"status":"UP"`

---

### 8.3 Swagger UI

**Open browser**: `http://localhost:{port}/swagger-ui.html`

- [ ] Swagger UI loads successfully
- [ ] Service title shows correct service name
- [ ] Endpoints are documented
- [ ] Server URL points to API Gateway (`http://localhost:8080`)

---

## Category 9: Docker Testing

### 9.1 Build sms-common-builder (First Time Only)

**Build base image** (only needed once, or when sms-common changes):
```bash
cd /Volumes/DATA/my-projects/salarean
docker-compose build sms-common-builder
```

- [ ] Base image builds successfully
- [ ] No errors during build
- [ ] Image created: `docker images | grep sms-common-builder`
- [ ] Image shows `sms-common-builder:latest`

**When to rebuild**:
- ✅ When sms-common source code changes
- ✅ When sms-common pom.xml changes
- ❌ NOT when service code changes

---

### 9.2 Build Docker Image

**Build service image**:
```bash
cd /Volumes/DATA/my-projects/salarean
docker-compose build {service-name}
```

- [ ] Build uses cached sms-common-builder image (not rebuilding sms-common)
- [ ] Build logs show: `FROM sms-common-builder:latest`
- [ ] Build logs show: `COPY --from=common-builder /root/.m2/repository/com/sms/sms-common`
- [ ] Image builds successfully
- [ ] No errors during build
- [ ] Image created: `docker images | grep {service-name}`

**Verify optimization**:
```bash
# Check build logs for optimization
docker-compose build {service-name} 2>&1 | grep "FROM sms-common-builder"
# Should show: [builder internal] load metadata for docker.io/library/sms-common-builder:latest
```

---

### 9.3 Start Service in Docker

**Start all services**:
```bash
docker-compose up -d
```

**Verify service is running**:
```bash
docker-compose ps
```

- [ ] Service container status: `Up (healthy)`
- [ ] No restart loops

**Check logs**:
```bash
docker-compose logs {service-name}
```

- [ ] Service starts successfully
- [ ] No errors in logs
- [ ] Database connection successful
- [ ] Eureka registration successful

---

### 9.4 Eureka Registration

**Open Eureka dashboard**: `http://localhost:8761`

- [ ] Service appears in registered instances
- [ ] Service name correct: `{SERVICE-NAME}` (uppercase)
- [ ] Status: `UP`
- [ ] Instance shows correct hostname

---

### 9.5 API Gateway Routing

**Test through gateway**:
```bash
# Get JWT token from auth-service
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}' \
  | jq -r '.data.accessToken')

# Test your service through gateway
curl -X GET http://localhost:8080/api/{resource}/health \
  -H "Authorization: Bearer $TOKEN"
```

- [ ] Request routes to your service
- [ ] Response received successfully
- [ ] JWT authentication works

---

## Category 10: Documentation

### 10.1 README.md

**File**: `{service-name}/README.md`

- [ ] Service name and description updated
- [ ] Port number documented
- [ ] Endpoints documented
- [ ] Environment variables documented
- [ ] Build instructions included

---

### 10.2 OpenAPI Documentation

**Verify Swagger docs**:
- [ ] All endpoints documented with `@Operation` annotations
- [ ] Request/response schemas documented
- [ ] Error responses documented
- [ ] Security requirements specified

---

## Category 11: Final Automated Validation

### 11.1 Run Compliance Validation

**Critical final check**:
```bash
.standards/scripts/validate-service-structure.sh {service-name}
```

**Expected result**:
```
================================================================================
Validation Summary
================================================================================

Total Checks: 25+
Passed: ALL
Failed: 0

✅ COMPLIANCE STATUS: PASSED

This service is 100% compliant with Salarean SMS standards.
```

- [ ] Validation script passes 100%
- [ ] All categories show ✅ PASS
- [ ] Zero failed checks

---

## Category 12: Version Control

### 12.1 Git Status

**Before committing**:
- [ ] No auth-service artifacts in git diff
- [ ] No hardcoded secrets (JWT_SECRET, DB_PASSWORD, etc.)
- [ ] No temporary/debug files
- [ ] No IDE-specific files (unless in .gitignore)

**Verify**:
```bash
git status
git diff {service-name}/
```

---

### 12.2 Commit Message

**Use standard format**:
```bash
git add {service-name}/
git add docker-compose.yml
git add api-gateway/src/main/resources/application.yml

git commit -m "feat: add {service-name} following standardization template

- Created {service-name} from auth-service template
- Added domain entities, services, controllers
- Configured Docker Compose integration
- Added API Gateway routes
- Validated 100% compliance with validation script

Closes #XXX"
```

---

## Summary Checklist

### Quick Final Verification

**Before declaring service complete, verify**:

- [ ] ✅ Validation script passes 100%
- [ ] ✅ Service builds successfully (`mvn clean package`)
- [ ] ✅ Service runs locally on unique port
- [ ] ✅ Health endpoint responds: `/actuator/health`
- [ ] ✅ Swagger UI accessible and correct
- [ ] ✅ Docker image builds successfully
- [ ] ✅ Service registers with Eureka
- [ ] ✅ API Gateway routes to service
- [ ] ✅ JWT authentication works
- [ ] ✅ Documentation updated
- [ ] ✅ No template artifacts remaining
- [ ] ✅ No secrets committed to Git

---

## Troubleshooting Guide

### If Validation Fails

**1. Review failure details**:
```bash
.standards/scripts/validate-service-structure.sh {service-name} | grep "FAIL"
```

**2. Common failures and fixes**:

| Failure | Fix |
|---------|-----|
| `entity/` package found | Rename to `model/` |
| JWT in `config/` | Move to `security/` |
| Wrong profile count | Delete extra profiles |
| Eureka `prefer-ip-address: true` | Change to `false` |
| OpenAPI wrong port | Change to `8080` |

**3. Refer to documentation**:
- `.standards/docs/common-locations.md` - Where files should be
- `.standards/docs/refactoring-checklist.md` - Safe changes
- `.standards/docs/quickstart-service-creation.md` - Step-by-step guide

---

### If Service Won't Start

**Check logs**:
```bash
# Local
mvn spring-boot:run

# Docker
docker-compose logs {service-name}
```

**Common issues**:
- Port already in use → Change port in application.yml
- Database connection refused → Check database is running
- Eureka connection timeout → Check network configuration
- ClassNotFoundException → Check package names match directory structure

---

## Next Steps

### After Passing All Checks

1. **Deploy to development environment**
2. **Run integration tests**
3. **Create pull request**
4. **Update project documentation**
5. **Add to CI/CD pipeline**

### Continuous Compliance

**Maintain standards**:
- Run validation script in CI/CD
- Review against checklist during code reviews
- Keep service aligned with template updates
- Monitor compliance metrics

---

**Version**: 1.0.0
**Last Updated**: 2025-11-22
**Maintained By**: Salarean Development Team
**Related Docs**: quickstart-service-creation.md, validate-service-structure.sh, common-locations.md
