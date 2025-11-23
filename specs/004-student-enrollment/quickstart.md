# Developer Quickstart: Student Class Enrollment Management

**Feature**: 004-student-enrollment
**Service**: student-service
**Date**: 2025-11-23
**Branch**: `004-student-enrollment`

---

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21+** - OpenJDK or Eclipse Temurin
- **Maven 3.9+** - Build tool
- **Docker & Docker Compose** - For PostgreSQL database
- **Git** - Version control
- **cURL or Postman** - For API testing (optional)

**Verify installations**:

```bash
java -version        # Should show Java 21+
mvn -version         # Should show Maven 3.9+
docker --version     # Should show Docker 20.10+
docker-compose --version
```

---

## Project Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd salarean
git checkout 004-student-enrollment
```

### 2. Start Infrastructure Services

Start PostgreSQL database and other required services:

```bash
# Start all services including postgres, eureka-server, api-gateway
docker-compose up -d

# Verify services are running
docker-compose ps

# Expected output:
# postgres-student    Up      5432/tcp
# eureka-server       Up      8761/tcp
# api-gateway         Up      8080/tcp
```

### 3. Configure Database Connection

The student-service uses the default profile for local development.

**Default configuration** (`student-service/src/main/resources/application.yml`):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/student_db
    username: sms_user
    password: sms_password
  jpa:
    hibernate:
      ddl-auto: validate  # Flyway manages schema
    show-sql: true        # Show SQL queries in console
  flyway:
    enabled: true
    baseline-on-migrate: true
```

**Note**: No changes needed for local development. Database credentials match docker-compose configuration.

---

## Running the Service

### Option 1: Using Maven (Recommended for Development)

```bash
# From project root
cd student-service

# Run with default profile (local development)
./mvnw spring-boot:run

# Or if mvnw is not executable
mvn spring-boot:run
```

**Expected output**:

```
...
Started StudentServiceApplication in 5.234 seconds (JVM running for 5.678)
Flyway migration completed: 6 migrations applied
Registered with Eureka Server at http://localhost:8761/eureka/
```

### Option 2: Using Docker (Production-like Environment)

```bash
# Build the service
docker-compose build student-service

# Start student-service with all dependencies
docker-compose up -d student-service

# View logs
docker-compose logs -f student-service
```

---

## Verifying the Setup

### 1. Health Check

```bash
# Direct service health check
curl http://localhost:8082/actuator/health

# Expected response:
# {"status":"UP"}
```

### 2. Swagger UI Access

Open your browser and navigate to:

- **Direct service**: http://localhost:8082/swagger-ui.html
- **Via API Gateway**: http://localhost:8080/api/students/swagger-ui.html

**Note**: Use the API Gateway URL for testing to avoid CORS issues.

### 3. Verify Database Migration

Check that all 6 migrations (including V6 enrollment status) have been applied:

```bash
# Connect to PostgreSQL
docker exec -it postgres-student psql -U sms_user -d student_db

# Check migration history
SELECT version, description, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;

# Expected output includes:
# version | description                      | installed_on
# --------+----------------------------------+--------------
# 1       | create students table            | 2025-11-20...
# 2       | create classes table             | 2025-11-20...
# 3       | create enrollments table         | 2025-11-20...
# ...
# 6       | add enrollment status field      | 2025-11-23...

# Verify status column exists
\d student_class_enrollments

# Exit psql
\q
```

---

## Testing the Enrollment APIs

### Prerequisites: Get JWT Token

All enrollment endpoints require authentication. First, obtain a JWT token from the auth-service:

```bash
# 1. Register or login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phoneOrEmail": "admin@example.com",
    "password": "admin123"
  }'

# Response:
# {
#   "errorCode": "SUCCESS",
#   "data": {
#     "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#     "refreshToken": "...",
#     "expiresIn": 3600
#   }
# }

# Copy the accessToken for subsequent requests
export JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Test Endpoint 1: Get Enrollment History

```bash
# Replace {student-id} with actual UUID
curl -X GET "http://localhost:8080/api/students/{student-id}/enrollment-history" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json"

# Expected response:
# {
#   "errorCode": "SUCCESS",
#   "data": {
#     "enrollments": [...],
#     "totalCount": 2,
#     "activeCount": 1,
#     "completedCount": 1,
#     "transferredCount": 0
#   }
# }
```

### Test Endpoint 2: Enroll Student in Class

```bash
# Replace {student-id} and {class-id} with actual UUIDs
curl -X POST "http://localhost:8080/api/students/{student-id}/enroll" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "classId": "{class-id}",
    "notes": "Regular enrollment for academic year 2024-2025"
  }'

