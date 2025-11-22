# Reusable Components Guide

## Overview

This guide explains how to use the standardized Java templates to accelerate microservice development. All templates are production-ready, copy-paste friendly, and require minimal customization.

**Location**: `.standards/templates/java/`

**Philosophy**: Copy → Rename Package → Customize TODOs → Done

## Available Templates

| Template | Purpose | Customization Level |
|----------|---------|-------------------|
| `CorsConfig.java` | CORS configuration for cross-origin requests | Minimal (package only) |
| `OpenAPIConfig.java` | Swagger/OpenAPI documentation setup | Low (package + metadata) |
| `SecurityConfig.java` | Spring Security with JWT integration | Medium (package + endpoints) |
| `JwtAuthenticationFilter.java` | JWT request interception & validation | Minimal (package only) |
| `JwtTokenProvider.java` | JWT token generation & parsing | Medium (package + claims) |

## Copy-Paste Workflow

### Step 1: Copy Template to Your Service

```bash
# Example: Adding JWT authentication to student-service
cp .standards/templates/java/JwtTokenProvider.java \
   student-service/src/main/java/com/sms/student/security/JwtTokenProvider.java
```

### Step 2: Rename Package

Find and replace `SERVICENAME` with your actual service name:

```java
// Before
package com.sms.SERVICENAME.security;

// After (for student-service)
package com.sms.student.security;
```

**Tools**:
- IntelliJ IDEA: `Cmd+Shift+R` (Mac) or `Ctrl+Shift+R` (Windows/Linux)
- VS Code: `Cmd+H` (Mac) or `Ctrl+H` (Windows/Linux)
- Command line: `sed -i 's/SERVICENAME/student/g' *.java`

### Step 3: Complete TODO Markers

All templates include `// TODO:` comments indicating customization points:

```java
// TODO: Replace SERVICENAME with your service name
// TODO: Customize public endpoints based on your service
// TODO: Update title (e.g., "Authentication Service API")
```

Search for `TODO` in the file and address each one.

### Step 4: Verify Compilation

```bash
# Maven
./mvnw clean compile

# Gradle
./gradlew clean build
```

If compilation succeeds, the template is ready for use.

## Template Details

### 1. CorsConfig.java

**Purpose**: Configure Cross-Origin Resource Sharing (CORS) to allow frontend applications to access your API.

**Package**: `com.sms.{service}.config`

**Customization Required**:
- ✅ Package name only (`SERVICENAME` → your service name)

**Optional Customization**:
- Allowed origins (default: `*` for development)
- Allowed methods (default: GET, POST, PUT, DELETE, OPTIONS, PATCH)
- Allow credentials (default: `false`)

**Copy Command**:
```bash
cp .standards/templates/java/CorsConfig.java \
   {service}/src/main/java/com/sms/{service}/config/CorsConfig.java
```

**Example Usage**:
```java
// auth-service example
package com.sms.auth.config;  // Changed from SERVICENAME

@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // ... (no other changes needed)
    }
}
```

**Production Considerations**:
- Replace `setAllowedOrigins(List.of("*"))` with specific domains
- Example: `setAllowedOrigins(List.of("https://yourdomain.com", "https://admin.yourdomain.com"))`

---

### 2. OpenAPIConfig.java

**Purpose**: Configure Swagger UI for interactive API documentation with JWT authentication support.

**Package**: `com.sms.{service}.config`

**Customization Required**:
1. ✅ Package name (`SERVICENAME` → your service name)
2. ✅ Method name (`servicenameAPI()` → `{service}API()`)
3. ✅ API title and description

**CRITICAL - Do NOT Change**:
- Server URL MUST be `http://localhost:8080` (API Gateway)
- Changing this causes CORS errors in Swagger UI

**Copy Command**:
```bash
cp .standards/templates/java/OpenAPIConfig.java \
   {service}/src/main/java/com/sms/{service}/config/OpenAPIConfig.java
```

**Example Customization**:
```java
// student-service example
package com.sms.student.config;  // Changed from SERVICENAME

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI studentAPI() {  // Changed from servicenameAPI()
        Server server = new Server();
        server.setUrl("http://localhost:8080");  // DO NOT CHANGE
        server.setDescription("API Gateway");

        return new OpenAPI()
                .servers(List.of(server))
                .info(new Info()
                        .title("Student Service API")  // Updated
                        .description("Manages student profiles, enrollment, and academic records")  // Updated
                        .version("1.0.0"))
                // ... (security config remains the same)
    }
}
```

