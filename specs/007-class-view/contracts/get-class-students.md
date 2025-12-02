# API Contract: GET /api/classes/{id}/students

**Feature**: 007-class-view
**Date**: 2025-12-02
**Service**: student-service
**Version**: 1.0

## Overview

Fetches all students enrolled in a specific class with optional status filter. Search filtering is performed client-side using TanStack Table for instant real-time feedback.

---

## Endpoint

```
GET /api/classes/{classId}/students
```

**Base URL**: `http://localhost:8080` (API Gateway)
**Service URL**: `http://localhost:8082` (student-service direct)

---

## Authentication

**Required**: Yes

**Method**: JWT Bearer token

```http
Authorization: Bearer {jwt_token}
```

**Roles allowed**:
- `TEACHER` (can view own classes)
- `ADMIN` (can view all classes)
- `PRINCIPAL` (can view all classes in their school)

---

## Authorization

User must have permission to view the specified class:
- Teachers can only view classes they are assigned to
- Admins/Principals can view all classes in their school

**Unauthorized access returns**: `403 FORBIDDEN` with error code `FORBIDDEN`

---

## Request

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `classId` | UUID | Yes | Unique identifier of the class |

**Example**: `/api/classes/550e8400-e29b-41d4-a716-446655440000/students`

### Query Parameters

| Parameter | Type | Required | Default | Constraints | Description |
|-----------|------|----------|---------|-------------|-------------|
| `status` | enum | No | (all statuses) | ACTIVE, TRANSFERRED, GRADUATED, WITHDRAWN | Filter by enrollment status |
| `sort` | string | No | `studentName,asc` | Format: `field,(asc\|desc)` | Sort field and direction |

**Valid sort fields**:
- `studentName` - Student's full name
- `studentCode` - Student's unique code
- `enrollmentDate` - Date student enrolled in class

**Note**: Search filtering (by name/code) is performed client-side using TanStack Table's `globalFilter` for instant real-time feedback. This avoids unnecessary API calls for typical class sizes (10-100 students).

### Request Examples

**Basic request (all students)**:
```http
GET /api/classes/550e8400-e29b-41d4-a716-446655440000/students
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Filter by ACTIVE status**:
```http
GET /api/classes/550e8400-e29b-41d4-a716-446655440000/students?status=ACTIVE
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Custom sort (by enrollment date, descending)**:
```http
GET /api/classes/550e8400-e29b-41d4-a716-446655440000/students?sort=enrollmentDate,desc
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Filter + custom sort**:
```http
GET /api/classes/550e8400-e29b-41d4-a716-446655440000/students?status=ACTIVE&sort=enrollmentDate,desc
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Response

### Success Response (200 OK)

**Content-Type**: `application/json`

**Structure**: Standard `ApiResponse<T>` wrapper

```json
{
  "errorCode": "SUCCESS",
  "data": {
    "students": [
      {
        "studentId": "123e4567-e89b-12d3-a456-426614174000",
        "studentName": "Sok Pisey",
        "studentCode": "STU-2024-0001",
        "photoUrl": "https://cdn.example.com/photos/student-123.jpg",
        "enrollmentDate": "2024-09-01",
        "enrollmentStatus": "ACTIVE"
      },
      {
        "studentId": "223e4567-e89b-12d3-a456-426614174001",
        "studentName": "Chan Dara",
        "studentCode": "STU-2024-0002",
        "photoUrl": null,
        "enrollmentDate": "2024-09-01",
        "enrollmentStatus": "ACTIVE"
      }
    ],
    "totalCount": 45
  }
}
```

**Response Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `errorCode` | string | Always "SUCCESS" for 200 responses |
| `data` | object | Response wrapper containing student list |
| `data.students` | array | List of all student enrollment items matching filter |
| `data.totalCount` | integer | Total number of students returned |

