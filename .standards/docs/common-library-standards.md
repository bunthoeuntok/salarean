# Common Library Standards (sms-common)

**Version**: 1.0.0
**Last Updated**: 2025-11-23
**Status**: Active

---

## Overview

The `sms-common` library is a shared Maven dependency that provides reusable DTOs, utilities, constants, and validation annotations used across ALL microservices in the Student Management System.

**Maven Coordinates**:
```xml
<dependency>
    <groupId>com.sms</groupId>
    <artifactId>sms-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Location**: `/sms-common/`

---

## Core Principle: What Belongs in sms-common?

### ✅ **INCLUDE** in sms-common

**Shared across ALL or MOST services:**

1. **API Standards**
   - `ApiResponse<T>` - Standard response wrapper
   - `ErrorCode` enum - Common error codes (SUCCESS, INVALID_INPUT, etc.)

2. **Business Domain Constants**
   - Academic year settings (start/end dates for Cambodia)
   - Age validation limits (min kindergarten age, Grade 1 age, etc.)
   - Date/time formats (Khmer date format, Cambodia timezone)
   - Supported languages (English, Khmer)

3. **File Upload Standards**
   - File size limits (MAX_PHOTO_SIZE_BYTES, MAX_DOCUMENT_SIZE_BYTES)
   - Allowed MIME types (ALLOWED_IMAGE_TYPES, ALLOWED_DOCUMENT_TYPES)
   - Note: File upload limits are business rules that apply across all services

4. **Pagination Defaults**
   - DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE, MIN_PAGE_SIZE

5. **HTTP Standards**
   - Header names (HEADER_AUTHORIZATION, HEADER_ACCEPT_LANGUAGE)
   - Bearer token prefix

6. **Common Utilities**
   - `DateUtils` - Cambodia-specific date operations (academic year calculation, age validation)
   - `FileUtils` - Security-focused file operations (MIME type detection, path traversal prevention)

7. **Shared Validation Annotations**
   - `@KhmerPhone` - Cambodia phone number validation
   - Other domain-specific validators used by multiple services

---

### ❌ **EXCLUDE** from sms-common

**Service-specific logic that varies between services:**

1. **Security Policies**
   - JWT expiration times (auth-service may use 24h, other services may differ)
   - Session timeout settings
   - Password strength requirements
   - Rate limiting thresholds
   - Maximum login attempts

2. **Service-Specific Business Rules**
   - Attendance policies (attendance-service)
   - Grading scales (grade-service)
   - Report templates (report-service)

3. **Cache Strategies**
   - Cache TTL values (each service defines based on its data volatility)

4. **Service-Specific Error Codes**
   - Add service-specific codes to each service's ErrorCode extension
   - Only add to common ErrorCode if used by 3+ services

5. **Implementation Details**
   - Database configurations
   - External API integrations
   - Service-specific algorithms

---

## Package Structure

```
sms-common/
├── pom.xml
└── src/main/java/com/sms/common/
    ├── constants/
    │   └── CommonConstants.java        # Business constants shared across services
    ├── dto/
    │   ├── ApiResponse.java            # Standard API response wrapper
    │   └── ErrorCode.java              # Common error code enum
    ├── util/
    │   ├── DateUtils.java              # Cambodia-specific date utilities
    │   └── FileUtils.java              # Security-focused file utilities
    └── validation/
        ├── KhmerPhone.java             # Phone validation annotation
        └── KhmerPhoneValidator.java    # Validator implementation
```

---

## Constants Organization Rules

### Rule 1: No Duplication Between Utils and Constants

**Problem**: Duplicate constants in both `DateUtils.java` and `CommonConstants.java`

**Solution**: Utils should REFERENCE CommonConstants, not duplicate them

**Example - DateUtils.java**:
```java
// ❌ WRONG - Duplicating constants
public class DateUtils {
    public static final String TIMEZONE_CAMBODIA = "Asia/Phnom_Penh";
    public static final String DATE_FORMAT_KHMER = "dd-MM-yyyy";
}

