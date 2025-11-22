# Configuration Patterns Standards

**Version**: 1.0.0
**Last Updated**: 2025-11-22
**Compliance**: MANDATORY for all microservices

---

## Overview

All microservices MUST include three core configuration classes:

1. **CorsConfig** - Cross-Origin Resource Sharing (CORS) settings
2. **OpenAPIConfig** - Swagger/OpenAPI documentation
3. **SecurityConfig** - Spring Security configuration

Additional configuration classes may be added based on service requirements (e.g., Redis, password encoding, file uploads).

---

## Table of Contents

1. [Required Configuration Classes](#required-configuration-classes)
2. [CorsConfig Pattern](#corsconfig-pattern)
3. [OpenAPIConfig Pattern](#openapiconfig-pattern)
4. [SecurityConfig Pattern](#securityconfig-pattern)
5. [Optional Configuration Classes](#optional-configuration-classes)
6. [Configuration Best Practices](#configuration-best-practices)

---

## Required Configuration Classes

### Mandatory for ALL Services

Every microservice MUST have these three configuration classes in the `config/` package:

| Class Name | Purpose | Mandatory? |
|------------|---------|------------|
| **CorsConfig.java** | CORS settings for cross-origin requests | ✅ Yes |
| **OpenAPIConfig.java** | Swagger UI and API documentation | ✅ Yes |
| **SecurityConfig.java** | Spring Security filter chain | ✅ Yes |

**Critical Rules**:
- ✅ Classes MUST be in `config/` package (e.g., `com.sms.auth.config`)
- ✅ Classes MUST be annotated with `@Configuration`
- ✅ Names MUST follow exact capitalization (e.g., `OpenAPIConfig`, NOT `OpenApiConfig`)

**Validation**:

Run the validation script to check:

```bash
.standards/scripts/validate-service-structure.sh <service-name>
```

The script will verify all required configuration classes are present.

---

## CorsConfig Pattern

### Purpose

**CorsConfig** allows cross-origin HTTP requests from frontend applications running on different domains/ports.

### Why It's Required

- ✅ Enables frontend (e.g., React on `http://localhost:3000`) to call backend APIs
- ✅ Prevents CORS errors in browser console
- ✅ Standardizes CORS policy across all microservices

### Standard Implementation

**File**: `src/main/java/com/sms/{service}/config/CorsConfig.java`

```java
package com.sms.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### Configuration Breakdown

**1. Allowed Origins**
```java
configuration.setAllowedOrigins(List.of("*"));
```
- `"*"` allows requests from ANY origin
- **Development**: Use `"*"` for simplicity
- **Production**: Replace with specific frontend URLs (e.g., `"https://sms.example.com"`)

**2. Allowed Methods**
```java
configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
```
- Allows all standard HTTP methods
- `OPTIONS` is required for preflight requests

**3. Allowed Headers**
```java
configuration.setAllowedHeaders(List.of("*"));
```
- `"*"` allows all headers
- Ensures `Authorization` header (for JWT) is allowed

**4. Allow Credentials**
```java
configuration.setAllowCredentials(false);
```
- `false` when using `AllowedOrigins("*")`
- Set to `true` when using specific origins with cookies/authentication

**5. Max Age**
```java
configuration.setMaxAge(3600L);
```
- Browsers cache preflight OPTIONS requests for 3600 seconds (1 hour)

**6. URL Patterns**
```java
source.registerCorsConfiguration("/**", configuration);
```
- Applies CORS policy to ALL endpoints (`/**`)

### Customization for Production

**Specific Origins**:
```java
configuration.setAllowedOrigins(List.of(
    "https://sms.example.com",
    "https://admin.sms.example.com"
));
configuration.setAllowCredentials(true);  // Enable when using specific origins
```

**Environment-Based Configuration**:
```java
@Value("${app.cors.allowed-origins}")
private String allowedOrigins;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
    // ... rest of configuration
}
```

In `application.yml`:
```yaml
app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:3001}
```

---

## OpenAPIConfig Pattern

### Purpose

**OpenAPIConfig** configures Swagger UI for interactive API documentation and testing.

### Why It's Required

- ✅ Provides interactive API documentation accessible at `/swagger-ui.html`
- ✅ Allows testing API endpoints with JWT authentication
- ✅ Auto-generates API specs from controller annotations

### Standard Implementation

**File**: `src/main/java/com/sms/{service}/config/OpenAPIConfig.java`

```java
package com.sms.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for auth-service.
 * Adds Bearer JWT authentication support for testing protected endpoints.
 */
@Configuration
public class OpenAPIConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI authServiceAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8080");
        server.setDescription("API Gateway");

        return new OpenAPI()
                .servers(List.of(server))
                .info(new Info()
                        .title("Authentication Service API")
                        .description("Authentication and authorization service for SMS")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, createSecurityScheme()))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token obtained from /api/auth/login or /api/auth/register");
    }
}
```

### Configuration Breakdown

**1. Server Configuration** (CRITICAL)
```java
Server server = new Server();
server.setUrl("http://localhost:8080");  // API Gateway port, NOT service port
server.setDescription("API Gateway");
```

**Critical Rule**:
- ✅ Server URL MUST point to API Gateway (port 8080)
- ❌ NEVER point to direct service port (e.g., 8081, 8082)
- **Reason**: Prevents CORS errors when Swagger UI calls endpoints through the gateway

**2. API Metadata**
```java
.info(new Info()
    .title("Authentication Service API")       // Service-specific title
    .description("Authentication and authorization service for SMS")
    .version("1.0.0"))
```

Customize:
- `title`: Service name + "API"
- `description`: Brief service purpose
- `version`: Semantic version (1.0.0, 1.1.0, etc.)

**3. Security Scheme** (JWT Authentication)
```java
private SecurityScheme createSecurityScheme() {
    return new SecurityScheme()
            .name(SECURITY_SCHEME_NAME)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("Enter JWT token obtained from /api/auth/login or /api/auth/register");
}
```

**What This Does**:
- Adds "Authorize" button in Swagger UI
- Allows entering JWT token for testing protected endpoints
- Automatically adds `Authorization: Bearer {token}` header to requests

**4. Global Security Requirement**
```java
.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
```

Applies Bearer Authentication to ALL endpoints by default.

**Override for Public Endpoints**:
```java
@Operation(summary = "Login", security = @SecurityRequirement(name = ""))
@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    // ...
}
```

The `security = @SecurityRequirement(name = "")` removes the lock icon for public endpoints.

### Customization for Different Services

**Student Service Example**:
```java
@Bean
public OpenAPI studentServiceAPI() {
    Server server = new Server();
    server.setUrl("http://localhost:8080");  // Always API Gateway
    server.setDescription("API Gateway");

    return new OpenAPI()
            .servers(List.of(server))
            .info(new Info()
                    .title("Student Service API")              // Updated title
                    .description("Student management service")  // Updated description
                    .version("1.0.0"))
            .components(new Components()
                    .addSecuritySchemes(SECURITY_SCHEME_NAME, createSecurityScheme()))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
}
```

**Only change**:
- ✅ Update `title` and `description`
- ✅ Keep server URL as `http://localhost:8080`
- ✅ Keep security configuration identical

---

## SecurityConfig Pattern

### Purpose

**SecurityConfig** defines the Spring Security filter chain, including:
- CORS integration
- CSRF protection (disabled for stateless JWT APIs)
- Public vs protected endpoints
- JWT authentication filter

### Why It's Required

- ✅ Integrates CORS configuration
- ✅ Defines which endpoints require authentication
- ✅ Adds JWT authentication filter to request pipeline
- ✅ Enables stateless session management

### Standard Implementation

**File**: `src/main/java/com/sms/{service}/config/SecurityConfig.java`

```java
package com.sms.auth.config;

import com.sms.auth.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh",
                                "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
```

### Configuration Breakdown

**1. Annotations**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
```

- `@Configuration`: Marks this as a Spring configuration class
- `@EnableWebSecurity`: Enables Spring Security
- `@EnableMethodSecurity`: Allows `@PreAuthorize`, `@PostAuthorize` annotations

**2. Dependencies**
```java
private final CorsConfigurationSource corsConfigurationSource;
private final JwtAuthenticationFilter jwtAuthenticationFilter;

public SecurityConfig(CorsConfigurationSource corsConfigurationSource,
                      JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.corsConfigurationSource = corsConfigurationSource;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
}
```

**Constructor Injection**:
- Injects `CorsConfigurationSource` bean from `CorsConfig`
- Injects `JwtAuthenticationFilter` bean from `security/` package

**3. CORS Integration**
```java
.cors(cors -> cors.configurationSource(corsConfigurationSource))
```

Applies CORS configuration from `CorsConfig` to all endpoints.

**4. CSRF Protection**
```java
.csrf(AbstractHttpConfigurer::disable)
```

**Disabled** because:
- JWT-based authentication is stateless (no cookies)
- CSRF protection is only needed for session-based auth

**5. Session Management**
```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

**STATELESS**: No HTTP session created or used.

**6. Authorization Rules**
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh",
            "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
    .requestMatchers("/actuator/**").permitAll()
    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
    .anyRequest().authenticated()
)
```

**Public Endpoints** (`.permitAll()`):
- Authentication endpoints (register, login, refresh)
- Health check endpoints (`/actuator/**`)
- Swagger documentation (`/v3/api-docs/**`, `/swagger-ui/**`)

**Protected Endpoints** (`.authenticated()`):
- Everything else requires valid JWT

**7. JWT Filter**
```java
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
```

Adds JWT authentication filter BEFORE Spring's default authentication filter.

**Order matters**:
1. `JwtAuthenticationFilter` runs first (extracts and validates JWT)
2. Sets `SecurityContext` with authenticated user
3. Spring Security checks authorization rules

### Customization for Different Services

**Student Service Example**:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // Public endpoints (if any)
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    // All other endpoints require authentication
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
}
```

**Changes**:
- ✅ Removed auth-specific public endpoints (register, login)
- ✅ Kept health check and Swagger endpoints public
- ✅ All other configuration identical

**Role-Based Authorization** (Optional):
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/students/**").hasRole("TEACHER")
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
```

---

## Optional Configuration Classes

### PasswordEncoderConfig

**When to Use**: Services that hash passwords (e.g., auth-service, teacher-service)

**File**: `src/main/java/com/sms/auth/config/PasswordEncoderConfig.java`

```java
package com.sms.auth.config;

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

**Why Separate Config**:
- ✅ Reusable across services
- ✅ Easy to inject wherever password hashing is needed
- ✅ Follows single responsibility principle

**Usage in Service**:
```java
@Service
public class AuthServiceImpl implements AuthService {
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        // ...
    }
}
```

### RedisConfig

**When to Use**: Services using Redis (e.g., for caching, refresh tokens)

**File**: `src/main/java/com/sms/auth/config/RedisConfig.java`

```java
package com.sms.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}
```

**Configuration in `application.yml`**:
```yaml
spring:
  redis:
    host: ${SPRING_REDIS_HOST:localhost}
    port: ${SPRING_REDIS_PORT:6379}
    password: ${SPRING_REDIS_PASSWORD:}
```

### FileUploadConfig

**When to Use**: Services handling file uploads (e.g., student photos, documents)

**File**: `src/main/java/com/sms/student/config/FileUploadConfig.java`

```java
package com.sms.student.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
```

**Configuration in `application.yml`**:
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
      enabled: true

app:
  file-upload:
    base-path: ${FILE_UPLOAD_BASE_PATH:uploads/}
    allowed-extensions: jpg,jpeg,png,pdf
```

---

## Configuration Best Practices

### 1. Naming Conventions

**Format**: `{Purpose}Config`

✅ **Correct**:
- `CorsConfig.java`
- `OpenAPIConfig.java` (note: "API" all caps)
- `SecurityConfig.java`
- `RedisConfig.java`

❌ **Incorrect**:
- `CorsConfiguration.java` (too verbose)
- `OpenApiConfig.java` (should be "OpenAPI")
- `ConfigCors.java` (wrong order)

### 2. Package Location

**Rule**: All configuration classes MUST be in `config/` package.

✅ **Correct**:
```
com.sms.auth.config.CorsConfig
com.sms.auth.config.OpenAPIConfig
com.sms.auth.config.SecurityConfig
```

❌ **Incorrect**:
```
com.sms.auth.CorsConfig              (missing config/ package)
com.sms.auth.configuration.CorsConfig (wrong package name)
```

### 3. Separation of Concerns

**Rule**: Configuration classes should ONLY configure, not contain business logic.

✅ **Correct**:
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // Configuration only
    }
}
```

❌ **Incorrect**:
```java
@Configuration
public class SecurityConfig {
    public boolean validateUser(String username) {
        // Business logic - belongs in service layer
    }
}
```

### 4. Dependency Injection

**Rule**: Use constructor injection for dependencies.

✅ **Correct**:
```java
@Configuration
public class SecurityConfig {
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }
}
```

❌ **Incorrect**:
```java
@Configuration
public class SecurityConfig {
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;  // Field injection
}
```

### 5. Environment-Based Configuration

**Rule**: Use `@Value` or `@ConfigurationProperties` for environment-specific values.

✅ **Correct**:
```java
@Configuration
public class CorsConfig {
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Use allowedOrigins
    }
}
```

**In `application.yml`**:
```yaml
app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
```

### 6. Bean Naming

**Rule**: Bean method names should be descriptive and unique.

✅ **Correct**:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) { }

@Bean
public CorsConfigurationSource corsConfigurationSource() { }
```

❌ **Incorrect**:
```java
@Bean
public SecurityFilterChain chain(HttpSecurity http) { }  // Too vague

@Bean
public CorsConfigurationSource cors() { }  // Too short
```

---

## Quick Reference

### Configuration Checklist

When creating a new service, ensure these configurations exist:

**Required** (all services):
- [ ] `CorsConfig.java` - CORS settings
- [ ] `OpenAPIConfig.java` - Swagger/API docs (note capitalization)
- [ ] `SecurityConfig.java` - Spring Security filter chain

**Optional** (as needed):
- [ ] `PasswordEncoderConfig.java` - BCrypt password encoder (auth services)
- [ ] `RedisConfig.java` - Redis connection (if using Redis)
- [ ] `FileUploadConfig.java` - File upload settings (if handling uploads)

### Common Configuration Mistakes

| Issue | Incorrect | Correct |
|-------|-----------|---------|
| Class naming | `OpenApiConfig.java` | `OpenAPIConfig.java` |
| Package location | `com.sms.auth.configuration` | `com.sms.auth.config` |
| Server URL | `http://localhost:8082` | `http://localhost:8080` (API Gateway) |
| CORS origins | Hardcoded | Environment variable |
| Dependency injection | `@Autowired` field | Constructor injection |

---

## Related Documentation

- **Package Structure**: `.standards/docs/package-structure.md`
- **Naming Conventions**: `.standards/docs/naming-conventions.md`
- **JWT Architecture**: `.standards/docs/jwt-architecture.md`
- **Service Template**: `.standards/templates/service-template.md`

---

## Version History

| Version | Date       | Changes                       |
|---------|------------|-------------------------------|
| 1.0.0   | 2025-11-22 | Initial configuration patterns |

---

## Support

For questions about configuration:

1. Review this document for pattern details
2. Check auth-service as reference implementation
3. Run validation script to verify required classes
4. Consult service template for complete examples
