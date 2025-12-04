# Student Service

Student Management microservice for the School Management System (SMS). Handles student information, enrollment, parent contacts, and photo management.

## Overview

The Student Service is a Spring Boot microservice that provides comprehensive student management functionality including:

- Student CRUD operations
- Parent contact management
- Student class enrollment tracking
- Batch student transfer between classes
- Transfer undo capability (5-minute window)
- Photo upload and storage
- Soft-delete with audit trail
- JWT-based authentication and authorization

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Data JPA** with Hibernate
- **Spring Security** with JWT authentication
- **Spring Cloud Netflix Eureka** (service discovery)
- **PostgreSQL 15+** (database)
- **Flyway** (database migrations)
- **Maven** (build tool)
- **Docker** (containerization)
- **OpenAPI 3.0 / Swagger UI** (API documentation)

## Prerequisites

- Java 21
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 15+ (if running locally without Docker)

## Getting Started

### Running with Docker Compose

1. Build the Docker image:
```bash
docker-compose build student-service
```

2. Start all required services:
```bash
docker-compose up -d eureka-server postgres-student student-service
```

3. Verify service health:
```bash
curl http://localhost:8082/actuator/health
```

### Running Locally

1. Compile the application:
```bash
./mvnw clean compile
```

2. Run the application:
```bash
./mvnw spring-boot:run
```

3. Access Swagger UI:
```
http://localhost:8082/swagger-ui/index.html
```

## API Endpoints

### Student Management

All endpoints require `TEACHER` role authorization via JWT token.

#### Create Student
```http
POST /api/students
Content-Type: application/json
Authorization: Bearer {jwt-token}

{
  "firstName": "Sokha",
  "lastName": "Kim",
  "firstNameKhmer": "សុខា",
  "lastNameKhmer": "គឹម",
  "dateOfBirth": "2010-01-15",
  "gender": "M",
  "address": "Phnom Penh, Cambodia",
  "emergencyContact": "+855999888777",
  "enrollmentDate": "2025-01-10",
  "classId": "uuid",
  "parentContacts": [
    {
      "fullName": "Dara Kim",
      "phoneNumber": "+855123456789",
      "relationship": "MOTHER",
      "isPrimary": true
    }
  ]
}
```

#### Update Student
```http
PUT /api/students/{id}
Content-Type: application/json
Authorization: Bearer {jwt-token}

{
  "firstName": "Updated Name",
  ...
}
```

#### Get Student by ID
```http
GET /api/students/{id}
Authorization: Bearer {jwt-token}
```

#### Get Student by Code
```http
GET /api/students/code/{studentCode}
Authorization: Bearer {jwt-token}
```

#### List Students by Class
```http
GET /api/students/class/{classId}?page=0&size=20
Authorization: Bearer {jwt-token}
```

#### List All Students (Paginated)
```http
GET /api/students?page=0&size=20&sort=lastName,asc
Authorization: Bearer {jwt-token}
```

#### Search Students
```http
GET /api/students/search?query=sokha&page=0&size=20
Authorization: Bearer {jwt-token}
```

#### Soft Delete Student
```http
DELETE /api/students/{id}?reason=Transferred
Authorization: Bearer {jwt-token}
```

#### Upload Student Photo
```http
POST /api/students/{id}/photo
Content-Type: multipart/form-data
Authorization: Bearer {jwt-token}

photo: [file]
```

### Class Management

#### Get Eligible Destination Classes
```http
GET /api/classes/{classId}/eligible-destinations
Authorization: Bearer {jwt-token}
```
Returns active classes at the same grade level with available capacity for student transfer.

#### Batch Transfer Students
```http
POST /api/classes/{classId}/batch-transfer
Content-Type: application/json
Authorization: Bearer {jwt-token}

{
  "destinationClassId": "uuid",
  "studentIds": ["uuid1", "uuid2", "uuid3"]
}
```
Transfers multiple students from source class to destination class. Validates grade match, capacity, and enrollment status. Returns transfer ID for undo capability.

**Response:**
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "transferId": "uuid",
    "sourceClassId": "uuid",
    "destinationClassId": "uuid",
    "successfulTransfers": 3,
    "failedTransfers": [],
    "transferredAt": "2025-12-04T12:30:00"
  }
}
```

#### Undo Batch Transfer
```http
POST /api/classes/transfers/{transferId}/undo
Authorization: Bearer {jwt-token}
```
Reverses a batch transfer within 5 minutes. Only the teacher who performed the transfer can undo it.

**Response:**
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "transferId": "uuid",
    "undoneStudents": 3,
    "sourceClassId": "uuid",
    "undoneAt": "2025-12-04T12:35:00"
  }
}
```

