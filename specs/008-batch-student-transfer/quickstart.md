# Developer Quickstart: Batch Student Transfer with Undo

**Date**: 2025-12-04
**Feature**: 008-batch-student-transfer

## Overview

This guide helps developers quickly set up and test the batch student transfer feature with undo functionality. Follow these steps to get the feature running in your local environment.

---

## Prerequisites

Ensure you have completed the main project setup:

```bash
# Verify Docker is running
docker ps

# Verify services are up
docker-compose ps

# You should see:
# - postgres-student (PostgreSQL for student-service)
# - redis (Redis for caching)
# - eureka-server (Service discovery)
# - api-gateway (API routing)
# - student-service (Backend API)
# - frontend (React dev server)
```

If services are not running, start them:
```bash
docker-compose up -d
```

---

## Step 1: Database Migration

The feature requires new columns in the `enrollment_history` table.

### Run Flyway Migration

**Option A: Via Maven (if student-service is not in Docker)**
```bash
cd student-service
./mvnw flyway:migrate
```

**Option B: Via Docker Exec (if student-service is running in Docker)**
```bash
docker-compose exec student-service ./mvnw flyway:migrate
```

**Option C: Restart student-service (auto-migration on startup)**
```bash
docker-compose restart student-service
docker-compose logs -f student-service  # Watch for migration success
```

### Verify Migration

Connect to PostgreSQL and check the schema:
```bash
docker-compose exec postgres-student psql -U sms_user -d student_db
```

```sql
-- Check new columns exist
\d enrollment_history

-- You should see:
-- transfer_id           | uuid
-- undo_of_transfer_id   | uuid
-- performed_by_user_id  | uuid (NOT NULL)

-- Check indexes
\di idx_enrollment_history_transfer_id
\di idx_enrollment_history_undo_of_transfer_id
\di idx_enrollment_history_performed_at

\q  -- Exit psql
```

---

## Step 2: Backend Service Setup

### Add New Service Classes

The feature adds a new service interface and implementation:

**File Structure**:
```
student-service/src/main/java/com/sms/student/
├── controller/
│   └── ClassStudentController.java       # NEW: Transfer endpoints
├── dto/
│   ├── BatchTransferRequest.java          # NEW
│   ├── BatchTransferResponse.java         # NEW
│   └── UndoTransferResponse.java          # NEW
├── service/
│   ├── interfaces/
│   │   └── IStudentTransferService.java   # NEW: Service interface
│   └── StudentTransferService.java        # NEW: Implementation
└── model/
    └── EnrollmentHistory.java             # UPDATED: New columns
```

### Verify Backend is Running

Check the student-service logs:
```bash
docker-compose logs -f student-service
```

You should see:
```
Started StudentServiceApplication in X.XXX seconds
Tomcat started on port(s): 8082
```

### Test Health Endpoint

```bash
curl http://localhost:8082/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

---

## Step 3: Frontend Setup

### Install Dependencies (if needed)

```bash
cd frontend
pnpm install
```

### Add New Frontend Files

**File Structure**:
```
frontend/src/
├── features/classes/
│   ├── components/
│   │   └── batch-transfer-dialog.tsx     # NEW: Transfer confirmation dialog
│   └── index.tsx                          # UPDATED: Add checkboxes
├── store/
│   └── undo-store.ts                      # NEW: Undo state management
├── services/
│   └── class.ts                           # UPDATED: Add transfer/undo API calls
├── types/
│   └── transfer.ts                        # NEW: Transfer types
└── lib/i18n/locales/
    ├── en.json                            # UPDATED: Add transfer i18n keys
    └── km.json                            # UPDATED: Add Khmer translations
```

### Start Frontend Dev Server

```bash
cd frontend
pnpm dev
```

Frontend should be available at: http://localhost:5173

---

## Step 4: Seed Test Data

To test the batch transfer feature, you need:
- At least 2 active classes with the same grade level
- Multiple students enrolled in one class

### Seed Data SQL Script

Create a file: `student-service/src/main/resources/db/test-data/seed-batch-transfer-test.sql`

```sql
-- Create two Grade 7 classes
INSERT INTO classes (id, name, code, grade_level, capacity, status, teacher_id, academic_year)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'Class 7A', '7A-2025', 7, 40, 'ACTIVE', '00000000-0000-0000-0000-000000000001', 2025),
  ('22222222-2222-2222-2222-222222222222', 'Class 7B', '7B-2025', 7, 40, 'ACTIVE', '00000000-0000-0000-0000-000000000002', 2025);

-- Create 5 test students
INSERT INTO students (id, student_code, first_name, last_name, date_of_birth, gender)
VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'STU001', 'Sok', 'Pisey', '2012-05-15', 'MALE'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'STU002', 'Chan', 'Sokha', '2012-08-20', 'FEMALE'),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'STU003', 'Heng', 'Dara', '2012-03-10', 'MALE'),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'STU004', 'Mao', 'Sreylin', '2012-11-25', 'FEMALE'),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'STU005', 'Lim', 'Veasna', '2012-07-18', 'MALE');

