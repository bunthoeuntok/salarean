# Developer Quickstart: Teacher-Based Student Data Isolation

**Feature**: 001-tenant-student-isolation
**Date**: 2025-12-07
**Phase**: 1 (Design & Contracts)
**Estimated Implementation Time**: 4-6 hours

## Overview

This guide helps developers implement teacher-based data isolation for student records. Follow these steps sequentially to ensure proper implementation and testing.

---

## Prerequisites

Before starting implementation:

- ✅ Feature specification reviewed (spec.md)
- ✅ Implementation plan reviewed (plan.md)
- ✅ Data model reviewed (data-model.md)
- ✅ API contracts reviewed (contracts/endpoints.md)
- ✅ Development environment running:
  - PostgreSQL container (student-service)
  - Redis container
  - Auth-service running (for JWT validation)
  - Student-service running locally or in Docker

---

## Implementation Checklist

### Phase 1: Database Migration (Est: 30 min)

#### Step 1.1: Create Migration Script

**File**: `student-service/src/main/resources/db/migration/V6__add_teacher_id_to_students.sql`

```sql
-- Migration: Add teacher_id column to students table
-- Feature: 001-tenant-student-isolation
-- Date: 2025-12-07

BEGIN;

-- Add teacher_id column (nullable for backward compatibility)
ALTER TABLE students
ADD COLUMN teacher_id UUID;

-- Create index for performance
CREATE INDEX idx_students_teacher_id ON students(teacher_id);

-- Add column comment
COMMENT ON COLUMN students.teacher_id IS 'References the teacher who owns/created this student. Used for teacher-based data isolation.';

COMMIT;
```

**Verification**:
```bash
# Check migration file syntax
cd student-service
./mvnw flyway:info

# Apply migration (dev environment)
./mvnw flyway:migrate

# Verify column exists
docker exec -it postgres-student psql -U sms_user -d student_db -c "\d students"
```

**Expected Output**:
```
Column     | Type | Nullable
-----------+------+----------
teacher_id | uuid | YES
```

---

### Phase 2: Entity & Repository Updates (Est: 45 min)

#### Step 2.1: Update Student Entity

**File**: `student-service/src/main/java/com/sms/student/model/Student.java`

**Add field**:
```java
@Entity
@Table(name = "students")
public class Student {
    // ... existing fields ...

    @Column(name = "teacher_id")
    private UUID teacherId;  // Add this field

    // ... rest of class ...
}
```

**Update Builder** (if using Lombok @Builder):
```java
@Builder
public class Student {
    // Lombok will automatically include teacherId in builder
}
```

#### Step 2.2: Update StudentRepository

**File**: `student-service/src/main/java/com/sms/student/repository/StudentRepository.java`

**Add custom queries**:
```java
@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    // NEW: Find all students by teacher ID
    @Query("SELECT s FROM Student s WHERE s.teacherId = :teacherId")
    List<Student> findAllByTeacherId(@Param("teacherId") UUID teacherId);

    // NEW: Find student by ID and teacher ID (ownership validation)
    @Query("SELECT s FROM Student s WHERE s.id = :id AND s.teacherId = :teacherId")
    Optional<Student> findByIdAndTeacherId(
        @Param("id") UUID id,
        @Param("teacherId") UUID teacherId
    );

    // NEW: Find by student code and teacher ID
    @Query("SELECT s FROM Student s WHERE s.studentCode = :code AND s.teacherId = :teacherId")
    Optional<Student> findByStudentCodeAndTeacherId(
        @Param("code") String studentCode,
        @Param("teacherId") UUID teacherId
    );

    // MODIFY EXISTING: Add teacher ID filter to avoid conflicts across teachers
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Student s " +
           "WHERE s.studentCode = :code AND s.teacherId = :teacherId")
    boolean existsByStudentCodeAndTeacherId(
        @Param("code") String studentCode,
        @Param("teacherId") UUID teacherId
    );
}
```

**Verification**:
```bash
# Compile to check for syntax errors
./mvnw clean compile
```

---

### Phase 3: Security Context Setup (Est: 30 min)

#### Step 3.1: Create TeacherContextHolder

**File**: `student-service/src/main/java/com/sms/student/security/TeacherContextHolder.java` (NEW)

