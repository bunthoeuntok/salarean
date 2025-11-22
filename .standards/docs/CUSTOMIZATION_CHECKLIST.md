# Service Customization Checklist

## Overview

This checklist covers ALL required customizations when creating a new microservice using the standardized templates. Follow this checklist step-by-step to ensure your service is properly configured.

**Estimated Time**: 15-30 minutes (for experienced developers)

---

## Quick Reference

### Required Information

Before starting, gather the following information about your new service:

| Item | Example | Your Value |
|------|---------|------------|
| Service Name | `student` | _____________ |
| Service Port | `8082` | _____________ |
| Database Name | `student_db` | _____________ |
| Database Port | `5433` | _____________ |
| Service Description | "Manages student profiles and enrollment" | _____________ |
| API Context Path | `/api/students` | _____________ |

---

## Part 1: Java Templates (Required)

### 1.1 CorsConfig.java

**File**: `{service}-service/src/main/java/com/sms/{service}/config/CorsConfig.java`

**Customizations**:
- [ ] **Line 1**: Replace `SERVICENAME` with your service name
  ```java
  // Before
  package com.sms.SERVICENAME.config;

  // After (example)
  package com.sms.student.config;
  ```

**Optional Production Customizations**:
- [ ] **Line 30** (optional): Replace `"*"` with specific allowed origins for production
  ```java
  // Development (keep as-is)
  configuration.setAllowedOrigins(List.of("*"));

  // Production (change to)
  configuration.setAllowedOrigins(List.of("https://yourdomain.com"));
  ```

---

### 1.2 OpenAPIConfig.java

**File**: `{service}-service/src/main/java/com/sms/{service}/config/OpenAPIConfig.java`

**Customizations**:
- [ ] **Line 1**: Replace `SERVICENAME` with your service name
  ```java
  package com.sms.student.config;
  ```

- [ ] **Line 33**: Rename method to match your service
  ```java
  // Before
  public OpenAPI servicenameAPI()

  // After (example)
  public OpenAPI studentAPI()
  ```

- [ ] **Line 42**: Update API title
  ```java
  .title("Student Service API")  // Change to your service name
  ```

- [ ] **Line 43**: Update API description
  ```java
  .description("Manages student profiles, enrollment, and academic records")
  ```

**DO NOT CHANGE**:
- [ ] **Line 36**: Server URL MUST remain `http://localhost:8080` (API Gateway)
  ```java
  server.setUrl("http://localhost:8080");  // DO NOT CHANGE
  ```

---

### 1.3 SecurityConfig.java

**File**: `{service}-service/src/main/java/com/sms/{service}/config/SecurityConfig.java`

**Customizations**:
- [ ] **Line 1**: Replace `SERVICENAME` with your service name
  ```java
  package com.sms.student.config;
  ```

- [ ] **Line 3**: Update import statement
  ```java
  // Before
  import com.sms.SERVICENAME.security.JwtAuthenticationFilter;

  // After (example)
  import com.sms.student.security.JwtAuthenticationFilter;
  ```

- [ ] **Lines 59-62**: Add service-specific public endpoints
  ```java
  // Example for auth-service
  .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()

  // Example for public API service
  .requestMatchers("/api/public/**").permitAll()

  // Example for service with no public endpoints (remove TODO comment)
  // (Keep only the standard public endpoints below)
  ```

**DO NOT CHANGE**:
- [ ] **Lines 64-65**: Keep these public endpoints as-is
  ```java
  .requestMatchers("/actuator/**").permitAll()
  .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
  ```

---

### 1.4 JwtAuthenticationFilter.java

**File**: `{service}-service/src/main/java/com/sms/{service}/security/JwtAuthenticationFilter.java`

**Customizations**:
- [ ] **Line 1**: Replace `SERVICENAME` with your service name
  ```java
  package com.sms.student.security;
  ```

**Usually No Other Changes Needed** - This filter works as-is.

---

### 1.5 JwtTokenProvider.java

**File**: `{service}-service/src/main/java/com/sms/{service}/security/JwtTokenProvider.java`

**Customizations**:
- [ ] **Line 1**: Replace `SERVICENAME` with your service name
  ```java
  package com.sms.student.security;
  ```

**Optional Customizations**:
- [ ] **Line 59** (optional): Customize roles claim if needed
  ```java
  // Template default
  .claim("roles", new String[]{"TEACHER"})

  // Example customization (pass role as parameter)
  .claim("roles", new String[]{role})
  ```