-- Enroll all 5 students in Class 7A
INSERT INTO student_class_enrollments (id, student_id, class_id, status, enrolled_at)
VALUES
  (gen_random_uuid(), 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'ACTIVE', CURRENT_TIMESTAMP),
  (gen_random_uuid(), 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111', 'ACTIVE', CURRENT_TIMESTAMP),
  (gen_random_uuid(), 'cccccccc-cccc-cccc-cccc-cccccccccccc', '11111111-1111-1111-1111-111111111111', 'ACTIVE', CURRENT_TIMESTAMP),
  (gen_random_uuid(), 'dddddddd-dddd-dddd-dddd-dddddddddddd', '11111111-1111-1111-1111-111111111111', 'ACTIVE', CURRENT_TIMESTAMP),
  (gen_random_uuid(), 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '11111111-1111-1111-1111-111111111111', 'ACTIVE', CURRENT_TIMESTAMP);

-- Create enrollment history records
INSERT INTO enrollment_history (id, student_id, class_id, action, performed_at, performed_by_user_id)
SELECT gen_random_uuid(), student_id, class_id, 'ENROLLED', enrolled_at, '00000000-0000-0000-0000-000000000099'
FROM student_class_enrollments
WHERE class_id = '11111111-1111-1111-1111-111111111111';
```

### Run Seed Script

```bash
docker-compose exec postgres-student psql -U sms_user -d student_db -f /path/to/seed-batch-transfer-test.sql
```

---

## Step 5: Manual Testing

### Test Batch Transfer

**1. Login to the Frontend**
- Navigate to http://localhost:5173
- Login as a teacher/admin with transfer permissions

**2. Navigate to Class Detail**
- Go to Class List page
- Click "View" on "Class 7A"
- You should see 5 students enrolled

**3. Select Students for Transfer**
- Check the checkboxes next to 3 students
- A floating action button should appear at bottom-right: "Transfer 3 Students"

**4. Execute Transfer**
- Click the floating action button
- A dialog should open showing the selected students
- Select "Class 7B" from the destination dropdown
- Click "Confirm Transfer"
- Wait for the loading indicator

**5. Verify Transfer Success**
- Success toast notification appears with undo button
- Countdown timer shows "4:59" remaining
- Student list in Class 7A now shows 2 students (3 removed)
- Navigate to Class 7B → should show 3 new students

### Test Undo Transfer

**1. Click Undo Button (within 5 minutes)**
- Click the "Undo" button in the toast notification
- Wait for the loading indicator

**2. Verify Undo Success**
- Toast notification shows "Transfer undone: 3 students returned to Class 7A"
- Student list in Class 7A now shows 5 students again (3 restored)
- Navigate to Class 7B → should show 0 students

### Test Undo Expiration

**1. Execute another transfer**
- Select students and transfer to Class 7B

**2. Wait 5 minutes**
- Observe the countdown timer decrease
- At 0:00, the toast should auto-dismiss

**3. Try to undo (should fail)**
- The undo button should no longer be visible
- No way to undo after 5 minutes (expected behavior)

### Test Undo Conflict

**1. Execute a transfer**
- Transfer 2 students from Class 7A to Class 7B
- Note the undo toast appears

**2. Immediately transfer one of those students again**
- In Class 7B, select 1 of the just-transferred students
- Transfer that student to a different class (create Class 7C if needed)

**3. Try to undo the original transfer**
- Click the undo button from step 1
- Should see error: "Cannot undo: One or more students have been transferred to another class"

---

## Step 6: API Testing with cURL

### Test Batch Transfer API

```bash
# Get JWT token first (use your auth endpoint)
TOKEN="your-jwt-token"

# Batch transfer students
curl -X POST http://localhost:8080/api/classes/11111111-1111-1111-1111-111111111111/students/batch-transfer \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "destinationClassId": "22222222-2222-2222-2222-222222222222",
    "studentIds": [
      "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
      "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
      "cccccccc-cccc-cccc-cccc-cccccccccccc"
    ]
  }'
```

Expected response:
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "transferId": "some-uuid-here",
    "sourceClassId": "11111111-1111-1111-1111-111111111111",
    "destinationClassId": "22222222-2222-2222-2222-222222222222",
    "successfulTransfers": 3,
    "failedTransfers": [],
    "transferredAt": "2025-12-04T10:30:00.000Z"
  }
}
```

### Test Undo API

```bash
# Use the transferId from the previous response
TRANSFER_ID="some-uuid-here"

curl -X POST http://localhost:8080/api/transfers/$TRANSFER_ID/undo \
  -H "Authorization: Bearer $TOKEN"
```

Expected response:
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "transferId": "some-uuid-here",
    "undoneStudents": 3,
    "sourceClassId": "11111111-1111-1111-1111-111111111111",
    "undoneAt": "2025-12-04T10:35:00.000Z"
  }
}
```

### Test Get Eligible Destinations API

```bash
curl -X GET http://localhost:8080/api/classes/11111111-1111-1111-1111-111111111111/eligible-destination-classes \
  -H "Authorization: Bearer $TOKEN"
