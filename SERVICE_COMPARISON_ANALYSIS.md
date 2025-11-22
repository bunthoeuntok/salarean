# Service Comparison Analysis: auth-service vs student-service

**Date**: 2025-11-22
**Purpose**: Identify differences in structure, conventions, and configuration to establish uniform standards across all microservices.

---

## 1. Spring Profiles

### AUTH-SERVICE
- **Available Profiles**:
  - `default` (application.yml)
  - `docker` (application-docker.yml)
- **Active Profile in Docker**: `docker`

### STUDENT-SERVICE
- **Available Profiles**:
  - `default` (application.yml)
  - `dev` (application-dev.yml)
  - `docker` (application-docker.yml)
  - `prod` (application-prod.yml)
- **Active Profile in Docker**: `prod`

### Analysis
- ❌ **INCONSISTENT**: student-service has 4 profiles vs auth-service has 2 profiles
- ❌ **INCONSISTENT**: Different profile names used in Docker (`docker` vs `prod`)
- ⚠️ **Issue**: student-service has unused `docker` profile (using `prod` instead)

---

## 2. Docker Environment Variables

### AUTH-SERVICE
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
  - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-auth:5432/auth_db
  - SPRING_DATASOURCE_USERNAME=sms_user
  - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
  - SPRING_REDIS_HOST=redis
  - JWT_SECRET=${JWT_SECRET}
```

### STUDENT-SERVICE
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod
  - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-student:5432/student_db
  - DB_USERNAME=sms_user
  - DB_PASSWORD=${DB_PASSWORD}
  - EUREKA_CLIENT_SERVICE_URL=http://eureka-server:8761/eureka/
  - EUREKA_INSTANCE_HOSTNAME=student-service
  - EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
  - JWT_SECRET=${JWT_SECRET}
  - UPLOAD_DIR=/app/uploads/students
```

### Analysis
#### ✅ Consistent
- Both use `${JWT_SECRET}` from environment
- Both use `${DB_PASSWORD}` from environment

#### ❌ Inconsistent Database Properties
| Property | auth-service | student-service |
|----------|--------------|-----------------|
| Username | `SPRING_DATASOURCE_USERNAME` | `DB_USERNAME` |
| Password | `SPRING_DATASOURCE_PASSWORD` | `DB_PASSWORD` |

#### ❌ Inconsistent Eureka Properties
| Property | auth-service | student-service |
|----------|--------------|-----------------|
| Service URL | `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | `EUREKA_CLIENT_SERVICE_URL` |
| Instance Hostname | ❌ Not set | ✅ `EUREKA_INSTANCE_HOSTNAME` |
| Prefer IP | ❌ Not set | ✅ `EUREKA_INSTANCE_PREFER_IP_ADDRESS` |

**Note**: auth-service relies on profile YAML for Eureka instance config, while student-service uses environment variables.

---

## 3. Application Configuration (application.yml)

### Database Connection Properties

#### AUTH-SERVICE
```yaml
datasource:
  url: jdbc:postgresql://localhost:5432/auth_db
  username: sms_user
  password: ${DB_PASSWORD:password}
```

#### STUDENT-SERVICE
```yaml
datasource:
  url: jdbc:postgresql://localhost:5433/student_db
  username: ${DB_USERNAME:sms_user}
  password: ${DB_PASSWORD:password}
```

### Analysis
- ❌ **INCONSISTENT**: auth-service uses hardcoded username, student-service uses `${DB_USERNAME:sms_user}`
- ⚠️ **Note**: Different PostgreSQL ports (5432 vs 5433) for local development

---

## 4. Profile-Specific Configuration

### AUTH-SERVICE (application-docker.yml)
```yaml
datasource:
  url: ${SPRING_DATASOURCE_URL}
  username: ${SPRING_DATASOURCE_USERNAME}
  password: ${SPRING_DATASOURCE_PASSWORD}

eureka:
  instance:
    hostname: auth-service
    prefer-ip-address: true
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

