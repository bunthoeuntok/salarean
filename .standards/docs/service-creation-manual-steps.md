# Service Creation Manual Steps

**Version**: 1.0.0
**Date**: 2025-11-22
**Purpose**: Document manual steps still required after automated service creation

---

## Overview

While the `create-service.sh` script automates most of the service creation process, some steps still require manual intervention. This document lists those steps and explains why they can't be fully automated.

---

## Manual Steps Required

### 1. Domain-Specific Code Implementation

**What**: Write business logic specific to your service

**Why manual**: Each service has unique business requirements that cannot be templated.

**Steps**:
1. Create entities in `model/` package
2. Create repositories in `repository/` package
3. Create service interfaces and implementations in `service/` package
4. Create controllers in `controller/` package
5. Create DTOs in `dto/request/` and `dto/response/` packages

**Estimated time**: 30-60 minutes

**References**:
- See quickstart-service-creation.md section "Add Domain-Specific Code"
- Example entities, services, and controllers provided in quickstart guide

---

### 2. Docker Compose Integration

**What**: Add service configuration to docker-compose.yml

**Why manual**: docker-compose.yml is a shared file that requires careful merging to avoid conflicts.

**Steps**:
1. Open `/Volumes/DATA/my-projects/salarean/docker-compose.yml`
2. Copy service template from `.standards/templates/docker-compose-entry.yml`
3. Replace placeholders: `{SERVICE_NAME}`, `{PORT}`, `{DB_PORT}`
4. Add to `services:` section
5. Add database container configuration
6. Add volume definition at bottom

**Estimated time**: 5-10 minutes

**Automation consideration**: Could be automated with careful YAML parsing, but manual verification recommended for safety.

---

### 3. API Gateway Route Configuration

**What**: Add routing rules for new service in API Gateway

**Why manual**: Routing patterns depend on service API design (REST paths).

**Steps**:
1. Open `api-gateway/src/main/resources/application.yml`
2. Add route configuration under `spring.cloud.gateway.routes`
3. Configure path predicate (e.g., `/api/attendance/**`)
4. Add authentication filter if needed

**Example**:
```yaml
- id: attendance-service
  uri: lb://attendance-service
  predicates:
    - Path=/api/attendance/**
  filters:
    - name: AuthenticationFilter
```

**Estimated time**: 3-5 minutes

**Automation consideration**: Could be automated, but path patterns are service-specific.

---

### 4. Database Schema and Migrations

**What**: Create Flyway migrations for initial database schema

**Why manual**: Schema design is specific to each service's data model.

**Steps**:
1. Create `src/main/resources/db/migration/` directory
2. Create `V1__initial_schema.sql`
3. Define tables, indexes, constraints
4. Add sample data if needed

**Estimated time**: 10-20 minutes

**References**:
- See quickstart-service-creation.md section "Database Migrations"

---

### 5. Security Configuration Customization

**What**: Update `SecurityConfig.java` with service-specific endpoint permissions

**Why manual**: Each service has different public/protected endpoint requirements.

**Steps**:
1. Open `src/main/java/com/sms/{service}/config/SecurityConfig.java`
2. Update `.authorizeHttpRequests()` configuration
3. Define public endpoints (no authentication)
4. Define protected endpoints (require JWT)

**Example**:
```java
.authorizeHttpRequests(auth -> auth
    // Public endpoints
    .requestMatchers(
        "/actuator/health",
        "/swagger-ui/**",
        "/v3/api-docs/**"
    ).permitAll()
    // Protected endpoints
    .anyRequest().authenticated()
)
```

**Estimated time**: 5 minutes

---

### 6. OpenAPI Documentation Configuration

**What**: Update `OpenAPIConfig.java` with service-specific API metadata

**Why manual**: API title, description, and version are service-specific.

**Steps**:
1. Open `src/main/java/com/sms/{service}/config/OpenAPIConfig.java`
2. Update `.info()` section with service name, description, version
3. Verify server URL points to API Gateway (`http://localhost:8080`)

**Example**:
```java
.info(new Info()
    .title("Attendance Management API")
    .description("API for managing student attendance records")
    .version("1.0.0")
    .contact(new Contact()
        .name("Salarean Development Team")
        .email("dev@salarean.com")))
```

**Estimated time**: 3 minutes

---

### 7. Service-Specific Dependencies

**What**: Add Maven dependencies specific to service functionality

**Why manual**: Dependency requirements vary by service (e.g., email, SMS, PDF generation).

**Steps**:
1. Open `pom.xml`
2. Add required dependencies
3. Remove unused dependencies (e.g., Redis if not caching)

**Common additions**:
- Email: `spring-boot-starter-mail`
- Scheduling: `spring-boot-starter-quartz`
- PDF generation: `itextpdf`
- Excel: `apache-poi`

**Estimated time**: 5-10 minutes

---

### 8. Exception Handling

**What**: Add service-specific exception classes and handlers

**Why manual**: Exception types depend on business logic.

**Steps**:
1. Create custom exception classes in `exception/` package
2. Update `GlobalExceptionHandler.java` with handlers
3. Define error codes for each exception type

