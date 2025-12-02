# API Contract: GET /api/classes/{id}/students

**Feature**: 007-class-view
**Date**: 2025-12-02
**Service**: student-service
**Version**: 1.0

## Overview

Fetches a paginated list of students enrolled in a specific class with optional search and filter capabilities.

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
| `page` | integer | No | `0` | `>= 0` | Zero-based page number |
| `size` | integer | No | `20` | `1-100` | Number of students per page |
| `search` | string | No | (empty) | Max 100 chars | Search term for student name or code (case-insensitive substring match) |
| `status` | enum | No | (all statuses) | ACTIVE, TRANSFERRED, GRADUATED, WITHDRAWN | Filter by enrollment status |
| `sort` | string | No | `studentName,asc` | Format: `field,(asc\|desc)` | Sort field and direction |

**Valid sort fields**:
- `studentName` - Student's full name
- `studentCode` - Student's unique code
- `enrollmentDate` - Date student enrolled in class

### Request Examples

**Basic request (first page, default size)**:
```http
GET /api/classes/550e8400-e29b-41d4-a716-446655440000/students
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Search for students named "John"**:
```http
GET /api/classes/550e8400-e29b-41d4-a716-446655440000/students?search=john
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Filter by ACTIVE status, page 2**:
```http
GET /api/classes/550e8400-e29b-41d4-a716-446655440000/students?status=ACTIVE&page=1&size=20
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Search + filter + custom sort**:
```http
GET /api/classes/550e8400-e29b-41d4-a716-446655440000/students?search=STU-2024&status=ACTIVE&sort=enrollmentDate,desc
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
    "content": [
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
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

**Response Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `errorCode` | string | Always "SUCCESS" for 200 responses |
| `data` | object | Pagination wrapper containing student list |
| `data.content` | array | List of student enrollment items |
| `data.page` | integer | Current page number (0-indexed) |
| `data.size` | integer | Page size requested |
| `data.totalElements` | integer | Total students matching filter criteria |
| `data.totalPages` | integer | Total pages available |
| `data.hasNext` | boolean | True if more pages available |
| `data.hasPrevious` | boolean | True if previous page exists |

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
- `page` < 0
- `size` < 1 or > 100
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
| Unfiltered query (20 students) | < 50ms | 100ms |
| With status filter | < 60ms | 120ms |
| With search term | < 100ms | 200ms |
| Search + filter | < 120ms | 250ms |

**Database query optimization**:
- Composite index on `(class_id, status)` for filtered queries
- Full-text index on `student_name` and `student_code` for search
- Pagination uses `LIMIT/OFFSET` (efficient for small offsets)

### Scalability

**Expected load**:
- 50-100 concurrent users (teachers viewing classes)
- Average class size: 30 students
- Peak class size: 200 students (rare)

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
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

### Class with Exactly 20 Students (Single Page)

**Response** (200 OK):
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "content": [ /* 20 students */ ],
    "page": 0,
    "size": 20,
    "totalElements": 20,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

### Search with No Results

**Request**:
```http
GET /api/classes/550e8400-e29b-41d4-a716-446655440000/students?search=nonexistent
```

**Response** (200 OK):
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

## Frontend Integration

### TypeScript Service Function

```typescript
// services/classes.ts
import { api } from '@/lib/api'
import type { ApiResponse, PagedStudentEnrollmentResponse } from '@/types'

export interface StudentFilters {
  page?: number
  size?: number
  search?: string
  status?: 'ACTIVE' | 'TRANSFERRED' | 'GRADUATED' | 'WITHDRAWN'
  sort?: string
}

export async function getClassStudents(
  classId: string,
  filters: StudentFilters = {}
): Promise<PagedStudentEnrollmentResponse> {
  const { data } = await api.get<ApiResponse<PagedStudentEnrollmentResponse>>(
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
  const [filters, setFilters] = useState<StudentFilters>({ page: 0, size: 20 })
  const { data, isLoading, error } = useClassStudents(classId, filters)

  if (isLoading) return <StudentListSkeleton />
  if (error) return <ErrorAlert message="Failed to load students" />
  if (!data || data.content.length === 0) return <EmptyState />

  return (
    <StudentTable
      students={data.content}
      pagination={{
        page: data.page,
        totalPages: data.totalPages,
        onPageChange: (page) => setFilters({ ...filters, page })
      }}
    />
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
    public ApiResponse<PagedStudentEnrollmentResponse> getStudentsByClass(
        @PathVariable UUID classId,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(required = false) @Size(max = 100) String search,
        @RequestParam(required = false)
        @Pattern(regexp = "ACTIVE|TRANSFERRED|GRADUATED|WITHDRAWN") String status,
        @RequestParam(defaultValue = "studentName,asc") String sort
    ) {
        PagedStudentEnrollmentResponse students = classService.getStudentsByClass(
            classId, page, size, search, status, sort
        );
        return ApiResponse.success(students);
    }
}
```

### Service Method (Interface)

```java
public interface IClassService {
    PagedStudentEnrollmentResponse getStudentsByClass(
        UUID classId,
        int page,
        int size,
        String search,
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
  it('returns paginated student list', async () => {
    const response = await getClassStudents('550e8400-e29b-41d4-a716-446655440000')

    expect(response).toHaveProperty('content')
    expect(response).toHaveProperty('page', 0)
    expect(response).toHaveProperty('totalElements')
    expect(Array.isArray(response.content)).toBe(true)

    if (response.content.length > 0) {
      const student = response.content[0]
      expect(student).toHaveProperty('studentId')
      expect(student).toHaveProperty('studentName')
      expect(student).toHaveProperty('studentCode')
      expect(student).toHaveProperty('enrollmentStatus')
    }
  })

  it('respects search parameter', async () => {
    const response = await getClassStudents(
      '550e8400-e29b-41d4-a716-446655440000',
      { search: 'John' }
    )

    response.content.forEach(student => {
      expect(
        student.studentName.toLowerCase().includes('john') ||
        student.studentCode.toLowerCase().includes('john')
      ).toBe(true)
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
    void getStudentsByClass_returnsPagedResponse() throws Exception {
        mockMvc.perform(get("/api/classes/{classId}/students", TEST_CLASS_ID)
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.page").value(0))
            .andExpect(jsonPath("$.data.size").value(20));
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
