# Quick Start: Teacher School Setup

**Feature**: 009-teacher-school-setup
**Branch**: `009-teacher-school-setup`
**Date**: 2025-12-10

## Overview

This guide helps developers set up their local environment to work on the teacher school setup feature. Follow these steps to get up and running quickly.

---

## Prerequisites

Ensure you have the following installed:

- **Java 21+**: `java --version`
- **Maven 3.8+**: `mvn --version` (or use wrapper `./mvnw`)
- **Node.js 18+**: `node --version`
- **pnpm 8+**: `pnpm --version`
- **Docker & Docker Compose**: `docker --version` && `docker-compose --version`
- **PostgreSQL 15+** (via Docker or local)
- **Git**: `git --version`

---

## Step 1: Clone and Checkout Feature Branch

```bash
# If not already cloned
git clone https://github.com/your-org/salarean.git
cd salarean

# Checkout feature branch
git checkout 009-teacher-school-setup

# Verify you're on the correct branch
git branch --show-current
# Expected output: 009-teacher-school-setup
```

---

## Step 2: Backend Setup

### A. Run Database Migrations

**For auth-service**:

```bash
cd auth-service

# Start PostgreSQL (if using Docker)
docker-compose up -d postgres-auth

# Run Flyway migrations
./mvnw flyway:migrate

# Verify migration V5 (teacher_school table) applied
./mvnw flyway:info
# Look for V5__create_teacher_school_table.sql in output
```

**For student-service**:

```bash
cd ../student-service

# Start PostgreSQL (if using Docker)
docker-compose up -d postgres-student

# Run Flyway migrations
./mvnw flyway:migrate

# Verify migrations V12-V14 (provinces, districts, schools FK) applied
./mvnw flyway:info
# Look for:
#   V12__create_provinces_and_districts_tables.sql
#   V13__populate_provinces_and_districts_from_existing_data.sql
#   V14__add_province_and_district_foreign_keys_to_schools.sql
```

### B. Seed Test Data (Optional)

**Provinces and Districts** (run in psql or DBeaver):

```sql
-- Connect to student_db
\c student_db

-- Insert test provinces
INSERT INTO provinces (name, name_km, code) VALUES
('Phnom Penh', 'ភ្នំពេញ', 'PP'),
('Siem Reap', 'សៀមរាប', 'SR'),
('Battambang', 'បាត់ដំបង', 'BB')
ON CONFLICT (name) DO NOTHING;

-- Insert test districts
INSERT INTO districts (province_id, name, name_km, code)
SELECT
    (SELECT id FROM provinces WHERE code = 'PP'),
    'Chamkar Mon', 'ចំការមន', 'CM'
WHERE NOT EXISTS (SELECT 1 FROM districts WHERE name = 'Chamkar Mon');

-- Verify
SELECT COUNT(*) FROM provinces;  -- Should be >= 3
SELECT COUNT(*) FROM districts;  -- Should be >= 1
```

### C. Start Microservices

**Option 1: Using Docker Compose (Recommended)**:

```bash
# From project root
docker-compose up -d

# Verify services are running
docker-compose ps

# Check logs
docker-compose logs -f auth-service student-service
```

**Option 2: Run Locally (for development)**:

```bash
# Terminal 1: Eureka Server
cd eureka-server
./mvnw spring-boot:run

# Terminal 2: API Gateway
cd api-gateway
./mvnw spring-boot:run

# Terminal 3: auth-service
cd auth-service
./mvnw spring-boot:run

# Terminal 4: student-service
cd student-service
./mvnw spring-boot:run
```

**Verify services**:
- Eureka Dashboard: http://localhost:8761
- auth-service Swagger: http://localhost:8082/swagger-ui.html
- student-service Swagger: http://localhost:8081/swagger-ui.html
- API Gateway: http://localhost:8080

---

## Step 3: Frontend Setup

### A. Install Dependencies

```bash
cd frontend

# Install packages
pnpm install

# Verify installation
pnpm list | grep -E "react|tanstack|zod|axios"
```