```java
package com.sms.student.security;

import com.sms.student.exception.UnauthorizedAccessException;
import java.util.UUID;

/**
 * Thread-local storage for authenticated teacher's ID.
 * Populated by JwtAuthenticationFilter on each request.
 */
public class TeacherContextHolder {

    private static final ThreadLocal<UUID> teacherIdHolder = new ThreadLocal<>();

    /**
     * Set teacher ID for current thread (called by filter)
     */
    public static void setTeacherId(UUID teacherId) {
        teacherIdHolder.set(teacherId);
    }

    /**
     * Get teacher ID for current thread (called by services)
     * @throws UnauthorizedAccessException if teacher context not set
     */
    public static UUID getTeacherId() {
        UUID teacherId = teacherIdHolder.get();
        if (teacherId == null) {
            throw new UnauthorizedAccessException("No teacher context found");
        }
        return teacherId;
    }

    /**
     * Clear teacher ID from current thread (called by filter in finally block)
     */
    public static void clear() {
        teacherIdHolder.remove();
    }
}
```

#### Step 3.2: Update JwtAuthenticationFilter

**File**: `student-service/src/main/java/com/sms/student/security/JwtAuthenticationFilter.java`

**Modify `doFilterInternal` method**:
```java
@Override
protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
) throws ServletException, IOException {

    try {
        String token = extractTokenFromRequest(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            UUID teacherId = jwtTokenProvider.getUserIdFromToken(token);

            // NEW: Store teacher ID in thread-local context
            TeacherContextHolder.setTeacherId(teacherId);

            // Existing authentication setup
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    teacherId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
                );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);

    } finally {
        // NEW: Always clear context to prevent thread leak
        TeacherContextHolder.clear();
    }
}
```

**Verification**:
```bash
# Compile
./mvnw clean compile
```

---

### Phase 4: Exception Handling (Est: 20 min)

#### Step 4.1: Create UnauthorizedAccessException

**File**: `student-service/src/main/java/com/sms/student/exception/UnauthorizedAccessException.java` (NEW)

```java
package com.sms.student.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
```

#### Step 4.2: Add Error Code

**File**: `student-service/src/main/java/com/sms/student/enums/ErrorCode.java` (or wherever ErrorCode is defined)

```java
public enum ErrorCode {
    SUCCESS,
    UNAUTHORIZED_ACCESS,  // NEW
    TEACHER_CONTEXT_MISSING,  // NEW
    STUDENT_NOT_FOUND,
    DUPLICATE_STUDENT_CODE,
    INVALID_INPUT,
    UNAUTHORIZED,
    // ... existing codes
}
```

#### Step 4.3: Update GlobalExceptionHandler

**File**: `student-service/src/main/java/com/sms/student/exception/GlobalExceptionHandler.java`

**Add handler method**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ... existing handlers ...

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedAccess(
        UnauthorizedAccessException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(ErrorCode.UNAUTHORIZED_ACCESS));
    }
}
```

---

### Phase 5: Service Layer Updates (Est: 1.5 hours)

#### Step 5.1: Update IStudentService Interface

**File**: `student-service/src/main/java/com/sms/student/service/interfaces/IStudentService.java`

**Add cache reload method**:
```java
public interface IStudentService {
    // Existing methods (no signature changes, but implementation will add teacher validation)
    List<StudentResponse> getAllStudents();
    StudentResponse getStudentById(UUID id);
    StudentResponse createStudent(StudentRequest request);
    StudentResponse updateStudent(UUID id, StudentRequest request);
    void deleteStudent(UUID id, String reason);

    // NEW: Cache management
    void clearTeacherCache();
}
```

#### Step 5.2: Update StudentService Implementation

**File**: `student-service/src/main/java/com/sms/student/service/StudentService.java`

**Import statements**:
```java
import com.sms.student.security.TeacherContextHolder;
import org.springframework.cache.annotation.*;
```

**Modify methods**:

```java
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "students")  // NEW
public class StudentService implements IStudentService {

    private final StudentRepository studentRepository;
    private final CacheManager cacheManager;

