# Research: Teacher School Setup

**Feature**: 009-teacher-school-setup
**Date**: 2025-12-10
**Status**: Complete

## Overview

This document captures technical research and decisions for implementing the teacher school setup feature, which allows newly registered teachers to associate with a school through a hierarchical selection flow.

## Key Research Areas

### 1. Database Structure Analysis

**Finding**: Current database schema review reveals:

#### auth-service Database (`auth_db`)
- **users table** exists with columns: id (UUID), email, phone_number, password_hash, name, profile_photo_url, preferred_language, account_status, created_at, updated_at
- User entity represents teachers (no separate Teacher entity exists)
- No existing school association in users table
- **Decision**: Add new `teacher_school` table in `auth_db` to store one-to-one relationship

#### student-service Database (`student_db`)
- **schools table** already exists with columns: id (UUID), name, name_km, address, province (VARCHAR 100), district (VARCHAR 100), type, created_at, updated_at
- Province and district are stored as **string columns** in schools table (not foreign keys)
- **No separate provinces or districts tables exist**
- **Decision**: Create new `provinces` and `districts` tables with proper relationships for hierarchical selection

**Rationale**: Current province/district as VARCHAR fields prevents efficient filtering and normalization. Creating proper reference tables enables:
- Cascading dropdowns with referential integrity
- Consistent province/district naming
- Efficient queries with indexed foreign keys
- Future scalability (e.g., adding province codes, multilingual names)

---

### 2. Table Design Decision: teacher_school

**Question**: Where should we store teacher-school associations?

**Options Evaluated**:
1. Add `school_id` column directly to `users` table (auth-service)
2. Create separate `teacher_school` join table (auth-service)
3. Create teacher profile table in student-service

**Decision**: Option 2 - Create `teacher_school` join table in auth-service

**Rationale**:
- **Principal metadata requirement**: User requirement specifies storing principal_name and principal_gender, which are school-specific metadata that don't belong in the generic users table
- **Separation of concerns**: Teacher authentication/profile (auth-service) vs. school operational data (student-service) remain separated
- **Future flexibility**: Allows additional school-specific teacher metadata (hire date, department, role) without polluting users table
- **Single responsibility**: teacher_school table has one job - associate teachers with schools and store school-specific teacher metadata

**Schema**:
```sql
CREATE TABLE teacher_school (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    school_id UUID NOT NULL,  -- References student_service.schools(id)
    principal_name VARCHAR(255) NOT NULL,
    principal_gender VARCHAR(1) NOT NULL CHECK (principal_gender IN ('M', 'F')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT unique_user_school UNIQUE (user_id)  -- One school per teacher
);
```

**Cross-Service Reference**: `school_id` is a UUID that references `student_service.schools(id)` but NOT enforced with foreign key constraint (violates microservice independence). Validation happens at application layer.

---

### 3. Province and District Tables Design

**Question**: How to implement hierarchical location selection?

**Current State**: Schools table has province and district as VARCHAR columns with no referential integrity.

**Decision**: Create normalized province and district tables in student-service

**Schema**:
```sql
-- Provinces table (top level)
CREATE TABLE provinces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    name_km VARCHAR(100),
    code VARCHAR(10) UNIQUE,  -- e.g., "PP" for Phnom Penh
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Districts table (belongs to province)
CREATE TABLE districts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    province_id UUID NOT NULL REFERENCES provinces(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    name_km VARCHAR(100),
    code VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT unique_district_per_province UNIQUE (province_id, name)
);

-- Update schools table to use foreign keys
ALTER TABLE schools
    ADD COLUMN province_id UUID REFERENCES provinces(id),
    ADD COLUMN district_id UUID REFERENCES districts(id);

-- Migration strategy: Backfill province_id and district_id from existing VARCHAR columns
-- then deprecate (but don't drop) province and district VARCHAR columns for backward compatibility
```

**Rationale**:
- Enables efficient cascading dropdown queries (GET /provinces, GET /districts?provinceId={id})
- Provides data consistency and referential integrity
- Supports multilingual names (English and Khmer)
- Allows future expansion (province codes, district metadata)

**Migration Strategy**:
1. Create provinces and districts tables
2. Populate with unique values from existing schools.province and schools.district columns
3. Add province_id and district_id columns to schools table
4. Backfill foreign keys by matching VARCHAR values
5. Keep old VARCHAR columns temporarily for backward compatibility

---

### 4. API Endpoint Design

**Decision**: RESTful endpoints across two services

