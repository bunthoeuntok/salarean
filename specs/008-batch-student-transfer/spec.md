# Feature Specification: Batch Student Transfer

**Feature Branch**: `008-batch-student-transfer`
**Created**: 2025-12-04
**Status**: Draft
**Input**: User description: "add batch transfer student in class detail. Frontend in calss students list table add checkbox where user can check multiple students when it will show a floating button at the botton right of the page and confirm the list of selected student to transfer to a active class with the same grade."

## Clarifications

### Session 2025-12-04

- Q: How long after a transfer should users be able to undo it? → A: 5 minutes - Balanced recovery time
- Q: What should happen if a student from the original transfer has been transferred to another class before the undo is attempted? → A: Block entire undo - Prevent undo if any student has moved again, show explanation
- Q: How should the undo action be presented to users after a successful transfer? → A: Toast notification with undo button - Non-intrusive popup with countdown timer and undo button
- Q: Who should be allowed to undo a batch transfer? → A: Original user only - Only the user who performed the transfer can undo it
- Q: Should undo be available if the user refreshes the page or logs out and back in within the 5-minute window? → A: Client session only - Undo lost on page refresh or logout

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Select Multiple Students for Transfer (Priority: P1)

A teacher or administrator wants to select multiple students from the class student list to prepare them for batch transfer to another class.

**Why this priority**: This is the foundation of the batch transfer feature. Users must be able to select students before they can transfer them. This delivers immediate value by providing a visual indication of which students will be affected by the transfer operation.

**Independent Test**: Can be fully tested by displaying checkboxes next to each student in the list, clicking multiple checkboxes, and verifying that the selections are tracked and a floating action button appears. Delivers value by enabling multi-student selection without any backend changes.

**Acceptance Scenarios**:

1. **Given** I am viewing the student list in a class detail page, **When** the page loads, **Then** I see a checkbox next to each student's name in the table
2. **Given** I am viewing the student list, **When** I click a checkbox for a student, **Then** the checkbox is marked as selected and the student row is visually highlighted
3. **Given** I have selected one or more students, **When** I view the page, **Then** I see a floating action button at the bottom right of the page showing the count of selected students (e.g., "Transfer 3 Students")
4. **Given** I have not selected any students, **When** I view the page, **Then** the floating action button is hidden
5. **Given** I have selected multiple students, **When** I uncheck all checkboxes, **Then** the floating action button disappears
6. **Given** I have selected students, **When** I click the floating action button, **Then** a confirmation dialog opens showing the list of selected students

---

### User Story 2 - Confirm Transfer Destination (Priority: P1)

A teacher or administrator wants to review the selected students and choose a destination class before executing the batch transfer to ensure accuracy and prevent mistakes.

**Why this priority**: Critical for data integrity and user confidence. Users need to verify their selection and choose the correct destination class before committing to the transfer. This prevents accidental transfers and provides a safety checkpoint.

**Independent Test**: Can be fully tested by opening the confirmation dialog, viewing the list of selected students, selecting a destination class from a dropdown, and verifying that the dialog displays accurate information. Delivers value by providing transparency and control over the transfer operation.

**Acceptance Scenarios**:

1. **Given** I have clicked the floating action button, **When** the confirmation dialog opens, **Then** I see a list of all selected students with their names and student codes
2. **Given** I am viewing the confirmation dialog, **When** the dialog loads, **Then** I see a dropdown to select a destination class showing only active classes with the same grade level as the current class
3. **Given** I am viewing the confirmation dialog, **When** there are no other active classes with the same grade, **Then** I see a message indicating "No eligible classes available for transfer" and the transfer action is disabled
4. **Given** I am viewing the confirmation dialog, **When** I have not yet selected a destination class, **Then** the "Confirm Transfer" button is disabled
5. **Given** I have selected a destination class, **When** I click "Confirm Transfer", **Then** the system executes the batch transfer and shows a loading indicator
6. **Given** I am viewing the confirmation dialog, **When** I click "Cancel", **Then** the dialog closes and all student selections remain unchanged (checkboxes stay checked)

---

### User Story 3 - Execute Batch Transfer (Priority: P1)

A teacher or administrator wants to transfer all selected students to the destination class in a single operation to save time and reduce manual effort.

**Why this priority**: This is the core value proposition of the feature. Batch transfer significantly reduces time and effort compared to transferring students one by one. This is essential for common scenarios like class reorganization, balancing class sizes, or moving students to different sections.

**Independent Test**: Can be fully tested by confirming a batch transfer, verifying that all selected students are transferred from the source class to the destination class, and checking that the student list updates correctly. Delivers value by automating a previously manual multi-step process.

