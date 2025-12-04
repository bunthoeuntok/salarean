# API Contracts: Batch Student Transfer with Undo

**Date**: 2025-12-04
**Feature**: 008-batch-student-transfer
**Service**: student-service

## Overview

This document defines the REST API contracts for batch student transfer and undo functionality. All endpoints follow the project's standardized `ApiResponse<T>` wrapper pattern with error codes for i18n.

---

## 1. Batch Transfer Endpoint

### POST `/api/classes/{classId}/students/batch-transfer`

**Description**: Transfer multiple students from one class to another in a single operation.

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| classId | UUID | Yes | Source class ID (students will be transferred FROM this class) |

**Request Body**:
```json
{
  "destinationClassId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "studentIds": [
    "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
    "2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
    "3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f"
  ]
}
```

**Request Schema**:
```typescript
interface BatchTransferRequest {
  destinationClassId: string; // UUID format
  studentIds: string[]; // Array of UUIDs, min 1, max 100
}
```

**Success Response** (200 OK):
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "transferId": "4d5e6f7a-8b9c-0d1e-2f3a-4b5c6d7e8f9a",
    "sourceClassId": "5e6f7a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b",
    "destinationClassId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "successfulTransfers": 3,
    "failedTransfers": [],
    "transferredAt": "2025-12-04T10:30:00.000Z"
  }
}
```

**Partial Success Response** (200 OK):
```json
{
  "errorCode": "PARTIAL_SUCCESS",
  "data": {
    "transferId": "4d5e6f7a-8b9c-0d1e-2f3a-4b5c6d7e8f9a",
    "sourceClassId": "5e6f7a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b",
    "destinationClassId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "successfulTransfers": 2,
    "failedTransfers": [
      {
        "studentId": "3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
        "studentName": "Sok Pisey",
        "reason": "ALREADY_ENROLLED"
      }
    ],
    "transferredAt": "2025-12-04T10:30:00.000Z"
  }
}
```

**Response Schema**:
```typescript
interface ApiResponse<BatchTransferResponse> {
  errorCode: ErrorCode;
  data: BatchTransferResponse | null;
}

interface BatchTransferResponse {
  transferId: string; // UUID
  sourceClassId: string; // UUID
  destinationClassId: string; // UUID
  successfulTransfers: number;
  failedTransfers: FailedTransfer[];
  transferredAt: string; // ISO 8601 timestamp
}

interface FailedTransfer {
  studentId: string; // UUID
  studentName: string;
  reason: string; // Error code
}
```

**Error Responses**:

| Status | Error Code | Description |
|--------|-----------|-------------|
| 400 | INVALID_REQUEST | Request validation failed (missing fields, invalid format) |
| 400 | STUDENT_NOT_FOUND | One or more student IDs do not exist |
| 400 | STUDENT_NOT_ENROLLED | One or more students are not enrolled in source class |
| 400 | CLASS_NOT_FOUND | Destination class does not exist |
| 400 | CLASS_INACTIVE | Destination class is not active |
| 400 | GRADE_MISMATCH | Source and destination classes have different grade levels |
| 400 | CAPACITY_EXCEEDED | Destination class cannot accommodate all students |
| 401 | UNAUTHORIZED | User is not authenticated |
| 403 | FORBIDDEN | User does not have TRANSFER_STUDENTS permission for source class |
| 404 | CLASS_NOT_FOUND | Source class does not exist |
| 429 | RATE_LIMIT_EXCEEDED | Too many requests from this user |
| 500 | INTERNAL_ERROR | Server error occurred during transfer |

**Example Error Response** (400 Bad Request):
```json
{
  "errorCode": "CAPACITY_EXCEEDED",
  "data": null
}
```

**Validation Rules**:
1. `destinationClassId` must be a valid UUID
2. `studentIds` must be an array with 1-100 valid UUIDs
3. `studentIds` must not contain duplicates
4. All students must exist in the database
5. All students must be currently enrolled in `{classId}` with ACTIVE status
6. Destination class must exist and be ACTIVE
7. Source and destination classes must have the same grade level
8. Destination class capacity must not be exceeded
9. No student can already be enrolled in destination class
10. User must have TRANSFER_STUDENTS permission for source class

**Authorization**:
- Requires valid JWT token in `Authorization` header
- User must have `TRANSFER_STUDENTS` role/permission for the source class

**Idempotency**:
- NOT idempotent - each request creates a new transfer with a unique `transferId`
- Retrying the same request will attempt a new transfer (may fail if students already transferred)

---

## 2. Undo Transfer Endpoint

### POST `/api/transfers/{transferId}/undo`

**Description**: Reverse a batch transfer within 5 minutes of the original transfer.

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| transferId | UUID | Yes | The ID of the transfer to undo (from BatchTransferResponse) |

**Request Body**: None (empty body)

**Success Response** (200 OK):
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "transferId": "4d5e6f7a-8b9c-0d1e-2f3a-4b5c6d7e8f9a",
    "undoneStudents": 3,
    "sourceClassId": "5e6f7a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b",
    "undoneAt": "2025-12-04T10:35:00.000Z"
  }
}
```