**Accessing Swagger UI**:
- Local: `http://localhost:{service-port}/swagger-ui.html`
- Via Gateway: `http://localhost:8080/{service-context}/swagger-ui.html`

---

### 3. SecurityConfig.java

**Purpose**: Configure Spring Security with JWT authentication, CORS integration, and endpoint authorization.

**Package**: `com.sms.{service}.config`

**Customization Required**:
1. ✅ Package name (`SERVICENAME` → your service name)
2. ✅ Import path (`com.sms.SERVICENAME.security.JwtAuthenticationFilter`)
3. ✅ Public endpoints (add service-specific unauthenticated routes)

**Copy Command**:
```bash
cp .standards/templates/java/SecurityConfig.java \
   {service}/src/main/java/com/sms/{service}/config/SecurityConfig.java
```

**Example Customization**:
```java
// auth-service example
package com.sms.auth.config;  // Changed from SERVICENAME

import com.sms.auth.security.JwtAuthenticationFilter;  // Updated import

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Service-specific public endpoints
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()

                        // Standard public endpoints (keep these)
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
```

**Common Public Endpoints by Service Type**:
- **Auth Service**: `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`
- **Public API Service**: `/api/public/**`
- **Webhook Service**: `/webhooks/**`
- **Report Service**: May have no public endpoints (all authenticated)

**Always Keep Public**:
- `/actuator/**` - Health checks and metrics
- `/v3/api-docs/**`, `/swagger-ui/**` - API documentation

---

### 4. JwtAuthenticationFilter.java

**Purpose**: Intercept HTTP requests, extract JWT tokens from Authorization headers, validate them, and set Spring Security authentication context.

**Package**: `com.sms.{service}.security`

**Customization Required**:
- ✅ Package name only (`SERVICENAME` → your service name)

**Usually No Other Changes Needed**: This filter delegates all business logic to `JwtTokenProvider` and `UserDetailsService`.

**Copy Command**:
```bash
cp .standards/templates/java/JwtAuthenticationFilter.java \
   {service}/src/main/java/com/sms/{service}/security/JwtAuthenticationFilter.java
```

**Example**:
```java
// student-service example
package com.sms.student.security;  // Changed from SERVICENAME

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // ... (no other changes needed)
}
```

**How It Works**:
1. Extracts JWT from `Authorization: Bearer {token}` header
2. Validates token using `JwtTokenProvider.validateToken()`
3. Extracts user ID using `JwtTokenProvider.getUserIdFromToken()`
4. Loads user details from database via `UserDetailsService`
5. Sets authentication in `SecurityContextHolder`
6. Continues filter chain (allows public endpoints to work)

**Dependencies**:
- Requires `JwtTokenProvider` bean
- Requires `UserDetailsService` implementation (you must create this)

**UserDetailsService Example**:
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TeacherRepository teacherRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UUID id = UUID.fromString(userId);
        Teacher teacher = teacherRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        return User.builder()
            .username(teacher.getId().toString())
            .password(teacher.getPasswordHash())
            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER")))
            .build();
    }
}
```

---

### 5. JwtTokenProvider.java

**Purpose**: Handle all JWT token operations - generation, validation, parsing, and claims extraction.

**Package**: `com.sms.{service}.security`

**Customization Required**:
1. ✅ Package name (`SERVICENAME` → your service name)
2. 🔧 Custom claims in `generateToken()` method (optional)
3. 🔧 Custom extraction methods for your claims (optional)

**Copy Command**:
```bash
cp .standards/templates/java/JwtTokenProvider.java \
   {service}/src/main/java/com/sms/{service}/security/JwtTokenProvider.java
```

**Example Customization - Custom Claims**:
```java
// auth-service example with role-based claims
package com.sms.auth.security;  // Changed from SERVICENAME

@Component
public class JwtTokenProvider {

