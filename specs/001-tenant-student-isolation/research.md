# Research: Teacher-Based Student Data Isolation

**Feature**: 001-tenant-student-isolation
**Date**: 2025-12-07
**Phase**: 0 (Outline & Research)

## Overview

This document consolidates technical research for implementing teacher-based data isolation in the student-service. Research covers caching strategies, JPA multi-tenancy patterns, and security validation approaches.

---

## 1. Spring Cache + Redis for Teacher-Scoped Caching

### Decision: Use `@Cacheable` with Teacher ID as Cache Key Component

**Rationale**:
- Spring Boot already includes Spring Cache abstraction
- Redis is already part of the tech stack (used for refresh tokens in auth-service)
- Cache keys must include teacher_id to prevent cross-teacher cache pollution
- TTL-based eviction + manual eviction endpoint for user control

**Implementation Approach**:
```java
@Cacheable(value = "students", key = "#teacherId + ':all'")
public List<StudentResponse> getAllStudents(UUID teacherId) { ... }

@Cacheable(value = "students", key = "#teacherId + ':' + #studentId")
public StudentResponse getStudent(UUID teacherId, UUID studentId) { ... }

@CacheEvict(value = "students", key = "#teacherId + '*'")
public void evictTeacherCache(UUID teacherId) { ... }
```

**Cache Configuration**:
- **Cache Name**: `students`
- **TTL**: 30 minutes (balance between performance and data freshness)
- **Eviction Strategy**: TTL + manual eviction via `/api/cache/reload` endpoint
- **Key Pattern**: `{teacherId}:{operation}` or `{teacherId}:{studentId}`

**Dependencies** (already in pom.xml or to be added):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

**Redis Configuration** (`application-docker.yml`):
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

**Alternatives Considered**:
- **In-memory caching (Caffeine)**: Rejected because cache doesn't persist across pod restarts in distributed environments
- **No caching**: Rejected because SC-001 requires <2s list queries; caching needed for performance at scale
- **Database-level caching**: Rejected because application-level cache gives finer control and teacher-scoped eviction

---

## 2. JPA Multi-Tenancy Patterns for Teacher Isolation

### Decision: Use Discriminator-Based Filtering with Custom Queries

**Rationale**:
- PostgreSQL shared database (single `student_db`) with logical separation
- Teacher ID acts as a discriminator column (similar to soft-delete patterns)
- All queries must include `WHERE teacher_id = ?` clause
- Prevents accidental cross-teacher data leakage

**Implementation Approach**:

**Option A: Custom @Query annotations** (CHOSEN):
```java
@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    @Query("SELECT s FROM Student s WHERE s.teacherId = :teacherId")
    List<Student> findAllByTeacherId(@Param("teacherId") UUID teacherId);

    @Query("SELECT s FROM Student s WHERE s.id = :id AND s.teacherId = :teacherId")
    Optional<Student> findByIdAndTeacherId(@Param("id") UUID id, @Param("teacherId") UUID teacherId);
}
```

**Why chosen**:
- Explicit query control (less magic, more visible)
- Easy to verify teacher_id is included in WHERE clause during code review
- Compatible with existing Student entity structure

**Option B: Spring Data method names** (NOT CHOSEN):
```java
List<Student> findAllByTeacherId(UUID teacherId);
Optional<Student> findByIdAndTeacherId(UUID id, UUID teacherId);
```

**Why rejected**:
- Method names can become verbose for complex queries
- Less explicit than @Query annotations
- Harder to verify security constraints at a glance

**Option C: Hibernate @Filter** (NOT CHOSEN):
```java
@Entity
@FilterDef(name = "teacherFilter", parameters = @ParamDef(name = "teacherId", type = "uuid"))
@Filter(name = "teacherFilter", condition = "teacher_id = :teacherId")
public class Student { ... }
```

**Why rejected**:
- Requires filter activation in session before each query (error-prone)
- Global filter could be forgotten in new code paths
- Less explicit than query-level constraints