**Acceptance Scenarios**:

1. **Given** I have confirmed a batch transfer, **When** the transfer completes successfully, **Then** all selected students are removed from the current class's student list
2. **Given** a batch transfer has completed, **When** I view the destination class's student list, **Then** I see all transferred students added to that class
3. **Given** I have confirmed a batch transfer, **When** the transfer completes successfully, **Then** I see a success message indicating "X students successfully transferred to [Class Name]"
4. **Given** a batch transfer is in progress, **When** the system processes the request, **Then** I see a loading indicator and cannot interact with the page
5. **Given** a batch transfer has completed, **When** I view the page, **Then** all checkboxes are unchecked and the floating action button disappears
6. **Given** a batch transfer has completed, **When** I view the student list, **Then** the list is automatically refreshed to show the updated enrollment

---

### User Story 4 - Undo Batch Transfer (Priority: P1)

A teacher or administrator wants to reverse a recently completed batch transfer within 5 minutes to correct mistakes or respond to changed circumstances.

**Why this priority**: Critical safety net for error recovery. Users can make selection mistakes or transfer to the wrong destination class, and the ability to quickly undo prevents data corruption and reduces support burden. This feature significantly increases user confidence when performing bulk operations.

**Independent Test**: Can be fully tested by completing a batch transfer, clicking the undo button in the success toast notification within 5 minutes, and verifying that all students are returned to their original class. Delivers value by providing immediate error recovery without requiring manual reversal.

**Acceptance Scenarios**:

1. **Given** I have completed a batch transfer, **When** the transfer succeeds, **Then** I see a success toast notification with an "Undo" button and a countdown timer showing remaining time (e.g., "4:45 remaining")
2. **Given** the undo toast is visible, **When** I click the "Undo" button within 5 minutes, **Then** the system reverses the transfer and all students are returned to the original source class
3. **Given** I have clicked undo, **When** the undo operation completes, **Then** I see a confirmation message "Transfer undone: X students returned to [Original Class]"
4. **Given** I have clicked undo, **When** the undo completes, **Then** the student list in both source and destination classes is automatically refreshed
5. **Given** the undo toast is visible, **When** 5 minutes have passed since the transfer, **Then** the toast automatically disappears and undo is no longer available
6. **Given** I attempt to undo a transfer, **When** any student from the transfer has been transferred to another class, **Then** I see an error message "Cannot undo: One or more students have been transferred again" with details of which students have moved
7. **Given** the undo toast is visible, **When** I refresh the page or navigate away, **Then** the undo option is lost (session-based only)
8. **Given** I attempt to undo a transfer, **When** I am not the user who performed the original transfer, **Then** the undo button is not visible (only original user can undo)

---

### User Story 5 - Handle Transfer Errors (Priority: P2)

A teacher or administrator wants to be informed if some students cannot be transferred due to errors, so they can take corrective action.

**Why this priority**: Important for reliability and user trust, but less critical than the core transfer functionality. Errors are expected to be infrequent, and partial success handling provides a better user experience than all-or-nothing transfers.

**Independent Test**: Can be fully tested by simulating various error scenarios (validation errors, network errors, business rule violations) and verifying that appropriate error messages are displayed and the page state is handled correctly. Delivers value by preventing data loss and guiding users to resolve issues.

**Acceptance Scenarios**:

1. **Given** a batch transfer fails for all students, **When** the error occurs, **Then** I see an error message explaining the failure reason (e.g., "Transfer failed: Invalid destination class")
2. **Given** a batch transfer succeeds for some students but fails for others, **When** the transfer completes, **Then** I see a detailed message indicating which students were transferred successfully and which failed with reasons
3. **Given** a batch transfer encounters a network error, **When** the error occurs, **Then** I see a user-friendly error message (e.g., "Transfer failed due to connection error. Please try again.") and can retry the operation
4. **Given** a batch transfer fails, **When** I view the student list, **Then** the students remain in the current class (no partial state)
5. **Given** a batch transfer error occurs, **When** I close the error message, **Then** the confirmation dialog remains open with the same selections so I can retry or modify

---

### Edge Cases

