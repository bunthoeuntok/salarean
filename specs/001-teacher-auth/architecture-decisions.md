# Architecture Decisions: Teacher Authentication

**Feature**: 001-teacher-auth | **Date**: 2025-11-20 | **Updated**: 2025-11-20

## Overview

This document captures key architectural decisions made during the planning phase of the teacher authentication feature.

---

## Decision 1: Internationalization (i18n) - Frontend vs Backend

### Context

The feature requires bilingual error messages (Khmer and English) per FR-011, FR-012, FR-013. Two approaches were considered:

1. **Backend i18n**: Spring MessageSource with `messages_en.properties` and `messages_km.properties`
2. **Frontend i18n**: Backend returns English messages + error codes, frontend translates

### Decision

**✅ Frontend handles all i18n translation**

### Rationale

**Backend Responsibility:**
- Return machine-readable error codes (ErrorCode enum) ONLY
- No error messages in API responses (maximally simplified)
- Language-agnostic API

**Frontend Responsibility:**
- Translate error codes to Khmer/English based on user preference
- Display localized messages to users
- Handle contextual error details (e.g., "The email **user@example.com** is already registered")

### Benefits

✅ **Maximally simplified backend**
- No `Accept-Language` header parsing
- No MessageSource configuration
- No locale management
- No error message strings in responses
- Smaller codebase

✅ **Faster iteration**
- Change translations without backend deployment
- Frontend can A/B test different phrasings
- Update messages without touching backend

✅ **Better UX**
- Frontend can add contextual details to error messages
- Consistent with modern SPA architecture (React/Next.js i18n libraries)
- Error codes work for any language (future-proof for Vietnamese, Thai, etc.)

✅ **API flexibility**
- Language-agnostic REST API
- Error codes enable machine-readable integration for API clients
- Minimal response payload (smaller network transfer)
- Follows REST best practices

### Implementation

**Backend Example:**
```java
// auth-service exception handler (error code only, no message)
@ExceptionHandler(DuplicateEmailException.class)
public ResponseEntity<BaseResponse<Object>> handleDuplicateEmail(DuplicateEmailException ex) {
    return ResponseEntity.badRequest()
        .body(BaseResponse.error(ErrorCode.DUPLICATE_EMAIL));
}

// Response: {"errorCode": "DUPLICATE_EMAIL", "data": null}
```

**Frontend Example (React/Next.js):**
```javascript
const errorTranslations = {
  en: {
    DUPLICATE_EMAIL: "This email is already registered",
    INVALID_PASSWORD: "Password must be at least 8 characters..."
  },
  km: {
    DUPLICATE_EMAIL: "អ៊ីមែលនេះត្រូវបានចុះឈ្មោះរួចហើយ",
    INVALID_PASSWORD: "ពាក្យសម្ងាត់ត្រូវមានយ៉ាងហោចណាស់ 8 តួអក្សរ..."
  }
};

// Display localized message based on error code
const localizedMessage = errorTranslations[userLanguage][response.errorCode];
```

### Alternatives Considered

❌ **Backend i18n with Spring MessageSource**
- Pros: Centralized translations, Spring Boot standard
- Cons: Adds complexity, couples translations to backend deployments, requires locale parsing
- **Why rejected**: Unnecessary backend complexity when frontend can handle it more efficiently

❌ **Database-stored translations**
- Pros: Dynamic translation updates
- Cons: Requires additional infrastructure, overkill for static error messages
- **Why rejected**: Over-engineering for simple error messages

### Consequences

**Positive:**
- Backend codebase simplified (no i18n dependencies)
- Frontend has full control over localization
- Easy to add new languages (only frontend changes)
- Error codes provide clear integration contract

**Negative:**
- Frontend must maintain translation files (acceptable trade-off)
- Error code changes require frontend updates (mitigated by versioned API contracts)

### References

- Modern SPA architecture patterns (Next.js, React i18next)
- REST API best practices (language-agnostic error codes)
- OpenAPI contracts: `specs/001-teacher-auth/contracts/*.yaml`

---

## Decision 2: BaseResponse Wrapper in Services vs API Gateway

### Context

User requested standardized response format: `{error: ErrorCodeEnum, errorMessage: string, data: T}`

Two approaches considered:

1. **Each service wraps responses** in BaseResponse format
2. **API Gateway wraps all responses** from services

### Decision

**✅ Each service implements BaseResponse wrapper**

### Rationale

**Service Responsibilities:**
- Business logic errors with BaseResponse format
- Service-specific error codes (DUPLICATE_EMAIL, STUDENT_NOT_FOUND, etc.)
- Localized error messages (now English-only per Decision 1)

**Gateway Responsibilities:**
- Gateway-specific errors (503 Service Unavailable, routing failures)
- Cross-cutting concerns (trace IDs, request logging)
- JWT authentication
- Rate limiting (global)

### Benefits

✅ **Service autonomy**
- Each service controls its error codes and messages
- Services can evolve independently
- OpenAPI contracts remain accurate

✅ **Type safety**
- `BaseResponse<AuthResponse>` provides compile-time type safety
- OpenAPI schemas match actual responses

✅ **Simpler gateway**
- Gateway doesn't need to know all service error codes
- No complex response transformation logic

### Implementation

**Each Service:**
```java
// auth-service/src/main/java/com/sms/auth/dto/BaseResponse.java
public class BaseResponse<T> {
    private ErrorCode error;
    private String errorMessage;
    private T data;
}
```

**API Gateway (gateway-specific errors only):**
```java
// api-gateway exception handler for gateway errors only
@ExceptionHandler(ServiceUnavailableException.class)
public ResponseEntity<BaseResponse<Object>> handleServiceUnavailable(ServiceUnavailableException ex) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(BaseResponse.error(ErrorCode.SERVICE_UNAVAILABLE, "Service temporarily unavailable"));
}
```

### Alternatives Considered

❌ **Gateway wraps all service responses**
- Pros: Central control over response format
- Cons: Gateway needs knowledge of all service error codes, breaks service autonomy, complex transformation logic
- **Why rejected**: Violates microservices principles, adds tight coupling

### Consequences

**Positive:**
- Services remain autonomous
- Clear separation of concerns (gateway = routing, services = business logic)
- Follows Spring Cloud microservices best practices

**Negative:**
- BaseResponse code duplicated across services (mitigated by shared library in future if needed)

### References

- Spring Cloud Gateway documentation
- Microservices architecture patterns
- Project constitution: Principle I (Microservices-First)

---

## Summary

Both decisions align with project constitution principles:

- **Simplicity (YAGNI)**: Frontend i18n avoids backend complexity
- **Microservices-First**: Service-level BaseResponse maintains autonomy
- **Security-First**: No impact on security posture
- **Observability**: Error codes enable better monitoring
- **Test Discipline**: Simpler backend = easier to test

All decisions documented in:
- `research.md` (technical details)
- `quickstart.md` (implementation guide)
- `contracts/*.yaml` (API specifications)
