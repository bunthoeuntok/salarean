# Technical Research: Batch Student Transfer with Undo

**Date**: 2025-12-04
**Feature**: 008-batch-student-transfer

## Research Summary

This document consolidates technical research for implementing batch student transfer functionality with a 5-minute undo window. All technical unknowns from the planning phase have been resolved.

---

## 1. Session-Based Undo State Management

**Decision**: Use Zustand store with session storage persistence

**Rationale**:
- Zustand provides lightweight, type-safe state management ideal for session-based data
- Session storage persists across page refreshes within the same tab/window
- Automatic cleanup on tab close aligns with the "session-based only" requirement
- No server-side storage needed, reducing complexity

**Implementation Approach**:
```typescript
// Store structure
interface UndoState {
  transferId: string;
  sourceClassId: string;
  destinationClassId: string;
  studentIds: string[];
  performedBy: string;
  transferredAt: number; // Unix timestamp
  expiresAt: number; // transferredAt + 5 minutes
}

// Zustand store with session persistence
const useUndoStore = create<UndoStore>()(
  persist(
    (set) => ({
      undoState: null,
      setUndoState: (state) => set({ undoState: state }),
      clearUndoState: () => set({ undoState: null }),
    }),
    {
      name: 'batch-transfer-undo',
      storage: createJSONStorage(() => sessionStorage),
    }
  )
);
```

**Alternatives Considered**:
- **Local Storage**: Rejected - persists across sessions, violating "session-based only" requirement
- **Server-side storage**: Rejected - adds complexity for a 5-minute window
- **React Context**: Rejected - lost on page refresh

---

## 2. Toast Notification with Countdown Timer

**Decision**: Use shadcn/ui Toast component with custom countdown hook

**Rationale**:
- shadcn/ui Toast (Radix UI primitives) supports action buttons and custom content
- Material Design-aligned patterns already established in the project
- Supports auto-dismiss with configurable duration (5 minutes = 300,000ms)
- Non-intrusive, accessible, and familiar UX pattern

**Implementation Approach**:
```typescript
// Custom hook for countdown timer
const useCountdown = (targetTimestamp: number) => {
  const [remaining, setRemaining] = useState<number>(0);

  useEffect(() => {
    const interval = setInterval(() => {
      const now = Date.now();
      const diff = targetTimestamp - now;
      setRemaining(diff > 0 ? diff : 0);

      if (diff <= 0) {
        clearInterval(interval);
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [targetTimestamp]);

  return remaining;
};

// Toast with undo button and countdown
const { toast } = useToast();
toast({
  title: "5 students transferred to Class 7A",
  description: `Undo available for ${formatTime(remaining)}`,
  action: (
    <Button onClick={handleUndo} disabled={isUndoing}>
      Undo
    </Button>
  ),
  duration: 300000, // 5 minutes
});
```

**Alternatives Considered**:
- **Persistent Banner**: Rejected - too intrusive, blocks content
- **Modal Dialog**: Rejected - blocks user workflow
- **History Page Only**: Rejected - poor discoverability

---

## 3. Batch Transfer API Design

**Decision**: Create new batch transfer endpoint with validation

**Endpoint**: `POST /api/classes/{classId}/students/batch-transfer`

**Request**:
```json
{
  "destinationClassId": "uuid",
  "studentIds": ["uuid1", "uuid2", "uuid3"]
}
```

**Response**:
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "transferId": "uuid",
    "sourceClassId": "uuid",
    "destinationClassId": "uuid",
    "successfulTransfers": 5,
    "failedTransfers": [],
    "transferredAt": "2025-12-04T10:30:00Z"
  }
}
```

**Validation Logic**:
1. Verify all students are enrolled in source class
2. Check destination class capacity (capacity >= currentCount + transferCount)
3. Validate same grade level (source.grade === destination.grade)
4. Check user permissions (can transfer students in source class)
5. Verify no duplicate enrollments (students not already in destination class)

**Rationale**:
- Single atomic operation reduces network requests
- Server-side validation ensures data integrity
- Transfer ID enables undo tracking
- Partial failure support (failed students list) provides transparency

**Alternatives Considered**:
- **Individual Transfer Calls**: Rejected - high latency, no atomicity
- **GraphQL Mutation**: Rejected - REST already established in project

---

## 4. Undo Transfer API Design

**Decision**: Create dedicated undo endpoint with conflict detection

**Endpoint**: `POST /api/transfers/{transferId}/undo`

**Response**:
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "undoneStudents": 5,
    "sourceClassId": "uuid",
    "undoneAt": "2025-12-04T10:35:00Z"
  }
}
```

