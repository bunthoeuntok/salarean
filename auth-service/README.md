# Auth Service

Authentication and authorization microservice for the School Management System (SMS).

## Features

- **Teacher Registration**: Register with email, Cambodia phone number (+855 format), and secure password
- **Teacher Login**: Login with email OR phone number + password
- **Token Management**: JWT access tokens (24h) + refresh tokens (30d) with token rotation
- **Session Management**: Secure session tracking with automatic invalidation on security events
- **Profile Management**: View and update profile (name, phone, language preference)
- **Password Management**: Change password with current password verification
- **Photo Upload**: Profile photo upload (JPG/PNG, max 5MB) with validation
- **Rate Limiting**: Protection against brute force attacks (5 failed attempts per 15 minutes)
- **Security**: Token replay detection, session invalidation on password change
- **Audit Trail**: Login attempt tracking with 7-year retention for compliance
- **Automatic Cleanup**: Scheduled jobs to remove expired sessions and old login attempts

## Tech Stack

- **Framework**: Spring Boot 3.5.7
- **Language**: Java 21
- **Database**: PostgreSQL 15+ (via Flyway migrations)
- **Authentication**: JWT (JJWT 0.12.5, HS256 algorithm)
- **Password Hashing**: BCrypt (cost factor 12)
- **Caching**: Redis 7+ (for refresh token storage and performance)
- **File Storage**: Local filesystem (profile photos)
- **File Validation**: Apache Tika (MIME type detection)
- **Service Discovery**: Eureka Client

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

#### POST /api/auth/refresh

Refresh access token using refresh token (token rotation - old refresh token is invalidated).

**Request Body:**
```json
{
  "refreshToken": "uuid-refresh-token"
}
```

**Success Response (200):**
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "accessToken": "new-jwt-token",
    "refreshToken": "new-refresh-token",
    "expiresIn": 86400
  }
}
```

**Error Codes:**
- `INVALID_TOKEN` (401): Invalid or expired refresh token
- `TOKEN_REPLAY_DETECTED` (401): Token has been reused - all sessions invalidated

#### POST /api/auth/logout

Logout and invalidate current session + all refresh tokens.

**Headers:** `Authorization: Bearer {access-token}`

**Success Response (200):**
```json
{
  "errorCode": "SUCCESS",
  "data": null
}
```

#### GET /api/auth/me

Get current user profile (requires authentication).

**Headers:** `Authorization: Bearer {access-token}`

**Success Response (200):**
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "id": "uuid",
    "email": "teacher@school.edu.kh",
    "phoneNumber": "+85512345678",
    "name": "John Doe",
    "preferredLanguage": "en",
    "profilePhotoUrl": "/uploads/profile-photos/uuid/profile.jpg",
    "profilePhotoUploadedAt": "2025-11-20T12:00:00",
    "accountStatus": "active",
    "createdAt": "2025-11-20T10:00:00"
  }
}
```

#### PUT /api/users/me

Update current user profile (name, phone, language).

**Headers:** `Authorization: Bearer {access-token}`

**Request Body:**
```json
{
  "name": "John Doe",
  "phoneNumber": "+85587654321",
  "preferredLanguage": "km"
}
```

**Success Response (200):**
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "id": "uuid",
    "email": "teacher@school.edu.kh",
    "phoneNumber": "+85587654321",
    "name": "John Doe",
    "preferredLanguage": "km",
    ...
  }
}
```

**Error Codes:**
- `DUPLICATE_PHONE` (400): Phone number already in use
- `INVALID_PHONE_FORMAT` (400): Invalid Cambodia phone format
- `VALIDATION_ERROR` (400): Invalid input data

#### PUT /api/users/me/password

Change password (invalidates all other sessions).

**Headers:** `Authorization: Bearer {access-token}`

**Request Body:**
```json
{
  "currentPassword": "OldPass123!",
  "newPassword": "NewSecurePass456!"
}
```

**Success Response (200):**
```json
{
  "errorCode": "SUCCESS",
  "data": null
}
```

**Error Codes:**
- `INCORRECT_PASSWORD` (400): Current password is incorrect
- `WEAK_PASSWORD` (400): New password doesn't meet requirements
- `PASSWORD_TOO_SHORT` (400): Password must be at least 8 characters
- `PASSWORD_MISSING_UPPERCASE` (400): Password must contain uppercase letter
- `PASSWORD_MISSING_LOWERCASE` (400): Password must contain lowercase letter
- `PASSWORD_MISSING_DIGIT` (400): Password must contain a digit
- `PASSWORD_MISSING_SPECIAL` (400): Password must contain special character
- `PASSWORD_TOO_COMMON` (400): Password is too common

#### POST /api/users/me/photo

Upload or update profile photo (JPG/PNG, max 5MB).

**Headers:**
- `Authorization: Bearer {access-token}`
- `Content-Type: multipart/form-data`

**Form Data:**
- `photo`: File (image/jpeg or image/png)

**Success Response (200):**
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "photoUrl": "/uploads/profile-photos/uuid/profile.jpg",
    "uploadedAt": "2025-11-20T12:00:00"
  }
}
```

