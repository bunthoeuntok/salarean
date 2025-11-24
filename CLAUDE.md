# salarean Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-11-20

## Active Technologies
- Java 21 (Spring Boot 3.5.7) + Spring Boot, Spring Security, Spring Data JPA, Spring Data Redis, JWT (jjwt 0.12.5), BCrypt, Hibernate Validator, Lombok (002-jwt-auth)
- PostgreSQL 15+ (auth-service database), Redis 7+ (refresh token cache) (002-jwt-auth)
- Java 21+ (Spring Boot 3.5.7) (003-student-crud)
- Java 21 (Spring Boot 3.5.7), Bash scripting for automation + Spring Boot, Spring Security, Spring Data JPA, Netflix Eureka, Docker Compose (001-service-standards)
- N/A (this is a standardization/documentation feature, not a data feature) (001-service-standards)
- Java 21+ + Spring Data JPA, Spring Web, Lombok, Hibernate Validator, Flyway (004-student-enrollment)
- PostgreSQL 15+ (student_db database) (004-student-enrollment)
- Java 21 with Spring Boot 3.5.7 (005-class-management)
- PostgreSQL 15+ (dedicated `class_db` database with tables: classes, enrollment_history) (005-class-management)
- Java 21 with Spring Boot 3.5.7 (existing student-service stack) (005-class-management)
- PostgreSQL 15+ (existing `student_db` database; **NEW tables**: classes, enrollment_history) (005-class-management)

- Java 21 (Spring Boot 3.5.7) + Spring Boot, Spring Security, Spring Data JPA, JWT (jjwt 0.12.5), BCrypt for password hashing (001-teacher-auth)

## Project Structure

```text
src/
tests/
```

## Commands

# Add commands for Java 21 (Spring Boot 3.5.7)

## Code Style

Java 21 (Spring Boot 3.5.7): Follow standard conventions

## Recent Changes
- 005-class-management: Added Java 21 with Spring Boot 3.5.7 (existing student-service stack)
- 005-class-management: Added Java 21 with Spring Boot 3.5.7
- 004-student-enrollment: Added Java 21+ + Spring Data JPA, Spring Web, Lombok, Hibernate Validator, Flyway


<!-- MANUAL ADDITIONS START -->

## API Standards (MANDATORY)

**All backend services MUST follow these standards:**

### Response Format

All API endpoints MUST return responses in this standardized format:

```typescript
{
  errorCode: ErrorCode,  // Error code for client-side i18n lookup, "SUCCESS" for successful operations
  data: T             // Response payload (type varies by endpoint), null on errors
}
```

**Success Example**:
```json
{
  "errorCode": "SUCCESS",
  "data": { "id": "123", "name": "John" }
}
```

**Error Example**:
```json
{
  "errorCode": "INVALID_INPUT",
  "data": null
}
```

### Internationalization (i18n) Policy

**Backend responsibilities**:
- Return ONLY machine-readable error codes (e.g., `INVALID_PHONE_FORMAT`, `UNAUTHORIZED`)
- Return data in neutral format (no human-readable messages)
- Store user language preference in profile

**Frontend responsibilities**:
- Maintain ALL internationalization (i18n) translations
- Map error codes to localized messages in user's preferred language
- Handle all UI text translations (English/Khmer)

**Rationale**: Separating i18n concerns keeps backend simple and maintainable. Frontend can dynamically update translations without backend changes.

### Error Code Standards

Error codes MUST be:
- UPPER_SNAKE_CASE format
- Self-documenting (e.g., `PASSWORD_TOO_SHORT`, not `ERR_001`)
- Consistent across all services
- Documented in feature specifications

---

## Common Library Standards (sms-common) - MANDATORY

**Reference Document**: See `.standards/docs/common-library-standards.md` for complete details.

The `sms-common` library provides shared DTOs, utilities, constants, and validation annotations used across ALL microservices.

### What Belongs in sms-common?

**✅ INCLUDE** (used by multiple services):
- API response wrappers (`ApiResponse<T>`)
- Common error codes (`ErrorCode` enum)
- Business domain constants (academic year, age limits, Cambodia timezone)
- File upload standards (size limits, MIME types)
- Pagination defaults
- Common utilities (`DateUtils`, `FileUtils`)
- Shared validation annotations (`@KhmerPhone`)

**❌ EXCLUDE** (service-specific):
- Security policies (JWT expiration, password rules, rate limiting)
- Service-specific business logic
- Cache strategies (TTL values)
- Service-specific error codes (unless used by 3+ services)

### Constants Organization Rules