### STUDENT-SERVICE (application-prod.yml)
```yaml
datasource:
  url: jdbc:postgresql://postgres-student:5432/student_db
  username: ${DB_USERNAME}
  password: ${DB_PASSWORD}

eureka:
  instance:
    hostname: student-service
    prefer-ip-address: false
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICE_URL:http://eureka-server:8761/eureka/}
```

### Analysis
- ❌ **INCONSISTENT**: auth-service uses `SPRING_DATASOURCE_*` prefix, student-service uses `DB_*` prefix
- ❌ **INCONSISTENT**: auth-service fully externalizes datasource URL, student-service hardcodes it in profile
- ❌ **INCONSISTENT**: Eureka property naming (`SERVICEURL_DEFAULTZONE` vs `SERVICE_URL`)
- ❌ **INCONSISTENT**: Eureka `prefer-ip-address` (auth=true, student=false)

---

## 5. Package Structure

### AUTH-SERVICE
```
com.sms.auth/
├── config/
│   ├── CorsConfig.java
│   ├── OpenAPIConfig.java
│   ├── PasswordEncoderConfig.java
│   ├── RedisConfig.java
│   └── SecurityConfig.java
├── controller/
├── dto/
├── exception/
├── model/           ← Entity/Model naming
├── repository/
├── security/        ← Separate security package
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
├── service/
└── validation/
```

### STUDENT-SERVICE
```
com.sms.student/
├── config/
│   ├── EurekaConfig.java
│   ├── FileUploadConfig.java
│   ├── JwtAuthenticationFilter.java  ← JWT in config package
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
├── controller/
├── dto/
├── entity/          ← Entity/Model naming
├── enums/
├── exception/
├── repository/
└── service/
    └── impl/        ← Service implementation subpackage
```

### Analysis
#### ❌ Inconsistent Package Organization
| Feature | auth-service | student-service |
|---------|--------------|-----------------|
| Entity package name | `model/` | `entity/` |
| JWT classes location | `security/` package | `config/` package |
| Service implementation | In `service/` | In `service/impl/` |
| CORS configuration | ✅ Has `CorsConfig.java` | ❌ Missing |
| Validation package | ✅ Has `validation/` | ❌ Missing |
| Enums package | ❌ Missing | ✅ Has `enums/` |

---

## 6. Security & JWT Configuration

### AUTH-SERVICE Structure
```
security/
├── JwtAuthenticationFilter.java (2.7 KB)
└── JwtTokenProvider.java (2.4 KB)
```
- Separates JWT token creation/validation (`JwtTokenProvider`) from authentication filter
- JWT logic in dedicated `security` package

### STUDENT-SERVICE Structure
```
config/
└── JwtAuthenticationFilter.java (4.7 KB)
```
- All JWT logic in single filter class
- JWT validation embedded in filter
- No separate token provider/utility class

### Analysis
- ❌ **INCONSISTENT**: auth-service separates concerns (filter + provider), student-service combines in one class
- ⚠️ **Issue**: student-service JWT filter is 2x larger (4.7KB vs 2.7KB) due to embedded validation logic

---

## 7. Configuration Classes

### AUTH-SERVICE
- ✅ `CorsConfig.java` - CORS configuration
- ✅ `OpenAPIConfig.java` - Swagger/OpenAPI
- ✅ `PasswordEncoderConfig.java` - BCrypt encoder
- ✅ `RedisConfig.java` - Redis connection
- ✅ `SecurityConfig.java` - Spring Security

### STUDENT-SERVICE
- ✅ `EurekaConfig.java` - Eureka client config
- ✅ `FileUploadConfig.java` - File upload settings
- ✅ `OpenApiConfig.java` - Swagger/OpenAPI
- ✅ `SecurityConfig.java` - Spring Security
- ❌ Missing `CorsConfig.java`
- ❌ Missing `PasswordEncoderConfig.java`

### Analysis
- ❌ **INCONSISTENT**: student-service missing CORS configuration
- ⚠️ **Note**: student-service has service-specific configs (FileUploadConfig, EurekaConfig)

---

## 8. OpenAPI Configuration Naming

| Service | File Name | Class Name |
|---------|-----------|------------|
| auth-service | `OpenAPIConfig.java` | `OpenAPIConfig` |
| student-service | `OpenApiConfig.java` | `OpenApiConfig` |

