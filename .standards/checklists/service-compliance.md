# Microservice Compliance Checklist

**Version**: 1.0.0
**Date**: 2025-11-22
**Purpose**: Validate microservice architecture compliance with Salarean SMS standards
**Template Service**: auth-service

---

## How to Use This Checklist

1. **Manual Review**: Go through each category and check items manually
2. **Automated Validation**: Run the validation script for automated checks
3. **Compliance Score**: Service MUST pass ALL checks to be considered compliant
4. **Code Review**: Use this checklist during pull request reviews

**Legend**:
- ✅ **PASS**: Item meets requirements
- ❌ **FAIL**: Item does not meet requirements
- 🔍 **MANUAL**: Requires manual inspection (cannot be automated)
- 🤖 **AUTO**: Can be automated via validation script

---

## Service Information

**Service Name**: ________________________
**Reviewer**: ________________________
**Date**: ________________________
**Total Checks**: 32
**Checks Passed**: ____ / 32

---

## Category 1: Profile Configuration (3 checks)

### PROFILE-001: Profile Count Validation 🤖
- [ ] Service has exactly 2 Spring profile files

**Pass Criteria**:
```
src/main/resources/
├── application.yml          ✅ MUST exist
├── application-docker.yml   ✅ MUST exist
└── (no other application-*.yml files)
```

**Fail Examples**:
- ❌ Has application-dev.yml, application-prod.yml (too many profiles)
- ❌ Missing application-docker.yml
- ❌ Has application-test.yml (unless specifically needed for testing)

**Rationale**: Simplifies configuration management and prevents profile sprawl.

---

### PROFILE-002: Profile Naming Convention 🤖
- [ ] Profile files use standard names: `application.yml`, `application-docker.yml`

**Pass Criteria**:
- ✅ Default profile: `application.yml`
- ✅ Docker profile: `application-docker.yml`

**Fail Examples**:
- ❌ `application-prod.yml` (use docker, not prod)
- ❌ `application-production.yml`
- ❌ `application-container.yml`

**Rationale**: Profile name describes deployment method (Docker), not environment (prod/dev).

---

### PROFILE-003: Docker Profile Activation 🤖
- [ ] docker-compose.yml sets `SPRING_PROFILES_ACTIVE=docker`

**Pass Criteria**:
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker  ✅ CORRECT
```

**Fail Examples**:
```yaml
- SPRING_PROFILES_ACTIVE=prod     ❌ WRONG
- SPRING_PROFILES_ACTIVE=production  ❌ WRONG
```

**Rationale**: Consistent activation across all services.

---

## Category 2: Environment Variable Naming (5 checks)

### ENV-001: Database Variable Naming (Spring Boot Standard) 🤖
- [ ] Database environment variables use `SPRING_DATASOURCE_*` prefix

**Pass Criteria**:
```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:postgresql://...       ✅ CORRECT
  - SPRING_DATASOURCE_USERNAME=sms_user               ✅ CORRECT
  - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}         ✅ CORRECT
```

**Fail Examples**:
```yaml
- DB_URL=jdbc:postgresql://...          ❌ WRONG
- DB_USERNAME=sms_user                  ❌ WRONG
- DATABASE_PASSWORD=${DB_PASSWORD}      ❌ WRONG
```

**Rationale**: Spring Boot automatically binds `SPRING_DATASOURCE_*` to datasource properties.

---

### ENV-002: Eureka Variable Naming (Standard) 🤖
- [ ] Eureka service URL uses `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`

**Pass Criteria**:
```yaml
environment:
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/  ✅ CORRECT
```

**Fail Examples**:
```yaml
- EUREKA_CLIENT_SERVICE_URL=...                ❌ WRONG (underscore in SERVICE_URL)
- EUREKA_URL=...                               ❌ WRONG
- EUREKA_DEFAULT_ZONE=...                      ❌ WRONG
```

**Rationale**: Standard Spring Cloud Eureka property name.

---

### ENV-003: Forbidden Custom Database Variables 🤖
- [ ] Service does NOT use custom database variable names

**Pass Criteria**:
- ✅ No `DB_USERNAME` environment variable
- ✅ No `DB_PASSWORD` environment variable (use `SPRING_DATASOURCE_PASSWORD`)
- ✅ No `DB_URL` environment variable

**Fail Examples**:
```yaml
environment:
  - DB_USERNAME=sms_user                  ❌ FORBIDDEN
  - DB_PASSWORD=${DB_PASSWORD}            ❌ FORBIDDEN
