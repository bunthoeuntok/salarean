# SMS Common Library

**Version**: 1.0.0
**Java Version**: 21
**Spring Boot**: 3.5.7

Shared utilities and DTOs for Salarean School Management System microservices.

---

## 📦 **What's Included**

### **1. API Response (DTO)**
- `ApiResponse<T>` - Standard response wrapper for all services
- `ErrorCode` - Common error codes for consistent error handling

### **2. Utilities**
- `DateUtils` - Cambodia-specific date/time operations (academic year, age calculation)
- `FileUtils` - Security-focused file operations (anti-spoofing, path traversal prevention)

### **3. Constants**
- `CommonConstants` - Shared constants (file limits, pagination, security settings)

### **4. Validation**
- `@KhmerPhone` - Cambodia phone number validation

---

## 🚀 **Installation**

### **Add to your service's pom.xml**

```xml
<dependency>
    <groupId>com.sms</groupId>
    <artifactId>sms-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

### **Build and install to local Maven repository**

```bash
cd sms-common
mvn clean install
```

---

## 📚 **Usage Examples**

### **1. API Response**

```java
import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.ErrorCode;

// Success response
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<Student>> getStudent(@PathVariable UUID id) {
    Student student = studentService.findById(id);
    return ResponseEntity.ok(ApiResponse.success(student));
}

// Error response
@ExceptionHandler(StudentNotFoundException.class)
public ResponseEntity<ApiResponse<Object>> handleNotFound(StudentNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ErrorCode.RESOURCE_NOT_FOUND));
}
```

---

### **2. DateUtils - Academic Year**

```java
import com.sms.common.util.DateUtils;

// Get current academic year
String currentYear = DateUtils.getCurrentAcademicYear();  // "2024-2025"

// Get academic year boundaries
LocalDate start = DateUtils.getAcademicYearStart("2024-2025");  // 2024-11-01
LocalDate end = DateUtils.getAcademicYearEnd("2024-2025");      // 2025-08-31

// Check if date is in academic year
boolean inYear = DateUtils.isInAcademicYear(LocalDate.now(), "2024-2025");
```

---

### **3. DateUtils - Age Validation**

```java
import com.sms.common.util.DateUtils;

// Calculate student age
LocalDate dob = LocalDate.of(2013, 5, 15);
int age = DateUtils.calculateAge(dob);  // Returns current age

// Validate age eligibility for Grade 1 (minimum 6 years old)
LocalDate enrollmentDate = LocalDate.of(2024, 11, 1);
boolean eligible = DateUtils.isAgeEligible(dob, enrollmentDate, 6);  // true

// Validate date of birth within reasonable range
boolean validDob = DateUtils.isValidDateOfBirth(dob, 5, 25);  // Age between 5-25
```

---

### **4. DateUtils - Khmer Formatting**

```java
import com.sms.common.util.DateUtils;

LocalDate today = LocalDate.now();

// Khmer date format (dd-MM-yyyy)
String khmerDate = DateUtils.formatKhmerDate(today);  // "23-11-2025"

// Display format (dd MMM yyyy)
String displayDate = DateUtils.formatDisplayDate(today);  // "23 Nov 2025"
```

---

### **5. FileUtils - Photo Validation (Security)**

```java
import com.sms.common.util.FileUtils;
import org.springframework.web.multipart.MultipartFile;

@PostMapping("/upload-photo")
public ResponseEntity<ApiResponse<String>> uploadPhoto(@RequestParam MultipartFile photo) {

    // 1. Validate file size (max 5MB)
    if (!FileUtils.isValidPhotoSize(photo)) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ErrorCode.PHOTO_SIZE_EXCEEDED));
    }

    // 2. Detect actual MIME type (security - prevents spoofing)
    String mimeType = FileUtils.detectMimeType(photo);

    // 3. Validate MIME type (JPEG/PNG/WebP only)
    if (!FileUtils.isValidPhotoMimeType(photo)) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ErrorCode.INVALID_PHOTO_FORMAT));
    }

    // 4. Verify extension matches content (anti-spoofing)
    String filename = photo.getOriginalFilename();
    if (!FileUtils.isExtensionMatchingContent(filename, mimeType)) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ErrorCode.CORRUPTED_IMAGE));
    }

    // 5. Generate safe filename
    UUID studentId = UUID.randomUUID();
    String extension = FileUtils.getExtensionFromMimeType(mimeType);
    String safeFilename = FileUtils.generateFilename(studentId, extension);
    // Result: "550e8400-e29b-41d4-a716-446655440000_20250123143000.jpg"

    // 6. Build safe path (prevents path traversal)
    Path baseDir = Paths.get("/app/uploads/students");
    Path safePath = FileUtils.buildSafePath(baseDir, safeFilename);

    // 7. Save file
    FileUtils.saveFile(photo, safePath);

    return ResponseEntity.ok(ApiResponse.success(safeFilename));
}
```

---

### **6. FileUtils - Filename Sanitization**

```java
import com.sms.common.util.FileUtils;

