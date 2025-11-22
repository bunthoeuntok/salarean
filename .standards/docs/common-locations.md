# Common Code Locations Guide

## Purpose

This guide helps developers quickly locate equivalent code across microservices by mapping feature types to their standard package locations. Use this as a reference when:

- Applying cross-service bug fixes
- Implementing consistent features across multiple services
- Reviewing code during PRs
- Onboarding new developers

---

## Quick Reference Table

| Feature Type | Package Location | File Pattern | Example |
|--------------|------------------|--------------|---------|
| JWT Token Operations | `security/` | `JwtTokenProvider.java` | auth-service/src/main/java/com/sms/auth/security/JwtTokenProvider.java |
| JWT Request Filtering | `security/` | `JwtAuthenticationFilter.java` | student-service/src/main/java/com/sms/student/security/JwtAuthenticationFilter.java |
| CORS Configuration | `config/` | `CorsConfig.java` | auth-service/src/main/java/com/sms/auth/config/CorsConfig.java |
| OpenAPI/Swagger Setup | `config/` | `OpenAPIConfig.java` | student-service/src/main/java/com/sms/student/config/OpenAPIConfig.java |
| Spring Security Config | `config/` | `SecurityConfig.java` | auth-service/src/main/java/com/sms/auth/config/SecurityConfig.java |
| Password Encoding | `config/` | `PasswordEncoderConfig.java` | auth-service/src/main/java/com/sms/auth/config/PasswordEncoderConfig.java |
| JPA Entities | `model/` | `{Entity}.java` | student-service/src/main/java/com/sms/student/model/Student.java |
| JPA Repositories | `repository/` | `{Entity}Repository.java` | auth-service/src/main/java/com/sms/auth/repository/TeacherRepository.java |
| Business Logic | `service/` | `{Domain}Service.java` | student-service/src/main/java/com/sms/student/service/StudentService.java |
| Service Implementation | `service/` | `{Domain}ServiceImpl.java` | auth-service/src/main/java/com/sms/auth/service/AuthService.java |
| REST Controllers | `controller/` | `{Domain}Controller.java` | student-service/src/main/java/com/sms/student/controller/StudentController.java |
| Request/Response DTOs | `dto/` | `{Purpose}Request.java`, `{Purpose}Response.java` | auth-service/src/main/java/com/sms/auth/dto/LoginRequest.java |
| Custom Exceptions | `exception/` | `{Type}Exception.java` | auth-service/src/main/java/com/sms/auth/exception/InvalidCredentialsException.java |
| Global Exception Handler | `exception/` | `GlobalExceptionHandler.java` | student-service/src/main/java/com/sms/student/exception/GlobalExceptionHandler.java |
| Custom Validators | `validation/` | `{Purpose}Validator.java` | auth-service/src/main/java/com/sms/auth/validation/PhoneNumberValidator.java |
| Local Profile Config | `src/main/resources/` | `application.yml` | auth-service/src/main/resources/application.yml |
| Docker Profile Config | `src/main/resources/` | `application-docker.yml` | student-service/src/main/resources/application-docker.yml |
| Application Entry Point | `{service root}` | `{Service}Application.java` | auth-service/src/main/java/com/sms/auth/AuthServiceApplication.java |

---

## Detailed Location Map

### Security & Authentication

#### JWT Token Provider
**Location**: `{service}/src/main/java/com/sms/{service}/security/JwtTokenProvider.java`

**Responsibilities**:
- Token generation
- Token validation
- Claims extraction
- Signature verification

**Common Changes**:
- Update token expiration time
- Add/modify custom claims
- Change signing algorithm
- Update secret key configuration

**Find Across Services**:
```bash
.standards/scripts/find-component.sh JwtTokenProvider
```

#### JWT Authentication Filter
**Location**: `{service}/src/main/java/com/sms/{service}/security/JwtAuthenticationFilter.java`

**Responsibilities**:
- Extract JWT from Authorization header
- Validate token via JwtTokenProvider
- Set Spring Security context
- Handle authentication failures