// ✅ CORRECT - Referencing CommonConstants
public class DateUtils {
    public static final ZoneId CAMBODIA_ZONE =
        ZoneId.of(CommonConstants.TIMEZONE_CAMBODIA);

    public static final DateTimeFormatter KHMER_DATE_FORMAT =
        DateTimeFormatter.ofPattern(CommonConstants.DATE_FORMAT_KHMER);
}
```

### Rule 2: Service-Specific Constants in Service Config Classes

**Problem**: JWT expiration, rate limiting, password rules in CommonConstants

**Solution**: Each service defines its own security properties

**Example - auth-service**:
```java
// auth-service/src/main/java/com/sms/auth/config/SecurityProperties.java
public final class SecurityProperties {
    // JWT Settings (auth-service specific)
    public static final int JWT_EXPIRATION_HOURS = 24;
    public static final int REFRESH_TOKEN_EXPIRATION_DAYS = 30;

    // Rate Limiting (auth-service specific)
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int ACCOUNT_LOCK_DURATION_MINUTES = 15;

    // Password Validation (auth-service specific)
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 128;
}
```

**Rationale**: Different services may have different security requirements. For example:
- auth-service might use 24-hour JWT tokens
- reporting-service might use 7-day JWT tokens for background jobs
- admin-service might enforce stricter password requirements

### Rule 3: The "3-Service Rule" for Error Codes

**When to add error codes to CommonConstants.ErrorCode**:
- ✅ Used by 3 or more services → Add to common
- ❌ Used by 1-2 services → Keep service-specific

**Example**:
```java
// Common ErrorCode (in sms-common)
public enum ErrorCode {
    SUCCESS,
    INVALID_INPUT,
    UNAUTHORIZED,
    NOT_FOUND,
    INTERNAL_ERROR,
    // ... codes used across multiple services
}

// Service-specific extension (in auth-service)
public enum AuthErrorCode {
    DUPLICATE_EMAIL,      // Only auth-service validates email uniqueness
    DUPLICATE_PHONE,      // Only auth-service validates phone uniqueness
    PASSWORD_TOO_WEAK,    // Only auth-service enforces password strength
    TOKEN_REPLAY_DETECTED // Only auth-service detects token replay
}
```

---

## CommonConstants.java Structure

**Current sections** (in order):

1. **File Upload Limits** - MAX_PHOTO_SIZE_BYTES, MAX_DOCUMENT_SIZE_BYTES, MAX_VIDEO_SIZE_BYTES
2. **Pagination** - DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE, MIN_PAGE_SIZE
3. **Date/Time Formats** - DATE_FORMAT_ISO, DATE_FORMAT_KHMER, TIMEZONE_CAMBODIA
4. **Validation - Age Limits** - MIN_KINDERGARTEN_AGE, MIN_GRADE_1_AGE, MAX_HIGH_SCHOOL_AGE
5. **Academic Year (Cambodia)** - ACADEMIC_YEAR_START_MONTH/DAY, END_MONTH/DAY
6. **HTTP Headers** - HEADER_AUTHORIZATION, BEARER_PREFIX, HEADER_CONTENT_TYPE
7. **Supported Languages** - LANG_ENGLISH, LANG_KHMER, DEFAULT_LANGUAGE
8. **File MIME Types** - ALLOWED_IMAGE_TYPES, ALLOWED_DOCUMENT_TYPES

**What was REMOVED** (now service-specific):
- ❌ JWT expiration settings → Moved to `auth-service/SecurityProperties.java`
- ❌ Session timeout settings → Moved to `auth-service/SecurityProperties.java`
- ❌ Password validation rules → Moved to `auth-service/SecurityProperties.java`
- ❌ Rate limiting thresholds → Moved to `auth-service/SecurityProperties.java`
- ❌ Cache TTL values → Each service defines its own

---

## Adding New Constants - Decision Tree

```
Is this constant used by multiple services?
├── YES → Is it a business rule or domain logic?
│   ├── YES → Add to CommonConstants
│   └── NO → Is it technical configuration (JWT, cache, etc.)?
│       ├── YES → Keep in service-specific config
│       └── NO → Consider if it's truly needed
└── NO → Keep in service-specific config
```

**Examples**:

| Constant | Decision | Rationale |
|----------|----------|-----------|
| Cambodia timezone | ✅ CommonConstants | All services need Cambodia time |
| Academic year dates | ✅ CommonConstants | Core business domain rule |
| Max photo size | ✅ CommonConstants | Business rule applying to all file uploads |
| JWT expiration | ❌ Service-specific | Auth policies vary by service |
| Cache TTL | ❌ Service-specific | Each service has different data volatility |
| Max login attempts | ❌ Service-specific | Security policies vary by service |

---

## Updating sms-common

### When to Update

Update `sms-common` when:
1. Adding new shared DTOs (e.g., common request/response wrappers)
2. Adding new domain-wide error codes (used by 3+ services)
3. Adding new shared validation annotations
4. Adding new utility methods used by multiple services
5. Updating business constants (academic year, age limits, etc.)

### Build and Install Process

```bash
cd /path/to/salarean/sms-common