#### student-service Endpoints (Location Data)
```
GET /api/provinces
- Returns list of all provinces (id, name, name_km)
- No pagination (< 30 provinces in Cambodia)
- Response: ApiResponse<List<ProvinceResponse>>

GET /api/districts?provinceId={uuid}
- Returns districts for a specific province
- Query param: provinceId (required)
- Response: ApiResponse<List<DistrictResponse>>

GET /api/schools?districtId={uuid}
- Returns schools for a specific district
- Query param: districtId (required)
- Response: ApiResponse<List<SchoolResponse>>

POST /api/schools
- Creates a new school under specific province/district
- Request: SchoolRequest (name, name_km, address, province_id, district_id, type)
- Requires JWT authentication
- Response: ApiResponse<SchoolResponse>
```

#### auth-service Endpoints (Teacher-School Association)
```
POST /api/teacher-school
- Associates authenticated teacher with a school
- Request: TeacherSchoolRequest (school_id, principal_name, principal_gender)
- Extracts user_id from JWT token
- Validates school_id exists (cross-service call to student-service)
- Creates teacher_school record
- Response: ApiResponse<TeacherSchoolResponse>

GET /api/teacher-school
- Retrieves current teacher's school association
- Extracts user_id from JWT token
- Response: ApiResponse<TeacherSchoolResponse> (includes school details from student-service)

PUT /api/teacher-school
- Updates teacher's school association
- Same request/response as POST
- For edge case: changing school assignment
```

**Rationale**:
- Clear service boundaries: student-service owns location/school data, auth-service owns teacher associations
- RESTful conventions for predictable API behavior
- JWT-based authentication for all endpoints
- Query parameters for filtering (standard REST pattern)

---

### 5. Frontend Component Architecture

**Decision**: Feature-based architecture with reusable components

**Component Structure**:
```
features/school-setup/
├── components/
│   ├── province-selector.tsx      # Dropdown for provinces
│   ├── district-selector.tsx      # Dropdown for districts (depends on province)
│   ├── school-table.tsx           # DataTable for schools list
│   └── add-school-modal.tsx       # Dialog for adding new school
└── index.tsx                      # Main school setup page
```

**State Management**:
- **TanStack Query** for server state (provinces, districts, schools queries)
- **Zustand** store for local UI state (selected province_id, selected district_id)
- **Form state**: react-hook-form with Zod validation for school creation

**Table Component Selection**:
- Use `ClientDataTable` (no URL persistence needed)
- Schools per district typically < 100 rows (no pagination required)
- Client-side filtering and sorting sufficient

**Interaction Flow**:
1. Load page → Fetch provinces → Render province selector
2. User selects province → Update Zustand store → Fetch districts for province_id
3. User selects district → Update Zustand store → Fetch schools for district_id
4. Display schools table → User selects school OR clicks "Add New School"
5. If adding school → Show modal with form → POST /api/schools → Refresh schools table
6. User confirms selection → POST /api/teacher-school → Redirect to main app

**Rationale**:
- Follows constitution frontend standards (feature-based, TanStack Query, Zustand)
- Reusable selector components enable future use in other features
- ClientDataTable appropriate for small school datasets per district

---

### 6. School Setup Flow Integration

**Question**: How does school setup integrate with existing registration flow?

**Current Registration Flow** (auth-service):
1. User submits registration form (email, phone, password, name)
2. Backend creates User record in users table
3. Backend generates JWT access/refresh tokens
4. Frontend stores tokens in HTTP-only cookies
5. User redirected to... **[NEEDS INTEGRATION]**

**Decision**: Post-Registration Redirect Guard

**Implementation**:
```typescript
// Frontend: TanStack Router loader for _authenticated routes
export const Route = createRoute({
  component: AuthenticatedLayout,
  beforeLoad: async ({ context }) => {
    // 1. Check if user is authenticated
    const isAuthenticated = await checkAuth();
    if (!isAuthenticated) throw redirect({ to: '/login' });

    // 2. Check if teacher has school association
    const teacherSchool = await fetchTeacherSchool(); // GET /api/teacher-school

    // 3. If no association, force redirect to school setup
    if (!teacherSchool.data) {
      throw redirect({ to: '/school-setup' });
    }

    return { teacherSchool: teacherSchool.data };
  }
});
```

**Backend Support**:
- `GET /api/teacher-school` returns `errorCode: "NOT_FOUND"` with `data: null` when no association exists
- Frontend interprets this as "setup incomplete" and redirects

**Rationale**:
- Enforces FR-011 (prevent main app access until school setup complete)
- Centralized guard logic in router loader (DRY principle)
- Works for both new registrations and existing users without school association

---

### 7. Error Code Definitions

**Decision**: Service-specific error codes per constitution

#### student-service Error Codes
```java
public enum ErrorCode {
    SUCCESS,
    INVALID_INPUT,
    PROVINCE_NOT_FOUND,
    DISTRICT_NOT_FOUND,
    SCHOOL_NOT_FOUND,
    DUPLICATE_SCHOOL_NAME,    // School with same name exists in district
    UNAUTHORIZED,
    INTERNAL_ERROR
}
```

