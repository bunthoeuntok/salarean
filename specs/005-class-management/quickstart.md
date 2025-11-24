# Quick Start Guide: Class Management (student-service Extension)

**Feature**: 005-class-management
**Last Updated**: 2025-11-24
**Service**: student-service (extension)

## Overview

This guide helps developers set up and start working on the class management feature within the existing student-service. This is NOT a new microservice - it extends student-service with a new `classmanagement` package.

---

## Prerequisites

Ensure you have the following installed:

- **Java 21** (OpenJDK or Eclipse Temurin)
- **Maven 3.8+** (or use Maven wrapper `./mvnw`)
- **Docker Desktop** (for PostgreSQL, Redis, and service containers)
- **Git** (for version control)
- **IDE**: IntelliJ IDEA (recommended) or VS Code with Java extensions

**Verify installations**:
```bash
java -version    # Should show Java 21
mvn -version     # Should show Maven 3.8+
docker --version # Should show Docker 20+
```

---

## Initial Setup

### 1. Clone Repository (if not already done)

```bash
git clone <repository-url>
cd salarean
```

### 2. Checkout Feature Branch

```bash
git fetch origin
git checkout 005-class-management
```

### 3. Build sms-common Library

The student-service depends on the shared `sms-common` library. Build and install it locally:

```bash
cd sms-common
./mvnw clean install -DskipTests
cd ..
```

**Note**: Run this step whenever sms-common is updated with new cache framework classes.

### 4. Start Infrastructure Services

Start PostgreSQL, Redis, and Eureka using Docker Compose:

```bash
# From repository root
docker-compose up -d postgres-student redis eureka-server
```

**Verify services are running**:
```bash
docker-compose ps
```

You should see:
- `postgres-student` (port 5432)
- `redis` (port 6379)
- `eureka-server` (port 8761)

### 5. Configure Environment Variables

Environment variables are configured in `docker-compose.yml` for Docker deployment. For local development, student-service uses `application.yml` (default profile).

**Docker Environment** (already configured in docker-compose.yml):
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
  - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-student:5432/student_db
  - SPRING_DATASOURCE_USERNAME=sms_user
  - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
  - SPRING_REDIS_HOST=redis
  - SPRING_REDIS_PORT=6379
  - JWT_SECRET=${JWT_SECRET}
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
```

**Local Development** (application.yml defaults):
- Database: localhost:5432/student_db
- Redis: localhost:6379
- Eureka: http://localhost:8761/eureka/

---

## Running the Service

### Option 1: Run from IDE (Recommended for Development)

1. **Import project** in IntelliJ IDEA:
   - File → Open → Select `student-service/pom.xml`
   - Wait for Maven dependencies to download

2. **Configure Run Configuration**:
   - Run → Edit Configurations → Add → Application
   - Main class: `com.sms.student.StudentServiceApplication`
   - Working directory: `$MODULE_DIR$`
   - VM options (optional): `-Dspring.profiles.active=default`

3. **Run** the application (Shift+F10)

4. **Verify** the service is running:
   ```bash
   curl http://localhost:8082/actuator/health
   ```

   Expected response:
   ```json
   {
     "status": "UP",
     "components": {
       "db": {"status": "UP"},
       "redis": {"status": "UP"}
     }
   }
   ```

### Option 2: Run from Command Line

```bash
cd student-service
./mvnw spring-boot:run
```

### Option 3: Run with Docker Compose

Build and run the entire service stack:

```bash
# From repository root
docker-compose build student-service
docker-compose up student-service
```

---

## Testing the API

### 1. Get JWT Token from auth-service

First, ensure auth-service is running:

```bash
docker-compose up -d auth-service
```

Login as a teacher to get a JWT token:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "0123456789",
    "password": "password123"
  }'
```