    public String generateToken(UUID userId, String language, String role) {  // Added role parameter
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_MS);
        String jti = UUID.randomUUID().toString();

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
            .setSubject(userId.toString())
            .setId(jti)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("lang", language)
            .claim("role", role)  // Changed from hardcoded "TEACHER"
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    // Add custom extraction method for role
    public String getRoleFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("role", String.class);
    }
}
```

**Standard Claims (Do Not Remove)**:
- `subject` - User ID (UUID)
- `id` (jti) - JWT ID for token tracking/revocation
- `issuedAt` - Token creation timestamp
- `expiration` - Token expiry timestamp

**Common Custom Claims**:
- `lang` - User's preferred language (en, km)
- `role` / `roles` - User permissions (TEACHER, STUDENT, ADMIN)
- `schoolId` - Multi-tenant school identifier
- `email` - User's email (avoid PII if possible)

**Configuration Required** (in application.yml):
```yaml
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here-must-be-at-least-32-chars}
  expiration: 86400000  # 24 hours in milliseconds
```

**Security Notes**:
- JWT secret MUST be at least 256 bits (32 characters) for HS256
- Use environment variable in production: `JWT_SECRET=${JWT_SECRET}`
- Never commit secrets to version control
- Refresh tokens are NOT JWTs - use separate `RefreshTokenService` with database storage

---

## Complete Service Setup Example

### Scenario: Adding JWT authentication to `attendance-service`

**Step 1: Copy all templates**
```bash
# Config templates
cp .standards/templates/java/CorsConfig.java \
   attendance-service/src/main/java/com/sms/attendance/config/

cp .standards/templates/java/OpenAPIConfig.java \
   attendance-service/src/main/java/com/sms/attendance/config/

cp .standards/templates/java/SecurityConfig.java \
   attendance-service/src/main/java/com/sms/attendance/config/

# Security templates
mkdir -p attendance-service/src/main/java/com/sms/attendance/security/

cp .standards/templates/java/JwtAuthenticationFilter.java \
   attendance-service/src/main/java/com/sms/attendance/security/

cp .standards/templates/java/JwtTokenProvider.java \
   attendance-service/src/main/java/com/sms/attendance/security/
```

**Step 2: Bulk rename packages**
```bash
cd attendance-service/src/main/java/com/sms/attendance/

# macOS/Linux
find . -name "*.java" -exec sed -i '' 's/SERVICENAME/attendance/g' {} +

# Linux (without '')
find . -name "*.java" -exec sed -i 's/SERVICENAME/attendance/g' {} +
```

**Step 3: Customize OpenAPIConfig**
```java
package com.sms.attendance.config;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI attendanceAPI() {  // Renamed method
        Server server = new Server();
        server.setUrl("http://localhost:8080");
        server.setDescription("API Gateway");

        return new OpenAPI()
                .servers(List.of(server))
                .info(new Info()
                        .title("Attendance Service API")  // Updated
                        .description("Manages class attendance, check-ins, and attendance reports")  // Updated
                        .version("1.0.0"))
                // ...
    }
}
```

**Step 4: Customize SecurityConfig public endpoints**
```java
.authorizeHttpRequests(auth -> auth
        // No public endpoints for attendance-service (all require auth)

        // Standard public endpoints
        .requestMatchers("/actuator/**").permitAll()
        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

        .anyRequest().authenticated()
)
```

**Step 5: Implement UserDetailsService**
```java
package com.sms.attendance.service;

@Service
public class AttendanceUserDetailsService implements UserDetailsService {

    private final TeacherRepository teacherRepository;  // Assuming teachers take attendance

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UUID id = UUID.fromString(userId);
        Teacher teacher = teacherRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        return User.builder()
            .username(teacher.getId().toString())
            .password(teacher.getPasswordHash())
            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER")))
            .build();
    }
}
```

**Step 6: Add configuration to application.yml**
```yaml
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here-must-be-at-least-32-chars}
  expiration: 86400000  # 24 hours
```

**Step 7: Verify compilation**
```bash
./mvnw clean compile
```

**Step 8: Test with Swagger**
1. Start service: `./mvnw spring-boot:run`
2. Open Swagger UI: `http://localhost:{port}/swagger-ui.html`
3. Click "Authorize" button
4. Enter JWT token from auth-service
5. Test protected endpoints

**Total time**: ~15 minutes (vs 2+ hours without templates)

---

## Verification Checklist

After copying templates, verify:

