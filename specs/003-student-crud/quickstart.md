# Developer Quickstart: Student CRUD Operations

**Feature**: 003-student-crud
**Service**: student-service
**Prerequisites**: Java 21, Docker, Maven

---

## Local Development Setup

### 1. Prerequisites

Ensure you have the following installed:

- **Java 21+** (verify: `java -version`)
- **Maven 3.9+** (verify: `mvn -version`)
- **Docker & Docker Compose** (verify: `docker --version`)
- **PostgreSQL client** (optional, for manual DB access)

### 2. Database Setup

The `student_db` database is created automatically via Docker Compose:

```bash
# Start PostgreSQL container
docker-compose up -d postgres-student

# Verify database is running
docker-compose ps postgres-student
```

**Database Connection Details**:
- Host: `localhost`
- Port: `5433` (mapped to avoid conflict with other services)
- Database: `student_db`
- Username: `student_user`
- Password: `student_pass` (from environment variables)

### 3. Build the Service

```bash
# From repository root
cd student-service

# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package JAR
mvn package -DskipTests
```

### 4. Run Locally (IDE)

**IntelliJ IDEA / VS Code**:
1. Open `student-service` as Maven project
2. Set run configuration:
   - Main class: `com.sms.student.StudentServiceApplication`
   - VM options: `-Dspring.profiles.active=dev`
3. Run the application

**Environment Variables** (for IDE run configuration):
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/student_db
SPRING_DATASOURCE_USERNAME=student_user
SPRING_DATASOURCE_PASSWORD=student_pass
EUREKA_CLIENT_SERVICE_URL=http://localhost:8761/eureka/
```

### 5. Run via Docker

```bash
# Build Docker image
docker-compose build student-service

# Start service
docker-compose up student-service

# View logs
docker logs -f sms-student-service
```

### 6. Verify Service is Running

**Health Check**:
```bash
curl http://localhost:8082/actuator/health
```

**Expected Response**:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

**API Documentation**:
- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI JSON: http://localhost:8082/v3/api-docs

---

## Development Workflow

### Creating a New Student

```bash
# Via API Gateway (port 8080)
curl -X POST http://localhost:8080/api/students \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Sok",
    "lastName": "Sara",
    "firstNameKhmer": "សុខ",
    "lastNameKhmer": "សារ៉ា",
    "dateOfBirth": "2010-03-15",
    "gender": "F",
    "classId": "UUID_OF_CLASS",
    "enrollmentDate": "2024-09-01",
    "parentContacts": [
      {
        "fullName": "Mrs. Sok Channary",
        "phoneNumber": "+85512345678",
        "relationship": "MOTHER",
        "isPrimary": true
      }
    ]
  }'
```

### Listing Students

```bash
# Get all active students (paginated)
curl http://localhost:8080/api/students?page=0&size=20 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Filter by class
curl "http://localhost:8080/api/students?classId=UUID_OF_CLASS" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Uploading Photo

```bash
curl -X POST http://localhost:8080/api/students/STUDENT_ID/photo \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/student-photo.jpg"
```

---

## Testing

### Unit Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=StudentServiceTest

# Run with coverage
mvn test jacoco:report
```

### Integration Tests (with Testcontainers)

Integration tests automatically spin up PostgreSQL containers:

```bash
mvn verify
```

**Note**: First run downloads Docker images, subsequent runs are faster.

### Manual Testing via Swagger UI

1. Navigate to http://localhost:8082/swagger-ui.html
2. Click "Authorize" and enter JWT token
3. Expand endpoint and click "Try it out"
4. Fill request body and execute

---

## Database Migrations

Flyway automatically applies migrations on startup. Migration scripts located in:
```
src/main/resources/db/migration/
├── V1__create_students_table.sql
├── V2__create_parent_contacts_table.sql
├── V3__create_enrollments_table.sql
└── V4__create_views_and_functions.sql
```

### Manual Migration

```bash
# Apply migrations
mvn flyway:migrate

# View migration status
mvn flyway:info

# Repair failed migrations (use with caution)
mvn flyway:repair
```

### Access Database Directly

```bash
# Via Docker
docker exec -it sms-postgres-student psql -U student_user -d student_db

# Useful queries
SELECT * FROM students WHERE status = 'ACTIVE';
SELECT * FROM v_current_class_roster WHERE class_id = 'UUID';
SELECT get_student_current_class('STUDENT_UUID');
```

---

## Troubleshooting

### Port Already in Use

If port 8082 is occupied:

```yaml
# docker-compose.yml (change student-service port mapping)
ports:
  - "8083:8082"  # External:Internal
```

Or set environment variable:
```bash
export SERVER_PORT=8083
mvn spring-boot:run
```

### Database Connection Issues

```bash
# Check if PostgreSQL is running
docker-compose ps postgres-student

# View database logs
docker logs sms-postgres-student

# Restart database
docker-compose restart postgres-student
```

### Eureka Registration Fails

Ensure Eureka server is running:
```bash
docker-compose up -d eureka-server
curl http://localhost:8761
```

To disable Eureka (local dev):
```yaml
# application-dev.yml
eureka:
  client:
    enabled: false
```

### Photo Upload Fails

Check upload directory permissions:
```bash
mkdir -p uploads/students
chmod 755 uploads/students
```

View upload directory:
```bash
ls -lah uploads/students/
```

---

## Hot Reload (Development Mode)

### Using Spring Boot DevTools

Already included in `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
</dependency>
```

**IntelliJ IDEA**:
1. Enable "Build project automatically" in Settings → Build → Compiler
2. Enable "Allow auto-make to start even if developed application is currently running"
3. Save file → Service restarts automatically

**VS Code**:
1. Install "Spring Boot Extension Pack"
2. Changes trigger automatic rebuild

---

## Configuration Reference

### application.yml (key properties)

```yaml
server:
  port: 8082

spring:
  application:
    name: student-service
  datasource:
    url: jdbc:postgresql://postgres-student:5432/student_db
    username: ${DB_USERNAME:student_user}
    password: ${DB_PASSWORD:student_pass}
  jpa:
    hibernate:
      ddl-auto: validate  # Flyway handles schema
    show-sql: false  # Set to true for SQL logging

app:
  upload:
    dir: uploads/students  # Photo storage directory
    max-file-size: 5MB

logging:
  level:
    com.sms.student: DEBUG
```

---

## Next Steps

1. **Implement Feature**: Follow tasks in `tasks.md` (generate via `/speckit.tasks`)
2. **Test Locally**: Use Swagger UI for manual testing
3. **Write Tests**: Add unit/integration tests as you implement
4. **Deploy**: Build Docker image and deploy via docker-compose

---

## Useful Commands Cheat Sheet

```bash
# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run

# Test
mvn test

# Docker
docker-compose up -d student-service
docker logs -f sms-student-service
docker exec -it sms-postgres-student psql -U student_user -d student_db

# Health check
curl http://localhost:8082/actuator/health

# API docs
open http://localhost:8082/swagger-ui.html
```

---

## Support

- **Documentation**: See `specs/003-student-crud/` for full specification
- **API Contract**: `contracts/student-api.yaml`
- **Data Model**: `data-model.md`
- **Implementation Plan**: `plan.md`

**Status**: ✅ Quickstart Guide Complete
