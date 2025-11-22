# Package Structure Standards

**Version**: 1.0.0
**Last Updated**: 2025-11-22
**Compliance**: MANDATORY for all microservices

---

## Overview

All microservices in the SMS project MUST follow a standardized package structure. This ensures:

- **Quick Navigation**: Developers can find code within 30 minutes in any service
- **Consistent Organization**: Business logic, configuration, and data access are clearly separated
- **Maintainability**: Standard locations for standard concerns

---

## Standard Package Layout

Every microservice MUST have this exact package structure:

```
com.sms.{service-name}/
├── config/          # Configuration classes ONLY (no business logic)
├── controller/      # REST API controllers
├── dto/             # Data Transfer Objects (Request/Response)
├── exception/       # Custom exceptions & global exception handlers
├── model/           # JPA Entities (database models)
├── repository/      # Spring Data JPA repositories
├── security/        # Security, authentication, JWT filters
├── service/         # Business logic (interfaces + implementations)
└── validation/      # Custom validators (optional)
```

**Critical Rules**:
- ✅ Package name MUST be `com.sms.{service-name}` (e.g., `com.sms.auth`, `com.sms.student`)
- ✅ All 8 core packages MUST exist (except `validation/` which is optional)
- ✅ Packages MUST be at the same level (flat structure, no nesting)

---

## Package Descriptions

### 1. `config/` - Configuration Classes

**Purpose**: Spring Boot `@Configuration` classes that configure frameworks and libraries.

**What Belongs Here**:
- CORS configuration (`CorsConfig.java`)
- OpenAPI/Swagger configuration (`OpenAPIConfig.java`)
- Spring Security configuration (`SecurityConfig.java`)
- Password encoder configuration (`PasswordEncoderConfig.java`)
- Redis configuration (`RedisConfig.java`)
- File upload configuration (`FileUploadConfig.java`)

**What Does NOT Belong Here**:
- ❌ Business logic or service classes
- ❌ JWT filters or token providers (these go in `security/`)
- ❌ Database entities (these go in `model/`)
- ❌ REST controllers

**Example from auth-service**:
```
config/
├── CorsConfig.java              # CORS settings for cross-origin requests
├── OpenAPIConfig.java           # Swagger UI configuration
├── PasswordEncoderConfig.java   # BCrypt password encoder bean
├── RedisConfig.java             # Redis connection settings
└── SecurityConfig.java          # Spring Security filter chain
```

**Naming Convention**: `{Purpose}Config.java` (e.g., `CorsConfig`, NOT `CorsConfiguration`)

---

### 2. `controller/` - REST API Controllers

**Purpose**: Handle HTTP requests and delegate to service layer.

**What Belongs Here**:
- `@RestController` annotated classes
- Request mappings (`@GetMapping`, `@PostMapping`, etc.)
- Request validation (`@Valid`)
- HTTP response construction

**What Does NOT Belong Here**:
- ❌ Business logic (delegate to `service/`)
- ❌ Direct database access (use `service/` which uses `repository/`)
- ❌ Exception handling logic (use `exception/` for global handlers)

**Example from auth-service**:
```
controller/
└── AuthController.java          # Handles /api/auth/register, /api/auth/login, etc.
```

**Naming Convention**: `{Domain}Controller.java` (e.g., `AuthController`, `StudentController`)

**Responsibilities**:
- Validate request DTOs
- Call service methods
- Return HTTP responses (200, 201, 400, 401, etc.)

---

### 3. `dto/` - Data Transfer Objects

**Purpose**: Define request and response payloads for API endpoints.

**What Belongs Here**:
- Request DTOs (data received from clients)
- Response DTOs (data sent to clients)
- Validation annotations (`@NotNull`, `@Email`, `@Size`, etc.)

**What Does NOT Belong Here**:
- ❌ JPA entities (these go in `model/`)
- ❌ Business logic
- ❌ Database annotations (`@Entity`, `@Table`, etc.)

**Example from auth-service**:
```
dto/
├── request/
│   ├── LoginRequest.java        # { "identifier": "...", "password": "..." }
│   ├── RefreshTokenRequest.java # { "refreshToken": "..." }
│   └── RegisterRequest.java     # { "email": "...", "phone": "...", "password": "..." }
└── response/
    ├── AuthResponse.java        # { "errorCode": "...", "data": {...} }
    └── JwtResponse.java         # { "accessToken": "...", "refreshToken": "..." }
```

