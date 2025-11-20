# Feature Specification: JWT Authentication, Authorization and User Profile Management

**Feature Branch**: `002-jwt-auth`
**Created**: 2025-11-20
**Status**: Draft
**Input**: User description: "JWT Authentication, Authorization and User Profile Management
**Acceptance Criteria:**
- [ ] Generate JWT access token (24h expiry)
- [ ] Generate refresh token (30d expiry)
- [ ] Store refresh token in Redis + database
- [ ] Validate JWT on all protected endpoints
- [ ] Refresh token endpoint
- [ ] Logout endpoint (invalidate tokens)
- [ ] View user profile
- [ ] Update profile (name, phone, photo)
- [ ] Change password
- [ ] Language preference (Khmer/English)
- [ ] Profile photo upload"

## Clarifications

### Session 2025-11-20

- Q: Should backend implement i18n (internationalization) for error messages? → A: No, frontend will handle all internationalization. Backend returns only error codes.
- Q: What is the standardized API response format? → A: All API responses must follow format: `{errorCode: string, data: T}`

## API Standards *(mandatory)*

### Response Format

**All API responses MUST follow this standardized format:**

```typescript
{
  errorCode: string,  // Error code for client-side i18n lookup, "SUCCESS" for successful operations
  data: T             // Response payload (type varies by endpoint), null on errors
}
```

**Success Response Example**:
```json
{
  "errorCode": "SUCCESS",
  "data": {
    "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "email": "teacher@school.edu.kh",
    "name": "Sok Dara"
  }
}
```

**Error Response Example**:
```json
{
  "errorCode": "INVALID_PHONE_FORMAT",
  "data": null
}
```

### Error Codes

Backend returns machine-readable error codes. Frontend is responsible for translating error codes to localized messages.

**Authentication Errors**:
- `INVALID_TOKEN` - Token is invalid or expired
- `TOKEN_REPLAY_DETECTED` - Refresh token has been reused (security incident)
- `UNAUTHORIZED` - Authentication required or failed

**Profile Errors**:
- `INVALID_PHONE_FORMAT` - Phone number doesn't match Cambodia format
- `DUPLICATE_PHONE` - Phone number already registered
- `PROFILE_UPDATE_FAILED` - Profile update operation failed

**Password Errors**:
- `INCORRECT_PASSWORD` - Current password verification failed
- `WEAK_PASSWORD` - New password doesn't meet strength requirements
- `PASSWORD_TOO_SHORT` - Password less than 8 characters
- `PASSWORD_MISSING_UPPERCASE` - Password missing uppercase letter
- `PASSWORD_MISSING_LOWERCASE` - Password missing lowercase letter
- `PASSWORD_MISSING_DIGIT` - Password missing digit
- `PASSWORD_MISSING_SPECIAL` - Password missing special character
- `PASSWORD_TOO_COMMON` - Password is in common password dictionary

**Photo Upload Errors**:
- `PHOTO_SIZE_EXCEEDED` - Photo size exceeds 5MB limit
- `INVALID_PHOTO_FORMAT` - Photo is not JPG or PNG
- `CORRUPTED_IMAGE` - Photo file is corrupted or invalid

**General Errors**:
- `VALIDATION_ERROR` - Generic validation error
- `INTERNAL_SERVER_ERROR` - Unexpected server error

### Internationalization Strategy

- **Backend**: Returns only error codes and data
- **Frontend**: Responsible for all internationalization (i18n)
- **Language Preference**: Stored in user profile, used by frontend to select appropriate translations
- **Error Messages**: Frontend maintains translation mappings for all error codes in both English and Khmer

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Secure Session Management (Priority: P1)

Teachers need secure, persistent sessions that automatically handle token refresh without interrupting their workflow, allowing them to focus on teaching activities rather than repeatedly logging in.

**Why this priority**: This is the foundation of the authentication system. Without reliable session management, users cannot access any protected features. This delivers the core value of keeping users securely authenticated.

**Independent Test**: Can be fully tested by logging in once, verifying the session persists across page refreshes and browser restarts (within token validity), and confirming automatic token refresh works without user intervention.

**Acceptance Scenarios**:

1. **Given** a teacher has valid credentials, **When** they log in successfully, **Then** they receive an access token (24h validity) and refresh token (30d validity) and remain authenticated across page refreshes
2. **Given** a teacher's access token is about to expire, **When** they make a request to a protected endpoint, **Then** the system automatically refreshes the access token using the refresh token without requiring re-authentication
3. **Given** a teacher has been inactive for 30 days, **When** their refresh token expires, **Then** they are redirected to login and must re-authenticate
4. **Given** a teacher wants to end their session, **When** they logout, **Then** both access and refresh tokens are invalidated and they can no longer access protected resources