**Save the token** from the response:
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "...",
    "expiresIn": 86400
  }
}
```

### 2. Test Class Management Endpoints

Export the token for convenience:

```bash
export TOKEN="eyJhbGciOiJIUzI1NiIs..."
```

**Create a class**:
```bash
curl -X POST http://localhost:8080/api/classes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mathematics Grade 10A",
    "gradeLevel": "GRADE_10",
    "subject": "Mathematics",
    "academicYear": "2024-2025",
    "capacity": 40,
    "description": "Advanced mathematics for grade 10 students"
  }'
```

**List teacher's classes**:
```bash
curl http://localhost:8080/api/classes \
  -H "Authorization: Bearer $TOKEN"
```

**Get class details**:
```bash
# Replace {classId} with actual UUID from create response
curl http://localhost:8080/api/classes/{classId} \
  -H "Authorization: Bearer $TOKEN"
```

**Get class students**:
```bash
curl http://localhost:8080/api/classes/{classId}/students \
  -H "Authorization: Bearer $TOKEN"
```

**Get enrollment history**:
```bash
curl http://localhost:8080/api/classes/{classId}/history \
  -H "Authorization: Bearer $TOKEN"
```

**Update class**:
```bash
curl -X PUT http://localhost:8080/api/classes/{classId} \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "capacity": 45,
    "description": "Updated capacity for high enrollment"
  }'
```

**Archive class**:
```bash
curl -X DELETE http://localhost:8080/api/classes/{classId} \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Access Swagger UI

Open your browser and navigate to:

```
http://localhost:8082/swagger-ui.html
```

This provides an interactive API documentation and testing interface.

**Note**: Swagger server URL points to API Gateway (port 8080) to avoid CORS issues.

---

## Development Workflow

### 1. Database Migrations

Flyway automatically runs migrations on startup. Migration files are located in:

```
student-service/src/main/resources/db/migration/
```

**Existing migrations** (assumed):
- V1__ through V4__ - Student tables (already exist)

**New migrations** (class management):
- `V5__create_classes_table.sql` - Creates classes table
- `V6__create_enrollment_history_table.sql` - Creates enrollment_history table

**Create a new migration**:

1. Create file: `V7__add_new_column.sql`
2. Write SQL:
   ```sql
   ALTER TABLE classes ADD COLUMN new_field VARCHAR(100);
   ```
3. Restart service - migration runs automatically

**Check migration status**:
```bash
cd student-service
./mvnw flyway:info
```

### 2. Running Tests

**Unit tests**:
```bash
cd student-service
./mvnw test
```

**Integration tests** (requires Docker):
```bash
./mvnw verify
```

**Single test class**:
```bash
./mvnw test -Dtest=ClassServiceImplTest
```

**Test coverage report**:
```bash
./mvnw test jacoco:report
# Open: target/site/jacoco/index.html
```

### 3. Code Quality Checks

**Format code** (Google Java Style):
```bash
./mvnw fmt:format
```

**Check for issues**:
```bash
./mvnw checkstyle:check
```

### 4. Hot Reload (Development)

IntelliJ IDEA automatically recompiles on save. For Spring Boot DevTools hot reload:

1. Add to `pom.xml` (if not already included):
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-devtools</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```

2. Enable "Build project automatically" in IntelliJ

3. Changes to Java files trigger automatic restart

---

## Debugging

### Enable Debug Logging

Edit `application.yml`:

```yaml
logging:
  level:
    com.sms.student.classmanagement: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.springframework.data.redis: DEBUG
```

### Debug in IDE

1. Set breakpoints in code
2. Run → Debug 'StudentServiceApplication'
3. Execute API requests to hit breakpoints

### Remote Debugging (Docker)

If running in Docker, expose debug port:

```yaml
# docker-compose.yml
student-service:
  environment:
    JAVA_OPTS: "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
  ports:
    - "5005:5005"
