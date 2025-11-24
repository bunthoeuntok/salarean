# Feature Specification: Class Management API

**Feature Branch**: `005-class-management`
**Created**: 2025-11-24
**Status**: Draft
**Input**: User description: "need to implement new api List teacher's classes, Get class details, Get current class student, Get class enrollment history, Create class, Update class, Archive class, please consider to use radis cache for class list and student class"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View My Classes (Priority: P1)

As a teacher, I want to view a list of all my classes so that I can quickly access the classes I'm teaching and monitor my teaching schedule.

**Why this priority**: This is the primary entry point for teachers to interact with their classes. Without this, teachers cannot navigate to any specific class, making it the foundation for all other class-related features.

**Independent Test**: Can be fully tested by authenticating as a teacher and requesting the class list. Delivers immediate value by showing teachers their teaching assignments.

**Acceptance Scenarios**:

1. **Given** I am a logged-in teacher with 3 active classes, **When** I request my class list, **Then** I see all 3 classes with basic information (class name, grade level, subject, student count)
2. **Given** I am a logged-in teacher with no classes, **When** I request my class list, **Then** I see an empty list with an appropriate message
3. **Given** I am a logged-in teacher with both active and archived classes, **When** I request my class list, **Then** I see only active classes by default
4. **Given** I have previously viewed my class list, **When** I request it again, **Then** the response is retrieved from cache and loads faster

---

### User Story 2 - View Class Details (Priority: P1)

As a teacher, I want to view detailed information about a specific class so that I can see the complete class profile including all enrolled students.

**Why this priority**: Essential for teachers to access detailed class information and student roster. This is critical for daily teaching activities and class management.

**Independent Test**: Can be fully tested by selecting a class from the list and viewing its details. Delivers value by providing complete class information including current enrollment.

**Acceptance Scenarios**:

1. **Given** I am viewing a specific class, **When** I request class details, **Then** I see complete class information (name, grade, subject, capacity, student count, description)
2. **Given** I am viewing a specific class, **When** I request class details, **Then** I see a list of all currently enrolled students with their basic information (name, student ID, enrollment date)
3. **Given** I request details for a class I don't teach, **When** the system processes the request, **Then** I receive an authorization error
4. **Given** I have previously viewed a class's details, **When** I request them again, **Then** the student list is retrieved from cache

---

### User Story 3 - Review Enrollment History (Priority: P2)

As a teacher, I want to view the enrollment history for my class so that I can track which students have joined or left the class over time.

**Why this priority**: Important for record-keeping and understanding class dynamics, but not required for basic class management. Teachers need this for administrative purposes and tracking student movements.

**Independent Test**: Can be fully tested by viewing a class's enrollment history. Delivers value by providing historical context about class composition changes.

**Note**: This feature provides read-only access to enrollment history. Enrollment history records are created by the separate student enrollment feature.

**Acceptance Scenarios**:

1. **Given** I am viewing a class with enrollment changes, **When** I request enrollment history, **Then** I see all enrollment events (student enrolled, student transferred out, student withdrawn) sorted by date (newest first)
2. **Given** I am viewing enrollment history, **When** the list includes transfers, **Then** each transfer shows the destination class
3. **Given** I am viewing a class with no enrollment changes, **When** I request enrollment history, **Then** I see only the initial enrollment records
4. **Given** I request enrollment history for a class I don't teach, **When** the system processes the request, **Then** I receive an authorization error

---

### User Story 4 - Create New Class (Priority: P2)

As a school administrator or authorized teacher, I want to create a new class so that I can set up classes for the upcoming term or academic year.

**Why this priority**: Essential for class setup but typically happens during administrative periods rather than daily operations. Required before classes can be used but not as frequently needed as viewing classes.

**Independent Test**: Can be fully tested by creating a new class with required information. Delivers value by enabling class setup and management infrastructure.

**Acceptance Scenarios**:

1. **Given** I have permission to create classes, **When** I submit complete class information (name, grade level, subject, capacity), **Then** a new class is created and I receive the class details
2. **Given** I create a new class, **When** the class is successfully created, **Then** the class appears in my class list
3. **Given** I attempt to create a class with incomplete information, **When** I submit the request, **Then** I receive validation errors indicating which fields are required
4. **Given** I attempt to create a class with a duplicate name for the same grade and subject, **When** I submit the request, **Then** I receive an error indicating the conflict
5. **Given** I create a new class, **When** the class is created, **Then** the class list cache is invalidated to reflect the new class

---

### User Story 5 - Update Class Information (Priority: P3)