**StudentEnrollmentItem Fields**:

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `studentId` | string (UUID) | No | Student's unique identifier |
| `studentName` | string | No | Student's full name |
| `studentCode` | string | No | Student's unique code (e.g., STU-2024-0001) |
| `photoUrl` | string (URL) | Yes | Student's profile photo URL (null if not uploaded) |
| `enrollmentDate` | string (ISO 8601 date) | No | Date student enrolled in this class (YYYY-MM-DD) |
| `enrollmentStatus` | enum | No | ACTIVE \| TRANSFERRED \| GRADUATED \| WITHDRAWN |

### Error Responses

#### 400 Bad Request - Invalid Parameters

```json
{
  "errorCode": "INVALID_INPUT",
  "data": null
}
```

**Triggers**:
- Invalid UUID format for `classId`
- Invalid `status` enum value
- Invalid `sort` format

#### 401 Unauthorized - Missing or Invalid Token

```json
{
  "errorCode": "UNAUTHORIZED",
  "data": null
}
```

**Triggers**:
- No `Authorization` header
- Invalid JWT token
- Expired JWT token

#### 403 Forbidden - Insufficient Permissions

```json
{
  "errorCode": "FORBIDDEN",
  "data": null
}
```

**Triggers**:
- User does not have permission to view this class
- Teacher trying to view class not assigned to them

#### 404 Not Found - Class Does Not Exist

```json
{
  "errorCode": "CLASS_NOT_FOUND",
  "data": null
}
```

**Triggers**:
- No class exists with the provided `classId`

#### 500 Internal Server Error - Server Fault

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "data": null
}
```

**Triggers**:
- Database connection failure
- Unhandled exception in service layer

---

## Performance

### Expected Response Times

| Scenario | Target | Max Acceptable |
|----------|--------|----------------|
| All students (no filter) | < 50ms | 100ms |
| With status filter | < 60ms | 120ms |

**Note**: Search filtering is performed client-side using TanStack Table's `globalFilter`, providing instant real-time feedback without API calls.

**Database query optimization**:
- Composite index on `(class_id, status)` for filtered queries
- All students returned in single query (no pagination needed for typical class sizes)

### Scalability

**Expected load**:
- 50-100 concurrent users (teachers viewing classes)
- Average class size: 30 students
- Peak class size: 200 students (rare)

**Design decision**: All students returned in single response (no pagination). Class sizes are typically 10-100 students, making pagination unnecessary and adding complexity. Search filtering is performed client-side using TanStack Table for instant real-time feedback.

**Caching strategy**:
- Frontend: TanStack Query caches response for 5 minutes (`staleTime: 300000`)
- Backend: No server-side caching (enrollment data changes infrequently but must be accurate)

---

## Examples

### Empty Class (No Students Enrolled)

**Request**:
```http
GET /api/classes/550e8400-e29b-41d4-a716-446655440000/students
```

**Response** (200 OK):
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "students": [],
    "totalCount": 0
  }
}
```

### Class with 45 Students

**Response** (200 OK):
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "students": [ /* 45 students */ ],
    "totalCount": 45
  }
}
```

### Status Filter with No Results

**Request**:
```http
GET /api/classes/550e8400-e29b-41d4-a716-446655440000/students?status=TRANSFERRED
```

**Response** (200 OK):
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "students": [],
    "totalCount": 0
  }
}
```

---

## Frontend Integration

### TypeScript Service Function

```typescript
// services/classes.ts
import { api } from '@/lib/api'
import type { ApiResponse, StudentEnrollmentListResponse } from '@/types'

export interface StudentFilters {
  status?: 'ACTIVE' | 'TRANSFERRED' | 'GRADUATED' | 'WITHDRAWN'
  sort?: string
}

export async function getClassStudents(
  classId: string,
  filters: StudentFilters = {}
): Promise<StudentEnrollmentListResponse> {
  const { data } = await api.get<ApiResponse<StudentEnrollmentListResponse>>(
    `/api/classes/${classId}/students`,
    { params: filters }
  )

  if (data.errorCode !== 'SUCCESS' || !data.data) {
    throw new Error(data.errorCode)
  }

  return data.data
}
```

### TanStack Query Hook