**Rule 1: No Duplication**
- Utility classes MUST reference `CommonConstants`, not duplicate values
- Example: `DateUtils` uses `CommonConstants.TIMEZONE_CAMBODIA`

**Rule 2: Service-Specific Config Classes**
- Each service defines its own security properties
- Example: `auth-service/config/SecurityProperties.java` contains JWT settings, password rules, rate limiting

**Rule 3: The "3-Service Rule"**
- Add to common ErrorCode only if used by 3+ services
- Otherwise, keep error codes service-specific

### Integration Requirements

**All services using sms-common MUST**:
1. Add dependency to `pom.xml`:
   ```xml
   <dependency>
       <groupId>com.sms</groupId>
       <artifactId>sms-common</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```

2. **Use optimized Docker build pattern** (MANDATORY):
   - Dockerfile MUST use `FROM sms-common-builder:latest AS common-builder`
   - Do NOT rebuild sms-common in each service Dockerfile
   - See `.standards/docs/docker-build-optimization.md` for template

3. Set Docker Compose context to root directory:
   ```yaml
   build:
     context: .  # Root directory (NOT ./service-name)
     dockerfile: ./service-name/Dockerfile
   ```

4. Build sms-common-builder base image first:
   ```bash
   docker-compose build sms-common-builder  # Only when sms-common changes
   docker-compose build auth-service student-service  # Services use cached base
   ```

**When updating sms-common**:
```bash
cd sms-common
./mvnw clean install -DskipTests
docker-compose build sms-common-builder  # Rebuild base image
docker-compose build  # Rebuild services
```

**For complete details**:
- Common library standards: `.standards/docs/common-library-standards.md`
- Docker optimization: `.standards/docs/docker-build-optimization.md`

---

## Microservice Architecture Standards (MANDATORY)

**Reference Document**: See `/SERVICE_COMPARISON_ANALYSIS.md` for complete details.

**ALL microservices in this project MUST follow these standards. When creating or modifying services, Claude MUST enforce these rules.**

### 1. Spring Profiles (MANDATORY)

**Rule**: Each service MUST have exactly 2 profiles:
- `application.yml` - Default profile for local development
- `application-docker.yml` - Docker profile for containerized deployment

**Docker Compose Configuration**:
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
```

**❌ FORBIDDEN**:
- Creating more than 2 profiles
- Using profile names other than `default` and `docker`
- Using `prod`, `dev`, `test` as profile names in Docker

---

### 2. Environment Variable Naming (MANDATORY)

**Rule**: Use Spring Boot standard property names exclusively.

**Required Format**:
```yaml
# Database
- SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-{service}:5432/{db_name}
- SPRING_DATASOURCE_USERNAME=sms_user
- SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

# Eureka
- EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

# JWT
- JWT_SECRET=${JWT_SECRET}

# Redis (if needed)
- SPRING_REDIS_HOST=redis
```

**❌ FORBIDDEN Variable Names**:
- `DB_USERNAME` (use `SPRING_DATASOURCE_USERNAME`)
- `DB_PASSWORD` (use `SPRING_DATASOURCE_PASSWORD`)
- `EUREKA_CLIENT_SERVICE_URL` (use `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`)
- `EUREKA_INSTANCE_*` as environment variables (configure in YAML)

**When creating/modifying docker-compose.yml**: Claude MUST use Spring Boot standard names.

---

### 3. Eureka Configuration (MANDATORY)

**Rule**: All services MUST use hostname-based Eureka registration.

**Required Configuration** (in application-docker.yml):
```yaml
eureka:
  instance:
    hostname: {service-name}
    prefer-ip-address: false  # MUST be false
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

**Rationale**: Using `prefer-ip-address: false` with hostname prevents multi-network IP registration issues in Docker.

**❌ FORBIDDEN**:
- Setting `prefer-ip-address: true`
- Configuring Eureka instance properties as environment variables
- Allowing IP-based registration

---

### 4. Package Structure (MANDATORY)

**Rule**: All services MUST follow this exact package structure:

```
com.sms.{service}/
├── config/          # Configuration classes ONLY (no business logic)
│   ├── CorsConfig.java
│   ├── OpenAPIConfig.java      # Note: "OpenAPI" not "OpenApi"
│   ├── SecurityConfig.java
│   └── {ServiceSpecific}Config.java
├── controller/      # REST controllers
├── dto/             # Request/Response DTOs
├── exception/       # Custom exceptions & global exception handlers
├── model/           # JPA Entities (MUST be "model/" NOT "entity/")
├── repository/      # JPA Repositories
├── security/        # Security, JWT, Auth filters (NOT in config/)
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
├── service/         # Service interfaces & implementations together
│   ├── {Domain}Service.java
│   └── {Domain}ServiceImpl.java
└── validation/      # Custom validators (optional)
```