- [ ] Add custom claim extraction methods if you add new claims

---

## Part 2: Configuration Files (Required)

### 2.1 application.yml (Local Development)

**File**: `{service}-service/src/main/resources/application.yml`

**Customizations**:
- [ ] **Server Port**: Update service port
  ```yaml
  server:
    port: 8082  # Change to your service's port
  ```

- [ ] **Application Name**: Update service name
  ```yaml
  spring:
    application:
      name: student-service  # Change to: {service}-service
  ```

- [ ] **Database Configuration**: Update database settings
  ```yaml
  datasource:
    url: jdbc:postgresql://localhost:5433/student_db  # Update port and database name
    username: sms_user  # Keep as-is (standard username)
    password: ${DB_PASSWORD:password}  # Keep as-is
  ```

- [ ] **Eureka Instance**: Update hostname
  ```yaml
  eureka:
    instance:
      hostname: student-service  # Change to: {service}-service
  ```

- [ ] **JWT Secret** (if needed): Keep as-is or use shared secret
  ```yaml
  jwt:
    secret: ${JWT_SECRET:your-256-bit-secret-key-here}  # Keep as-is for consistency
  ```

**Optional**: Add service-specific configuration (Redis, file upload, etc.)

---

### 2.2 application-docker.yml (Docker Environment)

**File**: `{service}-service/src/main/resources/application-docker.yml`

**Customizations**:
- [ ] **Database URL**: Update database host and name
  ```yaml
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    # In docker-compose.yml, set to:
    # jdbc:postgresql://postgres-{service}:5432/{service}_db
  ```

- [ ] **Eureka Hostname**: Update hostname
  ```yaml
  eureka:
    instance:
      hostname: student-service  # Change to: {service}-service
      prefer-ip-address: false  # DO NOT CHANGE - must be false
  ```

**DO NOT CHANGE**:
- [ ] **Prefer-IP-Address**: Must always be `false` for Docker
- [ ] Environment variable placeholders: `${SPRING_DATASOURCE_URL}`, `${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}`, etc.

---

## Part 3: Docker Configuration (Required)

### 3.1 docker-compose.yml

**Add service definition** (copy from `.standards/templates/docker-compose-service.yml`):

**Customizations**:
- [ ] **Service Name**: Replace all `{service-name}` placeholders
  ```yaml
  student-service:  # Service name
    container_name: student-service
    hostname: student-service  # MUST match service name
  ```

- [ ] **Build Context**: Update path
  ```yaml
  build:
    context: ./student-service  # Path to service directory
  ```

- [ ] **Ports**: Update port mapping
  ```yaml
  ports:
    - "8082:8082"  # {external-port}:{internal-port}
  ```

- [ ] **Environment Variables**: Update database settings
  ```yaml
  environment:
    - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-student:5432/student_db
    - SPRING_DATASOURCE_USERNAME=sms_user
    - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    - JWT_SECRET=${JWT_SECRET}
  ```

- [ ] **Database Service**: Add corresponding postgres service
  ```yaml
  postgres-student:  # Database service for student-service
    image: postgres:15-alpine
    container_name: postgres-student
    environment:
      - POSTGRES_DB=student_db
      - POSTGRES_USER=sms_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    ports:
      - "5433:5432"  # External port (different for each service)
    volumes:
      - postgres-student-data:/var/lib/postgresql/data
    networks:
      - sms-database

  volumes:
    postgres-student-data:  # Volume for database persistence
  ```

- [ ] **Networks**: Ensure service is on correct networks
  ```yaml
  networks:
    - sms-backend  # Required for Eureka communication
    - sms-database  # Required for database access
  ```

- [ ] **Dependencies**: Add dependency on database
  ```yaml
  depends_on:
    - postgres-student
    - eureka-server
  ```

---

## Part 4: Build Configuration (Required)

### 4.1 pom.xml (Maven)

**Customizations**:
- [ ] **Artifact ID**: Update service name
  ```xml
  <artifactId>student-service</artifactId>
  ```

- [ ] **Name**: Update display name
  ```xml
  <name>Student Service</name>
  ```

- [ ] **Description**: Update description
  ```xml
  <description>Student management service for SMS</description>
  ```