- [ ] All `SERVICENAME` placeholders replaced
- [ ] Package structure correct (`config/` and `security/`)
- [ ] All `TODO` comments addressed
- [ ] OpenAPI server URL points to API Gateway (`http://localhost:8080`)
- [ ] Public endpoints configured in SecurityConfig
- [ ] UserDetailsService implemented
- [ ] JWT secret configured in application.yml
- [ ] Service compiles without errors (`./mvnw clean compile`)
- [ ] Swagger UI accessible and shows "Authorize" button
- [ ] Protected endpoints return 401 without token
- [ ] Protected endpoints return 200 with valid token

---

## Common Mistakes

### 1. Wrong OpenAPI Server URL
**❌ Wrong**:
```java
server.setUrl("http://localhost:8082");  // Direct service port
```

**✅ Correct**:
```java
server.setUrl("http://localhost:8080");  // API Gateway
```

**Symptom**: CORS errors in Swagger UI when calling APIs

---

### 2. Missing UserDetailsService
**❌ Error**:
```
Parameter 1 of constructor in com.sms.student.security.JwtAuthenticationFilter
required a bean of type 'org.springframework.security.core.userdetails.UserDetailsService'
that could not be found.
```

**✅ Fix**: Implement UserDetailsService (see example above)

---

### 3. JWT Secret Too Short
**❌ Error**:
```
The specified key byte array is 128 bits which is not secure enough for any JWT HMAC-SHA algorithm.
```

**✅ Fix**: Use at least 32 characters for JWT secret
```yaml
jwt:
  secret: ${JWT_SECRET:this-is-a-256-bit-secret-key-for-development-only-change-in-prod}
```

---

### 4. Forgetting to Replace Package Import
**❌ Wrong**:
```java
package com.sms.student.config;

import com.sms.SERVICENAME.security.JwtAuthenticationFilter;  // Still has SERVICENAME
```

**✅ Correct**:
```java
package com.sms.student.config;

import com.sms.student.security.JwtAuthenticationFilter;  // Updated
```

---

### 5. Wrong Package for JWT Classes
**❌ Wrong Structure**:
```
com.sms.student/
├── config/
│   ├── JwtAuthenticationFilter.java  ❌ Wrong location
│   └── JwtTokenProvider.java          ❌ Wrong location
```

**✅ Correct Structure**:
```
com.sms.student/
├── config/
│   └── SecurityConfig.java
├── security/
│   ├── JwtAuthenticationFilter.java  ✅ Correct
│   └── JwtTokenProvider.java          ✅ Correct
```

---

## Related Documentation

- **Configuration Standards**: See `.standards/docs/environment-variables.md` for JWT_SECRET configuration
- **Security Architecture**: See `specs/002-jwt-auth/spec.md` for JWT implementation details
- **CORS Setup**: See `.standards/docs/cors-setup.md` for advanced CORS configuration
- **OpenAPI Setup**: See `.standards/docs/openapi-setup.md` for Swagger customization
- **Service Template**: See `auth-service/` for complete working example

---

## Quick Reference: Template Customization Matrix

| Template | Package Rename | Imports | Metadata | Logic | Config File |
|----------|---------------|---------|----------|-------|-------------|
| CorsConfig | ✅ | ❌ | ❌ | Optional* | ❌ |
| OpenAPIConfig | ✅ | ❌ | ✅ (title/desc) | ❌ | ❌ |
| SecurityConfig | ✅ | ✅ (Filter) | ❌ | ✅ (endpoints) | ❌ |
| JwtAuthenticationFilter | ✅ | ❌ | ❌ | ❌ | ❌ |
| JwtTokenProvider | ✅ | ❌ | ❌ | Optional** | ✅ (JWT secret) |

\* CORS logic customization only needed for production (restrict origins)
\** JwtTokenProvider logic customization only if you need custom claims

**Legend**:
- ✅ = Required customization
- Optional = May customize based on requirements
- ❌ = No customization needed

---

## Support

**Questions or issues?**
1. Check `auth-service/` for working examples
2. Review `.standards/validation-reports/auth-service-config-verification.md` for compliance verification
3. Consult `SERVICE_COMPARISON_ANALYSIS.md` for architecture standards
4. Run `.standards/scripts/smoke-test-deployment.sh` to verify service configuration

**Template Updates**:
If you find bugs or improvements needed in templates, update:
1. The template in `.standards/templates/java/`
2. The corresponding section in this documentation
3. The reference implementation in `auth-service/`