# Expected response (200 OK):
# {
#   "errorCode": "SUCCESS",
#   "data": {
#     "id": "770e8400-e29b-41d4-a716-446655440002",
#     "studentId": "...",
#     "classId": "...",
#     "className": "Grade 5 - Section A",
#     "schoolName": "Phnom Penh Primary School",
#     "enrollmentDate": "2025-11-23",
#     "status": "ACTIVE",
#     "reason": "NEW",
#     ...
#   }
# }
```

### Test Endpoint 3: Transfer Student to New Class

```bash
# Replace {student-id} and {target-class-id} with actual UUIDs
curl -X POST "http://localhost:8080/api/students/{student-id}/transfer" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "targetClassId": "{target-class-id}",
    "reason": "Student requested transfer due to scheduling conflict"
  }'

# Expected response (200 OK):
# {
#   "errorCode": "SUCCESS",
#   "data": {
#     "id": "880e8400-e29b-41d4-a716-446655440004",
#     "studentId": "...",
#     "classId": "{target-class-id}",
#     "className": "Grade 5 - Section B",
#     "enrollmentDate": "2025-11-23",
#     "status": "ACTIVE",
#     "reason": "TRANSFER",
#     ...
#   }
# }
```

---

## Common Issues & Troubleshooting

### Issue 1: Port Already in Use

**Error**: `Port 8082 is already in use`

**Solution**:

```bash
# Find process using port 8082
lsof -i :8082

# Kill the process
kill -9 <PID>

# Or change the port in application.yml
server:
  port: 8083
```

### Issue 2: Database Connection Failed

**Error**: `Connection to localhost:5432 refused`

**Solution**:

```bash
# Ensure PostgreSQL container is running
docker-compose ps | grep postgres-student

# If not running, start it
docker-compose up -d postgres-student

# Check logs for errors
docker-compose logs postgres-student
```

### Issue 3: Flyway Migration Failed

**Error**: `FlywayException: Validate failed: Migration checksum mismatch`

**Solution**:

```bash
# Option 1: Repair Flyway schema history
./mvnw flyway:repair

# Option 2: Clean database and re-run migrations (⚠️ DELETES ALL DATA)
./mvnw flyway:clean flyway:migrate

# Option 3: Drop and recreate database
docker exec -it postgres-student psql -U sms_user -d postgres
DROP DATABASE student_db;
CREATE DATABASE student_db OWNER sms_user;
\q

# Restart service to re-run migrations
./mvnw spring-boot:run
```

### Issue 4: JWT Token Expired

**Error**: `401 Unauthorized` - "Token has expired"

**Solution**:

```bash
# Request new access token using refresh token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<your-refresh-token>"
  }'

# Or login again to get new tokens
```

### Issue 5: Class Capacity Exceeded

**Error**: `409 Conflict` - `CLASS_CAPACITY_EXCEEDED`

**Solution**:

```bash
# Check class capacity
curl -X GET "http://localhost:8080/api/classes/{class-id}" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Response shows:
# "maxCapacity": 30,
# "studentCount": 30

# Either:
# 1. Enroll in a different class with available capacity
# 2. Update class capacity (if authorized)
```

### Issue 6: Student Already Enrolled

**Error**: `409 Conflict` - `DUPLICATE_ENROLLMENT`

**Solution**:

```bash
# Get student's enrollment history to verify
curl -X GET "http://localhost:8080/api/students/{student-id}/enrollment-history" \
  -H "Authorization: Bearer $JWT_TOKEN"

# If student has active enrollment in the same class:
# 1. Transfer to a different class (use /transfer endpoint)
# 2. Or verify you're enrolling in the correct class
```

---

## Development Workflow

### 1. Make Code Changes

```bash
# Edit files in student-service/src/main/java/com/sms/student/
# Example: Update EnrollmentServiceImpl.java
```

### 2. Run Tests

```bash
cd student-service

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=EnrollmentServiceTest

# Run specific test method
./mvnw test -Dtest=EnrollmentServiceTest#testEnrollStudent
```

### 3. Hot Reload (Spring Boot DevTools)

**Option 1**: Use Spring Boot DevTools (if enabled in pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
</dependency>
```

Changes are automatically reloaded when you rebuild the project.

**Option 2**: Manual restart

```bash
# Stop the service (Ctrl+C)
# Restart
./mvnw spring-boot:run
```

### 4. Database Migrations

**Add new migration**:

```bash
# Create new migration file
# File: student-service/src/main/resources/db/migration/V7__description.sql

# Restart service - Flyway auto-applies new migrations
./mvnw spring-boot:run
```

**Verify migration applied**:

```bash
docker exec -it postgres-student psql -U sms_user -d student_db \
  -c "SELECT version, description FROM flyway_schema_history ORDER BY installed_rank;"
```

---

## IDE Setup

### IntelliJ IDEA

1. **Import Project**:
   - File → Open → Select `salarean` directory
   - Wait for Maven import to complete

2. **Run Configuration**:
   - Right-click `StudentServiceApplication.java`
   - Run 'StudentServiceApplication'

3. **Recommended Plugins**:
   - Lombok Plugin (for @Getter, @Setter, @Builder)
   - OpenAPI (Swagger) Editor
   - Docker Integration

### VS Code

1. **Install Extensions**:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Lombok Annotations Support

2. **Run Service**:
   - Open Command Palette (Cmd+Shift+P)
   - "Spring Boot Dashboard" → Start `student-service`

---

## Testing with Postman

### 1. Import API Collection

Create a Postman collection with these requests:

**Environment Variables**:
- `base_url`: http://localhost:8080
- `jwt_token`: (set after login)
- `student_id`: (replace with actual UUID)
- `class_id`: (replace with actual UUID)

### 2. Sample Collection

```json
{
  "info": {
    "name": "Student Enrollment API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Get Enrollment History",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{jwt_token}}"
          }
        ],
        "url": "{{base_url}}/api/students/{{student_id}}/enrollment-history"
      }
    },
    {
      "name": "Enroll Student",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{jwt_token}}"
          },
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"classId\": \"{{class_id}}\",\n  \"notes\": \"Regular enrollment\"\n}"
        },
        "url": "{{base_url}}/api/students/{{student_id}}/enroll"
      }
    },
    {
      "name": "Transfer Student",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{jwt_token}}"
          },
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"targetClassId\": \"{{target_class_id}}\",\n  \"reason\": \"Transfer request\"\n}"
        },
        "url": "{{base_url}}/api/students/{{student_id}}/transfer"
      }
    }
  ]
}
```

---

## Performance Testing

### Load Test Enrollment History Endpoint

**Requirement**: SC-001 - History retrieval must complete in <2 seconds

```bash
# Install Apache Bench (if not already installed)
# macOS: brew install httpd
# Ubuntu: apt-get install apache2-utils

# Test enrollment history endpoint
ab -n 100 -c 10 \
  -H "Authorization: Bearer $JWT_TOKEN" \
  http://localhost:8080/api/students/{student-id}/enrollment-history

# Expected output:
# Time per request: <2000ms (mean)
```

### Test Concurrent Enrollments

**Requirement**: Verify capacity enforcement under concurrent load

```bash
# Create a test script: test-concurrent-enrollment.sh

#!/bin/bash
STUDENT_ID="<student-uuid>"
CLASS_ID="<class-uuid>"
JWT_TOKEN="<token>"

# Enroll 10 students concurrently in a class with capacity 5
for i in {1..10}; do
  curl -X POST "http://localhost:8080/api/students/$STUDENT_ID/enroll" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"classId\": \"$CLASS_ID\", \"notes\": \"Test $i\"}" &
done

wait
echo "All concurrent requests completed"

# Expected: Only 5 succeed, rest fail with CLASS_CAPACITY_EXCEEDED
```

---

## Cleanup

### Stop Services

```bash
# Stop student-service (if running via Maven)
# Press Ctrl+C in terminal

# Stop all Docker containers
docker-compose down

# Stop and remove volumes (⚠️ DELETES ALL DATA)
docker-compose down -v
```

### Reset Database

```bash
# Drop database
docker exec -it postgres-student psql -U sms_user -d postgres \
  -c "DROP DATABASE student_db;"

# Recreate database
docker exec -it postgres-student psql -U sms_user -d postgres \
  -c "CREATE DATABASE student_db OWNER sms_user;"

# Restart service to re-run migrations
./mvnw spring-boot:run
```

---

## Next Steps

1. ✅ Complete Phase 1 documentation review
2. ➡️ Run agent context update: `.specify/scripts/bash/update-agent-context.sh claude`
3. ➡️ Generate implementation tasks: `/speckit.tasks`
4. ➡️ Begin Phase 2 implementation

---

## Support & Resources

**Project Documentation**:
- [Feature Specification](./spec.md)
- [Implementation Plan](./plan.md)
- [Data Model](./data-model.md)
- [API Contracts](./contracts/enrollment-api.md)
- [Research Notes](./research.md)

**External Resources**:
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Project Constitution](../../.specify/constitution.md)

**Troubleshooting**:
- Check service logs: `docker-compose logs -f student-service`
- Verify database state: `docker exec -it postgres-student psql -U sms_user -d student_db`
- Review Swagger UI for API documentation: http://localhost:8082/swagger-ui.html