**Dependencies** (ensure these are present):
- [ ] Spring Boot Starter Web
- [ ] Spring Boot Starter Data JPA
- [ ] Spring Boot Starter Security
- [ ] Spring Cloud Starter Netflix Eureka Client
- [ ] Springdoc OpenAPI UI (for Swagger)
- [ ] PostgreSQL Driver
- [ ] JJWT (JWT token handling)
- [ ] Lombok (optional, for boilerplate reduction)

---

## Part 5: Additional Files (Required)

### 5.1 UserDetailsService Implementation

**File**: `{service}-service/src/main/java/com/sms/{service}/service/CustomUserDetailsService.java`

**MUST CREATE** - JwtAuthenticationFilter requires this:

```java
package com.sms.student.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TeacherRepository teacherRepository;  // Customize to your entity

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UUID id = UUID.fromString(userId);
        Teacher teacher = teacherRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        return User.builder()
            .username(teacher.getId().toString())
            .password(teacher.getPasswordHash())
            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER")))
            .build();
    }
}
```

**Customizations**:
- [ ] Replace `TeacherRepository` with your entity repository
- [ ] Replace `Teacher` with your user entity
- [ ] Update authorities/roles as needed

---

### 5.2 Main Application Class

**File**: `{service}-service/src/main/java/com/sms/{service}/StudentServiceApplication.java`

**Customizations**:
- [ ] Update class name to match service
- [ ] Ensure `@EnableEurekaClient` annotation is present
- [ ] Verify component scan includes all packages

```java
package com.sms.student;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class StudentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudentServiceApplication.class, args);
    }
}
```

---

## Part 6: Verification Steps

### 6.1 Local Verification

- [ ] **Compile**: `./mvnw clean compile`
  - Should complete without errors

- [ ] **Run locally**: `./mvnw spring-boot:run`
  - Should start on configured port
  - Should register with Eureka
  - Swagger UI accessible at `http://localhost:{port}/swagger-ui.html`

- [ ] **Test Swagger**: Open Swagger UI
  - Should show service title and description
  - Should have "Authorize" button
  - Public endpoints should work without authentication

### 6.2 Docker Verification

- [ ] **Build**: `docker-compose build {service-name}`
  - Should build without errors

- [ ] **Start**: `docker-compose up -d {service-name}`
  - Should start and stay running
  - Check logs: `docker-compose logs -f {service-name}`

- [ ] **Health Check**: `curl http://localhost:{port}/actuator/health`
  - Should return `{"status":"UP"}`

- [ ] **Eureka Registration**: Open `http://localhost:8761`
  - Should see service listed with hostname `{service-name}`

- [ ] **Smoke Test**: Run `.standards/scripts/smoke-test-deployment.sh`
  - Should pass all checks

---

## Common Mistakes Checklist

Avoid these common errors:

- [ ] ❌ **Wrong package name**: Using `SERVICENAME` instead of actual service name
- [ ] ❌ **Wrong import**: Not updating `SecurityConfig` import for `JwtAuthenticationFilter`
- [ ] ❌ **Wrong server URL**: Using service port instead of `http://localhost:8080` in OpenAPIConfig
- [ ] ❌ **Wrong Eureka setting**: Using `prefer-ip-address: true` in Docker (must be `false`)
- [ ] ❌ **Wrong database URL**: Using `localhost` instead of `postgres-{service}` in Docker
- [ ] ❌ **Missing UserDetailsService**: Forgetting to implement this required service
- [ ] ❌ **Wrong environment variable names**: Using `DB_URL` instead of `SPRING_DATASOURCE_URL`
- [ ] ❌ **Port conflicts**: Using same port as another service
- [ ] ❌ **Database port conflicts**: Using same external port (5432) for multiple databases
- [ ] ❌ **Missing networks**: Not adding service to `sms-backend` and `sms-database` networks
- [ ] ❌ **Missing dependencies**: Not declaring `depends_on: postgres-{service}`

---

## Quick Copy-Paste Commands

### Copy All Java Templates

```bash
# Set your service name
SERVICE_NAME="student"  # Change this

# Copy config templates
cp .standards/templates/java/CorsConfig.java \
   ${SERVICE_NAME}-service/src/main/java/com/sms/${SERVICE_NAME}/config/

cp .standards/templates/java/OpenAPIConfig.java \
   ${SERVICE_NAME}-service/src/main/java/com/sms/${SERVICE_NAME}/config/

cp .standards/templates/java/SecurityConfig.java \
   ${SERVICE_NAME}-service/src/main/java/com/sms/${SERVICE_NAME}/config/

# Copy security templates
mkdir -p ${SERVICE_NAME}-service/src/main/java/com/sms/${SERVICE_NAME}/security/

cp .standards/templates/java/JwtAuthenticationFilter.java \
   ${SERVICE_NAME}-service/src/main/java/com/sms/${SERVICE_NAME}/security/

cp .standards/templates/java/JwtTokenProvider.java \
   ${SERVICE_NAME}-service/src/main/java/com/sms/${SERVICE_NAME}/security/
```