---

### User Story 2 - Profile Viewing and Basic Updates (Priority: P2)

Teachers need to view and update their basic profile information (name, phone number) to keep their account information current and accurate for communication purposes.

**Why this priority**: After authentication, the most common user need is viewing and maintaining basic profile data. This provides immediate value by allowing users to verify and update their information.

**Independent Test**: Can be tested by logging in as a teacher, navigating to the profile page, viewing current information, updating the name and phone number, and verifying the changes persist across sessions.

**Acceptance Scenarios**:

1. **Given** an authenticated teacher, **When** they access their profile page, **Then** they see their current name, email, phone number, language preference, and profile photo
2. **Given** a teacher on their profile page, **When** they update their name and save, **Then** the new name is displayed immediately and persists across sessions
3. **Given** a teacher on their profile page, **When** they update their phone number with a valid Cambodia format, **Then** the new phone is saved and validated
4. **Given** a teacher on their profile page, **When** they enter an invalid phone number format, **Then** they receive an error code that the frontend translates to an appropriate message

---

### User Story 3 - Security Management (Priority: P2)

Teachers need to change their password to maintain account security, especially if they suspect their credentials have been compromised or want to strengthen their security.

**Why this priority**: Password management is a critical security feature that users expect. While not needed as frequently as profile viewing, it's essential for account security and user trust.

**Independent Test**: Can be tested by logging in, accessing the change password feature, providing current password and new password meeting strength requirements, and verifying the new password works for subsequent logins.

**Acceptance Scenarios**:

1. **Given** an authenticated teacher on the change password page, **When** they provide their current password and a new password meeting strength requirements, **Then** their password is updated and they can login with the new password
2. **Given** a teacher attempting to change password, **When** they provide an incorrect current password, **Then** they receive error code INCORRECT_PASSWORD
3. **Given** a teacher attempting to change password, **When** they provide a new password that doesn't meet strength requirements, **Then** they receive specific error codes (PASSWORD_TOO_SHORT, PASSWORD_MISSING_UPPERCASE, etc.) that frontend translates to detailed requirements
4. **Given** a teacher has just changed their password, **When** the change is successful, **Then** all existing sessions except the current one are invalidated

---

### User Story 4 - Localization Preference (Priority: P3)

Teachers can select their preferred language (Khmer or English) for the interface to ensure they can comfortably use the application in their language of choice.

**Why this priority**: While important for user experience, this is a preference setting that doesn't block core functionality. Most users will set this once and rarely change it.

**Independent Test**: Can be tested by logging in, changing the language preference from English to Khmer (or vice versa), and verifying the interface language updates immediately and persists across sessions.

**Acceptance Scenarios**:

1. **Given** an authenticated teacher, **When** they change their language preference to Khmer, **Then** the frontend immediately switches all interface text to Khmer using client-side i18n
2. **Given** a teacher has set language preference to Khmer, **When** they logout and login again, **Then** the frontend reads the saved preference and displays interface in Khmer automatically
3. **Given** a teacher on the profile page, **When** they select a language option, **Then** they can choose between Khmer ('km') and English ('en') only

---

### User Story 5 - Profile Photo Upload (Priority: P3)

Teachers can upload a profile photo to personalize their account and make their profile more recognizable to students and colleagues.

**Why this priority**: This is a nice-to-have feature that enhances personalization but is not critical for core functionality. It's valuable for user engagement but can be implemented after essential features.

**Independent Test**: Can be tested by logging in, accessing the profile photo upload feature, selecting a valid image file, uploading it, and verifying the photo displays on the profile and persists across sessions.

**Acceptance Scenarios**:

1. **Given** an authenticated teacher on their profile page, **When** they upload a valid image file (JPG, PNG) under 5MB, **Then** the image is saved and displayed as their profile photo
2. **Given** a teacher attempting to upload a photo, **When** they select a file that exceeds size limits or is not an accepted format, **Then** they receive appropriate error codes (PHOTO_SIZE_EXCEEDED, INVALID_PHOTO_FORMAT)
3. **Given** a teacher has uploaded a profile photo, **When** they upload a new photo, **Then** the old photo is replaced with the new one
4. **Given** a teacher with a profile photo, **When** they view their profile from any device, **Then** they see the same profile photo

---

### Edge Cases

