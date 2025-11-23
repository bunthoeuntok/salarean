# API Contract: Student Enrollment Management

**Feature**: 004-student-enrollment
**Service**: student-service
**Base Path**: `/api/students`
**Date**: 2025-11-23

---

## Authentication & Authorization

**All endpoints require**:
- Valid JWT token in `Authorization: Bearer <token>` header
- User role: `ADMIN` or `TEACHER` (enforced by Spring Security)

**Error Responses** (from existing security filter):
- `401 Unauthorized` - Missing or invalid JWT token (errorCode: `UNAUTHORIZED`)
- `403 Forbidden` - Insufficient permissions (errorCode: `FORBIDDEN`)

---

## API Endpoints

### 1. Get Student Enrollment History

**Endpoint**: `GET /api/students/{id}/enrollment-history`

**Description**: Retrieve complete enrollment history for a student, including all past and current class enrollments.

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | UUID | Yes | Student ID |

**Response**: `200 OK`

```json
{
  "errorCode": "SUCCESS",
  "data": {
    "enrollments": [
      {
        "id": "770e8400-e29b-41d4-a716-446655440002",
        "studentId": "880e8400-e29b-41d4-a716-446655440003",
        "classId": "550e8400-e29b-41d4-a716-446655440000",
        "className": "Grade 6 - Section A",
        "schoolName": "Phnom Penh Primary School",
        "enrollmentDate": "2025-09-01",
        "endDate": null,
        "reason": "PROMOTION",
        "status": "ACTIVE",
        "transferDate": null,
        "transferReason": null,
        "notes": null,
        "createdAt": "2025-09-01T08:00:00",
        "updatedAt": "2025-09-01T08:00:00"
      },
      {
        "id": "770e8400-e29b-41d4-a716-446655440001",
        "studentId": "880e8400-e29b-41d4-a716-446655440003",
        "classId": "550e8400-e29b-41d4-a716-446655440000",
        "className": "Grade 5 - Section A",
        "schoolName": "Phnom Penh Primary School",
        "enrollmentDate": "2024-09-01",
        "endDate": "2025-06-30",
        "reason": "NEW",
        "status": "COMPLETED",
        "transferDate": null,
        "transferReason": null,
        "notes": "Completed successfully",
        "createdAt": "2024-09-01T08:00:00",
        "updatedAt": "2025-06-30T10:00:00"
      }
    ],
    "totalCount": 2,
    "activeCount": 1,
    "completedCount": 1,
    "transferredCount": 0
  }
}
```

**Error Responses**:

| HTTP Status | Error Code | Description |
|-------------|------------|-------------|
| `404 Not Found` | `STUDENT_NOT_FOUND` | Student with given ID does not exist |
| `500 Internal Server Error` | `INTERNAL_ERROR` | Unexpected server error |

**Business Rules**:
- Returns all enrollments ordered by `enrollmentDate DESC` (most recent first)
- Empty list returned if student has no enrollments (not an error)
- Includes denormalized `className` and `schoolName` for UI convenience
- Status counts provide enrollment summary

---

### 2. Enroll Student in Class

**Endpoint**: `POST /api/students/{id}/enroll`

**Description**: Enroll a student in a class. Creates new enrollment record with status ACTIVE.

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | UUID | Yes | Student ID |

**Request Body**:

```json
{
  "classId": "550e8400-e29b-41d4-a716-446655440000",
  "notes": "Regular enrollment for academic year 2024-2025"
}
```

**Request Schema**:
| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `classId` | UUID | Yes | Must exist | Target class ID |
| `notes` | string | No | Max 500 chars | Optional enrollment notes |

**Response**: `200 OK`

```json
{
  "errorCode": "SUCCESS",
  "data": {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "studentId": "880e8400-e29b-41d4-a716-446655440003",
    "classId": "550e8400-e29b-41d4-a716-446655440000",
    "className": "Grade 5 - Section A",
    "schoolName": "Phnom Penh Primary School",
    "enrollmentDate": "2025-11-23",
    "endDate": null,
    "reason": "NEW",
    "status": "ACTIVE",
    "transferDate": null,
    "transferReason": null,
    "notes": "Regular enrollment for academic year 2024-2025",
    "createdAt": "2025-11-23T14:30:00",
    "updatedAt": "2025-11-23T14:30:00"
  }
}
```

**Error Responses**:

| HTTP Status | Error Code | Description |
|-------------|------------|-------------|
| `400 Bad Request` | `VALIDATION_ERROR` | Invalid request body (missing required fields) |
| `404 Not Found` | `STUDENT_NOT_FOUND` | Student with given ID does not exist |
| `404 Not Found` | `CLASS_NOT_FOUND` | Class with given ID does not exist |
| `409 Conflict` | `DUPLICATE_ENROLLMENT` | Student already actively enrolled in this class |
| `409 Conflict` | `CLASS_CAPACITY_EXCEEDED` | Class has reached maximum capacity |
| `500 Internal Server Error` | `INTERNAL_ERROR` | Unexpected server error |

**Business Rules**:
- `enrollmentDate` automatically set to current date
- `reason` automatically set to `NEW`
- `status` automatically set to `ACTIVE`
- Validates student existence (FR-007)
- Validates class existence (FR-008)
- Checks class capacity before enrollment (FR-009)
- Prevents duplicate active enrollments (FR-006)
- Increments class `student_count` in same transaction

---

### 3. Transfer Student to New Class

**Endpoint**: `POST /api/students/{id}/transfer`

**Description**: Transfer a student from their current active enrollment to a new class. Atomic operation that closes the old enrollment and creates a new one.

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | UUID | Yes | Student ID |

**Request Body**:

```json
{
  "targetClassId": "660e8400-e29b-41d4-a716-446655440001",
  "reason": "Student requested transfer due to scheduling conflict"
}
```

**Request Schema**:
| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `targetClassId` | UUID | Yes | Must exist | Target class ID |
| `reason` | string | Yes | Max 500 chars | Transfer reason (auditable) |

**Response**: `200 OK`

```json
{
  "errorCode": "SUCCESS",
  "data": {
    "id": "880e8400-e29b-41d4-a716-446655440004",
    "studentId": "880e8400-e29b-41d4-a716-446655440003",
    "classId": "660e8400-e29b-41d4-a716-446655440001",
    "className": "Grade 5 - Section B",
    "schoolName": "Phnom Penh Primary School",
    "enrollmentDate": "2025-11-23",
    "endDate": null,
    "reason": "TRANSFER",
    "status": "ACTIVE",
    "transferDate": null,
    "transferReason": null,
    "notes": "Student requested transfer due to scheduling conflict",
    "createdAt": "2025-11-23T14:35:00",
    "updatedAt": "2025-11-23T14:35:00"
  }
}
```

**Error Responses**:

| HTTP Status | Error Code | Description |
|-------------|------------|-------------|
| `400 Bad Request` | `VALIDATION_ERROR` | Invalid request body (missing required fields) |
| `404 Not Found` | `STUDENT_NOT_FOUND` | Student with given ID does not exist |
| `404 Not Found` | `ENROLLMENT_NOT_FOUND` | Student has no active enrollment to transfer from |
| `404 Not Found` | `CLASS_NOT_FOUND` | Target class with given ID does not exist |
| `409 Conflict` | `CLASS_CAPACITY_EXCEEDED` | Target class has reached maximum capacity |
| `500 Internal Server Error` | `INTERNAL_ERROR` | Unexpected server error (transaction rollback) |

**Business Rules**:
- Validates student has exactly one active enrollment (FR-015)
- Validates target class existence and capacity (FR-016)
- Atomic transaction (FR-018):
  1. Marks old enrollment as `TRANSFERRED` with `transfer_date` and `transfer_reason`
  2. Creates new enrollment with `reason=TRANSFER`, `status=ACTIVE`
  3. Updates both class `student_count` fields (decrement old, increment new)
  4. All steps succeed or entire transaction rolls back
- Transfer reason recorded for audit trail (FR-017)
- Returns the newly created enrollment (not the old one)

---

## Common Response Structure

All endpoints follow the standardized `ApiResponse<T>` pattern:

```json
{
  "errorCode": "ERROR_CODE_ENUM",
  "data": <T | null>
}
```

**Success Response**:
- `errorCode`: Always `"SUCCESS"`
- `data`: Response payload (varies by endpoint)

**Error Response**:
- `errorCode`: Machine-readable error code (e.g., `STUDENT_NOT_FOUND`)
- `data`: Always `null`

**Frontend Responsibility**:
- Map `errorCode` to localized error messages (English/Khmer)
- Handle all i18n translation (backend returns codes only)

---

## Error Code Reference

### New Error Codes (StudentErrorCode enum)

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `DUPLICATE_ENROLLMENT` | 409 Conflict | Student already enrolled in class |
| `ENROLLMENT_NOT_FOUND` | 404 Not Found | No active enrollment found for student |
| `INVALID_ENROLLMENT_STATUS` | 400 Bad Request | Enrollment status transition not allowed |