### Bulk Replace SERVICENAME

```bash
# macOS
find ${SERVICE_NAME}-service -name "*.java" -exec sed -i '' "s/SERVICENAME/${SERVICE_NAME}/g" {} +

# Linux
find ${SERVICE_NAME}-service -name "*.java" -exec sed -i "s/SERVICENAME/${SERVICE_NAME}/g" {} +
```

---

## Environment Variables Reference

### Required Environment Variables

All services need these environment variables in Docker:

| Variable | Example Value | Purpose |
|----------|---------------|---------|
| `SPRING_PROFILES_ACTIVE` | `docker` | Activate Docker profile |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres-student:5432/student_db` | Database connection |
| `SPRING_DATASOURCE_USERNAME` | `sms_user` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `${DB_PASSWORD}` | Database password (from .env) |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | `http://eureka-server:8761/eureka/` | Eureka server URL |
| `JWT_SECRET` | `${JWT_SECRET}` | JWT signing key (from .env) |

### .env File

Create `.env` file in project root (DO NOT commit to git):

```bash
# Database password
DB_PASSWORD=your_secure_password_here

# JWT secret (minimum 32 characters for HS256)
JWT_SECRET=your-256-bit-secret-key-here-must-be-at-least-32-chars-long
```

---

## Completion Checklist

Before considering your service complete:

### Code
- [ ] All `SERVICENAME` placeholders replaced
- [ ] All `TODO` comments addressed
- [ ] UserDetailsService implemented
- [ ] All imports correct
- [ ] Code compiles without errors

### Configuration
- [ ] application.yml customized (port, database, service name)
- [ ] application-docker.yml customized (eureka hostname)
- [ ] docker-compose.yml entry added
- [ ] pom.xml customized (artifact ID, name, description)
- [ ] All environment variables set

### Verification
- [ ] Runs locally without errors
- [ ] Swagger UI accessible and functional
- [ ] Builds in Docker without errors
- [ ] Runs in Docker without errors
- [ ] Registers with Eureka
- [ ] Health endpoint returns UP
- [ ] Smoke test passes

### Documentation
- [ ] README.md created for service
- [ ] API endpoints documented (at minimum in Swagger)
- [ ] Environment variables documented
- [ ] Setup instructions added

---

## Service-Specific Customization Examples

### Auth Service
- **Public Endpoints**: `/api/auth/login`, `/api/auth/register`, `/api/auth/refresh`
- **Port**: `8081`
- **Database**: `auth_db`
- **Special**: Includes RefreshTokenService, PasswordEncoderConfig

### Student Service
- **Public Endpoints**: None (all require authentication)
- **Port**: `8082`
- **Database**: `student_db`
- **Special**: Student entity with profile data

### Attendance Service (Example)
- **Public Endpoints**: None
- **Port**: `8083`
- **Database**: `attendance_db`
- **Special**: Time-based attendance records

---

## Timeline

**Experienced Developer**: 15-30 minutes
**New Developer**: 1-2 hours

**Steps**:
1. Copy templates (2 min)
2. Replace placeholders (5 min)
3. Customize configurations (10 min)
4. Implement UserDetailsService (10 min)
5. Test locally (5 min)
6. Add to docker-compose (5 min)
7. Test in Docker (5 min)
8. Final verification (5 min)

---

## Support

**Stuck?** Check these resources:

1. **Template Documentation**: `.standards/docs/reusable-components.md`
2. **Verification Report**: `.standards/validation-reports/template-verification.md`
3. **CORS Guide**: `.standards/docs/cors-setup.md`
4. **OpenAPI Guide**: `.standards/docs/openapi-setup.md`
5. **Working Example**: `auth-service/` (reference implementation)
6. **Test Script**: `.standards/scripts/test-template-copy-paste.sh`

**Still stuck?** Review the architecture standards in `SERVICE_COMPARISON_ANALYSIS.md`.