```

**Rationale**: Custom names require manual wiring and don't follow Spring Boot conventions.

---

### ENV-004: Eureka Instance Configuration Location 🔍
- [ ] Eureka instance properties are in YAML, NOT environment variables

**Pass Criteria**:
```yaml
# application-docker.yml
eureka:
  instance:
    hostname: {service-name}         ✅ CORRECT (in YAML)
    prefer-ip-address: false         ✅ CORRECT (in YAML)
```

**Fail Examples**:
```yaml
# docker-compose.yml
environment:
  - EUREKA_INSTANCE_HOSTNAME=service-name              ❌ WRONG
  - EUREKA_INSTANCE_PREFER_IP_ADDRESS=false            ❌ WRONG
```

**Rationale**: Instance config is deployment-method specific (belongs in docker profile), not environment-specific.

---

### ENV-005: Redis Variable Naming (If Applicable) 🤖
- [ ] If service uses Redis, variables use `SPRING_REDIS_*` prefix

**Pass Criteria** (if Redis is used):
```yaml
environment:
  - SPRING_REDIS_HOST=redis          ✅ CORRECT
  - SPRING_REDIS_PORT=6379           ✅ CORRECT
```

**Fail Examples**:
```yaml
- REDIS_HOST=redis                   ❌ WRONG
- REDIS_URL=redis://...              ❌ WRONG
```

**N/A**: Check if service doesn't use Redis

**Rationale**: Consistency with Spring Boot Redis auto-configuration.

---

## Category 3: Package Structure (6 checks)

### PKG-001: Entity Package Naming 🤖
- [ ] JPA entities are in `model/` package, NOT `entity/`

**Pass Criteria**:
```
com.sms.{service}/
└── model/                           ✅ CORRECT
    ├── User.java
    ├── RefreshToken.java
    └── ...
```

**Fail Examples**:
```
com.sms.{service}/
└── entity/                          ❌ WRONG
    └── User.java
```

**Rationale**: Consistent with auth-service template and domain-driven design terminology.

---

### PKG-002: JWT Package Location 🤖
- [ ] JWT classes are in `security/` package, NOT `config/`

**Pass Criteria**:
```
com.sms.{service}/
└── security/                        ✅ CORRECT
    ├── JwtAuthenticationFilter.java
    └── JwtTokenProvider.java
```

**Fail Examples**:
```
com.sms.{service}/
└── config/                          ❌ WRONG
    ├── JwtAuthenticationFilter.java
    └── SecurityConfig.java
```

**Rationale**: JWT logic is security concern, not configuration.

---

### PKG-003: Service Implementation Location 🤖
- [ ] Service implementations are in `service/` package, NOT `service/impl/`

**Pass Criteria**:
```
com.sms.{service}/
└── service/                         ✅ CORRECT
    ├── UserService.java
    └── UserServiceImpl.java
```

**Fail Examples**:
```
com.sms.{service}/
└── service/
    ├── UserService.java
    └── impl/                        ❌ WRONG
        └── UserServiceImpl.java
```

**Rationale**: Flatter structure is easier to navigate; interfaces and implementations belong together.

---

### PKG-004: Standard Package Presence 🤖
- [ ] All required standard packages exist

**Pass Criteria**:
```
com.sms.{service}/
├── config/          ✅ REQUIRED
├── controller/      ✅ REQUIRED
├── dto/             ✅ REQUIRED
├── exception/       ✅ REQUIRED
├── model/           ✅ REQUIRED
├── repository/      ✅ REQUIRED
├── security/        ✅ REQUIRED (if service handles auth)
├── service/         ✅ REQUIRED
└── validation/      ○ OPTIONAL (but recommended)
```

**Fail Examples**:
- ❌ Missing `exception/` package
- ❌ Missing `dto/` package

**Rationale**: Consistent structure across all services improves navigability.

---

### PKG-005: Config Package Purity 🔍
- [ ] `config/` package contains ONLY configuration classes (no business logic)

**Pass Criteria**:
- ✅ CorsConfig.java - Configuration only
- ✅ OpenAPIConfig.java - Configuration only
- ✅ SecurityConfig.java - Configuration only
- ❌ NO service logic, NO JWT validation logic

**Fail Examples**:
```java
// ❌ WRONG: Business logic in config package
@Configuration
public class UserConfig {
    public User createDefaultUser() {  // Business logic doesn't belong here
        return new User(...);
    }
}
```

**Rationale**: Separation of concerns - config classes should only wire beans.

---

### PKG-006: Validation Package (Optional but Recommended) 🤖
- [ ] If custom validators exist, they are in `validation/` package

**Pass Criteria**:
```
com.sms.{service}/
└── validation/                      ✅ CORRECT
    ├── PhoneNumberValidator.java
    └── EmailValidator.java
