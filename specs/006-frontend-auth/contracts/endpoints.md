# API Contracts: Frontend Authentication Integration

**Feature**: 006-frontend-auth
**Date**: 2025-11-25
**Base URL**: `http://localhost:8080` (API Gateway)

## Overview

This document defines the API contracts between the frontend application and the auth-service backend. All endpoints follow the standard `ApiResponse<T>` wrapper format.

## Response Format

All endpoints return responses in this format:

```json
{
  "errorCode": "SUCCESS" | "<ERROR_CODE>",
  "data": <T> | null
}
```

## Authentication Endpoints

### POST /api/auth/login

Authenticate a teacher with email/phone and password.

**Request**:
```json
{
  "emailOrPhone": "string",
  "password": "string"
}
```

**Success Response** (200 OK):
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "userId": "uuid",
    "email": "teacher@example.com",
    "phoneNumber": "+855123456789",
    "preferredLanguage": "en",
    "token": "jwt-access-token",
    "refreshToken": "jwt-refresh-token",
    "expiresIn": 900,
    "createdAt": "2025-01-15T10:30:00Z",
    "lastLoginAt": "2025-11-25T14:00:00Z"
  }
}
```

**Note**: With HTTP-only cookie implementation, tokens will be set via `Set-Cookie` headers instead of response body.

**Error Responses**:

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| INVALID_CREDENTIALS | 401 | Wrong email/phone or password |
| ACCOUNT_LOCKED | 403 | Account locked due to failed attempts |
| RATE_LIMITED | 429 | Too many login attempts |

---

### POST /api/auth/register

Register a new teacher account.

**Request**:
```json
{
  "email": "string (required, valid email)",
  "phoneNumber": "string (required, Cambodia format)",
  "password": "string (required, meets strength requirements)",
  "preferredLanguage": "en | km (optional, default: en)"
}
```

**Success Response** (200 OK):
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "userId": "uuid",
    "email": "teacher@example.com",
    "phoneNumber": "+855123456789",
    "preferredLanguage": "en",
    "token": "jwt-access-token",
    "refreshToken": "jwt-refresh-token",
    "expiresIn": 900,
    "createdAt": "2025-11-25T14:00:00Z",
    "lastLoginAt": "2025-11-25T14:00:00Z"
  }
}
```

**Error Responses**:

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| DUPLICATE_EMAIL | 409 | Email already registered |
| DUPLICATE_PHONE | 409 | Phone number already registered |
| INVALID_INPUT | 400 | Invalid request format |
| PASSWORD_TOO_SHORT | 400 | Password < 8 characters |
| PASSWORD_MISSING_UPPERCASE | 400 | No uppercase letter |
| PASSWORD_MISSING_LOWERCASE | 400 | No lowercase letter |
| PASSWORD_MISSING_DIGIT | 400 | No digit |
| PASSWORD_MISSING_SPECIAL | 400 | No special character |
| WEAK_PASSWORD | 400 | Password doesn't meet requirements |

---

### POST /api/auth/refresh

Exchange refresh token for new access token.

**Request**:
```json
{
  "refreshToken": "string (required)"
}
```

**Note**: With HTTP-only cookies, refresh token is sent automatically via cookie. Request body may be empty.

