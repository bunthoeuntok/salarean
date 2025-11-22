# OpenAPI/Swagger Setup Guide

## What is OpenAPI/Swagger?

**OpenAPI** (formerly Swagger) is a specification for documenting REST APIs. **Swagger UI** provides an interactive web interface where developers can:

- View all API endpoints and their parameters
- Test endpoints directly in the browser
- See request/response examples
- Authenticate with JWT tokens
- Generate API client code

### Example

After setup, visiting `http://localhost:8082/swagger-ui.html` shows:

```
Student Service API

GET /api/students
  Summary: Get all students
  Parameters: page, size
  Responses: 200 OK, 401 Unauthorized

POST /api/students
  Summary: Create new student
  Request Body: StudentCreateRequest
  Responses: 201 Created, 400 Bad Request, 401 Unauthorized

[Authorize] button - Click to enter JWT token
```

## Why We Need OpenAPI Configuration

**Without OpenAPI configuration**:
- No API documentation
- Manual testing with curl/Postman only
- New developers spend hours understanding endpoints
- CORS errors when testing from Swagger UI

**With OpenAPI configuration**:
- Automatic interactive documentation
- Test endpoints with one click
- JWT authentication built-in
- API Gateway integration (no CORS errors)

## Quick Setup (10 Minutes)

### Step 1: Add Dependencies

**For Maven** (pom.xml):

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**For Gradle** (build.gradle):

```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
```

**Note**: Springdoc v2.x is for Spring Boot 3.x. For Spring Boot 2.x, use v1.x.

### Step 2: Copy the Template

```bash
# Replace {service} with your service name (e.g., student, attendance, grade)
cp .standards/templates/java/OpenAPIConfig.java \
   {service}-service/src/main/java/com/sms/{service}/config/OpenAPIConfig.java
```

**Example for student-service**:
```bash
cp .standards/templates/java/OpenAPIConfig.java \
   student-service/src/main/java/com/sms/student/config/OpenAPIConfig.java
```

### Step 3: Customize the Configuration

Open the file and update:

```java
package com.sms.student.config;  // 1. Update package name

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI studentAPI() {  // 2. Rename method (servicename → student)
        Server server = new Server();
        server.setUrl("http://localhost:8080");  // 3. DO NOT CHANGE - API Gateway URL
        server.setDescription("API Gateway");

        return new OpenAPI()
                .servers(List.of(server))
                .info(new Info()
                        .title("Student Service API")  // 4. Update title
                        .description("Manages student profiles, enrollment, and academic records")  // 5. Update description
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, createSecurityScheme()))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    // No changes needed below this line
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

### Step 4: Allow Swagger Endpoints in Security

Update `SecurityConfig.java` to allow unauthenticated access to Swagger:

```java
.authorizeHttpRequests(auth -> auth
        // Service-specific public endpoints
        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()

        // Swagger/OpenAPI endpoints (REQUIRED)
        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
        .requestMatchers("/actuator/**").permitAll()

        .anyRequest().authenticated()
)
```

### Step 5: Test Swagger UI

1. Start your service:
   ```bash
   ./mvnw spring-boot:run
   ```

2. Open Swagger UI in browser:
   ```
   http://localhost:{service-port}/swagger-ui.html
   ```

3. You should see:
   - Service title and description
   - List of all endpoints
   - "Authorize" button in top-right
   - Try-it-out buttons for each endpoint

### Step 6: Test JWT Authentication

1. Click the **Authorize** button
2. Enter a JWT token from auth-service:
   ```
   eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```
   (Get this by logging in via `/api/auth/login`)

3. Click **Authorize**
4. Click **Close**
5. Try any protected endpoint - it should work with the token

## Configuration Explained

### Server Configuration (CRITICAL)

```java
Server server = new Server();
server.setUrl("http://localhost:8080");  // API Gateway port
server.setDescription("API Gateway");
```

**Why API Gateway URL?**

All API requests from Swagger UI go through the API Gateway, not directly to the service:

```
Swagger UI → http://localhost:8080/api/students → API Gateway → Student Service
```

**If you use the service's direct port** (e.g., `http://localhost:8082`):