**Response Schema**:
```typescript
interface ApiResponse<UndoTransferResponse> {
  errorCode: ErrorCode;
  data: UndoTransferResponse | null;
}

interface UndoTransferResponse {
  transferId: string; // UUID (same as request)
  undoneStudents: number; // Number of students returned to source class
  sourceClassId: string; // UUID of the original source class
  undoneAt: string; // ISO 8601 timestamp
}
```

**Error Responses**:

| Status | Error Code | Description |
|--------|-----------|-------------|
| 400 | INVALID_REQUEST | Invalid transfer ID format |
| 401 | UNAUTHORIZED | User is not authenticated |
| 403 | UNDO_UNAUTHORIZED | User did not perform the original transfer |
| 404 | TRANSFER_NOT_FOUND | Transfer ID does not exist |
| 409 | UNDO_EXPIRED | More than 5 minutes have elapsed since transfer |
| 409 | UNDO_CONFLICT | One or more students have been transferred to another class |
| 404 | SOURCE_CLASS_NOT_FOUND | Original source class has been deleted |
| 429 | RATE_LIMIT_EXCEEDED | Too many requests from this user |
| 500 | INTERNAL_ERROR | Server error occurred during undo |

**Example Error Response** (409 Conflict):
```json
{
  "errorCode": "UNDO_CONFLICT",
  "data": null
}
```

**Validation Rules**:
1. `transferId` must be a valid UUID
2. Transfer must exist in the database
3. Transfer must have occurred within last 5 minutes (server time)
4. Current user ID must match the user who performed the original transfer
5. No transferred student can have a new enrollment after the transfer timestamp
6. Original source class must still exist and be accessible

**Authorization**:
- Requires valid JWT token in `Authorization` header
- User ID must match `performedByUserId` from the original transfer

**Idempotency**:
- Idempotent - calling undo multiple times on the same transfer returns success if already undone
- Second undo attempt returns `TRANSFER_NOT_FOUND` (transfer history already reversed)

**Side Effects**:
- All students transferred in the original batch are returned to the source class
- Enrollment history records are created with `action = 'UNDO'` and `undo_of_transfer_id` set
- Student status in destination class changes from ACTIVE to historical record
- Student status in source class is restored to ACTIVE

---

## 3. Get Eligible Destination Classes Endpoint

### GET `/api/classes/{classId}/eligible-destination-classes`

**Description**: Fetch all active classes with the same grade level as the source class (for transfer destination dropdown).

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| classId | UUID | Yes | Source class ID |

**Query Parameters**: None

**Success Response** (200 OK):
```json
{
  "errorCode": "SUCCESS",
  "data": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "name": "Class 7B",
      "code": "7B-2025",
      "gradeLevel": 7,
      "capacity": 40,
      "currentEnrollment": 35,
      "teacherName": "Mr. Sok Dara"
    },
    {
      "id": "6fb85f64-5717-4562-b3fc-2c963f66afa7",
      "name": "Class 7C",
      "code": "7C-2025",
      "gradeLevel": 7,
      "capacity": 40,
      "currentEnrollment": 32,
      "teacherName": "Mrs. Chea Sophea"
    }
  ]
}
```

**Response Schema**:
```typescript
interface ApiResponse<EligibleClass[]> {
  errorCode: ErrorCode;
  data: EligibleClass[] | null;
}

interface EligibleClass {
  id: string; // UUID
  name: string;
  code: string;
  gradeLevel: number;
  capacity: number;
  currentEnrollment: number;
  teacherName: string;
}
```

**Error Responses**:

| Status | Error Code | Description |
|--------|-----------|-------------|
| 400 | INVALID_REQUEST | Invalid class ID format |
| 401 | UNAUTHORIZED | User is not authenticated |
| 404 | CLASS_NOT_FOUND | Source class does not exist |
| 429 | RATE_LIMIT_EXCEEDED | Too many requests from this user |
| 500 | INTERNAL_ERROR | Server error occurred |

**Authorization**:
- Requires valid JWT token in `Authorization` header
- No specific permission required (read-only operation)

**Filtering Logic**:
- Excludes the source class itself
- Includes only classes with `status = 'ACTIVE'`
- Includes only classes with `gradeLevel = sourceClass.gradeLevel`
- Sorted by class name alphabetically

---

## 4. Error Code Definitions

### Transfer Error Codes

| Error Code | HTTP Status | Description | Frontend i18n Key |
|-----------|-------------|-------------|-------------------|
| SUCCESS | 200 | Transfer completed successfully | transfer.success |
| PARTIAL_SUCCESS | 200 | Some students transferred, some failed | transfer.partial_success |
| INVALID_REQUEST | 400 | Request validation failed | errors.invalid_request |
| STUDENT_NOT_FOUND | 400 | Student ID does not exist | transfer.errors.student_not_found |
| STUDENT_NOT_ENROLLED | 400 | Student not enrolled in source class | transfer.errors.student_not_enrolled |
| CLASS_NOT_FOUND | 404 | Class does not exist | errors.class_not_found |
| CLASS_INACTIVE | 400 | Class is not active | transfer.errors.class_inactive |
| GRADE_MISMATCH | 400 | Grade levels do not match | transfer.errors.grade_mismatch |
| CAPACITY_EXCEEDED | 400 | Destination class is full | transfer.errors.capacity_exceeded |
| ALREADY_ENROLLED | 400 | Student already in destination class | transfer.errors.already_enrolled |
| UNAUTHORIZED | 401 | User not authenticated | errors.unauthorized |
| FORBIDDEN | 403 | User lacks permission | errors.forbidden |
| RATE_LIMIT_EXCEEDED | 429 | Too many requests | errors.rate_limit_exceeded |
| INTERNAL_ERROR | 500 | Server error | errors.internal_error |

### Undo Error Codes

| Error Code | HTTP Status | Description | Frontend i18n Key |
|-----------|-------------|-------------|-------------------|
| SUCCESS | 200 | Undo completed successfully | undo.success |
| TRANSFER_NOT_FOUND | 404 | Transfer ID does not exist | undo.errors.transfer_not_found |
| UNDO_EXPIRED | 409 | 5-minute window expired | undo.errors.undo_expired |
| UNDO_UNAUTHORIZED | 403 | User did not perform transfer | undo.errors.undo_unauthorized |
| UNDO_CONFLICT | 409 | Students have moved again | undo.errors.undo_conflict |
| SOURCE_CLASS_NOT_FOUND | 404 | Source class deleted | undo.errors.source_class_not_found |

**Frontend Error Message Mapping**:
Frontend should map error codes to localized messages:

```typescript
// Example: frontend/src/lib/i18n/locales/en.json
{
  "transfer": {
    "success": "{{count}} students successfully transferred to {{className}}",
    "partial_success": "{{successCount}} students transferred, {{failCount}} failed",
    "errors": {
      "capacity_exceeded": "Cannot transfer: {{className}} is at full capacity ({{current}}/{{max}} students)",
      "grade_mismatch": "Cannot transfer students to a class with a different grade level",
      "already_enrolled": "{{studentName}} is already enrolled in {{className}}"
    }
  },
  "undo": {
    "success": "Transfer undone: {{count}} students returned to {{className}}",
    "errors": {
      "undo_expired": "Cannot undo: The 5-minute undo window has expired",
      "undo_conflict": "Cannot undo: One or more students have been transferred to another class",
      "undo_unauthorized": "Cannot undo: Only the user who performed the transfer can undo it"
    }
  }
}
```

---

## 5. Controller Implementation Guide

### Controller Class

**File**: `student-service/src/main/java/com/sms/student/controller/ClassStudentController.java`