```

Connect IDE remote debugger to `localhost:5005`.

---

## Common Issues & Solutions

### Issue: Database connection refused

**Symptoms**: `Connection refused` or `Unknown database 'student_db'`

**Solutions**:
1. Verify PostgreSQL container is running:
   ```bash
   docker-compose ps postgres-student
   ```

2. Check database credentials in `application.yml` or environment variables

3. Manually create database if needed:
   ```bash
   docker exec -it postgres-student psql -U sms_user -d postgres
   CREATE DATABASE student_db OWNER sms_user;
   ```

### Issue: Redis connection failed

**Symptoms**: Cache operations fail, service still works but slower

**Solutions**:
1. Verify Redis container is running:
   ```bash
   docker-compose ps redis
   ```

2. Test Redis connection:
   ```bash
   docker exec -it redis redis-cli ping
   # Should return: PONG
   ```

3. Check Redis host/port in configuration

**Note**: Application uses cache-aside pattern with graceful degradation - Redis failures don't break functionality.

### Issue: Eureka registration failed

**Symptoms**: Service runs but not visible in Eureka dashboard

**Solutions**:
1. Verify Eureka is running:
   ```bash
   curl http://localhost:8761/eureka/apps
   ```

2. Check `eureka.client.service-url.defaultZone` in `application-docker.yml`

3. For local development, Eureka registration is optional

### Issue: JWT validation fails

**Symptoms**: `401 Unauthorized` on all requests

**Solutions**:
1. Verify JWT_SECRET matches between auth-service and student-service

2. Check token hasn't expired (24-hour lifetime)

3. Ensure Authorization header format: `Bearer {token}` (note the space)

4. Verify auth-service is running and accessible

### Issue: Flyway migration version conflict

**Symptoms**: `FlywayException: Detected resolved migration not applied to database: V5`

**Solutions**:
1. Check current migration status:
   ```bash
   ./mvnw flyway:info
   ```

2. If needed, repair Flyway metadata:
   ```bash
   ./mvnw flyway:repair
   ```

3. Ensure migration files follow naming convention: `V{number}__{description}.sql`

---

## Useful Commands

### Docker Management

```bash
# View logs
docker-compose logs -f student-service

# Restart service
docker-compose restart student-service

# Stop all services
docker-compose down

# Remove volumes (clears database)
docker-compose down -v

# Rebuild after code changes
docker-compose build student-service
```

### Database Access

```bash
# Connect to PostgreSQL
docker exec -it postgres-student psql -U sms_user -d student_db

# List tables
\dt

# Describe table
\d classes

# Query data
SELECT * FROM classes;
SELECT * FROM enrollment_history;

# Check Flyway migration history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

### Redis Access

```bash
# Connect to Redis CLI
docker exec -it redis redis-cli

# List all keys
KEYS *

# Get cached value
GET "student-service:teacher:classes:{teacherId}"

# Check class details cache
GET "student-service:class:{classId}"

# View cache TTL
TTL "student-service:teacher:classes:{teacherId}"

# Flush cache (development only)
FLUSHALL
```

---

## Next Steps

1. **Read the spec**: Review [spec.md](./spec.md) for feature requirements
2. **Understand data model**: See [data-model.md](./data-model.md) for entities
3. **Review API contracts**: Check [contracts/openapi.yml](./contracts/openapi.yml)
4. **Implementation tasks**: See [tasks.md](./tasks.md) for task breakdown

---

## Additional Resources

- **Project Constitution**: `.specify/memory/constitution.md`
- **Microservice Standards**: `CLAUDE.md` (auto-generated development guidelines)
- **Spring Boot Docs**: https://docs.spring.io/spring-boot/docs/3.5.7/reference/html/
- **Spring Data Redis**: https://docs.spring.io/spring-data/redis/reference/
- **Flyway Documentation**: https://documentation.red-gate.com/fd/flyway-documentation-138346877.html
- **OpenAPI Spec**: https://swagger.io/specification/

---

## Getting Help

- **Team Documentation**: `/specs/005-class-management/` directory
- **Code Examples**: See `auth-service` for established patterns
- **Architecture Questions**: Review `.specify/memory/constitution.md`
- **Development Guidelines**: `CLAUDE.md` in repository root

---

**Quick Start Status**: ✅ Complete - Ready for development!