```
Swagger UI → http://localhost:8082/api/students → Student Service (direct)
❌ CORS error: Origin http://localhost:8082 blocked
```

**Best Practice**: Always use API Gateway URL (`http://localhost:8080`) in Swagger server configuration.

### API Metadata

```java
.info(new Info()
    .title("Student Service API")
    .description("Manages student profiles, enrollment, and academic records")
    .version("1.0.0"))
```

**Customization Guidelines**:

- **Title**: `{Service Name} API` (e.g., "Authentication Service API", "Grade Service API")
- **Description**: Brief summary of what the service does
- **Version**: Use semantic versioning (e.g., "1.0.0", "2.1.0")

**Optional metadata** you can add:

```java
.info(new Info()
    .title("Student Service API")
    .description("Manages student profiles, enrollment, and academic records")
    .version("1.0.0")
    .contact(new Contact()
        .name("SMS Development Team")
        .email("dev@sms.com")
        .url("https://github.com/yourorg/sms"))
    .license(new License()
        .name("MIT License")
        .url("https://opensource.org/licenses/MIT")))
```

### JWT Security Configuration

```java
.components(new Components()
    .addSecuritySchemes(SECURITY_SCHEME_NAME, createSecurityScheme()))
.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
```

This configuration:
1. Defines "Bearer Authentication" security scheme
2. Applies it globally to all endpoints
3. Shows "Authorize" button in Swagger UI
4. Adds `Authorization: Bearer {token}` header to all requests

**Security Scheme Details**:

```java
private SecurityScheme createSecurityScheme() {
    return new SecurityScheme()
            .name("Bearer Authentication")
            .type(SecurityScheme.Type.HTTP)  // HTTP authentication
            .scheme("bearer")                  // Bearer token
            .bearerFormat("JWT")               // Token format is JWT
            .description("Enter JWT token obtained from /api/auth/login or /api/auth/register");
}
```

## Advanced Customization

### Multiple Servers (Environments)

```java
@Bean
public OpenAPI studentAPI() {
    Server devServer = new Server();
    devServer.setUrl("http://localhost:8080");
    devServer.setDescription("Development (API Gateway)");

    Server prodServer = new Server();
    prodServer.setUrl("https://api.yourdomain.com");
    prodServer.setDescription("Production");

    return new OpenAPI()
            .servers(List.of(devServer, prodServer))  // Dropdown in Swagger UI
            .info(new Info()
                .title("Student Service API")
                .version("1.0.0"));
}
```

**Result**: Swagger UI shows a dropdown to switch between environments.

### Environment-Based Server URL

```java
@Value("${swagger.server.url:http://localhost:8080}")
private String serverUrl;

@Value("${swagger.server.description:API Gateway}")
private String serverDescription;

@Bean
public OpenAPI studentAPI() {
    Server server = new Server();
    server.setUrl(serverUrl);
    server.setDescription(serverDescription);

    return new OpenAPI()
            .servers(List.of(server))
            // ...
}
```

**application.yml** (local):
```yaml
swagger:
  server:
    url: http://localhost:8080
    description: API Gateway
```

**application-docker.yml**:
```yaml
swagger:
  server:
    url: ${SWAGGER_SERVER_URL:http://localhost:8080}
    description: ${SWAGGER_SERVER_DESCRIPTION:API Gateway}
```

### Endpoint-Specific Security

**Problem**: Some endpoints are public, but Swagger shows lock icon on all.

**Solution**: Use `@SecurityRequirement` annotation on controllers.

**Global security** (in OpenAPIConfig):
```java
// Remove this line to disable global security
// .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
```

**Per-endpoint security** (in controllers):

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    @SecurityRequirement(name = "")  // Public endpoint - no lock icon
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // ...
    }

    @GetMapping("/profile")
    @SecurityRequirement(name = "Bearer Authentication")  // Protected - shows lock icon
    public ResponseEntity<UserProfile> getProfile() {
        // ...
    }
}
```

### Custom Tags for Grouping

```java
@RestController
@RequestMapping("/api/students")
@Tag(name = "Student Management", description = "APIs for managing student profiles")
public class StudentController {