**Naming Convention**:
- Request DTOs: `{Action}Request.java` (e.g., `LoginRequest`, `RegisterRequest`)
- Response DTOs: `{Domain}Response.java` (e.g., `AuthResponse`, `JwtResponse`)

**Why DTOs Matter**:
- Decouples API contracts from database models
- Allows different validation rules for different endpoints
- Prevents exposing sensitive database fields (e.g., password hashes)

---

### 4. `exception/` - Custom Exceptions & Handlers

**Purpose**: Define custom exception types and global exception handling.

**What Belongs Here**:
- Custom exception classes extending `RuntimeException`
- `@ControllerAdvice` annotated global exception handlers
- Error response DTOs

**What Does NOT Belong Here**:
- ❌ Business logic
- ❌ Controller-specific exception handling (use global handlers)

**Example from auth-service**:
```
exception/
├── GlobalExceptionHandler.java  # @ControllerAdvice for all controllers
├── InvalidCredentialsException.java
├── RefreshTokenExpiredException.java
└── UserAlreadyExistsException.java
```

**Naming Convention**:
- Exception classes: `{Reason}Exception.java` (e.g., `UserNotFoundException`)
- Handler classes: `GlobalExceptionHandler.java` or `{Domain}ExceptionHandler.java`

**Pattern**:
```java
// Custom exception
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

// Global handler
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {
        // Return standardized error response
    }
}
```

---

### 5. `model/` - JPA Entities

**Purpose**: Define database entities (tables).

**What Belongs Here**:
- `@Entity` annotated classes
- JPA relationships (`@OneToMany`, `@ManyToOne`, etc.)
- Database column mappings (`@Column`, `@Id`, `@GeneratedValue`)
- Lifecycle callbacks (`@PrePersist`, `@PreUpdate`)

**What Does NOT Belong Here**:
- ❌ DTOs (these go in `dto/`)
- ❌ Business logic (use `service/`)
- ❌ Validation annotations for API requests (use DTOs)

**Example from auth-service**:
```
model/
├── RefreshToken.java            # refresh_tokens table
└── User.java                    # users table
```

**CRITICAL NAMING RULE**:
- ✅ Package MUST be named `model/` (NOT `entity/`, NOT `domain/`)
- ✅ This is checked by the validation script

**Naming Convention**: `{EntityName}.java` (singular, e.g., `User`, `Student`, `Teacher`)

**Pattern**:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    // ... fields, getters, setters
}
```

---

### 6. `repository/` - Spring Data JPA Repositories

**Purpose**: Database access layer using Spring Data JPA.

**What Belongs Here**:
- Interfaces extending `JpaRepository<Entity, ID>`
- Custom query methods using Spring Data method naming
- `@Query` annotated custom JPQL/SQL queries

**What Does NOT Belong Here**:
- ❌ Business logic (use `service/`)
- ❌ Transaction management (use `@Transactional` in `service/`)
- ❌ Complex business queries (encapsulate in `service/` methods)

**Example from auth-service**:
```
repository/
├── RefreshTokenRepository.java  # Data access for RefreshToken entity
└── UserRepository.java          # Data access for User entity
```

**Naming Convention**: `{Entity}Repository.java` (e.g., `UserRepository`, `StudentRepository`)

**Pattern**:
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
}
```

---

### 7. `security/` - Security & Authentication

**Purpose**: Security-related classes including JWT authentication.

**What Belongs Here**:
- JWT authentication filter (`JwtAuthenticationFilter.java`)
- JWT token provider (`JwtTokenProvider.java`)
- Custom authentication/authorization logic
- User details services

**What Does NOT Belong Here**:
- ❌ Spring Security configuration (goes in `config/SecurityConfig.java`)
- ❌ Password encoding configuration (goes in `config/PasswordEncoderConfig.java`)

**Example from auth-service**:
```
security/
├── JwtAuthenticationFilter.java # OncePerRequestFilter for JWT extraction
└── JwtTokenProvider.java        # Token generation, parsing, validation
```

**CRITICAL RULE**:
- ✅ JWT classes MUST be in `security/` package (NOT in `config/`)
- ✅ This is checked by the validation script

**Naming Convention**:
- Filter classes: `Jwt{Purpose}Filter.java` (e.g., `JwtAuthenticationFilter`)
- Provider classes: `Jwt{Purpose}Provider.java` (e.g., `JwtTokenProvider`)

**Architecture Pattern**:
- **JwtAuthenticationFilter**: Handles HTTP requests, extracts JWT from headers
- **JwtTokenProvider**: Pure token operations (generate, parse, validate)
- See `.standards/docs/jwt-architecture.md` for complete details