```

**N/A**: Check if service has no custom validators

**Rationale**: Centralizes validation logic for reusability.

---

## Category 4: JWT Architecture (4 checks)

### JWT-001: JWT Class Separation 🤖
- [ ] JWT logic is split into Filter and Provider classes

**Pass Criteria**:
```
security/
├── JwtAuthenticationFilter.java     ✅ Filter logic
└── JwtTokenProvider.java            ✅ Token operations
```

**Fail Examples**:
```
security/
└── JwtAuthenticationFilter.java     ❌ WRONG (all logic in one class)
```

**Rationale**: Single Responsibility Principle - Filter handles HTTP, Provider handles tokens.

---

### JWT-002: Filter Class Inheritance 🔍
- [ ] `JwtAuthenticationFilter` extends `OncePerRequestFilter`

**Pass Criteria**:
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // ✅ CORRECT
}
```

**Fail Examples**:
```java
public class JwtAuthenticationFilter implements Filter {
    // ❌ WRONG (should use OncePerRequestFilter)
}
```

**Rationale**: `OncePerRequestFilter` ensures filter executes once per request, even with forwards.

---

### JWT-003: Provider Responsibilities 🔍
- [ ] `JwtTokenProvider` handles token operations ONLY

**Pass Criteria**:
- ✅ Token generation
- ✅ Token parsing
- ✅ Signature validation
- ✅ Claims extraction
- ✅ Expiration checking
- ❌ NO HTTP request handling
- ❌ NO SecurityContext manipulation

**Fail Examples**:
```java
// ❌ WRONG: HTTP logic in provider
public class JwtTokenProvider {
    public void authenticateRequest(HttpServletRequest request) {
        // HTTP handling doesn't belong here
    }
}
```

**Rationale**: Provider should be a pure utility class with no framework dependencies.

---

### JWT-004: Filter Delegation Pattern 🔍
- [ ] Filter delegates token operations to Provider

**Pass Criteria**:
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;  ✅ Uses provider

    @Override
    protected void doFilterInternal(...) {
        String token = extractToken(request);
        if (jwtTokenProvider.validateToken(token)) {  ✅ Delegates validation
            // Set security context
        }
    }
}
```

**Fail Examples**:
```java
// ❌ WRONG: Filter does token validation itself
if (Jwts.parserBuilder()...) {  // Token logic embedded in filter
    // ...
}
```

**Rationale**: Clean separation allows reusing provider in other contexts (e.g., WebSocket auth).

---

## Category 5: Required Configuration Classes (4 checks)

### CFG-001: CorsConfig Presence 🤖
- [ ] Service has `CorsConfig.java` in `config/` package

**Pass Criteria**:
```
config/
└── CorsConfig.java                  ✅ REQUIRED
```

**Fail Examples**:
- ❌ File missing entirely
- ❌ CORS configured inline in SecurityConfig (should be separate)

**Rationale**: CORS is essential for web applications; should be explicitly configured.

---

### CFG-002: OpenAPIConfig Presence and Naming 🤖
- [ ] Service has `OpenAPIConfig.java` (correct capitalization)

**Pass Criteria**:
```
config/
└── OpenAPIConfig.java               ✅ CORRECT (note: "API" not "Api")
```

**Fail Examples**:
```
config/
└── OpenApiConfig.java               ❌ WRONG (incorrect capitalization)
```

**Rationale**: "OpenAPI" is the official name (not "OpenApi").

---

### CFG-003: SecurityConfig Presence 🤖
- [ ] Service has `SecurityConfig.java` in `config/` package

**Pass Criteria**:
```
config/
└── SecurityConfig.java              ✅ REQUIRED
```

**Fail Examples**:
- ❌ File missing
- ❌ Security configured in Application.java main class

**Rationale**: Security configuration should be in dedicated config class.

---

### CFG-004: Service-Specific Configs (Optional) 🔍
- [ ] Service-specific configs are properly named and scoped

**Pass Criteria**:
```
config/
├── PasswordEncoderConfig.java       ✅ Auth-related services
├── RedisConfig.java                 ✅ If service uses Redis
├── FileUploadConfig.java            ✅ If service handles uploads
└── ...
```

**N/A**: Check if service has no special configuration needs

**Rationale**: Clear naming indicates purpose.

---

## Category 6: OpenAPI Configuration (3 checks)

### API-001: Server URL Points to API Gateway 🔍
- [ ] OpenAPI server URL points to API Gateway, NOT direct service port

**Pass Criteria**:
```java
@Bean
public OpenAPI serviceAPI() {
    Server server = new Server();
    server.setUrl("http://localhost:8080");        ✅ CORRECT (API Gateway port)
    server.setDescription("API Gateway");
    // ...
}
```

**Fail Examples**:
```java
server.setUrl("http://localhost:8082");           ❌ WRONG (direct service port)
server.setUrl("http://localhost:8083");           ❌ WRONG (direct service port)
```

**Rationale**: Prevents CORS errors when Swagger UI calls APIs through gateway.

---

### API-002: OpenAPI Info Configuration 🔍
- [ ] OpenAPI has proper title, description, and version

**Pass Criteria**:
```java
return new OpenAPI()
    .servers(List.of(server))
    .info(new Info()
        .title("Student Service API")              ✅ Clear title
        .description("API for student management") ✅ Description present
        .version("1.0.0"))                         ✅ Version specified