    @GetMapping
    @Operation(summary = "Get all students", description = "Returns paginated list of students")
    public ResponseEntity<Page<Student>> getAllStudents() {
        // ...
    }

    @PostMapping
    @Operation(summary = "Create student", description = "Creates a new student record")
    public ResponseEntity<Student> createStudent(@RequestBody StudentRequest request) {
        // ...
    }
}
```

**Result**: Swagger UI groups endpoints under "Student Management" section.

### Hide Internal Endpoints

```java
@RestController
@RequestMapping("/internal")
@Hidden  // Hides entire controller from Swagger
public class InternalController {
    // ...
}

// Or hide specific endpoints
@GetMapping("/debug")
@Hidden  // Hides this endpoint only
public ResponseEntity<String> debugInfo() {
    // ...
}
```

## Annotations for Better Documentation

### @Operation - Document Endpoints

```java
@GetMapping("/{id}")
@Operation(
    summary = "Get student by ID",
    description = "Returns student details for the given student ID",
    responses = {
        @ApiResponse(responseCode = "200", description = "Student found"),
        @ApiResponse(responseCode = "404", description = "Student not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    }
)
public ResponseEntity<Student> getStudent(@PathVariable UUID id) {
    // ...
}
```

### @Schema - Document DTOs

```java
@Schema(description = "Request body for creating a new student")
public class StudentCreateRequest {

    @Schema(description = "Student's full name in Khmer", example = "សូម លីម", required = true)
    @NotBlank
    private String nameKhmer;

    @Schema(description = "Student's full name in Latin", example = "Som Lim", required = true)
    @NotBlank
    private String nameLatin;

    @Schema(description = "Date of birth", example = "2010-05-15", required = true)
    @NotNull
    private LocalDate dateOfBirth;

    @Schema(description = "Student's phone number", example = "0123456789", required = false)
    private String phoneNumber;
}
```

**Result in Swagger**:
```json
{
  "nameKhmer": "សូម លីម",
  "nameLatin": "Som Lim",
  "dateOfBirth": "2010-05-15",
  "phoneNumber": "0123456789"
}
```

### @Parameter - Document Path/Query Parameters

```java
@GetMapping
@Operation(summary = "Get all students")
public ResponseEntity<Page<Student>> getAllStudents(
    @Parameter(description = "Page number (0-indexed)", example = "0")
    @RequestParam(defaultValue = "0") int page,

    @Parameter(description = "Number of items per page", example = "20")
    @RequestParam(defaultValue = "20") int size,

    @Parameter(description = "Filter by student name", example = "Som")
    @RequestParam(required = false) String name
) {
    // ...
}
```

## Common Issues and Solutions

### Issue 1: Swagger UI Not Accessible (404)

**Symptoms**:
- `http://localhost:8082/swagger-ui.html` returns 404

**Possible Causes**:

1. **Dependency not added**
   - Check `pom.xml` or `build.gradle` for springdoc dependency
   - Run `./mvnw dependency:tree | grep springdoc`

2. **SecurityConfig blocks Swagger endpoints**
   - Check `.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()`
   - Must come BEFORE `.anyRequest().authenticated()`

3. **Wrong URL**
   - Try `/swagger-ui/index.html` instead of `/swagger-ui.html`
   - Spring Boot 3 uses `/swagger-ui/index.html` as default

**Solution**:
```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/v3/api-docs/**").permitAll()
        .requestMatchers("/swagger-ui/**").permitAll()
        .requestMatchers("/swagger-ui.html").permitAll()  // Both formats
        .anyRequest().authenticated()
)
```

### Issue 2: CORS Errors in Swagger UI

**Symptoms**:
```
Access to fetch at 'http://localhost:8082/api/students' from origin 'http://localhost:8082'
has been blocked by CORS policy
```

**Cause**: Server URL in OpenAPIConfig points to service port instead of API Gateway.

**Solution**:
```java
// Wrong
server.setUrl("http://localhost:8082");  ❌

// Correct
server.setUrl("http://localhost:8080");  ✅ API Gateway
```

### Issue 3: "Authorize" Button Not Showing

**Symptoms**:
- Swagger UI loads, but no "Authorize" button

**Cause**: Security scheme not configured.

**Solution**: Ensure OpenAPIConfig includes:
```java
.components(new Components()
    .addSecuritySchemes(SECURITY_SCHEME_NAME, createSecurityScheme()))
.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
```

### Issue 4: JWT Token Not Sent with Requests

**Symptoms**:
- Clicked "Authorize" and entered token
- Requests still return 401 Unauthorized

**Debugging**:
1. Open browser DevTools → Network tab
2. Click "Try it out" on an endpoint
3. Check request headers - should see:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

**If header is missing**:
- Security scheme not applied globally: check `.addSecurityItem()`
- Token not saved: click "Authorize" again and ensure token is saved

**If header is present but 401 still returned**:
- Token expired: get a new token from auth-service
- Token invalid: check JWT secret matches across services
- JwtAuthenticationFilter not working: check service logs

### Issue 5: Endpoint Not Showing in Swagger

**Symptoms**:
- Controller exists, but endpoint not in Swagger UI

**Possible Causes**:

1. **Controller not in component scan**
   ```java
   @SpringBootApplication(scanBasePackages = "com.sms.student")  // Check this
   public class StudentServiceApplication {
       // ...
   }
   ```

2. **Endpoint has @Hidden annotation**
   ```java
   @GetMapping("/debug")
   @Hidden  // Remove this if you want it in Swagger
   public ResponseEntity<String> debug() { }
   ```

3. **OpenAPI not scanning package**
   - Check application.yml for `springdoc.packagesToScan`

### Issue 6: Wrong Request/Response Examples

**Symptoms**:
- Swagger shows generic examples instead of meaningful data

**Solution**: Add `@Schema` annotations with examples:

```java
public class StudentRequest {

    @Schema(description = "Student name", example = "Som Lim")  // Add example
    private String name;

    @Schema(description = "Age", example = "16", minimum = "5", maximum = "25")
    private int age;
}
```

## Configuration Properties (application.yml)

You can customize Swagger behavior in `application.yml`:

```yaml
springdoc:
  # Swagger UI configuration
  swagger-ui:
    path: /swagger-ui.html  # Custom path
    operationsSorter: method  # Sort by HTTP method
    tagsSorter: alpha  # Sort tags alphabetically
    enabled: true  # Disable in production

  # API Docs configuration
  api-docs:
    path: /v3/api-docs  # Custom path
    enabled: true

  # Package scanning
  packagesToScan: com.sms.student.controller  # Only scan these packages

  # Path matching
  pathsToMatch: /api/**  # Only include these paths

  # Show actuator endpoints
  show-actuator: false  # Hide actuator from Swagger
```

**Production Configuration** (application-docker.yml):

```yaml
springdoc:
  swagger-ui:
    enabled: ${SWAGGER_ENABLED:false}  # Disabled by default
  api-docs:
    enabled: ${SWAGGER_ENABLED:false}
```

**docker-compose.yml**:
```yaml
environment:
  - SWAGGER_ENABLED=true  # Enable for staging, false for production
```

## Testing with Swagger UI

### Workflow

1. **Get JWT Token**:
   - Open Swagger UI for auth-service: `http://localhost:8080/auth-service/swagger-ui.html`
   - Find `/api/auth/login` endpoint
   - Click "Try it out"
   - Enter credentials:
     ```json
     {
       "emailOrPhone": "teacher@example.com",
       "password": "password123",
       "language": "en"
     }
     ```
   - Click "Execute"
   - Copy the `access_token` from response

2. **Authorize in Target Service**:
   - Open Swagger UI for student-service: `http://localhost:8080/student-service/swagger-ui.html`
   - Click "Authorize" button (top-right)
   - Paste the JWT token
   - Click "Authorize", then "Close"

3. **Test Protected Endpoint**:
   - Find `/api/students` GET endpoint
   - Click "Try it out"
   - Set parameters (page, size)
   - Click "Execute"
   - Should return 200 OK with student list

4. **Test Create Endpoint**:
   - Find `/api/students` POST endpoint
   - Click "Try it out"
   - Modify request body:
     ```json
     {
       "nameKhmer": "សូម លីម",
       "nameLatin": "Som Lim",
       "dateOfBirth": "2010-05-15"
     }
     ```
   - Click "Execute"
   - Should return 201 Created

### Automated Testing with Swagger

You can use Swagger's OpenAPI spec for automated testing:

```bash
# Generate client code from OpenAPI spec
curl http://localhost:8082/v3/api-docs -o openapi.json

# Use OpenAPI Generator to create test client
npx @openapitools/openapi-generator-cli generate \
  -i openapi.json \
  -g java \
  -o student-service-client/
```

## Comparison: Swagger vs Postman

| Feature | Swagger UI | Postman |
|---------|-----------|---------|
| Setup | Automatic from code | Manual endpoint creation |
| Documentation | Auto-generated | Manual documentation |
| Authentication | Built-in JWT support | Manual header setup |
| Request Examples | From `@Schema` annotations | Manual examples |
| Sharing | URL only (no export needed) | Export/import collections |
| Version Control | In code (git tracked) | Separate collection files |
| Learning Curve | Lower (point-and-click) | Higher (more features) |

**Recommendation**: Use **Swagger** for internal team development, **Postman** for complex testing scenarios and external API consumers.

## Production Considerations

### Security

1. **Disable Swagger in Production** (optional):
   ```yaml
   springdoc:
     swagger-ui:
       enabled: false
     api-docs:
       enabled: false
   ```

2. **Or restrict access**:
   ```java
   .authorizeHttpRequests(auth -> auth
           .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
           .hasRole("ADMIN")  // Only admins can access
           .anyRequest().authenticated()
   )
   ```

3. **Use HTTPS** for server URL:
   ```java
   server.setUrl("https://api.yourdomain.com");
   ```

### Performance

- OpenAPI spec generation has minimal overhead
- Swagger UI is static HTML/JS (no server impact)
- Consider caching OpenAPI JSON:
  ```yaml
  springdoc:
    cache:
      disabled: false
  ```

## Related Documentation

- **CORS Integration**: See `.standards/docs/cors-setup.md` for CORS configuration
- **Security Integration**: See `.standards/docs/reusable-components.md` for SecurityConfig
- **Template Reference**: See `.standards/templates/java/OpenAPIConfig.java`
- **Working Example**: See `auth-service/src/main/java/com/sms/auth/config/OpenAPIConfig.java`

## External Resources

- [Springdoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)
- [Spring Boot + OpenAPI Guide](https://www.baeldung.com/spring-rest-openapi-documentation)

## Quick Reference

### URLs

- **Swagger UI**: `http://localhost:{port}/swagger-ui.html` or `/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:{port}/v3/api-docs`
- **Via Gateway**: `http://localhost:8080/{service-name}/swagger-ui.html`

### Common Annotations

```java
@Tag(name = "Students", description = "Student management APIs")
@Operation(summary = "Get student", description = "Detailed description")
@Parameter(description = "Student ID", example = "123")
@Schema(description = "Student name", example = "Som Lim")
@ApiResponse(responseCode = "200", description = "Success")
@SecurityRequirement(name = "Bearer Authentication")
@Hidden  // Hide from Swagger
```

### Troubleshooting Checklist

- [ ] Springdoc dependency added
- [ ] OpenAPIConfig class created with @Configuration
- [ ] Server URL points to API Gateway (http://localhost:8080)
- [ ] Swagger endpoints permitted in SecurityConfig
- [ ] Service running and accessible
- [ ] JWT token obtained from auth-service
- [ ] Token entered in "Authorize" dialog
- [ ] CORS configured properly
- [ ] Endpoints not marked with @Hidden