**Error Codes:**
- `PHOTO_SIZE_EXCEEDED` (400): File exceeds 5MB limit
- `INVALID_PHOTO_FORMAT` (400): File must be JPG or PNG
- `CORRUPTED_IMAGE` (400): File content doesn't match extension

## API Response Format

All endpoints return a consistent response format:

**Success:**
```json
{
  "errorCode": "SUCCESS",
  "data": { ... }
}
```

**Error:**
```json
{
  "errorCode": "ERROR_CODE",
  "data": null
}
```

The frontend is responsible for translating error codes to user-friendly messages in English and Khmer.

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

The service manages 4 main tables:

- **users**: Teacher accounts (with profile fields: name, photo URL, language preference)
- **sessions**: Active JWT access token sessions (24-hour TTL)
- **refresh_tokens**: Long-lived refresh tokens (30-day TTL) with replay detection
- **login_attempts**: Audit trail of all login attempts (7-year retention)

Migrations are managed by Flyway and located in `src/main/resources/db/migration/`.

**Key Indexes:**
- `users(email)`, `users(phone_number)` - Fast lookup for login
- `refresh_tokens(user_id)`, `refresh_tokens(expires_at)`, `refresh_tokens(has_been_used)` - Token validation and cleanup
- `sessions(token_jti)`, `sessions(user_id)` - Session management

## Security Features

- **BCrypt Password Hashing**: Cost factor 12 for passwords, also used for refresh token hashing
- **JWT Access Tokens**: HS256 algorithm, 24-hour expiration, includes user ID and language claim
- **Refresh Token Rotation**: One-time use refresh tokens (30-day expiration) with automatic rotation
- **Token Replay Detection**: Detects and blocks replay attacks, invalidates all sessions on detection
- **Dual Token Storage**: Refresh tokens stored in PostgreSQL (persistence) and Redis (performance)
- **Rate Limiting**: Database-backed, 5 attempts per 15-minute sliding window
- **Session Tracking**: IP address and user agent recorded for audit
- **Password Change Security**: Invalidates all other sessions when password is changed
- **Photo Upload Validation**: File size, MIME type, and content verification using Apache Tika
- **Automatic Cleanup**: Hourly expired session removal, daily old data purge

## Scheduled Jobs

- **Session Cleanup**: Runs every hour, removes expired sessions
- **Login Attempt Cleanup**: Runs daily at 2 AM, removes attempts older than 7 years

## Monitoring

Health check endpoint: `http://localhost:8081/actuator/health`

## Architecture

This service follows a layered architecture:

- **Controller Layer**: REST endpoints (`AuthController`, `ProfileController`)
- **Service Layer**: Business logic (`AuthService`, `ProfileService`, `TokenService`, `PhotoStorageService`, `RateLimitService`)
- **Repository Layer**: Data access (`UserRepository`, `SessionRepository`, `RefreshTokenRepository`, `LoginAttemptRepository`)
- **Security Layer**: JWT generation/validation (`JwtTokenProvider`, `JwtAuthenticationFilter`)
- **Validation Layer**: Input validation (`PasswordStrengthValidator`, `CambodiaPhoneValidator`)
- **Storage Layer**: File storage (`PhotoStorageService` with Apache Tika validation)


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