#### Get Class Students
```http
GET /api/classes/{classId}/students?status=ACTIVE
Authorization: Bearer {jwt-token}
```
Returns all students enrolled in a class, optionally filtered by enrollment status.

### Health & Monitoring

#### Health Check
```http
GET /actuator/health
```

#### Application Info
```http
GET /actuator/info
```

#### Metrics
```http
GET /actuator/metrics
```

## Authentication

All API endpoints (except `/actuator/health`, `/swagger-ui/**`, `/v3/api-docs/**`) require JWT authentication.

### Obtaining a JWT Token

1. Authenticate with the `auth-service`:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teacher@school.com",
    "password": "password"
  }'
```

2. Extract the `accessToken` from the response

3. Use the token in subsequent requests:
```bash
curl http://localhost:8082/api/students \
  -H "Authorization: Bearer {accessToken}"
```

### Using Swagger UI with Authentication

1. Navigate to http://localhost:8082/swagger-ui/index.html
2. Click the "Authorize" button (lock icon)
3. Enter your JWT token in the format: `Bearer {token}`
4. Click "Authorize" and then "Close"
5. All requests will now include the authorization header

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5433/student_db` |
| `DB_USERNAME` | Database username | `sms_user` |
| `DB_PASSWORD` | Database password | - |
| `EUREKA_CLIENT_SERVICE_URL` | Eureka server URL | `http://localhost:8761/eureka/` |
| `JWT_SECRET` | JWT signing secret | - |
| `UPLOAD_DIR` | Photo upload directory | `uploads/students` |

### Database Configuration

The service uses Flyway for database migrations. Migrations are located in `src/main/resources/db/migration/`.

Migration files:
- `V1__create_students_table.sql` - Core tables (schools, classes, students)
- `V2__create_parent_contacts_table.sql` - Parent contact management
- `V3__create_enrollments_table.sql` - Student class enrollments
- `V4__create_views_and_functions.sql` - Database views and functions
- `V5__fix_gender_column_type.sql` - Schema fixes

## Development

### Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Code Coverage

The project uses JaCoCo for code coverage with a minimum requirement of 70% line coverage.

### Building

```bash
# Clean and package
./mvnw clean package

# Skip tests during build
./mvnw clean package -DskipTests
```

## Docker

### Building the Image

```bash
docker-compose build student-service
```

### Running the Container

```bash
docker-compose up -d student-service
```

### Viewing Logs

```bash
docker logs -f sms-student-service
```

## Troubleshooting

### Service Won't Start

1. Check PostgreSQL is running:
```bash
docker ps | grep postgres-student
```

2. Check Eureka server is accessible:
```bash
curl http://localhost:8761
```

3. View service logs:
```bash
docker logs sms-student-service --tail=100
```

### Database Issues

Reset database migrations if needed:
```bash
# Connect to database
docker exec -it sms-postgres-student psql -U sms_user -d student_db

# Check migration history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

# If needed, run Flyway repair (use with caution)
./mvnw flyway:repair
```

### Health Check Fails

The health endpoint returns DOWN if:
- Database connection fails
- Eureka registration fails

Check detailed health:
```bash
curl http://localhost:8082/actuator/health -H "Authorization: Bearer {token}"
```

## API Response Format

All API responses follow this standardized format:

### Success Response
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "id": "uuid",
    "studentCode": "STU-2025-0001",
    "firstName": "Sokha",
    ...
  }
}
```

### Error Response
```json
{
  "errorCode": "STUDENT_NOT_FOUND",
  "data": null
}
```

### Error Codes

- `SUCCESS` - Operation completed successfully
- `STUDENT_NOT_FOUND` - Student with given ID not found
- `STUDENT_CODE_EXISTS` - Student code already exists
- `CLASS_NOT_FOUND` - Class with given ID not found
- `CLASS_FULL` - Class has reached maximum capacity
- `INVALID_INPUT` - Request validation failed
- `UNAUTHORIZED` - Missing or invalid authentication
- `FORBIDDEN` - Insufficient permissions
- `INTERNAL_ERROR` - Server error

## Service Discovery

The service registers with Eureka at startup. View registered services:

```
http://localhost:8761
```

Service name: `STUDENT-SERVICE`

## Monitoring

### Actuator Endpoints

- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

### Health Probes (Kubernetes)

- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`

## License

Copyright (c) 2025 School Management System
