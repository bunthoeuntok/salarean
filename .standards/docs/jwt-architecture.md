# JWT Architecture Standards

**Version**: 1.0.0
**Last Updated**: 2025-11-22
**Compliance**: MANDATORY for all microservices

---

## Overview

All microservices MUST implement JWT (JSON Web Token) authentication using a **two-class architecture**:

1. **JwtAuthenticationFilter** - HTTP request handling and security context management
2. **JwtTokenProvider** - Pure token operations (generation, parsing, validation)

This separation ensures:
- ✅ **Clear Responsibilities**: Filter handles HTTP, Provider handles JWT logic
- ✅ **Testability**: Provider can be unit tested independently
- ✅ **Reusability**: Provider can be used outside filter context
- ✅ **Maintainability**: Changes to JWT logic don't affect filter logic

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [JwtAuthenticationFilter](#jwtauthenticationfilter)
3. [JwtTokenProvider](#jwttokenprovider)
4. [Integration with Spring Security](#integration-with-spring-security)
5. [Configuration](#configuration)
6. [Usage Patterns](#usage-patterns)
7. [Testing](#testing)

---

## Architecture Overview

### Two-Class Separation

**Rule**: JWT logic MUST be split into exactly two classes in the `security/` package.

```
com.sms.{service}/
└── security/
    ├── JwtAuthenticationFilter.java    # HTTP request handling
    └── JwtTokenProvider.java           # Token operations
```

**Critical Rules**:
- ✅ Both classes MUST be in `security/` package (NOT `config/`)
- ✅ Filter MUST extend `OncePerRequestFilter`
- ✅ Provider MUST be annotated with `@Component`
- ✅ Filter MUST delegate all JWT operations to Provider

**Forbidden Patterns**:
```
❌ config/JwtAuthenticationFilter.java    (wrong package)
❌ config/JwtUtils.java                   (wrong package, wrong name)
❌ security/JwtFilter.java                (combined logic, missing provider)
```

### Request Flow

```
HTTP Request with Authorization Header
         |
         v
┌────────────────────────────────┐
│  JwtAuthenticationFilter       │  1. Extract JWT from header
│  (security/)                    │  2. Validate token (delegate to provider)
│                                 │  3. Load user details
│                                 │  4. Set security context
└─────────────┬──────────────────┘
              |
              v (delegates)
┌────────────────────────────────┐
│  JwtTokenProvider               │  1. Validate token signature
│  (security/)                    │  2. Check expiration
│                                 │  3. Extract claims (userId, roles, etc.)
│                                 │  4. Return parsed data
└────────────────────────────────┘
              |
              v
┌────────────────────────────────┐
│  SecurityContext                │  Authentication set
│  (Spring Security)              │  User authorized
└────────────────────────────────┘
```

---

## JwtAuthenticationFilter

### Purpose

**JwtAuthenticationFilter** intercepts HTTP requests to:
1. Extract JWT from `Authorization` header
2. Validate the token (delegate to `JwtTokenProvider`)
3. Load user details from database
4. Set Spring Security authentication context

### Standard Implementation

**File**: `src/main/java/com/sms/{service}/security/JwtAuthenticationFilter.java`

```java
package com.sms.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                java.util.UUID userId = tokenProvider.getUserIdFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### Implementation Breakdown

**1. Class Declaration**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
```

- `@Component`: Registers as Spring bean (auto-detected by component scan)
- `OncePerRequestFilter`: Ensures filter executes exactly once per request

**2. Dependencies**
```java
private final JwtTokenProvider tokenProvider;
private final UserDetailsService userDetailsService;

public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
    this.tokenProvider = tokenProvider;
    this.userDetailsService = userDetailsService;
}
```

**Constructor Injection**:
- `JwtTokenProvider`: For token validation and parsing
- `UserDetailsService`: For loading user from database

**3. Main Filter Logic**
```java
@Override
protected void doFilterInternal(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull FilterChain filterChain) throws ServletException, IOException {
```

**Method Signature**:
- `@NonNull`: Parameters cannot be null
- Returns void
- Throws `ServletException`, `IOException`

**Filter Flow**:

```java
try {
    // Step 1: Extract JWT from request
    String jwt = getJwtFromRequest(request);

    // Step 2: Validate token (delegate to provider)
    if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
        // Step 3: Extract user ID from token (delegate to provider)
        java.util.UUID userId = tokenProvider.getUserIdFromToken(jwt);

        // Step 4: Load user from database
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());

        // Step 5: Create authentication object
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Step 6: Set security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
} catch (Exception ex) {
    logger.error("Could not set user authentication in security context", ex);
}

// Step 7: Always continue filter chain
filterChain.doFilter(request, response);
```

**Critical**: `filterChain.doFilter()` MUST be called even if authentication fails, allowing public endpoints to work.

**4. JWT Extraction**
```java
private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
        return bearerToken.substring(7);  // Remove "Bearer " prefix
    }
    return null;
}
```

**Expected Header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Returns**: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` (without "Bearer " prefix)

### Filter Responsibilities

**What the Filter DOES**:
- ✅ Extract JWT from HTTP headers
- ✅ Call `tokenProvider.validateToken()` for validation
- ✅ Call `tokenProvider.getUserIdFromToken()` for claims extraction
- ✅ Load user details from database
- ✅ Set Spring Security authentication context
- ✅ Handle exceptions gracefully

**What the Filter DOES NOT DO**:
- ❌ Token validation logic (delegated to provider)
- ❌ Token parsing (delegated to provider)
- ❌ Claims extraction (delegated to provider)
- ❌ Token generation (handled by provider in service layer)

---

## JwtTokenProvider

### Purpose

**JwtTokenProvider** handles all JWT operations:
1. Token generation with claims
2. Token signature verification
3. Token expiration checking
4. Claims extraction (user ID, roles, etc.)

### Standard Implementation

**File**: `src/main/java/com/sms/{service}/security/JwtTokenProvider.java`

```java
package com.sms.auth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final long EXPIRATION_MS = 86400000; // 24 hours

    public String generateToken(UUID userId, String language) {
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
            .claim("roles", new String[]{"TEACHER"})
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public String getJtiFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getId();
    }

    public UUID getUserIdFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String subject = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
        return UUID.fromString(subject);
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Note: Refresh tokens are NOT JWTs - they're UUID-based tokens stored in the database
    // Refresh token generation and management is handled by TokenService
}
```

### Implementation Breakdown

**1. Class Declaration**
```java
@Component
public class JwtTokenProvider {
```

- `@Component`: Registers as Spring bean
- No extension required (pure utility class)

**2. Configuration**
```java
@Value("${jwt.secret}")
private String jwtSecret;

private static final long EXPIRATION_MS = 86400000; // 24 hours
```

**JWT Secret**:
- Injected from `application.yml`: `jwt.secret: ${JWT_SECRET:defaultSecretForDevelopment}`
- Used to sign and verify tokens

**Expiration**:
- 86400000 ms = 24 hours
- Can be made configurable via `@Value("${jwt.expiration-ms}")`

**3. Token Generation**
```java
public String generateToken(UUID userId, String language) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + EXPIRATION_MS);
    String jti = UUID.randomUUID().toString();

    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

    return Jwts.builder()
        .setSubject(userId.toString())           // User ID in "sub" claim
        .setId(jti)                              // JWT ID (unique identifier)
        .setIssuedAt(now)                        // Issued at timestamp
        .setExpiration(expiryDate)               // Expiration timestamp
        .claim("lang", language)                 // Custom claim: user language
        .claim("roles", new String[]{"TEACHER"}) // Custom claim: user roles
        .signWith(key, SignatureAlgorithm.HS256) // Sign with HMAC-SHA256
        .compact();                              // Build compact JWT string
}
```

**Generated JWT Structure**:
```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",  // User ID
  "jti": "7c9e6679-7425-40de-944b-e07fc1f90ae7",  // JWT ID
  "iat": 1700000000,                               // Issued at (Unix timestamp)
  "exp": 1700086400,                               // Expiration (Unix timestamp)
  "lang": "en",                                    // Language preference
  "roles": ["TEACHER"]                             // User roles
}
```

**4. Token Validation**
```java
public boolean validateToken(String token) {
    try {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Jwts.parser()
            .verifyWith(key)       // Verify signature
            .build()
            .parseSignedClaims(token); // Parse and validate
        return true;
    } catch (Exception e) {
        return false;              // Invalid token
    }
}
```

**Validation Checks** (automatic via JJWT library):
- ✅ Signature verification (token not tampered with)
- ✅ Expiration check (token not expired)
- ✅ Format check (valid JWT structure)

**Returns**:
- `true`: Token is valid
- `false`: Token is invalid (expired, tampered, malformed)

**5. Claims Extraction**
```java
public UUID getUserIdFromToken(String token) {
    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    String subject = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();              // Extract "sub" claim
    return UUID.fromString(subject);
}
```

**Additional Extractors** (examples):
```java
public String getJtiFromToken(String token) {
    // Extract JWT ID ("jti" claim)
}

public String getLanguageFromToken(String token) {
    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("lang", String.class);  // Extract custom claim
}

public String[] getRolesFromToken(String token) {
    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("roles", String[].class);  // Extract roles array
}
```

### Provider Responsibilities

**What the Provider DOES**:
- ✅ Generate JWT with claims
- ✅ Validate JWT signature
- ✅ Check token expiration
- ✅ Extract claims (user ID, roles, custom claims)
- ✅ Handle JWT parsing exceptions

**What the Provider DOES NOT DO**:
- ❌ HTTP request handling (handled by filter)
- ❌ Load user details from database (handled by filter)
- ❌ Set security context (handled by filter)
- ❌ Manage refresh tokens (handled by service layer)

---

## Integration with Spring Security

### SecurityConfig Integration

**File**: `src/main/java/com/sms/{service}/config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
```

**Key Points**:
- ✅ Inject `JwtAuthenticationFilter` via constructor
- ✅ Add filter BEFORE `UsernamePasswordAuthenticationFilter`
- ✅ Use stateless session management (no HTTP sessions)

### Service Layer Integration

**Example**: Login endpoint generates JWT

```java
@Service
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(JwtTokenProvider tokenProvider,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public JwtResponse login(LoginRequest request) {
        // Step 1: Find user
        User user = userRepository.findByEmailOrPhone(request.getIdentifier())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        // Step 2: Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Step 3: Generate JWT (delegate to provider)
        String accessToken = tokenProvider.generateToken(user.getId(), user.getLanguage());

        // Step 4: Return response
        return new JwtResponse(accessToken, refreshToken);
    }
}
```

**Pattern**:
- ✅ Service layer calls `tokenProvider.generateToken()`
- ✅ Provider handles all JWT logic
- ✅ Service handles business logic (user lookup, password verification)

---

## Configuration

### Application Configuration

**File**: `src/main/resources/application.yml`

```yaml
jwt:
  secret: ${JWT_SECRET:defaultSecretForDevelopmentOnlyPleaseChangeInProduction}
  expiration-ms: ${JWT_EXPIRATION_MS:86400000}  # 24 hours
```

**Environment Variables** (docker-compose.yml):
```yaml
environment:
  - JWT_SECRET=${JWT_SECRET}
  - JWT_EXPIRATION_MS=${JWT_EXPIRATION_MS:86400000}
```

**Production Best Practices**:
1. ✅ Use strong secret (256-bit minimum for HS256)
2. ✅ Never commit secrets to version control
3. ✅ Use environment variables or secret management systems
4. ✅ Rotate secrets periodically

**Secret Generation**:
```bash
# Generate strong JWT secret (256-bit)
openssl rand -base64 32
```

### Maven Dependencies

**File**: `pom.xml`

```xml
<dependencies>
    <!-- JWT Support -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.5</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

**JJWT Version**: 0.12.5 (latest stable as of Spring Boot 3.5.7)

---

## Usage Patterns

### Pattern 1: Protected Endpoint

**Controller**:
```java
@RestController
@RequestMapping("/api/students")
public class StudentController {

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable Long id) {
        // JWT authentication is automatic
        // User is already authenticated by JwtAuthenticationFilter
        // Access SecurityContext to get user details if needed
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();  // User ID from JWT

        // ... business logic
    }
}
```

**No JWT code needed in controller** - filter handles authentication automatically.

### Pattern 2: Public Endpoint

**Controller**:
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        // Public endpoint - no JWT required
        // SecurityConfig marks this endpoint as .permitAll()

        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
```

**SecurityConfig**:
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/login").permitAll()  // Public endpoint
    .anyRequest().authenticated()                     // All others protected
)
```

### Pattern 3: Accessing Current User

**Controller**:
```java
@GetMapping("/me")
public ResponseEntity<?> getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) auth.getPrincipal();
    String userId = userDetails.getUsername();  // User ID

    // ... fetch user data
}
```

**Or use `@AuthenticationPrincipal`**:
```java
@GetMapping("/me")
public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
    String userId = userDetails.getUsername();
    // ... fetch user data
}
```

### Pattern 4: Role-Based Authorization

**Method-Level**:
```java
@Service
public class StudentServiceImpl implements StudentService {

    @PreAuthorize("hasRole('TEACHER')")
    public Student createStudent(CreateStudentRequest request) {
        // Only users with TEACHER role can call this
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public void deleteStudent(Long id) {
        // Only TEACHER or ADMIN can call this
    }
}
```

**Endpoint-Level**:
```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")  // All endpoints require ADMIN role
public class AdminController {
    // ...
}
```

---

## Testing

### Unit Testing JwtTokenProvider

```java
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", "testSecretKeyForUnitTestingPurposesOnly1234567890");
    }

    @Test
    void generateToken_ShouldReturnValidJwt() {
        UUID userId = UUID.randomUUID();
        String language = "en";

        String token = tokenProvider.generateToken(userId, language);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(userId, tokenProvider.getUserIdFromToken(token));
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Create token with negative expiration
        // ... test implementation
    }

    @Test
    void getUserIdFromToken_WithValidToken_ShouldExtractUserId() {
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateToken(userId, "en");

        UUID extractedId = tokenProvider.getUserIdFromToken(token);

        assertEquals(userId, extractedId);
    }
}
```

### Integration Testing Filter

```java
@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Test
    void accessProtectedEndpoint_WithValidToken_ShouldSucceed() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateToken(userId, "en");

        mockMvc.perform(get("/api/students/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void accessProtectedEndpoint_WithoutToken_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessPublicEndpoint_WithoutToken_ShouldSucceed() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"test@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk());
    }
}
```

---

## Quick Reference

### JWT Architecture Checklist

When creating JWT authentication for a service:

**Package Structure**:
- [ ] `security/JwtAuthenticationFilter.java` exists
- [ ] `security/JwtTokenProvider.java` exists
- [ ] Both classes in `security/` package (NOT `config/`)

**Filter Implementation**:
- [ ] Extends `OncePerRequestFilter`
- [ ] Annotated with `@Component`
- [ ] Injects `JwtTokenProvider` and `UserDetailsService`
- [ ] Delegates validation to provider
- [ ] Delegates claims extraction to provider
- [ ] Sets `SecurityContext` authentication
- [ ] Always calls `filterChain.doFilter()`

**Provider Implementation**:
- [ ] Annotated with `@Component`
- [ ] Injects `jwt.secret` from configuration
- [ ] Implements `generateToken()`
- [ ] Implements `validateToken()`
- [ ] Implements `getUserIdFromToken()`
- [ ] Uses JJWT library 0.12.5+
- [ ] Uses HMAC-SHA256 algorithm

**Configuration**:
- [ ] `application.yml` has `jwt.secret` property
- [ ] Secret uses environment variable (`${JWT_SECRET}`)
- [ ] `SecurityConfig` adds filter before `UsernamePasswordAuthenticationFilter`
- [ ] Maven dependencies include JJWT 0.12.5

---

## Common Mistakes

| Issue | Incorrect | Correct |
|-------|-----------|---------|
| Package location | `config/JwtAuthenticationFilter.java` | `security/JwtAuthenticationFilter.java` |
| Combined logic | Single `JwtUtils.java` class | Separate Filter + Provider |
| Filter naming | `JwtFilter.java` | `JwtAuthenticationFilter.java` |
| Provider naming | `JwtUtils.java` | `JwtTokenProvider.java` |
| Token validation | Filter contains validation logic | Filter delegates to provider |
| Claims extraction | Filter parses JWT directly | Filter calls provider methods |
| Filter chain | Doesn't call `filterChain.doFilter()` | Always calls it |
| Secret management | Hardcoded in code | Environment variable |

---

## Related Documentation

- **Package Structure**: `.standards/docs/package-structure.md`
- **Naming Conventions**: `.standards/docs/naming-conventions.md`
- **Configuration Patterns**: `.standards/docs/configuration-patterns.md`
- **Security Config**: `.standards/docs/configuration-patterns.md#securityconfig-pattern`

---

## Version History

| Version | Date       | Changes                     |
|---------|------------|-----------------------------|
| 1.0.0   | 2025-11-22 | Initial JWT architecture    |

---

## Support

For questions about JWT architecture:

1. Review this document for pattern details
2. Check auth-service as reference implementation (auth-service/src/main/java/com/sms/auth/security/)
3. Run validation script to verify JWT classes in correct package
4. Test with unit and integration tests
