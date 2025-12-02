# Developer Quickstart: Class Detail View

**Feature**: 007-class-view
**Date**: 2025-12-02
**Estimated setup time**: 15-20 minutes

## Prerequisites

Before starting development on this feature, ensure you have:

### Required Software

- **Node.js** 20+ and **pnpm** 8+
- **Java** 21 and **Maven** 3.9+
- **Docker** and **Docker Compose**
- **Git** (already on `007-class-view` branch)

### Running Services

The following services must be running:

- ✅ PostgreSQL (student database) - port 5433
- ✅ student-service - port 8082
- ✅ API Gateway - port 8080 (routes requests to services)
- ✅ Eureka Server - port 8761 (service discovery)

---

## Quick Setup (5 minutes)

### 1. Verify Branch

```bash
git branch --show-current
# Should output: 007-class-view
```

If not on correct branch:
```bash
git checkout 007-class-view
```

### 2. Start Backend Services

```bash
cd /Volumes/DATA/my-projects/salarean

# Start all backend services
docker-compose up -d postgres-student student-service api-gateway eureka-server
```

**Wait 30-60 seconds** for services to initialize.

**Verify services are healthy**:
```bash
# Check Docker containers
docker-compose ps

# Expected output:
# postgres-student    running    5433/tcp
# student-service     running    8082/tcp
# api-gateway         running    8080/tcp
# eureka-server       running    8761/tcp
```

### 3. Install Frontend Dependencies

```bash
cd frontend
pnpm install
```

**First time setup**: This may take 2-3 minutes.

### 4. Start Frontend Dev Server

```bash
pnpm dev
```

**Expected output**:
```
VITE v5.x ready in 1234 ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
```

Open **http://localhost:5173** in your browser.

---

## Verify Setup (2 minutes)

### 1. Test API Gateway

```bash
curl http://localhost:8080/actuator/health
```

**Expected response**:
```json
{"status":"UP"}
```

### 2. Test Student Service

```bash
curl http://localhost:8082/actuator/health
```

**Expected response**:
```json
{"status":"UP"}
```

### 3. Test Class Students Endpoint

First, get a JWT token by logging in through the frontend or using this curl command:

```bash
# Login (replace with actual credentials)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teacher@example.com","password":"password123"}'
```

**Copy the `accessToken`** from the response, then test the endpoint:

```bash
# Replace {TOKEN} with your JWT token
# Replace {CLASS_ID} with an existing class UUID from database

curl -H "Authorization: Bearer {TOKEN}" \
  http://localhost:8080/api/classes/{CLASS_ID}/students?page=0&size=20
```

**Expected response**:
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "content": [ /* array of students */ ],
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

## Development Workflow

### Frontend Development

#### File Structure for This Feature

```
frontend/src/
├── routes/_authenticated/
│   └── classes.$id.tsx         # NEW: Route component
├── features/classes/
│   ├── [id].tsx                # NEW: Class detail page
│   └── components/
│       ├── class-header.tsx    # NEW: Class info header
│       ├── students-tab.tsx    # NEW: Students tab content
│       ├── student-list.tsx    # NEW: Student table
│       ├── coming-soon-tab.tsx # NEW: Placeholder tabs
│       └── student-search.tsx  # NEW: Search/filter controls
├── services/
│   └── classes.ts              # MODIFY: Add getClassStudents()
├── types/
│   └── class.ts                # MODIFY: Add interfaces
├── hooks/
│   ├── use-class-students.ts   # NEW: TanStack Query hook
│   └── use-debounce.ts         # NEW: Debounce hook
└── lib/validations/
    └── class-filters.ts        # NEW: Zod schemas
```

#### Development Commands

```bash
cd frontend

# Run dev server (hot reload)
pnpm dev

# Run type checking
pnpm exec tsc --noEmit

# Run tests (Vitest)
pnpm test

# Run E2E tests (Playwright)
pnpm test:e2e

# Generate TanStack Router types
pnpm exec tsr generate

# Lint code
pnpm lint
```

#### Useful Dev Tools

- **TanStack Router DevTools**: Automatically injected in dev mode (bottom-left icon)
- **TanStack Query DevTools**: Automatically injected in dev mode (bottom-right icon)
- **React DevTools**: Browser extension for component inspection
- **axe DevTools**: Browser extension for accessibility testing