# Build and install to local Maven repository
./mvnw clean install -DskipTests

# Verify installation
ls ~/.m2/repository/com/sms/sms-common/1.0.0/
```

### Versioning Strategy

**Current**: Version 1.0.0 (not using SNAPSHOT for Docker build compatibility)

**Update strategy**:
- Breaking changes → Bump major version (2.0.0)
- New features → Bump minor version (1.1.0)
- Bug fixes → Bump patch version (1.0.1)

**When updating version**:
1. Update `sms-common/pom.xml` version
2. Update all service dependencies (auth-service, student-service, etc.)
3. Rebuild sms-common: `./mvnw clean install`
4. Rebuild affected services

---

## Docker Build Considerations

### Optimized Build Pattern (MANDATORY)

Services that depend on `sms-common` MUST use the optimized Docker build pattern to avoid rebuilding sms-common multiple times.

**Architecture**:
1. **sms-common-builder** - Base image with sms-common pre-installed
2. **Service Dockerfiles** - Reference the base image instead of building sms-common

**Service Dockerfile Pattern**:

```dockerfile
# Stage 1: Use pre-built sms-common library
FROM sms-common-builder:latest AS common-builder

# Stage 2: Build service (with access to sms-common from base image)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
RUN apk add --no-cache maven

# Copy sms-common from base image's Maven repository
COPY --from=common-builder /root/.m2/repository/com/sms/sms-common \
     /root/.m2/repository/com/sms/sms-common

COPY auth-service/pom.xml .
RUN mvn dependency:go-offline -B
COPY auth-service/src src
RUN mvn clean package -DskipTests -B

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine
# ... copy JAR and run
```

**Benefits**:
- ✅ sms-common built once (not per service)
- ✅ ~53% faster builds (7 min vs 15 min for 3 services)
- ✅ Cleaner Dockerfiles (1 line vs 11 lines)
- ✅ Single source of truth for sms-common build

**Build Commands**:

```bash
# 1. Build base image first (only when sms-common changes)
docker-compose build sms-common-builder

# 2. Build services (they'll use the cached base image)
docker-compose build auth-service student-service
```

**For Complete Details**: See `.standards/docs/docker-build-optimization.md`

### Docker Compose Context

Services using `sms-common` need parent directory context:

```yaml
# docker-compose.yml
services:
  sms-common-builder:
    build:
      context: ./sms-common
      dockerfile: Dockerfile
    image: sms-common-builder:latest
    profiles:
      - build-only

  auth-service:
    build:
      context: .              # Parent directory (not ./auth-service)
      dockerfile: ./auth-service/Dockerfile