**Critical Rules**:
- ✅ Entity package MUST be named `model/` (NOT `entity/`)
- ✅ JWT classes MUST be in `security/` package (NOT `config/`)
- ✅ Service implementations MUST be in `service/` (NOT `service/impl/`)
- ✅ OpenAPI config MUST be named `OpenAPIConfig` (NOT `OpenApiConfig`)

**When creating new services**: Claude MUST create this exact structure.
**When modifying services**: Claude MUST suggest corrections if structure violates standards.

---

### 5. JWT Architecture (MANDATORY)

**Rule**: JWT logic MUST be separated into exactly 2 classes in the `security/` package.

**Required Classes**:

1. **JwtAuthenticationFilter.java** (Filter logic only):
   - Extends `OncePerRequestFilter`
   - Extracts JWT from Authorization header
   - Delegates validation to `JwtTokenProvider`
   - Sets Spring Security context

2. **JwtTokenProvider.java** (Token operations only):
   - Token generation
   - Token parsing and validation
   - Signature verification
   - Claims extraction
   - Expiration checking

**❌ FORBIDDEN**:
- Combining all JWT logic in a single filter class
- Placing JWT classes in `config/` package
- Embedding token validation logic directly in the filter

**When creating JWT authentication**: Claude MUST create both classes with proper separation of concerns.

---

### 6. Required Configuration Classes (MANDATORY)

**Rule**: All services MUST include these configuration classes:

**Minimum Required**:
- ✅ `CorsConfig.java` - CORS settings (allow cross-origin requests)
- ✅ `OpenAPIConfig.java` - Swagger/OpenAPI documentation
- ✅ `SecurityConfig.java` - Spring Security configuration

**Additional (as needed)**:
- `PasswordEncoderConfig.java` - BCrypt password encoder (auth services)
- `RedisConfig.java` - Redis connection settings (if using Redis)
- `FileUploadConfig.java` - File upload settings (if handling uploads)

**When creating a new service**: Claude MUST include at minimum CorsConfig, OpenAPIConfig, and SecurityConfig.

---

### 7. OpenAPI Server Configuration (MANDATORY)

**Rule**: OpenAPI configuration MUST point to API Gateway, NOT direct service port.

**Required Configuration**:
```java
@Bean
public OpenAPI {serviceName}API() {
    Server server = new Server();
    server.setUrl("http://localhost:8080");  // API Gateway port
    server.setDescription("API Gateway");

    return new OpenAPI()
        .servers(List.of(server))
        .info(new Info()
            .title("{Service Name} API")
            .description("...")
            .version("1.0.0"))
        // ... security configuration
}
```

**❌ FORBIDDEN**:
- Pointing to direct service port (e.g., `http://localhost:8082`)
- Using service-specific ports in server URL
- Omitting server configuration

**Rationale**: Prevents CORS errors when Swagger UI calls APIs through the gateway.

---

### 8. Template Service (MANDATORY)

**Rule**: When creating NEW services, use `auth-service` as the template.

**Standard Process**:
1. Copy `auth-service/` directory structure
2. Rename packages from `com.sms.auth` to `com.sms.{newservice}`
3. Update configuration files (application.yml, pom.xml)
4. Verify all standards are met using the checklist in SERVICE_COMPARISON_ANALYSIS.md

**Planned Services** (must follow standards):
- ✅ auth-service (COMPLETED - template service)
- ✅ student-service (COMPLETED - needs migration)
- 📋 attendance-service (PLANNED)
- 📋 grade-service (PLANNED)
- 📋 report-service (PLANNED)
- 📋 notification-service (PLANNED)

---

### 9. Code Review Checklist (MANDATORY)

**When Claude creates or modifies a microservice, verify**:

**Structure**:
- [ ] Exactly 2 profiles (default, docker)
- [ ] Package structure matches standard (model/, security/, service/)
- [ ] JWT split into Filter + Provider in security/ package
- [ ] All required config classes present

**Configuration**:
- [ ] Environment variables use Spring Boot standard names (SPRING_DATASOURCE_*, etc.)
- [ ] Eureka configured with `prefer-ip-address: false`
- [ ] OpenAPI points to API Gateway (port 8080)
- [ ] JWT secret default matches other services

**Code Quality**:
- [ ] Service implementations in `service/` (not `service/impl/`)
- [ ] Entity package named `model/` (not `entity/`)
- [ ] OpenAPI config named `OpenAPIConfig` (not `OpenApiConfig`)
- [ ] CORS configuration included

