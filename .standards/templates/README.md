# Service Templates

This directory contains templates and code examples for creating standardized microservices in the SMS project.

## Overview

All templates in this directory follow the architectural standards defined in the parent `.standards/` directory. Use these templates to ensure new services are compliant from day one.

## Available Templates

### Documentation Templates

- **service-template.md** - Complete service structure documentation (41KB)
  - Full directory tree with explanations
  - Required files checklist
  - Configuration file templates
  - Java class templates with annotations
  - Step-by-step setup guide

### Configuration Templates

- **application.yml** - Default profile for local development
  - Database connection settings
  - Server port configuration
  - Local environment defaults
  - Commented examples of all standard properties

- **application-docker.yml** - Docker profile for containerized deployment
  - Environment variable references (SPRING_DATASOURCE_*, EUREKA_*)
  - Eureka instance configuration (hostname, prefer-ip-address: false)
  - Docker-specific settings

- **docker-compose-service.yml** - Docker Compose service definition
  - Standard environment variables
  - Network configuration (backend-network, database-network)
  - Port mappings
  - Health checks
  - Dependencies

- **Dockerfile** - Container image template
  - Multi-stage build for Java 21 / Spring Boot 3.5.7
  - Production-ready configuration
  - Health check support

- **pom-template.xml** - Maven dependencies template
  - Spring Boot 3.5.7 parent
  - Standard dependencies (Spring Web, Security, Data JPA, Eureka, OpenAPI)
  - Build plugin configuration

### Java Class Templates

Located in `java/` subdirectory - production-ready Spring Boot configuration classes:

- **CorsConfig.java** - CORS configuration
  - Allows cross-origin requests from frontend
  - Configurable allowed origins, methods, headers
  - Credentials support