    @Override
    @Cacheable(key = "#result.size() + ':all'")  // NEW: Cache list
    public List<StudentResponse> getAllStudents() {
        UUID teacherId = TeacherContextHolder.getTeacherId();  // NEW
        List<Student> students = studentRepository.findAllByTeacherId(teacherId);  // MODIFIED
        return students.stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Cacheable(key = "#id")  // NEW: Cache individual student
    public StudentResponse getStudentById(UUID id) {
        UUID teacherId = TeacherContextHolder.getTeacherId();  // NEW
        Student student = studentRepository.findByIdAndTeacherId(id, teacherId)  // MODIFIED
            .orElseThrow(() -> new UnauthorizedAccessException(
                "Student not found or access denied"
            ));
        return toResponse(student);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'all'")  // NEW: Evict list cache
    public StudentResponse createStudent(StudentRequest request) {
        UUID teacherId = TeacherContextHolder.getTeacherId();  // NEW

        Student student = Student.builder()
            .teacherId(teacherId)  // NEW: Auto-assign
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            // ... other fields
            .createdBy(teacherId)  // MODIFIED: Use teacherId instead of extracting from SecurityContext
            .status(StudentStatus.ACTIVE)
            .build();

        return toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)  // NEW: Evict all caches for this teacher
    public StudentResponse updateStudent(UUID id, StudentRequest request) {
        UUID teacherId = TeacherContextHolder.getTeacherId();  // NEW

        Student student = studentRepository.findByIdAndTeacherId(id, teacherId)  // MODIFIED
            .orElseThrow(() -> new UnauthorizedAccessException(
                "Student not found or access denied"
            ));

        // Update fields
        student.setFirstName(request.getFirstName());
        // ... other updates
        student.setUpdatedBy(teacherId);  // MODIFIED

        return toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)  // NEW
    public void deleteStudent(UUID id, String reason) {
        UUID teacherId = TeacherContextHolder.getTeacherId();  // NEW

        Student student = studentRepository.findByIdAndTeacherId(id, teacherId)  // MODIFIED
            .orElseThrow(() -> new UnauthorizedAccessException(
                "Student not found or access denied"
            ));

        student.setStatus(StudentStatus.INACTIVE);
        student.setDeletionReason(reason);
        student.setDeletedAt(LocalDateTime.now());
        student.setDeletedBy(teacherId);  // MODIFIED

        studentRepository.save(student);
    }

    @Override
    @CacheEvict(allEntries = true)  // NEW method
    public void clearTeacherCache() {
        // Cache will be evicted by annotation
    }
}
```

---

### Phase 6: Redis Cache Configuration (Est: 30 min)

#### Step 6.1: Add RedisConfig

**File**: `student-service/src/main/java/com/sms/student/config/RedisConfig.java` (NEW)

```java
package com.sms.student.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))  // 30-minute TTL
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()
                )
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()
                )
            );

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

#### Step 6.2: Update application-docker.yml

**File**: `student-service/src/main/resources/application-docker.yml`

**Add Redis configuration**:
```yaml
spring:
  redis:
    host: redis
    port: 6379
  cache:
    type: redis
    redis:
      time-to-live: 1800000  # 30 minutes in milliseconds
```

#### Step 6.3: Update docker-compose.yml (if needed)

Verify Redis service exists and student-service can access it:

```yaml
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - sms-network

  student-service:
    depends_on:
      - redis
      - postgres-student
    # ... existing config
```

---

### Phase 7: Cache Management Endpoint (Est: 30 min)

#### Step 7.1: Create CacheReloadResponse DTO

**File**: `student-service/src/main/java/com/sms/student/dto/CacheReloadResponse.java` (NEW)

```java
package com.sms.student.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CacheReloadResponse {
    private UUID teacherId;
    private boolean cacheCleared;
    private LocalDateTime timestamp;
    private String message;
}
```

#### Step 7.2: Create CacheController

**File**: `student-service/src/main/java/com/sms/student/controller/CacheController.java` (NEW)

