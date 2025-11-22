# CORS Setup Guide

## What is CORS?

**Cross-Origin Resource Sharing (CORS)** is a security mechanism that allows web browsers to make requests from one domain (origin) to another. Without CORS configuration, browsers block these requests for security reasons.

### Why Do We Need CORS?

**Scenario**: Your frontend runs on `http://localhost:3000` (React dev server) and tries to call your backend API at `http://localhost:8080` (API Gateway).

Without CORS:
```
❌ Access to fetch at 'http://localhost:8080/api/students' from origin 'http://localhost:3000'
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

With CORS properly configured:
```
✅ Request succeeds, data returned to frontend
```

## When to Configure CORS

**Every Spring Boot service** in our microservice architecture MUST include CORS configuration because:

1. **Frontend applications** (React, Vue, Angular) run on different ports during development
2. **Mobile apps** make HTTP requests from different origins
3. **Swagger UI** needs to call APIs from the documentation interface
4. **Third-party integrations** may access your APIs from external domains

## Quick Setup (5 Minutes)

### Step 1: Copy the Template

```bash
# Replace {service} with your service name (e.g., student, attendance, grade)
cp .standards/templates/java/CorsConfig.java \
   {service}-service/src/main/java/com/sms/{service}/config/CorsConfig.java
```

**Example for student-service**:
```bash
cp .standards/templates/java/CorsConfig.java \
   student-service/src/main/java/com/sms/student/config/CorsConfig.java
```

### Step 2: Rename Package

Open the file and change the package name:

```java
// Before
package com.sms.SERVICENAME.config;

// After (for student-service)
package com.sms.student.config;
```

### Step 3: Verify It Works

Start your service and check the response headers:

```bash
curl -I -X OPTIONS http://localhost:8082/api/students \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET"
```

**Expected response headers**:
```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
Access-Control-Allow-Headers: *
Access-Control-Max-Age: 3600
```

If you see these headers, CORS is configured correctly.

## Configuration Details

### Default Configuration (Development)

The template provides a permissive CORS configuration suitable for development:

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow all origins (development only)
        configuration.setAllowedOrigins(List.of("*"));

        // Allow all common HTTP methods
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));

        // Don't allow credentials (cookies, authorization headers) with wildcard origin
        configuration.setAllowCredentials(false);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        // Apply to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### Integration with Security

CORS configuration automatically integrates with Spring Security through `SecurityConfig.java`:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            // CORS is applied first
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // Then other security rules
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // ...
            .build();
}
```

**Order matters**: CORS must be configured before CSRF and other security filters.

## Customization Scenarios

### Scenario 1: Production Deployment

**Problem**: Wildcard `*` origin is insecure in production.

**Solution**: Specify exact allowed origins.

```java
configuration.setAllowedOrigins(List.of(
    "https://yourdomain.com",           // Production frontend
    "https://admin.yourdomain.com",     // Admin panel
    "https://mobile.yourdomain.com"     // Mobile app API
));
```

**With credentials** (cookies, JWT in Authorization header):
```java
configuration.setAllowedOrigins(List.of("https://yourdomain.com"));
configuration.setAllowCredentials(true);  // Now safe with specific origin
```

**Environment-based configuration**:
```java
@Value("${cors.allowed.origins}")
private String allowedOrigins;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Split comma-separated origins from application.yml
    configuration.setAllowedOrigins(
        Arrays.asList(allowedOrigins.split(","))
    );
    // ...
}
```

**application.yml**:
```yaml
cors:
  allowed:
    origins: ${ALLOWED_ORIGINS:http://localhost:3000,http://localhost:4200}
```

**application-docker.yml**:
```yaml
cors:
  allowed:
    origins: ${ALLOWED_ORIGINS}  # Set in docker-compose.yml
```

### Scenario 2: Restrict HTTP Methods

**Problem**: You only have GET and POST endpoints, but CORS allows DELETE.

**Solution**: Specify only needed methods.

```java
configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
```

**Always include OPTIONS**: Browsers send preflight OPTIONS requests before actual requests.

### Scenario 3: Custom Headers

**Problem**: Your frontend sends custom headers (e.g., `X-Tenant-ID`, `X-Request-ID`).

**Solution**: Explicitly allow them.

```java
configuration.setAllowedHeaders(List.of(
    "Authorization",
    "Content-Type",
    "X-Tenant-ID",
    "X-Request-ID"
));

// Or allow all (development)
configuration.setAllowedHeaders(List.of("*"));
```

### Scenario 4: Expose Response Headers