- **OpenAPIConfig.java** - Swagger/OpenAPI documentation
  - API Gateway server URL (http://localhost:8080)
  - Bearer token authentication
  - API info (title, description, version)

- **SecurityConfig.java** - Spring Security configuration
  - JWT authentication integration
  - Public endpoint configuration (health checks, OpenAPI)
  - CORS integration
  - Security filter chain

- **JwtAuthenticationFilter.java** - JWT request filter
  - Extends OncePerRequestFilter
  - Extracts JWT from Authorization header
  - Delegates validation to JwtTokenProvider
  - Sets Spring Security context

- **JwtTokenProvider.java** - JWT token operations
  - Token generation with claims
  - Token parsing and validation
  - Signature verification
  - Expiration checking
  - Claims extraction

## Usage

### Quick Start: Create New Service from Template

**Option 1: Automated (Recommended)**

```bash
# Use the service creation script
.standards/scripts/create-service.sh --name attendance --port 8084

# The script will:
# 1. Copy auth-service as template
# 2. Rename packages (com.sms.auth → com.sms.attendance)
# 3. Update configuration files
# 4. Validate compliance
```

**Option 2: Manual**

1. **Copy the template service** (auth-service is the reference):
   ```bash
   cp -r auth-service new-service
   cd new-service
   ```

2. **Rename packages**:
   ```bash
   # macOS
   find . -type f -name "*.java" -exec sed -i '' 's/com.sms.auth/com.sms.newservice/g' {} +

   # Linux
   find . -type f -name "*.java" -exec sed -i 's/com.sms.auth/com.sms.newservice/g' {} +
   ```

3. **Update configuration files**:
   - Edit `src/main/resources/application.yml`:
     - Update `spring.application.name: new-service`
     - Update `server.port: 8084` (choose available port)
     - Update database URL if different

   - Edit `src/main/resources/application-docker.yml`:
     - Update `eureka.instance.hostname: new-service`

   - Edit `pom.xml`:
     - Update `<artifactId>new-service</artifactId>`
     - Update `<name>New Service</name>`
     - Update `<description>Service description</description>`

4. **Update Docker Compose**:
   - Copy `docker-compose-service.yml` template
   - Replace placeholders with service-specific values
   - Add to main `docker-compose.yml`

5. **Validate compliance**:
   ```bash
   .standards/scripts/validate-service-structure.sh new-service
   ```

6. **Remove auth-specific code**:
   - Delete authentication-related entities (User, RefreshToken)
   - Remove PasswordEncoderConfig if not needed
   - Remove RedisConfig if not using Redis
   - Keep: CorsConfig, OpenAPIConfig, SecurityConfig, JWT classes (for API security)

### Using Individual Templates

#### Configuration File Templates

**application.yml**:
```bash
# Copy to your service
cp .standards/templates/application.yml new-service/src/main/resources/application.yml

# Customize:
# - spring.application.name
# - server.port
# - datasource.url (if using database)
```

**application-docker.yml**:
```bash
# Copy to your service
cp .standards/templates/application-docker.yml new-service/src/main/resources/application-docker.yml

# Customize:
# - eureka.instance.hostname (must match service name)
```

#### Java Class Templates

**CorsConfig.java**:
```bash
# Copy to your service config package
cp .standards/templates/java/CorsConfig.java new-service/src/main/java/com/sms/newservice/config/

# Customize:
# - Update package declaration to match your service
# - Adjust allowed origins if needed
```

**OpenAPIConfig.java**:
```bash
# Copy to your service config package
cp .standards/templates/java/OpenAPIConfig.java new-service/src/main/java/com/sms/newservice/config/

# Customize:
# - Update package declaration
# - Update API title and description
```

**SecurityConfig.java**:
```bash
# Copy to your service config package
cp .standards/templates/java/SecurityConfig.java new-service/src/main/java/com/sms/newservice/config/

# Customize:
# - Update package declaration
# - Update public endpoints list (health checks, specific APIs)
```

**JWT Classes**:
```bash
# Copy both JWT classes to security package
cp .standards/templates/java/JwtAuthenticationFilter.java new-service/src/main/java/com/sms/newservice/security/
cp .standards/templates/java/JwtTokenProvider.java new-service/src/main/java/com/sms/newservice/security/

# Customize:
# - Update package declarations
# - JWT secret and expiration are configured in application.yml
```

## Template Maintenance

### Keeping Templates Up-to-Date

Templates should be synchronized with the reference service (auth-service):

1. **When auth-service is updated**:
   - Review changes for architectural patterns
   - Update affected templates
   - Document changes in `.standards/CHANGELOG.md`
   - Increment version in `.standards/docs/version-history.md`

2. **When Spring Boot is upgraded**:
   - Update pom-template.xml with new parent version
   - Update Java templates for new annotations/patterns
   - Test all templates with new Spring Boot version
   - Document breaking changes

3. **When new standards are introduced**:
   - Create new template files
   - Update service-template.md documentation
   - Update this README with usage instructions
   - Update validation script to check new standards

### Template Validation

Before publishing template changes:

1. **Test template completeness**:
   ```bash
   # Create test service from templates
   # Verify it compiles without errors
   # Verify it passes compliance validation
   ```

2. **Verify against reference service**:
   ```bash
   # Compare template with auth-service implementation
   diff .standards/templates/java/CorsConfig.java auth-service/src/main/java/com/sms/auth/config/CorsConfig.java
   ```

3. **Check for version-specific code**:
   - Ensure templates don't hardcode version-specific APIs
   - Use property placeholders where appropriate
   - Document required customizations

## Template Customization Guidelines

### Required Customizations

Every new service MUST customize:

1. **Package names**: `com.sms.{service-name}`
2. **Service name**: In application.yml and Eureka config
3. **Port**: Choose available port (see port allocation table in docs)
4. **Database**: Update datasource URL and database name
5. **API metadata**: Update OpenAPI title/description

### Optional Customizations

Services MAY customize based on requirements:

1. **CORS origins**: Add specific allowed origins
2. **Public endpoints**: Add service-specific public APIs
3. **Security rules**: Add custom authorization rules
4. **Dependencies**: Add service-specific Maven dependencies

### Forbidden Modifications

Do NOT modify these in new services:

1. **Package structure**: Must follow model/, security/, service/ standard
2. **Profile count**: Must have exactly 2 profiles (default, docker)
3. **Environment variable names**: Must use SPRING_DATASOURCE_*, EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
4. **JWT architecture**: Must keep Filter + Provider separation
5. **Configuration class names**: CorsConfig, OpenAPIConfig, SecurityConfig (exact names)

## Reference

- **Full Documentation**: `service-template.md`
- **Quickstart Guide**: `.standards/docs/quickstart-service-creation.md`
- **Validation**: `.standards/scripts/validate-service-structure.sh`
- **Compliance Checklist**: `.standards/checklists/service-compliance.md`

## Support

For questions or issues with templates:

1. Review `service-template.md` for complete documentation
2. Check `.standards/docs/troubleshooting.md` for common issues
3. Consult `.standards/docs/FAQ.md` for frequently asked questions
4. Compare with reference service (auth-service)