### B. Configure Environment

Create `.env.local` (if not exists):

```bash
# Frontend environment variables
VITE_API_BASE_URL=http://localhost:8080
```

### C. Start Development Server

```bash
# From frontend directory
pnpm dev

# Application should open at http://localhost:5173
```

**Verify frontend**:
- Navigate to http://localhost:5173
- Open DevTools Console (should see no errors)
- Check Network tab (API calls should go to http://localhost:8080)

---

## Step 4: Verify Integration

### A. Test API Endpoints

**Using cURL**:

```bash
# 1. Register a test teacher (if auth endpoint exists)
curl -X POST http://localhost:8080/auth-service/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teacher@test.com",
    "phoneNumber": "085123456",
    "password": "Test1234!",
    "name": "Test Teacher"
  }'

# 2. Login to get JWT token
curl -X POST http://localhost:8080/auth-service/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "login": "teacher@test.com",
    "password": "Test1234!"
  }'
# Copy the access token from response

# 3. Test provinces endpoint
curl http://localhost:8080/student-service/api/provinces \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Expected: List of provinces in ApiResponse format

# 4. Test districts endpoint
curl "http://localhost:8080/student-service/api/districts?provinceId=PROVINCE_UUID" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Expected: List of districts for province
```

**Using Swagger UI**:
1. Open http://localhost:8081/swagger-ui.html (student-service)
2. Click "Authorize" and enter JWT token
3. Try GET /api/provinces, GET /api/districts, GET /api/schools
4. Verify responses match specification

### B. Test School Setup Flow (Frontend)

1. Navigate to http://localhost:5173
2. Register a new teacher account (if registration flow exists)
3. After registration, verify redirect to `/school-setup`
4. Test province dropdown → district dropdown → schools table
5. Click "Add New School" and submit form
6. Verify new school appears in table
7. Select school and complete association
8. Verify redirect to main application

---

## Step 5: Run Tests

### Backend Tests

**auth-service**:

```bash
cd auth-service

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=TeacherSchoolServiceTest

# Run integration tests only
./mvnw test -Dtest=*IntegrationTest
```

**student-service**:

```bash
cd student-service

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=SchoolServiceTest
```

### Frontend Tests

```bash
cd frontend

# Run all tests
pnpm test

# Run tests in watch mode
pnpm test:watch

# Run tests with coverage
pnpm test:coverage
```

---

## Troubleshooting

### Issue: Flyway Migration Fails

**Error**: `Migration V12 failed`

**Solution**:
```bash
# Repair Flyway metadata
./mvnw flyway:repair

# Re-run migration
./mvnw flyway:migrate

# If still failing, check PostgreSQL logs
docker logs postgres-student
```

---

### Issue: JWT Token Expired

**Error**: `401 UNAUTHORIZED`

**Solution**:
- JWT tokens expire after 24 hours (access token)
- Re-login via `/api/auth/login` endpoint to get fresh token
- For development, consider increasing token expiry in `application.yml`

---

### Issue: CORS Error in Browser

**Error**: `Access to XMLHttpRequest blocked by CORS policy`

**Solution**:
- Verify API Gateway is running on port 8080
- Check `CorsConfig.java` in API Gateway includes `http://localhost:5173`
- Clear browser cache and reload

---

### Issue: Province/District Dropdown Empty

**Error**: Empty dropdowns in school setup page

**Solution**:
```bash
# Verify provinces exist in database
docker exec -it postgres-student psql -U sms_user -d student_db -c "SELECT COUNT(*) FROM provinces;"

# If 0, run seed script (see Step 2B)

# Check API response
curl http://localhost:8080/student-service/api/provinces \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### Issue: Cannot Create School (Duplicate Name)

**Error**: `409 DUPLICATE_SCHOOL_NAME`

**Solution**:
- School names must be unique within a district
- Check existing schools: `SELECT name FROM schools WHERE district_id = 'YOUR_DISTRICT_UUID'`
- Use a different school name or select existing school

---

### Issue: Docker Containers Won't Start

**Error**: `port already allocated` or `container name conflict`

**Solution**:
```bash
# Stop all containers
docker-compose down

# Remove orphaned containers
docker-compose down --remove-orphans

# Restart
docker-compose up -d

# If port conflict persists, check for other processes
lsof -i :8080  # Check what's using port 8080
kill -9 PID    # Kill the process
```

---

## Development Workflow

### Making Changes

1. **Backend Changes**:
   ```bash
   # Edit Java files in auth-service or student-service
   # Tests auto-recompile in IDE (IntelliJ/Eclipse)

   # Or use Maven
   ./mvnw compile

   # Restart service
   # Docker: docker-compose restart auth-service
   # Local: Stop and re-run ./mvnw spring-boot:run
   ```

2. **Frontend Changes**:
   ```bash
   # Edit files in frontend/src/
   # Vite hot-reloads automatically
   # Check browser for changes

   # If issues, restart dev server
   # Ctrl+C, then pnpm dev
   ```

3. **Database Changes**:
   ```bash
   # Create new migration file
   # auth-service/src/main/resources/db/migration/V6__description.sql

   # Run migration
   ./mvnw flyway:migrate

   # Verify
   ./mvnw flyway:info
   ```

### Testing Changes

1. **Unit Tests**: Run after each code change
   ```bash
   ./mvnw test  # Backend
   pnpm test    # Frontend
   ```

2. **Integration Tests**: Run before commit
   ```bash
   ./mvnw verify
   ```

3. **Manual Testing**: Test complete flow in browser

---

## Useful Commands

### Docker

```bash
# View logs
docker-compose logs -f SERVICE_NAME

# Restart service
docker-compose restart SERVICE_NAME

# Rebuild and restart
docker-compose up -d --build SERVICE_NAME

# Access database
docker exec -it postgres-student psql -U sms_user -d student_db
```

### Database

```bash
# Connect to auth_db
docker exec -it postgres-auth psql -U sms_user -d auth_db

# Connect to student_db
docker exec -it postgres-student psql -U sms_user -d student_db

# Common queries
\dt                                 # List tables
\d table_name                       # Describe table
SELECT * FROM teacher_school;       # View associations
SELECT * FROM provinces;            # View provinces
SELECT * FROM districts WHERE province_id = 'UUID';  # View districts
```

### Maven

```bash
# Clean and compile
./mvnw clean compile

# Run specific test
./mvnw test -Dtest=ClassName#methodName

# Skip tests
./mvnw install -DskipTests

# View dependency tree
./mvnw dependency:tree
```

---

## Next Steps

After completing setup:

1. **Read Documentation**:
   - [spec.md](./spec.md) - Feature requirements
   - [plan.md](./plan.md) - Implementation plan
   - [data-model.md](./data-model.md) - Database schema
   - [contracts/endpoints.md](./contracts/endpoints.md) - API specifications

2. **Review Code Structure**:
   - auth-service: `TeacherSchoolController`, `TeacherSchoolService`, `TeacherSchool` entity
   - student-service: `ProvinceController`, `DistrictController`, `SchoolController`
   - frontend: `features/school-setup/` components

3. **Start Implementation**:
   - Follow tasks.md (generated by `/speckit.tasks`)
   - Create feature branch for each task
   - Write tests first (TDD)
   - Submit PRs for review

---

## Resources

- **Project README**: `/README.md`
- **Constitution**: `/.specify/memory/constitution.md`
- **Standards**: `/SERVICE_COMPARISON_ANALYSIS.md`
- **Docker Compose**: `/docker-compose.yml`
- **API Gateway**: `/api-gateway/`

---

## Support

- **Slack**: #sms-development channel
- **Wiki**: https://wiki.sms.edu.kh
- **Issues**: https://github.com/your-org/salarean/issues

---

**Last Updated**: 2025-12-10
**Maintainer**: SMS Development Team