```

Expected response:
```json
{
  "errorCode": "SUCCESS",
  "data": [
    {
      "id": "22222222-2222-2222-2222-222222222222",
      "name": "Class 7B",
      "code": "7B-2025",
      "gradeLevel": 7,
      "capacity": 40,
      "currentEnrollment": 0,
      "teacherName": "Teacher Name"
    }
  ]
}
```

---

## Step 7: Verify Enrollment History

After testing transfers and undos, check the audit trail:

```bash
docker-compose exec postgres-student psql -U sms_user -d student_db
```

```sql
-- View all enrollment history for the test students
SELECT
  eh.performed_at,
  eh.action,
  s.first_name || ' ' || s.last_name as student_name,
  c.name as class_name,
  eh.transfer_id,
  eh.undo_of_transfer_id
FROM enrollment_history eh
JOIN students s ON s.id = eh.student_id
JOIN classes c ON c.id = eh.class_id
WHERE eh.student_id IN (
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
  'cccccccc-cccc-cccc-cccc-cccccccccccc'
)
ORDER BY eh.performed_at DESC;
```

You should see records like:
```
 performed_at        | action      | student_name | class_name | transfer_id  | undo_of_transfer_id
---------------------+-------------+--------------+------------+--------------+--------------------
 2025-12-04 10:35:00 | UNDO        | Sok Pisey    | Class 7A   | null         | abc123...
 2025-12-04 10:30:00 | TRANSFERRED | Sok Pisey    | Class 7B   | abc123...    | null
 2025-12-04 10:30:00 | TRANSFERRED | Sok Pisey    | Class 7A   | abc123...    | null
```

---

## Step 8: Run Automated Tests

### Backend Unit Tests

```bash
cd student-service
./mvnw test -Dtest=StudentTransferServiceTest
```

### Backend Integration Tests

```bash
./mvnw verify -Dtest=ClassStudentControllerIntegrationTest
```

### Frontend Component Tests

```bash
cd frontend
pnpm test batch-transfer-dialog.test.tsx
```

---

## Troubleshooting

### Issue: "Transfer ID not found" when undoing

**Cause**: Transfer record may not exist or session storage was cleared.

**Solution**:
1. Check session storage in browser DevTools (Application → Session Storage)
2. Verify `batch-transfer-undo` key exists with valid transfer data
3. Ensure you're testing in the same browser tab (undo state is session-based)

### Issue: "Cannot undo: Students have been transferred again"

**Cause**: One or more students were enrolled in a different class after the transfer.

**Solution**:
- This is expected behavior (per spec)
- Undo is blocked to prevent data conflicts
- To test undo, don't transfer students between the initial transfer and undo

### Issue: "Capacity exceeded" error

**Cause**: Destination class doesn't have enough capacity.

**Solution**:
```sql
-- Increase class capacity
UPDATE classes
SET capacity = 100
WHERE id = '22222222-2222-2222-2222-222222222222';
```

### Issue: "Grade mismatch" error

**Cause**: Source and destination classes have different grade levels.

**Solution**:
```sql
-- Verify grade levels match
SELECT id, name, grade_level FROM classes WHERE id IN (
  '11111111-1111-1111-1111-111111111111',
  '22222222-2222-2222-2222-222222222222'
);

-- Update destination class grade if needed
UPDATE classes
SET grade_level = 7
WHERE id = '22222222-2222-2222-2222-222222222222';
```

### Issue: Toast notification doesn't show countdown

**Cause**: Undo state not properly stored or countdown hook not triggered.

**Solution**:
1. Open browser console and check for React errors
2. Verify `useCountdown` hook is called with correct `expiresAt` timestamp
3. Check that `expiresAt` is a future timestamp (current time + 5 minutes)

---

## Quick Reference

**Key URLs**:
- Frontend: http://localhost:5173
- API Gateway: http://localhost:8080
- Student Service (direct): http://localhost:8082
- Swagger UI: http://localhost:8082/swagger-ui.html
- Eureka Dashboard: http://localhost:8761

**Database Connection**:
```bash
docker-compose exec postgres-student psql -U sms_user -d student_db
```

**Logs**:
```bash
docker-compose logs -f student-service  # Backend logs
docker-compose logs -f frontend         # Frontend logs
docker-compose logs -f api-gateway      # Gateway logs
```

**Rebuild Services**:
```bash
docker-compose build student-service    # Rebuild backend
docker-compose up -d student-service    # Restart
```

---

## Next Steps

Once the feature is working locally:
1. Run the full test suite: `./mvnw verify`
2. Update API documentation: Check Swagger UI reflects new endpoints
3. Verify i18n translations: Test in Khmer (km) language
4. Test on mobile: Verify touch interactions work on tablet/phone
5. Performance test: Try transferring 50+ students at once

For detailed implementation guidance, see:
- `data-model.md` - Database schema and entities
- `contracts/api-endpoints.md` - API specifications
- `research.md` - Technical decisions and rationale

Happy coding! 🚀