**Common Changes**:
- Update header extraction logic
- Modify authentication error responses
- Change filter ordering

**Find Across Services**:
```bash
.standards/scripts/find-component.sh JwtAuthenticationFilter
```

---

### Configuration Classes

#### CORS Configuration
**Location**: `{service}/src/main/java/com/sms/{service}/config/CorsConfig.java`

**Responsibilities**:
- Define allowed origins
- Configure allowed HTTP methods
- Set allowed headers
- Configure credentials policy

**Common Changes**:
- Add production origins
- Restrict HTTP methods
- Update max age settings

**Find Across Services**:
```bash
.standards/scripts/find-component.sh CorsConfig
```

#### OpenAPI Configuration
**Location**: `{service}/src/main/java/com/sms/{service}/config/OpenAPIConfig.java`

**Responsibilities**:
- Configure Swagger UI
- Define API metadata (title, description, version)
- Set server URL (API Gateway)
- Configure JWT authentication scheme

**Common Changes**:
- Update API version
- Add multiple server environments
- Modify security schemes

**Find Across Services**:
```bash
.standards/scripts/find-component.sh OpenAPIConfig
```

#### Security Configuration
**Location**: `{service}/src/main/java/com/sms/{service}/config/SecurityConfig.java`

**Responsibilities**:
- Configure HTTP security
- Define public/protected endpoints
- Integrate CORS
- Add JWT filter to filter chain

**Common Changes**:
- Add new public endpoints
- Change authentication requirements
- Update CSRF configuration

**Find Across Services**:
```bash
.standards/scripts/find-component.sh SecurityConfig
```

#### Password Encoder Configuration
**Location**: `{service}/src/main/java/com/sms/{service}/config/PasswordEncoderConfig.java`

**Responsibilities**:
- Provide BCrypt password encoder bean
- Configure encoding strength

**Common Changes**:
- Update BCrypt strength
- Switch to different encoder algorithm

**Find Across Services**:
```bash
.standards/scripts/find-component.sh PasswordEncoderConfig
```

**Note**: Only present in services that handle password authentication

---

### Data Layer

#### JPA Entities
**Location**: `{service}/src/main/java/com/sms/{service}/model/{Entity}.java`

**Naming Convention**: Singular noun (e.g., `Student`, `Teacher`, `Attendance`)

**Common Annotations**:
- `@Entity`
- `@Table(name = "...")`
- `@Id`
- `@GeneratedValue`
- `@Column`
- `@OneToMany`, `@ManyToOne`, etc.

**Common Changes**:
- Add new fields
- Modify validation annotations
- Update relationships

**Find Entity Across Services**:
```bash
find {service}/src/main/java -name "*{EntityName}*.java" -path "*/model/*"
```

#### JPA Repositories
**Location**: `{service}/src/main/java/com/sms/{service}/repository/{Entity}Repository.java`

**Naming Convention**: `{Entity}Repository` (e.g., `StudentRepository`, `TeacherRepository`)

**Extends**: `JpaRepository<Entity, IDType>`

**Common Changes**:
- Add custom query methods
- Define @Query annotations
- Add derived queries

**Find Repository Across Services**:
```bash
find {service}/src/main/java -name "*Repository.java" -path "*/repository/*"
```

---

### Business Logic Layer

#### Service Interfaces
**Location**: `{service}/src/main/java/com/sms/{service}/service/{Domain}Service.java`

**Naming Convention**: `{Domain}Service` (e.g., `StudentService`, `AuthService`)

**Common Changes**:
- Add new business methods
- Update method signatures
- Modify return types

#### Service Implementations
**Location**: `{service}/src/main/java/com/sms/{service}/service/{Domain}ServiceImpl.java`

**Naming Convention**: `{Domain}ServiceImpl` or integrated into service class directly

**Common Annotations**:
- `@Service`
- `@Transactional`

**Common Changes**:
- Implement business logic
- Add validation
- Modify transaction boundaries

**Find Service Across Services**:
```bash
.standards/scripts/find-component.sh "{Domain}Service"
```

---

### API Layer

