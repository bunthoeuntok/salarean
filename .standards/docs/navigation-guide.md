# Service Navigation Guide

**Version**: 1.0.0
**Last Updated**: 2025-11-22
**Target Audience**: New developers, code reviewers, maintainers

---

## Overview

This guide helps you navigate any microservice in the SMS project. By following standardized patterns, you should be able to:

- **Locate any component in under 5 minutes** across any service
- **Navigate unfamiliar services with confidence** using consistent structure
- **Find equivalent functionality** across different services instantly

**Success Metric**: Navigate auth-service, student-service, and any future service to complete a simple task (adding an endpoint) within 30 minutes total.

---

## Table of Contents

1. [Visual Service Map](#visual-service-map)
2. [Common Tasks](#common-tasks)
3. [Component Location Quick Reference](#component-location-quick-reference)
4. [Navigation Patterns](#navigation-patterns)
5. [Real Examples](#real-examples)

---

## Visual Service Map

### Standard Microservice Structure

```
{service-name}/
│
├── src/main/java/com/sms/{service}/
│   │
│   ├── config/                      🔧 Configuration
│   │   ├── CorsConfig.java              ↳ Cross-origin request settings
│   │   ├── OpenAPIConfig.java           ↳ Swagger/API documentation
│   │   ├── SecurityConfig.java          ↳ Spring Security filter chain
│   │   ├── PasswordEncoderConfig.java   ↳ Password hashing (optional)
│   │   └── RedisConfig.java             ↳ Redis connection (optional)
│   │
│   ├── controller/                  🌐 API Endpoints
│   │   ├── {Domain}Controller.java      ↳ REST endpoints for domain
│   │   └── ...                          ↳ One controller per domain
│   │
│   ├── dto/                         📦 Data Transfer Objects
│   │   ├── request/
│   │   │   ├── {Action}Request.java     ↳ Incoming request payloads
│   │   │   └── ...
│   │   └── response/
│   │       ├── {Domain}Response.java    ↳ Outgoing response payloads
│   │       └── ...
│   │
│   ├── exception/                   ⚠️ Error Handling
│   │   ├── GlobalExceptionHandler.java  ↳ Handles all exceptions
│   │   ├── {Reason}Exception.java       ↳ Custom exception types
│   │   └── ...
│   │
│   ├── model/                       💾 Database Entities
│   │   ├── {Entity}.java                ↳ JPA entities (one per table)
│   │   └── ...
│   │
│   ├── repository/                  🗄️ Database Access
│   │   ├── {Entity}Repository.java      ↳ Data access for each entity
│   │   └── ...
│   │
│   ├── security/                    🔒 Authentication & Authorization
│   │   ├── JwtAuthenticationFilter.java ↳ JWT request filter
│   │   ├── JwtTokenProvider.java        ↳ JWT token operations
│   │   └── ...                          ↳ Custom auth logic
│   │
│   ├── service/                     💼 Business Logic
│   │   ├── {Domain}Service.java         ↳ Service interface
│   │   ├── {Domain}ServiceImpl.java     ↳ Service implementation
│   │   └── ...
│   │
│   └── validation/                  ✅ Custom Validators (optional)
│       ├── Valid{Purpose}.java          ↳ Custom validation annotations
│       └── {Purpose}Validator.java      ↳ Validator implementations
│
├── src/main/resources/
│   ├── application.yml              ⚙️ Default profile (local dev)
│   └── application-docker.yml       🐳 Docker profile (containerized)
│
├── Dockerfile                       🐳 Container image definition
├── docker-compose.yml               🐳 Service composition
└── pom.xml                          📦 Maven dependencies

```

---

## Common Tasks

### Task 1: "I need to add a new API endpoint"

**Navigation Path**:

```
1. controller/
   ↓
2. Find or create {Domain}Controller.java
   ↓
3. Add @GetMapping/@PostMapping method
   ↓
4. Create DTOs (if needed)
   ├─ dto/request/{Action}Request.java
   └─ dto/response/{Domain}Response.java
   ↓
5. Implement business logic in service/
   ├─ {Domain}Service.java (interface)
   └─ {Domain}ServiceImpl.java (implementation)
```

**Example**: Add "Get Student by ID" endpoint
```
student-service/
├── controller/StudentController.java      # Add @GetMapping("/{id}")
├── dto/response/StudentResponse.java      # Define response structure
└── service/StudentServiceImpl.java        # Implement getStudentById(Long id)
```

**Time Estimate**: 5 minutes to locate, 30-60 minutes to implement

---

### Task 2: "I need to find where user authentication happens"

**Navigation Path**:

```
1. security/
   ↓
2. Look for JwtAuthenticationFilter.java
   ↓
3. Trace to JwtTokenProvider.java
   ↓
4. Check SecurityConfig in config/SecurityConfig.java
   ↓
5. Find login endpoint in controller/
```

**Files Involved**:
```
auth-service/
├── security/
│   ├── JwtAuthenticationFilter.java    # JWT extraction and validation
│   └── JwtTokenProvider.java           # Token generation and parsing
├── config/SecurityConfig.java          # Security filter chain
└── controller/AuthController.java      # /api/auth/login endpoint
```

**Time Estimate**: 3 minutes to locate all components

---

### Task 3: "I need to understand how this service connects to the database"

**Navigation Path**:

```
1. src/main/resources/application.yml
   ↓ (check datasource configuration)
2. model/
   ↓ (find JPA entities)
3. repository/
   ↓ (find data access interfaces)
4. service/
   ↓ (find where repositories are used)
```

**Example**: Student database access
```
student-service/
├── src/main/resources/application.yml    # Database URL, credentials
├── model/Student.java                    # @Entity for students table
├── repository/StudentRepository.java     # Data access methods
└── service/StudentServiceImpl.java       # Uses repository for CRUD
```

**Time Estimate**: 5 minutes to trace from config to usage

---

### Task 4: "I need to add a new configuration setting"

**Navigation Path**:

```
1. Is it a framework configuration (CORS, Security, OpenAPI)?
   YES → config/{Purpose}Config.java
   NO  → Continue
   ↓
2. Is it a database/Redis/external service config?
   YES → src/main/resources/application.yml
   NO  → Continue
   ↓
3. Is it a custom application setting?
   YES → Add to application.yml under 'app:' namespace
        Create @ConfigurationProperties class in config/
```

**Examples**:
```
✅ CORS settings         → config/CorsConfig.java
✅ Database credentials  → application.yml (spring.datasource)
✅ JWT secret           → application.yml (jwt.secret)
✅ Custom app settings  → application.yml (app.*)
```

**Time Estimate**: 2 minutes to locate correct file

---

### Task 5: "I need to add validation to a DTO"

**Navigation Path**:

```
1. dto/request/{Action}Request.java
   ↓
2. Add annotations (@NotNull, @Email, @Size, etc.)
   ↓
3. For custom validation:
   ├─ validation/Valid{Purpose}.java (annotation)
   └─ validation/{Purpose}Validator.java (logic)
```

**Example**: Validate phone number format
```
student-service/
├── dto/request/CreateStudentRequest.java   # Add @ValidPhone annotation
└── validation/
    ├── ValidPhone.java                     # Custom annotation
    └── PhoneValidator.java                 # Validation logic
```

**Time Estimate**: 1 minute to locate DTO, 10-30 minutes to add custom validator

---

### Task 6: "I need to handle a new type of error"

**Navigation Path**:

```
1. exception/
   ↓
2. Create {Reason}Exception.java
   ↓
3. Add handler in GlobalExceptionHandler.java
```

**Example**: Handle user not found error
```
auth-service/
├── exception/
│   ├── UserNotFoundException.java        # extends RuntimeException
│   └── GlobalExceptionHandler.java       # @ExceptionHandler method
```

**Time Estimate**: 2 minutes to locate, 10-15 minutes to implement

---

### Task 7: "I need to find where Swagger/API docs are configured"

**Navigation Path**:

```
1. config/OpenAPIConfig.java
   ↓
2. Check server URL (should be API Gateway: http://localhost:8080)
3. Check security scheme (Bearer JWT)
4. Verify title and description
```

**Critical Check**: Server URL MUST be API Gateway, not service-specific port

**Time Estimate**: 1 minute to locate

---

### Task 8: "I need to understand how services are deployed"

**Navigation Path**:

```
1. Dockerfile
   ↓ (understand container image)
2. docker-compose.yml
   ↓ (see service definition, environment variables)
3. src/main/resources/application-docker.yml
   ↓ (check Docker-specific configuration)
```

**Files Involved**:
```
{service}/
├── Dockerfile                          # Image build steps
├── docker-compose.yml                  # Service orchestration
└── src/main/resources/
    └── application-docker.yml          # Docker profile config
```

**Time Estimate**: 5 minutes to understand full deployment

---

## Component Location Quick Reference

| I need to find... | Navigate to... | File Pattern |
|-------------------|----------------|--------------|
| **API endpoint** | `controller/` | `{Domain}Controller.java` |
| **Request/response data** | `dto/request/` or `dto/response/` | `{Action}Request.java`, `{Domain}Response.java` |
| **Database table** | `model/` | `{Entity}.java` |
| **Database queries** | `repository/` | `{Entity}Repository.java` |
| **Business logic** | `service/` | `{Domain}ServiceImpl.java` |
| **Error handling** | `exception/` | `GlobalExceptionHandler.java` |
| **Authentication** | `security/` | `JwtAuthenticationFilter.java` |
| **JWT operations** | `security/` | `JwtTokenProvider.java` |
| **CORS settings** | `config/` | `CorsConfig.java` |
| **API docs config** | `config/` | `OpenAPIConfig.java` |
| **Security settings** | `config/` | `SecurityConfig.java` |
| **Database config** | `src/main/resources/` | `application.yml` |
| **Docker config** | `src/main/resources/` | `application-docker.yml` |
| **Custom validation** | `validation/` | `Valid{Purpose}.java`, `{Purpose}Validator.java` |

---

## Navigation Patterns

### Pattern 1: Top-Down Navigation (from HTTP to Database)

**Use when**: Understanding how a specific API endpoint works end-to-end

```
HTTP Request
    ↓
controller/ → {Domain}Controller.java       # Receives request
    ↓
dto/request/ → {Action}Request.java         # Validates input
    ↓
service/ → {Domain}ServiceImpl.java         # Processes business logic
    ↓
repository/ → {Entity}Repository.java       # Accesses database
    ↓
model/ → {Entity}.java                      # Maps to table
    ↓
Database
```

**Example**: Login flow in auth-service
```
POST /api/auth/login
    ↓
controller/AuthController.login()
    ↓
dto/request/LoginRequest (validate identifier, password)
    ↓
service/AuthServiceImpl.login()
    ↓
repository/UserRepository.findByEmailOrPhone()
    ↓
model/User
    ↓
PostgreSQL users table
```

---

### Pattern 2: Bottom-Up Navigation (from Database to HTTP)

**Use when**: Understanding how a database entity is exposed via API

```
Database
    ↓
model/ → {Entity}.java                      # JPA entity
    ↓
repository/ → {Entity}Repository.java       # Data access
    ↓
service/ → {Domain}ServiceImpl.java         # Business logic
    ↓
dto/response/ → {Domain}Response.java       # Output format
    ↓
controller/ → {Domain}Controller.java       # API endpoint
    ↓
HTTP Response
```

**Example**: Student data exposure in student-service
```
PostgreSQL students table
    ↓
model/Student.java
    ↓
repository/StudentRepository.java
    ↓
service/StudentServiceImpl.getStudentById()
    ↓
dto/response/StudentResponse.java
    ↓
controller/StudentController.getStudentById()
    ↓
GET /api/students/{id}
```

---

### Pattern 3: Cross-Cutting Concerns Navigation

**Use when**: Understanding aspects that affect all endpoints (security, CORS, error handling)

```
Global Configuration
    ├─ config/SecurityConfig.java        # Applies to all endpoints
    ├─ config/CorsConfig.java            # Applies to all endpoints
    ├─ exception/GlobalExceptionHandler  # Catches all exceptions
    └─ security/JwtAuthenticationFilter  # Runs on every request
```

**Flow**:
```
1. Request arrives
   ↓
2. CorsFilter (from CorsConfig)
   ↓
3. JwtAuthenticationFilter (from security/)
   ↓
4. SecurityFilterChain (from SecurityConfig)
   ↓
5. Controller method
   ↓
6. Service method
   ↓ (if exception occurs)
7. GlobalExceptionHandler
```

---

## Real Examples

### Example 1: Finding Login Implementation

**Goal**: Understand how user login works

**Navigation**:
```
1. Start: "Where is the login endpoint?"
   → controller/AuthController.java
   → Find @PostMapping("/login")

2. "What data does it accept?"
   → dto/request/LoginRequest.java
   → Fields: identifier (email/phone), password

3. "What's the business logic?"
   → service/AuthServiceImpl.login()
   → Validates credentials, generates JWT

4. "How are users looked up?"
   → repository/UserRepository.findByEmailOrPhone()
   → Query by email or phone

5. "What's returned?"
   → dto/response/JwtResponse.java
   → Returns accessToken and refreshToken

6. "How is JWT generated?"
   → security/JwtTokenProvider.generateToken()
   → Creates signed JWT with user ID and claims
```

**Time**: 5-10 minutes to trace complete flow

---

### Example 2: Adding a New Student Endpoint

**Goal**: Add GET /api/students?grade={grade} endpoint

**Navigation**:
```
1. "Where do I add the endpoint?"
   → controller/StudentController.java
   → Add @GetMapping method with @RequestParam

2. "Do I need a new DTO?"
   → dto/response/StudentResponse.java already exists
   → Reuse existing response DTO

3. "Where's the business logic?"
   → service/StudentServiceImpl.java
   → Add findStudentsByGrade() method

4. "How do I query by grade?"
   → repository/StudentRepository.java
   → Add: List<Student> findByGradeLevel(int gradeLevel);

5. "What's the entity?"
   → model/Student.java
   → Has gradeLevel field (maps to grade_level column)
```

**Time**: 5 minutes to navigate, 30 minutes to implement

---

### Example 3: Fixing a CORS Error

**Goal**: Allow requests from new frontend origin

**Navigation**:
```
1. "Where are CORS settings?"
   → config/CorsConfig.java

2. "What's currently allowed?"
   → setAllowedOrigins(List.of("*"))
   → Currently allows all origins

3. "How do I add specific origin?"
   → Change to: List.of("http://localhost:3000", "https://sms.example.com")
   → Or use environment variable: ${CORS_ALLOWED_ORIGINS}

4. "Where's the environment variable configured?"
   → src/main/resources/application.yml
   → Add: app.cors.allowed-origins

5. "How do I update for Docker?"
   → docker-compose.yml
   → Add CORS_ALLOWED_ORIGINS environment variable
```

**Time**: 2 minutes to locate, 5 minutes to fix

---

## Navigation Tips

### Tip 1: Start with the Package

**If you know the component type**, go directly to its package:

- Need an endpoint? → `controller/`
- Need business logic? → `service/`
- Need data access? → `repository/`
- Need authentication? → `security/`
- Need configuration? → `config/`

### Tip 2: Use Naming Conventions

**File names follow predictable patterns**:

- Controllers: `{Domain}Controller.java`
- Services: `{Domain}ServiceImpl.java`
- Repositories: `{Entity}Repository.java`
- DTOs: `{Action}Request.java`, `{Domain}Response.java`
- Exceptions: `{Reason}Exception.java`

### Tip 3: Follow the HTTP Request Flow

**For API endpoints**, trace the request:
```
Controller → DTO → Service → Repository → Entity
```

### Tip 4: Check config/ for Framework Settings

**CORS, Security, OpenAPI** are always in `config/` package.

### Tip 5: Check security/ for Authentication

**JWT, filters, auth logic** are always in `security/` package.

### Tip 6: Use IDE Search

**IntelliJ IDEA / VS Code shortcuts**:
- `Ctrl+N` (Cmd+O on Mac): Search for class by name
- `Ctrl+Shift+N` (Cmd+Shift+O): Search for file by name
- `Ctrl+Shift+F` (Cmd+Shift+F): Search in files

**Search patterns**:
- Find controller: Search "StudentController"
- Find service: Search "StudentService"
- Find entity: Search "class Student"

---

## Validation Checklist

After navigating a service, verify you understand:

- [ ] Where API endpoints are defined (`controller/`)
- [ ] Where business logic lives (`service/`)
- [ ] Where database entities are (`model/`)
- [ ] Where data access happens (`repository/`)
- [ ] Where authentication is configured (`security/`)
- [ ] Where CORS is configured (`config/CorsConfig.java`)
- [ ] How the service is deployed (`docker-compose.yml`, `application-docker.yml`)

**Success**: You can locate any of these in under 5 minutes.

---

## Related Documentation

- **Package Structure**: `.standards/docs/package-structure.md` - Detailed explanation of each package
- **Naming Conventions**: `.standards/docs/naming-conventions.md` - Class and file naming rules
- **Configuration Patterns**: `.standards/docs/configuration-patterns.md` - Configuration class details
- **JWT Architecture**: `.standards/docs/jwt-architecture.md` - Authentication flow details

---

## Next Steps

1. **Practice**: Navigate `auth-service` using this guide
2. **Test Yourself**: Find the login endpoint, JWT token provider, and user repository in under 5 minutes
3. **Apply**: Try navigating `student-service` to find equivalent components
4. **Measure**: Track how long it takes to locate components (target: < 5 minutes)

---

## Version History

| Version | Date       | Changes                  |
|---------|------------|--------------------------|
| 1.0.0   | 2025-11-22 | Initial navigation guide |

---

## Support

For questions about navigation:

1. Review the component location quick reference table
2. Use the navigation patterns (top-down, bottom-up)
3. Consult package structure documentation
4. Check auth-service as reference implementation
