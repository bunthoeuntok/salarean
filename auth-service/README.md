# Auth Service

Authentication and authorization microservice for the School Management System (SMS).

## Features

- **Teacher Registration**: Register with email, Cambodia phone number (+855 format), and secure password
- **Teacher Login**: Login with email OR phone number + password
- **Session Management**: JWT-based authentication with 24-hour expiration
- **Rate Limiting**: Protection against brute force attacks (5 failed attempts per 15 minutes)
- **Audit Trail**: Login attempt tracking with 7-year retention for compliance
- **Automatic Cleanup**: Scheduled jobs to remove expired sessions and old login attempts

## Tech Stack

- **Framework**: Spring Boot 3.5.7
- **Language**: Java 21
- **Database**: PostgreSQL 15+ (via Flyway migrations)
- **Authentication**: JWT (JJWT 0.12.5, HS256 algorithm)
- **Password Hashing**: BCrypt (cost factor 12)
- **Service Discovery**: Eureka Client
- **Caching**: Redis (optional)

## API Documentation

Full API specifications are available in the project documentation:

- [Registration API Contract](../specs/001-teacher-auth/contracts/register-api.yaml)
- [Login API Contract](../specs/001-teacher-auth/contracts/login-api.yaml)

### Quick API Reference

#### POST /api/auth/register

Register a new teacher account.

**Request Body:**
```json
{
  "email": "teacher@school.edu.kh",
  "phoneNumber": "+85512345678",
  "password": "SecurePass123!",
  "preferredLanguage": "en"
}
```

**Success Response (200):**
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "userId": "uuid-here",
    "email": "teacher@school.edu.kh",
    "phoneNumber": "+85512345678",
    "preferredLanguage": "en",
    "token": "jwt-token-here",
    "createdAt": "2025-11-20T10:00:00",
    "lastLoginAt": "2025-11-20T10:00:00"
  }
}
```

**Error Codes:**
- `DUPLICATE_EMAIL` (400): Email already registered
- `DUPLICATE_PHONE` (400): Phone number already registered
- `INVALID_PASSWORD` (400): Password doesn't meet strength requirements
- `INVALID_EMAIL_FORMAT` (400): Invalid email format
- `INVALID_PHONE_FORMAT` (400): Invalid Cambodia phone format

#### POST /api/auth/login

Login with email or phone number.

**Request Body:**
```json
{
  "emailOrPhone": "teacher@school.edu.kh",
  "password": "SecurePass123!"
}
```

**Success Response (200):**
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "userId": "uuid-here",
    "email": "teacher@school.edu.kh",
    "phoneNumber": "+85512345678",
    "preferredLanguage": "en",
    "token": "jwt-token-here",
    "createdAt": "2025-11-20T10:00:00",
    "lastLoginAt": "2025-11-20T10:00:00"
  }
}
```

**Error Codes:**
- `INVALID_CREDENTIALS` (401): Invalid email/phone or password
- `RATE_LIMIT_EXCEEDED` (429): Too many failed login attempts (wait 15 minutes)

## Password Requirements

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character (@#$%^&+=!*()_-)

## Phone Number Format

Must be Cambodia format: `+855` followed by 8-9 digits (not starting with 0)

**Valid examples:**
- `+85512345678` (8 digits)
- `+855123456789` (9 digits)

## Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/auth_db` | Yes |
| `SPRING_DATASOURCE_USERNAME` | Database username | `sms_user` | Yes |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `password` | Yes |
| `JWT_SECRET` | Secret key for JWT signing (min 32 chars) | - | Yes |
| `SPRING_REDIS_HOST` | Redis host for caching | `localhost` | No |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka server URL | `http://localhost:8761/eureka/` | Yes |

## Running Locally

### Prerequisites

- Java 21
- PostgreSQL 15+
- Redis (optional)
- Maven 3.9+

### Setup

1. **Start PostgreSQL:**
   ```bash
   docker run -d \
     --name sms-postgres-auth \
     -e POSTGRES_DB=auth_db \
     -e POSTGRES_USER=sms_user \
     -e POSTGRES_PASSWORD=password \
     -p 5432:5432 \
     postgres:15
   ```

2. **Set environment variables:**
   ```bash
   export JWT_SECRET="your-secret-key-at-least-32-characters-long"
   export DB_PASSWORD="password"
   ```

3. **Build and run:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

The service will start on port 8081.

## Running with Docker Compose

From the project root:

```bash
docker-compose up -d auth-service
```

## Testing

### Run all tests:
```bash
mvn test
```

### Run integration tests:
```bash
mvn verify -Dspring.profiles.active=test
```

### Test coverage:
```bash
mvn test jacoco:report
```

## Database Schema

The service manages 3 main tables:

- **users**: Teacher accounts
- **sessions**: Active JWT sessions (24-hour TTL)
- **login_attempts**: Audit trail of all login attempts (7-year retention)

Migrations are managed by Flyway and located in `src/main/resources/db/migration/`.

## Security Features

- **BCrypt Password Hashing**: Cost factor 12
- **JWT Token Security**: HS256 algorithm, 24-hour expiration
- **Rate Limiting**: Database-backed, 5 attempts per 15-minute sliding window
- **Session Tracking**: IP address and user agent recorded for audit
- **Automatic Cleanup**: Hourly expired session removal, daily old data purge

## Scheduled Jobs

- **Session Cleanup**: Runs every hour, removes expired sessions
- **Login Attempt Cleanup**: Runs daily at 2 AM, removes attempts older than 7 years

## Monitoring

Health check endpoint: `http://localhost:8081/actuator/health`

## Architecture

This service follows a layered architecture:

- **Controller Layer**: REST endpoints (`AuthController`)
- **Service Layer**: Business logic (`AuthService`, `RateLimitService`)
- **Repository Layer**: Data access (`UserRepository`, `SessionRepository`, `LoginAttemptRepository`)
- **Security Layer**: JWT generation/validation (`JwtTokenProvider`)
- **Validation Layer**: Input validation (`PasswordValidator`, `CambodiaPhoneValidator`)

## Error Handling

All errors return a consistent response format:

```json
{
  "errorCode": "ERROR_CODE_ENUM",
  "data": null
}
```

The frontend is responsible for translating error codes to user-friendly messages in English and Khmer.

## Development

### Code Style
- Follow Java naming conventions
- Use Lombok annotations to reduce boilerplate
- Write comprehensive tests (target: >85% coverage)
- Document public APIs with Javadoc

### Adding New Features
1. Update database schema with Flyway migration
2. Create/update entities and repositories
3. Implement business logic in service layer
4. Add REST endpoints in controller
5. Write unit and integration tests
6. Update this README with API documentation

## License

Proprietary - School Management System