---

### 8. `service/` - Business Logic

**Purpose**: Implement business logic and orchestrate repository calls.

**What Belongs Here**:
- Service interfaces defining business operations
- Service implementations with `@Service` annotation
- `@Transactional` methods
- Business validation logic
- Complex queries and data transformations

**What Does NOT Belong Here**:
- ❌ HTTP request handling (use `controller/`)
- ❌ Direct database access (use `repository/`)
- ❌ Configuration (use `config/`)

**Example from auth-service**:
```
service/
├── AuthService.java             # Interface defining business operations
├── AuthServiceImpl.java         # Implementation with business logic
├── RefreshTokenService.java     # Interface for token refresh operations
└── RefreshTokenServiceImpl.java # Implementation
```

**CRITICAL RULE**:
- ✅ Service implementations MUST be in `service/` package (NOT `service/impl/`)
- ✅ Flat structure: interfaces and implementations together
- ✅ This is checked by the validation script

**Naming Convention**:
- Interfaces: `{Domain}Service.java` (e.g., `AuthService`, `StudentService`)
- Implementations: `{Domain}ServiceImpl.java` (e.g., `AuthServiceImpl`, `StudentServiceImpl`)

**Pattern**:
```java
// Interface
public interface AuthService {
    JwtResponse register(RegisterRequest request);
    JwtResponse login(LoginRequest request);
}

// Implementation
@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public JwtResponse register(RegisterRequest request) {
        // Business logic here
    }
}
```

---

### 9. `validation/` - Custom Validators (Optional)

**Purpose**: Custom validation logic using Bean Validation API.

**What Belongs Here**:
- Custom constraint annotations (e.g., `@ValidPhone`)
- Validator implementations
- Cross-field validation logic

**What Does NOT Belong Here**:
- ❌ Built-in validators (use standard annotations like `@Email`, `@NotNull`)
- ❌ Business validation (use `service/`)

**Example** (if used):
```
validation/
├── ValidPhone.java              # Custom annotation for Cambodia phone format
└── PhoneValidator.java          # Validator implementation
```

**When to Use**:
- ✅ Complex validation rules (e.g., phone number format)
- ✅ Cross-field validation (e.g., password confirmation)
- ✅ Reusable validation logic across multiple DTOs

**When NOT to Use**:
- ❌ Simple validations (`@NotNull`, `@Email`, `@Size` are sufficient)
- ❌ Business rules (these belong in `service/`)

**Naming Convention**:
- Annotations: `Valid{Purpose}.java` (e.g., `ValidPhone`, `ValidPassword`)
- Validators: `{Purpose}Validator.java` (e.g., `PhoneValidator`, `PasswordValidator`)

---

## Package Structure Violations

### Common Mistakes

**1. Using `entity/` instead of `model/`**
```
❌ com.sms.student/entity/Student.java
✅ com.sms.student/model/Student.java
```

**2. Placing JWT classes in `config/`**
```
❌ com.sms.auth/config/JwtAuthenticationFilter.java
✅ com.sms.auth/security/JwtAuthenticationFilter.java
```

**3. Creating `service/impl/` subdirectory**
```
❌ com.sms.auth/service/impl/AuthServiceImpl.java
✅ com.sms.auth/service/AuthServiceImpl.java
```

**4. Mixing configuration with business logic**
```
❌ com.sms.auth/config/AuthConfig.java (contains business logic)
✅ com.sms.auth/config/SecurityConfig.java (only configuration)
✅ com.sms.auth/service/AuthServiceImpl.java (business logic)
```

---

## Validation

### Automated Validation

Run the validation script to check package structure:

```bash
.standards/scripts/validate-service-structure.sh <service-name>
```

**Checks Performed**:
- ✅ `model/` package exists (not `entity/`)
- ✅ JWT classes in `security/` package (not `config/`)
- ✅ Service implementations in `service/` (not `service/impl/`)
- ✅ All required configuration classes present

**Exit Codes**:
- `0` = All checks passed (100% compliant)
- `1` = One or more checks failed
- `2` = Invalid input (service directory not found)

### Manual Validation

Use the compliance checklist:

```bash
cat .standards/checklists/service-compliance.md
```

**Package Structure Checks** (from checklist):
- [ ] Package structure follows standard layout (8 core packages)
- [ ] Entity package named `model/` (NOT `entity/`)
- [ ] JWT classes in `security/` (NOT `config/`)
- [ ] Service implementations in `service/` (NOT `service/impl/`)