#### REST Controllers
**Location**: `{service}/src/main/java/com/sms/{service}/controller/{Domain}Controller.java`

**Naming Convention**: `{Domain}Controller` (e.g., `StudentController`, `AuthController`)

**Common Annotations**:
- `@RestController`
- `@RequestMapping("/api/...")`
- `@GetMapping`, `@PostMapping`, etc.

**Common Changes**:
- Add new endpoints
- Update request/response DTOs
- Modify HTTP status codes
- Add validation annotations

**Find Controller Across Services**:
```bash
find {service}/src/main/java -name "*Controller.java" -path "*/controller/*"
```

---

### DTOs (Data Transfer Objects)

#### Request DTOs
**Location**: `{service}/src/main/java/com/sms/{service}/dto/{Purpose}Request.java`

**Naming Convention**: `{Purpose}Request` (e.g., `LoginRequest`, `StudentCreateRequest`)

**Common Annotations**:
- `@NotNull`, `@NotBlank`, `@Size`, etc. (validation)
- `@Schema` (OpenAPI documentation)

**Common Changes**:
- Add/remove fields
- Update validation rules
- Modify OpenAPI documentation

#### Response DTOs
**Location**: `{service}/src/main/java/com/sms/{service}/dto/{Purpose}Response.java`

**Naming Convention**: `{Purpose}Response` (e.g., `LoginResponse`, `StudentResponse`)

**Common Changes**:
- Add/remove fields
- Update field mappings
- Modify serialization

**Find DTOs Across Services**:
```bash
find {service}/src/main/java -name "*Request.java" -path "*/dto/*"
find {service}/src/main/java -name "*Response.java" -path "*/dto/*"
```

---

### Exception Handling

#### Custom Exceptions
**Location**: `{service}/src/main/java/com/sms/{service}/exception/{Type}Exception.java`

**Naming Convention**: `{Type}Exception` (e.g., `InvalidCredentialsException`, `StudentNotFoundException`)

**Common Patterns**:
- Extend `RuntimeException`
- Include error codes
- Provide meaningful messages

#### Global Exception Handler
**Location**: `{service}/src/main/java/com/sms/{service}/exception/GlobalExceptionHandler.java`

**Common Annotations**:
- `@RestControllerAdvice`
- `@ExceptionHandler`

**Responsibilities**:
- Map exceptions to HTTP responses
- Provide consistent error format
- Log exceptions

**Common Changes**:
- Add new exception handlers
- Update error response format
- Modify logging behavior

**Find Exception Handler Across Services**:
```bash
.standards/scripts/find-component.sh GlobalExceptionHandler
```

---

### Validation

#### Custom Validators
**Location**: `{service}/src/main/java/com/sms/{service}/validation/{Purpose}Validator.java`

**Naming Convention**: `{Purpose}Validator` (e.g., `PhoneNumberValidator`, `EmailValidator`)

**Common Annotations**:
- `@Component`
- Implements `ConstraintValidator<AnnotationType, ValueType>`

**Common Changes**:
- Add validation logic
- Update validation criteria
- Modify error messages

---

### Configuration Files

#### Local Development Profile
**Location**: `{service}/src/main/resources/application.yml`

**Purpose**: Configuration for local development

**Common Sections**:
- `server.port`
- `spring.application.name`
- `spring.datasource.*`
- `eureka.instance.*`
- `eureka.client.*`
- `jwt.secret`

**Common Changes**:
- Update port numbers
- Modify database connections
- Change Eureka settings

#### Docker Profile
**Location**: `{service}/src/main/resources/application-docker.yml`

**Purpose**: Configuration for Docker deployments

**Common Sections**:
- Environment variable references
- `eureka.instance.hostname`
- `eureka.instance.prefer-ip-address: false`

**Common Changes**:
- Update environment variable names
- Modify Eureka hostname

---

## Cross-Service Change Patterns

### Pattern 1: Update JWT Expiration Across All Services

**Files to Modify**:
- `{service}/src/main/java/com/sms/{service}/security/JwtTokenProvider.java`