#### auth-service Error Codes (additions)
```java
public enum ErrorCode {
    // ... existing codes
    TEACHER_ALREADY_ASSIGNED,  // Teacher already has school association
    SCHOOL_NOT_FOUND,          // school_id doesn't exist in student-service
    INVALID_PRINCIPAL_DATA,    // principal_name or principal_gender invalid
    TEACHER_NOT_FOUND          // user_id doesn't exist or not a teacher
}
```

**Frontend Translation** (i18n keys):
```json
{
  "en": {
    "errors.PROVINCE_NOT_FOUND": "Province not found",
    "errors.DUPLICATE_SCHOOL_NAME": "A school with this name already exists in the district",
    "errors.TEACHER_ALREADY_ASSIGNED": "You have already been assigned to a school"
  },
  "km": {
    "errors.PROVINCE_NOT_FOUND": "រកមិនឃើញខេត្ត",
    "errors.DUPLICATE_SCHOOL_NAME": "មានសាលារៀនដែលមានឈ្មោះនេះរួចហើយនៅក្នុងស្រុក",
    "errors.TEACHER_ALREADY_ASSIGNED": "អ្នកត្រូវបានចាត់តាំងទៅសាលារៀនរួចហើយ"
  }
}
```

---

### 8. Validation Rules

**Backend Validation** (Spring Bean Validation):

**SchoolRequest DTO**:
```java
public class SchoolRequest {
    @NotBlank(message = "School name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String nameKhmer;

    @NotBlank(message = "Address is required")
    @Size(max = 500)
    private String address;

    @NotNull(message = "Province is required")
    private UUID provinceId;

    @NotNull(message = "District is required")
    private UUID districtId;

    @NotNull(message = "School type is required")
    @Enumerated(EnumType.STRING)
    private SchoolType type;  // PRIMARY, SECONDARY, HIGH_SCHOOL, VOCATIONAL
}
```

**TeacherSchoolRequest DTO**:
```java
public class TeacherSchoolRequest {
    @NotNull(message = "School is required")
    private UUID schoolId;

    @NotBlank(message = "Principal name is required")
    @Size(max = 255)
    private String principalName;

    @NotNull(message = "Principal gender is required")
    @Pattern(regexp = "M|F", message = "Gender must be M or F")
    private String principalGender;
}
```

**Frontend Validation** (Zod schemas):
```typescript
const schoolSchema = z.object({
  name: z.string().min(1, "School name is required").max(255),
  nameKhmer: z.string().max(255).optional(),
  address: z.string().min(1, "Address is required").max(500),
  provinceId: z.string().uuid("Invalid province"),
  districtId: z.string().uuid("Invalid district"),
  type: z.enum(["PRIMARY", "SECONDARY", "HIGH_SCHOOL", "VOCATIONAL"])
});

const teacherSchoolSchema = z.object({
  schoolId: z.string().uuid("Invalid school"),
  principalName: z.string().min(1, "Principal name is required").max(255),
  principalGender: z.enum(["M", "F"], { required_error: "Principal gender is required" })
});
```

---

### 9. Performance Considerations

**Caching Strategy** (Not implemented in MVP per YAGNI):
- Provinces list changes rarely → Could cache for 24 hours (deferred)
- Districts list changes rarely → Could cache per province (deferred)
- Schools list changes frequently (new additions) → No caching initially

**Query Optimization**:
- Index on districts.province_id for fast filtering
- Index on schools.district_id for fast filtering
- Limit schools query to single district (no "load all schools" endpoint)

**Frontend Optimization**:
- TanStack Query automatic caching (5-minute default)
- Debounce school search input (300ms)
- Lazy load districts only after province selection
- Lazy load schools only after district selection

**Rationale**: Premature optimization avoided. Simple indexed queries sufficient for initial scale (~1000 schools, ~10,000 teachers).

---

### 10. Security Considerations

**Authentication**:
- All endpoints require valid JWT token (except public health checks)
- User ID extracted from JWT token (not from request body)

**Authorization**:
- Teachers can only create/update their own school association
- Teachers can add new schools (no admin approval required per spec)
- No role-based restrictions on school creation (simplified for MVP)

**Validation**:
- Parameterized queries prevent SQL injection (JPA/Hibernate)
- DTO validation prevents invalid data
- Cross-service school_id validation (check school exists before association)

**Audit Logging**:
- Log new school creation (user_id, school_id, timestamp)
- Log teacher-school association creation (user_id, school_id, timestamp)
- Log school setup completion metric for analytics

**Rationale**: Security-first principle enforced. JWT-based auth, input validation, audit logging meet compliance requirements.

---