### Analysis
- ❌ **INCONSISTENT**: Different capitalization (OpenAPI vs OpenApi)
- Standard: `OpenAPI` is the official name

---

## 9. Dependencies (springdoc-openapi)

### Both Services
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.7.0</version>
</dependency>
```

### Analysis
- ✅ **CONSISTENT**: Both use springdoc-openapi 2.7.0

---

## 10. Testing Structure

### AUTH-SERVICE
- **Test Files**: 8 test files
- More comprehensive test coverage

### STUDENT-SERVICE
- **Test Files**: 5 test files
- Less test coverage

### Analysis
- ⚠️ **Different**: auth-service has more tests (possibly better coverage)

---

## 11. Application Properties Defaults

### JWT Configuration
| Service | JWT Secret Default |
|---------|-------------------|
| auth-service | `your-256-bit-secret-key-here-minimum-32-chars` |
| student-service | `your-256-bit-secret-key-for-development-only-change-in-production` |

### Analysis
- ❌ **INCONSISTENT**: Different default values (both need to match for token validation)

---

# RECOMMENDATIONS

## 🏆 Recommended Standard: AUTH-SERVICE Pattern

### Reasons:
1. ✅ **Cleaner separation of concerns** (security package, separate JWT provider)
2. ✅ **Standard Spring Boot naming** (`SPRING_DATASOURCE_*` environment variables)
3. ✅ **CORS configuration included** (essential for web applications)
4. ✅ **Proper OpenAPI naming** (OpenAPI not OpenApi)
5. ✅ **Simpler profile structure** (default + docker)
6. ✅ **Better test coverage**
7. ✅ **Dedicated validation package** (better organization)

---

## 📋 Standardization Checklist

### 1. Profile Configuration (Priority: HIGH)
**Action**: Standardize all services to use 2 profiles:
- `default` (application.yml) - For local development
- `docker` (application-docker.yml) - For Docker deployment

**Changes Needed**:
- ✏️ Rename student-service `application-prod.yml` → `application-docker.yml`
- ✏️ Delete `application-dev.yml` (merge into default)
- ✏️ Update docker-compose.yml: `SPRING_PROFILES_ACTIVE=docker` (not `prod`)

---

### 2. Environment Variable Naming (Priority: HIGH)
**Standard**: Use Spring Boot standard property names

**Changes Needed for student-service**:
```yaml
# BEFORE (student-service)
- DB_USERNAME=sms_user
- DB_PASSWORD=${DB_PASSWORD}
- EUREKA_CLIENT_SERVICE_URL=...