**Error Codes**:
- `TRANSFER_NOT_FOUND`: Transfer ID does not exist
- `UNDO_EXPIRED`: More than 5 minutes have passed (server-side check)
- `UNDO_CONFLICT`: One or more students have been transferred again
- `UNDO_UNAUTHORIZED`: User is not the one who performed the original transfer
- `SOURCE_CLASS_NOT_FOUND`: Original source class has been deleted

**Conflict Detection Logic**:
```sql
-- Check if any student has been enrolled in a different class after the original transfer
SELECT se.student_id
FROM student_enrollments se
WHERE se.student_id IN (transferred_student_ids)
  AND se.enrolled_at > original_transfer_timestamp
  AND se.status = 'ACTIVE'
  AND se.class_id != original_destination_class_id
```

**Rationale**:
- Idempotent operation (can safely retry)
- Server-side validation ensures data consistency
- Clear error codes for client-side messaging
- Audit trail preserved (enrollment history records undo action)

---

## 5. Enrollment History Tracking

**Decision**: Extend existing enrollment history table with transfer metadata

**Schema Addition**:
```sql
ALTER TABLE enrollment_history ADD COLUMN transfer_id UUID;
ALTER TABLE enrollment_history ADD COLUMN undo_of_transfer_id UUID;
ALTER TABLE enrollment_history ADD COLUMN performed_by_user_id UUID NOT NULL;
```

**Record Types**:
1. **TRANSFERRED_OUT**: Student leaves source class (action = 'TRANSFER', transfer_id set)
2. **TRANSFERRED_IN**: Student joins destination class (action = 'TRANSFER', transfer_id set)
3. **UNDO_TRANSFER**: Reversal of a transfer (action = 'UNDO', undo_of_transfer_id set)

**Rationale**:
- Reuses existing enrollment_history infrastructure
- Maintains complete audit trail
- Transfer ID links related enrollment events
- Supports future reporting and analytics

**Alternatives Considered**:
- **Separate Transfer Table**: Rejected - duplicates enrollment history data
- **Soft Delete Enrollments**: Rejected - doesn't track history

---

## 6. TanStack Query Cache Invalidation

**Decision**: Invalidate class student lists on transfer and undo

**Implementation**:
```typescript
const queryClient = useQueryClient();

// After successful transfer
queryClient.invalidateQueries({ queryKey: ['class-students', sourceClassId] });
queryClient.invalidateQueries({ queryKey: ['class-students', destinationClassId] });

// After successful undo
queryClient.invalidateQueries({ queryKey: ['class-students', sourceClassId] });
queryClient.invalidateQueries({ queryKey: ['class-students', undoState.destinationClassId] });
```

**Rationale**:
- Ensures UI reflects latest enrollment data
- TanStack Query already established in project
- Automatic refetch when cache is invalidated
- Optimistic updates could be added later for better UX

---

## 7. UI Component Selection

**Decision**: Use ClientDataTableWithUrl for class student lists

**Rationale** (per Constitution DataTable Selection Standards):
- Class student lists are typically < 100 students (within < 1000 rows threshold)
- All students loaded upfront (no pagination needed)
- Users need to share links with search/sort state (e.g., "Class 7A with 'transferred' status filter")
- State should persist on page refresh for usability

**Component Usage**:
```typescript
<ClientDataTableWithUrl
  data={classStudents}
  columns={studentColumns}
  storageKey={`class-students-${classId}`}
  searchPlaceholder="Search students..."
/>
```

