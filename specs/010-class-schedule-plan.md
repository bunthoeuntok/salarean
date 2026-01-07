# Weekly Class Timetable/Schedule Module - Implementation Plan

**Status**: ✅ COMPLETED | **Date**: 2026-01-07

## Overview

Build a weekly class timetable system for Khmer academic scheduling, allowing teachers to configure time slots and assign subjects to each period.

## Requirements Summary

| Requirement | Description |
|-------------|-------------|
| **Type** | Weekly Class Timetable (Mon-Sat) |
| **Scope** | Per Class - each class has its own unique timetable |
| **Shift** | Add `shift` column to classes (MORNING, AFTERNOON, FULLDAY) |
| **Periods** | Configurable number per class |
| **Time Slots** | Template-based with customization option |

## Architecture Decision

**Location**: student-service (where classes already exist)

- Classes entity at: `student-service/src/main/java/com/sms/student/model/SchoolClass.java`
- Subject reference: Store `subject_id` UUID, frontend fetches details from grade-service

---

## Database Schema

### V17: Add shift to classes

```sql
ALTER TABLE classes ADD COLUMN shift VARCHAR(20) NOT NULL DEFAULT 'MORNING';
ALTER TABLE classes ADD CONSTRAINT classes_shift_check
    CHECK (shift IN ('MORNING', 'AFTERNOON', 'FULLDAY'));
```

### V18: Time Slot Templates

```sql
CREATE TABLE time_slot_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID,  -- NULL for system defaults
    name VARCHAR(100) NOT NULL,
    name_km VARCHAR(100) NOT NULL,
    shift VARCHAR(20) NOT NULL,
    slots JSONB NOT NULL,  -- [{periodNumber, startTime, endTime, label, labelKm, isBreak}]
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Default templates inserted for MORNING, AFTERNOON, FULLDAY shifts
```

### V19: Class Schedules & Entries

```sql
CREATE TABLE class_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    time_slot_template_id UUID REFERENCES time_slot_templates(id),
    custom_slots JSONB,  -- Override template if customized
    academic_year VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (class_id, academic_year)
);

CREATE TABLE schedule_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_schedule_id UUID NOT NULL REFERENCES class_schedules(id) ON DELETE CASCADE,
    day_of_week INTEGER NOT NULL CHECK (day_of_week BETWEEN 1 AND 6),
    period_number INTEGER NOT NULL,
    subject_id UUID NOT NULL,
    room VARCHAR(50),
    notes VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (class_schedule_id, day_of_week, period_number)
);
```

---

## Default Time Slot Templates

### Morning Shift (5 periods)

| Period | Time | Label (EN) | Label (KM) |
|--------|------|------------|------------|
| 1 | 07:00-07:45 | Period 1 | មុខវិជ្ជាទី១ |
| 2 | 07:45-08:30 | Period 2 | មុខវិជ្ជាទី២ |
| 3 | 08:30-09:15 | Period 3 | មុខវិជ្ជាទី៣ |
| - | 09:15-09:30 | Break | សម្រាក |
| 4 | 09:30-10:15 | Period 4 | មុខវិជ្ជាទី៤ |
| 5 | 10:15-11:00 | Period 5 | មុខវិជ្ជាទី៥ |

### Afternoon Shift (5 periods)

| Period | Time | Label (EN) | Label (KM) |
|--------|------|------------|------------|
| 1 | 13:00-13:45 | Period 1 | មុខវិជ្ជាទី១ |
| 2 | 13:45-14:30 | Period 2 | មុខវិជ្ជាទី២ |
| 3 | 14:30-15:15 | Period 3 | មុខវិជ្ជាទី៣ |
| - | 15:15-15:30 | Break | សម្រាក |
| 4 | 15:30-16:15 | Period 4 | មុខវិជ្ជាទី៤ |
| 5 | 16:15-17:00 | Period 5 | មុខវិជ្ជាទី៥ |

### Full Day Shift (10 periods)

