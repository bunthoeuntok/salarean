# salarean Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-11-20

## Active Technologies
- Java 21 (Spring Boot 3.5.7) + Spring Boot, Spring Security, Spring Data JPA, Spring Data Redis, JWT (jjwt 0.12.5), BCrypt, Hibernate Validator, Lombok (002-jwt-auth)
- PostgreSQL 15+ (auth-service database), Redis 7+ (refresh token cache) (002-jwt-auth)

- Java 21 (Spring Boot 3.5.7) + Spring Boot, Spring Security, Spring Data JPA, JWT (jjwt 0.12.5), BCrypt for password hashing (001-teacher-auth)

## Project Structure

```text
src/
tests/
```

## Commands

# Add commands for Java 21 (Spring Boot 3.5.7)

## Code Style

Java 21 (Spring Boot 3.5.7): Follow standard conventions

## Recent Changes
- 002-jwt-auth: Added Java 21 (Spring Boot 3.5.7) + Spring Boot, Spring Security, Spring Data JPA, Spring Data Redis, JWT (jjwt 0.12.5), BCrypt, Hibernate Validator, Lombok

- 001-teacher-auth: Added Java 21 (Spring Boot 3.5.7) + Spring Boot, Spring Security, Spring Data JPA, JWT (jjwt 0.12.5), BCrypt for password hashing

<!-- MANUAL ADDITIONS START -->

## API Standards (MANDATORY)

**All backend services MUST follow these standards:**

### Response Format

All API endpoints MUST return responses in this standardized format:

```typescript
{
  errorCode: string,  // Error code for client-side i18n lookup, "SUCCESS" for successful operations
  data: T             // Response payload (type varies by endpoint), null on errors
}
```

**Success Example**:
```json
{
  "errorCode": "SUCCESS",
  "data": { "id": "123", "name": "John" }
}
```

**Error Example**:
```json
{
  "errorCode": "INVALID_INPUT",
  "data": null
}
```

### Internationalization (i18n) Policy

**Backend responsibilities**:
- Return ONLY machine-readable error codes (e.g., `INVALID_PHONE_FORMAT`, `UNAUTHORIZED`)
- Return data in neutral format (no human-readable messages)
- Store user language preference in profile

**Frontend responsibilities**:
- Maintain ALL internationalization (i18n) translations
- Map error codes to localized messages in user's preferred language
- Handle all UI text translations (English/Khmer)

**Rationale**: Separating i18n concerns keeps backend simple and maintainable. Frontend can dynamically update translations without backend changes.

### Error Code Standards

Error codes MUST be:
- UPPER_SNAKE_CASE format
- Self-documenting (e.g., `PASSWORD_TOO_SHORT`, not `ERR_001`)
- Consistent across all services
- Documented in feature specifications

<!-- MANUAL ADDITIONS END -->