---

## 3. Security: Extracting Teacher ID from JWT

### Decision: Use Thread-Local Context Holder Pattern

**Rationale**:
- JWT already contains teacher_id in `subject` claim (verified in auth-service)
- Avoid passing teacher_id as parameter through all service layers
- Spring Security sets authentication in SecurityContext (thread-local)
- We can mirror this pattern for teacher_id

**Implementation Approach**:

**TeacherContextHolder** (new class):
```java
@Component
public class TeacherContextHolder {
    private static final ThreadLocal<UUID> teacherIdHolder = new ThreadLocal<>();

    public static void setTeacherId(UUID teacherId) {
        teacherIdHolder.set(teacherId);
    }

    public static UUID getTeacherId() {
        UUID teacherId = teacherIdHolder.get();
        if (teacherId == null) {
            throw new UnauthorizedAccessException("No teacher context found");
        }
        return teacherId;
    }

    public static void clear() {
        teacherIdHolder.remove();
    }
}
```

**JWT Filter Enhancement** (modify existing `JwtAuthenticationFilter`):
```java
@Override
protected void doFilterInternal(HttpServletRequest request, ...) {
    try {
        String token = extractToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            UUID teacherId = jwtTokenProvider.getUserIdFromToken(token);
            TeacherContextHolder.setTeacherId(teacherId);  // NEW LINE
            // ... existing authentication setup
        }
    } finally {
        TeacherContextHolder.clear();  // NEW LINE - prevent thread leak
    }
}
```

**Service Layer Usage**:
```java
@Service
public class StudentService implements IStudentService {
    public List<StudentResponse> getAllStudents() {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        return studentRepository.findAllByTeacherId(teacherId);
    }
}
```

**Alternatives Considered**:
- **Pass teacherId as parameter**: Rejected because it clutters method signatures and can be forgotten
- **Extract from SecurityContext**: Rejected because it couples service layer to Spring Security details
- **Request-scoped bean**: Rejected because ThreadLocal is simpler and proven pattern

---

## 4. Cache Eviction Strategy for Manual Reload

### Decision: Provide `/api/cache/reload` Endpoint with Pattern-Based Eviction

**Rationale**:
- User requirement: "user able to reload cache"
- Teachers should only evict their own cache (not global cache clear)
- Use Spring's `@CacheEvict` with wildcard pattern matching

**Implementation Approach**:

**CacheController** (new):
```java
@RestController
@RequestMapping("/api/cache")
public class CacheController {

    @PostMapping("/reload")
    public ApiResponse<CacheReloadResponse> reloadCache() {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        cacheService.evictTeacherCache(teacherId);

        return ApiResponse.success(CacheReloadResponse.builder()
            .teacherId(teacherId)
            .cacheCleared(true)
            .timestamp(LocalDateTime.now())
            .build());
    }
}
```

**CacheService** (new):
```java
@Service
public class CacheService implements ICacheService {
    private final CacheManager cacheManager;

    @Override
    public void evictTeacherCache(UUID teacherId) {
        Cache studentsCache = cacheManager.getCache("students");
        if (studentsCache != null) {
            // Evict all keys starting with teacherId
            studentsCache.evict(teacherId + ":all");
            // Note: Individual student cache entries will naturally expire via TTL
        }
    }
}
```

**Alternatives Considered**:
- **Clear entire Redis cache**: Rejected because it affects all teachers (security violation)
- **Only evict specific keys**: Rejected because requires tracking all possible cache keys
- **No manual eviction**: Rejected because user explicitly requested "reload cache" capability

---

## 5. Authorization Error Handling

### Decision: Custom Exception with Global Handler

**Rationale**:
- Constitution Principle VI: Return machine-readable error codes
- Consistent error format across all endpoints
- 401 status code for unauthorized access

**Implementation Approach**:

**UnauthorizedAccessException** (new):
```java
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
```