# AFTER (standardized)
- SPRING_DATASOURCE_USERNAME=sms_user
- SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
- EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=...
```

**Rationale**:
- Spring Boot automatically maps `SPRING_DATASOURCE_*` to `spring.datasource.*`
- Follows Spring Boot conventions
- More explicit and self-documenting

---

### 3. Eureka Configuration (Priority: HIGH)
**Standard**: Configure Eureka instance in profile YAML, not environment variables

**Changes Needed for student-service**:
- ✏️ Move `EUREKA_INSTANCE_HOSTNAME` to application-docker.yml
- ✏️ Move `EUREKA_INSTANCE_PREFER_IP_ADDRESS` to application-docker.yml
- ✏️ Remove from docker-compose.yml environment section

**Benefit**: Keeps docker-compose cleaner, configuration in code

---

### 4. Package Structure (Priority: MEDIUM)
**Standard**: Use auth-service package naming

**Changes Needed for all services**:
```
Standardized Package Structure:
com.sms.{service}/
├── config/         (Config classes only, no business logic)
├── controller/     (REST controllers)
├── dto/            (Data Transfer Objects)
├── exception/      (Custom exceptions)
├── model/          (JPA Entities) ← Use "model", not "entity"
├── repository/     (JPA Repositories)
├── security/       (Security, JWT, Auth filters) ← NEW package
├── service/        (Service interfaces & implementations together)
└── validation/     (Custom validators)
```

**Changes for student-service**:
- ✏️ Rename `entity/` → `model/`
- ✏️ Create `security/` package
- ✏️ Move `JwtAuthenticationFilter.java` from `config/` to `security/`
- ✏️ Remove `service/impl/` subpackage (keep implementations in `service/`)
- ✏️ Create `validation/` package for custom validators
- ✏️ Move service-specific enums into `model/` or keep `enums/` if preferred

---

### 5. JWT Architecture (Priority: MEDIUM)
**Standard**: Separate JWT concerns into two classes

**Changes Needed for student-service**:
- ✏️ Create `JwtTokenProvider.java` in `security/` package
  - Extract token parsing, validation, claims extraction
- ✏️ Refactor `JwtAuthenticationFilter.java`
  - Keep only filter logic
  - Delegate to `JwtTokenProvider` for token operations

**Benefit**:
- Single Responsibility Principle
- Reusable JWT utilities
- Easier to test
- Smaller, more maintainable classes

---

### 6. CORS Configuration (Priority: LOW)
**Standard**: Include CORS configuration in all services

**Changes Needed for student-service**:
- ✏️ Copy `CorsConfig.java` from auth-service
- ✏️ Adjust CORS settings as needed

---

### 7. Configuration Class Naming (Priority: LOW)
**Standard**: Use correct OpenAPI capitalization

**Changes Needed for student-service**:
- ✏️ Rename `OpenApiConfig.java` → `OpenAPIConfig.java`
- ✏️ Update class name: `OpenApiConfig` → `OpenAPIConfig`

---

### 8. Default Configuration Values (Priority: HIGH)
**Standard**: Use consistent defaults across services

**Changes Needed**:
- ✏️ Standardize JWT secret default value across all services
- ✏️ Use same default: `your-256-bit-secret-key-here-minimum-32-chars`

---

### 9. Profile YAML External References (Priority: MEDIUM)
**Standard**: Profile YAMLs should externalize ALL environment-specific values

**Changes Needed for student-service (application-docker.yml)**:
```yaml
# BEFORE
datasource:
  url: jdbc:postgresql://postgres-student:5432/student_db

# AFTER
datasource:
  url: ${SPRING_DATASOURCE_URL}
```

**Benefit**: Complete flexibility without code changes

---

## 📊 Summary Table: Recommended Standards

| Aspect | Standard | auth-service | student-service | Action |
|--------|----------|--------------|-----------------|--------|
| Profiles | 2 (default, docker) | ✅ | ❌ (has 4) | Reduce to 2 |
| Active Profile in Docker | `docker` | ✅ | ❌ (`prod`) | Change to `docker` |
| DB Env Var Prefix | `SPRING_DATASOURCE_*` | ✅ | ❌ (`DB_*`) | Change to Spring standard |
| Eureka Env Var | `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | ✅ | ❌ (different) | Standardize |
| Entity Package | `model/` | ✅ | ❌ (`entity/`) | Rename to `model/` |
| JWT Package | `security/` | ✅ | ❌ (`config/`) | Create `security/` |
| JWT Classes | Filter + Provider | ✅ | ❌ (combined) | Split into 2 classes |
| Service Impl | In `service/` | ✅ | ❌ (`service/impl/`) | Flatten structure |
| CORS Config | Has `CorsConfig` | ✅ | ❌ Missing | Add `CorsConfig` |
| OpenAPI Naming | `OpenAPIConfig` | ✅ | ❌ (`OpenApiConfig`) | Fix capitalization |
| Validation Package | Has `validation/` | ✅ | ❌ Missing | Create if needed |

---

## 🎯 Implementation Priority

### Phase 1: Critical (Affects Runtime)
1. ✅ JWT_SECRET environment variable (DONE)
2. Standardize environment variable naming (`SPRING_DATASOURCE_*`, `EUREKA_*`)
3. Profile consolidation (`docker` profile)

### Phase 2: Structural (Affects Maintainability)
4. Package restructuring (`security/`, `model/`, flatten `service/`)
5. JWT class separation (Filter + Provider)
6. Add missing `CorsConfig`

### Phase 3: Polish (Affects Consistency)
7. Fix OpenAPI naming
8. Add validation package
9. Standardize default values
10. Improve test coverage