**Class Structure**:
```java
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Tag(name = "Class Student Management", description = "APIs for managing student enrollment and transfers")
public class ClassStudentController {

    private final IStudentTransferService transferService;

    @PostMapping("/{classId}/students/batch-transfer")
    @Operation(summary = "Batch transfer students to another class")
    public ResponseEntity<ApiResponse<BatchTransferResponse>> batchTransferStudents(
        @PathVariable UUID classId,
        @Valid @RequestBody BatchTransferRequest request,
        @AuthenticationPrincipal JwtUser currentUser
    ) {
        BatchTransferResponse response = transferService.batchTransfer(
            classId,
            request,
            currentUser.getId()
        );

        ErrorCode errorCode = response.getFailedTransfers().isEmpty()
            ? ErrorCode.SUCCESS
            : ErrorCode.PARTIAL_SUCCESS;

        return ResponseEntity.ok(ApiResponse.of(errorCode, response));
    }

    @PostMapping("/transfers/{transferId}/undo")
    @Operation(summary = "Undo a batch transfer within 5 minutes")
    public ResponseEntity<ApiResponse<UndoTransferResponse>> undoTransfer(
        @PathVariable UUID transferId,
        @AuthenticationPrincipal JwtUser currentUser
    ) {
        UndoTransferResponse response = transferService.undoTransfer(
            transferId,
            currentUser.getId()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{classId}/eligible-destination-classes")
    @Operation(summary = "Get eligible destination classes for transfer")
    public ResponseEntity<ApiResponse<List<EligibleClassResponse>>> getEligibleDestinations(
        @PathVariable UUID classId
    ) {
        List<EligibleClassResponse> classes = transferService.getEligibleDestinations(classId);
        return ResponseEntity.ok(ApiResponse.success(classes));
    }
}
```

**Note**: This follows the project's controller conventions:
- `@RestController` with `@RequiredArgsConstructor`
- `ApiResponse<T>` wrapper for all responses
- OpenAPI annotations for documentation
- `@AuthenticationPrincipal` for current user injection
- Error codes instead of messages

---

## 6. OpenAPI Specification Excerpt

```yaml
openapi: 3.0.3
info:
  title: Student Service API
  version: 1.0.0

paths:
  /api/classes/{classId}/students/batch-transfer:
    post:
      tags:
        - Class Student Management
      summary: Batch transfer students to another class
      operationId: batchTransferStudents
      parameters:
        - name: classId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/BatchTransferRequest'
      responses:
        '200':
          description: Transfer completed (fully or partially)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/BatchTransferResponse'
        '400':
          description: Validation error
        '401':
          description: Unauthorized
        '403':
          description: Forbidden
        '404':
          description: Class not found

  /api/transfers/{transferId}/undo:
    post:
      tags:
        - Class Student Management
      summary: Undo a batch transfer within 5 minutes
      operationId: undoTransfer
      parameters:
        - name: transferId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Undo successful
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UndoTransferResponse'
        '403':
          description: Unauthorized to undo
        '404':
          description: Transfer not found
        '409':
          description: Undo expired or conflict

components:
  schemas:
    BatchTransferRequest:
      type: object
      required:
        - destinationClassId
        - studentIds
      properties:
        destinationClassId:
          type: string
          format: uuid
        studentIds:
          type: array
          items:
            type: string
            format: uuid
          minItems: 1
          maxItems: 100

    BatchTransferResponse:
      type: object
      properties:
        transferId:
          type: string
          format: uuid
        sourceClassId:
          type: string
          format: uuid
        destinationClassId:
          type: string
          format: uuid
        successfulTransfers:
          type: integer
        failedTransfers:
          type: array
          items:
            $ref: '#/components/schemas/FailedTransfer'
        transferredAt:
          type: string
          format: date-time

    FailedTransfer:
      type: object
      properties:
        studentId:
          type: string
          format: uuid
        studentName:
          type: string
        reason:
          type: string

    UndoTransferResponse:
      type: object
      properties:
        transferId:
          type: string
          format: uuid
        undoneStudents:
          type: integer
        sourceClassId:
          type: string
          format: uuid
        undoneAt:
          type: string
          format: date-time
```

---

## Summary

**Total Endpoints**: 3
- `POST /api/classes/{classId}/students/batch-transfer` - Main transfer operation
- `POST /api/transfers/{transferId}/undo` - Undo operation
- `GET /api/classes/{classId}/eligible-destination-classes` - Helper for destination dropdown

**Error Codes Defined**: 15 total
- 12 transfer-related error codes
- 5 undo-related error codes
- All mapped to HTTP status codes and i18n keys

**Controller Convention**: Follows project standards
- Service layer pattern with I-prefixed interfaces
- ApiResponse wrapper for all responses
- OpenAPI documentation annotations
- JWT authentication with user injection

All API contracts defined. Ready for implementation and task breakdown.