**GlobalExceptionHandler** (modify existing):
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(ErrorCode.UNAUTHORIZED_ACCESS));
    }
}
```

**ErrorCode enum** (add to existing):
```java
public enum ErrorCode {
    SUCCESS,
    UNAUTHORIZED_ACCESS,  // NEW
    STUDENT_NOT_FOUND,
    // ... existing codes
}
```

---

## 6. Database Migration Strategy

### Decision: Flyway Migration with Default Value and Backfill

**Rationale**:
- Flyway already in use for database versioning
- Backward-compatible migration (nullable column first)
- Backfill existing students with a default teacher or mark as unassigned

**Implementation Approach**:

**Migration File**: `V6__add_teacher_id_to_students.sql`
```sql
-- Step 1: Add nullable column
ALTER TABLE students ADD COLUMN teacher_id UUID;

-- Step 2: Create index for performance
CREATE INDEX idx_students_teacher_id ON students(teacher_id);

-- Step 3: Backfill strategy (choose one):
-- Option A: Set to a default "system" teacher (if one exists)
-- UPDATE students SET teacher_id = 'system-teacher-uuid' WHERE teacher_id IS NULL;

-- Option B: Mark as unassigned (teacher_id remains NULL until explicitly assigned)
-- No action needed

-- Step 4: Make column non-nullable (after backfill)
-- ALTER TABLE students ALTER COLUMN teacher_id SET NOT NULL;
```

**Migration Strategy Decision**:
- **Chosen**: Keep `teacher_id` nullable initially
- **Rationale**: Existing students may not have a teacher assigned yet; forcing NOT NULL would block the migration
- **Post-Migration**: Application logic will enforce teacher_id on new student creation

**Alternatives Considered**:
- **Immediate NOT NULL constraint**: Rejected because existing students don't have teacher assignments
- **Separate database per teacher**: Rejected because constitution mandates shared database with logical separation

---

## 7. Testing Strategy

### Decision: Multi-Layer Testing with Testcontainers

**Rationale**:
- Unit tests: Service layer validation logic
- Integration tests: Repository queries + Redis cache behavior
- Contract tests: API endpoints return correct error codes

**Testing Layers**:

**1. Unit Tests** (`StudentServiceTest.java`):
```java
@Test
void getAllStudents_shouldOnlyReturnTeacherStudents() {
    UUID teacherId = UUID.randomUUID();
    TeacherContextHolder.setTeacherId(teacherId);

    studentService.getAllStudents();

    verify(studentRepository).findAllByTeacherId(teacherId);
}
```

**2. Integration Tests** (`StudentRepositoryTest.java`):
```java
@DataJpaTest
class StudentRepositoryTest {
    @Test
    void findByIdAndTeacherId_shouldReturnEmptyForWrongTeacher() {
        UUID teacherA = UUID.randomUUID();
        UUID teacherB = UUID.randomUUID();
        Student student = createStudent(teacherA);

        Optional<Student> result = repo.findByIdAndTeacherId(student.getId(), teacherB);

        assertThat(result).isEmpty();
    }
}
```

**3. Cache Tests** (`CacheServiceTest.java`):
```java
@SpringBootTest
class CacheServiceTest {
    @Test
    void evictTeacherCache_shouldClearOnlyTeacherCache() {
        cacheService.evictTeacherCache(teacherA);

        // Verify teacherA cache is cleared but teacherB cache remains
    }
}
```

---

## Summary of Decisions

| Area | Decision | Rationale |
|------|----------|-----------|
| Caching | Spring Cache + Redis with TTL + manual eviction | Performance + user control |
| JPA Queries | Custom @Query with explicit teacher_id filters | Security + code reviewability |
| Teacher ID Extraction | ThreadLocal context holder | Clean service layer, proven pattern |
| Authorization | Custom exception + global handler | Consistent error format |
| Migration | Flyway with nullable teacher_id | Backward compatibility |
| Testing | Multi-layer (unit, integration, contract) | Comprehensive coverage |

**Next Phase**: Data model design and API contract specification (Phase 1)