---

## 🔄 Migration Strategy

**For Existing Services**:
1. Create new `security/` package
2. Copy standardized JWT classes from auth-service
3. Refactor to use new structure
4. Update docker-compose.yml environment variables
5. Rename/consolidate profile files
6. Test thoroughly before deployment

**For New Services**:
- Use auth-service as template
- Follow standardized package structure from day 1
- Use standardized environment variable names

---

## ✅ Benefits of Standardization

1. **Easier Onboarding** - Developers can navigate any service quickly
2. **Reduced Bugs** - Consistent patterns reduce mental overhead
3. **Better Maintainability** - Changes apply uniformly across services
4. **Simplified Deployment** - Same environment variable names everywhere
5. **Code Reusability** - Can share configuration classes between services
6. **Professional Quality** - Follows Spring Boot and industry best practices

---

# CLARIFICATIONS AND DECISIONS

**Date**: 2025-11-22
**Status**: ✅ Decisions Made - Ready for Implementation

---

## Decision 1: Eureka `prefer-ip-address` Configuration

**Question**: Should we use `true` or `false` for `eureka.instance.prefer-ip-address`?

**Decision**: ✅ **Use `false` for all services**

**Rationale**:
- Docker's internal DNS resolves service hostnames (e.g., `student-service`, `auth-service`)
- Using hostname-based discovery avoids multi-network IP registration issues
- More reliable in Docker Swarm/Kubernetes environments
- Prevents the issue where services register with wrong network IP (database-network vs backend-network)

**Standard Configuration**:
```yaml
eureka:
  instance:
    hostname: {service-name}
    prefer-ip-address: false
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

**Note**: This decision was validated by fixing the student-service timeout issue (registered with 172.19.0.x instead of 172.20.0.x).

---

## Decision 2: Migration Timeline

**Question**: Migrate student-service now or wait?

**Decision**: ✅ **WAIT - Do not migrate existing services yet**

**Rationale**:
- Current services are working in development
- Focus on building new features first
- Standardize incrementally to avoid disruption
- Apply standards to NEW services immediately

**Migration Plan**:
1. **Immediate**: Use this document as reference for all NEW services
2. **Phase 1** (After core features complete): Migrate student-service
3. **Phase 2**: Migrate auth-service (minor adjustments)
4. **Phase 3**: Migrate remaining services (attendance, grade, report, notification)

**Timeline**: TBD based on feature delivery schedule

---

## Decision 3: Development Status

**Question**: Production deployment or still in development?

**Answer**: ✅ **Still in Development**

**Impact**:
- ✅ Safe to make breaking changes
- ✅ Can refactor without backward compatibility concerns
- ✅ No need to maintain old environment variable names
- ✅ Clean slate for standardization

**Action**: When standardizing, make all changes at once (no gradual migration needed).

---

## Decision 4: Entity Package Naming

**Question**: Use `model/` or `entity/` package?

**Decision**: ✅ **Use `model/` package**

**Rationale**:
- auth-service uses `model/` (consistency)
- More generic and flexible naming
- Can include both JPA entities and domain models
- Common in Spring Boot examples and documentation
- Aligns with Domain-Driven Design terminology

**Standard**:
```
com.sms.{service}/
└── model/
    ├── User.java
    ├── RefreshToken.java
    └── ...
```

**Migration**: Rename `student-service/src/main/java/com/sms/student/entity/` → `model/`

---

## Decision 5: Service Implementation Package Structure

**Question**: Use `service/impl/` subpackage or flatten into `service/`?

**Decision**: ✅ **Use Recommended Pattern - Flatten into `service/` package**

**Standard Structure**:
```
service/
├── StudentService.java          (interface)
├── StudentServiceImpl.java      (implementation)
├── PhotoService.java             (interface)
└── PhotoServiceImpl.java         (implementation)
```

**NOT**:
```
service/
├── StudentService.java
└── impl/
    └── StudentServiceImpl.java