```

**Rationale**: Dockerfile needs access to both `sms-common/` and `auth-service/` directories.

---

## Migration Checklist

When refactoring constants from services to sms-common:

- [ ] **Identify duplicates** - Check if constant exists in multiple services
- [ ] **Verify business logic** - Is this a domain rule or service-specific config?
- [ ] **Add to CommonConstants** - Place in appropriate section with JavaDoc
- [ ] **Update Utils** - Reference CommonConstants instead of duplicating
- [ ] **Update services** - Replace hard-coded values with CommonConstants
- [ ] **Rebuild sms-common** - `./mvnw clean install`
- [ ] **Test all services** - Verify no functionality broken
- [ ] **Update Docker builds** - Ensure multi-stage builds work
- [ ] **Document in CHANGELOG** - Record what was moved and why

---

## Best Practices

### 1. Documentation

**Every constant MUST have JavaDoc**:

```java
/** Maximum photo file size: 5MB */
public static final long MAX_PHOTO_SIZE_BYTES = 5 * 1024 * 1024;

/** Cambodia timezone: Asia/Phnom_Penh (UTC+7) */
public static final String TIMEZONE_CAMBODIA = "Asia/Phnom_Penh";
```

### 2. Naming Conventions

- Constants: `UPPER_SNAKE_CASE`
- Utility classes: `final` class with `private` constructor
- Method names: `camelCase` and verb-based (e.g., `calculateAge`, `formatKhmerDate`)

### 3. Avoid Magic Numbers

**❌ Bad**:
```java
if (age >= 6) { // What is 6?
    // enroll in Grade 1
}
```

**✅ Good**:
```java
if (age >= CommonConstants.MIN_GRADE_1_AGE) {
    // enroll in Grade 1
}
```

### 4. Immutability

- All constants: `public static final`
- Utility classes: `final` with private constructor
- Arrays → Use immutable collections:

```java
// ❌ Mutable array
public static final String[] ALLOWED_TYPES = {"jpg", "png"};

// ✅ Immutable Set
public static final Set<String> ALLOWED_TYPES = Set.of("jpg", "png");
```

---

## Testing Strategy

### Unit Tests for Utilities

**Location**: `sms-common/src/test/java/com/sms/common/util/`

**Coverage requirements**:
- DateUtils: Test academic year calculation, age validation
- FileUtils: Test MIME detection, path traversal prevention
- Validators: Test edge cases (null, empty, invalid formats)

**Example**:
```java
@Test
void calculateAge_withValidDOB_returnsCorrectAge() {
    LocalDate dob = LocalDate.of(2010, 5, 15);
    LocalDate asOf = LocalDate.of(2025, 11, 23);

    int age = DateUtils.calculateAge(dob, asOf);

    assertEquals(15, age);
}
```

### Integration Tests in Services

**Verify** that services correctly use sms-common:
- Error codes map correctly
- Validation annotations work
- Constants are applied properly

---

## Troubleshooting

### Problem: "Could not find artifact com.sms:sms-common:jar:1.0.0"

**Cause**: sms-common not installed to local Maven repository

**Solution**:
```bash
cd sms-common
./mvnw clean install -DskipTests
```

### Problem: Docker build fails - "Failed to resolve dependency: sms-common"

**Cause**: Docker container doesn't have access to local Maven repo

**Solution**: Use multi-stage build (see "Docker Build Considerations" above)

### Problem: Tests fail after adding new constant

**Cause**: Service still uses hard-coded value instead of constant

**Solution**: Search codebase for magic numbers:
```bash
# Find hard-coded "24" (JWT expiration hours)
grep -r "\b24\b" auth-service/src/
```

---

## Related Documentation

- **Package Structure**: `.standards/docs/package-structure.md`
- **Configuration Patterns**: `.standards/docs/configuration-patterns.md`
- **Service Creation**: `.standards/docs/quickstart-service-creation.md`
- **Migration Guide**: `.standards/docs/migration-notes.md`

---

## Changelog

| Date | Version | Changes | Author |
|------|---------|---------|--------|
| 2025-11-23 | 1.0.0 | Initial common library standards created | Claude |

---

## Questions?

See `.standards/docs/FAQ.md` or raise an issue with the architecture team.