**Location in File**: Line ~20 (EXPIRATION_MS constant)

**Commands**:
```bash
# Find all JWT providers
.standards/scripts/find-component.sh JwtTokenProvider

# Update expiration time (example: 24h to 48h)
find . -name "JwtTokenProvider.java" -path "*/security/*" \
  -exec sed -i '' 's/86400000/172800000/g' {} +
```

---

### Pattern 2: Add New Public Endpoint Across All Services

**Files to Modify**:
- `{service}/src/main/java/com/sms/{service}/config/SecurityConfig.java`

**Location in File**: `.authorizeHttpRequests()` section

**Steps**:
1. Locate SecurityConfig in target service
2. Find `.requestMatchers(...).permitAll()` block
3. Add new matcher before `.anyRequest().authenticated()`

---

### Pattern 3: Update CORS Origins for Production

**Files to Modify**:
- `{service}/src/main/java/com/sms/{service}/config/CorsConfig.java`

**Location in File**: Line ~30 (setAllowedOrigins)

**Steps**:
1. Find all CORS configs
2. Replace `List.of("*")` with specific origins
3. Set `setAllowCredentials(true)` if needed

---

## Package Prohibitions

### ❌ DO NOT Use These Locations

| Prohibited | Use Instead | Rationale |
|------------|-------------|-----------|
| `entity/` | `model/` | Consistency with Spring conventions |
| `service/impl/` | `service/` | Avoid unnecessary nesting |
| `config/` for JWT classes | `security/` | Separate security concerns |
| `util/` or `helper/` | Domain-specific packages | Avoid catch-all packages |

---

## Finding Components Quickly

### Using find-component.sh Script

```bash
# Find a component by class name
.standards/scripts/find-component.sh JwtTokenProvider

# Find all CORS configurations
.standards/scripts/find-component.sh CorsConfig

# Find all controllers
find . -name "*Controller.java" -path "*/controller/*"

# Find all services
find . -name "*Service.java" -path "*/service/*"
```

### Using grep for Content

```bash
# Find all files using a specific annotation
grep -r "@RestController" --include="*.java"

# Find all Bearer token extractions
grep -r "Bearer " --include="*.java"

# Find all Eureka configurations
grep -r "prefer-ip-address" --include="*.yml"
```

---

## Service Directory Structure

```
{service}-service/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/sms/{service}/
│       │       ├── {Service}Application.java
│       │       ├── config/
│       │       │   ├── CorsConfig.java
│       │       │   ├── OpenAPIConfig.java
│       │       │   ├── SecurityConfig.java
│       │       │   └── PasswordEncoderConfig.java (optional)
│       │       ├── controller/
│       │       │   └── {Domain}Controller.java
│       │       ├── dto/
│       │       │   ├── {Purpose}Request.java
│       │       │   └── {Purpose}Response.java
│       │       ├── exception/
│       │       │   ├── {Type}Exception.java
│       │       │   └── GlobalExceptionHandler.java
│       │       ├── model/
│       │       │   └── {Entity}.java
│       │       ├── repository/
│       │       │   └── {Entity}Repository.java
│       │       ├── security/
│       │       │   ├── JwtAuthenticationFilter.java
│       │       │   └── JwtTokenProvider.java
│       │       ├── service/
│       │       │   ├── {Domain}Service.java
│       │       │   └── {Domain}ServiceImpl.java
│       │       └── validation/ (optional)
│       │           └── {Purpose}Validator.java
│       └── resources/
│           ├── application.yml
│           └── application-docker.yml
└── pom.xml
```

---

## Related Documentation

- **Refactoring Checklist**: See `.standards/docs/refactoring-checklist.md`
- **Cross-Service Changes**: See `.standards/docs/cross-service-changes.md`
- **Reusable Components**: See `.standards/docs/reusable-components.md`
- **Service Template**: See `.standards/templates/service-template.md`

---

## Validation

To verify package locations in a service:

```bash
.standards/scripts/validate-service-structure.sh {service-name}
```

This will check:
- Package naming conventions
- Required classes presence
- File locations against standards