```

**Rationale**:
- Simpler package structure
- Easier to navigate (interfaces and implementations together)
- Follows auth-service pattern
- Common Spring Boot convention
- Less nesting = better readability

**Migration**: Move all classes from `service/impl/` up to `service/` and delete `impl/` folder.

---

## Decision 6: Standardization Priority

**Question**: Which approach to take?

**Decision**: ✅ **Option B - Full Standardization (1-2 hours)**

**Scope**:
1. ✅ Fix environment variable names (`SPRING_DATASOURCE_*`, `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`)
2. ✅ Consolidate to `docker` profile only
3. ✅ Rename `OpenApiConfig` → `OpenAPIConfig`
4. ✅ Restructure packages (`security/`, `model/`)
5. ✅ Split JWT classes (Filter + Provider)
6. ✅ Add `CorsConfig`
7. ✅ Flatten `service/impl/` into `service/`
8. ✅ Add `validation/` package if needed

**Timeline**: Execute when ready to standardize (not immediate, but comprehensive when done).

---

## Decision 7: Future Services

**Question**: Are there more services to build?

**Answer**: ✅ **YES - Multiple services planned**

**Planned Services**:
- ✅ auth-service (COMPLETED)
- ✅ student-service (COMPLETED)
- 📋 attendance-service (PLANNED)
- 📋 grade-service (PLANNED)
- 📋 report-service (PLANNED)
- 📋 notification-service (PLANNED)

**Action**:
- All NEW services MUST follow standardized structure from day 1
- Use auth-service as template
- Copy configuration classes from standardized template
- Follow package naming conventions defined in this document

---

# FINAL STANDARDIZATION REFERENCE

## 🎯 Official Standards (Effective Immediately for New Services)

### 1. Spring Profiles
```yaml
# ONLY 2 profiles allowed
application.yml          # default profile (local development)
application-docker.yml   # docker profile (Docker deployment)
```

**Docker Compose**:
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
```

---

### 2. Environment Variable Naming
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

# Service-specific
- UPLOAD_DIR=/app/uploads/{service}
```

**❌ DO NOT USE**:
- `DB_USERNAME` (use `SPRING_DATASOURCE_USERNAME`)
- `DB_PASSWORD` (use `SPRING_DATASOURCE_PASSWORD`)
- `EUREKA_CLIENT_SERVICE_URL` (use `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`)
- `EUREKA_INSTANCE_*` as env vars (configure in YAML instead)

---

### 3. Eureka Configuration
```yaml
# application-docker.yml
eureka:
  instance:
    hostname: {service-name}
    prefer-ip-address: false  # MUST be false
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

**Do NOT** configure Eureka instance in environment variables. Keep in YAML.

---

### 4. Package Structure (MANDATORY)
```
com.sms.{service}/
├── config/          # Configuration classes ONLY
│   ├── CorsConfig.java
│   ├── OpenAPIConfig.java        # Note: OpenAPI not OpenApi
│   ├── SecurityConfig.java
│   └── {ServiceSpecific}Config.java
├── controller/      # REST controllers
├── dto/             # Request/Response DTOs
├── exception/       # Custom exceptions & handlers
├── model/           # JPA Entities (NOT 'entity' package)
├── repository/      # JPA Repositories
├── security/        # Security, JWT, Auth filters
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
├── service/         # Service interfaces & implementations
│   ├── {Domain}Service.java
│   └── {Domain}ServiceImpl.java
└── validation/      # Custom validators (optional)
```

**Key Rules**:
- ✅ Use `model/` NOT `entity/`
- ✅ JWT classes go in `security/` NOT `config/`
- ✅ Service implementations stay in `service/` NOT `service/impl/`
- ✅ OpenAPI config is `OpenAPIConfig` NOT `OpenApiConfig`

---

### 5. JWT Architecture (MANDATORY)
```
security/
├── JwtAuthenticationFilter.java  # Filter logic only
└── JwtTokenProvider.java         # Token creation, parsing, validation
```

**Separation of Concerns**:
- **JwtAuthenticationFilter**:
  - Extends `OncePerRequestFilter`
  - Extracts token from request
  - Delegates validation to `JwtTokenProvider`
  - Sets `SecurityContextHolder`

- **JwtTokenProvider**:
  - Token generation
  - Token parsing
  - Signature validation
  - Claims extraction
  - Expiration checking