Morning (5 periods) + Lunch Break (11:00-13:00) + Afternoon (5 periods)

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/schedules/templates` | Get available templates |
| GET | `/api/schedules/templates/shift/{shift}` | Get templates by shift |
| GET | `/api/schedules/templates/{id}` | Get template by ID |
| POST | `/api/schedules/templates` | Create custom template |
| DELETE | `/api/schedules/templates/{id}` | Delete custom template |
| GET | `/api/schedules/class/{classId}` | Get class schedule |
| POST | `/api/schedules/class` | Create schedule |
| PUT | `/api/schedules/class/{classId}/entries` | Update schedule entries |
| DELETE | `/api/schedules/class/{classId}` | Delete schedule |
| POST | `/api/schedules/class/{classId}/clear` | Clear all entries |
| POST | `/api/schedules/class/{targetId}/copy-from/{sourceId}` | Copy schedule from another class |

---

## Backend Implementation

### Files Created

#### Enums
- `student-service/src/main/java/com/sms/student/enums/ClassShift.java`

#### Models
- `student-service/src/main/java/com/sms/student/model/TimeSlot.java`
- `student-service/src/main/java/com/sms/student/model/TimeSlotTemplate.java`
- `student-service/src/main/java/com/sms/student/model/ClassSchedule.java`
- `student-service/src/main/java/com/sms/student/model/ScheduleEntry.java`

#### Modified Models
- `student-service/src/main/java/com/sms/student/model/SchoolClass.java` - Added shift field

#### DTOs
- `ScheduleErrorCode.java` - Error codes for schedule operations
- `TimeSlotDto.java` - Time slot data transfer object
- `TimeSlotTemplateResponse.java` - Template response DTO
- `CreateTimeSlotTemplateRequest.java` - Create template request
- `ScheduleEntryDto.java` - Schedule entry DTO
- `ScheduleEntryResponse.java` - Entry response DTO
- `ClassScheduleResponse.java` - Full schedule response
- `CreateClassScheduleRequest.java` - Create schedule request
- `UpdateScheduleEntriesRequest.java` - Update entries request

#### Repositories
- `TimeSlotTemplateRepository.java`
- `ClassScheduleRepository.java`
- `ScheduleEntryRepository.java`

#### Services
- `student-service/src/main/java/com/sms/student/service/interfaces/IScheduleService.java`
- `student-service/src/main/java/com/sms/student/service/ScheduleService.java`

#### Controllers
- `student-service/src/main/java/com/sms/student/controller/ScheduleController.java`

#### Configuration
- `api-gateway/src/main/resources/application.yml` - Added `/api/schedules/**` route

---

## Frontend Implementation

### Files Created

#### Types
- `frontend/src/types/schedule.types.ts` - Schedule type definitions

#### Modified Types
- `frontend/src/types/class.types.ts` - Added ClassShift type and shift field

#### Services
- `frontend/src/services/schedule.service.ts` - API service for schedules

#### Hooks
- `frontend/src/hooks/use-schedule.ts` - TanStack Query hooks

#### Components
- `frontend/src/features/classes/class-detail/components/schedule-tab.tsx` - Main schedule tab
- `frontend/src/features/classes/class-detail/components/schedule-grid.tsx` - Weekly grid display
- `frontend/src/features/classes/class-detail/components/create-schedule-dialog.tsx` - Template selection
- `frontend/src/features/classes/class-detail/components/copy-schedule-dialog.tsx` - Copy from another class
- `frontend/src/features/classes/class-detail/components/edit-entry-dialog.tsx` - Add/edit subject

#### Modified Components
- `frontend/src/features/classes/class-detail/index.tsx` - Integrated ScheduleTab

---

## UI Features

### Schedule Grid
- **Weekly view**: Monday to Saturday columns
- **Time-ordered rows**: All time slots (periods + breaks) sorted by start time
- **Break rows**: Displayed inline with amber styling, spanning all columns
- **Period cells**: Clickable cells to add/edit subjects
- **Subject display**: Shows subject name and optional room number

### Dialogs
- **Create Schedule**: Select from available time slot templates based on class shift
- **Copy Schedule**: Copy schedule from another class
- **Edit Entry**: Add/edit subject assignment with room and notes

### Actions
- **Refresh**: Reload schedule data
- **Clear All**: Remove all subject assignments (keeps time slots)
- **Delete Schedule**: Remove entire schedule

---

## Implementation Phases (Completed)

### Phase 1: Database & Backend Foundation ✅
1. Migration V17: Add shift to classes
2. Migration V18: Time slot templates with defaults
3. Migration V19: Class schedules and entries
4. Create ClassShift enum
5. Create entities with JSONB support (hypersistence-utils)
6. Update SchoolClass with shift field
7. Create repositories

### Phase 2: Backend Business Logic ✅
8. Create DTOs
9. Create IScheduleService interface
10. Implement ScheduleService
11. Create ScheduleController
12. Update API Gateway routes
13. Update class DTOs with shift field

### Phase 3: Frontend Foundation ✅
14. Create schedule.types.ts
15. Update class.types.ts
16. Create schedule.service.ts
17. Create use-schedule.ts hook

### Phase 4: Frontend UI ✅
18. Create ScheduleTab component
19. Create ScheduleGrid component
20. Create CreateScheduleDialog
21. Create CopyScheduleDialog
22. Create EditEntryDialog
23. Update class-detail to use ScheduleTab
24. Add alert-dialog component

### Phase 5: Testing & Verification ✅
25. Backend compilation verified
26. Frontend build verified
27. TypeScript errors fixed

---

## Dependencies Added

### Backend (student-service)
```xml
<dependency>
    <groupId>io.hypersistence</groupId>
    <artifactId>hypersistence-utils-hibernate-63</artifactId>
    <version>3.9.0</version>
</dependency>
```

### Frontend
- `@radix-ui/react-alert-dialog` (via shadcn/ui alert-dialog component)

---

## Notes

- Schedule data is teacher-scoped (multi-tenancy via teacher_id)
- Subjects are fetched from grade-service and displayed by name
- JSONB used for flexible time slot storage in PostgreSQL
- Bilingual support (English/Khmer) for all labels