**Problem**: Frontend needs to read custom response headers (e.g., `X-Total-Count` for pagination).

**Solution**: Use `setExposedHeaders()`.

```java
configuration.setExposedHeaders(List.of(
    "X-Total-Count",
    "X-Page-Number",
    "X-Page-Size"
));
```

**By default**, only these headers are exposed:
- Cache-Control
- Content-Language
- Content-Type
- Expires
- Last-Modified
- Pragma

### Scenario 5: Different CORS Rules for Different Endpoints

**Problem**: Public endpoints should allow all origins, but admin endpoints should be restricted.

**Solution**: Register multiple CORS configurations.

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    // Public endpoints - allow all
    CorsConfiguration publicConfig = new CorsConfiguration();
    publicConfig.setAllowedOrigins(List.of("*"));
    publicConfig.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
    publicConfig.setAllowedHeaders(List.of("*"));
    source.registerCorsConfiguration("/api/public/**", publicConfig);

    // Admin endpoints - restricted
    CorsConfiguration adminConfig = new CorsConfiguration();
    adminConfig.setAllowedOrigins(List.of("https://admin.yourdomain.com"));
    adminConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    adminConfig.setAllowedHeaders(List.of("*"));
    adminConfig.setAllowCredentials(true);
    source.registerCorsConfiguration("/api/admin/**", adminConfig);

    // Default for all other endpoints
    CorsConfiguration defaultConfig = new CorsConfiguration();
    defaultConfig.setAllowedOrigins(List.of("https://yourdomain.com"));
    defaultConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    defaultConfig.setAllowedHeaders(List.of("*"));
    source.registerCorsConfiguration("/**", defaultConfig);

    return source;
}
```

## Testing CORS

### Method 1: Browser DevTools

1. Open your frontend application in Chrome/Firefox
2. Open DevTools (F12) → Network tab
3. Make an API request
4. Check the request headers:
   - Look for `Origin: http://localhost:3000`
5. Check the response headers:
   - Look for `Access-Control-Allow-Origin: *`

**If CORS fails**, you'll see:
```
Access to fetch at 'http://localhost:8080/api/students' from origin 'http://localhost:3000'
has been blocked by CORS policy
```

### Method 2: curl Command

**Preflight request (OPTIONS)**:
```bash
curl -X OPTIONS http://localhost:8082/api/students \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type, Authorization" \
  -v
```

**Look for these response headers**:
```
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
< Access-Control-Allow-Headers: Content-Type, Authorization
< Access-Control-Max-Age: 3600
```

**Actual request (GET)**:
```bash
curl -X GET http://localhost:8082/api/students \
  -H "Origin: http://localhost:3000" \
  -v
```

**Look for**:
```
< Access-Control-Allow-Origin: *
```

### Method 3: Automated Test

Create an integration test:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCorsConfiguration() throws Exception {
        mockMvc.perform(options("/api/students")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(header().string("Access-Control-Allow-Methods",
                    containsString("POST")));
    }

    @Test
    public void testActualRequestWithCors() throws Exception {
        mockMvc.perform(get("/api/students")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())  // Or 401 if auth required
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }
}
```

## Common Issues and Solutions

### Issue 1: CORS Still Blocked After Configuration

**Symptoms**:
- Configuration looks correct
- Still seeing CORS errors in browser

**Possible Causes**:

1. **CorsConfig not being picked up**
   - Check package scanning: `@SpringBootApplication(scanBasePackages = "com.sms.student")`
   - Verify `@Configuration` annotation is present
   - Check Spring logs for "CorsConfig" bean creation

2. **Security filter ordering**
   - Ensure `.cors()` comes before `.csrf()` in SecurityConfig
   ```java
   http
       .cors(cors -> cors.configurationSource(corsConfigurationSource))  // First
       .csrf(AbstractHttpConfigurer::disable)  // Then this
   ```

3. **API Gateway overriding CORS**
   - If using Spring Cloud Gateway, configure CORS there too
   - Gateway CORS takes precedence over service CORS

4. **Nginx/Reverse proxy adding conflicting headers**
   - Check if proxy is adding its own CORS headers
   - May need to disable service CORS and handle it at proxy level

### Issue 2: Credentials Not Allowed with Wildcard Origin

**Error**:
```
Access to fetch at 'http://localhost:8080/api/students' has been blocked by CORS policy:
The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*'
when the request's credentials mode is 'include'.
```

**Cause**: Frontend is sending cookies or Authorization headers, but CORS allows wildcard origin.

**Solution**:
```java
// Change from wildcard
configuration.setAllowedOrigins(List.of("*"));
configuration.setAllowCredentials(false);