```

**Fail Examples**:
```java
.info(new Info()
    .title("API")                                 ❌ WRONG (too generic)
    .version("v1"))                               ❌ WRONG (use semantic versioning)
```

**Rationale**: Proper documentation improves API discoverability.

---

### API-003: Security Scheme Configuration (If Applicable) 🔍
- [ ] If service uses JWT, OpenAPI includes security scheme

**Pass Criteria**:
```java
.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
.components(new Components()
    .addSecuritySchemes("bearerAuth",
        new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")))
```

**N/A**: Check if service doesn't require authentication

**Rationale**: Swagger UI should allow testing authenticated endpoints.

---

## Category 7: Eureka Configuration (3 checks)

### EUR-001: Prefer IP Address Setting 🔍
- [ ] `eureka.instance.prefer-ip-address` is set to `false`

**Pass Criteria**:
```yaml
# application-docker.yml
eureka:
  instance:
    prefer-ip-address: false         ✅ CORRECT
```

**Fail Examples**:
```yaml
eureka:
  instance:
    prefer-ip-address: true          ❌ WRONG
```

**Rationale**: Hostname-based registration prevents multi-network IP issues in Docker.

---

### EUR-002: Hostname Configuration 🔍
- [ ] `eureka.instance.hostname` matches service name

**Pass Criteria**:
```yaml
# For auth-service
eureka:
  instance:
    hostname: auth-service           ✅ CORRECT
```

**Fail Examples**:
```yaml
eureka:
  instance:
    hostname: localhost              ❌ WRONG
```

**Rationale**: Hostname should match Docker service name for DNS resolution.

---

### EUR-003: Instance Config in YAML, Not Env Vars 🤖
- [ ] Eureka instance properties are in `application-docker.yml`, NOT docker-compose.yml

**Pass Criteria**:
```yaml
# application-docker.yml ✅ CORRECT
eureka:
  instance:
    hostname: auth-service
    prefer-ip-address: false
```

**Fail Examples**:
```yaml
# docker-compose.yml ❌ WRONG
environment:
  - EUREKA_INSTANCE_HOSTNAME=auth-service
  - EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
```

**Rationale**: Instance config is deployment-method specific (Docker profile), not environment-specific.

---

## Category 8: Docker Compose Configuration (4 checks)

### DCK-001: Profile Activation 🤖
- [ ] docker-compose.yml sets `SPRING_PROFILES_ACTIVE=docker`

**Pass Criteria**:
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker    ✅ CORRECT
```

**Fail Examples**:
```yaml
- SPRING_PROFILES_ACTIVE=prod        ❌ WRONG
```

**Rationale**: Consistent across all services (covered in PROFILE-003, but worth double-checking).

---

### DCK-002: Standard Environment Variables 🤖
- [ ] docker-compose.yml uses all required standard variable names

**Pass Criteria**:
```yaml
environment:
  - SPRING_DATASOURCE_URL=...                       ✅ CORRECT
  - SPRING_DATASOURCE_USERNAME=sms_user             ✅ CORRECT
  - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}       ✅ CORRECT
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=...        ✅ CORRECT
  - JWT_SECRET=${JWT_SECRET}                        ✅ CORRECT
```

**Fail Examples**:
- ❌ Uses `DB_USERNAME` instead of `SPRING_DATASOURCE_USERNAME`

**Rationale**: Enforces standard naming convention.

---

### DCK-003: Network Configuration 🔍
- [ ] Service connects to appropriate Docker networks

**Pass Criteria**:
```yaml
networks:
  - backend-network          ✅ REQUIRED (for Eureka/API Gateway)
  - database-network         ✅ REQUIRED (if service has database)
```

**Fail Examples**:
- ❌ Missing `backend-network`
- ❌ Connected to only one network when database is needed

**Rationale**: Proper network segmentation for service communication.

---

### DCK-004: Dependencies Declaration 🔍
- [ ] Service declares dependencies on required services

**Pass Criteria**:
```yaml
depends_on:
  - postgres-{service}       ✅ If service has database
  - eureka-server            ✅ Always required
  - redis                    ✅ If service uses Redis
```

**Fail Examples**:
- ❌ Missing `eureka-server` dependency
- ❌ Service starts before database is ready

**Rationale**: Ensures services start in correct order.

---

## Summary Scorecard

| Category | Checks | Passed | Failed |
|----------|--------|--------|--------|
| 1. Profile Configuration | 3 | ___ | ___ |
| 2. Environment Variable Naming | 5 | ___ | ___ |
| 3. Package Structure | 6 | ___ | ___ |
| 4. JWT Architecture | 4 | ___ | ___ |
| 5. Required Configuration Classes | 4 | ___ | ___ |
| 6. OpenAPI Configuration | 3 | ___ | ___ |
| 7. Eureka Configuration | 3 | ___ | ___ |
| 8. Docker Compose Configuration | 4 | ___ | ___ |
| **TOTAL** | **32** | ___ | ___ |

---

## Compliance Status

- [ ] **COMPLIANT**: All 32 checks passed (100%)
- [ ] **NEEDS WORK**: ___ checks failed
- [ ] **CRITICAL FAILURES**: ___ critical checks failed (Profile, Environment Vars, JWT)

---

## Critical vs Non-Critical Checks

### CRITICAL (Must fix immediately)
- All Category 1 (Profile Configuration)
- All Category 2 (Environment Variable Naming)
- All Category 4 (JWT Architecture)
- EUR-001 (prefer-ip-address setting)

### HIGH Priority (Fix before production)
- PKG-001, PKG-002, PKG-003 (Package structure)
- CFG-001, CFG-002, CFG-003 (Required configs)
- API-001 (OpenAPI server URL)

### MEDIUM Priority (Fix soon)
- PKG-004, PKG-005, PKG-006 (Package completeness)
- API-002, API-003 (OpenAPI documentation)
- EUR-002, EUR-003 (Eureka details)

### LOW Priority (Good to have)
- CFG-004 (Service-specific configs)
- DCK-003, DCK-004 (Docker network/dependency refinements)

---

## Automated vs Manual Checks

### Automated (22 checks) 🤖
Can be verified by `validation-script.sh`:
- PROFILE-001, PROFILE-002, PROFILE-003
- ENV-001, ENV-002, ENV-003, ENV-005
- PKG-001, PKG-002, PKG-003, PKG-004, PKG-006
- JWT-001
- CFG-001, CFG-002, CFG-003
- EUR-003
- DCK-001, DCK-002

### Manual Review (10 checks) 🔍
Require human inspection:
- ENV-004 (YAML vs env var location)
- PKG-005 (Config package purity)
- JWT-002, JWT-003, JWT-004 (JWT implementation details)
- CFG-004 (Service-specific configs)
- API-001, API-002, API-003 (OpenAPI configuration)
- EUR-001, EUR-002 (Eureka settings)
- DCK-003, DCK-004 (Docker networking)

---

## Action Items

**For Services That Fail Compliance**:

1. Run automated validation script:
   ```bash
   ./specs/001-service-standards/contracts/validation-script.sh {service-directory}
   ```

2. Review this checklist manually

3. Prioritize fixes:
   - **First**: Fix all CRITICAL failures
   - **Second**: Fix HIGH priority issues
   - **Third**: Address MEDIUM and LOW priority items

4. Re-run validation after fixes

5. Update service documentation with compliance date

---

## Migration Guide Reference

For services that need standardization, see:
- `/Volumes/DATA/my-projects/salarean/SERVICE_COMPARISON_ANALYSIS.md` (Section: Migration Strategy)
- `/Volumes/DATA/my-projects/salarean/specs/001-service-standards/contracts/service-template.md`

---

## Template Service Reference

**Canonical Example**: `auth-service`
**Path**: `/Volumes/DATA/my-projects/salarean/auth-service`

When in doubt, replicate auth-service structure.

---

**Checklist Version**: 1.0.0
**Last Updated**: 2025-11-22
**Maintained By**: Salarean Development Team