### Existing Error Codes (reused)

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `SUCCESS` | 200 OK | Operation completed successfully |
| `VALIDATION_ERROR` | 400 Bad Request | Request validation failed |
| `STUDENT_NOT_FOUND` | 404 Not Found | Student not found |
| `CLASS_NOT_FOUND` | 404 Not Found | Class not found |
| `CLASS_CAPACITY_EXCEEDED` | 409 Conflict | Class is full |
| `UNAUTHORIZED` | 401 Unauthorized | Invalid/missing JWT token |
| `FORBIDDEN` | 403 Forbidden | Insufficient permissions |
| `INTERNAL_ERROR` | 500 Internal Server Error | Unexpected error |

---

## OpenAPI Specification

**Controller Annotations**:

```java
@RestController
@RequestMapping("/api/students")
@Tag(name = "Student Enrollment", description = "Enrollment management APIs")
@RequiredArgsConstructor
public class EnrollmentController {

    @GetMapping("/{id}/enrollment-history")
    @Operation(
        summary = "Get student enrollment history",
        description = "Retrieve complete enrollment history for a student"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public ResponseEntity<ApiResponse<EnrollmentHistoryResponse>> getEnrollmentHistory(
        @PathVariable UUID id
    );

    @PostMapping("/{id}/enroll")
    @Operation(
        summary = "Enroll student in class",
        description = "Create new enrollment record for student in specified class"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Student enrolled successfully"),
        @ApiResponse(responseCode = "404", description = "Student or class not found"),
        @ApiResponse(responseCode = "409", description = "Duplicate enrollment or capacity exceeded")
    })
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollStudent(
        @PathVariable UUID id,
        @Valid @RequestBody EnrollmentRequest request
    );

    @PostMapping("/{id}/transfer")
    @Operation(
        summary = "Transfer student to new class",
        description = "Atomically transfer student from current class to new class"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Student transferred successfully"),
        @ApiResponse(responseCode = "404", description = "Student, enrollment, or class not found"),
        @ApiResponse(responseCode = "409", description = "Target class capacity exceeded")
    })
    public ResponseEntity<ApiResponse<EnrollmentResponse>> transferStudent(
        @PathVariable UUID id,
        @Valid @RequestBody TransferRequest request
    );
}
```

**Swagger UI Access**: `http://localhost:8082/swagger-ui.html`

---

## Testing Examples

### cURL Examples

**1. Get Enrollment History**:
```bash
curl -X GET "http://localhost:8082/api/students/880e8400-e29b-41d4-a716-446655440003/enrollment-history" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json"
```

**2. Enroll Student**:
```bash
curl -X POST "http://localhost:8082/api/students/880e8400-e29b-41d4-a716-446655440003/enroll" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "classId": "550e8400-e29b-41d4-a716-446655440000",
    "notes": "Regular enrollment"
  }'
```

**3. Transfer Student**:
```bash
curl -X POST "http://localhost:8082/api/students/880e8400-e29b-41d4-a716-446655440003/transfer" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "targetClassId": "660e8400-e29b-41d4-a716-446655440001",
    "reason": "Scheduling conflict"
  }'
```

---

## Contract Testing Strategy

### Test Cases

**Positive Tests**:
1. Enroll student successfully (200 OK)
2. Transfer student successfully (200 OK)
3. Get history for student with enrollments (200 OK)
4. Get history for student with no enrollments (200 OK, empty list)

**Negative Tests**:
1. Enroll with invalid student ID (404 STUDENT_NOT_FOUND)
2. Enroll with invalid class ID (404 CLASS_NOT_FOUND)
3. Enroll when class is full (409 CLASS_CAPACITY_EXCEEDED)
4. Enroll duplicate (409 DUPLICATE_ENROLLMENT)
5. Transfer with no active enrollment (404 ENROLLMENT_NOT_FOUND)
6. Transfer when target class full (409 CLASS_CAPACITY_EXCEEDED)
7. Missing JWT token (401 UNAUTHORIZED)
8. Invalid role (403 FORBIDDEN)

**Performance Tests**:
1. Enrollment history with 1000+ records < 2 seconds
2. Enrollment operation < 1 second
3. Transfer operation < 1 second
4. Concurrent enrollments (verify capacity not exceeded)

---

## Summary

**Endpoints Defined**: 3 REST APIs
**Authentication**: JWT required for all endpoints
**Response Format**: Standardized `ApiResponse<T>` wrapper
**Error Codes**: 8 codes (3 new, 5 existing)
**Performance**: < 2s for history, < 1s for mutations
**Atomicity**: Transfer guaranteed atomic via `@Transactional`