As a school administrator or authorized teacher, I want to update class information so that I can correct errors or adjust class details as needed.

**Why this priority**: Useful for maintaining accurate class information but less critical than viewing or creating classes. Updates are infrequent and can be handled through workarounds if needed.

**Independent Test**: Can be fully tested by modifying an existing class's information. Delivers value by enabling class information maintenance.

**Acceptance Scenarios**:

1. **Given** I have permission to update a class, **When** I submit updated class information, **Then** the class details are updated and I receive the updated class information
2. **Given** I update class information, **When** the update is successful, **Then** the changes are reflected immediately in class details views
3. **Given** I attempt to update a class I don't have permission to modify, **When** I submit the request, **Then** I receive an authorization error
4. **Given** I update class information, **When** the update is successful, **Then** both class list and class details caches are invalidated
5. **Given** I attempt to update with invalid data, **When** I submit the request, **Then** I receive validation errors

---

### User Story 6 - Archive Completed Class (Priority: P3)

As a school administrator or authorized teacher, I want to archive classes that are no longer active so that I can keep my class list focused on current classes while preserving historical records.

**Why this priority**: Important for long-term data management but not required for daily operations. Archiving is typically done at term/year end and doesn't affect active teaching.

**Independent Test**: Can be fully tested by archiving a completed class. Delivers value by enabling historical record management without deleting data.

**Acceptance Scenarios**:

1. **Given** I have permission to archive a class, **When** I archive the class, **Then** the class is marked as archived and no longer appears in the default class list
2. **Given** I archive a class, **When** the archival is successful, **Then** I can still access the archived class and its historical data by filtering for archived classes
3. **Given** I attempt to archive a class with currently enrolled students, **When** I submit the request, **Then** the class is archived successfully and students remain enrolled in the archived class
4. **Given** I archive a class, **When** the archival is successful, **Then** the class list cache is invalidated
5. **Given** I attempt to archive a class I don't have permission to modify, **When** I submit the request, **Then** I receive an authorization error

---

### Edge Cases

- What happens when a teacher requests a class that doesn't exist?
- How does the system handle concurrent updates to the same class information?
- What happens when cache is unavailable or redis connection fails?
- How does the system handle very large class lists (teachers with 50+ classes)?
- What happens when a class has exceeded its maximum student capacity?
- What happens when attempting to view enrollment history for a newly created class with no history?
- How does the system handle archived classes in search or filtering operations?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide an endpoint to list all classes for the authenticated teacher
- **FR-002**: System MUST filter active classes by default, excluding archived classes from the class list
- **FR-003**: System MUST cache class list results in Redis to improve performance
- **FR-004**: System MUST provide an endpoint to retrieve detailed information for a specific class
- **FR-005**: System MUST include the current student roster when returning class details
- **FR-006**: System MUST cache class details including student roster in Redis
- **FR-007**: System MUST provide an endpoint to retrieve enrollment history for a specific class
- **FR-008**: System MUST sort enrollment history by date in descending order (newest first)
- **FR-009**: System MUST include enrollment event type (enrolled, transferred, withdrawn) and timestamp in history records
- **FR-010**: System MUST provide an endpoint to create a new class with required information (name, grade level, subject, capacity, academic year)
- **FR-011**: System MUST validate class creation data including required fields and data format
- **FR-012**: System MUST validate academic year format as "YYYY-YYYY" where second year equals first year plus one
- **FR-013**: System MUST prevent duplicate classes with the same name, grade level, subject, and academic year combination
- **FR-014**: System MUST invalidate class list cache when a new class is created
- **FR-015**: System MUST provide an endpoint to update existing class information
- **FR-016**: System MUST validate update requests to ensure data integrity
- **FR-017**: System MUST invalidate both class list and class details caches when class information is updated
- **FR-018**: System MUST provide an endpoint to archive a class
- **FR-019**: System MUST maintain archived class data for historical reference
- **FR-020**: System MUST invalidate class list cache when a class is archived
- **FR-021**: System MUST verify teacher authorization before allowing access to class information
- **FR-022**: System MUST verify appropriate permissions before allowing class creation, updates, or archival
- **FR-023**: System MUST handle cache misses gracefully by fetching data from the database
- **FR-024**: System MUST handle Redis connection failures without breaking core functionality
- **FR-025**: System MUST return appropriate error messages for authorization failures
- **FR-026**: System MUST return appropriate error messages for validation failures
- **FR-027**: System MUST return appropriate error messages when requested class does not exist

### Key Entities