**Docker**:
- [ ] docker-compose.yml uses `SPRING_PROFILES_ACTIVE=docker`
- [ ] All environment variables follow standard naming
- [ ] JWT_SECRET environment variable included

---

### 10. Migration Status

**Current Status**:
- ✅ Standards documented in SERVICE_COMPARISON_ANALYSIS.md
- ✅ auth-service follows all standards (template)
- ⚠️ student-service needs migration (deferred until after core features)
- ⏳ New services MUST follow standards from day 1

**When to Apply Standards**:
- ✅ **Immediate**: All NEW services created from now on
- ⏳ **Later**: Existing services will be migrated after core features are complete

---

## Enforcement Rules for Claude

**When creating new services**:
1. MUST use auth-service as template
2. MUST follow all package structure requirements
3. MUST use Spring Boot standard environment variable names
4. MUST separate JWT into Filter + Provider
5. MUST include all required configuration classes
6. MUST point OpenAPI to API Gateway

**When modifying existing services**:
1. SHOULD suggest improvements if code violates standards
2. MUST follow standards for any new code added
3. MUST NOT break existing functionality during modifications
4. SHOULD document deviations from standards with rationale

**When user asks to create microservice-related code**:
1. MUST check SERVICE_COMPARISON_ANALYSIS.md for detailed specifications
2. MUST verify against the checklist before considering work complete
3. MUST refuse to create non-standard structures unless explicitly overridden by user

**For all code generation in this project**: Claude MUST strictly adhere to these microservice architecture standards without exception, unless user explicitly requests deviation with clear justification.

---

## Microservice Standards Reference

**IMPORTANT**: Complete standardization framework is now available in `.standards/` directory.

### Quick Links

**For creating new services**:
- **Automation Script**: `.standards/scripts/create-service.sh <service-name> <port> <db-port>`
- **Quick Start Guide**: `.standards/docs/quickstart-service-creation.md`
- **Verification Checklist**: `.standards/docs/service-creation-checklist.md`

**For maintaining existing services**:
- **Component Location**: `.standards/docs/common-locations.md`
- **Cross-Service Changes**: `.standards/docs/cross-service-changes.md`
- **Refactoring Guide**: `.standards/docs/refactoring-checklist.md`

**For questions and troubleshooting**:
- **FAQ**: `.standards/docs/FAQ.md` (50+ questions answered)
- **Troubleshooting**: `.standards/docs/troubleshooting.md`
- **Standards Overview**: `.standards/README.md`

### Validation and Compliance

**Before committing code**:
```bash
# Validate service structure
.standards/scripts/validate-service-structure.sh <service-name>

# Validate all services
.standards/scripts/validate-all-services.sh
```

**All services MUST pass validation** before deployment. See `.standards/docs/service-creation-checklist.md` for complete verification checklist.

### Templates Available

- **Java Classes**: CorsConfig, OpenAPIConfig, SecurityConfig, JwtAuthenticationFilter, JwtTokenProvider
- **Configuration**: application.yml, application-docker.yml, pom.xml
- **Docker**: Dockerfile, docker-compose-entry.yml
- **All templates**: `.standards/templates/`

### Documentation Structure

```
.standards/
├── README.md                          # Overview and quick links
├── CHANGELOG.md                       # Standards evolution
├── docs/                              # All documentation
│   ├── quickstart-service-creation.md # Step-by-step guide
│   ├── service-creation-checklist.md  # Verification checklist
│   ├── common-locations.md            # Where to find components
│   ├── cross-service-changes.md       # Bulk update patterns
│   ├── refactoring-checklist.md       # Safe refactoring
│   ├── FAQ.md                         # Frequently asked questions
│   └── ... (25+ more guides)
├── scripts/                           # Automation tools
│   ├── create-service.sh              # Service creation automation
│   ├── validate-service-structure.sh  # Compliance validation
│   ├── validate-all-services.sh       # Batch validation
│   └── find-component.sh              # Component finder
└── templates/                         # Reusable templates
    ├── java/                          # Java class templates
    ├── Dockerfile                     # Docker template
    ├── pom-template.xml               # Maven template
    └── docker-compose-entry.yml       # Docker Compose template
```

### Success Metrics

- ✅ **Service Creation**: <2 hours from start to deployment-ready
- ✅ **Maintenance Time**: 50% reduction (2-4 hours → 1-2 hours)
- ✅ **Developer Onboarding**: Navigate any service in <30 minutes
- ✅ **Compliance**: 100% validation pass required before deployment

**Version**: 1.0.0 (Released 2025-11-22)

---

<!-- MANUAL ADDITIONS END -->