**Example**:
```java
// Custom exception
public class AttendanceNotFoundException extends RuntimeException {
    public AttendanceNotFoundException(String message) {
        super(message);
    }
}

// Handler in GlobalExceptionHandler
@ExceptionHandler(AttendanceNotFoundException.class)
public ResponseEntity<ApiResponse<Void>> handleAttendanceNotFound(
        AttendanceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiResponse<>("ATTENDANCE_NOT_FOUND", null));
}
```

**Estimated time**: 10 minutes

---

### 9. Unit and Integration Tests

**What**: Write tests for service functionality

**Why manual**: Tests are specific to business logic.

**Steps**:
1. Create test classes in `src/test/java/com/sms/{service}/`
2. Write unit tests for services
3. Write integration tests for controllers
4. Configure test database (H2)

**Estimated time**: 30-60 minutes

---

### 10. README Documentation

**What**: Update service README with specific details

**Why manual**: Service functionality and API documentation are unique.

**Steps**:
1. Open `{service-name}/README.md`
2. Update service description
3. Document API endpoints
4. Add setup instructions
5. List environment variables

**Estimated time**: 10-15 minutes

---

## Verification Checklist

After completing manual steps, verify:

- [ ] Service builds successfully: `mvn clean package`
- [ ] Service starts locally: `mvn spring-boot:run`
- [ ] Health endpoint responds: `curl http://localhost:{port}/actuator/health`
- [ ] Swagger UI accessible: `http://localhost:{port}/swagger-ui.html`
- [ ] Docker image builds: `docker-compose build {service-name}`
- [ ] Service registers with Eureka
- [ ] API Gateway routes requests correctly
- [ ] Validation script passes: `.standards/scripts/validate-service-structure.sh {service-name}`

---

## Total Estimated Time

**Automated steps** (via create-service.sh): ~5 minutes
**Manual steps**:

1-2 hours total:
- Domain code: 30-60 min
- Docker Compose: 5-10 min
- API Gateway: 3-5 min
- Database migrations: 10-20 min
- Security config: 5 min
- OpenAPI config: 3 min
- Dependencies: 5-10 min
- Exception handling: 10 min
- Tests: 30-60 min
- Documentation: 10-15 min

**Total**: 1.5-2.5 hours from start to deployment-ready service

---

## Automation Opportunities (Future)

### Possible Improvements

1. **Docker Compose Automation**:
   - YAML parser to safely merge service configuration
   - Validate no port conflicts
   - Auto-generate volume names

2. **API Gateway Automation**:
   - Infer path patterns from service name
   - Add default authentication filter
   - Update routes without manual editing

3. **Code Generation**:
   - Generate CRUD controllers from entity definitions
   - Create repository interfaces automatically
   - Generate basic service implementations

4. **Test Scaffolding**:
   - Generate test class templates
   - Create integration test boilerplate
   - Set up test database configuration

### Why Not Automated Now

- Risk of breaking existing configurations (docker-compose.yml)
- Service-specific requirements need human decision-making
- Code generation quality vs. manual coding trade-offs
- Focus on standardization first, automation second

---

## Comparison: Manual vs. Automated

### Without create-service.sh (All Manual)

**Time**: 3-4 hours
**Error-prone steps**:
- Package renaming (30+ files)
- Directory structure updates
- Port number consistency
- Missing configuration updates

### With create-service.sh

**Time**: 1.5-2.5 hours
**Automated**:
- Package renaming ✅
- Directory structure ✅
- Port updates ✅
- Template cleanup ✅
- Build verification ✅

**Manual** (requires domain knowledge):
- Business logic
- Docker Compose integration
- API documentation
- Testing

**Time saved**: ~1.5-2 hours (50% reduction)

---

## Tips for Efficient Manual Work

### 1. Use Templates

Copy-paste from quickstart guide examples:
- Entity template
- Repository template
- Service template
- Controller template

### 2. Reference Existing Services

Look at auth-service or student-service for:
- Configuration patterns
- Exception handling
- Test structure

### 3. Work in Order

Follow this sequence for efficiency:
1. Entities & Repositories (data layer)
2. Services (business logic)
3. Controllers (API layer)
4. Tests
5. Docker & deployment config
6. Documentation

### 4. Validate Early and Often

Run validation after each major step:
```bash
# After domain code
mvn clean package

# After Docker config
.standards/scripts/validate-service-structure.sh {service}

# After API Gateway
docker-compose config
```

---

## Getting Help

**If you're stuck on manual steps**:

1. **Check documentation**:
   - `.standards/docs/quickstart-service-creation.md`
   - `.standards/docs/service-creation-checklist.md`
   - `.standards/docs/common-locations.md`

2. **Review template service**:
   - `auth-service/` - Complete reference implementation
   - Compare your service structure with auth-service

3. **Run validation script**:
   ```bash
   .standards/scripts/validate-service-structure.sh {your-service}
   ```

4. **Check existing services**:
   - How did auth-service implement similar features?
   - How are exceptions handled in student-service?

---

**Version**: 1.0.0
**Last Updated**: 2025-11-22
**Maintained By**: Salarean Development Team
**Related Docs**: create-service.sh, service-creation-checklist.md, quickstart-service-creation.md
