# Microservice Template Documentation

**Version**: 1.0.0
**Date**: 2025-11-22
**Template Service**: auth-service
**Purpose**: Complete reference guide for creating standardized microservices

---

## Table of Contents

1. [Overview](#overview)
2. [Directory Structure](#directory-structure)
3. [Package Structure](#package-structure)
4. [Configuration Files](#configuration-files)
5. [Required Configuration Classes](#required-configuration-classes)
6. [JWT Architecture](#jwt-architecture)
7. [Docker Compose Configuration](#docker-compose-configuration)
8. [Service Creation Workflow](#service-creation-workflow)
9. [Quick Start Guide](#quick-start-guide)
10. [Troubleshooting](#troubleshooting)

---

## Overview

This document provides the complete template for creating microservices in the Salarean SMS project. All microservices MUST follow this template to ensure architectural consistency, maintainability, and ease of development.

**Canonical Example**: `/Volumes/DATA/my-projects/salarean/auth-service`

### Key Principles

1. **Template-Based Development**: Use auth-service as the authoritative template
2. **Convention Over Configuration**: Follow Spring Boot conventions
3. **Separation of Concerns**: Clear package boundaries
4. **Docker-First**: Designed for containerized deployment
5. **Eureka-Ready**: Service discovery integration
6. **API Gateway Integration**: All services accessed through gateway

---

## Directory Structure

### Complete Service Directory Tree

```
{service-name}/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── sms/
│   │   │           └── {service}/
│   │   │               ├── config/                    # Configuration classes
│   │   │               │   ├── CorsConfig.java
│   │   │               │   ├── OpenAPIConfig.java
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   └── [ServiceSpecific]Config.java
│   │   │               ├── controller/                # REST controllers
│   │   │               │   └── {Domain}Controller.java
│   │   │               ├── dto/                       # Data Transfer Objects
│   │   │               │   ├── request/
│   │   │               │   │   └── {Action}Request.java
│   │   │               │   └── response/
│   │   │               │       └── {Action}Response.java
│   │   │               ├── exception/                 # Custom exceptions
│   │   │               │   ├── {Domain}Exception.java
│   │   │               │   └── GlobalExceptionHandler.java
│   │   │               ├── model/                     # JPA Entities
│   │   │               │   └── {Entity}.java
│   │   │               ├── repository/                # JPA Repositories
│   │   │               │   └── {Entity}Repository.java
│   │   │               ├── security/                  # Security & JWT
│   │   │               │   ├── JwtAuthenticationFilter.java
│   │   │               │   └── JwtTokenProvider.java
│   │   │               ├── service/                   # Business logic
│   │   │               │   ├── {Domain}Service.java
│   │   │               │   └── {Domain}ServiceImpl.java
│   │   │               ├── validation/                # Custom validators
│   │   │               │   └── {Custom}Validator.java
│   │   │               └── {Service}Application.java  # Main class
│   │   └── resources/
│   │       ├── application.yml                        # Default profile
│   │       ├── application-docker.yml                 # Docker profile
│   │       └── application-test.yml                   # Optional: Test profile
│   └── test/
│       └── java/
│           └── com/
│               └── sms/
│                   └── {service}/
│                       ├── controller/
│                       ├── service/
│                       └── {Service}ApplicationTests.java
├── Dockerfile                                          # Container build
├── pom.xml                                             # Maven dependencies
└── README.md                                           # Service documentation
```

### Key Directory Rules

**MUST HAVE**:
- ✅ `config/` - Configuration classes only
- ✅ `controller/` - REST endpoints
- ✅ `dto/` - Request/Response objects
- ✅ `exception/` - Error handling
- ✅ `model/` - JPA entities (NOT `entity/`)
- ✅ `repository/` - Data access layer
- ✅ `security/` - JWT and authentication (if applicable)
- ✅ `service/` - Business logic

**OPTIONAL**:
- ○ `validation/` - Custom validators (recommended)
- ○ `enums/` - Service-specific enums (if needed)
- ○ `util/` - Utility classes (if needed)

**FORBIDDEN**:
- ❌ `entity/` package (use `model/`)
- ❌ `service/impl/` subdirectory (keep implementations in `service/`)
- ❌ Business logic in `config/` package
- ❌ JWT classes in `config/` package

---

## Package Structure

### Standard Package Organization

```java
// Base package: com.sms.{service}

com.sms.{service}.config          // Configuration beans
com.sms.{service}.controller      // REST API endpoints
com.sms.{service}.dto             // Data Transfer Objects
  ├── request                     // Request DTOs
  └── response                    // Response DTOs
com.sms.{service}.exception       // Exception handling
com.sms.{service}.model           // JPA entities
com.sms.{service}.repository      // Data access
com.sms.{service}.security        // Authentication & JWT
com.sms.{service}.service         // Business logic
com.sms.{service}.validation      // Custom validators
```

### Package Responsibilities

#### config/
**Purpose**: Spring configuration classes ONLY (no business logic)

**Contents**:
- Bean definitions
- Framework configuration
- Integration settings

**Examples**:
- `CorsConfig.java` - Cross-Origin Resource Sharing
- `OpenAPIConfig.java` - Swagger/OpenAPI documentation
- `SecurityConfig.java` - Spring Security setup
- `RedisConfig.java` - Redis connection (if needed)
- `FileUploadConfig.java` - File upload settings (if needed)

**Rules**:
- ❌ NO business logic
- ❌ NO JWT validation logic (belongs in `security/`)
- ✅ Only `@Configuration`, `@Bean` methods

---

#### controller/
**Purpose**: REST API endpoints

**Naming Convention**: `{Domain}Controller.java`

**Example**:
```java
@RestController
@RequestMapping("/api/students")
public class StudentController {
    // REST endpoints
}
```

**Rules**:
- ✅ Handle HTTP requests/responses
- ✅ Validate input using `@Valid`
- ✅ Delegate to service layer
- ❌ NO business logic
- ❌ NO direct repository access

---

#### dto/
**Purpose**: Data Transfer Objects for API communication

**Structure**:
```
dto/
├── request/                      # Incoming data
│   ├── CreateStudentRequest.java
│   └── UpdateStudentRequest.java
└── response/                     # Outgoing data
    ├── StudentResponse.java
    └── ApiResponse.java
```

**Standard API Response Format**:
```java
public class ApiResponse<T> {
    private String errorCode;     // "SUCCESS" or error code
    private T data;               // Response payload or null
}
```

**Rules**:
- ✅ Use for API contracts
- ✅ Include validation annotations
- ❌ NO persistence annotations (@Entity)
- ❌ NO business logic

---

#### exception/
**Purpose**: Custom exceptions and global error handling

**Contents**:
- `{Domain}Exception.java` - Custom exceptions
- `GlobalExceptionHandler.java` - `@ControllerAdvice` for error handling

**Example**:
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(StudentNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse<>("STUDENT_NOT_FOUND", null));
    }
}
```

**Rules**:
- ✅ Return error codes (not human-readable messages)
- ✅ Use `ApiResponse` format
- ❌ NO i18n messages in backend (frontend responsibility)

---

#### model/
**Purpose**: JPA entities (database models)

**Naming**: Use `model/` NOT `entity/`

**Example**:
```java
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ... fields
}
```

**Rules**:
- ✅ Use JPA annotations
- ✅ Include validation annotations
- ✅ Use Lombok for boilerplate
- ❌ NO business logic

---

#### repository/
**Purpose**: Data access layer

**Example**:
```java
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
}
```

**Rules**:
- ✅ Extend `JpaRepository`
- ✅ Define custom query methods
- ❌ NO business logic

---

#### security/
**Purpose**: Authentication, authorization, and JWT handling

**Required Classes** (for services with JWT):
1. `JwtAuthenticationFilter.java` - HTTP filter
2. `JwtTokenProvider.java` - Token operations

**Separation of Concerns**:
- **Filter**: HTTP request processing
- **Provider**: Token creation/validation

**Example Structure**:
```java
// JwtAuthenticationFilter.java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(...) {
        String token = extractToken(request);
        if (jwtTokenProvider.validateToken(token)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}

// JwtTokenProvider.java
@Component
public class JwtTokenProvider {
    public String generateToken(UserDetails userDetails) { ... }
    public boolean validateToken(String token) { ... }
    public Authentication getAuthentication(String token) { ... }
}
```

**Rules**:
- ✅ Filter handles HTTP only
- ✅ Provider handles tokens only
- ❌ NO combined logic in one class

---

#### service/
**Purpose**: Business logic layer

**Structure**:
```
service/
├── StudentService.java          # Interface
└── StudentServiceImpl.java      # Implementation
```

**NOT**:
```
service/
├── StudentService.java
└── impl/                         # ❌ FORBIDDEN
    └── StudentServiceImpl.java
```

**Rules**:
- ✅ Keep interfaces and implementations together
- ✅ Use `@Service` on implementation
- ✅ `@Transactional` where needed
- ❌ NO `service/impl/` subdirectory

---

#### validation/
**Purpose**: Custom validators (optional but recommended)

**Example**:
```java
@Component
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Validation logic
    }
}
```

---

## Configuration Files

### 1. application.yml (Default Profile)

**Purpose**: Local development configuration

**Location**: `src/main/resources/application.yml`

**Template**:
```yaml
spring:
  application:
    name: {service-name}          # e.g., student-service

  datasource:
    url: jdbc:postgresql://localhost:5432/{service}_db
    username: sms_user
    password: ${DB_PASSWORD:password}     # Default: "password" for local dev
    driver-class-name: org.postgresql.drivers.PostgresDriver

  jpa:
    hibernate:
      ddl-auto: update                     # Auto-create tables in dev
    show-sql: true                         # Show SQL queries in logs
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 808X                               # Service-specific port
                                           # 8081 = auth-service
                                           # 8082 = student-service
                                           # 8083 = attendance-service, etc.

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here-minimum-32-chars}
  expiration: 86400000                     # 24 hours (in milliseconds)
  refresh-expiration: 2592000000           # 30 days (if using refresh tokens)

eureka:
  client:
    enabled: false                         # Disable Eureka for local development

logging:
  level:
    com.sms.{service}: DEBUG               # Service-specific logging
    org.springframework.security: DEBUG    # Security logging (for debugging)
```

**Key Points**:
- Uses `localhost` for database
- Eureka disabled (not needed for local dev)
- Shows SQL queries for debugging
- All secrets use environment variables with defaults

---

### 2. application-docker.yml (Docker Profile)

**Purpose**: Docker/containerized deployment

**Location**: `src/main/resources/application-docker.yml`

**Template**:
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate                   # Don't auto-create in Docker (use migrations)
    show-sql: false                        # Disable SQL logging in containers

eureka:
  instance:
    hostname: {service-name}               # MUST match Docker service name
    prefer-ip-address: false               # MUST be false for Docker
  client:
    enabled: true
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
    register-with-eureka: true
    fetch-registry: true

logging:
  level:
    com.sms.{service}: INFO                # Less verbose logging in containers
    org.springframework.security: WARN

# Optional: Redis configuration (if service uses Redis)
spring:
  redis:
    host: ${SPRING_REDIS_HOST:redis}
    port: ${SPRING_REDIS_PORT:6379}
    password: ${SPRING_REDIS_PASSWORD:}
```

**Key Points**:
- All values externalized via environment variables
- Eureka enabled with hostname-based registration
- `prefer-ip-address: false` prevents multi-network issues
- Production-ready logging levels

---

### 3. application-test.yml (Test Profile - Optional)

**Purpose**: Integration testing configuration

**Location**: `src/main/resources/application-test.yml`

**Template**:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb                # In-memory database for tests
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop                # Recreate schema for each test
    show-sql: false

  redis:
    host: localhost                        # Embedded Redis for tests
    port: 6379

eureka:
  client:
    enabled: false                         # Disable service discovery in tests

jwt:
  secret: test-secret-key-for-unit-tests-minimum-32-characters-long
  expiration: 3600000                      # 1 hour for tests
```

**Usage**:
```java
@SpringBootTest
@ActiveProfiles("test")
public class StudentServiceTests {
    // Tests use application-test.yml
}
```

---

## Required Configuration Classes

### 1. CorsConfig.java

**Purpose**: Cross-Origin Resource Sharing configuration

**Location**: `config/CorsConfig.java`

**Template**:
```java
package com.sms.{service}.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:3000",      // React dev server
                    "http://localhost:5173",      // Vite dev server
                    "http://localhost:8080"       // API Gateway
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

**When to Modify**:
- Add production frontend URLs
- Adjust allowed methods per service
- Configure credentials policy

---

### 2. OpenAPIConfig.java

**Purpose**: Swagger/OpenAPI documentation

**Location**: `config/OpenAPIConfig.java`

**NOTE**: Capitalization is `OpenAPIConfig` NOT `OpenApiConfig`

**Template**:
```java
package com.sms.{service}.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI {service}API() {
        // CRITICAL: Server URL MUST point to API Gateway
        Server server = new Server();
        server.setUrl("http://localhost:8080");    // API Gateway port
        server.setDescription("API Gateway");

        return new OpenAPI()
                .servers(List.of(server))
                .info(new Info()
                        .title("{Service Name} API")
                        .description("API documentation for {service name}")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token")));
    }
}
```

**CRITICAL RULE**:
- ✅ Server URL MUST be `http://localhost:8080` (API Gateway)
- ❌ NOT `http://localhost:8082` (direct service port)

**Rationale**: Prevents CORS errors when Swagger UI calls APIs

**Access Swagger UI**:
- URL: `http://localhost:8080/swagger-ui.html`
- Select service from dropdown

---

### 3. SecurityConfig.java

**Purpose**: Spring Security configuration

**Location**: `config/SecurityConfig.java`

**Template**:
```java
package com.sms.{service}.config;

import com.sms.{service}.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                    // Disable CSRF for API
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Stateless (JWT)
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/**"
                ).permitAll()
                // Service-specific public endpoints
                .requestMatchers(
                    "/api/auth/**"                           // Auth endpoints (login, register)
                ).permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

**Customization Points**:
- Add service-specific public endpoints
- Configure role-based access: `.hasRole("ADMIN")`
- Add custom authentication providers

---

### 4. Service-Specific Configurations

#### PasswordEncoderConfig.java (Auth Services)

**When Needed**: Services that handle password authentication

```java
package com.sms.{service}.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

#### RedisConfig.java (Caching/Session Services)

**When Needed**: Services using Redis for caching or refresh tokens

```java
package com.sms.{service}.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
```

---

#### FileUploadConfig.java (File Handling Services)

**When Needed**: Services that handle file uploads (student photos, documents)

```java
package com.sms.{service}.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${upload.dir:/app/uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
```

---

## JWT Architecture

### Two-Class Separation Pattern

**CRITICAL RULE**: JWT logic MUST be split into two classes:
1. `JwtAuthenticationFilter.java` - HTTP filter
2. `JwtTokenProvider.java` - Token operations

**Location**: `security/` package (NOT `config/`)

---

### JwtAuthenticationFilter.java

**Purpose**: HTTP request filtering for JWT authentication

**Responsibilities**:
- Extract JWT from Authorization header
- Delegate validation to `JwtTokenProvider`
- Set Spring Security context

**Template**:
```java
package com.sms.{service}.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set authentication for user: {}", authentication.getName());
            }
        } catch (Exception ex) {
            log.error("Cannot set user authentication: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // Remove "Bearer " prefix
        }

        return null;
    }
}
```

**Key Points**:
- ✅ Extends `OncePerRequestFilter`
- ✅ Delegates validation to `JwtTokenProvider`
- ✅ No token parsing logic (provider handles that)
- ❌ NO direct JWT library usage

---

### JwtTokenProvider.java

**Purpose**: JWT token creation, parsing, and validation

**Responsibilities**:
- Generate JWT tokens
- Parse and validate tokens
- Extract claims (username, roles, etc.)
- Check expiration

**Template**:
```java
package com.sms.{service}.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey jwtSecret;
    private final long jwtExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpiration = expiration;
    }

    /**
     * Generate JWT token from username and roles
     */
    public String generateToken(String username, Collection<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", String.join(",", roles))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecret, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract username from token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Validate token signature and expiration
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Get Authentication object from token
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        String rolesString = claims.get("roles", String.class);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(rolesString.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
}
```

**Key Points**:
- ✅ Handles all JWT operations
- ✅ No HTTP dependencies
- ✅ Reusable in other contexts (e.g., WebSocket)
- ❌ NO request/response handling

---

## Docker Compose Configuration

### Service Definition Template

**Location**: `docker-compose.yml` (project root)

**Template**:
```yaml
services:
  {service-name}:
    build:
      context: ./{service-name}
      dockerfile: Dockerfile
    container_name: {service-name}
    environment:
      # Profile activation
      - SPRING_PROFILES_ACTIVE=docker

      # Database configuration (Spring Boot standard)
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-{service}:5432/{service}_db
      - SPRING_DATASOURCE_USERNAME=sms_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

      # Eureka service discovery
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

      # JWT authentication
      - JWT_SECRET=${JWT_SECRET}

      # Optional: Redis (if service uses caching)
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379

      # Optional: Service-specific variables
      - UPLOAD_DIR=/app/uploads/{service}
      - MAX_FILE_SIZE=10485760                    # 10MB

    ports:
      - "{external-port}:{internal-port}"         # e.g., "8082:8082"

    networks:
      - backend-network                           # Required: For Eureka/API Gateway
      - database-network                          # Required: If service has database

    depends_on:
      - postgres-{service}                        # Database dependency
      - eureka-server                             # Service discovery dependency
      # - redis                                   # Optional: If using Redis

    volumes:
      - {service}-uploads:/app/uploads/{service}  # Persistent file storage

    restart: unless-stopped

    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8082/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

# Networks
networks:
  backend-network:
    driver: bridge
  database-network:
    driver: bridge

# Volumes
volumes:
  {service}-uploads:
    driver: local
```

### Environment Variable Checklist

**REQUIRED** (all services):
- ✅ `SPRING_PROFILES_ACTIVE=docker`
- ✅ `SPRING_DATASOURCE_URL`
- ✅ `SPRING_DATASOURCE_USERNAME`
- ✅ `SPRING_DATASOURCE_PASSWORD`
- ✅ `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`
- ✅ `JWT_SECRET`

**OPTIONAL** (service-specific):
- ○ `SPRING_REDIS_HOST` (caching services)
- ○ `UPLOAD_DIR` (file handling services)
- ○ Custom service variables

**FORBIDDEN**:
- ❌ `DB_USERNAME` (use `SPRING_DATASOURCE_USERNAME`)
- ❌ `DB_PASSWORD` (use `SPRING_DATASOURCE_PASSWORD`)
- ❌ `EUREKA_CLIENT_SERVICE_URL` (use `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`)
- ❌ `EUREKA_INSTANCE_HOSTNAME` (configure in YAML)
- ❌ `EUREKA_INSTANCE_PREFER_IP_ADDRESS` (configure in YAML)

---

### .env File Template

**Location**: Project root

**Purpose**: Store secrets and environment-specific values

**Template**:
```properties
# Database Credentials
DB_PASSWORD=your_secure_postgres_password_here

# JWT Secret (minimum 32 characters)
JWT_SECRET=your-256-bit-secret-key-here-minimum-32-chars

# Redis Password (if using Redis)
REDIS_PASSWORD=your_redis_password_here

# Service-Specific Secrets
# Add as needed per service
```

**Security Rules**:
- ✅ Add `.env` to `.gitignore`
- ✅ Provide `.env.example` in repo
- ✅ Use strong secrets in production
- ❌ NEVER commit `.env` to version control

---

## Service Creation Workflow

### Step-by-Step Process

#### Step 1: Copy Template Service

```bash
# Navigate to project root
cd /path/to/salarean

# Copy auth-service as template
cp -r auth-service new-service

# Navigate to new service
cd new-service
```

---

#### Step 2: Rename Packages

```bash
# Option A: Manual find/replace
# Replace all occurrences of "auth" with "newservice"

# Option B: Automated (macOS/Linux)
find . -type f -name "*.java" -exec sed -i '' 's/com\.sms\.auth/com.sms.newservice/g' {} +
find . -type f -name "*.java" -exec sed -i '' 's/auth-service/new-service/g' {} +

# Option C: Automated (Linux)
find . -type f -name "*.java" -exec sed -i 's/com\.sms\.auth/com.sms.newservice/g' {} +
find . -type f -name "*.java" -exec sed -i 's/auth-service/new-service/g' {} +
```

---

#### Step 3: Rename Directory Structure

```bash
# Rename package directories
cd src/main/java/com/sms
mv auth newservice

cd ../../../../../test/java/com/sms
mv auth newservice
```

---

#### Step 4: Update Configuration Files

**pom.xml**:
```xml
<artifactId>new-service</artifactId>
<name>new-service</name>
<description>Description of new service</description>
```

**application.yml**:
```yaml
spring:
  application:
    name: new-service

server:
  port: 808X  # Choose unique port (8083, 8084, etc.)
```

**application-docker.yml**:
```yaml
eureka:
  instance:
    hostname: new-service  # Match service name
```

---

#### Step 5: Update Main Application Class

**Rename file**: `{Service}Application.java`

```java
package com.sms.newservice;

@SpringBootApplication
@EnableEurekaClient
public class NewServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NewServiceApplication.class, args);
    }
}
```

---

#### Step 6: Remove Auth-Specific Code

Delete or modify:
- ❌ `RefreshTokenRepository.java` (if not needed)
- ❌ `RedisConfig.java` (if not using Redis)
- ❌ Auth-specific controllers, services
- ✅ Keep `JwtAuthenticationFilter`, `JwtTokenProvider` (needed for JWT validation)

---

#### Step 7: Add Service-Specific Code

Create:
- Domain entities in `model/`
- Repositories in `repository/`
- Services in `service/`
- Controllers in `controller/`
- DTOs in `dto/request/` and `dto/response/`

---

#### Step 8: Update Docker Compose

**Add service definition**:
```yaml
new-service:
  build: ./new-service
  container_name: new-service
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-new:5432/new_db
    - SPRING_DATASOURCE_USERNAME=sms_user
    - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    - JWT_SECRET=${JWT_SECRET}
  ports:
    - "808X:808X"
  networks:
    - backend-network
    - database-network
  depends_on:
    - postgres-new
    - eureka-server

postgres-new:
  image: postgres:15-alpine
  container_name: postgres-new
  environment:
    - POSTGRES_DB=new_db
    - POSTGRES_USER=sms_user
    - POSTGRES_PASSWORD=${DB_PASSWORD}
  volumes:
    - postgres-new-data:/var/lib/postgresql/data
  networks:
    - database-network

volumes:
  postgres-new-data:
```

---

#### Step 9: Validate Compliance

```bash
# Run validation script
./specs/001-service-standards/contracts/validation-script.sh ./new-service

# Expected output: All checks passed
```

---

#### Step 10: Build and Test

```bash
# Build service
cd new-service
mvn clean package

# Run locally
mvn spring-boot:run

# Or build Docker image
docker-compose build new-service

# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f new-service
```

---

## Quick Start Guide

### Create New Service (5-Minute Quickstart)

```bash
# 1. Copy template
cp -r auth-service student-service

# 2. Rename packages (automated)
cd student-service
find . -type f \( -name "*.java" -o -name "*.yml" -o -name "pom.xml" \) \
  -exec sed -i '' 's/auth/student/g' {} +

# 3. Move directories
cd src/main/java/com/sms && mv auth student
cd ../../../../../../test/java/com/sms && mv auth student

# 4. Update ports (student-service uses 8082)
sed -i '' 's/8081/8082/g' src/main/resources/application.yml

# 5. Validate
../specs/001-service-standards/contracts/validation-script.sh .

# 6. Build and run
mvn clean package
mvn spring-boot:run
```

---

## Troubleshooting

### Common Issues

#### Issue 1: "Service not registering with Eureka"

**Symptoms**:
- Service starts but doesn't appear in Eureka dashboard
- API Gateway can't find service

**Solution**:
1. Check `eureka.instance.prefer-ip-address: false` in `application-docker.yml`
2. Verify `hostname` matches Docker service name
3. Check `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` env var
4. Ensure service is on `backend-network`

```yaml
# Correct configuration
eureka:
  instance:
    hostname: student-service      # Must match docker-compose service name
    prefer-ip-address: false       # CRITICAL
```

---

#### Issue 2: "CORS errors in Swagger UI"

**Symptoms**:
- Swagger UI loads but API calls fail with CORS error

**Solution**:
1. Verify `OpenAPIConfig` server URL is `http://localhost:8080` (API Gateway)
2. Check `CorsConfig` allows `http://localhost:8080`

```java
// WRONG
server.setUrl("http://localhost:8082");

// CORRECT
server.setUrl("http://localhost:8080");
```

---

#### Issue 3: "JWT validation fails"

**Symptoms**:
- 401 Unauthorized on protected endpoints
- Logs show "Invalid JWT signature"

**Solution**:
1. Verify all services use same `JWT_SECRET` env var
2. Check token is sent in `Authorization: Bearer {token}` header
3. Ensure `JwtTokenProvider` secret matches across services

```bash
# Check .env file
echo $JWT_SECRET

# All services MUST use same secret
```

---

#### Issue 4: "Database connection refused"

**Symptoms**:
- Service fails to start with "Connection refused" error

**Solution**:
1. Check database service is running: `docker-compose ps`
2. Verify `SPRING_DATASOURCE_URL` uses correct hostname
3. Ensure service is on `database-network`

```yaml
# Correct database URL
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-student:5432/student_db
#                                      ^^^^^^^^^^^^^^
#                                      Must match postgres service name
```

---

#### Issue 5: "Package structure validation fails"

**Symptoms**:
- Validation script reports package issues

**Solution**:
1. Ensure entities are in `model/` NOT `entity/`
2. Move JWT classes to `security/` NOT `config/`
3. Flatten `service/impl/` to `service/`

```bash
# Run validation to see specific issues
./specs/001-service-standards/contracts/validation-script.sh ./your-service
```

---

## Reference Documents

### Primary References
- **Compliance Checklist**: `compliance-checklist.md`
- **Validation Script**: `validation-script.sh`
- **Service Comparison**: `../../SERVICE_COMPARISON_ANALYSIS.md`
- **Data Model**: `../data-model.md`
- **Research**: `../research.md`

### Template Service
- **Path**: `/Volumes/DATA/my-projects/salarean/auth-service`
- **Status**: Production-ready template
- **When to Reference**: Always - this is the authoritative example

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-11-22 | Initial template documentation |

---

**Maintained By**: Salarean Development Team
**Last Updated**: 2025-11-22
**Template Version**: 1.0.0