---

## Migration Guide

If an existing service violates package structure standards:

### Step 1: Identify Violations

```bash
.standards/scripts/validate-service-structure.sh <service-name>
```

### Step 2: Rename Packages

**Example: Rename `entity/` to `model/`**

```bash
# Navigate to service directory
cd <service-name>

# Rename directory
mv src/main/java/com/sms/<service>/entity src/main/java/com/sms/<service>/model

# Update package declarations
find src -type f -name "*.java" -exec sed -i '' 's/com\.sms\.<service>\.entity/com.sms.<service>.model/g' {} +

# Update imports
find src -type f -name "*.java" -exec sed -i '' 's/import com\.sms\.<service>\.entity/import com.sms.<service>.model/g' {} +
```

### Step 3: Move Misplaced Classes

**Example: Move JWT filter from `config/` to `security/`**

```bash
# Create security package if it doesn't exist
mkdir -p src/main/java/com/sms/<service>/security

# Move classes
mv src/main/java/com/sms/<service>/config/JwtAuthenticationFilter.java \
   src/main/java/com/sms/<service>/security/

# Update package declarations
sed -i '' 's/package com\.sms\.<service>\.config/package com.sms.<service>.security/' \
   src/main/java/com/sms/<service>/security/JwtAuthenticationFilter.java

# Update imports across codebase
find src -type f -name "*.java" -exec sed -i '' \
   's/import com\.sms\.<service>\.config\.JwtAuthenticationFilter/import com.sms.<service>.security.JwtAuthenticationFilter/g' {} +
```

### Step 4: Flatten Service Package

**Example: Move from `service/impl/` to `service/`**

```bash
# Move all implementation classes to service/
mv src/main/java/com/sms/<service>/service/impl/*.java \
   src/main/java/com/sms/<service>/service/

# Remove impl directory
rmdir src/main/java/com/sms/<service>/service/impl

# Update package declarations
find src/main/java/com/sms/<service>/service -type f -name "*Impl.java" \
   -exec sed -i '' 's/package com\.sms\.<service>\.service\.impl/package com.sms.<service>.service/g' {} +

# Update imports
find src -type f -name "*.java" -exec sed -i '' \
   's/import com\.sms\.<service>\.service\.impl/import com.sms.<service>.service/g' {} +
```

### Step 5: Verify

```bash
# Rebuild the service
./mvnw clean install

# Run validation script
.standards/scripts/validate-service-structure.sh <service-name>

# Expected: All checks should pass
```

---

## Quick Reference

### Package Checklist

When creating a new service, ensure these packages exist:

- [ ] `config/` - Configuration classes (CorsConfig, OpenAPIConfig, SecurityConfig)
- [ ] `controller/` - REST API controllers
- [ ] `dto/` - Request and response DTOs
- [ ] `exception/` - Custom exceptions and global handlers
- [ ] `model/` - JPA entities (NOT `entity/`)
- [ ] `repository/` - Spring Data JPA repositories
- [ ] `security/` - JWT filters and providers (NOT in `config/`)
- [ ] `service/` - Business logic (flat structure, NOT `service/impl/`)
- [ ] `validation/` - Custom validators (optional)

### File Count Guidelines

**Small Service** (e.g., auth-service):
- config/: 3-5 files
- controller/: 1-3 files
- dto/: 4-8 files
- exception/: 2-5 files
- model/: 1-3 files
- repository/: 1-3 files
- security/: 2 files
- service/: 2-6 files

**Large Service** (e.g., student-service):
- config/: 3-5 files
- controller/: 3-8 files
- dto/: 10-20 files
- exception/: 3-8 files
- model/: 3-10 files
- repository/: 3-10 files
- security/: 2 files
- service/: 6-20 files

---

## Related Documentation

- **Naming Conventions**: `.standards/docs/naming-conventions.md`
- **Configuration Patterns**: `.standards/docs/configuration-patterns.md`
- **JWT Architecture**: `.standards/docs/jwt-architecture.md`
- **Service Template**: `.standards/templates/service-template.md`
- **Compliance Checklist**: `.standards/checklists/service-compliance.md`

---

## Version History

| Version | Date       | Changes                          |
|---------|------------|----------------------------------|
| 1.0.0   | 2025-11-22 | Initial version with 9 packages |

---

## Support

For questions about package structure:

1. Review the service template: `.standards/templates/service-template.md`
2. Check auth-service as the reference implementation
3. Run the validation script to identify issues
4. Consult this documentation for migration steps