```java
package com.sms.student.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.student.dto.CacheReloadResponse;
import com.sms.student.security.TeacherContextHolder;
import com.sms.student.service.interfaces.IStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Tag(name = "Cache", description = "Cache management endpoints")
public class CacheController {

    private final IStudentService studentService;

    @PostMapping("/reload")
    @Operation(summary = "Reload cache for authenticated teacher")
    public ResponseEntity<ApiResponse<CacheReloadResponse>> reloadCache() {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        studentService.clearTeacherCache();

        CacheReloadResponse response = CacheReloadResponse.builder()
            .teacherId(teacherId)
            .cacheCleared(true)
            .timestamp(LocalDateTime.now())
            .message("Cache reloaded successfully")
            .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

---

### Phase 8: Testing (Est: 1.5 hours)

#### Step 8.1: Unit Tests for StudentService

**File**: `student-service/src/test/java/com/sms/student/service/StudentServiceTest.java`

**Add tests**:
```java
@SpringBootTest
class StudentServiceTest {

    @Autowired
    private IStudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void getAllStudents_shouldOnlyReturnTeacherStudents() {
        // Arrange
        UUID teacherA = UUID.randomUUID();
        UUID teacherB = UUID.randomUUID();
        TeacherContextHolder.setTeacherId(teacherA);

        createStudent("STU-001", teacherA);
        createStudent("STU-002", teacherA);
        createStudent("STU-003", teacherB);  // Different teacher

        // Act
        List<StudentResponse> result = studentService.getAllStudents();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getTeacherId().equals(teacherA));
    }

    @Test
    void getStudentById_shouldThrowUnauthorizedForDifferentTeacher() {
        // Arrange
        UUID teacherA = UUID.randomUUID();
        UUID teacherB = UUID.randomUUID();
        UUID studentId = createStudent("STU-001", teacherA).getId();
        TeacherContextHolder.setTeacherId(teacherB);  // Different teacher

        // Act & Assert
        assertThatThrownBy(() -> studentService.getStudentById(studentId))
            .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void createStudent_shouldAutoAssignTeacherId() {
        // Arrange
        UUID teacherId = UUID.randomUUID();
        TeacherContextHolder.setTeacherId(teacherId);
        StudentRequest request = buildStudentRequest("STU-001");

        // Act
        StudentResponse result = studentService.createStudent(request);

        // Assert
        assertThat(result.getTeacherId()).isEqualTo(teacherId);
        assertThat(result.getCreatedBy()).isEqualTo(teacherId);
    }

    // Helper methods
    private Student createStudent(String code, UUID teacherId) {
        return studentRepository.save(Student.builder()
            .studentCode(code)
            .teacherId(teacherId)
            .firstName("Test")
            .lastName("Student")
            .dateOfBirth(LocalDate.of(2010, 1, 1))
            .gender(Gender.M)
            .enrollmentDate(LocalDate.now())
            .status(StudentStatus.ACTIVE)
            .build());
    }

    private StudentRequest buildStudentRequest(String code) {
        // Build request object
    }
}
```

#### Step 8.2: Integration Tests for Repository

**File**: `student-service/src/test/java/com/sms/student/repository/StudentRepositoryTest.java`

```java
@DataJpaTest
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void findByIdAndTeacherId_shouldReturnEmptyForWrongTeacher() {
        // Arrange
        UUID teacherA = UUID.randomUUID();
        UUID teacherB = UUID.randomUUID();
        Student student = createStudent("STU-001", teacherA);

        // Act
        Optional<Student> result = studentRepository.findByIdAndTeacherId(
            student.getId(),
            teacherB
        );

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findAllByTeacherId_shouldReturnOnlyTeacherStudents() {
        // Arrange
        UUID teacherA = UUID.randomUUID();
        UUID teacherB = UUID.randomUUID();
        createStudent("STU-001", teacherA);
        createStudent("STU-002", teacherA);
        createStudent("STU-003", teacherB);

        // Act
        List<Student> result = studentRepository.findAllByTeacherId(teacherA);

        // Assert
        assertThat(result).hasSize(2);
    }
}
```

---

### Phase 9: Manual Testing (Est: 30 min)

#### Step 9.1: Start Services

```bash
# Start infrastructure
docker-compose up -d postgres-student redis

# Start auth-service (for JWT generation)
cd auth-service
./mvnw spring-boot:run

# Start student-service
cd student-service
./mvnw spring-boot:run
```

#### Step 9.2: Get JWT Token

```bash
# Register/login as Teacher A
curl -X POST http://localhost:8080/auth-service/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+855-12-345-678",
    "password": "SecurePass123!"
  }'