### Backend Development (If API Changes Needed)

#### File Structure for This Feature

```
student-service/src/main/java/com/sms/student/
├── controller/
│   └── ClassController.java   # MODIFY: Add getStudentsByClass endpoint
├── service/
│   ├── interfaces/
│   │   └── IClassService.java # MODIFY: Add method signature
│   └── ClassService.java      # MODIFY: Implement method
├── dto/
│   ├── PagedStudentEnrollmentResponse.java  # NEW
│   └── StudentEnrollmentItem.java           # NEW
└── repository/
    └── StudentClassEnrollmentRepository.java # MODIFY: Add query method
```

#### Development Commands

```bash
cd student-service

# Compile code
./mvnw clean compile

# Run tests
./mvnw test

# Run service locally (without Docker)
./mvnw spring-boot:run

# Package JAR
./mvnw clean package -DskipTests

# Rebuild Docker image
docker-compose build student-service

# Restart service in Docker
docker-compose restart student-service
```

#### Swagger UI (API Documentation)

Access Swagger UI for student-service:

**URL**: http://localhost:8082/swagger-ui.html

**Features**:
- View all endpoints
- Test endpoints with "Try it out" button
- See request/response schemas

---

## Common Tasks

### Create New Route

**1. Create route file**:
```bash
touch frontend/src/routes/_authenticated/classes.\$id.tsx
```

**2. Define route with TanStack Router**:
```typescript
import { createFileRoute } from '@tanstack/react-router'
import { ClassDetailPage } from '@/features/classes/[id]'

export const Route = createFileRoute('/_authenticated/classes/$id')({
  component: ClassDetailPage,
})
```

**3. Generate route types**:
```bash
pnpm exec tsr generate
```

### Add New API Service Function

**File**: `frontend/src/services/classes.ts`

```typescript
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

### Create TanStack Query Hook

**File**: `frontend/src/hooks/use-class-students.ts`

```typescript
import { useQuery } from '@tanstack/react-query'
import { getClassStudents } from '@/services/classes'

export function useClassStudents(classId: string, filters = {}) {
  return useQuery({
    queryKey: ['class-students', classId, filters],
    queryFn: () => getClassStudents(classId, filters),
    staleTime: 5 * 60 * 1000,
  })
}
```

### Add New Backend Endpoint

**File**: `student-service/src/main/java/com/sms/student/controller/ClassController.java`

```java
@GetMapping("/{classId}/students")
public ApiResponse<PagedStudentEnrollmentResponse> getStudentsByClass(
    @PathVariable UUID classId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(required = false) String search,
    @RequestParam(required = false) String status
) {
    var students = classService.getStudentsByClass(classId, page, size, search, status);
    return ApiResponse.success(students);
}
```

---

## Testing

### Unit Tests (Frontend)

```bash
cd frontend

# Run all tests
pnpm test

# Run tests in watch mode
pnpm test --watch

# Run tests with coverage
pnpm test --coverage
```

**Test file naming**: `*.test.tsx` or `*.spec.tsx`

**Example test**:
```typescript
import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { StudentList } from './student-list'

describe('StudentList', () => {
  it('renders empty state when no students', () => {
    render(<StudentList students={[]} />)
    expect(screen.getByText(/no students enrolled/i)).toBeInTheDocument()
  })
})
```

### E2E Tests (Playwright)

```bash
cd frontend

# Run E2E tests (headless)
pnpm test:e2e

# Run E2E tests with UI
pnpm exec playwright test --ui

# Run specific test file
pnpm exec playwright test class-detail.spec.ts
```

**Test file location**: `frontend/tests/e2e/class-detail.spec.ts`

### Accessibility Tests

```bash
cd frontend

# Run accessibility tests (axe-core)
pnpm test:a11y

# Or manually with Playwright
pnpm exec playwright test --grep @accessibility
```

**Example accessibility test**:
```typescript
import { test, expect } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'

test('class detail page passes accessibility checks', async ({ page }) => {
  await page.goto('/classes/123')
  const accessibilityScanResults = await new AxeBuilder({ page }).analyze()
  expect(accessibilityScanResults.violations).toEqual([])
})
```

### Backend Tests (JUnit)

```bash
cd student-service

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ClassControllerTest