```typescript
// hooks/use-class-students.ts
import { useQuery } from '@tanstack/react-query'
import { getClassStudents, type StudentFilters } from '@/services/classes'

export function useClassStudents(classId: string, filters: StudentFilters = {}) {
  return useQuery({
    queryKey: ['class-students', classId, filters],
    queryFn: () => getClassStudents(classId, filters),
    staleTime: 5 * 60 * 1000,  // 5 minutes
    enabled: !!classId,         // Only fetch if classId exists
  })
}
```

### Usage in Component

```typescript
function StudentsTab({ classId }: { classId: string }) {
  const [statusFilter, setStatusFilter] = useState<string | null>(null)
  const [searchTerm, setSearchTerm] = useState('')
  const debouncedSearch = useDebouncedValue(searchTerm, 300)

  const { data, isLoading, error } = useClassStudents(classId, {
    status: statusFilter || undefined
  })

  // Client-side search filtering using TanStack Table
  const table = useReactTable({
    data: data?.students ?? [],
    columns,
    state: { globalFilter: debouncedSearch },
    globalFilterFn: 'includesString',
  })

  if (isLoading) return <StudentListSkeleton />
  if (error) return <ErrorAlert message="Failed to load students" />
  if (!data || data.students.length === 0) return <EmptyState />

  return (
    <StudentTable table={table} />
  )
}
```

---

## Backend Implementation Reference

### Controller Method Signature

```java
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

    private final IClassService classService;

    @GetMapping("/{classId}/students")
    public ApiResponse<StudentEnrollmentListResponse> getStudentsByClass(
        @PathVariable UUID classId,
        @RequestParam(required = false)
        @Pattern(regexp = "ACTIVE|TRANSFERRED|GRADUATED|WITHDRAWN") String status,
        @RequestParam(defaultValue = "studentName,asc") String sort
    ) {
        StudentEnrollmentListResponse students = classService.getStudentsByClass(
            classId, status, sort
        );
        return ApiResponse.success(students);
    }
}
```

### Service Method (Interface)

```java
public interface IClassService {
    StudentEnrollmentListResponse getStudentsByClass(
        UUID classId,
        String status,
        String sort
    );
}
```

---

## Testing

### Contract Test (Frontend)

```typescript
import { describe, it, expect } from 'vitest'
import { getClassStudents } from '@/services/classes'

describe('GET /api/classes/{id}/students', () => {
  it('returns student list', async () => {
    const response = await getClassStudents('550e8400-e29b-41d4-a716-446655440000')

    expect(response).toHaveProperty('students')
    expect(response).toHaveProperty('totalCount')
    expect(Array.isArray(response.students)).toBe(true)

    if (response.students.length > 0) {
      const student = response.students[0]
      expect(student).toHaveProperty('studentId')
      expect(student).toHaveProperty('studentName')
      expect(student).toHaveProperty('studentCode')
      expect(student).toHaveProperty('enrollmentStatus')
    }
  })

  it('respects status filter parameter', async () => {
    const response = await getClassStudents(
      '550e8400-e29b-41d4-a716-446655440000',
      { status: 'ACTIVE' }
    )

    response.students.forEach(student => {
      expect(student.enrollmentStatus).toBe('ACTIVE')
    })
  })
})
```

### Integration Test (Backend)

```java
@SpringBootTest
@AutoConfigureMockMvc
class ClassControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "TEACHER")
    void getStudentsByClass_returnsStudentList() throws Exception {
        mockMvc.perform(get("/api/classes/{classId}/students", TEST_CLASS_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
            .andExpect(jsonPath("$.data.students").isArray())
            .andExpect(jsonPath("$.data.totalCount").isNumber());
    }

    @Test
    void getStudentsByClass_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/classes/{classId}/students", TEST_CLASS_ID))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }
}
```

---

## Changelog

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-02 | System | Initial contract definition |

---

## Related Contracts

- `GET /api/classes/{id}` - Get class details (for header display)
- `GET /api/students/{id}` - Get individual student profile (future: click student name)
- `POST /api/classes/{id}/enrollments` - Enroll student in class (future feature)
