# Phase 0: Research & Technical Decisions

**Feature**: Class Management API (student-service extension)
**Date**: 2025-11-24
**Status**: Complete

## Overview

This document captures technical research and design decisions for extending student-service with class management capabilities. All architectural decisions incorporate clarifications from the specification phase.

---

## 1. Cache Key Naming Conventions

### Decision

Standardized cache key format for consistency:

```
{service-name}:{entity-type}:{identifier}:{optional-suffix}
```

**Examples for student-service**:
- Teacher class list: `student-service:teacher:classes:{teacherId}`
- Class details: `student-service:class:{classId}`
- Class students roster: `student-service:class:{classId}:students`
- Enrollment history: `student-service:class:{classId}:history`

###Rationale

- **Consistent format**: All services use same pattern
- **Service prefix**: Prevents key collisions when multiple services use Redis
- **Hierarchical structure**: Colon-separated enables Redis pattern matching for bulk invalidation
- **Centralized generator**: sms-common provides utility

### Cache TTL Strategy

| Cache Type | TTL | Justification |
|------------|-----|---------------|
| Teacher class list | 30 minutes | Updated infrequently (class creation/archive events) |
| Class details + students | 15 minutes | Student roster changes with enrollments |
| Enrollment history | 60 minutes | Historical data rarely changes |

---

## 2. Academic Year Validation

### Decision

Format: `"YYYY-YYYY"` with validation: second year = first year + 1

**Validation Logic**:
```java
@Pattern(regexp = "^\\d{4}-\\d{4}$", message = "Academic year must be in format YYYY-YYYY")
public String academicYear;

// Custom validator
public boolean isValidAcademicYear(String academicYear) {
    String[] parts = academicYear.split("-");
    int firstYear = Integer.parseInt(parts[0]);
    int secondYear = Integer.parseInt(parts[1]);
    return secondYear == firstYear + 1;
}
```

### Rationale

- Standard format for academic years spanning calendar years
- Enables clear distinction between classes across years
- Prevents typos and invalid data

---

## 3. Enrollment History Query Optimization

### Decision

**Read-Only Access Pattern** (per clarification #5):
- ClassController → ClassService → EnrollmentHistoryRepository (read-only queries)
- **NO write operations** in class management feature
- Enrollment records created by separate enrollment feature

**Optimized Query**:
```java
@Query("SELECT eh FROM EnrollmentHistory eh " +
       "WHERE eh.classId = :classId " +
       "ORDER BY eh.eventTimestamp DESC")
List<EnrollmentHistory> findByClassIdOrderByEventTimestampDesc(
    @Param("classId") UUID classId
);
```

**Indexes**:
```sql
CREATE INDEX idx_enrollment_class_timestamp
ON enrollment_history(class_id, event_timestamp DESC);
```

### Rationale

- DESC index on timestamp supports "newest first" requirement
- Composite index (class_id + timestamp) optimizes filtered queries
- Read-only access simplifies caching (no invalidation on write)

---

## 4. Redis Configuration for student-service

### Decision

**Spring Data Redis Configuration**:

```yaml
# application.yml (local)
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms

# application-docker.yml
spring:
  redis:
    host: redis
    port: 6379
    timeout: 2000ms
```

**Java Configuration** (RedisConfig.java):
```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)))
            .build();
    }
}
```

### Rationale

- Jackson2JsonRedisSerializer for human-readable cached values
- Configurable TTL per cache type
- Connection timeout prevents hanging on Redis failures

---

## 5. Flyway Migration Numbering

### Decision

**Continue from existing student-service migrations**:
- Check highest existing migration number in `student-service/src/main/resources/db/migration/`
- Assume existing migrations: V1__ through V4__ (student tables)
- **New migrations**:
  - `V5__create_classes_table.sql`
  - `V6__create_enrollment_history_table.sql`

### Rationale

- Maintains chronological order
- Prevents conflicts with existing migrations
- Flyway validates version sequence automatically

---

## 6. Grade Level Enumeration (from Clarification #2)

### Decision

**12 values** following Cambodia MoEYS structure:

```java
public enum GradeLevel {
    // Primary (6 years)
    GRADE_1, GRADE_2, GRADE_3, GRADE_4, GRADE_5, GRADE_6,

    // Lower Secondary (3 years)
    GRADE_7, GRADE_8, GRADE_9,

    // Upper Secondary (3 years)
    GRADE_10, GRADE_11, GRADE_12
}
```

### Rationale

- Matches Cambodia's 6-3-3 education structure
- Enum provides type safety and validation
- No kindergarten levels (per clarification)

---

## 7. Schedule Data Exclusion (from Clarification #3)

### Decision

**NO schedule fields** in ClassEntity:
- No `schedule_json` column
- No `ScheduleSession` DTO
- No schedule validation logic

**Future Integration**:
- When schedule service is implemented, classes will reference schedules by ID
- Relationship: Class → Schedule (external service, eventual consistency)

### Rationale

- Deferred to future dedicated schedule service
- Reduces current implementation scope
- Avoids premature data model design

---

## 8. sms-common Cache Framework

### Decision

**Abstract interface** in sms-common:

```java
public interface CacheService {
    <T> Optional<T> get(String key, Class<T> type);
    <T> void put(String key, T value, Duration ttl);
    void evict(String key);
    void evictPattern(String pattern);
}
```

**Redis implementation**:
```java
@Service
public class RedisCacheService implements CacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(objectMapper.convertValue(cached, type));
        } catch (Exception e) {
            log.warn("Cache get failed for key: {}", key, e);
            return Optional.empty();  // Graceful degradation
        }
    }

    // ... other methods
}
```

### Rationale

- Interface enables future cache implementations (Memcached, Hazelcast)
- Graceful degradation: cache failures never break application
- Centralized in sms-common for reuse across services

---

## Research Summary

| Research Item | Decision | Status |
|---------------|----------|--------|
| Cache key format | `service:entity:id:suffix` pattern | ✅ Complete |
| Academic year validation | "YYYY-YYYY" with year+1 check | ✅ Complete |
| Enrollment history access | Read-only queries, no writes | ✅ Complete |
| Redis configuration | Spring Data Redis with Jackson serialization | ✅ Complete |
| Flyway migrations | V5__, V6__ (continue from V4__) | ✅ Complete |
| Grade levels | GRADE_1 to GRADE_12 (12 values) | ✅ Complete |
| Schedule data | Excluded - future service | ✅ Complete |
| Base cache framework | Abstract interface in sms-common | ✅ Complete |

**All technical decisions resolved. Ready for Phase 1 design.**

---

**Phase 0 Status**: ✅ COMPLETE