// Sanitize filename (security - prevents path traversal)
String unsafe = "../../etc/passwd";
String safe = FileUtils.sanitizeFilename(unsafe);  // "etcpasswd"

String unsafe2 = "../../../photo.jpg";
String safe2 = FileUtils.sanitizeFilename(unsafe2);  // "photo.jpg"

String unsafe3 = "my photo (1).jpg";
String safe3 = FileUtils.sanitizeFilename(unsafe3);  // "my_photo_1.jpg"
```

---

### **7. Validation - @KhmerPhone**

```java
import com.sms.common.validation.KhmerPhone;
import jakarta.validation.constraints.NotBlank;

public class ParentContactRequest {

    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    @KhmerPhone  // Validates Cambodia phone format
    private String phoneNumber;

    // Valid examples:
    // - +855 12 345 678
    // - +85512345678
    // - 012 345 678
    // - 012345678
}
```

---

### **8. CommonConstants**

```java
import com.sms.common.constants.CommonConstants;

// File size limits
long maxPhotoSize = CommonConstants.MAX_PHOTO_SIZE_BYTES;  // 5MB

// Pagination
int defaultPageSize = CommonConstants.DEFAULT_PAGE_SIZE;  // 20
int maxPageSize = CommonConstants.MAX_PAGE_SIZE;          // 100

// Security - JWT
int jwtExpirationHours = CommonConstants.JWT_EXPIRATION_HOURS;  // 24

// Academic year
int academicYearStart = CommonConstants.ACADEMIC_YEAR_START_MONTH;  // 11 (November)

// Age limits
int minGrade1Age = CommonConstants.MIN_GRADE_1_AGE;  // 6 years
```

---

## 🔒 **Security Features**

### **FileUtils Security**

1. **Anti-Spoofing** (Apache Tika)
   - Detects actual file content, not just extension
   - Prevents `.exe` files disguised as `.jpg`

2. **Path Traversal Prevention**
   - `sanitizeFilename()` removes `../`, `./`, null bytes
   - `buildSafePath()` ensures files stay within allowed directories

3. **Extension Verification**
   - `isExtensionMatchingContent()` validates extension matches MIME type

---

## 🇰🇭 **Cambodia-Specific Features**

### **DateUtils**

- **Academic Year**: November 1 - August 31
- **Timezone**: Asia/Phnom_Penh (UTC+7)
- **Khmer Date Format**: dd-MM-yyyy
- **Age Eligibility**: Validates minimum age for school enrollment (6 years for Grade 1)

### **Validation**

- **@KhmerPhone**: Validates Cambodia phone formats (+855 or 0, operator codes 1-9)

---

## 📝 **Error Handling Best Practices**

### **Service-Specific Error Codes**

Each service should create its own error code enum for service-specific errors:

```java
// In auth-service
public enum AuthErrorCode {
    DUPLICATE_EMAIL,
    DUPLICATE_PHONE,
    WEAK_PASSWORD,
    PASSWORD_TOO_SHORT,
    ACCOUNT_LOCKED
}

// In student-service
public enum StudentErrorCode {
    STUDENT_NOT_FOUND,
    DUPLICATE_STUDENT_CODE,
    CLASS_CAPACITY_EXCEEDED
}
```

**Common codes** (from `ErrorCode`) handle generic errors:
- `VALIDATION_ERROR`
- `UNAUTHORIZED`
- `RESOURCE_NOT_FOUND`
- `PHOTO_SIZE_EXCEEDED`

---

## 🧪 **Testing**

```bash
# Run unit tests
mvn test

# Build without tests
mvn clean install -DskipTests

# Build with tests
mvn clean install
```

---

## 📦 **Dependencies**

- **Lombok** (1.18.36) - Reduce boilerplate
- **Apache Tika** (2.9.1) - MIME type detection (security)
- **Jakarta Validation** (3.0.2) - Bean validation
- **Spring Web** (6.2.1, provided) - MultipartFile support

---

## 🎯 **Design Principles**

1. **Cambodia-Specific** - Focus on local business rules (academic year, phone formats)
2. **Security-First** - File operations prioritize security (anti-spoofing, path traversal prevention)
3. **Service Independence** - Avoid coupling between services
4. **Single Source of Truth** - Constants defined once, used everywhere
5. **Minimal Dependencies** - Only essential libraries

---

## 🚫 **What's NOT Included**

### **Not Shared (By Design)**

- **JWT Token Generation/Validation** - Each service validates independently
- **Configuration Classes** - Service-specific (use templates in `.standards/`)
- **Domain Models** - Violates bounded context principle
- **Service Business Logic** - Belongs in individual services

---

## 📖 **Related Documentation**

- **Microservice Standards**: `.standards/README.md`
- **Service Creation Guide**: `.standards/docs/quickstart-service-creation.md`
- **Claude Development Guidelines**: `CLAUDE.md`

---

## 👥 **Maintainers**

Salarean Development Team

---

## 📄 **License**

Internal use only - Salarean SMS Project