# Save access token
export TEACHER_A_TOKEN="<access_token_from_response>"
```

#### Step 9.3: Test Isolation

```bash
# Create student as Teacher A
curl -X POST http://localhost:8080/student-service/api/students \
  -H "Authorization: Bearer $TEACHER_A_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "studentCode": "STU-TEST-001",
    "firstName": "Sok",
    "lastName": "Chan",
    "dateOfBirth": "2010-05-15",
    "gender": "M",
    "enrollmentDate": "2024-12-07"
  }'

# Save student ID
export STUDENT_ID="<id_from_response>"

# Login as Teacher B
curl -X POST http://localhost:8080/auth-service/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+855-16-789-012",
    "password": "AnotherPass456!"
  }'

export TEACHER_B_TOKEN="<access_token_from_response>"

# Try to access Teacher A's student (should fail with 401)
curl -X GET "http://localhost:8080/student-service/api/students/$STUDENT_ID" \
  -H "Authorization: Bearer $TEACHER_B_TOKEN"

# Expected response:
# {
#   "errorCode": "UNAUTHORIZED_ACCESS",
#   "data": null
# }
```

#### Step 9.4: Test Cache Reload

```bash
# Access students (cache miss)
curl -X GET http://localhost:8080/student-service/api/students \
  -H "Authorization: Bearer $TEACHER_A_TOKEN"

# Access again (cache hit - faster)
curl -X GET http://localhost:8080/student-service/api/students \
  -H "Authorization: Bearer $TEACHER_A_TOKEN"

# Reload cache
curl -X POST http://localhost:8080/student-service/api/cache/reload \
  -H "Authorization: Bearer $TEACHER_A_TOKEN"

# Next access will be cache miss (refetch from DB)
curl -X GET http://localhost:8080/student-service/api/students \
  -H "Authorization: Bearer $TEACHER_A_TOKEN"
```

---

## Troubleshooting

### Issue: Migration Fails

**Symptom**: Flyway error "column teacher_id already exists"

**Solution**:
```bash
# Check current migration version
./mvnw flyway:info

# If V6 already applied, create V7 instead
# Or rollback (dev only):
./mvnw flyway:clean  # WARNING: Deletes all data
./mvnw flyway:migrate
```

### Issue: Cache Not Working

**Symptom**: Every request hits database (slow)

**Solution**:
```bash
# Check Redis is running
docker ps | grep redis

# Check Redis connectivity
docker exec -it student-service redis-cli ping
# Expected: PONG

# Check cache config in application-docker.yml
```

### Issue: UnauthorizedAccessException on All Requests

**Symptom**: 401 error even for teacher's own students

**Solution**:
- Verify JwtAuthenticationFilter is setting TeacherContextHolder
- Add debug logging:
  ```java
  UUID teacherId = TeacherContextHolder.getTeacherId();
  log.debug("Teacher ID from context: {}", teacherId);
  ```
- Check JWT token contains correct teacher ID:
  ```bash
  # Decode JWT at https://jwt.io
  # Verify "sub" claim contains valid UUID
  ```

---

## Checklist

Before marking implementation complete:

- [ ] Database migration applied successfully
- [ ] Student entity has teacher_id field
- [ ] StudentRepository has teacher-scoped queries
- [ ] TeacherContextHolder created and integrated
- [ ] JwtAuthenticationFilter sets/clears context
- [ ] UnauthorizedAccessException handled globally
- [ ] StudentService uses TeacherContextHolder
- [ ] Redis cache configured with 30-min TTL
- [ ] CacheController endpoint works
- [ ] Unit tests pass (run `./mvnw test`)
- [ ] Integration tests pass
- [ ] Manual testing confirms isolation
- [ ] Cache reload endpoint works
- [ ] Documentation updated (API docs, README)

---

## Next Steps

After implementation is complete:

1. **Create tasks.md**: Run `/speckit.tasks` to break down into specific sub-tasks
2. **Code review**: Have another developer review for security vulnerabilities
3. **Performance testing**: Load test with 50+ concurrent teachers
4. **Deployment**: Deploy to staging environment
5. **User acceptance testing**: Have teachers verify functionality

**Estimated Total Time**: 4-6 hours (for experienced Spring Boot developer)