- What happens when a student is already enrolled in the destination class? System should skip that student and display a warning message indicating "Student [Name] is already enrolled in [Destination Class]".
- What happens when the destination class has reached its capacity? System should prevent the transfer and display an error message indicating "Cannot transfer: [Destination Class] is at full capacity (X/Y students)".
- What happens when a student is transferred to the destination class by another user while the confirmation dialog is open? System should detect the conflict during transfer execution and display an error for that student indicating they are already enrolled.
- What happens when the source class is archived or deleted while the user is viewing it? System should detect this during transfer execution and display an error preventing the transfer.
- What happens when the user has selected 50+ students for transfer? System should handle large batch transfers efficiently with appropriate loading indicators and batch processing.
- What happens when the user refreshes the page after selecting students but before confirming transfer? All selections are lost (this is acceptable behavior; selections are session-based, not persisted).
- What happens when a user with read-only permissions tries to transfer students? The checkboxes and transfer button should not be visible to users without transfer permissions.
- What happens when the undo toast countdown reaches zero? The toast automatically disappears and the undo option is no longer available. The transfer becomes permanent.
- What happens when a user clicks undo multiple times rapidly? System should disable the undo button after the first click and show a loading indicator to prevent duplicate undo requests.
- What happens when the original source class is archived or deleted before the undo is attempted? System should block the undo and display an error message indicating the source class is no longer available.
- What happens when the destination class capacity has changed and would be exceeded by the undo? System should allow the undo since students are being returned to their original class (capacity was already validated during the original transfer).
- What happens when a different user (not the original transferring user) views a class where a transfer was just completed? They should not see the undo toast notification since only the original user can undo.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST display a checkbox next to each student in the class student list table
- **FR-002**: System MUST allow users to select multiple students by clicking their checkboxes
- **FR-003**: System MUST visually highlight selected student rows (e.g., with a background color change)
- **FR-004**: System MUST display a floating action button at the bottom right of the page when one or more students are selected
- **FR-005**: The floating action button MUST show the count of selected students (e.g., "Transfer 3 Students")
- **FR-006**: The floating action button MUST be hidden when no students are selected
- **FR-007**: System MUST open a confirmation dialog when the user clicks the floating action button
- **FR-008**: The confirmation dialog MUST display a list of all selected students with their names and student codes
- **FR-009**: The confirmation dialog MUST provide a dropdown to select a destination class
- **FR-010**: The destination class dropdown MUST show only active classes with the same grade level as the current class
- **FR-011**: The confirmation dialog MUST display a message "No eligible classes available for transfer" when there are no suitable destination classes, and disable the transfer action
- **FR-012**: The "Confirm Transfer" button MUST be disabled until a destination class is selected
- **FR-013**: System MUST execute the batch transfer when the user clicks "Confirm Transfer" in the dialog
- **FR-014**: System MUST show a loading indicator while the batch transfer is in progress
- **FR-015**: System MUST prevent user interaction with the page during the transfer operation
- **FR-016**: System MUST remove all successfully transferred students from the current class's student list
- **FR-017**: System MUST add all successfully transferred students to the destination class
- **FR-018**: System MUST create enrollment history records for each transferred student documenting the transfer
- **FR-019**: System MUST display a success message after a successful batch transfer (e.g., "5 students successfully transferred to Class 7A")
- **FR-020**: System MUST automatically refresh the student list after the transfer completes
- **FR-021**: System MUST uncheck all checkboxes and hide the floating action button after the transfer completes
- **FR-022**: System MUST display appropriate error messages if the batch transfer fails (network errors, validation errors, business rule violations)
- **FR-023**: System MUST handle partial success scenarios by displaying which students were transferred successfully and which failed with reasons
- **FR-024**: System MUST allow users to cancel the confirmation dialog without executing the transfer
- **FR-025**: System MUST preserve student selections when the user cancels the confirmation dialog
- **FR-026**: System MUST validate that the destination class has sufficient capacity before executing the transfer
- **FR-027**: System MUST prevent transferring students who are already enrolled in the destination class
- **FR-028**: System MUST validate that the user has permission to transfer students (authorization check)
- **FR-029**: The checkbox column MUST be hidden for users without transfer permissions
- **FR-030**: System MUST validate that all selected students are currently enrolled in the source class before executing the transfer
- **FR-031**: System MUST display a success toast notification with an "Undo" button after a successful batch transfer
- **FR-032**: The success toast MUST include a countdown timer showing the remaining time to undo (starting at 5 minutes)
- **FR-033**: The undo button MUST only be visible to the user who performed the original transfer
- **FR-034**: System MUST allow the user to click the undo button to reverse the transfer within 5 minutes
- **FR-035**: System MUST validate that none of the transferred students have been transferred again before allowing undo
- **FR-036**: System MUST block the entire undo operation if any student has been transferred to another class after the original transfer
- **FR-037**: System MUST display a clear error message when undo is blocked, indicating which students have been transferred again
- **FR-038**: System MUST reverse all student enrollments when undo is executed, returning students to the original source class
- **FR-039**: System MUST create enrollment history records documenting the undo operation
- **FR-040**: System MUST display a confirmation message after successful undo (e.g., "Transfer undone: 5 students returned to Class 7A")
- **FR-041**: System MUST automatically refresh the student lists in both source and destination classes after undo completes
- **FR-042**: The success toast MUST automatically disappear after 5 minutes, making undo unavailable
- **FR-043**: System MUST remove the undo option if the user refreshes the page or navigates away (session-based only)
- **FR-044**: System MUST disable the undo button after it is clicked to prevent duplicate undo requests
- **FR-045**: System MUST show a loading indicator while the undo operation is in progress
- **FR-046**: System MUST validate that the original source class still exists and is accessible before allowing undo