- What happens when a teacher tries to refresh a token that has already been used to generate a new token (token replay)? → Returns errorCode: TOKEN_REPLAY_DETECTED, invalidates all sessions
- How does the system handle concurrent login sessions from multiple devices? → Each device gets independent refresh tokens, all sessions remain valid until explicitly logged out or token expires
- What happens when a teacher attempts to update their phone number to one already used by another teacher? → Returns errorCode: DUPLICATE_PHONE
- How does the system handle profile photo uploads that are corrupted or contain malicious content? → Returns errorCode: CORRUPTED_IMAGE after validation fails
- What happens when a teacher's session expires while they are in the middle of updating their profile? → Returns errorCode: INVALID_TOKEN, frontend redirects to login
- How does the system handle logout when the refresh token has already been invalidated or expired? → Returns errorCode: SUCCESS (idempotent operation)
- What happens when a teacher changes their password while logged in on multiple devices? → Current session remains valid, all other sessions invalidated
- How does the system handle timezone differences for token expiry (24h/30d)? → All timestamps stored and compared in UTC

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST generate a JWT access token with 24-hour expiration upon successful authentication
- **FR-002**: System MUST generate a refresh token with 30-day expiration upon successful authentication
- **FR-003**: System MUST store refresh tokens in both Redis (for fast lookup) and database (for persistence)
- **FR-004**: System MUST validate JWT access tokens on all protected endpoints before allowing access
- **FR-005**: System MUST provide a refresh token endpoint that generates a new access token when provided with a valid refresh token
- **FR-006**: System MUST provide a logout endpoint that invalidates both access and refresh tokens
- **FR-007**: System MUST prevent reuse of refresh tokens after they have been used to generate a new access token
- **FR-008**: System MUST allow authenticated users to view their complete profile including name, email, phone, language preference, and profile photo
- **FR-009**: System MUST allow authenticated users to update their name
- **FR-010**: System MUST allow authenticated users to update their phone number with Cambodia format validation
- **FR-011**: System MUST enforce password strength requirements when users change their password
- **FR-012**: System MUST require current password verification before allowing password changes
- **FR-013**: System MUST invalidate all other sessions when a user changes their password (except the current session)
- **FR-014**: System MUST allow users to select language preference between Khmer ('km') and English ('en')
- **FR-015**: System MUST persist language preference in user profile
- **FR-016**: System MUST allow users to upload profile photos in JPG or PNG format
- **FR-017**: System MUST validate profile photo file size (maximum 5MB)
- **FR-018**: System MUST sanitize and validate uploaded images to prevent malicious content
- **FR-019**: System MUST return machine-readable error codes in standardized response format {errorCode, data}
- **FR-020**: System MUST prevent phone number duplication across teacher accounts
- **FR-021**: System MUST handle token expiration gracefully by returning appropriate error codes
- **FR-022**: System MUST include user identity information in JWT tokens for authorization decisions
- **FR-023**: All API responses MUST follow standardized format: {errorCode: string, data: T}
- **FR-024**: Backend MUST NOT implement internationalization - all error codes are machine-readable strings for frontend i18n lookup

### Key Entities

- **User Profile**: Represents a teacher's personal information including name, email, phone number, language preference ('en' or 'km'), and profile photo URL. Links to authentication credentials and session tokens.
- **Access Token**: Short-lived authentication credential (24h) that proves user identity for API requests. Contains user ID, roles, and expiration timestamp.
- **Refresh Token**: Long-lived authentication credential (30d) used to obtain new access tokens. Stored securely and can be invalidated on logout or password change.
- **Session**: Represents an active user session linking a user to their current tokens. Tracks creation time, last activity, device information, and token validity status.
- **Profile Photo**: Image file associated with a user profile. Includes metadata like file size, format, upload timestamp, and storage location.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Teachers can remain authenticated for up to 30 days without re-entering credentials, with seamless token refresh occurring automatically
- **SC-002**: Token refresh operations complete in under 500 milliseconds without user-perceivable delay
- **SC-003**: 99% of profile updates (name, phone, language) save successfully on first attempt
- **SC-004**: Users can change their password in under 1 minute from start to completion
- **SC-005**: Profile photo uploads under 2MB complete in under 3 seconds
- **SC-006**: 95% of users successfully complete profile viewing on first attempt without errors
- **SC-007**: Logout operations successfully invalidate all tokens within 1 second
- **SC-008**: Zero unauthorized access attempts succeed after token invalidation or expiration
- **SC-009**: Language preference changes are saved and retrieved correctly (< 200ms), frontend applies language immediately
- **SC-010**: System correctly handles 1000 concurrent token refresh requests without failures
- **SC-011**: All API responses conform to standardized format {errorCode, data}