# Run tests with coverage
./mvnw test jacoco:report
```

---

## Troubleshooting

### Issue: "Cannot GET /classes/123" (404 on route)

**Cause**: Route file not created or TanStack Router types not generated

**Solution**:
```bash
cd frontend
pnpm exec tsr generate
# Restart dev server
```

### Issue: "CORS error" when calling API

**Cause**: API Gateway CORS configuration missing

**Solution**:
- Check `api-gateway/src/main/java/com/sms/gateway/config/CorsConfig.java`
- Ensure `http://localhost:5173` is in allowed origins
- Restart API Gateway: `docker-compose restart api-gateway`

### Issue: "Empty student list" even though students exist

**Cause**: Class has no enrollments OR authorization issue

**Solution**:
1. Check database:
   ```sql
   SELECT * FROM student_class_enrollments WHERE class_id = '{CLASS_ID}';
   ```
2. Verify JWT token has correct permissions
3. Check browser console for API errors

### Issue: TypeScript errors after adding new types

**Cause**: TypeScript cache or missing type generation

**Solution**:
```bash
cd frontend
rm -rf node_modules/.vite  # Clear Vite cache
pnpm exec tsc --noEmit      # Type check
pnpm exec tsr generate      # Regenerate router types
```

### Issue: Backend service not starting

**Cause**: Port conflict or database connection failure

**Solution**:
1. Check logs: `docker-compose logs student-service`
2. Verify PostgreSQL running: `docker-compose ps postgres-student`
3. Check port availability: `lsof -i :8082`
4. Restart services: `docker-compose restart student-service`

### Issue: Hot reload not working

**Cause**: File watcher limit reached (macOS/Linux)

**Solution** (macOS):
```bash
# Increase file watcher limit
echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf
sudo sysctl -p
```

---

## Useful Commands Reference

### Docker

```bash
# View logs
docker-compose logs -f student-service

# Restart specific service
docker-compose restart student-service

# Stop all services
docker-compose down

# Rebuild and start service
docker-compose up -d --build student-service

# Access database
docker-compose exec postgres-student psql -U sms_user -d student_db
```

### Database

```bash
# Connect to student database
docker-compose exec postgres-student psql -U sms_user -d student_db

# Useful queries for this feature
# List all classes
SELECT id, class_code, class_name FROM classes LIMIT 10;

# List enrollments for a class
SELECT s.student_code, s.full_name, e.status
FROM student_class_enrollments e
JOIN students s ON e.student_id = s.id
WHERE e.class_id = '{CLASS_ID}';

# Count students by status
SELECT status, COUNT(*) FROM student_class_enrollments
WHERE class_id = '{CLASS_ID}'
GROUP BY status;
```

### Git

```bash
# View current changes
git status

# Stage changes
git add frontend/src/routes/_authenticated/classes.\$id.tsx

# Commit
git commit -m "feat: add class detail page route"

# Push to remote
git push origin 007-class-view
```

---

## Next Steps

Once setup is complete:

1. **Review the spec**: Read `spec.md` for feature requirements
2. **Review the plan**: Read `plan.md` for technical approach
3. **Check the contracts**: Read `contracts/get-class-students.md` for API details
4. **Run `/speckit.tasks`**: Generate task breakdown for implementation
5. **Start coding**: Begin with P1 tasks (Students tab)

**Recommended implementation order**:
1. Create route and basic page structure
2. Implement API service function and TanStack Query hook
3. Build Students tab with table display
4. Add search/filter functionality
5. Implement tab navigation with lazy loading
6. Add accessibility features (keyboard nav, ARIA)
7. Write tests (unit, E2E, accessibility)
8. Manual QA on mobile devices

**Estimated development time**: 2-3 days

---

## Getting Help

- **Frontend issues**: Check TanStack Router/Query DevTools
- **Backend issues**: Check Swagger UI (http://localhost:8082/swagger-ui.html)
- **API issues**: Check API Gateway logs (`docker-compose logs api-gateway`)
- **Database issues**: Connect to PostgreSQL and inspect data

**Documentation references**:
- [TanStack Router](https://tanstack.com/router/latest)
- [TanStack Query](https://tanstack.com/query/latest)
- [shadcn/ui](https://ui.shadcn.com)
- [WCAG 2.1](https://www.w3.org/WAI/WCAG21/quickref/)