### Key Entities

- **Student Selection**: UI state tracking which students are selected for transfer, with attributes: student ID, student name, student code, selection status
- **Transfer Request**: The batch transfer operation request, with attributes: source class ID, destination class ID, list of student IDs to transfer, requested by user ID, timestamp
- **Transfer Result**: The outcome of a batch transfer operation, with attributes: total students requested, successfully transferred count, failed students list with error reasons
- **Destination Class**: An active class eligible for receiving transferred students, with attributes: class ID, class name, grade level, current enrollment count, maximum capacity
- **Undo State**: Session-based state tracking undo eligibility for a completed transfer, with attributes: transfer ID, source class ID, destination class ID, list of transferred student IDs, transfer timestamp, performing user ID, expiration timestamp (5 minutes after transfer)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Teachers can select multiple students using checkboxes in under 30 seconds for a typical class of 30 students
- **SC-002**: The floating action button appears within 100ms of selecting the first student
- **SC-003**: The confirmation dialog loads and displays all selected students within 1 second
- **SC-004**: The destination class dropdown loads all eligible classes within 2 seconds
- **SC-005**: Batch transfer of up to 20 students completes within 5 seconds under normal network conditions
- **SC-006**: The student list refreshes and shows updated enrollment within 2 seconds after transfer completion
- **SC-007**: 95% of batch transfers complete successfully without errors
- **SC-008**: Users can successfully transfer 5 students to another class in under 2 minutes (compared to 10+ minutes for manual individual transfers)
- **SC-009**: The confirmation dialog prevents 100% of accidental transfers by requiring explicit destination class selection and confirmation
- **SC-010**: Error messages clearly identify which students failed to transfer and why, with 90% of users understanding how to resolve the issue
- **SC-011**: The feature remains responsive on mobile devices (tablet and phone screen sizes) with checkboxes and floating button accessible via touch
- **SC-012**: The success toast notification with undo button appears within 500ms of transfer completion
- **SC-013**: Users can successfully undo a batch transfer within 10 seconds by clicking the undo button
- **SC-014**: The countdown timer in the toast accurately reflects remaining undo time (updates every second)
- **SC-015**: Undo operations complete within 3 seconds for transfers of up to 20 students
- **SC-016**: 100% of undo operations that pass validation successfully reverse all student enrollments
- **SC-017**: Users attempting to undo after students have been transferred again receive a clear explanation of why undo is blocked
- **SC-018**: The toast automatically disappears exactly 5 minutes after the transfer with no manual dismissal required

### Business Value

- **BV-001**: Reduces time spent on class reorganization by 80% compared to manual individual student transfers
- **BV-002**: Enables efficient class balancing and student regrouping during term transitions or administrative changes
- **BV-003**: Improves data accuracy by reducing human error associated with repetitive manual transfers
- **BV-004**: Increases teacher and administrator productivity by automating a common bulk operation
- **BV-005**: Reduces support tickets and administrative burden by allowing users to self-correct transfer mistakes within 5 minutes

## Assumptions

- The class detail page and student list already exist and display enrolled students (from feature 007-class-view)
- Student enrollment API already supports transferring students between classes (individual transfer capability exists)
- User authentication and authorization are already implemented (users can only transfer students in classes they have permission to manage)
- The system already tracks class enrollment counts and capacity limits
- Grade levels are standardized across classes (e.g., Grade 7, Grade 8), enabling same-grade filtering
- The confirmation dialog will use a standard modal/dialog UI component from the design system (shadcn/ui)
- The floating action button follows Material Design guidelines for floating action button placement and behavior
- Network errors are transient and users can retry failed transfers
- Partial success scenarios are acceptable (some students transfer successfully even if others fail) with clear communication to users
- The batch transfer operation is atomic per student (each student transfer either succeeds or fails independently)
- Toast notifications are supported by the UI framework for displaying success messages with action buttons
- Client-side session storage or state management (Zustand) is sufficient for tracking undo state (no server-side persistence required for the 5-minute window)
- Users who need to undo a transfer will typically recognize the mistake immediately and act within the same session
- The 5-minute undo window is sufficient for most error recovery scenarios without creating long-term data uncertainty

