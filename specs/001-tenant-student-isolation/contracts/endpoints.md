# API Contracts: Teacher-Based Student Data Isolation

**Feature**: 001-tenant-student-isolation
**Date**: 2025-12-07
**Phase**: 1 (Design & Contracts)
**Base URL**: `http://localhost:8080/student-service` (via API Gateway)

## Overview

This document specifies the API contract changes for student endpoints to enforce teacher-based data isolation. All endpoints require JWT authentication and automatically filter/validate by the authenticated teacher's ID.

---

## Authentication

**All endpoints require JWT authentication via Authorization header:**

```
Authorization: Bearer <jwt_token>
```

**JWT Claims Used**:
- `sub` (subject): Contains teacher_id (UUID)
- `roles`: Contains ["TEACHER"]

**Teacher ID Extraction**:
Teacher ID is automatically extracted from JWT by `JwtAuthenticationFilter` and stored in `TeacherContextHolder` for the request duration.

---

## Modified Endpoints

### 1. GET /api/students
**List all students for authenticated teacher**

**Changes**:
- ✅ Automatically filters by teacher_id from JWT
- ✅ Returns only students owned by authenticated teacher
- ✅ Cached with teacher-scoped key

#### Request
```http
GET /api/students HTTP/1.1
Host: localhost:8080
Authorization: Bearer <jwt_token>
```

#### Success Response (200 OK)
```json
{
  "errorCode": "SUCCESS",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "studentCode": "STU-2024-001",
      "firstName": "Sok",
      "lastName": "Chan",
      "firstNameKhmer": "សុខ",
      "lastNameKhmer": "ច័ន្ទ",
      "dateOfBirth": "2010-05-15",
      "gender": "M",
      "photoUrl": "/uploads/students/550e8400.jpg",
      "address": "Phnom Penh, Cambodia",
      "emergencyContact": "+855-12-345-678",
      "enrollmentDate": "2024-01-15",
      "status": "ACTIVE",
      "teacherId": "7f3e8a10-b5c4-4d2a-9f1e-2c6d8b4a3f9e",  // Always matches authenticated teacher
      "age": 14,
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    },
    {
      "id": "661f9511-f3ac-52e5-b827-557766551111",
      "studentCode": "STU-2024-002",
      "firstName": "Dara",
      "lastName": "Kim",
      // ... (more student objects)
    }
  ]
}
```

#### Error Response (401 Unauthorized)
```json
{
  "errorCode": "UNAUTHORIZED",
  "data": null
}
```

**Cache Behavior**:
- Cache Key: `students:{teacherId}:all`
- TTL: 30 minutes
- Evicted on: Student create/update/delete, manual cache reload

---

### 2. GET /api/students/{id}
**Get single student by ID (teacher ownership validated)**

**Changes**:
- ✅ Validates student belongs to authenticated teacher
- ✅ Returns 401 if student belongs to different teacher
- ✅ Cached with teacher-scoped key

#### Request
```http
GET /api/students/550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
Host: localhost:8080
Authorization: Bearer <jwt_token>
```

#### Success Response (200 OK)
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "studentCode": "STU-2024-001",
    "firstName": "Sok",
    "lastName": "Chan",
    // ... (full student object)
    "parentContacts": [
      {
        "id": "parent-uuid-1",
        "relationship": "FATHER",
        "name": "Chan Bora",
        "phoneNumber": "+855-12-345-678",
        "email": "bora.chan@example.com"
      }
    ]
  }
}
```

#### Error Responses

**401 Unauthorized** (student belongs to different teacher):
```json
{
  "errorCode": "UNAUTHORIZED_ACCESS",
  "data": null
}
```

**404 Not Found** (student doesn't exist):
```json
{
  "errorCode": "STUDENT_NOT_FOUND",
  "data": null
}
```

**Cache Behavior**:
- Cache Key: `students:{teacherId}:{studentId}`
- TTL: 30 minutes
- Evicted on: Student update/delete

---

### 3. POST /api/students
**Create new student (auto-assign teacher_id)**

**Changes**:
- ✅ Automatically sets `teacher_id` from JWT (not provided in request body)
- ✅ Sets `createdBy` to authenticated teacher ID
- ✅ Evicts teacher's student list cache

#### Request
```http
POST /api/students HTTP/1.1
Host: localhost:8080
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "studentCode": "STU-2024-003",
  "firstName": "Sopheap",
  "lastName": "Rath",
  "firstNameKhmer": "សុភាព",
  "lastNameKhmer": "រ៉ាត់",
  "dateOfBirth": "2011-08-20",
  "gender": "F",
  "address": "Siem Reap, Cambodia",
  "emergencyContact": "+855-16-789-012",
  "enrollmentDate": "2024-12-07",
  "parentContacts": [
    {
      "relationship": "MOTHER",
      "name": "Rath Srey",
      "phoneNumber": "+855-16-789-012",
      "email": "srey.rath@example.com"
    }
  ]
}
```

**Note**: `teacher_id` is NOT in request body - it's auto-assigned from JWT.

#### Success Response (201 Created)
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "id": "772g0622-g4bd-63f6-c938-668877662222",
    "studentCode": "STU-2024-003",
    "firstName": "Sopheap",
    "lastName": "Rath",
    "teacherId": "7f3e8a10-b5c4-4d2a-9f1e-2c6d8b4a3f9e",  // Auto-assigned
    "createdBy": "7f3e8a10-b5c4-4d2a-9f1e-2c6d8b4a3f9e",  // Same as teacherId
    // ... (full student object)
  }
}
```

