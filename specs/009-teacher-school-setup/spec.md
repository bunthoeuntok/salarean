# Feature Specification: Teacher School Setup

**Feature Branch**: `009-teacher-school-setup`
**Created**: 2025-12-10
**Status**: Draft
**Input**: User description: "teacher school feature, after new user register as teacher, it should redirect user to set up their school inforamtion. Select province then district then select aviable school from table schools, if school not exist user can add new school under select province and district."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Select Existing School (Priority: P1)

After registering as a teacher, the user needs to associate their account with an existing school in the database by navigating through a hierarchical location structure (Province → District → School).

**Why this priority**: This is the most common scenario - teachers joining schools that already exist in the system. Enables immediate platform usage without administrative overhead.

**Independent Test**: Can be fully tested by registering a new teacher account and successfully selecting an existing school from the database, then verifying the teacher's profile shows the correct school association.

**Acceptance Scenarios**:

1. **Given** a newly registered teacher with no school assignment, **When** they are redirected to the school setup page, **Then** they see a province selection dropdown
2. **Given** a province is selected, **When** the selection is confirmed, **Then** the district dropdown is populated with districts in that province
3. **Given** a district is selected, **When** the selection is confirmed, **Then** a table displays all schools in that district
4. **Given** the schools table is displayed, **When** the teacher selects a school from the table, **Then** their account is associated with that school and they proceed to the main application

---

### User Story 2 - Add New School (Priority: P2)

When a teacher's school doesn't exist in the database, they need the ability to add their school under the correct province and district so they can complete registration and use the platform.

**Why this priority**: Critical for onboarding teachers from new schools, but less common than selecting existing schools. Enables platform growth without admin bottlenecks.

**Independent Test**: Can be tested independently by registering a new teacher, navigating to Province → District, confirming no matching school exists, then successfully adding a new school and verifying it appears in the schools table.

**Acceptance Scenarios**:

1. **Given** a teacher has selected province and district but their school is not listed, **When** they click "Add New School" option, **Then** a form appears to enter new school details
2. **Given** the new school form is displayed, **When** the teacher enters valid school information (name, address, etc.), **Then** the new school is saved to the database under the selected province and district
3. **Given** a new school has been successfully added, **When** the teacher returns to the school selection table, **Then** the newly added school appears in the list and can be selected
4. **Given** a new school is added, **When** the teacher selects it, **Then** their account is associated with that school and they proceed to the main application

---

### User Story 3 - Edit Selection Before Confirmation (Priority: P3)

Teachers may accidentally select the wrong province or district and need the ability to change their selection before final confirmation.

**Why this priority**: Improves user experience by preventing errors, but not blocking for MVP since users can refresh/restart the flow.

**Independent Test**: Can be tested by starting the school setup flow, selecting a province, then changing to a different province, and verifying the district dropdown resets appropriately.

**Acceptance Scenarios**:

1. **Given** a teacher has selected a province, **When** they change the province selection, **Then** the district dropdown resets and repopulates with the new province's districts
2. **Given** a teacher has selected a district, **When** they change the district selection, **Then** the schools table refreshes to show schools from the new district
3. **Given** a teacher has made selections, **When** they navigate back in the flow, **Then** their previous selections are preserved until they make a new choice

---

### Edge Cases

- What happens when a province has no districts in the database?
- What happens when a district has no schools in the database?
- How does the system handle duplicate school names within the same district?
- What happens if the user closes the browser during school setup?
- How does the system prevent teachers from registering twice with different schools?
- What validation is required for new school information (minimum fields, character limits)?
- Can a teacher change their school association after initial setup?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST redirect newly registered teachers to the school setup page immediately after registration completion
- **FR-002**: System MUST display a dropdown/selector for provinces populated from the existing schools database
- **FR-003**: System MUST display a dropdown/selector for districts that filters based on the selected province
- **FR-004**: System MUST display a table of schools filtered by the selected province and district
- **FR-005**: System MUST allow teachers to select a school from the displayed table to associate with their account
- **FR-006**: System MUST provide an "Add New School" option when viewing the schools table
- **FR-007**: System MUST display a form to collect new school information (name, address, contact details) when adding a new school
- **FR-008**: System MUST validate new school data before saving to the database
- **FR-009**: System MUST save new schools under the correct province and district hierarchy
- **FR-010**: System MUST associate the teacher's account with the selected or newly created school
- **FR-011**: System MUST prevent access to the main application until school setup is completed
- **FR-012**: System MUST persist the teacher-school association in the database
- **FR-013**: System MUST display appropriate error messages when selections fail or data cannot be loaded

### Key Entities

- **Province**: Represents a Cambodian province; contains a name/identifier and has many districts
- **District**: Represents a district within a province; contains a name/identifier, belongs to one province, and has many schools
- **School**: Represents an educational institution; contains name, address, contact information; belongs to one district and one province; has many teachers
- **Teacher**: Represents a registered teacher account; contains user credentials and profile information; belongs to one school after setup completion

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Teachers can complete school setup (from registration to main app access) in under 3 minutes for existing schools
- **SC-002**: Teachers can add a new school and complete setup in under 5 minutes
- **SC-003**: 95% of teachers successfully complete school setup on first attempt without errors
- **SC-004**: System displays location dropdowns and school tables in under 2 seconds after each selection
- **SC-005**: Zero teachers can access the main application without completing school setup
- **SC-006**: New school additions are immediately visible in the schools table for subsequent teacher registrations

## Assumptions

- The `schools` table already exists in the database with province and district relationships established
- Province and district data is already populated in the database
- School data includes sufficient information to uniquely identify schools within a district
- Teachers can only be associated with one school at a time
- The registration flow creates a teacher account before redirecting to school setup
- Teachers have basic familiarity with Cambodia's provincial/district structure
- School names within the same district are unique (or system can handle disambiguation)
