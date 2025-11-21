# Quickstart Guide: JWT Auth & Profile Management

**Feature**: 002-jwt-auth | **Date**: 2025-11-20 | **Phase**: 1 (Design)

## Purpose

This guide helps developers set up, test, and integrate the JWT token lifecycle and profile management endpoints. Follow these steps to work with refresh tokens, logout, profile CRUD, password changes, and photo uploads.

---

## Prerequisites

From feature 001-teacher-auth, you should already have:

- ✅ Docker and Docker Compose installed
- ✅ PostgreSQL container running (auth-service database)
- ✅ Redis container running (session cache)
- ✅ auth-service Spring Boot application (Java 21)
- ✅ Basic authentication working (login/register)

**Verify prerequisites**:
```bash
# Check containers are running
docker-compose ps

# Expected output:
# NAME                  STATUS
# salarean-postgres     Up
# salarean-redis        Up
# salarean-auth-service Up
```

---

## Setup Steps

### 1. Apply Database Migration

Run the Flyway migration to add refresh tokens and profile fields:

```bash
# From repository root
cd auth-service

# Run migration (automatically applied on service startup)
./mvnw spring-boot:run

# Or manually with Flyway
./mvnw flyway:migrate
```

**Verify migration**:
```sql
-- Connect to PostgreSQL
docker exec -it salarean-postgres psql -U sms_user -d auth_db

-- Check new columns
\d users
-- Should see: name, profile_photo_url, profile_photo_uploaded_at

-- Check new table
\d refresh_tokens
-- Should see: id, user_id, token_hash, expires_at, has_been_used, etc.

-- Exit
\q
```

---

### 2. Add Apache Tika Dependency

Update `auth-service/pom.xml`:

```xml
<!-- Add after existing dependencies -->
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-core</artifactId>
    <version>2.9.1</version>
</dependency>
```

**Rebuild**:
```bash
./mvnw clean install
```

---

### 3. Configure Photo Storage

Update `auth-service/src/main/resources/application.yml`:

```yaml
# Add photo storage configuration
app:
  photo:
    upload-dir: ./uploads/profile-photos
    max-file-size: 5242880  # 5MB in bytes
    allowed-types:
      - image/jpeg
      - image/png
```

**Create uploads directory**:
```bash
mkdir -p auth-service/uploads/profile-photos
```

**Add to .gitignore**:
```bash
echo "auth-service/uploads/" >> .gitignore
```

---

### 4. Verify API Standards Configuration

**IMPORTANT**: This feature uses standardized API response format `{errorCode: ErrorCode, data: T}`.

- **Backend**: Returns only machine-readable error codes (e.g., "INVALID_TOKEN", "DUPLICATE_PHONE")
- **Frontend**: Handles all internationalization by mapping error codes to localized messages
- **No backend i18n**: Do NOT create messages_en.properties or messages_km.properties files

**Error codes are defined in**: `auth-service/src/main/java/com/sms/auth/dto/ErrorCodes.java`

---

## API Testing

### Setup: Create Test User

First, register a test teacher (from 001-teacher-auth):

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teacher@test.com",
    "phoneNumber": "+85512345678",
    "password": "TestPassword123!",
    "preferredLanguage": "en"
  }'
```

**Expected response**:
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "expiresIn": 86400
  }
}
```

**Save tokens**:
```bash
export ACCESS_TOKEN="<your-access-token>"
export REFRESH_TOKEN="<your-refresh-token>"
```

---

### 1. Refresh Access Token

**Request**:
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"
```

**Expected response**:
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "expiresIn": 86400
  }
}
```

**Notes**:
- Old refresh token is now invalid (token rotation)
- New access token valid for 24h
- New refresh token valid for 30d

---

### 2. Get Current User Profile

**Request**:
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected response**:
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "email": "teacher@test.com",
    "phoneNumber": "+85512345678",
    "name": null,
    "preferredLanguage": "en",
    "profilePhotoUrl": null,
    "profilePhotoUploadedAt": null,
    "accountStatus": "active",
    "createdAt": "2025-11-20T10:00:00Z"
  }
}
```

---

### 3. Update Profile

**Request**:
```bash
curl -X PUT http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sok Dara",
    "phoneNumber": "+85587654321",
    "preferredLanguage": "km"
  }'
```

**Expected response**:
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "email": "teacher@test.com",
    "phoneNumber": "+85587654321",
    "name": "Sok Dara",
    "preferredLanguage": "km",
    "profilePhotoUrl": null,
    "accountStatus": "active",
    "createdAt": "2025-11-20T10:00:00Z"
  }
}
```

**Error case (duplicate phone)**:
```bash
# Try to use another user's phone number
curl -X PUT http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+85512345678"}'
```

**Expected error**:
```json
{
  "errorCode": "DUPLICATE_PHONE",
  "data": null
}
```

---

### 4. Change Password

**Request**:
```bash
curl -X PUT http://localhost:8080/api/users/me/password \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "TestPassword123!",
    "newPassword": "NewPassword456@"
  }'
```

**Expected response**:
```json
{
  "errorCode": "SUCCESS",
  "data": null
}
```

**Notes**:
- Current session remains valid
- All other sessions/refresh tokens invalidated
- Must use new password for future logins