#### Error Responses

**400 Bad Request** (validation error):
```json
{
  "errorCode": "INVALID_INPUT",
  "data": null
}
```

**409 Conflict** (duplicate student code):
```json
{
  "errorCode": "DUPLICATE_STUDENT_CODE",
  "data": null
}
```

**Cache Behavior**:
- Evicts: `students:{teacherId}:all`
- Adds: `students:{teacherId}:{newStudentId}`

---

### 4. PUT /api/students/{id}
**Update student (teacher ownership validated)**

**Changes**:
- ✅ Validates student belongs to authenticated teacher before update
- ✅ Returns 401 if student belongs to different teacher
- ✅ Preserves original `teacher_id` (cannot be changed)
- ✅ Updates `updatedBy` to authenticated teacher ID
- ✅ Evicts student cache

#### Request
```http
PUT /api/students/550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
Host: localhost:8080
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "firstName": "Sok",
  "lastName": "Chan",
  "firstNameKhmer": "សុខ",
  "lastNameKhmer": "ច័ន្ទ",
  "dateOfBirth": "2010-05-15",
  "gender": "M",
  "address": "Phnom Penh, Street 123",  // Updated
  "emergencyContact": "+855-12-999-888",  // Updated
  "photoUrl": "/uploads/students/550e8400.jpg"
}
```

**Note**: `teacher_id` cannot be changed (immutable after creation).

#### Success Response (200 OK)
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "address": "Phnom Penh, Street 123",
    "emergencyContact": "+855-12-999-888",
    "updatedBy": "7f3e8a10-b5c4-4d2a-9f1e-2c6d8b4a3f9e",
    "updatedAt": "2024-12-07T14:25:00Z",
    // ... (full updated student object)
  }
}
```

#### Error Responses

**401 Unauthorized** (student belongs to different teacher):
```json
{
  "errorCode": "UNAUTHORIZED_ACCESS",
  "data": null
}
```

**404 Not Found** (student doesn't exist):
```json
{
  "errorCode": "STUDENT_NOT_FOUND",
  "data": null
}
```

**Cache Behavior**:
- Evicts: `students:{teacherId}:all`
- Evicts: `students:{teacherId}:{studentId}`

---

### 5. DELETE /api/students/{id}
**Soft delete student (teacher ownership validated)**

**Changes**:
- ✅ Validates student belongs to authenticated teacher before deletion
- ✅ Returns 401 if student belongs to different teacher
- ✅ Sets `deletedBy` to authenticated teacher ID
- ✅ Evicts student cache

#### Request
```http
DELETE /api/students/550e8400-e29b-41d4-a716-446655440000?reason=Transferred%20to%20another%20school HTTP/1.1
Host: localhost:8080
Authorization: Bearer <jwt_token>
```

**Query Parameters**:
- `reason` (optional): Reason for deletion (max 500 chars)

#### Success Response (200 OK)
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "status": "INACTIVE",
    "deletionReason": "Transferred to another school",
    "deletedAt": "2024-12-07T14:30:00Z",
    "deletedBy": "7f3e8a10-b5c4-4d2a-9f1e-2c6d8b4a3f9e"
  }
}
```

#### Error Responses

**401 Unauthorized** (student belongs to different teacher):
```json
{
  "errorCode": "UNAUTHORIZED_ACCESS",
  "data": null
}
```