**Success Response** (200 OK):
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "accessToken": "new-jwt-access-token",
    "refreshToken": "new-jwt-refresh-token",
    "expiresIn": 900
  }
}
```

**Error Responses**:

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| UNAUTHORIZED | 401 | Invalid or expired refresh token |

---

### POST /api/auth/logout

Invalidate tokens and end session.

**Headers**:
```
Authorization: Bearer <access_token>
```

**Request**: Empty body

**Success Response** (200 OK):
```json
{
  "errorCode": "SUCCESS",
  "data": null
}
```

**Error Responses**:

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| UNAUTHORIZED | 401 | Invalid or missing token |

---

### POST /api/auth/forgot-password

Request password reset email.

**Request**:
```json
{
  "email": "string (required, valid email)"
}
```

**Success Response** (200 OK):
```json
{
  "errorCode": "SUCCESS",
  "data": null
}
```

**Note**: Returns success even if email not found (for security).

**Error Responses**:

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| INVALID_INPUT | 400 | Invalid email format |
| RATE_LIMITED | 429 | Too many reset requests |

---

### POST /api/auth/reset-password

Reset password using token from email.

**Request**:
```json
{
  "token": "string (required, from email link)",
  "newPassword": "string (required, meets strength requirements)"
}
```

**Success Response** (200 OK):
```json
{
  "errorCode": "SUCCESS",
  "data": null
}
```

**Error Responses**:

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| RESET_TOKEN_INVALID | 400 | Token not found or invalid |
| RESET_TOKEN_EXPIRED | 400 | Token has expired |
| PASSWORD_TOO_SHORT | 400 | Password < 8 characters |
| WEAK_PASSWORD | 400 | Password doesn't meet requirements |

---

### GET /api/auth/me (Proposed New Endpoint)

Get current authenticated user profile. Used for session validation on app load.

**Headers**:
```
Authorization: Bearer <access_token>
```

Or with HTTP-only cookies: Cookies sent automatically

**Success Response** (200 OK):
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "userId": "uuid",
    "email": "teacher@example.com",
    "phoneNumber": "+855123456789",
    "preferredLanguage": "en",
    "createdAt": "2025-01-15T10:30:00Z",
    "lastLoginAt": "2025-11-25T14:00:00Z"
  }
}
```

**Error Responses**:

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| UNAUTHORIZED | 401 | Not authenticated or session expired |

---

## HTTP-Only Cookie Implementation

When HTTP-only cookies are enabled, the following changes apply:

### Login Response Headers
```
Set-Cookie: access_token=<jwt>; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=900
Set-Cookie: refresh_token=<jwt>; HttpOnly; Secure; SameSite=Strict; Path=/api/auth/refresh; Max-Age=2592000
```

### Logout Response Headers
```
Set-Cookie: access_token=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0
Set-Cookie: refresh_token=; HttpOnly; Secure; SameSite=Strict; Path=/api/auth/refresh; Max-Age=0
```

### Frontend Configuration
```typescript
// axios instance
const apiClient = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true, // Required for cookies
})
```

### CORS Configuration (Backend)
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173")); // Vite dev server
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // Required for cookies
        // ...
    }
}
```

## Error Code Reference

### Common Error Codes (ErrorCode)

| Code | Description |
|------|-------------|
| SUCCESS | Operation completed successfully |
| INVALID_INPUT | Request validation failed |
| NOT_FOUND | Resource not found |
| UNAUTHORIZED | Authentication required |
| FORBIDDEN | Permission denied |
| INTERNAL_ERROR | Server error |
| RATE_LIMITED | Too many requests |

### Auth-Specific Error Codes (AuthErrorCode)

| Code | Description |
|------|-------------|
| INVALID_CREDENTIALS | Wrong email/phone or password |
| ACCOUNT_LOCKED | Account locked after failed attempts |
| DUPLICATE_EMAIL | Email already registered |
| DUPLICATE_PHONE | Phone already registered |
| INVALID_PASSWORD | Generic password error |
| WEAK_PASSWORD | Password doesn't meet requirements |
| PASSWORD_TOO_SHORT | Less than 8 characters |
| PASSWORD_MISSING_UPPERCASE | No uppercase letter |
| PASSWORD_MISSING_LOWERCASE | No lowercase letter |
| PASSWORD_MISSING_DIGIT | No digit (0-9) |
| PASSWORD_MISSING_SPECIAL | No special character |
| PASSWORD_TOO_COMMON | Password in blacklist |
| USER_NOT_FOUND | User ID not found |
| EMAIL_NOT_FOUND | Email not found |
| RESET_TOKEN_INVALID | Password reset token invalid |
| RESET_TOKEN_EXPIRED | Password reset token expired |

## Request Examples (cURL)

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"emailOrPhone": "teacher@example.com", "password": "Password123!"}'
```

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teacher@example.com",
    "phoneNumber": "+855123456789",
    "password": "Password123!",
    "preferredLanguage": "en"
  }'
```

### Forgot Password
```bash
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "teacher@example.com"}'
```

### Reset Password
```bash
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"token": "reset-token-from-email", "newPassword": "NewPassword456!"}'
```
