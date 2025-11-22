# Microservice Creation Quickstart Guide

**Version**: 1.0.0
**Date**: 2025-11-22
**Template Service**: auth-service
**Purpose**: Step-by-step guide for creating standardized microservices

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Quick Start (5 Minutes)](#quick-start-5-minutes)
3. [Detailed Setup](#detailed-setup)
4. [Validation](#validation)
5. [Testing](#testing)
6. [Troubleshooting](#troubleshooting)
7. [Next Steps](#next-steps)
8. [Reference](#reference)

---

## Prerequisites

### Required Tools

Before creating a new microservice, ensure you have:

**Development Environment**:
- ✅ Java 21 (JDK)
- ✅ Maven 3.8+
- ✅ Docker Desktop (or Docker Engine + Docker Compose)
- ✅ Git
- ✅ Text editor or IDE (IntelliJ IDEA, VS Code, etc.)

**Project Access**:
- ✅ Clone of Salarean repository
- ✅ Access to `auth-service` (the template)
- ✅ Docker Compose environment set up

**Verify Installation**:

```bash
# Check Java version
java -version
# Expected: openjdk version "21.x.x"

# Check Maven
mvn -version
# Expected: Apache Maven 3.8.x or higher

# Check Docker
docker --version
docker-compose --version

# Navigate to project
cd /path/to/salarean

# Verify auth-service exists (template)
ls -la auth-service/
```

### Knowledge Requirements

**You should understand**:
- Basic Spring Boot concepts
- RESTful API principles
- Docker and containerization basics
- Maven project structure
- Git version control

**Nice to have**:
- Spring Security fundamentals
- JWT authentication
- Microservice architecture patterns
- PostgreSQL basics

---

## Quick Start (5 Minutes)

**Goal**: Create a working microservice from the template in 5 minutes.

### Step 1: Copy Template (30 seconds)

```bash
# Navigate to project root
cd /path/to/salarean

# Copy auth-service as template
# Replace 'attendance' with your service name (use lowercase, hyphen-separated)
cp -r auth-service attendance-service

# Navigate to new service
cd attendance-service
```

**Service Naming Convention**:
- Use lowercase with hyphens: `attendance-service`, `grade-service`
- Avoid underscores: ❌ `attendance_service`
- Keep it concise: ❌ `student-attendance-management-service`

---

### Step 2: Rename Package References (1 minute)

```bash
# Automated package renaming (macOS/BSD sed)
find . -type f \( -name "*.java" -o -name "*.yml" -o -name "*.xml" \) \
  -exec sed -i '' 's/auth-service/attendance-service/g' {} +

find . -type f \( -name "*.java" -o -name "*.yml" -o -name "*.xml" \) \
  -exec sed -i '' 's/com\.sms\.auth/com.sms.attendance/g' {} +

# For Linux (GNU sed)
find . -type f \( -name "*.java" -o -name "*.yml" -o -name "*.xml" \) \
  -exec sed -i 's/auth-service/attendance-service/g' {} +

find . -type f \( -name "*.java" -o -name "*.yml" -o -name "*.xml" \) \
  -exec sed -i 's/com\.sms\.auth/com.sms.attendance/g' {} +
```

**What this does**:
- Replaces all occurrences of `auth-service` with `attendance-service`
- Updates package names from `com.sms.auth` to `com.sms.attendance`
- Processes Java files, YAML configs, and Maven POM

---

### Step 3: Rename Directory Structure (30 seconds)

```bash
# Rename Java package directories
cd src/main/java/com/sms
mv auth attendance
cd ../../../../../

cd src/test/java/com/sms
mv auth attendance
cd ../../../../../
```

**Directory tree should now look like**:
```
attendance-service/
├── src/main/java/com/sms/attendance/  ✅
├── src/test/java/com/sms/attendance/  ✅
```

---

### Step 4: Update Service-Specific Configurations (1 minute)

**Choose a unique port** for your service:
- 8081: auth-service (reserved)
- 8082: student-service (reserved)
- 8083: attendance-service ← Use this for our example
- 8084: grade-service
- 8085: report-service
- etc.

```bash
# Update port in application.yml
sed -i '' 's/8081/8083/g' src/main/resources/application.yml

# For Linux
sed -i 's/8081/8083/g' src/main/resources/application.yml
```

**Manually verify** `src/main/resources/application.yml`:
```yaml
spring:
  application:
    name: attendance-service  # ✅ Updated
server:
  port: 8083  # ✅ Updated to unique port
```

**Manually verify** `src/main/resources/application-docker.yml`:
```yaml
eureka:
  instance:
    hostname: attendance-service  # ✅ Updated
```

**Manually verify** `pom.xml`:
```xml
<artifactId>attendance-service</artifactId>
<name>attendance-service</name>
<description>Attendance Management Service</description>
```

---

### Step 5: Rename Main Application Class (1 minute)

```bash
# Rename main application file
cd src/main/java/com/sms/attendance
mv AuthServiceApplication.java AttendanceServiceApplication.java
cd ../../../../../

# Update class name inside the file
sed -i '' 's/AuthServiceApplication/AttendanceServiceApplication/g' \
  src/main/java/com/sms/attendance/AttendanceServiceApplication.java

# For Linux
sed -i 's/AuthServiceApplication/AttendanceServiceApplication/g' \
  src/main/java/com/sms/attendance/AttendanceServiceApplication.java
```

**Verify the file** `src/main/java/com/sms/attendance/AttendanceServiceApplication.java`:
```java
package com.sms.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class AttendanceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AttendanceServiceApplication.class, args);
    }
}
```

---

### Step 6: Build and Verify (1 minute)

```bash
# Build the service
mvn clean package -DskipTests

# Expected output:
# [INFO] BUILD SUCCESS
# [INFO] Total time: 15-30s
```

**If build fails**:
- Check Java version: `java -version` (must be 21)
- Check Maven version: `mvn -version`
- See [Troubleshooting](#troubleshooting) section

---

### Step 7: Quick Local Test (30 seconds)

```bash
# Run locally (Ctrl+C to stop)
mvn spring-boot:run
```

**Expected console output**:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v3.5.7)

...Started AttendanceServiceApplication in X.XXX seconds
...Tomcat started on port(s): 8083 (http)
...Eureka client disabled (local dev mode)
```

**Test the service**:
```bash
# In a new terminal, test health endpoint
curl http://localhost:8083/actuator/health

# Expected response:
{"status":"UP"}
```

Press `Ctrl+C` to stop the service.

---

### Congratulations!

You now have a working microservice based on the standardized template. The service:
- ✅ Follows package structure standards
- ✅ Has correct Spring profiles (default, docker)
- ✅ Uses standard environment variable naming
- ✅ Includes JWT architecture (filter + provider)
- ✅ Has all required configuration classes

**Next steps**: [Validation](#validation) to ensure 100% compliance.

---

## Detailed Setup

This section provides a comprehensive walkthrough with explanations.

### Understanding the Template Service

**Template**: `auth-service`

The auth-service is the canonical reference implementation. It includes:

1. **Package Structure**:
   - `config/` - Configuration classes (CORS, Security, OpenAPI)
   - `controller/` - REST endpoints
   - `dto/` - Request/Response objects
   - `exception/` - Error handling
   - `model/` - JPA entities (NOTE: not `entity/`)
   - `repository/` - Data access layer
   - `security/` - JWT authentication
   - `service/` - Business logic
   - `validation/` - Custom validators

2. **Configuration Files**:
   - `application.yml` - Local development
   - `application-docker.yml` - Docker deployment

3. **JWT Architecture**:
   - `JwtAuthenticationFilter.java` - HTTP filter
   - `JwtTokenProvider.java` - Token operations

---

### Step-by-Step Service Creation

#### 1. Copy and Initial Setup

**Create service directory**:
```bash
cd /path/to/salarean

# Choose your service name
export SERVICE_NAME="attendance-service"
export SERVICE_PORT="8083"

# Copy template
cp -r auth-service $SERVICE_NAME
cd $SERVICE_NAME
```

**What files were copied**:
```
attendance-service/
├── Dockerfile              # Container build
├── pom.xml                 # Maven dependencies
├── src/
│   ├── main/
│   │   ├── java/           # Source code
│   │   └── resources/      # Configuration files
│   └── test/               # Unit tests
└── README.md               # Documentation
```

---

#### 2. Package Renaming

**Why we need to rename**:
- Java packages must match directory structure
- Service names must be unique in Eureka
- Configuration references package names

**Automated renaming** (recommended):

```bash
# Define variables
OLD_SERVICE="auth"
NEW_SERVICE="attendance"

# Rename in all Java files
find . -type f -name "*.java" \
  -exec sed -i '' "s/com\.sms\.$OLD_SERVICE/com.sms.$NEW_SERVICE/g" {} +

# Rename in configuration files
find . -type f \( -name "*.yml" -o -name "*.xml" \) \
  -exec sed -i '' "s/$OLD_SERVICE-service/$NEW_SERVICE-service/g" {} +
```

**Manual verification checklist**:
- [ ] `pom.xml` - Check `<artifactId>`, `<name>`, `<description>`
- [ ] `application.yml` - Check `spring.application.name`
- [ ] `application-docker.yml` - Check `eureka.instance.hostname`
- [ ] All `*.java` files - Check `package` declarations

---

#### 3. Directory Structure Renaming

**Rename package directories**:

```bash
# Main source code
mv src/main/java/com/sms/auth src/main/java/com/sms/attendance

# Test code
mv src/test/java/com/sms/auth src/test/java/com/sms/attendance

# Verify structure
tree src/main/java/com/sms/
```

**Expected output**:
```
src/main/java/com/sms/
└── attendance/
    ├── config/
    ├── controller/
    ├── dto/
    ├── exception/
    ├── model/
    ├── repository/
    ├── security/
    ├── service/
    ├── validation/
    └── AttendanceServiceApplication.java
```

---

#### 4. Port Assignment

**Why unique ports matter**:
- Prevents conflicts when running multiple services locally
- Enables direct service access during development
- Simplifies debugging (know which service is on which port)

**Port allocation strategy**:
| Service | Port | Status |
|---------|------|--------|
| API Gateway | 8080 | Reserved |
| Auth Service | 8081 | Reserved |
| Student Service | 8082 | Reserved |
| Attendance Service | 8083 | Available ← Use this |
| Grade Service | 8084 | Available |
| Report Service | 8085 | Available |
| Notification Service | 8086 | Available |
| Eureka Server | 8761 | Reserved |

**Update port in application.yml**:
```yaml
server:
  port: 8083  # Your chosen port
```

---

#### 5. Remove Auth-Specific Code

**What to keep**:
- ✅ `JwtAuthenticationFilter.java` - Needed for JWT validation
- ✅ `JwtTokenProvider.java` - Needed for JWT validation
- ✅ `SecurityConfig.java` - Needed for API security
- ✅ `CorsConfig.java` - Needed for cross-origin requests
- ✅ `OpenAPIConfig.java` - Needed for Swagger documentation

**What to remove** (auth-specific):
```bash
# Remove auth-specific controllers
rm src/main/java/com/sms/attendance/controller/AuthController.java

# Remove auth-specific services
rm src/main/java/com/sms/attendance/service/AuthService.java
rm src/main/java/com/sms/attendance/service/AuthServiceImpl.java

# Remove auth-specific entities
rm src/main/java/com/sms/attendance/model/RefreshToken.java

# Remove auth-specific repositories
rm src/main/java/com/sms/attendance/repository/RefreshTokenRepository.java

# Remove auth-specific DTOs
rm -rf src/main/java/com/sms/attendance/dto/request/*
rm -rf src/main/java/com/sms/attendance/dto/response/*

# Remove Redis config if not using caching
rm src/main/java/com/sms/attendance/config/RedisConfig.java
rm src/main/java/com/sms/attendance/config/PasswordEncoderConfig.java
```

**Keep directory structure intact**:
```bash
# Ensure empty directories exist
mkdir -p src/main/java/com/sms/attendance/controller
mkdir -p src/main/java/com/sms/attendance/dto/request
mkdir -p src/main/java/com/sms/attendance/dto/response
mkdir -p src/main/java/com/sms/attendance/model
mkdir -p src/main/java/com/sms/attendance/repository
mkdir -p src/main/java/com/sms/attendance/service
```

---

#### 6. Add Domain-Specific Code

**Example: Attendance Service**

**Create Entity** (`model/Attendance.java`):
```java
package com.sms.attendance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    private String notes;

    public enum AttendanceStatus {
        PRESENT, ABSENT, LATE, EXCUSED
    }
}
```

**Create Repository** (`repository/AttendanceRepository.java`):
```java
package com.sms.attendance.repository;

import com.sms.attendance.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudentId(Long studentId);

    List<Attendance> findByDate(LocalDate date);

    List<Attendance> findByStudentIdAndDateBetween(
        Long studentId, LocalDate startDate, LocalDate endDate);
}
```

**Create DTOs**:

`dto/request/CreateAttendanceRequest.java`:
```java
package com.sms.attendance.dto.request;

import com.sms.attendance.model.Attendance.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateAttendanceRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Check-in time is required")
    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    @NotNull(message = "Status is required")
    private AttendanceStatus status;

    private String notes;
}
```

`dto/response/AttendanceResponse.java`:
```java
package com.sms.attendance.dto.response;

import com.sms.attendance.model.Attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class AttendanceResponse {
    private Long id;
    private Long studentId;
    private LocalDate date;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private AttendanceStatus status;
    private String notes;
}
```

`dto/response/ApiResponse.java` (standard response wrapper):
```java
package com.sms.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String errorCode;  // "SUCCESS" or error code
    private T data;            // Response payload or null
}
```

**Create Service**:

`service/AttendanceService.java`:
```java
package com.sms.attendance.service;

import com.sms.attendance.dto.request.CreateAttendanceRequest;
import com.sms.attendance.dto.response.AttendanceResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    AttendanceResponse createAttendance(CreateAttendanceRequest request);

    AttendanceResponse getAttendanceById(Long id);

    List<AttendanceResponse> getAttendanceByStudent(Long studentId);

    List<AttendanceResponse> getAttendanceByDate(LocalDate date);

    void deleteAttendance(Long id);
}
```

`service/AttendanceServiceImpl.java`:
```java
package com.sms.attendance.service;

import com.sms.attendance.dto.request.CreateAttendanceRequest;
import com.sms.attendance.dto.response.AttendanceResponse;
import com.sms.attendance.exception.AttendanceNotFoundException;
import com.sms.attendance.model.Attendance;
import com.sms.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;

    @Override
    @Transactional
    public AttendanceResponse createAttendance(CreateAttendanceRequest request) {
        Attendance attendance = new Attendance();
        attendance.setStudentId(request.getStudentId());
        attendance.setDate(request.getDate());
        attendance.setCheckInTime(request.getCheckInTime());
        attendance.setCheckOutTime(request.getCheckOutTime());
        attendance.setStatus(request.getStatus());
        attendance.setNotes(request.getNotes());

        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved);
    }

    @Override
    public AttendanceResponse getAttendanceById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
            .orElseThrow(() -> new AttendanceNotFoundException(
                "Attendance not found with ID: " + id));
        return mapToResponse(attendance);
    }

    @Override
    public List<AttendanceResponse> getAttendanceByStudent(Long studentId) {
        return attendanceRepository.findByStudentId(studentId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceResponse> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAttendance(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new AttendanceNotFoundException(
                "Attendance not found with ID: " + id);
        }
        attendanceRepository.deleteById(id);
    }

    private AttendanceResponse mapToResponse(Attendance attendance) {
        return new AttendanceResponse(
            attendance.getId(),
            attendance.getStudentId(),
            attendance.getDate(),
            attendance.getCheckInTime(),
            attendance.getCheckOutTime(),
            attendance.getStatus(),
            attendance.getNotes()
        );
    }
}
```

**Create Controller** (`controller/AttendanceController.java`):
```java
package com.sms.attendance.controller;

import com.sms.attendance.dto.request.CreateAttendanceRequest;
import com.sms.attendance.dto.response.ApiResponse;
import com.sms.attendance.dto.response.AttendanceResponse;
import com.sms.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Attendance management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @Operation(summary = "Create attendance record")
    public ResponseEntity<ApiResponse<AttendanceResponse>> createAttendance(
            @Valid @RequestBody CreateAttendanceRequest request) {
        AttendanceResponse response = attendanceService.createAttendance(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>("SUCCESS", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get attendance by ID")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceById(
            @PathVariable Long id) {
        AttendanceResponse response = attendanceService.getAttendanceById(id);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get attendance by student ID")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceByStudent(
            @PathVariable Long studentId) {
        List<AttendanceResponse> response =
            attendanceService.getAttendanceByStudent(studentId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get attendance by date")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        List<AttendanceResponse> response = attendanceService.getAttendanceByDate(date);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete attendance record")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", null));
    }
}
```

**Create Exception** (`exception/AttendanceNotFoundException.java`):
```java
package com.sms.attendance.exception;

public class AttendanceNotFoundException extends RuntimeException {
    public AttendanceNotFoundException(String message) {
        super(message);
    }
}
```

**Update GlobalExceptionHandler** (`exception/GlobalExceptionHandler.java`):
```java
package com.sms.attendance.exception;

import com.sms.attendance.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AttendanceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAttendanceNotFound(
            AttendanceNotFoundException ex) {
        log.error("Attendance not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse<>("ATTENDANCE_NOT_FOUND", null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Validation errors: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse<>("VALIDATION_ERROR", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponse<>("INTERNAL_ERROR", null));
    }
}
```

---

#### 7. Update SecurityConfig

**Modify** `config/SecurityConfig.java` to allow public endpoints:

```java
package com.sms.attendance.config;

import com.sms.attendance.security.JwtAuthenticationFilter;
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
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/**"
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

---

#### 8. Configure Docker Compose Integration

**Add service to** `docker-compose.yml` (project root):

```yaml
services:
  # ... existing services ...

  attendance-service:
    build:
      context: ./attendance-service
      dockerfile: Dockerfile
    container_name: attendance-service
    environment:
      # Profile activation
      - SPRING_PROFILES_ACTIVE=docker

      # Database configuration
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-attendance:5432/attendance_db
      - SPRING_DATASOURCE_USERNAME=sms_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

      # Eureka service discovery
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

      # JWT authentication
      - JWT_SECRET=${JWT_SECRET}

    ports:
      - "8083:8083"  # External:Internal port mapping

    networks:
      - backend-network
      - database-network

    depends_on:
      - postgres-attendance
      - eureka-server

    restart: unless-stopped

    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8083/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  # Database for attendance service
  postgres-attendance:
    image: postgres:15-alpine
    container_name: postgres-attendance
    environment:
      - POSTGRES_DB=attendance_db
      - POSTGRES_USER=sms_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres-attendance-data:/var/lib/postgresql/data
    networks:
      - database-network
    ports:
      - "5435:5432"  # External port (avoid conflicts with other DBs)

# Add volume
volumes:
  postgres-attendance-data:
    driver: local
```

**Environment variable checklist**:
- ✅ `SPRING_PROFILES_ACTIVE=docker` (NOT `prod`)
- ✅ `SPRING_DATASOURCE_*` (NOT `DB_*`)
- ✅ `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` (exact spelling)
- ✅ `JWT_SECRET` (shared across all services)

---

#### 9. Update API Gateway Routes

**Add route to API Gateway** (`api-gateway/src/main/resources/application.yml`):

```yaml
spring:
  cloud:
    gateway:
      routes:
        # ... existing routes ...

        - id: attendance-service
          uri: lb://attendance-service  # Load-balanced via Eureka
          predicates:
            - Path=/api/attendance/**
          filters:
            - name: AuthenticationFilter  # JWT validation
```

---

#### 10. Build and Test

**Build the service**:
```bash
cd attendance-service

# Clean and build
mvn clean package

# Expected output:
# [INFO] BUILD SUCCESS
```

**Run locally**:
```bash
# Terminal 1: Start service
mvn spring-boot:run

# Terminal 2: Test endpoints
curl http://localhost:8083/actuator/health

# Expected: {"status":"UP"}
```

**Run in Docker**:
```bash
# Build Docker image
docker-compose build attendance-service

# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f attendance-service

# Expected: Service registered with Eureka
```

---

## Validation

### Automated Compliance Check

**Run validation script** to ensure 100% compliance:

```bash
# Navigate to project root
cd /path/to/salarean

# Run validation
./specs/001-service-standards/contracts/validation-script.sh ./attendance-service
```

**Expected output** (all checks pass):
```
================================================================================
Microservice Compliance Validation
================================================================================

Service: attendance-service
Directory: ./attendance-service
Date: 2025-11-22

### Category 1: Profile Configuration (3 checks)

✅ PASS: PROFILE-001: Service has exactly 2 profile files
✅ PASS: PROFILE-002: Profile files use standard names
✅ PASS: PROFILE-003: docker-compose.yml sets SPRING_PROFILES_ACTIVE=docker

### Category 2: Environment Variable Naming (5 checks)

✅ PASS: ENV-001: Database variables use SPRING_DATASOURCE_* prefix
✅ PASS: ENV-002: Eureka variable uses EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
✅ PASS: ENV-003: No forbidden custom database variables

### Category 3: Package Structure (6 checks)

✅ PASS: PKG-001: Entities are in 'model/' package (correct)
✅ PASS: PKG-002: JWT classes are in 'security/' package (correct)
✅ PASS: PKG-003: Service implementations are in 'service/' package
✅ PASS: PKG-004: All required standard packages exist

### Category 4: JWT Architecture (4 checks)

✅ PASS: JWT-001: JWT logic split into Filter and Provider classes

### Category 5: Required Configuration Classes (4 checks)

✅ PASS: CFG-001: CorsConfig.java exists
✅ PASS: CFG-002: OpenAPIConfig.java exists (correct capitalization)
✅ PASS: CFG-003: SecurityConfig.java exists

### Category 7: Eureka Configuration (3 checks)

✅ PASS: EUR-003: Eureka instance config not in environment variables

### Category 8: Docker Compose Configuration (4 checks)

✅ PASS: DCK-001: docker-compose.yml uses SPRING_PROFILES_ACTIVE=docker
✅ PASS: DCK-002: All standard environment variables present

================================================================================
Validation Summary
================================================================================

Total Checks: 25
Passed: 25
Failed: 0

✅ COMPLIANCE STATUS: PASSED

This service is 100% compliant with Salarean SMS standards.
```

---

### Manual Verification Checklist

**Use this checklist** for final review:

#### Structure
- [ ] Exactly 2 profiles: `application.yml`, `application-docker.yml`
- [ ] Package structure matches standard (model/, security/, service/)
- [ ] JWT split into JwtAuthenticationFilter + JwtTokenProvider
- [ ] All required config classes present (CorsConfig, OpenAPIConfig, SecurityConfig)

#### Configuration
- [ ] Environment variables use Spring Boot standard names
- [ ] Eureka configured with `prefer-ip-address: false`
- [ ] OpenAPI points to API Gateway (port 8080)
- [ ] Service name matches in all configs

#### Code Quality
- [ ] Service implementations in `service/` (not `service/impl/`)
- [ ] Entity package named `model/` (not `entity/`)
- [ ] OpenAPI config named `OpenAPIConfig` (not `OpenApiConfig`)
- [ ] CORS configuration included

#### Docker
- [ ] docker-compose.yml uses `SPRING_PROFILES_ACTIVE=docker`
- [ ] All environment variables follow standard naming
- [ ] JWT_SECRET environment variable included
- [ ] Service registered in API Gateway routes

---

## Testing

### Local Testing (Without Docker)

**1. Start the service**:
```bash
cd attendance-service
mvn spring-boot:run
```

**2. Test health endpoint**:
```bash
curl http://localhost:8083/actuator/health

# Expected:
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

**3. Access Swagger UI**:
```bash
# Open in browser
open http://localhost:8083/swagger-ui.html
```

**4. Test API endpoints** (requires JWT token):

```bash
# Get JWT token from auth-service first
TOKEN=$(curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}' \
  | jq -r '.data.accessToken')

# Create attendance record
curl -X POST http://localhost:8083/api/attendance \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "date": "2025-11-22",
    "checkInTime": "08:00:00",
    "status": "PRESENT"
  }'

# Expected:
{
  "errorCode": "SUCCESS",
  "data": {
    "id": 1,
    "studentId": 1,
    "date": "2025-11-22",
    "checkInTime": "08:00:00",
    "checkOutTime": null,
    "status": "PRESENT",
    "notes": null
  }
}
```

---

### Docker Testing (Full Stack)

**1. Build service image**:
```bash
docker-compose build attendance-service
```

**2. Start all services**:
```bash
docker-compose up -d
```

**3. Verify service is running**:
```bash
# Check container status
docker-compose ps

# Expected:
NAME                 STATUS
attendance-service   Up (healthy)
eureka-server        Up
postgres-attendance  Up
api-gateway          Up
```

**4. Check Eureka registration**:
```bash
# Open Eureka dashboard
open http://localhost:8761

# Look for "ATTENDANCE-SERVICE" in registered instances
```

**5. Test through API Gateway**:
```bash
# Get token from auth-service through gateway
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}' \
  | jq -r '.data.accessToken')

# Call attendance-service through gateway
curl -X GET http://localhost:8080/api/attendance/student/1 \
  -H "Authorization: Bearer $TOKEN"
```

**6. Check service logs**:
```bash
# View logs
docker-compose logs -f attendance-service

# Expected to see:
# - Service startup messages
# - Eureka registration confirmation
# - Database connection successful
# - No error messages
```

---

### Integration Testing

**Create integration test** (`src/test/java/com/sms/attendance/AttendanceIntegrationTest.java`):

```java
package com.sms.attendance;

import com.sms.attendance.dto.request.CreateAttendanceRequest;
import com.sms.attendance.model.Attendance.AttendanceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AttendanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreateAttendance() throws Exception {
        String requestJson = """
            {
                "studentId": 1,
                "date": "2025-11-22",
                "checkInTime": "08:00:00",
                "status": "PRESENT"
            }
            """;

        mockMvc.perform(post("/api/attendance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
                .andExpect(jsonPath("$.data.studentId").value(1));
    }
}
```

**Run tests**:
```bash
mvn test

# Expected:
# Tests run: X, Failures: 0, Errors: 0, Skipped: 0
# BUILD SUCCESS
```

---

## Troubleshooting

### Common Issues and Solutions

#### Issue 1: Build Fails with "Package does not exist"

**Symptoms**:
```
[ERROR] package com.sms.auth does not exist
[ERROR] cannot find symbol
```

**Cause**: Package names not updated after copying template.

**Solution**:
```bash
# Re-run package rename
find . -type f -name "*.java" \
  -exec sed -i '' 's/com\.sms\.auth/com.sms.attendance/g' {} +

# Verify all files updated
grep -r "com.sms.auth" src/
# Should return no results
```

---

#### Issue 2: Service Won't Start - Port Already in Use

**Symptoms**:
```
***************************
APPLICATION FAILED TO START
***************************

Description:
Web server failed to start. Port 8083 was already in use.
```

**Solution**:
```bash
# Option 1: Kill process using port
lsof -ti:8083 | xargs kill -9

# Option 2: Change port in application.yml
sed -i '' 's/8083/8084/g' src/main/resources/application.yml

# Restart service
mvn spring-boot:run
```

---

#### Issue 3: Database Connection Refused (Local)

**Symptoms**:
```
com.zaxxer.hikari.pool.HikariPool$PoolInitializationException
Caused by: org.postgresql.util.PSQLException: Connection refused
```

**Solution**:
```bash
# Check PostgreSQL is running
psql -U postgres -c "SELECT 1"

# If not running, start it
# macOS (Homebrew)
brew services start postgresql@15

# Linux (systemd)
sudo systemctl start postgresql

# Create database
psql -U postgres -c "CREATE DATABASE attendance_db;"
psql -U postgres -c "CREATE USER sms_user WITH PASSWORD 'password';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE attendance_db TO sms_user;"
```

---

#### Issue 4: Service Not Registering with Eureka (Docker)

**Symptoms**:
- Service starts but doesn't appear in Eureka dashboard
- Logs show connection timeout to Eureka

**Solution**:

**Check 1**: Verify `application-docker.yml`:
```yaml
eureka:
  instance:
    hostname: attendance-service  # Must match docker-compose service name
    prefer-ip-address: false      # CRITICAL - must be false
  client:
    enabled: true
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

**Check 2**: Verify docker-compose.yml:
```yaml
attendance-service:
  environment:
    - SPRING_PROFILES_ACTIVE=docker  # Must be 'docker'
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
  networks:
    - backend-network  # MUST be on same network as Eureka
  depends_on:
    - eureka-server
```

**Check 3**: Verify networks:
```bash
# Check service is on correct network
docker inspect attendance-service | jq '.[0].NetworkSettings.Networks'

# Should show 'backend-network'
```

**Check 4**: Restart services in order:
```bash
docker-compose up -d eureka-server
sleep 30  # Wait for Eureka to be ready
docker-compose up -d attendance-service
docker-compose logs -f attendance-service
```

---

#### Issue 5: CORS Errors in Swagger UI

**Symptoms**:
- Swagger UI loads successfully
- API calls from Swagger fail with CORS error
- Browser console shows: "Access to XMLHttpRequest blocked by CORS policy"

**Solution**:

**Check OpenAPIConfig server URL**:
```java
// WRONG - points to service directly
server.setUrl("http://localhost:8083");

// CORRECT - points to API Gateway
server.setUrl("http://localhost:8080");
```

**Update** `config/OpenAPIConfig.java`:
```java
@Bean
public OpenAPI attendanceAPI() {
    Server server = new Server();
    server.setUrl("http://localhost:8080");  // API Gateway
    server.setDescription("API Gateway");
    // ...
}
```

**Restart service** and access Swagger through gateway:
```bash
open http://localhost:8080/swagger-ui.html
```

---

#### Issue 6: JWT Validation Fails

**Symptoms**:
```
401 Unauthorized
Invalid JWT signature
```

**Cause**: JWT secret mismatch between services.

**Solution**:

**Check .env file** (project root):
```bash
cat .env | grep JWT_SECRET
```

**Ensure all services use same secret**:
```bash
# In docker-compose.yml, all services should have:
- JWT_SECRET=${JWT_SECRET}

# Restart services after .env change
docker-compose down
docker-compose up -d
```

**Test token validation**:
```bash
# Get token from auth-service
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}' \
  | jq -r '.data.accessToken')

# Verify token works
curl -X GET http://localhost:8080/api/attendance/student/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

#### Issue 7: Maven Build Fails - Dependency Issues

**Symptoms**:
```
[ERROR] Failed to execute goal on project attendance-service
[ERROR] Could not resolve dependencies
```

**Solution**:
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Force update dependencies
mvn clean install -U

# If still fails, check pom.xml parent version
# Should match other services
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.7</version>
</parent>
```

---

#### Issue 8: Validation Script Reports Failures

**Symptoms**:
```
❌ FAIL: PKG-001: Entities are in 'entity/' package. Rename to 'model/'
❌ FAIL: PKG-002: JWT classes are in 'config/' package. Move to 'security/'
```

**Solution**:

**Fix entity package**:
```bash
# Rename directory
mv src/main/java/com/sms/attendance/entity \
   src/main/java/com/sms/attendance/model

# Update package declarations
find src/main/java/com/sms/attendance/model -name "*.java" \
  -exec sed -i '' 's/package com.sms.attendance.entity/package com.sms.attendance.model/g' {} +

# Update imports
find src -name "*.java" \
  -exec sed -i '' 's/com.sms.attendance.entity/com.sms.attendance.model/g' {} +
```

**Fix JWT package**:
```bash
# Move files
mv src/main/java/com/sms/attendance/config/JwtAuthenticationFilter.java \
   src/main/java/com/sms/attendance/security/

mv src/main/java/com/sms/attendance/config/JwtTokenProvider.java \
   src/main/java/com/sms/attendance/security/

# Update package declarations
sed -i '' 's/package com.sms.attendance.config/package com.sms.attendance.security/g' \
  src/main/java/com/sms/attendance/security/JwtAuthenticationFilter.java

sed -i '' 's/package com.sms.attendance.config/package com.sms.attendance.security/g' \
  src/main/java/com/sms/attendance/security/JwtTokenProvider.java

# Update imports
find src -name "*.java" \
  -exec sed -i '' 's/com.sms.attendance.config.JwtAuthenticationFilter/com.sms.attendance.security.JwtAuthenticationFilter/g' {} +

find src -name "*.java" \
  -exec sed -i '' 's/com.sms.attendance.config.JwtTokenProvider/com.sms.attendance.security.JwtTokenProvider/g' {} +
```

**Re-run validation**:
```bash
./specs/001-service-standards/contracts/validation-script.sh ./attendance-service
```

---

### Getting Help

**If you're stuck**:

1. **Check validation script** for specific issues:
   ```bash
   ./specs/001-service-standards/contracts/validation-script.sh ./your-service
   ```

2. **Compare with template** (auth-service):
   ```bash
   diff -r auth-service/src/main/java/com/sms/auth \
           your-service/src/main/java/com/sms/yourservice
   ```

3. **Review reference docs**:
   - Compliance Checklist: `specs/001-service-standards/contracts/compliance-checklist.md`
   - Service Template: `specs/001-service-standards/contracts/service-template.md`
   - Research Document: `specs/001-service-standards/research.md`

4. **Check Docker logs**:
   ```bash
   docker-compose logs -f your-service
   ```

5. **Verify Eureka dashboard**:
   ```bash
   open http://localhost:8761
   ```

---

## Next Steps

### After Creating Your Service

**1. Version Control**:
```bash
# Navigate to project root
cd /path/to/salarean

# Stage changes
git add attendance-service/
git add docker-compose.yml
git add api-gateway/src/main/resources/application.yml

# Commit
git commit -m "feat: add attendance-service following standardization template

- Created attendance-service from auth-service template
- Added domain entities, services, controllers
- Configured Docker Compose integration
- Added API Gateway routes
- Validated 100% compliance with validation script

Closes #XXX"
```

**2. Update Documentation**:
- Update main README.md with service description
- Add service to architecture diagram
- Document API endpoints
- Update service comparison table

**3. CI/CD Integration**:
```yaml
# .github/workflows/attendance-service.yml
name: Attendance Service CI

on:
  push:
    paths:
      - 'attendance-service/**'

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run compliance validation
        run: |
          chmod +x specs/001-service-standards/contracts/validation-script.sh
          ./specs/001-service-standards/contracts/validation-script.sh ./attendance-service

  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run tests
        run: |
          cd attendance-service
          mvn test

  build:
    runs-on: ubuntu-latest
    needs: [validate, test]
    steps:
      - uses: actions/checkout@v3
      - name: Build Docker image
        run: docker-compose build attendance-service
```

**4. Database Migrations**:

**Create Flyway migration** (`src/main/resources/db/migration/V1__initial_schema.sql`):
```sql
-- Attendance table
CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    date DATE NOT NULL,
    check_in_time TIME NOT NULL,
    check_out_time TIME,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_attendance_student_id ON attendance(student_id);
CREATE INDEX idx_attendance_date ON attendance(date);
CREATE INDEX idx_attendance_student_date ON attendance(student_id, date);

-- Comments
COMMENT ON TABLE attendance IS 'Student attendance records';
COMMENT ON COLUMN attendance.status IS 'PRESENT, ABSENT, LATE, EXCUSED';
```

**Add Flyway dependency** to `pom.xml`:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

**5. Monitoring and Observability**:

**Add Actuator endpoints**:
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

**6. API Documentation**:

**Add OpenAPI annotations** to controllers:
```java
@Operation(
    summary = "Create attendance record",
    description = "Creates a new attendance record for a student",
    responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Attendance created successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid JWT token")
    }
)
@PostMapping
public ResponseEntity<ApiResponse<AttendanceResponse>> createAttendance(...) {
    // ...
}
```

**7. Performance Testing**:

**Create load test** (using k6 or JMeter):
```javascript
// k6-load-test.js
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  stages: [
    { duration: '30s', target: 20 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 0 },
  ],
};

export default function () {
  let response = http.get('http://localhost:8083/actuator/health');
  check(response, {
    'status is 200': (r) => r.status === 200,
  });
}
```

**8. Security Review**:

**Security checklist**:
- [ ] JWT secret is strong (minimum 32 characters)
- [ ] No secrets committed to Git
- [ ] All sensitive endpoints require authentication
- [ ] Input validation on all endpoints
- [ ] SQL injection prevention (use JPA/Hibernate)
- [ ] XSS prevention (Spring Security default)
- [ ] CORS configured correctly
- [ ] HTTPS in production (configure in deployment)

---

### Deploying to Production

**Production readiness checklist**:

**1. Configuration**:
- [ ] Environment-specific values in .env (not hardcoded)
- [ ] Database credentials secured
- [ ] JWT secret rotated from default
- [ ] Logging level set to INFO/WARN
- [ ] Health checks configured

**2. Database**:
- [ ] Migrations tested
- [ ] Backups configured
- [ ] Connection pooling optimized
- [ ] Indexes created for performance

**3. Monitoring**:
- [ ] Prometheus metrics exposed
- [ ] Grafana dashboards created
- [ ] Alerting rules configured
- [ ] Log aggregation setup (ELK/Loki)

**4. Performance**:
- [ ] Load testing completed
- [ ] Memory limits set in Docker
- [ ] Connection pool sized correctly
- [ ] Caching strategy implemented (if needed)

**5. Security**:
- [ ] HTTPS configured (TLS certificates)
- [ ] Secrets management (Vault/AWS Secrets Manager)
- [ ] Network policies configured
- [ ] Rate limiting enabled

---

## Reference

### Quick Command Reference

**Service Creation**:
```bash
# Copy template
cp -r auth-service new-service

# Rename packages
find . -type f -name "*.java" -exec sed -i '' 's/auth/new/g' {} +

# Move directories
mv src/main/java/com/sms/auth src/main/java/com/sms/new

# Build
mvn clean package

# Run locally
mvn spring-boot:run

# Validate
./specs/001-service-standards/contracts/validation-script.sh ./new-service
```

**Docker Operations**:
```bash
# Build image
docker-compose build new-service

# Start service
docker-compose up -d new-service

# View logs
docker-compose logs -f new-service

# Restart service
docker-compose restart new-service

# Stop service
docker-compose down
```

**Testing**:
```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Coverage report
mvn jacoco:report
open target/site/jacoco/index.html
```

---

### Service Port Allocation

| Service | Port | Status |
|---------|------|--------|
| API Gateway | 8080 | Reserved |
| Auth Service | 8081 | Reserved |
| Student Service | 8082 | Reserved |
| Attendance Service | 8083 | Available |
| Grade Service | 8084 | Available |
| Report Service | 8085 | Available |
| Notification Service | 8086 | Available |
| Teacher Service | 8087 | Available |
| Course Service | 8088 | Available |
| Payment Service | 8089 | Available |
| Eureka Server | 8761 | Reserved |

---

### File Checklist

**Required files for new service**:

```
new-service/
├── Dockerfile
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/sms/newservice/
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java           ✅ Required
│   │   │   │   ├── OpenAPIConfig.java        ✅ Required
│   │   │   │   └── SecurityConfig.java       ✅ Required
│   │   │   ├── controller/                   ✅ Required
│   │   │   ├── dto/                          ✅ Required
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   │       └── ApiResponse.java      ✅ Required
│   │   │   ├── exception/                    ✅ Required
│   │   │   │   └── GlobalExceptionHandler.java ✅ Required
│   │   │   ├── model/                        ✅ Required
│   │   │   ├── repository/                   ✅ Required
│   │   │   ├── security/                     ✅ Required (if JWT)
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtTokenProvider.java
│   │   │   ├── service/                      ✅ Required
│   │   │   ├── validation/                   ○ Optional
│   │   │   └── NewServiceApplication.java    ✅ Required
│   │   └── resources/
│   │       ├── application.yml               ✅ Required
│   │       ├── application-docker.yml        ✅ Required
│   │       └── db/migration/                 ○ Optional (Flyway)
│   └── test/                                 ✅ Required
└── .gitignore
```

---

### Environment Variables Reference

**Standard variables** (all services):
```properties
# Profile
SPRING_PROFILES_ACTIVE=docker

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-{service}:5432/{service}_db
SPRING_DATASOURCE_USERNAME=sms_user
SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

# JWT
JWT_SECRET=${JWT_SECRET}
```

**Optional variables** (service-specific):
```properties
# Redis
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=${REDIS_PASSWORD}

# File Upload
UPLOAD_DIR=/app/uploads/{service}
MAX_FILE_SIZE=10485760

# Custom
{SERVICE}_SPECIFIC_VAR=value
```

---

### Related Documentation

**Primary References**:
- **Service Template**: `specs/001-service-standards/contracts/service-template.md`
- **Compliance Checklist**: `specs/001-service-standards/contracts/compliance-checklist.md`
- **Validation Script**: `specs/001-service-standards/contracts/validation-script.sh`
- **Data Model**: `specs/001-service-standards/data-model.md`
- **Research**: `specs/001-service-standards/research.md`

**Project Documentation**:
- **Service Comparison**: `SERVICE_COMPARISON_ANALYSIS.md`
- **CLAUDE.md**: Project-wide development guidelines
- **Main README**: Overall project documentation

**Template Service**:
- **Path**: `/Volumes/DATA/my-projects/salarean/auth-service`
- **Purpose**: Authoritative reference implementation

---

## Appendix: Common Patterns

### Standard API Response Format

**All endpoints MUST return**:
```java
public class ApiResponse<T> {
    private String errorCode;  // "SUCCESS" or error code
    private T data;            // Response payload or null
}
```

**Success example**:
```json
{
  "errorCode": "SUCCESS",
  "data": { "id": 1, "name": "John" }
}
```

**Error example**:
```json
{
  "errorCode": "RESOURCE_NOT_FOUND",
  "data": null
}
```

---

### Error Code Conventions

**Format**: `UPPER_SNAKE_CASE`

**Categories**:
- `SUCCESS` - Operation successful
- `VALIDATION_ERROR` - Input validation failed
- `RESOURCE_NOT_FOUND` - Entity not found
- `UNAUTHORIZED` - Authentication required
- `FORBIDDEN` - Insufficient permissions
- `INTERNAL_ERROR` - Server error

**Service-specific**:
- `ATTENDANCE_NOT_FOUND`
- `GRADE_ALREADY_EXISTS`
- `INVALID_DATE_RANGE`

---

### Standard Controller Pattern

```java
@RestController
@RequestMapping("/api/{resource}")
@RequiredArgsConstructor
@Tag(name = "{Resource}", description = "{Resource} management")
@SecurityRequirement(name = "bearerAuth")
public class {Resource}Controller {

    private final {Resource}Service service;

    @PostMapping
    @Operation(summary = "Create {resource}")
    public ResponseEntity<ApiResponse<{Resource}Response>> create(
            @Valid @RequestBody Create{Resource}Request request) {
        {Resource}Response response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>("SUCCESS", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get {resource} by ID")
    public ResponseEntity<ApiResponse<{Resource}Response>> getById(
            @PathVariable Long id) {
        {Resource}Response response = service.getById(id);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
    }

    @GetMapping
    @Operation(summary = "List all {resources}")
    public ResponseEntity<ApiResponse<List<{Resource}Response>>> getAll() {
        List<{Resource}Response> response = service.getAll();
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update {resource}")
    public ResponseEntity<ApiResponse<{Resource}Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody Update{Resource}Request request) {
        {Resource}Response response = service.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete {resource}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", null));
    }
}
```

---

**Version**: 1.0.0
**Last Updated**: 2025-11-22
**Maintained By**: Salarean Development Team