**404 Not Found** (student doesn't exist):
```json
{
  "errorCode": "STUDENT_NOT_FOUND",
  "data": null
}
```

**Cache Behavior**:
- Evicts: `students:{teacherId}:all`
- Evicts: `students:{teacherId}:{studentId}`

---

## New Endpoints

### 6. POST /api/cache/reload
**Manually reload cache for authenticated teacher**

**Purpose**: Allow teachers to force-refresh their student data cache

#### Request
```http
POST /api/cache/reload HTTP/1.1
Host: localhost:8080
Authorization: Bearer <jwt_token>
```

#### Success Response (200 OK)
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "teacherId": "7f3e8a10-b5c4-4d2a-9f1e-2c6d8b4a3f9e",
    "cacheCleared": true,
    "timestamp": "2024-12-07T14:35:00Z",
    "message": "Cache reloaded successfully"
  }
}
```

#### Error Response (401 Unauthorized)
```json
{
  "errorCode": "UNAUTHORIZED",
  "data": null
}
```

**Cache Behavior**:
- Evicts: `students:{teacherId}:all`
- Individual student caches (`students:{teacherId}:{studentId}`) remain (will expire via TTL)

---

## Error Codes

### New Error Codes

| Error Code | HTTP Status | Description | When Used |
|------------|-------------|-------------|-----------|
| `UNAUTHORIZED_ACCESS` | 401 | Student belongs to different teacher | GET/PUT/DELETE student owned by another teacher |
| `TEACHER_CONTEXT_MISSING` | 401 | Teacher ID not found in JWT | JWT token missing or invalid |

### Existing Error Codes (Unchanged)

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `SUCCESS` | 200/201 | Operation successful |
| `STUDENT_NOT_FOUND` | 404 | Student ID doesn't exist |
| `DUPLICATE_STUDENT_CODE` | 409 | Student code already exists |
| `INVALID_INPUT` | 400 | Validation error in request body |
| `UNAUTHORIZED` | 401 | JWT token missing or invalid |

---

## Security Constraints

### Authorization Rules

1. **All endpoints require authentication**:
   - JWT token must be present in `Authorization` header
   - Token must be valid and not expired
   - Token must contain `teacher_id` in subject claim

2. **Teacher ownership validation**:
   - **Read operations** (GET): Only return students where `student.teacher_id == authenticated_teacher_id`
   - **Write operations** (POST): Auto-assign `teacher_id` from JWT
   - **Update operations** (PUT/DELETE): Reject if `student.teacher_id != authenticated_teacher_id`

3. **Cache isolation**:
   - Cache keys always include teacher_id
   - Cache eviction only affects authenticated teacher's cache
   - No cross-teacher cache pollution possible

### SQL Injection Prevention

All queries use JPA parameterized queries:

```java
@Query("SELECT s FROM Student s WHERE s.teacherId = :teacherId")
List<Student> findAllByTeacherId(@Param("teacherId") UUID teacherId);
```

**Not vulnerable to**:
- SQL injection (parameterized queries)
- Teacher ID manipulation (extracted from validated JWT, not request params)
- Cache poisoning (keys scoped by teacher_id)

---

## Performance Guarantees

### Response Time Targets

| Endpoint | Without Cache | With Cache | Target (SC-001) |
|----------|--------------|------------|-----------------|
| GET /api/students | ~500ms | ~50ms | <2s |
| GET /api/students/{id} | ~100ms | ~10ms | <200ms |
| POST /api/students | ~200ms | N/A | <500ms |
| PUT /api/students/{id} | ~200ms | N/A | <500ms |
| DELETE /api/students/{id} | ~150ms | N/A | <500ms |
| POST /api/cache/reload | ~50ms | N/A | <100ms |

### Concurrent Access

- ✅ Supports 50+ concurrent teachers (per SC-004)
- ✅ No race conditions (each teacher isolated via cache keys)
- ✅ No deadlocks (read-mostly workload, optimistic locking on writes)

---

## API Versioning

**Current Version**: v1 (no version prefix in URL)
**Base Path**: `/api/students`

**Future Considerations**:
- If breaking changes needed, use `/api/v2/students`
- Current endpoint maintains backward compatibility (nullable teacher_id)

---

## Testing Scenarios

### Contract Tests

1. **Unauthorized access attempt**:
   ```
   Given: Teacher A creates a student
   And: Student ID is known
   When: Teacher B attempts GET /api/students/{student_id}
   Then: Response is 401 UNAUTHORIZED_ACCESS
   ```

2. **Successful isolation**:
   ```
   Given: Teacher A has 5 students, Teacher B has 3 students
   When: Teacher A calls GET /api/students
   Then: Response contains exactly 5 students (all owned by Teacher A)
   ```

3. **Auto-assignment on create**:
   ```
   Given: Teacher A is authenticated
   When: Teacher A calls POST /api/students (without teacher_id in body)
   Then: Created student has teacher_id = Teacher A's ID
   ```

4. **Cache reload**:
   ```
   Given: Teacher A has cached student list
   When: Teacher A calls POST /api/cache/reload
   Then: Next GET /api/students refetches from database
   ```

---

## Summary

| Change Type | Endpoints Affected | Key Behavior |
|-------------|-------------------|--------------|
| **Modified** | GET /api/students | Auto-filter by teacher_id |
| **Modified** | GET /api/students/{id} | Validate ownership before return |
| **Modified** | POST /api/students | Auto-assign teacher_id from JWT |
| **Modified** | PUT /api/students/{id} | Validate ownership before update |
| **Modified** | DELETE /api/students/{id} | Validate ownership before delete |
| **New** | POST /api/cache/reload | Evict teacher's cache manually |

**Next Steps**: Developer quickstart guide (Phase 1 continued)
