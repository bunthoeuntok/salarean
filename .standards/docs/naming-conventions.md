# Naming Conventions Standards

**Version**: 1.0.0
**Last Updated**: 2025-11-22
**Compliance**: MANDATORY for all microservices

---

## Overview

Consistent naming conventions enable developers to:

- **Find code quickly**: Predictable naming means you know where to look
- **Understand purpose at a glance**: Names convey intent and responsibility
- **Maintain codebase easily**: Standard names prevent confusion across services

---

## Table of Contents

1. [Package Naming](#package-naming)
2. [Class Naming](#class-naming)
3. [File Naming](#file-naming)
4. [Variable Naming](#variable-naming)
5. [Method Naming](#method-naming)
6. [Configuration Property Naming](#configuration-property-naming)
7. [Database Naming](#database-naming)
8. [API Endpoint Naming](#api-endpoint-naming)

---

## Package Naming

### Rule: lowercase, singular domain terms

**Format**: `com.sms.{service-name}.{package-type}`

**Examples**:
```
✅ com.sms.auth.config
✅ com.sms.auth.controller
✅ com.sms.auth.dto
✅ com.sms.auth.exception
✅ com.sms.auth.model
✅ com.sms.auth.repository
✅ com.sms.auth.security
✅ com.sms.auth.service
✅ com.sms.auth.validation
```

**Forbidden**:
```
❌ com.sms.auth.Config           (uppercase)
❌ com.sms.auth.entities          (plural, wrong name)
❌ com.sms.auth.dtos              (plural)
❌ com.sms.auth.services          (plural)
❌ com.sms.auth.service.impl      (nested subdirectory)
```

### Service Name in Package

**Rule**: Service name MUST match the service's domain in lowercase, singular form.

| Service Name    | Base Package          | Correct? |
|-----------------|----------------------|----------|
| auth-service    | com.sms.auth         | ✅       |
| student-service | com.sms.student      | ✅       |
| attendance-service | com.sms.attendance | ✅       |
| teacher-service | com.sms.teacher      | ✅       |
| ~~auth-service~~ | com.sms.authentication | ❌ (too verbose) |
| ~~auth-service~~ | com.sms.authservice  | ❌ (no hyphen in package) |

### Package Type Standards

**Required Package Types**:

| Package Type | Purpose                              | Mandatory? |
|--------------|--------------------------------------|------------|
| `config`     | Configuration classes                | ✅ Yes     |
| `controller` | REST API controllers                 | ✅ Yes     |
| `dto`        | Data Transfer Objects                | ✅ Yes     |
| `exception`  | Custom exceptions & handlers         | ✅ Yes     |
| `model`      | JPA Entities                         | ✅ Yes     |
| `repository` | Spring Data JPA repositories         | ✅ Yes     |
| `security`   | Security, JWT, authentication        | ✅ Yes     |
| `service`    | Business logic                       | ✅ Yes     |
| `validation` | Custom validators                    | ⚠️ Optional |

**Critical Rule**: Package type MUST be exactly as listed (e.g., `model`, NOT `entity`).

---

## Class Naming

### General Rules

1. **PascalCase** (UpperCamelCase): Every word capitalized
2. **Singular nouns** for entities and services
3. **Descriptive suffixes** indicating responsibility

### Configuration Classes

**Format**: `{Purpose}Config`

**Examples**:
```java
✅ CorsConfig.java
✅ OpenAPIConfig.java              // Note: "API" all caps
✅ SecurityConfig.java
✅ PasswordEncoderConfig.java
✅ RedisConfig.java
✅ FileUploadConfig.java
```

**Forbidden**:
```java
❌ CorsConfiguration.java          // Too verbose, use "Config"
❌ OpenApiConfig.java              // Should be "OpenAPI" (all caps)
❌ Cors.java                       // Missing "Config" suffix
❌ ConfigCors.java                 // Suffix, not prefix
```

**Critical Rule**: OpenAPI configuration MUST be named `OpenAPIConfig` (NOT `OpenApiConfig`).

### Controller Classes

**Format**: `{Domain}Controller`

**Examples**:
```java
✅ AuthController.java
✅ StudentController.java
✅ AttendanceController.java
✅ TeacherController.java
```

**Forbidden**:
```java
❌ AuthRestController.java         // "Rest" is redundant (all are REST)
❌ AuthResource.java               // Use "Controller" suffix
❌ AuthAPI.java                    // Use "Controller" suffix
❌ AuthEndpoint.java               // Use "Controller" suffix
```

**Naming by Responsibility**:

| Responsibility         | Class Name              | File Path |
|------------------------|-------------------------|-----------|
| Authentication         | AuthController.java     | controller/AuthController.java |
| Student management     | StudentController.java  | controller/StudentController.java |
| Attendance tracking    | AttendanceController.java | controller/AttendanceController.java |

### DTO Classes

**Format**: `{Action/Domain}{Request|Response}`

**Request DTOs**:
```java
✅ LoginRequest.java
✅ RegisterRequest.java
✅ RefreshTokenRequest.java
✅ CreateStudentRequest.java
✅ UpdateStudentRequest.java
```

**Response DTOs**:
```java
✅ AuthResponse.java
✅ JwtResponse.java
✅ StudentResponse.java
✅ ErrorResponse.java
```

**Forbidden**:
```java
❌ LoginDTO.java                   // Use "Request" or "Response" suffix
❌ LoginRequestDTO.java            // "DTO" suffix is redundant (package already indicates DTO)
❌ Login.java                      // Too vague, missing suffix
❌ AuthRequestData.java            // Use "Request" suffix, not "Data"
```

**Nested DTOs**:

When organizing DTOs into subdirectories:

```
dto/
├── request/
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── RefreshTokenRequest.java
└── response/
    ├── AuthResponse.java
    └── JwtResponse.java
```

### Exception Classes

**Format**: `{Reason}Exception`

**Examples**:
```java
✅ UserNotFoundException.java
✅ InvalidCredentialsException.java
✅ RefreshTokenExpiredException.java
✅ UserAlreadyExistsException.java
✅ UnauthorizedException.java
```

**Forbidden**:
```java
❌ UserNotFound.java               // Missing "Exception" suffix
❌ NotFoundException.java          // Too generic, specify domain (e.g., "UserNotFoundException")
❌ InvalidUserCredentials.java     // Missing "Exception" suffix
```

**Handler Classes**:
```java
✅ GlobalExceptionHandler.java
✅ AuthExceptionHandler.java       // If domain-specific
```

### Entity (Model) Classes

**Format**: Singular noun representing the entity

**Examples**:
```java
✅ User.java
✅ RefreshToken.java
✅ Student.java
✅ Teacher.java
✅ Attendance.java
✅ Grade.java
```

**Forbidden**:
```java
❌ Users.java                      // Plural, use singular
❌ UserEntity.java                 // "Entity" suffix is redundant (package already indicates entity)
❌ UserModel.java                  // "Model" suffix is redundant
❌ TblUser.java                    // Don't prefix with "Tbl"
```

**Composite Entities**:

For entities representing relationships or composite concepts:

```java
✅ RefreshToken.java               // Two-word entity name
✅ StudentCourse.java              // Junction table for many-to-many
✅ AttendanceRecord.java           // Descriptive composite name
```

### Repository Classes

**Format**: `{Entity}Repository`

**Examples**:
```java
✅ UserRepository.java
✅ RefreshTokenRepository.java
✅ StudentRepository.java
✅ TeacherRepository.java
```

**Forbidden**:
```java
❌ UserRepo.java                   // Use full word "Repository"
❌ UserDAO.java                    // Spring Data uses "Repository", not "DAO"
❌ UsersRepository.java            // Entity name should be singular
```

### Service Classes

**Format**:
- Interface: `{Domain}Service`
- Implementation: `{Domain}ServiceImpl`

**Examples**:
```java
✅ AuthService.java                // Interface
✅ AuthServiceImpl.java            // Implementation
✅ StudentService.java
✅ StudentServiceImpl.java
✅ RefreshTokenService.java
✅ RefreshTokenServiceImpl.java
```

**Forbidden**:
```java
❌ AuthServiceImplementation.java // Use "ServiceImpl" not full word
❌ AuthServiceImp.java             // Use "ServiceImpl" not "Imp"
❌ IAuthService.java               // Don't prefix interfaces with "I"
❌ AuthServiceBean.java            // Use "ServiceImpl" not "Bean"
```

**Critical Rule**: Service implementations MUST be in the same `service/` package, NOT in `service/impl/` subdirectory.

```
✅ service/AuthService.java
✅ service/AuthServiceImpl.java

❌ service/AuthService.java
❌ service/impl/AuthServiceImpl.java
```

### Security Classes

**Format**:
- Filters: `Jwt{Purpose}Filter`
- Providers: `Jwt{Purpose}Provider`
- User details: `{Domain}UserDetailsService`

**Examples**:
```java
✅ JwtAuthenticationFilter.java
✅ JwtTokenProvider.java
✅ CustomUserDetailsService.java
```

**Forbidden**:
```java
❌ JwtFilter.java                  // Too vague, specify purpose
❌ JwtUtils.java                   // Use specific names like "JwtTokenProvider"
❌ JwtHelper.java                  // Use "Provider" or "Filter"
❌ TokenProvider.java              // Prefix with "Jwt" for clarity
```

### Validation Classes

**Format**:
- Annotations: `Valid{Purpose}`
- Validators: `{Purpose}Validator`

**Examples**:
```java
✅ ValidPhone.java                 // Annotation
✅ PhoneValidator.java             // Validator implementation
✅ ValidPassword.java
✅ PasswordValidator.java
```

**Forbidden**:
```java
❌ PhoneValidation.java            // Use "Validator" suffix
❌ PhoneNumberValidator.java       // Keep concise, use "Phone" not "PhoneNumber"
❌ ValidatePhone.java              // Use "Valid" prefix for annotation
```

---

## File Naming

### Java Source Files

**Rule**: File name MUST exactly match the public class name.

**Examples**:
```
✅ AuthController.java             (contains public class AuthController)
✅ UserRepository.java             (contains public interface UserRepository)
✅ LoginRequest.java               (contains public class LoginRequest)
```

**Forbidden**:
```
❌ authController.java             (lowercase)
❌ auth-controller.java            (kebab-case)
❌ auth_controller.java            (snake_case)
```

### Configuration Files

**Rule**: Use kebab-case for application configuration files.

**Examples**:
```
✅ application.yml
✅ application-docker.yml
✅ application-test.yml            (if using test profile)
```

**Forbidden**:
```
❌ application-dev.yml             (use "default" profile, not "dev")
❌ application-prod.yml            (use "docker" profile, not "prod")
❌ application_docker.yml          (use hyphen, not underscore)
❌ applicationDocker.yml           (use kebab-case, not camelCase)
```

**Critical Rule**: Services MUST have exactly 2 profiles:
- `application.yml` (default profile for local development)
- `application-docker.yml` (docker profile for containerized deployment)

### Docker Files

**Examples**:
```
✅ Dockerfile
✅ docker-compose.yml
✅ .dockerignore
```

**Forbidden**:
```
❌ dockerfile                      (lowercase)
❌ Dockerfile.prod                 (don't create multiple Dockerfiles)
❌ docker_compose.yml              (use hyphen, not underscore)
```

### Maven Files

**Examples**:
```
✅ pom.xml
✅ .mvn/
✅ mvnw
✅ mvnw.cmd
```

---

## Variable Naming

### Instance Variables

**Rule**: camelCase, descriptive names

**Examples**:
```java
✅ private String email;
✅ private String phoneNumber;
✅ private LocalDateTime createdAt;
✅ private UserRepository userRepository;
✅ private PasswordEncoder passwordEncoder;
```

**Forbidden**:
```java
❌ private String Email;           (PascalCase, use camelCase)
❌ private String e;                (too short, not descriptive)
❌ private String email_address;   (snake_case, use camelCase)
❌ private String strEmail;         (Hungarian notation, avoid type prefixes)
```

### Constants

**Rule**: UPPER_SNAKE_CASE with `static final`

**Examples**:
```java
✅ public static final String JWT_SECRET = "...";
✅ public static final long JWT_EXPIRATION_MS = 86400000L;
✅ public static final int MAX_LOGIN_ATTEMPTS = 5;
```

**Forbidden**:
```java
❌ public static final String jwtSecret = "...";           (camelCase, use UPPER_SNAKE_CASE)
❌ public static final String Jwt_Secret = "...";          (mixed case)
❌ public static final String JWT-SECRET = "...";          (hyphen not allowed in Java)
```

### Method Parameters

**Rule**: camelCase, match DTO field names when applicable

**Examples**:
```java
✅ public User findByEmail(String email)
✅ public void updatePassword(String userId, String newPassword)
✅ public List<Student> findByGrade(int gradeLevel)
```

**Forbidden**:
```java
❌ public User findByEmail(String Email)          (PascalCase)
❌ public User findByEmail(String e)              (too short)
❌ public User findByEmail(String email_address)  (snake_case)
```

---

## Method Naming

### General Rules

1. **camelCase**: First word lowercase, subsequent words capitalized
2. **Verb-first**: Start with action verb
3. **Descriptive**: Clearly convey what the method does

### Repository Methods

**Spring Data JPA Query Methods**:

**Format**: `{action}By{Property}{Condition}`

**Examples**:
```java
✅ Optional<User> findByEmail(String email);
✅ Optional<User> findByPhone(String phone);
✅ List<User> findByRoleAndActiveTrue(String role);
✅ boolean existsByEmail(String email);
✅ void deleteByEmail(String email);
```

**Forbidden**:
```java
❌ Optional<User> getByEmail(String email);       // Use "find" not "get" for queries
❌ Optional<User> FindByEmail(String email);      // PascalCase, use camelCase
❌ Optional<User> findUserByEmail(String email);  // Don't repeat entity name
```

**Custom Query Methods**:

```java
✅ @Query("SELECT u FROM User u WHERE u.email = :email")
   Optional<User> findByEmailCustom(@Param("email") String email);
```

### Service Methods

**Format**: `{action}{Domain}{Details}`

**Examples**:
```java
✅ public JwtResponse register(RegisterRequest request);
✅ public JwtResponse login(LoginRequest request);
✅ public void deleteRefreshToken(String token);
✅ public Student createStudent(CreateStudentRequest request);
✅ public Student updateStudent(Long id, UpdateStudentRequest request);
✅ public void deleteStudent(Long id);
✅ public Student getStudentById(Long id);
✅ public List<Student> getAllStudents();
```

**Forbidden**:
```java
❌ public JwtResponse RegisterUser(RegisterRequest request);  // PascalCase
❌ public JwtResponse reg(RegisterRequest request);           // Abbreviations
❌ public JwtResponse userRegister(RegisterRequest request);  // Domain before action
```

### Controller Methods

**Format**: Match HTTP verb with method name

**Examples**:
```java
✅ @PostMapping("/register")
   public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request)

✅ @PostMapping("/login")
   public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request)

✅ @GetMapping("/{id}")
   public ResponseEntity<?> getStudentById(@PathVariable Long id)

✅ @PutMapping("/{id}")
   public ResponseEntity<?> updateStudent(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequest request)

✅ @DeleteMapping("/{id}")
   public ResponseEntity<?> deleteStudent(@PathVariable Long id)
```

**Naming by HTTP Method**:

| HTTP Method | Method Name Pattern | Example |
|-------------|---------------------|---------|
| GET         | get{Domain}         | getStudentById, getAllStudents |
| POST        | create{Domain} or {action} | createStudent, register, login |
| PUT         | update{Domain}      | updateStudent |
| DELETE      | delete{Domain}      | deleteStudent |

---

## Configuration Property Naming

### Application Properties (YAML)

**Rule**: kebab-case (lowercase with hyphens)

**Examples**:
```yaml
✅ spring:
     application:
       name: auth-service
     datasource:
       url: jdbc:postgresql://localhost:5432/sms_auth
       username: sms_user
       password: password
     jpa:
       hibernate:
         ddl-auto: update
       show-sql: true
```

**Forbidden**:
```yaml
❌ spring:
     applicationName: auth-service          (camelCase, use kebab-case)
     datasource_url: jdbc:...               (snake_case, use nested structure)
```

### Environment Variables

**Rule**: UPPER_SNAKE_CASE, use Spring Boot standard names

**Standard Names**:
```bash
✅ SPRING_DATASOURCE_URL
✅ SPRING_DATASOURCE_USERNAME
✅ SPRING_DATASOURCE_PASSWORD
✅ EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
✅ JWT_SECRET
✅ JWT_EXPIRATION_MS
✅ SPRING_REDIS_HOST
✅ SPRING_REDIS_PORT
```

**Forbidden**:
```bash
❌ DB_URL                          (use SPRING_DATASOURCE_URL)
❌ DB_USERNAME                     (use SPRING_DATASOURCE_USERNAME)
❌ DB_PASSWORD                     (use SPRING_DATASOURCE_PASSWORD)
❌ EUREKA_CLIENT_SERVICE_URL       (use EUREKA_CLIENT_SERVICEURL_DEFAULTZONE)
❌ db_url                          (lowercase, use UPPER_SNAKE_CASE)
❌ spring.datasource.url           (dots not allowed in env vars)
```

**Critical Rule**: All environment variables MUST follow Spring Boot standard naming conventions.

### Custom Properties

**Rule**: Group under `app.*` namespace

**Examples**:
```yaml
✅ app:
     jwt:
       secret: ${JWT_SECRET:defaultSecretForDevelopment}
       expiration-ms: ${JWT_EXPIRATION_MS:86400000}
     cors:
       allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
```

**Access in Java**:
```java
✅ @Value("${app.jwt.secret}")
   private String jwtSecret;

✅ @Value("${app.jwt.expiration-ms}")
   private long jwtExpirationMs;
```

---

## Database Naming

### Table Names

**Rule**: lowercase snake_case, plural nouns

**Examples**:
```sql
✅ users
✅ refresh_tokens
✅ students
✅ teachers
✅ attendance_records
```

**Forbidden**:
```sql
❌ Users                    (uppercase)
❌ user                     (singular, use plural)
❌ tbl_users                (don't prefix with "tbl")
❌ usersTable               (camelCase, use snake_case)
```

**Mapping in JPA**:
```java
@Entity
@Table(name = "users")          // ✅ Lowercase snake_case
public class User {             // ✅ Singular class name
    // ...
}
```

### Column Names

**Rule**: lowercase snake_case

**Examples**:
```sql
✅ id
✅ email
✅ phone_number
✅ created_at
✅ updated_at
✅ is_active
```

**Forbidden**:
```sql
❌ Email                    (PascalCase)
❌ phoneNumber              (camelCase)
❌ phone-number             (hyphen not standard for SQL)
```

**Mapping in JPA**:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;              // ✅ Maps to "email" column

    @Column(name = "phone_number")     // ✅ Explicit mapping for snake_case
    private String phoneNumber;        // ✅ camelCase in Java

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_active")
    private Boolean isActive;
}
```

### Foreign Key Names

**Rule**: `{referencing_table}_id`

**Examples**:
```sql
✅ user_id
✅ student_id
✅ teacher_id
✅ course_id
```

**Forbidden**:
```sql
❌ userId                   (camelCase)
❌ user                     (missing "_id" suffix)
❌ id_user                  (suffix, not prefix)
```

### Index Names

**Rule**: `idx_{table}_{columns}`

**Examples**:
```sql
✅ idx_users_email
✅ idx_users_phone
✅ idx_refresh_tokens_token
✅ idx_students_email_phone
```

**Constraint Names**:
```sql
✅ uk_users_email           (unique key)
✅ fk_refresh_tokens_user   (foreign key)
✅ pk_users                 (primary key)
```

---

## API Endpoint Naming

### General Rules

1. **kebab-case**: Lowercase with hyphens
2. **Plural nouns** for collections
3. **RESTful conventions**

### Endpoint Patterns

**Resource Collections**:
```
✅ GET    /api/students           (get all students)
✅ POST   /api/students           (create student)
✅ GET    /api/students/{id}      (get student by ID)
✅ PUT    /api/students/{id}      (update student)
✅ DELETE /api/students/{id}      (delete student)
```

**Forbidden**:
```
❌ GET    /api/student            (singular, use plural)
❌ GET    /api/getStudents        (verb in URL, use HTTP method)
❌ GET    /api/students/get       (redundant verb)
❌ POST   /api/createStudent      (verb in URL)
❌ GET    /api/students_list      (snake_case, use kebab-case)
```

**Non-CRUD Actions**:
```
✅ POST   /api/auth/register
✅ POST   /api/auth/login
✅ POST   /api/auth/logout
✅ POST   /api/auth/refresh-token
✅ POST   /api/students/{id}/activate
✅ POST   /api/students/{id}/deactivate
```

**Nested Resources**:
```
✅ GET    /api/teachers/{id}/students          (get students for a teacher)
✅ GET    /api/students/{id}/attendance        (get attendance for a student)
✅ POST   /api/courses/{id}/enroll             (enroll in course)
```

### Query Parameters

**Rule**: camelCase or snake_case (be consistent within the project)

**Examples** (using camelCase):
```
✅ GET /api/students?gradeLevel=10
✅ GET /api/students?isActive=true
✅ GET /api/students?sortBy=name&sortOrder=asc
✅ GET /api/students?page=1&pageSize=20
```

**Examples** (using snake_case):
```
✅ GET /api/students?grade_level=10
✅ GET /api/students?is_active=true
✅ GET /api/students?sort_by=name&sort_order=asc
✅ GET /api/students?page=1&page_size=20
```

**Forbidden**:
```
❌ GET /api/students?GradeLevel=10          (PascalCase)
❌ GET /api/students?GRADE_LEVEL=10         (UPPER_SNAKE_CASE)
❌ GET /api/students?grade-level=10         (kebab-case not standard for query params)
```

**Recommendation**: Use **camelCase** for query parameters to match Java conventions.

---

## Naming Violations

### Common Mistakes and Fixes

**1. Wrong Package Names**
```
❌ com.sms.auth.entity          → ✅ com.sms.auth.model
❌ com.sms.auth.service.impl    → ✅ com.sms.auth.service
❌ com.sms.auth.Config          → ✅ com.sms.auth.config
```

**2. Wrong Class Names**
```
❌ OpenApiConfig.java           → ✅ OpenAPIConfig.java
❌ UserEntity.java              → ✅ User.java
❌ AuthServiceImplementation    → ✅ AuthServiceImpl.java
```

**3. Wrong File Names**
```
❌ application-dev.yml          → ✅ application.yml (default profile)
❌ application-prod.yml         → ✅ application-docker.yml
```

**4. Wrong Environment Variables**
```
❌ DB_USERNAME                  → ✅ SPRING_DATASOURCE_USERNAME
❌ EUREKA_CLIENT_SERVICE_URL    → ✅ EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
```

---

## Validation

### Automated Checks

Run the validation script:

```bash
.standards/scripts/validate-service-structure.sh <service-name>
```

**Checks**:
- ✅ Package names (model/, security/, service/)
- ✅ Configuration class names (OpenAPIConfig, not OpenApiConfig)
- ✅ Service implementation locations (service/, not service/impl/)

### Manual Checks

Use the compliance checklist:

```bash
cat .standards/checklists/service-compliance.md
```

**Naming Convention Checks**:
- [ ] Package names follow lowercase snake_case
- [ ] Class names follow PascalCase with appropriate suffixes
- [ ] Configuration classes use "{Purpose}Config" pattern
- [ ] DTOs use "{Action}{Request|Response}" pattern
- [ ] Entities use singular nouns
- [ ] Repositories use "{Entity}Repository" pattern
- [ ] Services use "{Domain}Service" and "{Domain}ServiceImpl" pattern

---

## Quick Reference

### Naming Patterns Cheat Sheet

| Element | Pattern | Example |
|---------|---------|---------|
| **Packages** | lowercase | com.sms.auth.model |
| **Classes** | PascalCase + Suffix | UserRepository |
| **Config Classes** | {Purpose}Config | OpenAPIConfig |
| **Controllers** | {Domain}Controller | AuthController |
| **DTOs** | {Action}{Request\|Response} | LoginRequest |
| **Entities** | Singular noun | User, Student |
| **Repositories** | {Entity}Repository | UserRepository |
| **Services** | {Domain}Service{Impl} | AuthService, AuthServiceImpl |
| **Exceptions** | {Reason}Exception | UserNotFoundException |
| **Security** | Jwt{Purpose}{Filter\|Provider} | JwtTokenProvider |
| **Variables** | camelCase | emailAddress |
| **Constants** | UPPER_SNAKE_CASE | JWT_SECRET |
| **Methods** | camelCase verb-first | findByEmail |
| **Tables** | snake_case plural | users, refresh_tokens |
| **Columns** | snake_case | created_at |
| **API Endpoints** | kebab-case | /api/refresh-token |
| **Config Files** | kebab-case | application-docker.yml |
| **Env Variables** | UPPER_SNAKE_CASE | SPRING_DATASOURCE_URL |

---

## Related Documentation

- **Package Structure**: `.standards/docs/package-structure.md`
- **Configuration Patterns**: `.standards/docs/configuration-patterns.md`
- **JWT Architecture**: `.standards/docs/jwt-architecture.md`
- **Service Template**: `.standards/templates/service-template.md`

---

## Version History

| Version | Date       | Changes                      |
|---------|------------|------------------------------|
| 1.0.0   | 2025-11-22 | Initial naming conventions   |

---

## Support

For questions about naming conventions:

1. Check this document for the specific naming pattern
2. Review auth-service as the reference implementation
3. Run validation script to identify naming violations
4. Consult service template for complete examples