- **Class**: Represents an academic class taught by a teacher. Key attributes include class name, grade level, subject, academic year (format "YYYY-YYYY"), capacity, description, creation date, and archive status. Each class is associated with a specific teacher and contains a roster of enrolled students.

- **Enrollment History**: Represents the chronological record of student enrollment changes for a class. Key attributes include student reference, event type (enrolled, transferred, withdrawn), event timestamp, and for transfers, the destination class reference. Maintains complete audit trail of class composition changes. **Note**: This feature provides read-only access; records are created by the separate student enrollment feature.

- **Student Roster**: Represents the current list of students enrolled in a class. Key attributes include student reference, enrollment date, and student status. This is a snapshot of current enrollments, distinct from the complete enrollment history.

- **Teacher**: Represents the instructor assigned to teach classes. Referenced by class entity to establish ownership and determine authorization for class access and management.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Teachers can view their complete class list in under 2 seconds on first load
- **SC-002**: Cached class list requests return results in under 500 milliseconds
- **SC-003**: Teachers can access detailed class information including full student roster in under 2 seconds
- **SC-004**: System supports at least 100 concurrent teachers accessing their class information without performance degradation
- **SC-005**: Class creation completes in under 3 seconds including validation
- **SC-006**: 100% of unauthorized access attempts are blocked with appropriate error messages
- **SC-007**: Cache hit rate for class list and class details reaches at least 70% after initial usage
- **SC-008**: System remains functional (read operations) even when cache is unavailable, with graceful degradation
- **SC-009**: All API endpoints return consistent error response format following project standards (errorCode + data structure)
- **SC-010**: Teachers can successfully archive classes and verify they no longer appear in default class lists

## Clarifications

### Session 2025-11-24

- Q: Should class management be implemented as a new microservice or integrated into student-service? → A: Integrate into student-service (Option B) - Classes and students are tightly coupled; eliminates cross-service calls for rosters; enables atomic enrollment transactions; reduces operational complexity
- Q: What grade level values should the system support for Cambodia's education system? → A: Grades 1-12 only (Primary 1-6, Lower Secondary 7-9, Upper Secondary 10-12) - Matches Cambodia's Ministry of Education 6-3-3 structure
- Q: How should class schedule information be structured? → A: Schedule handled by separate future service - Class entity will NOT include schedule data; scheduling to be implemented as dedicated service later
- Q: How should academic year be formatted and validated for classes? → A: Format "YYYY-YYYY" (e.g., "2024-2025") with validation that second year = first year + 1
- Q: Who creates enrollment history records - this class management feature or a separate enrollment feature? → A: Separate enrollment feature - Class management only reads history; enrollment operations create records

## Assumptions

1. Teacher authentication is handled by the existing auth-service (JWT-based authentication)
2. Class management functionality will be integrated into the existing student-service (not a separate microservice)
3. Student data and enrollment operations are managed within student-service
4. Redis is already configured and available in the infrastructure
5. Default cache TTL will be set to 30 minutes for class list and 15 minutes for class details (adjustable based on usage patterns)
6. Permission system distinguishes between regular teachers and administrators, with administrators having broader class management permissions
7. The system will use the existing ApiResponse<T> wrapper and ErrorCode enum from sms-common library
8. Maximum class capacity is a configurable limit (suggested default: 40 students)
9. Grade levels are enumerated as GRADE_1 through GRADE_12, following Cambodia's MoEYS structure (Primary 1-6, Lower Secondary 7-9, Upper Secondary 10-12)
10. All timestamps use Cambodia timezone (Asia/Phnom_Penh) as per project standards
11. Class and student data will reside in the same database (student_db) within student-service

## Dependencies

- **auth-service**: Required for teacher authentication and authorization
- **student-service (extended)**: Will contain both student and class management functionality
- **sms-common library**: Required for ApiResponse wrapper, ErrorCode enum, and common utilities
- **Redis**: Required for caching class list and class details
- **PostgreSQL**: Required for persistent storage of class and student data (shared student_db database)

## Out of Scope

- Student enrollment/withdrawal operations (separate feature within student-service; this feature only views enrollment history)
- Class scheduling and timetable management (will be implemented as separate service in future)
- Class attendance tracking
- Grade management within classes
- Automated class creation from templates
- Bulk class operations (bulk create, bulk archive)
- Class cloning or duplication
- Parent or student access to class information
- Class capacity enforcement during enrollment (handled by enrollment service)
- Email notifications for class changes
- Class roster export to external formats
- Integration with external learning management systems