### 11. Migration Strategy

**Database Migration Order**:
1. **student-service V12**: Create provinces and districts tables
2. **student-service V13**: Populate provinces/districts from existing schools data
3. **student-service V14**: Add province_id and district_id columns to schools table
4. **student-service V15**: Backfill schools.province_id and schools.district_id by matching VARCHAR columns
5. **auth-service V5**: Create teacher_school table

**Backward Compatibility**:
- Keep schools.province and schools.district VARCHAR columns temporarily
- Add deprecation notice in code comments
- Plan removal in future release after migration verification

**Rollback Plan**:
- Migrations are reversible (DROP TABLE statements)
- Old VARCHAR columns remain as fallback
- No breaking changes to existing APIs

---

## Alternatives Considered and Rejected

### Alternative 1: Add school_id to users table directly
**Rejected**: Violates single responsibility. Users table is for authentication, not school associations. User requirement for principal metadata (name, gender) would pollute users table.

### Alternative 2: Create location-service microservice
**Rejected**: Violates YAGNI principle. Provinces/districts/schools are tightly coupled to student-service domain. No justification for separate service at current scale.

### Alternative 3: Keep province/district as VARCHAR columns
**Rejected**: Prevents efficient hierarchical filtering. No referential integrity. Difficult to maintain data consistency.

### Alternative 4: Use IP-based geolocation for province detection
**Rejected**: Over-engineering. Teachers know their school location. Geolocation adds unnecessary complexity and privacy concerns.

### Alternative 5: Admin approval workflow for new schools
**Rejected**: Not specified in requirements. Would slow down teacher onboarding. YAGNI principle - add if needed later.

---

## Summary of Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **teacher_school table location** | auth-service database | Separates authentication from school metadata; supports principal_name/gender fields |
| **Province/District tables** | Create in student-service | Enables hierarchical filtering; provides referential integrity |
| **school_id validation** | Application-layer cross-service call | Maintains microservice independence (no foreign key across databases) |
| **API structure** | RESTful endpoints split across services | Clear service boundaries; follows REST conventions |
| **Frontend table component** | ClientDataTable | Small datasets per district; no URL persistence needed |
| **School setup guard** | TanStack Router beforeLoad | Centralized guard logic; enforces setup completion |
| **Error codes** | Service-specific enums | Per constitution; frontend handles i18n translation |
| **Caching** | Deferred (TanStack Query default only) | YAGNI - premature optimization; simple queries sufficient |
| **School creation approval** | No approval (immediate) | Per spec; simplifies onboarding; YAGNI |

---

## Dependencies and Integration Points

### Cross-Service Communication
- **auth-service → student-service**: Validate school_id exists before creating teacher_school association
- **Frontend → auth-service**: Teacher authentication, school association CRUD
- **Frontend → student-service**: Location data fetching, school creation

### External Dependencies
- None (no third-party services required)

### Shared Libraries
- `sms-common`: ApiResponse<T>, ErrorCode enum, CommonConstants

---

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| **Province/district data migration fails** | Teachers can't select schools | Comprehensive testing; rollback plan; keep VARCHAR columns as fallback |
| **Cross-service school_id validation latency** | Slow teacher-school association | Add timeout + fallback; consider caching school IDs if needed |
| **Duplicate school names in same district** | Data ambiguity | Unique constraint on (district_id, name); error message guides user |
| **Teacher abandons setup** | Incomplete registration | Track metric; consider email reminder (future enhancement) |
| **School table grows large** | Query performance degrades | Index on district_id; pagination if > 1000 schools per district (unlikely) |

---

## Testing Strategy

**Unit Tests**:
- Service layer logic (province/district filtering, school creation, teacher-school association)
- Validation rules (DTO constraints, Zod schemas)

**Integration Tests**:
- API endpoints (GET provinces, GET districts, POST schools, POST teacher-school)
- Cross-service validation (school_id existence check)

**Contract Tests**:
- API response schemas match ApiResponse<T> format
- Error codes match documented enums

**Frontend Tests**:
- Component interactions (dropdown selection, table display, form submission)
- TanStack Query cache behavior
- Router guard redirect logic

**Manual Testing Scenarios**:
- Complete school setup flow (select existing school)
- Add new school and associate
- Change province/district selection
- Handle empty states (no districts, no schools)
- Error handling (network failures, validation errors)

---

## Next Steps

1. **Phase 1**: Generate data-model.md with detailed schema definitions
2. **Phase 1**: Generate API contracts with OpenAPI/Swagger specs
3. **Phase 1**: Generate quickstart.md with developer setup instructions
4. **Phase 2**: Generate tasks.md with implementation task breakdown

---

**Research completed**: 2025-12-10
**Status**: Ready for Phase 1 (Design & Contracts)