**Error case (weak password)**:
```bash
curl -X PUT http://localhost:8080/api/users/me/password \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "NewPassword456@",
    "newPassword": "weak"
  }'
```

**Expected error**:
```json
{
  "errorCode": "WEAK_PASSWORD",
  "data": null
}
```

**Note**: Frontend is responsible for displaying specific password requirements based on the error code.

---

### 5. Upload Profile Photo

**Request**:
```bash
curl -X POST http://localhost:8080/api/users/me/photo \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -F "photo=@/path/to/your/photo.jpg"
```

**Expected response**:
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "photoUrl": "/uploads/profile-photos/7c9e6679-7425-40de-944b-e07fc1f90ae7/profile.jpg",
    "uploadedAt": "2025-11-20T14:25:00Z"
  }
}
```

**Verify upload**:
```bash
ls -lh auth-service/uploads/profile-photos/7c9e6679-7425-40de-944b-e07fc1f90ae7/
# Should see: profile.jpg
```

**Error case (file too large)**:
```bash
# Try to upload 6MB file
curl -X POST http://localhost:8080/api/users/me/photo \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -F "photo=@/path/to/large-photo.jpg"
```

**Expected error**:
```json
{
  "errorCode": "PHOTO_SIZE_EXCEEDED",
  "data": null
}
```

---

### 6. Logout

**Request**:
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected response**:
```json
{
  "errorCode": "SUCCESS",
  "data": null
}
```

**Verify logout**:
```bash
# Try to use the same token
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected error**:
```json
{
  "errorCode": "UNAUTHORIZED",
  "data": null
}
```

---

## Testing Token Replay Protection

This demonstrates the security mechanism that detects and prevents token replay attacks.

### Setup

1. Login and get tokens:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "teacher@test.com",
    "password": "NewPassword456@"
  }'
```

2. Save both tokens:
```bash
export ACCESS_TOKEN="<access-token>"
export REFRESH_TOKEN="<refresh-token>"
```

### Test Replay Attack

1. Use refresh token once (legitimate):
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"
```

**Expected**: New tokens returned

2. Try to reuse the same refresh token (replay attack):
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"
```

**Expected response**:
```json
{
  "errorCode": "TOKEN_REPLAY_DETECTED",
  "data": null
}
```

3. Verify all sessions invalidated:
```bash
# Try to use any previously valid token
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected**: 401 Unauthorized

**Security implication**: If an attacker steals and reuses a refresh token, the system detects the replay and invalidates all sessions, protecting the user.

---

## Common Issues & Solutions

### Issue 1: "Photo upload directory not found"

**Solution**:
```bash
mkdir -p auth-service/uploads/profile-photos
chmod 755 auth-service/uploads
```

### Issue 2: "Refresh token not found in Redis"

**Cause**: Redis container restarted (cache cleared)

**Solution**: Token lookup falls back to PostgreSQL automatically. To verify:
```bash
docker exec -it salarean-postgres psql -U sms_user -d auth_db -c "SELECT id, user_id, expires_at FROM refresh_tokens WHERE expires_at > NOW();"
```

### Issue 3: "Database migration failed"

**Solution**: Manually run migration:
```bash
cd auth-service
./mvnw flyway:migrate

# If still fails, check migration status
./mvnw flyway:info
```

### Issue 4: "Error codes not recognized by frontend"

**Cause**: Frontend i18n mapping missing or incorrect

**Solution**: Verify frontend has all error code translations:
- Check frontend i18n files contain mappings for all error codes
- Error codes must match exactly (case-sensitive): INVALID_TOKEN, DUPLICATE_PHONE, etc.
- Frontend should handle unknown error codes gracefully with generic message

---

## Performance Testing

### Load Test: Token Refresh

**Tool**: Apache Bench (ab)

```bash
# Install ab
brew install apache2  # macOS
sudo apt install apache2-utils  # Linux

# Create request body file
echo '{"refreshToken":"'$REFRESH_TOKEN'"}' > refresh-request.json

# Run 1000 requests, 100 concurrent
ab -n 1000 -c 100 -p refresh-request.json -T application/json \
   http://localhost:8080/api/auth/refresh
```

**Expected results**:
- Requests per second: > 500
- Mean time per request: < 200ms
- 99th percentile: < 500ms

### Load Test: Profile Photo Upload

```bash
# Create 1MB test image
dd if=/dev/urandom of=test-photo.jpg bs=1024 count=1024

# Run 100 sequential uploads
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/users/me/photo \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    -F "photo=@test-photo.jpg" \
    -w "Time: %{time_total}s\n" \
    -o /dev/null -s
done
```

**Expected**: < 3s per upload for 1MB files

---

## Next Steps

After verifying all endpoints work:

1. Run `/speckit.tasks` to generate implementation tasks
2. Review tasks.md for step-by-step implementation guide
3. Implement features following TDD (write tests first)
4. Run integration tests to verify complete flows

## Additional Resources

- **OpenAPI Spec**: `specs/002-jwt-auth/contracts/api-contracts.yaml`
- **Data Model**: `specs/002-jwt-auth/data-model.md`
- **Research Decisions**: `specs/002-jwt-auth/research.md`
- **Feature Spec**: `specs/002-jwt-auth/spec.md`