// To specific origin
configuration.setAllowedOrigins(List.of("http://localhost:3000"));
configuration.setAllowCredentials(true);
```

### Issue 3: Preflight Request Returns 403 Forbidden

**Symptoms**:
- OPTIONS request fails with 403
- Actual request never sent

**Cause**: SecurityConfig requires authentication for OPTIONS requests.

**Solution**: Allow OPTIONS for all endpoints:
```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // Add this
        .requestMatchers("/api/auth/login").permitAll()
        .anyRequest().authenticated()
)
```

### Issue 4: Custom Headers Not Allowed

**Error**:
```
Request header field X-Tenant-ID is not allowed by Access-Control-Allow-Headers in preflight response.
```

**Solution**: Add custom headers to allowed list:
```java
configuration.setAllowedHeaders(List.of(
    "*",  // Or list specific headers
    "X-Tenant-ID",
    "X-Request-ID"
));
```

## Production Checklist

Before deploying to production, verify:

- [ ] Replaced wildcard `*` origin with specific domain(s)
- [ ] Removed unnecessary HTTP methods from allowedMethods
- [ ] Set `allowCredentials` appropriately (true if using cookies/JWT)
- [ ] Limited allowedHeaders to only what's needed (or keep `*` if dynamic)
- [ ] Configured CORS in API Gateway if using one
- [ ] Tested with actual production frontend URL
- [ ] Verified preflight requests complete successfully
- [ ] Checked that custom headers are exposed if needed
- [ ] Reviewed CORS configuration in nginx/reverse proxy (if applicable)
- [ ] Added CORS configuration to environment variables for flexibility

## Environment-Specific Configuration Example

**CorsConfig.java**:
```java
@Configuration
public class CorsConfig {

    @Value("${cors.allowed.origins}")
    private String allowedOrigins;

    @Value("${cors.allowed.credentials:false}")
    private boolean allowCredentials;

    @Value("${cors.max.age:3600}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
            Arrays.asList(allowedOrigins.split(","))
        );
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

**application.yml** (local development):
```yaml
cors:
  allowed:
    origins: http://localhost:3000,http://localhost:4200
  allowed:
    credentials: false
  max:
    age: 3600
```

**application-docker.yml** (Docker environment):
```yaml
cors:
  allowed:
    origins: ${ALLOWED_ORIGINS}
  allowed:
    credentials: ${ALLOWED_CREDENTIALS:false}
  max:
    age: ${CORS_MAX_AGE:3600}
```

**docker-compose.yml**:
```yaml
environment:
  - ALLOWED_ORIGINS=https://yourdomain.com,https://admin.yourdomain.com
  - ALLOWED_CREDENTIALS=true
  - CORS_MAX_AGE=7200
```

## API Gateway CORS (Spring Cloud Gateway)

If you're using Spring Cloud Gateway, you may need CORS configuration there too:

**application.yml** (Gateway):
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins:
              - "http://localhost:3000"
              - "https://yourdomain.com"
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowed-headers:
              - "*"
            allow-credentials: true
            max-age: 3600
```

**Decision**: Configure CORS in **one place only**:
- **Services only**: If no API Gateway
- **Gateway only**: If Gateway handles all external traffic (recommended)
- **Both**: Only if services are accessed directly AND through gateway

## Related Documentation

- **Security Integration**: See `.standards/docs/reusable-components.md` for SecurityConfig template
- **OpenAPI Integration**: See `.standards/docs/openapi-setup.md` for CORS with Swagger
- **Template Reference**: See `.standards/templates/java/CorsConfig.java`
- **Production Example**: See `auth-service/src/main/java/com/sms/auth/config/CorsConfig.java`

## Further Reading

- [Spring Boot CORS Official Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.servlet.spring-mvc.cors)
- [MDN Web Docs: CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
- [Spring Security CORS](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)

## Quick Troubleshooting Commands

```bash
# Test OPTIONS preflight
curl -X OPTIONS http://localhost:8082/api/students \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -v 2>&1 | grep -i "access-control"

# Test actual GET request
curl -X GET http://localhost:8082/api/students \
  -H "Origin: http://localhost:3000" \
  -v 2>&1 | grep -i "access-control"

# Check if CorsConfig bean exists
curl http://localhost:8082/actuator/beans | jq '.contexts[].beans | keys[]' | grep -i cors

# View all CORS-related logs
./mvnw spring-boot:run | grep -i cors
```