## Out of Scope

The following are explicitly excluded from this feature:

- Transferring students to classes with different grade levels (only same-grade transfers are supported)
- Transferring students to archived or inactive classes
- Scheduling batch transfers for a future date/time
- Persistent undo beyond 5 minutes or across user sessions (undo is session-based only)
- Sending notifications to students or parents about the transfer
- Bulk student import/export functionality
- Advanced filtering or sorting of the student list before selection
- Selecting all students with a single "Select All" checkbox (users must select students individually)
- Transferring students between different schools or campuses
- Approval workflow for batch transfers (transfers execute immediately upon confirmation)
- Audit log or detailed transfer history UI (enrollment history is updated, but no dedicated transfer report)

## Dependencies

- **Class Detail Page (007-class-view)**: Must provide the student list table UI where checkboxes will be added
- **Student Enrollment API**: Must provide an endpoint to transfer students between classes (or support batch enrollment changes) and undo transfers
- **Class List API**: Must provide an endpoint to fetch active classes filtered by grade level for the destination dropdown
- **Authorization System**: Must verify that the user has permission to transfer students in the current class and track the user who performed each transfer
- **UI Components**: Modal/dialog component, floating action button component, and toast notification component from shadcn/ui or design system
- **State Management**: Frontend state management (Zustand) to track selected students, transfer operation status, and undo state (transfer metadata, countdown timer)

## Risks & Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Large batch transfers (50+ students) cause performance issues | Medium | Low | Implement client-side batching and server-side rate limiting; show progress indicator for large batches |
| Concurrent transfers by multiple users cause data conflicts | High | Medium | Implement optimistic locking or last-write-wins with conflict detection; display clear error messages for conflicts |
| Destination class capacity not checked, leading to over-enrollment | High | Medium | Validate capacity before transfer; display current enrollment and capacity in destination dropdown |
| Users accidentally transfer students to wrong class | High | Medium | Require explicit destination class selection; show clear confirmation dialog with both source and destination class names; provide 5-minute undo window |
| Network errors during transfer leave students in uncertain state | Medium | Medium | Implement idempotent transfer API; allow retry without duplicating transfers; show clear error messages |
| Users expect to transfer students across different grades | Low | High | Use clear UI messaging that only same-grade classes are shown; document limitation in help text |
| Users refresh page and lose undo ability for a recent transfer | Medium | Medium | Use clear messaging in toast that undo is session-based; display countdown timer to encourage immediate action if needed |
| Undo conflicts when students have been transferred again | High | Low | Block entire undo with clear explanation; validate student state before allowing undo |
| Users attempt undo after 5-minute window expires | Low | Medium | Toast disappears automatically; clear visual countdown timer indicates remaining time |

## Future Enhancements

The following features may be considered for future releases but are not part of this specification:

- **Select All Checkbox**: Add a checkbox in the table header to select/unselect all students at once
- **Extended Undo Window**: Increase undo time window beyond 5 minutes (e.g., 30 minutes, 1 hour)
- **Persistent Undo**: Allow undo to persist across page refreshes and user sessions (requires server-side storage)
- **Partial Undo**: Allow undoing only specific students from a batch transfer rather than all-or-nothing
- **Undo History Page**: Dedicated UI to view all recent transfers with undo options
- **Transfer Notifications**: Send email or SMS notifications to students and parents when they are transferred to a new class
- **Cross-Grade Transfer**: Support transferring students to classes with different grade levels (with additional validation)
- **Transfer Approval Workflow**: Require administrator approval before executing batch transfers
- **Transfer Scheduling**: Allow users to schedule batch transfers for a future date/time
- **Detailed Transfer Report**: Generate a downloadable report showing all transfer details, affected students, and timestamps
- **Advanced Filtering**: Allow users to filter students by criteria (e.g., enrollment status, attendance rate) before selecting for transfer
- **Bulk Transfer History**: Provide a dedicated UI to view all past batch transfer operations with full details
- **Transfer Templates**: Save frequently used transfer configurations (e.g., "Move top 10 students to advanced class") for quick reuse
