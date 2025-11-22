# Spring Boot Microservice Standards Research

**Project**: Salarean School Management System
**Date**: 2025-11-22
**Purpose**: Document Spring Boot best practices and architectural decisions for microservice standardization
**Primary Source**: /Volumes/DATA/my-projects/salarean/SERVICE_COMPARISON_ANALYSIS.md

---

## Executive Summary

This research document provides comprehensive analysis of Spring Boot best practices for microservice architecture based on comparative analysis of auth-service and student-service implementations. The findings establish uniform standards for all current and future microservices in the Salarean SMS project.

**Key Recommendation**: Adopt auth-service patterns as the standard template for all microservices.

---

## Table of Contents

1. [Spring Profile Strategy](#1-spring-profile-strategy)
2. [Environment Variable Naming](#2-environment-variable-naming)
3. [Eureka Service Discovery](#3-eureka-service-discovery)
4. [Package Structure Standards](#4-package-structure-standards)
5. [JWT Architecture](#5-jwt-architecture)
6. [OpenAPI Configuration](#6-openapi-configuration)
7. [Service Template Creation](#7-service-template-creation)
8. [Implementation Recommendations](#8-implementation-recommendations)

---

## 1. Spring Profile Strategy

### Question Being Answered

What is the optimal Spring Boot profile strategy for a microservice architecture that supports both local development and Docker containerization?

### Research Findings from SERVICE_COMPARISON_ANALYSIS.md

**Current State Analysis**:

**auth-service** (2 profiles):
- `application.yml` - default profile for local development
- `application-docker.yml` - docker profile for containerized deployment
- Docker Compose uses: `SPRING_PROFILES_ACTIVE=docker`

**student-service** (4 profiles):
- `application.yml` - default profile
- `application-dev.yml` - development profile
- `application-docker.yml` - docker profile (unused)
- `application-prod.yml` - production profile
- Docker Compose uses: `SPRING_PROFILES_ACTIVE=prod`

**Issues Identified**:
- ❌ Inconsistent number of profiles across services
- ❌ Different profile names used in Docker (docker vs prod)
- ❌ student-service has unused docker profile
- ❌ Confusion about which profile to activate in containers

### Decision Made

✅ **Use exactly 2 Spring profiles for all microservices**:
1. `default` (application.yml) - Local development
2. `docker` (application-docker.yml) - Docker deployment

**Docker Compose Configuration**:
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
```

### Rationale

**Why 2 profiles?**
1. **Simplicity**: Minimal cognitive overhead - developers know exactly which profile to use
2. **Clear separation**: Local vs containerized environments have distinct concerns
3. **Maintainability**: Fewer files to manage and keep synchronized
4. **Consistency**: Same pattern across all services reduces confusion
5. **Spring Boot alignment**: Follows Spring Boot's philosophy of convention over configuration

**Why NOT multiple profiles (dev, test, prod)?**
1. **Environment-specific values should be externalized**: Use environment variables instead of profile files
2. **Docker handles environment differences**: Different environments use same docker profile with different env vars
3. **Reduces duplication**: Same configurations repeated across multiple profile files
4. **Deployment flexibility**: Can deploy same Docker image to dev/staging/prod with different env vars
5. **Less maintenance burden**: Fewer files to update when configuration changes

**Why name it "docker" not "prod"?**
1. **Describes deployment method, not environment**: The profile configures how to run in Docker, not which environment
2. **Environment-agnostic**: Same docker profile works for dev, staging, and production containers
3. **Self-documenting**: Developers immediately understand this profile is for containerized deployment
4. **Prevents confusion**: "prod" implies production-only, limiting reusability

### Alternatives Considered and Why Rejected

**Alternative 1: Multiple environment profiles (dev, test, staging, prod)**
- ❌ **Rejected**: Violates DRY principle (duplication across profiles)
- ❌ **Rejected**: Makes Docker image environment-specific (anti-pattern for containerization)
- ❌ **Rejected**: Configuration drift between profiles
- ❌ **Rejected**: More files to maintain

**Alternative 2: Single profile (application.yml only)**
- ❌ **Rejected**: Cannot differentiate local vs Docker database hosts
- ❌ **Rejected**: Requires complex conditional logic in single file
- ❌ **Rejected**: Makes local development harder (need to override many properties)

**Alternative 3: Three profiles (default, docker, test)**
- ✅ **Acceptable if needed**: Test profile can be useful for integration tests
- ⚠️ **Conditional**: Only create if tests require different configuration
- ✅ **auth-service example**: Has application-test.yml for test-specific Redis/DB config

### Spring Boot Best Practice References

From Spring Boot documentation:
- **Profile-specific files**: application-{profile}.yml is the recommended pattern
- **Active profiles**: Can be set via `spring.profiles.active` property or `SPRING_PROFILES_ACTIVE` env var
- **Profile precedence**: Profile-specific properties override default properties
- **Externalized configuration**: Environment variables take highest precedence over profile files

### Implementation Standard

**Required Files**:
```
src/main/resources/
├── application.yml           # Default profile (local development)
├── application-docker.yml    # Docker profile (containerized deployment)
└── application-test.yml      # Optional: Test profile (if needed for testing)
```

**application.yml** (default profile):
```yaml
spring:
  application:
    name: {service-name}
  datasource:
    url: jdbc:postgresql://localhost:5432/{db_name}
    username: sms_user
    password: ${DB_PASSWORD:password}  # Override via env var or default

server:
  port: 808X  # Service-specific port
```

**application-docker.yml** (docker profile):
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

eureka:
  instance:
    hostname: {service-name}
    prefer-ip-address: false
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

**docker-compose.yml**:
```yaml
services:
  {service-name}:
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-{service}:5432/{db_name}
      - SPRING_DATASOURCE_USERNAME=sms_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - JWT_SECRET=${JWT_SECRET}
```

---

## 2. Environment Variable Naming

### Question Being Answered

Should we use Spring Boot standard environment variable names (SPRING_DATASOURCE_*) or custom abbreviated names (DB_USERNAME, DB_PASSWORD)?

### Research Findings from SERVICE_COMPARISON_ANALYSIS.md

**Current State Analysis**:

**auth-service** (Spring Boot standard):
```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-auth:5432/auth_db
  - SPRING_DATASOURCE_USERNAME=sms_user
  - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
  - SPRING_REDIS_HOST=redis
  - JWT_SECRET=${JWT_SECRET}
```

**student-service** (Custom names):
```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-student:5432/student_db
  - DB_USERNAME=sms_user
  - DB_PASSWORD=${DB_PASSWORD}
  - EUREKA_CLIENT_SERVICE_URL=http://eureka-server:8761/eureka/
  - EUREKA_INSTANCE_HOSTNAME=student-service
  - EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
  - JWT_SECRET=${JWT_SECRET}
```

**Issues Identified**:
- ❌ Inconsistent database property naming (SPRING_DATASOURCE_USERNAME vs DB_USERNAME)
- ❌ Inconsistent Eureka property naming (EUREKA_CLIENT_SERVICEURL_DEFAULTZONE vs EUREKA_CLIENT_SERVICE_URL)
- ❌ student-service mixes standard and custom names
- ❌ Eureka instance properties configured as env vars in student-service, in YAML in auth-service

### Decision Made

✅ **Use Spring Boot standard property names exclusively**

**Required Environment Variable Format**:
```yaml
# Database Configuration
- SPRING_DATASOURCE_URL=jdbc:postgresql://{host}:{port}/{database}
- SPRING_DATASOURCE_USERNAME={username}
- SPRING_DATASOURCE_PASSWORD={password}

# Eureka Configuration
- EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

# Redis Configuration (if needed)
- SPRING_REDIS_HOST={host}
- SPRING_REDIS_PORT={port}
- SPRING_REDIS_PASSWORD={password}

# JWT Configuration
- JWT_SECRET={secret}

# Service-Specific
- UPLOAD_DIR={path}
- {OTHER_SERVICE_SPECIFIC_VARS}
```

**Forbidden Variable Names**:
```yaml
# ❌ DO NOT USE
- DB_USERNAME          # Use SPRING_DATASOURCE_USERNAME
- DB_PASSWORD          # Use SPRING_DATASOURCE_PASSWORD
- DB_URL               # Use SPRING_DATASOURCE_URL
- EUREKA_CLIENT_SERVICE_URL  # Use EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
- EUREKA_INSTANCE_HOSTNAME   # Configure in YAML, not env var
- EUREKA_INSTANCE_PREFER_IP_ADDRESS  # Configure in YAML, not env var
```

### Rationale

**Why Spring Boot standard names?**

1. **Automatic property binding**: Spring Boot automatically maps `SPRING_DATASOURCE_URL` to `spring.datasource.url`
   - No manual `@Value` annotations needed
   - No custom configuration code required
   - Works out-of-the-box with Spring Boot's externalized configuration

2. **Self-documenting**: Variable name clearly indicates which Spring Boot property it configures
   - `SPRING_DATASOURCE_USERNAME` → obviously for datasource username
   - `DB_USERNAME` → unclear, could be for any database connection

3. **Official Spring Boot convention**: Follows Spring Boot's relaxed binding rules
   - Environment variables: `SPRING_DATASOURCE_URL`
   - Properties file: `spring.datasource.url`
   - Clear 1:1 mapping

4. **IDE autocomplete support**: IDEs recognize standard Spring Boot properties
   - Provides autocomplete for configuration
   - Shows deprecation warnings
   - Links to documentation

5. **Consistency with Spring ecosystem**: Spring Cloud, Spring Data, etc. all follow this pattern
   - `SPRING_REDIS_HOST` for Redis
   - `SPRING_KAFKA_BOOTSTRAP_SERVERS` for Kafka
   - `SPRING_MAIL_HOST` for email

6. **Prevents naming conflicts**: Namespaced under `SPRING_*` prefix
   - No collision with OS or container environment variables
   - Clear ownership (Spring Boot managed)

7. **Documentation alignment**: Spring Boot reference documentation uses these names
   - Easier to find help online
   - Consistent with tutorials and Stack Overflow answers

8. **Validation and type conversion**: Spring Boot validates these properties automatically
   - Type checking (e.g., port must be integer)
   - Format validation (e.g., URL format)
   - Better error messages

**Why NOT custom abbreviated names?**

1. **Requires manual wiring**: Custom names need explicit `@Value` or `@ConfigurationProperties`
   ```java
   // ❌ Custom name requires manual wiring
   @Value("${DB_USERNAME}")
   private String username;

   // ✅ Standard name works automatically
   // spring.datasource.username automatically injected
   ```

2. **Ambiguity**: `DB_USERNAME` - which database? Application DB? Auth DB? Cache DB?
   - Standard names are specific: `SPRING_DATASOURCE_USERNAME` vs `SPRING_REDIS_PASSWORD`

3. **Maintenance burden**: Team must remember custom naming conventions
   - New developers need to learn project-specific patterns
   - Documentation overhead

4. **Configuration drift**: Custom names can diverge from Spring Boot updates
   - Spring Boot adds new properties → need to invent new custom names
   - Inconsistency accumulates over time

**Why configure Eureka instance in YAML, not environment variables?**

1. **Deployment-method specific, not environment-specific**:
   - `prefer-ip-address: false` is needed for Docker, regardless of dev/staging/prod
   - Hostname pattern is same across all environments
   - Belongs in docker profile, not runtime configuration

2. **Type safety**: YAML provides structure and validation
   ```yaml
   eureka:
     instance:
       hostname: auth-service
       prefer-ip-address: false  # Boolean type enforced
   ```
   vs
   ```yaml
   - EUREKA_INSTANCE_PREFER_IP_ADDRESS=false  # String "false", requires parsing
   ```

3. **Readability**: Nested YAML structure is clearer than flat environment variables
   ```yaml
   # ✅ YAML: Clear hierarchy
   eureka:
     instance:
       hostname: auth-service
       prefer-ip-address: false
     client:
       service-url:
         defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}

   # ❌ Environment variables: Flat and verbose
   - EUREKA_INSTANCE_HOSTNAME=auth-service
   - EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
   - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://...
   ```

4. **Separation of concerns**:
   - YAML: Deployment-method configuration (how to run in Docker)
   - Environment variables: Environment-specific secrets and URLs (which Eureka server)

### Alternatives Considered and Why Rejected

**Alternative 1: Mix of standard and custom names**
- ❌ **Rejected**: Creates confusion and inconsistency
- ❌ **Rejected**: Developers don't know which pattern to follow
- ❌ **Rejected**: student-service current state demonstrates this problem

**Alternative 2: All custom abbreviated names**
- ❌ **Rejected**: Loses Spring Boot automatic configuration
- ❌ **Rejected**: Requires maintaining custom configuration classes
- ❌ **Rejected**: Not aligned with Spring ecosystem

**Alternative 3: All configuration via environment variables (no YAML)**
- ❌ **Rejected**: docker-compose.yml becomes extremely verbose
- ❌ **Rejected**: Loses type safety and structure of YAML
- ❌ **Rejected**: Harder to read and maintain
- ✅ **Limited use case**: Acceptable for 12-factor apps in Kubernetes with ConfigMaps, but overkill for Docker Compose

### Spring Boot Best Practice References

From Spring Boot Reference Documentation (v3.5):

**Externalized Configuration Priority** (highest to lowest):
1. Command line arguments
2. Java System properties
3. OS environment variables
4. Profile-specific properties (application-{profile}.yml)
5. Application properties (application.yml)

**Relaxed Binding Rules**:
- Environment variable: `SPRING_DATASOURCE_URL`
- System property: `spring.datasource.url`
- Properties file: `spring.datasource.url`
- All map to same configuration property

**Common Application Properties** (from Spring Boot docs):
```properties
# DATASOURCE
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=

# EUREKA
eureka.client.service-url.defaultZone=
eureka.instance.hostname=
eureka.instance.prefer-ip-address=

# REDIS
spring.redis.host=
spring.redis.port=
```

### Implementation Standard

**docker-compose.yml** (all services):
```yaml
services:
  {service-name}:
    environment:
      # Profile activation
      - SPRING_PROFILES_ACTIVE=docker

      # Database (Spring Boot standard)
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-{service}:5432/{db_name}
      - SPRING_DATASOURCE_USERNAME=sms_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}  # From .env file

      # Eureka (Only URL via env var, instance config in YAML)
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

      # JWT (Custom property, but consistent across services)
      - JWT_SECRET=${JWT_SECRET}  # From .env file

      # Redis (if needed)
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
      - SPRING_REDIS_PASSWORD=${REDIS_PASSWORD}  # From .env file

      # Service-specific (prefix with service name or purpose)
      - UPLOAD_DIR=/app/uploads/{service}
      - MAX_FILE_SIZE=10485760
```

**application-docker.yml** (Eureka configuration):
```yaml
eureka:
  instance:
    hostname: ${HOSTNAME:{service-name}}  # Allow override, default to service name
    prefer-ip-address: false  # MUST be false for Docker
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

**.env file** (project root):
```properties
# Database credentials
DB_PASSWORD=your_secure_password_here

# JWT secret (shared across all services)
JWT_SECRET=your-256-bit-secret-key-here-minimum-32-chars

# Redis password
REDIS_PASSWORD=your_redis_password
```

---

## 3. Eureka Service Discovery

### Question Being Answered

Should Eureka instance registration use hostname-based or IP-based discovery in Docker environments? What is the correct value for `eureka.instance.prefer-ip-address`?

### Research Findings from SERVICE_COMPARISON_ANALYSIS.md

**Current State Analysis**:

**auth-service** (application-docker.yml):
```yaml
eureka:
  instance:
    hostname: auth-service
    prefer-ip-address: true  # ⚠️ IP-based registration
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

**student-service** (application-prod.yml):
```yaml
eureka:
  instance:
    hostname: student-service
    prefer-ip-address: false  # ✅ Hostname-based registration
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICE_URL:http://eureka-server:8761/eureka/}
```

**Issues Identified**:
- ❌ Inconsistent `prefer-ip-address` setting (auth=true, student=false)
- ⚠️ auth-service registered with IP from wrong Docker network
- ⚠️ student-service had timeout issues before fixing prefer-ip-address
- 🐛 **Root cause of multi-network issue**: Services connected to multiple networks (backend-network + database-network) caused wrong IP registration

**Documented Issue from Analysis**:
> "This decision was validated by fixing the student-service timeout issue (registered with 172.19.0.x instead of 172.20.0.x)."

### Decision Made

✅ **Use hostname-based registration with `prefer-ip-address: false` for ALL services**

**Standard Configuration**:
```yaml
eureka:
  instance:
    hostname: {service-name}  # e.g., auth-service, student-service
    prefer-ip-address: false  # MANDATORY: Must be false
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

### Rationale

**Why `prefer-ip-address: false`?**

1. **Docker DNS resolution**: Docker's embedded DNS automatically resolves service hostnames
   - Hostname `auth-service` → Docker routes to correct container
   - No need to know specific IP address
   - Works across Docker networks

2. **Multi-network compatibility**: Services connected to multiple Docker networks have multiple IPs
   ```yaml
   # Example: student-service connected to two networks
   networks:
     - backend-network    # IP: 172.20.0.5 (for API Gateway communication)
     - database-network   # IP: 172.19.0.3 (for PostgreSQL communication)
   ```
   - With `prefer-ip-address: true`: Eureka registers with wrong network IP (database-network instead of backend-network)
   - With `prefer-ip-address: false`: Eureka registers hostname, Docker DNS routes correctly

3. **Container restarts**: IP addresses can change when containers restart
   - Hostname remains constant
   - No Eureka re-registration needed
   - More stable service discovery

4. **Kubernetes/Swarm compatibility**: Hostname-based discovery is standard in orchestration platforms
   - Prepares codebase for future migration to Kubernetes
   - Service mesh compatibility (Istio, Linkerd)
   - Load balancing works better with hostnames

5. **Debugging simplicity**: Service names are human-readable
   - Eureka dashboard shows: `auth-service`, `student-service`
   - Not: `172.20.0.3`, `172.20.0.5`
   - Easier to identify services in logs and monitoring

6. **Follows Spring Cloud Netflix best practices**: Official recommendation for Docker deployments

**Why NOT `prefer-ip-address: true`?**

1. **Multi-network IP confusion**: Registers IP from wrong network
   - Service might register database-network IP
   - API Gateway can't reach it (only on backend-network)
   - **Real bug experienced**: student-service registered 172.19.0.x (database network) instead of 172.20.0.x (backend network)

2. **Container IP instability**: IPs change on container restart
   - Requires Eureka heartbeat to update
   - Temporary service unavailability
   - More failure points

3. **Not compatible with overlay networks**: In Docker Swarm, overlay networks need hostname resolution

4. **Harder to debug**: Looking at `172.20.0.5` in logs doesn't tell you which service it is

**Docker Networking Context**:

Typical salarean service network setup:
```yaml
services:
  student-service:
    networks:
      - backend-network    # For Eureka, API Gateway communication
      - database-network   # For PostgreSQL communication
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

networks:
  backend-network:     # 172.20.0.0/16
  database-network:    # 172.19.0.0/16
```

With `prefer-ip-address: true`:
- Container has two IPs: 172.20.0.5 (backend) and 172.19.0.3 (database)
- Eureka might register: 172.19.0.3
- API Gateway on backend-network tries to call: 172.19.0.3
- ❌ **FAILS**: API Gateway not on database-network, can't reach that IP

With `prefer-ip-address: false`:
- Eureka registers: `student-service` (hostname)
- API Gateway calls: `student-service`
- Docker DNS resolves to correct backend-network IP: 172.20.0.5
- ✅ **SUCCEEDS**: Hostname resolution uses correct network context

### Alternatives Considered and Why Rejected

**Alternative 1: Use `prefer-ip-address: true` with explicit `ipAddress` configuration**
```yaml
eureka:
  instance:
    prefer-ip-address: true
    ip-address: 172.20.0.5  # Manually specify backend network IP
```
- ❌ **Rejected**: Hardcoding IPs defeats Docker's dynamic networking
- ❌ **Rejected**: IPs change on container recreation
- ❌ **Rejected**: Requires manual network inspection to find correct IP
- ❌ **Rejected**: Not scalable (every service needs manual IP configuration)

**Alternative 2: Use `prefer-ip-address: true` with single network per service**
```yaml
services:
  student-service:
    networks:
      - backend-network  # Only one network
```
- ❌ **Rejected**: Violates network isolation principles
- ❌ **Rejected**: Database should not be exposed to backend-network (security risk)
- ❌ **Rejected**: Increases blast radius of network issues
- ⚠️ **Limited use case**: Might work for simple deployments, but not recommended

**Alternative 3: Use IP-based registration with network aliases**
- ❌ **Rejected**: Overcomplicated solution
- ❌ **Rejected**: Hostname-based registration is simpler and more standard
- ❌ **Rejected**: No benefit over hostname approach

### Spring Boot Best Practice References

From Spring Cloud Netflix Eureka documentation:

**Default behavior**:
- `eureka.instance.prefer-ip-address` defaults to `false`
- Recommends hostname-based registration for cloud deployments

**Use cases for `true`**:
- Legacy systems with DNS issues
- Specific AWS EC2 configurations
- Not recommended for Docker/Kubernetes

**Docker-specific recommendations**:
- Set `eureka.instance.hostname` to Docker service name
- Use `prefer-ip-address: false`
- Let Docker DNS handle resolution

From Docker Networking documentation:
- Embedded DNS server in Docker resolves container names to IPs
- Hostname resolution is network-scoped (chooses correct IP for calling network)
- Service discovery should use container names, not IPs

### Implementation Standard

**application-docker.yml** (all services):
```yaml
spring:
  application:
    name: {service-name}

eureka:
  instance:
    # Hostname MUST match docker-compose service name
    hostname: ${HOSTNAME:{service-name}}  # Allow override via env var
    prefer-ip-address: false  # MANDATORY: MUST be false for Docker

    # Optional: Custom instance ID for multiple instances
    instance-id: ${spring.application.name}:${HOSTNAME:${random.value}}

    # Health check configuration
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90

  client:
    # Externalized via environment variable
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}

    # Fetch registry from Eureka server
    fetch-registry: true
    register-with-eureka: true

    # Registry fetch interval
    registry-fetch-interval-seconds: 30
```

**docker-compose.yml**:
```yaml
services:
  {service-name}:
    image: {service-image}
    container_name: {service-name}  # Should match hostname

    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      # Optional: Override hostname if needed
      # - HOSTNAME={custom-hostname}

    networks:
      - backend-network    # For service-to-service communication
      - database-network   # For database access

    depends_on:
      - eureka-server
      - postgres-{service}

networks:
  backend-network:
    driver: bridge
  database-network:
    driver: bridge
```

**Validation Checklist**:
- [ ] `prefer-ip-address: false` in all application-docker.yml files
- [ ] `hostname` matches docker-compose service name
- [ ] Service connected to backend-network for Eureka communication
- [ ] Eureka dashboard shows service names (not IP addresses)
- [ ] Inter-service communication works via hostname (e.g., http://student-service:8082)

**Debugging Tips**:
```bash
# Check Eureka registration
curl http://localhost:8761/eureka/apps

# Should show:
<instance>
  <hostName>student-service</hostName>  <!-- ✅ Hostname, not IP -->
  <ipAddr>172.20.0.5</ipAddr>           <!-- IP for reference -->
  <status>UP</status>
</instance>

# Test service discovery
docker exec api-gateway curl http://student-service:8082/actuator/health
# Should succeed (Docker DNS resolves hostname)
```

---

## 4. Package Structure Standards

### Question Being Answered

What is the optimal package structure for Spring Boot microservices to ensure consistency, maintainability, and adherence to separation of concerns principles?

### Research Findings from SERVICE_COMPARISON_ANALYSIS.md

**Current State Analysis**:

**auth-service**:
```
com.sms.auth/
├── config/
│   ├── CorsConfig.java
│   ├── OpenAPIConfig.java              # ✅ Correct capitalization
│   ├── PasswordEncoderConfig.java
│   ├── RedisConfig.java
│   └── SecurityConfig.java
├── controller/
├── dto/
├── exception/
├── model/                               # ✅ Entity package named "model"
├── repository/
├── security/                            # ✅ Dedicated security package
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
├── service/                             # ✅ Flat structure
│   ├── AuthService.java
│   ├── AuthServiceImpl.java
│   ├── RefreshTokenService.java
│   └── RefreshTokenServiceImpl.java
└── validation/                          # ✅ Custom validators
```

**student-service**:
```
com.sms.student/
├── config/
│   ├── EurekaConfig.java
│   ├── FileUploadConfig.java
│   ├── JwtAuthenticationFilter.java    # ❌ JWT in config package
│   ├── OpenApiConfig.java              # ❌ Incorrect capitalization
│   └── SecurityConfig.java
├── controller/
├── dto/
├── entity/                              # ❌ Entity package named "entity"
├── enums/                               # Service-specific enums package
├── exception/
├── repository/
└── service/
    ├── StudentService.java
    ├── PhotoService.java
    └── impl/                            # ❌ Nested implementation package
        ├── StudentServiceImpl.java
        └── PhotoServiceImpl.java
```

**Issues Identified**:
- ❌ Different entity package names (model/ vs entity/)
- ❌ JWT classes in different locations (security/ vs config/)
- ❌ Service implementations organized differently (flat vs nested)
- ❌ OpenAPI config naming inconsistency (OpenAPIConfig vs OpenApiConfig)
- ❌ Missing CORS configuration in student-service
- ❌ Missing validation package in student-service
- ❌ auth-service has better separation of concerns

### Decision Made

✅ **Adopt auth-service package structure as the standard for all microservices**

**Standardized Package Structure**:
```
com.sms.{service}/
├── config/          # Configuration classes ONLY (no business logic)
│   ├── CorsConfig.java
│   ├── OpenAPIConfig.java      # Note: "OpenAPI" not "OpenApi"
│   ├── SecurityConfig.java
│   └── {ServiceSpecific}Config.java
├── controller/      # REST controllers
├── dto/             # Request/Response DTOs
├── exception/       # Custom exceptions & global exception handlers
├── model/           # JPA Entities (MUST be "model/" NOT "entity/")
├── repository/      # JPA Repositories
├── security/        # Security, JWT, Auth filters (NOT in config/)
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
├── service/         # Service interfaces & implementations together
│   ├── {Domain}Service.java
│   └── {Domain}ServiceImpl.java
└── validation/      # Custom validators (optional)
```

### Rationale

**1. Why `model/` instead of `entity/`?**

✅ **Benefits of "model" package**:
- **More generic and flexible**: Can contain both JPA entities and domain models
- **Aligns with Domain-Driven Design (DDD)**: "Model" is DDD terminology
- **Spring documentation uses "model"**: Examples in Spring Data JPA docs
- **Separation from persistence**: "Entity" implies JPA-specific, "model" is broader
- **Consistency with auth-service**: Established pattern in codebase

❌ **Problems with "entity" package**:
- **JPA-coupled naming**: Implies tight coupling to persistence layer
- **Less flexible**: Harder to introduce non-JPA domain objects
- **Inconsistent with DDD**: Domain layer should be persistence-agnostic

**Example**:
```java
// model/ package can contain:
com.sms.student.model/
├── Student.java              // JPA Entity
├── StudentProfile.java       // Value Object (not a JPA entity)
└── StudentStatus.java        // Enum

// entity/ package implies all must be JPA entities
com.sms.student.entity/
├── Student.java              // OK: JPA Entity
├── StudentProfile.java       // Confusing: Is this a JPA entity?
└── StudentStatus.java        // Not an entity, but in entity package?
```

**2. Why `security/` package instead of putting JWT in `config/`?**

✅ **Benefits of dedicated security package**:
- **Separation of concerns**: Security logic isolated from configuration
- **Single Responsibility Principle**: Config classes configure, security classes handle authentication/authorization
- **Easier navigation**: Developers know where to find security-related code
- **Testability**: Security components can be tested independently
- **Scalability**: As security grows (OAuth2, LDAP), security package can expand

❌ **Problems with JWT in config package**:
- **Violates SRP**: Config classes should only configure beans, not implement business logic
- **Confusing**: `JwtAuthenticationFilter` in `config/` doesn't configure anything, it filters requests
- **Harder to test**: Mixed responsibilities make unit testing harder
- **Incorrect semantics**: Filters and providers are not configuration classes

**Example**:
```java
// ✅ CORRECT: security/ package
com.sms.auth.security/
├── JwtAuthenticationFilter.java     // Request filter (business logic)
└── JwtTokenProvider.java            // Token operations (business logic)

com.sms.auth.config/
├── SecurityConfig.java              // Configures security (registers filter)
├── CorsConfig.java                  // Configures CORS
└── RedisConfig.java                 // Configures Redis connection

// ❌ INCORRECT: All in config/
com.sms.student.config/
├── SecurityConfig.java              // Configuration ✅
├── JwtAuthenticationFilter.java     // Business logic ❌ (wrong package)
└── OpenApiConfig.java               // Configuration ✅
```

**3. Why flat service structure instead of `service/impl/`?**

✅ **Benefits of flat structure**:
- **Simplicity**: Interface and implementation together for easy navigation
- **Less nesting**: Fewer directories to navigate
- **Modern Spring practice**: `@Service` on implementation, interface optional
- **Easier refactoring**: Moving files doesn't break imports

❌ **Problems with `service/impl/` nesting**:
- **Outdated pattern**: From early 2000s Java EE era
- **Unnecessary separation**: Interface and implementation always 1:1 mapping
- **More boilerplate**: Extra package for no benefit
- **Harder navigation**: Need to switch between packages frequently

**Example**:
```java
// ✅ CORRECT: Flat structure
com.sms.student.service/
├── StudentService.java          // Interface
├── StudentServiceImpl.java      // Implementation
├── PhotoService.java             // Interface
└── PhotoServiceImpl.java         // Implementation

// ❌ OUTDATED: Nested structure
com.sms.student.service/
├── StudentService.java
├── PhotoService.java
└── impl/
    ├── StudentServiceImpl.java
    └── PhotoServiceImpl.java
```

**Modern Alternative** (if you don't need interface):
```java
// If no interface needed, just use class directly
com.sms.student.service/
└── StudentService.java    // Concrete class annotated with @Service
```

**4. Why `OpenAPIConfig` instead of `OpenApiConfig`?**

✅ **Correct**: `OpenAPIConfig` matches official brand name
- "OpenAPI" is the official specification name (successor to Swagger)
- Consistent with OpenAPI Initiative branding
- Matches library naming: `springdoc-openapi-starter-webmvc-ui`

❌ **Incorrect**: `OpenApiConfig` (wrong capitalization)
- "OpenApi" is not the official name
- Inconsistent with documentation and community

**5. Why mandatory CORS configuration?**

✅ **Benefits of CorsConfig**:
- **Web application requirement**: Frontend needs CORS to call backend APIs
- **Security best practice**: Explicitly define allowed origins
- **Centralized configuration**: All CORS settings in one place
- **Testing support**: Can configure different CORS for dev/prod

❌ **Risks of missing CORS**:
- Frontend can't call APIs (blocked by browser)
- Developers add `@CrossOrigin` everywhere (decentralized, hard to maintain)
- Security misconfigurations (overly permissive CORS)

**Example CorsConfig**:
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:3000",      // React dev server
                    "http://localhost:4200",      // Angular dev server
                    "https://sms.example.com"     // Production frontend
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

**6. Why validation package?**

✅ **Benefits of validation package**:
- **Custom validators**: Business logic validators (e.g., valid phone number format for Cambodia)
- **Reusability**: Validators can be used across DTOs
- **Testability**: Validation logic can be unit tested independently
- **Separation from DTOs**: DTOs focus on structure, validators focus on rules

**Example**:
```java
// validation/ package
com.sms.student.validation/
├── PhoneNumberValidator.java        // Custom validator for Cambodian phone numbers
├── StudentIdValidator.java          // Validates student ID format
└── AcademicYearValidator.java       // Validates academic year range

// Used in DTOs
@PhoneNumber  // Custom annotation
private String phoneNumber;
```

⚠️ **Optional**: Only create if service needs custom validators
- Not needed if only using standard validators (`@NotNull`, `@Email`, `@Size`)
- Create when business rules require custom validation logic

### Alternatives Considered and Why Rejected

**Alternative 1: Keep entity/ package name**
- ❌ **Rejected**: Inconsistent with auth-service
- ❌ **Rejected**: Less flexible for DDD
- ❌ **Rejected**: Not aligned with Spring documentation

**Alternative 2: Keep JWT in config/ package**
- ❌ **Rejected**: Violates Single Responsibility Principle
- ❌ **Rejected**: Confuses configuration with business logic
- ❌ **Rejected**: Harder to test and maintain

**Alternative 3: Use service/impl/ nesting**
- ❌ **Rejected**: Outdated pattern from Java EE era
- ❌ **Rejected**: Unnecessary complexity
- ❌ **Rejected**: Harder to navigate

**Alternative 4: No interfaces, only concrete service classes**
- ✅ **Acceptable**: Modern Spring doesn't require interfaces if using @Service
- ⚠️ **Trade-off**: Interfaces help with mocking in tests and future extensibility
- 🎯 **Recommendation**: Use interfaces for core domain services, skip for simple utilities

**Alternative 5: More granular packages (e.g., controller/request, controller/response)**
- ❌ **Rejected**: Over-engineering for microservices
- ❌ **Rejected**: Too much nesting
- ✅ **Alternative**: Can use inner classes or separate packages if controller grows very large

### Spring Boot Best Practice References

From Spring Boot and Spring Data JPA documentation:

**Package structure recommendations**:
- Use domain-driven package naming
- Separate concerns by responsibility (controller, service, repository)
- Configuration classes in `config/` package
- Keep related classes together

**Spring Data JPA examples use "model"**:
```java
// From Spring Data JPA documentation
com.example.application/
├── model/        // Domain entities
├── repository/   // Data access
└── service/      // Business logic
```

**Spring Security recommendations**:
- Keep security filters and providers separate from configuration
- Configuration classes should configure, not implement

**Clean Architecture principles**:
- Entities/Models: Core domain objects (persistence-agnostic naming)
- Use Cases/Services: Business logic
- Interface Adapters/Controllers: HTTP handling
- Frameworks/Config: Spring configuration

### Implementation Standard

**Required Packages** (all services):
```
com.sms.{service}/
├── config/          # MANDATORY: Configuration classes
│   ├── CorsConfig.java              # MANDATORY
│   ├── OpenAPIConfig.java           # MANDATORY (Note: OpenAPI not OpenApi)
│   ├── SecurityConfig.java          # MANDATORY
│   └── ...                          # Service-specific configs
├── controller/      # MANDATORY: REST endpoints
├── dto/             # MANDATORY: Request/response objects
├── exception/       # MANDATORY: Exception handling
├── model/           # MANDATORY: Domain entities (NOT entity/)
├── repository/      # MANDATORY: Data access
├── security/        # MANDATORY (if using JWT): Auth filters
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
├── service/         # MANDATORY: Business logic (flat structure)
└── validation/      # OPTIONAL: Custom validators
```

**Package Naming Rules**:
- ✅ Use lowercase package names (com.sms.auth not com.sms.Auth)
- ✅ Use singular for single responsibility (config not configs)
- ✅ Use plural for collections (exceptions for exception package)
- ✅ Be consistent with Spring Boot conventions

**Class Naming Conventions**:
- Controllers: `{Entity}Controller.java` (e.g., StudentController)
- Services: `{Entity}Service.java` + `{Entity}ServiceImpl.java`
- Repositories: `{Entity}Repository.java`
- DTOs: `{Entity}Request.java`, `{Entity}Response.java`, `{Entity}DTO.java`
- Configs: `{Purpose}Config.java` (e.g., CorsConfig, SecurityConfig)
- Exceptions: `{Description}Exception.java` (e.g., StudentNotFoundException)

**Configuration Class Standards**:

**CorsConfig.java** (MANDATORY):
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

**OpenAPIConfig.java** (MANDATORY):
```java
@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI {serviceName}API() {
        Server server = new Server();
        server.setUrl("http://localhost:8080");  // API Gateway
        server.setDescription("API Gateway");

        return new OpenAPI()
                .servers(List.of(server))
                .info(new Info()
                        .title("{Service Name} API")
                        .description("API for {service} management")
                        .version("1.0.0"));
    }
}
```

**SecurityConfig.java** (MANDATORY):
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Service-specific security configuration
        return http.build();
    }
}
```

**Migration Checklist for Existing Services**:
- [ ] Rename `entity/` → `model/` (if applicable)
- [ ] Create `security/` package
- [ ] Move JWT classes from `config/` to `security/`
- [ ] Flatten `service/impl/` into `service/`
- [ ] Rename `OpenApiConfig` → `OpenAPIConfig`
- [ ] Add `CorsConfig.java` if missing
- [ ] Create `validation/` package if needed
- [ ] Update imports in all affected files
- [ ] Run tests to verify refactoring

---

## 5. JWT Architecture

### Question Being Answered

Should JWT authentication logic be combined in a single filter class or separated into multiple components with distinct responsibilities?

### Research Findings from SERVICE_COMPARISON_ANALYSIS.md

**Current State Analysis**:

**auth-service** (Separated architecture):
```
security/
├── JwtAuthenticationFilter.java (2.7 KB)
│   - Extends OncePerRequestFilter
│   - Extracts JWT from Authorization header
│   - Delegates validation to JwtTokenProvider
│   - Sets Spring Security context
│
└── JwtTokenProvider.java (2.4 KB)
    - Token generation
    - Token parsing and validation
    - Signature verification
    - Claims extraction
    - Expiration checking
```

**student-service** (Combined architecture):
```
config/
└── JwtAuthenticationFilter.java (4.7 KB)
    - All JWT logic in single class
    - Filter + validation + parsing combined
    - No separate token provider
    - Larger file size (nearly 2x auth-service filter)
```

**Issues Identified**:
- ❌ Inconsistent separation of concerns
- ❌ student-service JWT filter is 2x larger (4.7KB vs 2.7KB)
- ❌ student-service combines filtering and token operations
- ❌ Harder to test student-service JWT (tightly coupled)
- ❌ Cannot reuse token operations in student-service (embedded in filter)

**From Analysis**:
> "auth-service separates concerns (filter + provider), student-service combines in one class"
> "student-service JWT filter is 2x larger (4.7KB vs 2.7KB) due to embedded validation logic"

### Decision Made

✅ **Separate JWT logic into exactly 2 classes in the `security/` package**

**Required Classes**:

**1. JwtAuthenticationFilter.java** (Filter logic only):
- Extends `OncePerRequestFilter`
- Extracts JWT from Authorization header
- Delegates validation to `JwtTokenProvider`
- Sets Spring Security context on successful authentication
- Handles filter exceptions

**2. JwtTokenProvider.java** (Token operations only):
- Token generation (for auth-service)
- Token parsing and validation
- Signature verification
- Claims extraction (username, roles, etc.)
- Expiration checking
- Secret key management

### Rationale

**Why separate into Filter + Provider?**

1. **Single Responsibility Principle (SRP)**:
   - **Filter**: Responsible for HTTP request/response handling
   - **Provider**: Responsible for JWT token operations
   - Each class has one reason to change

2. **Reusability**:
   - `JwtTokenProvider` can be used outside filter context
   - Other components can generate/validate tokens (e.g., refresh token endpoint)
   - Admin endpoints can validate tokens without filter

3. **Testability**:
   - Filter can be tested with mocked `JwtTokenProvider`
   - Provider can be unit tested without HTTP context
   - More focused, smaller unit tests

4. **Maintainability**:
   - Smaller classes (2.7KB filter vs 4.7KB combined)
   - Easier to understand and modify
   - Clear separation makes code reviews easier

5. **Dependency management**:
   - Provider can be injected where needed
   - Filter is registered once in security config
   - Clearer dependency graph

6. **Aligns with Spring Security patterns**:
   - Spring Security filters delegate to providers (e.g., AuthenticationProvider)
   - Separation of concerns is Spring Security design principle

**Real-world example of benefits**:

**Scenario**: Need to validate JWT in WebSocket connection (outside HTTP filter)

**❌ Combined approach** (student-service):
```java
// Cannot reuse - JWT logic embedded in filter
@Configuration
public class WebSocketConfig {
    // Must duplicate token validation logic here
    // OR make filter do things it shouldn't (validate outside HTTP context)
}
```

**✅ Separated approach** (auth-service):
```java
@Configuration
public class WebSocketConfig {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;  // ✅ Reuse token validation

    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Use jwtTokenProvider.validateToken() in handshake interceptor
    }
}
```

**Why NOT combine in single filter?**

1. **Violates SRP**: One class doing filtering AND token operations
2. **Harder to test**: Need to mock HTTP request/response for token tests
3. **Cannot reuse**: Token logic locked inside filter
4. **Larger classes**: 4.7KB vs 2.7KB (harder to understand)
5. **Tight coupling**: Changes to token logic affect filter, and vice versa

### Class Responsibilities

**JwtAuthenticationFilter.java** (Request filtering):
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) {

        // 1. Extract token from Authorization header
        String token = extractTokenFromRequest(request);

        // 2. Validate token (delegate to provider)
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 3. Extract username (delegate to provider)
            String username = jwtTokenProvider.getUsernameFromToken(token);

            // 4. Create authentication object
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

            // 5. Set security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 6. Continue filter chain
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

**Responsibilities**:
- ✅ HTTP request/response handling
- ✅ Authorization header extraction
- ✅ Security context management
- ✅ Filter chain continuation
- ❌ NOT token parsing (delegates to provider)
- ❌ NOT token validation logic (delegates to provider)

**JwtTokenProvider.java** (Token operations):
```java
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // 1. Generate token (for auth-service)
    public String generateToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // 2. Validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    // 3. Extract username
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // 4. Extract roles
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("roles", List.class);
    }

    // 5. Check expiration
    public boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        return expiration.before(new Date());
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

**Responsibilities**:
- ✅ Token generation with claims
- ✅ Token signature verification
- ✅ Claims extraction (username, roles)
- ✅ Expiration checking
- ✅ Secret key management
- ❌ NOT HTTP handling (no request/response dependencies)
- ❌ NOT security context management (that's filter's job)

### Alternatives Considered and Why Rejected

**Alternative 1: Single JwtUtil class with all JWT logic**
```java
public class JwtUtil {
    // All token operations + filtering logic combined
}
```
- ❌ **Rejected**: Violates SRP (too many responsibilities)
- ❌ **Rejected**: Harder to test (HTTP dependencies mixed with token logic)
- ❌ **Rejected**: Cannot inject where only token operations needed
- ❌ **Rejected**: Current student-service problem

**Alternative 2: Three classes (Filter + Provider + Validator)**
```java
JwtAuthenticationFilter.java   // Filtering
JwtTokenProvider.java           // Token generation
JwtTokenValidator.java          // Token validation
```
- ❌ **Rejected**: Over-engineering for microservices
- ❌ **Rejected**: Token generation and validation are closely related
- ❌ **Rejected**: Provider can handle both generation and validation
- ⚠️ **Acceptable**: Only if validation logic becomes very complex (e.g., multiple token types)

**Alternative 3: Filter with injected validator, separate generator**
```java
JwtAuthenticationFilter.java   // Filtering + validation
JwtTokenGenerator.java          // Generation only (for auth-service)
```
- ❌ **Rejected**: Inconsistent - why separate generation but not validation?
- ❌ **Rejected**: Non-auth services don't generate tokens, but still have generator class
- ❌ **Rejected**: Validation and generation share same secret key management

**Alternative 4: No interfaces, just concrete classes**
- ✅ **RECOMMENDED**: For microservices, concrete classes are sufficient
- ✅ **Justification**: No need for multiple JWT implementations
- ✅ **Spring handles**: `@Component` on concrete class works fine
- ⚠️ **Exception**: If you need to swap JWT providers (rare), add interface later

### Spring Boot Best Practice References

From Spring Security documentation:

**Filter recommendations**:
- Filters should focus on HTTP request/response handling
- Delegate complex operations to services/providers
- Keep filters thin and focused

**Authentication Provider pattern**:
- Spring Security uses provider pattern (e.g., DaoAuthenticationProvider)
- Filters delegate to providers for authentication logic
- JWT should follow same pattern

From Clean Code principles:

**Single Responsibility Principle**:
- A class should have one, and only one, reason to change
- JwtAuthenticationFilter changes when: Filter logic changes
- JwtTokenProvider changes when: Token format or validation logic changes

**Separation of Concerns**:
- HTTP concerns (filter) separate from business logic (token operations)
- Makes code easier to test, understand, and maintain

### Implementation Standard

**File Location** (MANDATORY):
```
com.sms.{service}/
└── security/
    ├── JwtAuthenticationFilter.java    # Filter logic
    └── JwtTokenProvider.java           # Token operations
```

**❌ FORBIDDEN Locations**:
- `config/JwtAuthenticationFilter.java` (JWT is not configuration)
- `util/JwtUtil.java` (Not a utility, it's a security component)
- `filter/JwtFilter.java` (Use `security/` package for all security components)

**JwtAuthenticationFilter Template**:
```java
package com.sms.{service}.security;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.getUsernameFromToken(jwt);
                List<String> roles = jwtTokenProvider.getRolesFromToken(jwt);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
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

**JwtTokenProvider Template**:
```java
package com.sms.{service}.security;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return (List<String>) claims.get("roles");
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

**SecurityConfig Integration**:
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

**Migration Checklist for Existing Services**:
- [ ] Create `security/` package
- [ ] Create `JwtTokenProvider.java` in security package
- [ ] Extract token operations from filter into provider
- [ ] Refactor `JwtAuthenticationFilter` to use provider
- [ ] Move filter from `config/` to `security/` (if needed)
- [ ] Update SecurityConfig to inject filter
- [ ] Write unit tests for provider
- [ ] Write integration tests for filter with mocked provider
- [ ] Verify JWT authentication still works

---

## 6. OpenAPI Configuration

### Question Being Answered

When configuring OpenAPI/Swagger documentation for microservices behind an API Gateway, should the server URL point to the API Gateway or directly to the service port?

### Research Findings from SERVICE_COMPARISON_ANALYSIS.md

**Current State Analysis**:

**From CLAUDE.md Standards**:
```java
@Bean
public OpenAPI {serviceName}API() {
    Server server = new Server();
    server.setUrl("http://localhost:8080");  // API Gateway port
    server.setDescription("API Gateway");

    return new OpenAPI()
        .servers(List.of(server))
        .info(new Info()
            .title("{Service Name} API")
            .description("...")
            .version("1.0.0"))
        // ... security configuration
}
```

**Problem Scenario**:
```java
// ❌ INCORRECT: Pointing to service port
@Bean
public OpenAPI studentAPI() {
    Server server = new Server();
    server.setUrl("http://localhost:8082");  // Direct service port
    server.setDescription("Student Service");
    // ...
}
```

**Issues Identified**:
- ⚠️ **CORS errors**: Swagger UI loaded from localhost:8080 (API Gateway) tries to call localhost:8082 (service)
- ⚠️ **Cross-origin request**: Browser blocks requests from :8080 → :8082
- ⚠️ **Confusing for users**: Swagger shows wrong URL for how API should be accessed
- ⚠️ **Security inconsistency**: Direct service access may bypass API Gateway authentication

**From Analysis**:
> "Prevents CORS errors when Swagger UI calls APIs through the gateway"

### Decision Made

✅ **OpenAPI server URL MUST point to API Gateway, NOT direct service port**

**Required Configuration**:
```java
@Bean
public OpenAPI {serviceName}API() {
    Server server = new Server();
    server.setUrl("http://localhost:8080");  // API Gateway URL (MANDATORY)
    server.setDescription("API Gateway");

    return new OpenAPI()
            .servers(List.of(server))
            .info(new Info()
                    .title("{Service Name} API")
                    .description("API documentation for {service}")
                    .version("1.0.0"))
            .components(new Components()
                    .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .in(SecurityScheme.In.HEADER)
                            .name("Authorization")))
            .addSecurityItem(new SecurityRequirement()
                    .addList("bearer-jwt"));
}
```

### Rationale

**Why point to API Gateway?**

1. **Prevents CORS errors**:
   - **Problem**: Swagger UI served from http://localhost:8080 (API Gateway)
   - **If server URL is :8082**: Browser blocks cross-origin requests (8080 → 8082)
   - **If server URL is :8080**: Same-origin requests work without CORS issues

2. **Reflects actual API usage**:
   - **Production**: Frontend always calls through API Gateway (never direct service)
   - **Documentation**: Should show how API is actually accessed
   - **Testing**: Swagger UI should test same path as production

3. **Consistent security**:
   - API Gateway handles: Rate limiting, authentication, logging
   - Direct service access bypasses these protections
   - Swagger should test the secure path

4. **Gateway routing**:
   - API Gateway routes: `/api/students/**` → `student-service:8082`
   - Swagger shows: `http://localhost:8080/api/students/...`
   - Matches how frontend will call the API

**Example User Flow**:

**❌ INCORRECT (server URL = :8082)**:
```
1. User opens: http://localhost:8080/swagger-ui/index.html
2. Swagger loads: http://localhost:8080/v3/api-docs
3. OpenAPI config says: server.url = http://localhost:8082
4. User clicks "Try it out" for GET /api/students
5. Swagger tries: http://localhost:8082/api/students
6. Browser blocks: CORS error (origin :8080 requesting :8082)
7. ❌ Request fails
```

**✅ CORRECT (server URL = :8080)**:
```
1. User opens: http://localhost:8080/swagger-ui/index.html
2. Swagger loads: http://localhost:8080/v3/api-docs
3. OpenAPI config says: server.url = http://localhost:8080
4. User clicks "Try it out" for GET /api/students
5. Swagger tries: http://localhost:8080/api/students
6. API Gateway routes to: student-service:8082/api/students
7. ✅ Request succeeds
```

**Why NOT point to service port?**

1. **CORS violations**: Browser security blocks cross-origin requests
2. **Misleading documentation**: Doesn't reflect how API is accessed in production
3. **Bypasses gateway**: Testing without rate limiting, auth, logging
4. **Inconsistent with architecture**: Microservices should be accessed through gateway

### Microservice Architecture Pattern

**Typical Setup**:
```
Frontend (React)          → http://localhost:8080 (API Gateway)
Swagger UI                → http://localhost:8080 (API Gateway)
Mobile App                → https://api.sms.com (API Gateway)

API Gateway :8080         → Routes to microservices
├── /api/auth/**          → auth-service:8081
├── /api/students/**      → student-service:8082
├── /api/attendance/**    → attendance-service:8083
└── /api/grades/**        → grade-service:8084

Microservices
├── auth-service:8081     (Not directly accessible)
├── student-service:8082  (Not directly accessible)
├── attendance-service:8083 (Not directly accessible)
└── grade-service:8084    (Not directly accessible)
```

**Gateway Routing Example** (API Gateway configuration):
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: student-service
          uri: lb://student-service  # Load balanced via Eureka
          predicates:
            - Path=/api/students/**
          filters:
            - RewritePath=/api/students/(?<segment>.*), /${segment}
```

**OpenAPI Alignment**:
```java
// student-service OpenAPI config
server.setUrl("http://localhost:8080");  // Matches gateway

// Swagger shows:
// GET http://localhost:8080/api/students/1

// Which gateway routes to:
// GET http://student-service:8082/api/students/1
```

### Alternatives Considered and Why Rejected

**Alternative 1: Point to direct service port**
```java
server.setUrl("http://localhost:8082");  // Direct service
```
- ❌ **Rejected**: CORS errors in Swagger UI
- ❌ **Rejected**: Doesn't reflect actual API access pattern
- ❌ **Rejected**: Bypasses API Gateway features

**Alternative 2: Multiple server configurations**
```java
List<Server> servers = List.of(
    new Server().url("http://localhost:8080").description("API Gateway"),
    new Server().url("http://localhost:8082").description("Direct Service")
);
return new OpenAPI().servers(servers);
```
- ✅ **Possible**: Allows switching between gateway and direct
- ❌ **Rejected**: Encourages direct service access (anti-pattern)
- ❌ **Rejected**: Confusing for users (which URL to use?)
- ⚠️ **Limited use case**: Only useful for debugging

**Alternative 3: Different URLs for dev/prod**
```java
@Value("${openapi.server.url}")
private String serverUrl;

@Bean
public OpenAPI api() {
    return new OpenAPI()
            .servers(List.of(new Server().url(serverUrl)));
}

# application.yml (dev)
openapi.server.url: http://localhost:8080

# application-docker.yml (prod)
openapi.server.url: https://api.sms.com
```
- ✅ **RECOMMENDED**: Best practice for multiple environments
- ✅ **Justification**: Production URL different from local gateway URL
- ✅ **Benefit**: Single configuration for environment-specific server URLs

**Alternative 4: No server configuration (use relative URLs)**
```java
// Don't configure server URL, let Swagger UI use relative paths
return new OpenAPI()
        .info(...)  // No .servers()
        .components(...);
```
- ✅ **Possible**: Swagger UI uses current page origin
- ⚠️ **Trade-off**: Works if Swagger served from gateway, fails if served from service
- ❌ **Rejected**: Less explicit, harder to understand

### Spring Boot Best Practice References

From springdoc-openapi documentation:

**Server configuration**:
```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local server"),
                new Server().url("https://api.example.com").description("Production server")
            ));
}
```

**Multiple environments**:
- Use different server URLs for dev/staging/prod
- Configure via properties for flexibility
- Document actual API endpoints

From API Gateway pattern (microservices architecture):

**Single entry point**:
- All external traffic goes through API Gateway
- Microservices not directly accessible
- Documentation should reflect gateway URLs

**Benefits**:
- Centralized authentication
- Rate limiting
- Request/response logging
- SSL termination

### Implementation Standard

**OpenAPIConfig.java** (Required for all services):
```java
package com.sms.{service}.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${openapi.server.url:http://localhost:8080}")
    private String serverUrl;

    @Value("${openapi.server.description:API Gateway}")
    private String serverDescription;

    @Bean
    public OpenAPI {serviceName}API() {
        Server server = new Server();
        server.setUrl(serverUrl);
        server.setDescription(serverDescription);

        return new OpenAPI()
                .servers(List.of(server))
                .info(new Info()
                        .title("{Service Name} API")
                        .description("API documentation for {service name}")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearer-jwt"));
    }
}
```

**application.yml** (Default - local development):
```yaml
# OpenAPI Configuration
openapi:
  server:
    url: http://localhost:8080
    description: Local API Gateway

# springdoc configuration
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
```

**application-docker.yml** (Docker environment):
```yaml
# OpenAPI Configuration
openapi:
  server:
    url: ${API_GATEWAY_URL:http://localhost:8080}
    description: API Gateway
```

**Production Configuration** (environment variable):
```yaml
# docker-compose.yml (production)
environment:
  - API_GATEWAY_URL=https://api.sms.com
```

**Access URLs**:
```
# Development
Swagger UI:  http://localhost:8080/swagger-ui.html
OpenAPI Doc: http://localhost:8080/v3/api-docs

# Production
Swagger UI:  https://api.sms.com/swagger-ui.html
OpenAPI Doc: https://api.sms.com/v3/api-docs
```

**Verification Checklist**:
- [ ] OpenAPI server URL points to API Gateway (port 8080)
- [ ] Swagger UI "Try it out" works without CORS errors
- [ ] Requests route through gateway (check gateway logs)
- [ ] JWT authentication works through Swagger UI
- [ ] Production configuration uses production gateway URL

**Security Configuration** (Optional but recommended):
```java
@Bean
public OpenAPI secureAPI() {
    return new OpenAPI()
            .servers(List.of(new Server().url("http://localhost:8080")))
            .info(new Info().title("Student API").version("1.0.0"))
            .components(new Components()
                    .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT token from /api/auth/login")))
            .security(List.of(new SecurityRequirement().addList("bearer-jwt")));
}
```

This adds "Authorize" button in Swagger UI for JWT token entry.

---

## 7. Service Template Creation

### Question Being Answered

What is the most efficient and consistent approach to creating new microservices in the Salarean SMS project?

### Research Findings from SERVICE_COMPARISON_ANALYSIS.md

**Current State Analysis**:

**Existing Services**:
- ✅ auth-service - Completed (follows all standards)
- ✅ student-service - Completed (needs migration to standards)
- 📋 attendance-service - Planned
- 📋 grade-service - Planned
- 📋 report-service - Planned
- 📋 notification-service - Planned

**From CLAUDE.md Standards**:
> "When creating NEW services, use auth-service as the template"

> "Standard Process:
> 1. Copy auth-service/ directory structure
> 2. Rename packages from com.sms.auth to com.sms.{newservice}
> 3. Update configuration files (application.yml, pom.xml)
> 4. Verify all standards are met using the checklist"

**Template Service**: auth-service

**Rationale from Analysis**:
- ✅ Cleaner separation of concerns (security package, separate JWT provider)
- ✅ Standard Spring Boot naming (SPRING_DATASOURCE_* environment variables)
- ✅ CORS configuration included (essential for web applications)
- ✅ Proper OpenAPI naming (OpenAPI not OpenApi)
- ✅ Simpler profile structure (default + docker)
- ✅ Better test coverage
- ✅ Dedicated validation package (better organization)

### Decision Made

✅ **Use auth-service as the standard template for all new microservices**

**Template Selection Reasoning**:
1. auth-service follows ALL established standards
2. Includes all required configuration classes
3. Proper package structure with security/ package
4. JWT architecture correctly separated (Filter + Provider)
5. Comprehensive test coverage
6. Production-ready structure

### Template Creation Process

**Step 1: Copy Template Service**
```bash
# Navigate to project root
cd /Volumes/DATA/my-projects/salarean

# Copy auth-service as template
cp -r auth-service {new-service-name}

# Example: Create grade-service
cp -r auth-service grade-service

cd grade-service
```

**Step 2: Rename Packages**
```bash
# Find and replace package names
# macOS/Linux:
find . -type f -name "*.java" -exec sed -i '' 's/com.sms.auth/com.sms.grade/g' {} +

# Windows (Git Bash):
find . -type f -name "*.java" -exec sed -i 's/com.sms.auth/com.sms.grade/g' {} +

# Manually rename physical directories
mv src/main/java/com/sms/auth src/main/java/com/sms/grade
mv src/test/java/com/sms/auth src/test/java/com/sms/grade
```

**Step 3: Update Configuration Files**

**pom.xml**:
```xml
<!-- Update artifact information -->
<artifactId>grade-service</artifactId>
<name>grade-service</name>
<description>Grade Management Service for SMS</description>

<!-- Keep parent and dependencies same as auth-service -->
```

**application.yml**:
```yaml
spring:
  application:
    name: grade-service  # Update service name
  datasource:
    url: jdbc:postgresql://localhost:5434/grade_db  # Update database name and port
    username: sms_user
    password: ${DB_PASSWORD:password}

server:
  port: 8084  # Update service port (8081=auth, 8082=student, 8083=attendance, 8084=grade)

# Update other service-specific properties
```

**application-docker.yml**:
```yaml
eureka:
  instance:
    hostname: grade-service  # Update hostname to match service name
  # Rest stays same
```

**Step 4: Update Domain-Specific Code**

**Remove auth-specific code**:
```bash
# Delete auth-specific model classes
rm src/main/java/com/sms/grade/model/User.java
rm src/main/java/com/sms/grade/model/RefreshToken.java

# Delete auth-specific services
rm src/main/java/com/sms/grade/service/AuthService.java
rm src/main/java/com/sms/grade/service/AuthServiceImpl.java
rm src/main/java/com/sms/grade/service/RefreshTokenService.java
rm src/main/java/com/sms/grade/service/RefreshTokenServiceImpl.java

# Delete auth-specific controllers
rm src/main/java/com/sms/grade/controller/AuthController.java

# Delete auth-specific repositories
rm src/main/java/com/sms/grade/repository/UserRepository.java
rm src/main/java/com/sms/grade/repository/RefreshTokenRepository.java

# Delete auth-specific DTOs
rm src/main/java/com/sms/grade/dto/LoginRequest.java
rm src/main/java/com/sms/grade/dto/RegisterRequest.java
rm src/main/java/com/sms/grade/dto/AuthResponse.java
```

**Keep framework code**:
```bash
# KEEP these directories (framework, not domain-specific):
# - config/             (all config classes)
# - security/           (JWT filter and provider)
# - exception/          (global exception handler)
# - validation/         (if exists)
```

**Add new domain code**:
```java
// Create domain-specific entities
src/main/java/com/sms/grade/model/
├── Grade.java
├── GradeCategory.java
└── GradeType.java (enum)

// Create domain-specific DTOs
src/main/java/com/sms/grade/dto/
├── GradeRequest.java
├── GradeResponse.java
└── GradeUpdateRequest.java

// Create domain-specific services
src/main/java/com/sms/grade/service/
├── GradeService.java
└── GradeServiceImpl.java

// Create domain-specific repositories
src/main/java/com/sms/grade/repository/
└── GradeRepository.java

// Create domain-specific controllers
src/main/java/com/sms/grade/controller/
└── GradeController.java
```

**Step 5: Update Configuration Classes**

**OpenAPIConfig.java**:
```java
@Bean
public OpenAPI gradeServiceAPI() {  // Rename method
    Server server = new Server();
    server.setUrl("http://localhost:8080");  // Keep pointing to gateway
    server.setDescription("API Gateway");

    return new OpenAPI()
            .servers(List.of(server))
            .info(new Info()
                    .title("Grade Service API")  // Update title
                    .description("API for grade management")  // Update description
                    .version("1.0.0"))
            .components(new Components()
                    .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement()
                    .addList("bearer-jwt"));
}
```

**CorsConfig.java**: No changes needed (framework code)

**SecurityConfig.java**:
```java
// Update if service has different security requirements
// Otherwise keep same (JWT authentication for all endpoints)

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/grades/public/**").permitAll()  // Update path
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

**Remove service-specific configs**:
```java
// Delete if not needed for new service
rm src/main/java/com/sms/grade/config/RedisConfig.java  // Only auth-service uses Redis
rm src/main/java/com/sms/grade/config/PasswordEncoderConfig.java  // Only auth-service hashes passwords
```

**Step 6: Update Docker Configuration**

**Create docker-compose service entry**:
```yaml
# docker-compose.yml
services:
  grade-service:
    build:
      context: ./grade-service
      dockerfile: Dockerfile
    container_name: grade-service
    ports:
      - "8084:8084"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-grade:5432/grade_db
      - SPRING_DATASOURCE_USERNAME=sms_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - JWT_SECRET=${JWT_SECRET}
    networks:
      - backend-network
      - database-network
    depends_on:
      - eureka-server
      - postgres-grade
    restart: unless-stopped

  postgres-grade:
    image: postgres:15
    container_name: postgres-grade
    environment:
      - POSTGRES_DB=grade_db
      - POSTGRES_USER=sms_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres-grade-data:/var/lib/postgresql/data
    networks:
      - database-network
    restart: unless-stopped

volumes:
  postgres-grade-data:
```

**Copy Dockerfile**:
```bash
# Dockerfile already copied from auth-service
# No changes needed (multi-stage build works for all services)
```

**Step 7: Update Tests**

**Delete auth-specific tests**:
```bash
rm -rf src/test/java/com/sms/grade/controller/
rm -rf src/test/java/com/sms/grade/service/
```

**Keep framework tests** (if applicable):
```bash
# KEEP (if they test framework components):
# - SecurityConfigTest.java
# - JwtTokenProviderTest.java
# - JwtAuthenticationFilterTest.java
```

**Add domain-specific tests**:
```java
src/test/java/com/sms/grade/
├── controller/
│   └── GradeControllerTest.java
├── service/
│   └── GradeServiceImplTest.java
└── repository/
    └── GradeRepositoryTest.java
```

**Step 8: Verify Standards Checklist**

Run through standardization checklist from SERVICE_COMPARISON_ANALYSIS.md:

**Structure**:
- [ ] Exactly 2 profiles (default, docker)
- [ ] Package structure matches standard (model/, security/, service/)
- [ ] JWT split into Filter + Provider in security/ package
- [ ] All required config classes present (CORS, OpenAPI, Security)

**Configuration**:
- [ ] Environment variables use Spring Boot standard names (SPRING_DATASOURCE_*, etc.)
- [ ] Eureka configured with `prefer-ip-address: false`
- [ ] OpenAPI points to API Gateway (port 8080)
- [ ] JWT secret default matches other services

**Code Quality**:
- [ ] Service implementations in `service/` (not `service/impl/`)
- [ ] Entity package named `model/` (not `entity/`)
- [ ] OpenAPI config named `OpenAPIConfig` (not `OpenApiConfig`)
- [ ] CORS configuration included

**Docker**:
- [ ] docker-compose.yml uses `SPRING_PROFILES_ACTIVE=docker`
- [ ] All environment variables follow standard naming
- [ ] JWT_SECRET environment variable included

**Step 9: Build and Test**

```bash
# Build the service
mvn clean package

# Build Docker image
docker-compose build grade-service

# Start service with dependencies
docker-compose up eureka-server postgres-grade grade-service

# Verify Eureka registration
curl http://localhost:8761/eureka/apps/GRADE-SERVICE

# Test API through gateway
curl -H "Authorization: Bearer {JWT_TOKEN}" http://localhost:8080/api/grades

# Check Swagger UI
open http://localhost:8080/swagger-ui.html
```

### Rationale

**Why use auth-service as template?**

1. **Completeness**: auth-service has all required components
   - Security configuration
   - JWT authentication
   - Database integration
   - Eureka registration
   - OpenAPI documentation
   - CORS configuration
   - Exception handling
   - Validation

2. **Standards compliance**: Follows all established standards
   - 2 profiles (default, docker)
   - Standard environment variable naming
   - Correct package structure
   - Separated JWT architecture
   - Proper OpenAPI naming

3. **Production-ready**: Includes best practices
   - Comprehensive error handling
   - Security hardening
   - Monitoring endpoints (Actuator)
   - Health checks
   - Logging configuration

4. **Test coverage**: Includes test structure
   - Unit tests
   - Integration tests
   - Test configuration (application-test.yml)

5. **Consistency**: All services will have same foundation
   - Same security model
   - Same configuration pattern
   - Same error handling
   - Easier onboarding for new developers

**Why NOT start from scratch?**

1. **Time-consuming**: Setting up Spring Boot, security, Eureka, OpenAPI takes hours
2. **Error-prone**: Easy to miss configuration, forget CORS, misconfigure Eureka
3. **Inconsistent**: Each service might have different patterns
4. **Missing components**: Might forget validation, exception handling, etc.

**Why NOT use student-service as template?**

1. ❌ Has 4 profiles (inconsistent)
2. ❌ Uses custom environment variable names
3. ❌ JWT in config/ package (wrong location)
4. ❌ Missing CORS configuration
5. ❌ Service implementation in service/impl/ (outdated pattern)
6. ❌ Entity package named entity/ (inconsistent)

### Alternatives Considered and Why Rejected

**Alternative 1: Spring Initializr for each service**
- ❌ **Rejected**: Each service would have different base configuration
- ❌ **Rejected**: Need to manually add all security, Eureka, OpenAPI setup
- ❌ **Rejected**: No consistency guarantee across services
- ⚠️ **Use case**: Only for very different service types (e.g., batch processing service)

**Alternative 2: Maven archetype**
```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.sms \
  -DarchetypeArtifactId=sms-service-archetype
```
- ✅ **Possible**: Create custom Maven archetype from auth-service
- ⚠️ **Overhead**: Need to maintain archetype separately
- ⚠️ **Flexibility**: Template copying allows manual adjustments
- 📋 **Future**: Consider if creating 10+ services

**Alternative 3: Use Spring Boot features to share common code**
```xml
<!-- Common library approach -->
<dependency>
    <groupId>com.sms</groupId>
    <artifactId>sms-common</artifactId>
    <version>1.0.0</version>
</dependency>
```
- ✅ **Complementary**: Can use alongside template approach
- ✅ **Benefits**: Share JWT, exception handling, common DTOs
- ⚠️ **Complexity**: Adds dependency management overhead
- 📋 **Recommendation**: Implement after 3-4 services to extract common patterns

**Alternative 4: Gradle multi-project build**
```groovy
// settings.gradle
include 'auth-service', 'student-service', 'grade-service'
```
- ✅ **Possible**: Manage all services in single Gradle project
- ⚠️ **Coupling**: Services become more tightly coupled
- ⚠️ **Build time**: Building one service triggers others
- ❌ **Rejected**: Violates microservice independence principle

### Spring Boot Best Practice References

From Spring Boot documentation:

**Service templates**:
- Use consistent base configuration across services
- Extract common patterns into shared libraries
- Maintain service independence while sharing patterns

From Microservices patterns (Sam Newman, Martin Fowler):

**Template services**:
- Establish template service for consistency
- Clone and modify for new services
- Balance between consistency and service autonomy

**Shared libraries**:
- Extract truly common code into libraries
- Avoid over-sharing (creates coupling)
- Use for cross-cutting concerns (security, logging)

### Implementation Standard

**Service Creation Workflow**:

1. **Clone Template**
   ```bash
   cp -r auth-service {new-service-name}
   cd {new-service-name}
   ```

2. **Rename Packages**
   ```bash
   find . -type f -name "*.java" -exec sed -i '' 's/com.sms.auth/com.sms.{service}/g' {} +
   mv src/main/java/com/sms/auth src/main/java/com/sms/{service}
   mv src/test/java/com/sms/auth src/test/java/com/sms/{service}
   ```

3. **Update Configuration**
   - pom.xml: artifactId, name, description
   - application.yml: service name, port, database
   - application-docker.yml: Eureka hostname
   - OpenAPIConfig.java: API title and description

4. **Remove Auth-Specific Code**
   - Delete User, RefreshToken models
   - Delete AuthService, RefreshTokenService
   - Delete AuthController
   - Delete auth repositories
   - Delete auth DTOs
   - Remove RedisConfig (if not needed)
   - Remove PasswordEncoderConfig (if not needed)

5. **Keep Framework Code**
   - security/ package (JWT filter and provider)
   - config/ package (CORS, OpenAPI, Security)
   - exception/ package (global exception handling)
   - validation/ package (if exists)

6. **Add Domain Code**
   - Create domain entities in model/
   - Create DTOs in dto/
   - Create services in service/
   - Create repositories in repository/
   - Create controllers in controller/

7. **Update Docker**
   - Add service to docker-compose.yml
   - Add database service (postgres-{service})
   - Add volume for database
   - Configure environment variables

8. **Verify Standards**
   - Run through standardization checklist
   - Ensure all required components present
   - Verify naming conventions

9. **Build and Test**
   - mvn clean package
   - docker-compose build
   - docker-compose up
   - Test Swagger UI
   - Verify Eureka registration

**Service Naming Convention**:
- Service Name: `{domain}-service` (e.g., grade-service, attendance-service)
- Package: `com.sms.{domain}` (e.g., com.sms.grade, com.sms.attendance)
- Eureka Instance: `{domain}-service` (e.g., grade-service, attendance-service)
- Database: `{domain}_db` (e.g., grade_db, attendance_db)
- Docker Container: `{domain}-service` (e.g., grade-service, attendance-service)
- Port: Sequential (8081=auth, 8082=student, 8083=attendance, 8084=grade, etc.)

**Template Service Structure** (auth-service):
```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/sms/auth/
│   │   │   ├── config/           ← KEEP ALL
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── OpenAPIConfig.java
│   │   │   │   ├── PasswordEncoderConfig.java  ← DELETE if not needed
│   │   │   │   ├── RedisConfig.java            ← DELETE if not needed
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/       ← DELETE ALL, add domain controllers
│   │   │   ├── dto/              ← DELETE ALL, add domain DTOs
│   │   │   ├── exception/        ← KEEP ALL (framework code)
│   │   │   ├── model/            ← DELETE ALL, add domain entities
│   │   │   ├── repository/       ← DELETE ALL, add domain repositories
│   │   │   ├── security/         ← KEEP ALL (JWT framework)
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtTokenProvider.java
│   │   │   ├── service/          ← DELETE ALL, add domain services
│   │   │   └── validation/       ← KEEP structure, add domain validators
│   │   └── resources/
│   │       ├── application.yml              ← UPDATE service name, port
│   │       ├── application-docker.yml       ← UPDATE hostname
│   │       └── application-test.yml         ← KEEP
│   └── test/                     ← DELETE domain tests, KEEP framework tests
├── Dockerfile                    ← KEEP as-is
└── pom.xml                       ← UPDATE artifactId, name, description
```

**Maintenance**:
- When standards change: Update template (auth-service) first
- Propagate changes: Update other services incrementally
- Document deviations: If a service needs different config, document why

---

## 8. Implementation Recommendations

### Summary of Key Decisions

Based on comprehensive analysis of SERVICE_COMPARISON_ANALYSIS.md and Spring Boot best practices, the following standards are established for all microservices in the Salarean SMS project:

| Aspect | Standard | Rationale |
|--------|----------|-----------|
| **Spring Profiles** | Exactly 2 profiles: `default` (local), `docker` (containerized) | Simplicity, clear separation, no env-specific profiles |
| **Environment Variables** | Spring Boot standard names: `SPRING_DATASOURCE_*`, `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Automatic binding, self-documenting, IDE support |
| **Eureka Registration** | `prefer-ip-address: false` (hostname-based) | Multi-network compatibility, Docker DNS, stability |
| **Package Structure** | `model/`, `security/`, flat `service/`, `OpenAPIConfig` | DDD alignment, SRP, modern Spring practices |
| **JWT Architecture** | Separated: `JwtAuthenticationFilter` + `JwtTokenProvider` | SRP, reusability, testability, maintainability |
| **OpenAPI Server URL** | Point to API Gateway (`http://localhost:8080`) | Prevents CORS, reflects actual usage, consistent security |
| **Service Template** | Use auth-service as template for new services | Completeness, standards compliance, consistency |

### Migration Strategy

**For Existing Services** (student-service):

**Phase 1: Critical (Immediate)** - Already completed:
- ✅ JWT_SECRET environment variable standardized

**Phase 2: Deferred Until After Core Features**:
- Profile consolidation (4 profiles → 2 profiles)
- Environment variable naming (DB_* → SPRING_DATASOURCE_*)
- Package restructuring (entity/ → model/, create security/)
- JWT class separation (create JwtTokenProvider)
- Add missing configurations (CorsConfig)

**Rationale for Deferral**:
- Current services work in development
- Avoid disruption during feature development
- Apply all changes at once when ready (not gradual)
- Focus on delivering core functionality first

**For New Services** (attendance, grade, report, notification):

**Mandatory**: Follow ALL standards from day 1
- Use auth-service as template
- Verify against standardization checklist
- No deviations without documented justification

### Priority Classification

**HIGH PRIORITY** (Affects runtime and functionality):
1. ✅ Environment variable naming (for new services)
2. ✅ Eureka `prefer-ip-address: false` (for new services)
3. ✅ OpenAPI server URL pointing to gateway (for new services)
4. ✅ Spring profile strategy (2 profiles only)

**MEDIUM PRIORITY** (Affects maintainability):
5. ✅ Package structure standardization (for new services)
6. ✅ JWT architecture separation (for new services)
7. ✅ Service template usage (for new services)

**LOW PRIORITY** (Affects consistency):
8. ✅ OpenAPI naming convention (for new services)
9. ✅ CORS configuration inclusion (for new services)
10. Validation package creation (only if needed)

### Enforcement Rules for Claude AI

**When creating new services**:
1. ✅ **MUST** use auth-service as template
2. ✅ **MUST** follow all package structure requirements
3. ✅ **MUST** use Spring Boot standard environment variable names
4. ✅ **MUST** separate JWT into Filter + Provider
5. ✅ **MUST** include all required configuration classes
6. ✅ **MUST** point OpenAPI to API Gateway
7. ✅ **MUST** verify against standardization checklist

**When modifying existing services**:
1. ✅ **SHOULD** suggest improvements if code violates standards
2. ✅ **MUST** follow standards for any new code added
3. ✅ **MUST NOT** break existing functionality during modifications
4. ✅ **SHOULD** document deviations from standards with rationale

**When user asks to create microservice-related code**:
1. ✅ **MUST** check SERVICE_COMPARISON_ANALYSIS.md for detailed specifications
2. ✅ **MUST** verify against the checklist before considering work complete
3. ✅ **MUST** refuse to create non-standard structures unless explicitly overridden by user

### Standardization Checklist

Use this checklist when creating or auditing microservices:

**Structure Checklist**:
- [ ] Exactly 2 profiles (application.yml, application-docker.yml)
- [ ] Package structure: model/, security/, service/, config/, controller/, dto/, exception/, repository/
- [ ] JWT split into JwtAuthenticationFilter + JwtTokenProvider in security/ package
- [ ] All required config classes present: CorsConfig, OpenAPIConfig, SecurityConfig

**Configuration Checklist**:
- [ ] Environment variables use Spring Boot standard names (SPRING_DATASOURCE_USERNAME, not DB_USERNAME)
- [ ] Eureka configured with `prefer-ip-address: false` in application-docker.yml
- [ ] Eureka instance config in YAML, not environment variables
- [ ] OpenAPI server URL points to API Gateway (http://localhost:8080)
- [ ] JWT secret default value matches other services
- [ ] Docker uses `SPRING_PROFILES_ACTIVE=docker`

**Code Quality Checklist**:
- [ ] Service implementations in `service/` package (not `service/impl/`)
- [ ] Entity package named `model/` (not `entity/`)
- [ ] OpenAPI config class named `OpenAPIConfig` (not `OpenApiConfig`)
- [ ] CORS configuration included
- [ ] Global exception handler present
- [ ] Validation configured (Bean Validation)

**Docker Checklist**:
- [ ] docker-compose.yml service entry created
- [ ] Environment variables follow standard naming
- [ ] Service connected to backend-network and database-network
- [ ] Database service created (postgres-{service})
- [ ] Volume for database persistence
- [ ] Health checks configured
- [ ] Depends on Eureka server

**Security Checklist**:
- [ ] JWT authentication configured
- [ ] SecurityConfig properly configured
- [ ] CORS origins properly restricted
- [ ] No sensitive data in configuration files
- [ ] Secrets externalized to environment variables
- [ ] OpenAPI security scheme configured

**Testing Checklist**:
- [ ] Unit tests for services
- [ ] Integration tests for controllers
- [ ] Repository tests (if complex queries)
- [ ] JWT token provider tests
- [ ] Test profile configuration (application-test.yml)

### Benefits of Standardization

**For Developers**:
- ✅ Faster onboarding (consistent structure across services)
- ✅ Easier navigation (know where to find code)
- ✅ Reduced cognitive load (same patterns everywhere)
- ✅ Better productivity (can reuse knowledge across services)
- ✅ Clearer code reviews (deviations are obvious)

**For Project**:
- ✅ Reduced bugs (consistent patterns reduce errors)
- ✅ Better maintainability (changes apply uniformly)
- ✅ Simplified deployment (same config pattern)
- ✅ Code reusability (can share components)
- ✅ Professional quality (follows industry best practices)

**For Operations**:
- ✅ Consistent monitoring (same actuator endpoints)
- ✅ Uniform logging (same log format)
- ✅ Predictable behavior (same configuration patterns)
- ✅ Easier troubleshooting (know where to look)

### Future Considerations

**After 3-4 services are created**:
1. **Extract common library**: Create `sms-common` module
   - JWT security components (can be shared)
   - Global exception handling (same across services)
   - Common DTOs (e.g., ErrorResponse, PageResponse)
   - Validation utilities
   - OpenAPI configuration base

2. **Create Maven archetype**: Automate template creation
   ```bash
   mvn archetype:generate \
     -DarchetypeGroupId=com.sms \
     -DarchetypeArtifactId=sms-service-archetype
   ```

3. **Implement service mesh**: If scaling requires it
   - Istio or Linkerd for traffic management
   - Distributed tracing (Jaeger)
   - Circuit breakers (Resilience4j)

4. **Migrate to Kubernetes**: If Docker Compose becomes limiting
   - Helm charts for service deployment
   - ConfigMaps for configuration
   - Secrets for sensitive data
   - Ingress for API Gateway

**Continuous Improvement**:
- Review standards every 6 months
- Update template service with improvements
- Document new patterns as they emerge
- Gather feedback from development team

---

## Conclusion

This research document establishes comprehensive Spring Boot microservice standards for the Salarean SMS project based on detailed analysis of existing services (auth-service vs student-service) documented in SERVICE_COMPARISON_ANALYSIS.md.

**Key Takeaways**:

1. **auth-service is the template**: All new services must follow auth-service patterns
2. **Consistency is critical**: Same structure, naming, configuration across all services
3. **Standards prevent problems**: Proactive standardization avoids CORS errors, multi-network issues, and maintainability problems
4. **Deferral strategy**: Existing services can be migrated later; new services must comply immediately
5. **Documentation is essential**: This research document and SERVICE_COMPARISON_ANALYSIS.md are the source of truth

**Next Steps**:

1. ✅ Use this document as reference when creating new services
2. ✅ Verify all new services against standardization checklist
3. ✅ Document any deviations with justification
4. 📋 Plan migration of student-service after core features complete
5. 📋 Extract common code into shared library after 3-4 services

**Status**: This research document is now the official reference for Spring Boot best practices in the Salarean SMS project.

**Last Updated**: 2025-11-22

---

## References

1. **SERVICE_COMPARISON_ANALYSIS.md** - Primary source document analyzing auth-service vs student-service
2. **CLAUDE.md** - Project-specific development guidelines and microservice architecture standards
3. **Spring Boot Reference Documentation** (v3.5.7) - Official Spring Boot documentation
4. **Spring Cloud Netflix Eureka Documentation** - Service discovery best practices
5. **springdoc-openapi Documentation** - OpenAPI/Swagger configuration
6. **Docker Networking Documentation** - Container networking and DNS resolution
7. **Clean Code** by Robert C. Martin - Software design principles
8. **Building Microservices** by Sam Newman - Microservice architecture patterns

---

**Document Metadata**:
- **File Path**: /Volumes/DATA/my-projects/salarean/specs/001-service-standards/research.md
- **Created**: 2025-11-22
- **Last Modified**: 2025-11-22
- **Author**: Generated from SERVICE_COMPARISON_ANALYSIS.md
- **Status**: Official Reference Document