**❌ DO NOT** combine all JWT logic in one filter class.

---

### 6. Required Configuration Classes
```
config/
├── CorsConfig.java              # CORS settings
├── OpenAPIConfig.java           # Swagger/OpenAPI docs
├── PasswordEncoderConfig.java   # BCrypt encoder (if needed)
├── SecurityConfig.java          # Spring Security
└── {Redis/FileUpload/etc}Config.java  # Service-specific
```

All services MUST have:
- ✅ `CorsConfig.java`
- ✅ `OpenAPIConfig.java`
- ✅ `SecurityConfig.java`

---

### 7. OpenAPI Server Configuration
```java
@Bean
public OpenAPI {serviceName}API() {
    Server server = new Server();
    server.setUrl("http://localhost:8080");  // API Gateway URL
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

**MUST** point to API Gateway (port 8080), NOT direct service port.

---

### 8. Default Configuration Values

**JWT Secret** (application.yml):
```yaml
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here-minimum-32-chars}
  expiration: 86400000      # 24 hours
  refresh-expiration: 2592000000  # 30 days (if using refresh tokens)
```

**All services MUST use the same default value for consistency.**

---

## 📋 New Service Checklist

When creating a new service, ensure:

### Structure
- [ ] Uses 2 profiles only (default, docker)
- [ ] Package structure follows standard (model/, security/, service/)
- [ ] JWT split into Filter + Provider
- [ ] All config classes present (CORS, OpenAPI, Security)

### Configuration
- [ ] Environment variables use Spring Boot standard names
- [ ] Eureka configured with `prefer-ip-address: false`
- [ ] OpenAPI points to API Gateway (port 8080)
- [ ] JWT secret default matches other services

### Code Quality
- [ ] Service implementations in `service/` (not `service/impl/`)
- [ ] OpenAPI config named `OpenAPIConfig` (not `OpenApiConfig`)
- [ ] Tests included from day 1
- [ ] Follows naming conventions

### Docker
- [ ] docker-compose.yml uses `SPRING_PROFILES_ACTIVE=docker`
- [ ] All environment variables follow standard naming
- [ ] Networks configured (backend-network, database-network, etc.)
- [ ] Health checks configured

---

## 🚀 Implementation Guide for New Services

### Step 1: Copy Template
```bash
# Use auth-service as template
cp -r auth-service new-service
cd new-service
```

### Step 2: Rename Packages
```bash
# Rename com.sms.auth → com.sms.{newservice}
find . -type f -name "*.java" -exec sed -i '' 's/com.sms.auth/com.sms.newservice/g' {} +
```

### Step 3: Update Configuration
- Update `application.yml` (service name, port)
- Update `application-docker.yml` (datasource, service name)
- Update `pom.xml` (artifactId, name, description)

### Step 4: Verify Standards
- Run checklist above
- Ensure all standards are met
- Test locally before Docker deployment

---

## ✅ Migration Priority for Existing Services

**When ready to standardize student-service** (full Option B migration):

### Phase 1: Configuration (15 min)
1. Consolidate profiles to 2 (default, docker)
2. Update environment variable names in docker-compose.yml
3. Update Eureka configuration

### Phase 2: Package Restructuring (30 min)
4. Rename `entity/` → `model/`
5. Create `security/` package
6. Move `JwtAuthenticationFilter` from `config/` to `security/`
7. Flatten `service/impl/` into `service/`
8. Rename `OpenApiConfig` → `OpenAPIConfig`

### Phase 3: JWT Refactoring (30 min)
9. Create `JwtTokenProvider.java`
10. Extract token operations from filter
11. Refactor filter to use provider

### Phase 4: Missing Configs (15 min)
12. Add `CorsConfig.java`
13. Add `validation/` package if needed
14. Verify all required config classes present

### Phase 5: Testing (15 min)
15. Rebuild Docker image
16. Test all endpoints
17. Verify JWT authentication works
18. Check Swagger UI integration

**Total Estimated Time: 1.5-2 hours**

---

**Status**: This document is now the official reference for all microservice development in the SMS project.

**Last Updated**: 2025-11-22