**Checkboxes Integration**:
- Add selection column to DataTable columns configuration
- Use TanStack Table's built-in row selection state
- Track selected rows in Zustand store for transfer dialog

**Alternatives Considered**:
- **ServerDataTable**: Rejected - class sizes too small for pagination
- **ClientDataTable**: Rejected - no URL persistence for shareable views

---

## 8. Permission Model

**Decision**: Reuse existing class management permissions

**Permission Check**:
- User must have `TRANSFER_STUDENTS` permission for the source class
- Permission is role-based (teachers, admins)
- Checked at API Gateway and service layer

**Undo Permission**:
- Server stores `performed_by_user_id` with each transfer record
- Undo endpoint validates `currentUser.id === transfer.performedByUserId`
- If mismatch, return `UNDO_UNAUTHORIZED` error

**Rationale**:
- Leverages existing RBAC infrastructure
- No new permission types needed
- Accountability through user ID tracking

---

## 9. Floating Action Button Pattern

**Decision**: Use shadcn/ui Button with fixed positioning

**Implementation**:
```tsx
{selectedStudents.length > 0 && (
  <div className="fixed bottom-6 right-6 z-50">
    <Button size="lg" onClick={openTransferDialog}>
      <ArrowRight className="mr-2 h-5 w-5" />
      Transfer {selectedStudents.length} Student{selectedStudents.length > 1 ? 's' : ''}
    </Button>
  </div>
)}
```

**Rationale**:
- Material Design pattern, familiar to users
- Fixed positioning ensures always visible
- Conditional rendering based on selection state
- z-index ensures visibility above content

**Accessibility**:
- Focusable via keyboard navigation (Tab)
- Screen reader announces count and action
- Touch target size meets WCAG 2.1 Level AA (44x44px minimum)

---

## 10. Error Handling Best Practices

**Decision**: Use centralized error handling with i18n mapping

**Error Code Mapping** (in frontend):
```typescript
const errorMessages = {
  TRANSFER_CONFLICT: {
    en: "Cannot transfer: One or more students are already enrolled in the destination class",
    km: "មិនអាចផ្ទេរបាន៖ សិស្សមួយចំនួនត្រូវបានចុះឈ្មោះរួចហើយនៅក្នុងថ្នាក់ទិសដៅ"
  },
  CAPACITY_EXCEEDED: {
    en: "Cannot transfer: Destination class has reached its maximum capacity",
    km: "មិនអាចផ្ទេរបាន៖ ថ្នាក់ទិសដៅបានឈានដល់សមត្ថភាពអតិបរមា"
  },
  UNDO_CONFLICT: {
    en: "Cannot undo: One or more students have been transferred to another class",
    km: "មិនអាចបដិសេធបាន៖ សិស្សមួយចំនួនត្រូវបានផ្ទេរទៅថ្នាក់ផ្សេងទៀត"
  },
  UNDO_EXPIRED: {
    en: "Cannot undo: The 5-minute undo window has expired",
    km: "មិនអាចបដិសេធបាន៖ បង្អួចបដិសេធ ៥ នាទីបានផុតកំណត់"
  }
};
```

**Rationale**:
- Backend returns only error codes (per Constitution)
- Frontend handles all translation
- Consistent error presentation across features
- Easy to add new languages

---

## Summary of Technology Decisions

| Component | Technology | Rationale |
|-----------|------------|-----------|
| Frontend State | Zustand + Session Storage | Lightweight, type-safe, auto-cleanup on tab close |
| Toast Notifications | shadcn/ui Toast (Radix UI) | Project standard, supports actions, accessible |
| DataTable | ClientDataTableWithUrl | < 1000 rows, URL persistence for shareable views |
| API Design | REST with ApiResponse wrapper | Project standard, consistent error handling |
| Cache Management | TanStack Query invalidation | Already used project-wide, automatic refetch |
| Countdown Timer | Custom useCountdown hook | Simple, reusable, accurate 1-second updates |
| Permission Model | Existing RBAC + user ID check | No new infrastructure needed |
| Audit Trail | Existing enrollment_history table | Reuses established pattern |

All research complete. Ready for Phase 1 (data model and contracts).
