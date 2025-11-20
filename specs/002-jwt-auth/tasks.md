# Tasks: JWT Authentication, Authorization and User Profile Management

**Input**: Design documents from `/specs/002-jwt-auth/`
**Prerequisites**: plan.md, spec.md (with API standards), research.md (updated), data-model.md, contracts/api-contracts.yaml

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

**API Standards**: All tasks updated to reflect standardized `{errorCode: string, data: T}` response format. NO backend i18n - frontend handles all localization.

**Tests**: Not explicitly requested in spec - tests are OPTIONAL for this feature.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

Project uses microservices architecture. This feature extends the existing auth-service:
- **Backend**: `auth-service/src/main/java/com/sms/auth/`
- **Resources**: `auth-service/src/main/resources/`
- **Tests**: `auth-service/src/test/java/com/sms/auth/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and dependencies

- [ ] T001 Add Apache Tika dependency to auth-service/pom.xml for image validation (org.apache.tika:tika-core:2.9.1)
- [ ] T002 Create uploads directory at auth-service/uploads/profile-photos/
- [ ] T003 Update application.yml with photo storage configuration (upload-dir: ./uploads/profile-photos, max-file-size: 5242880, allowed-types: [image/jpeg, image/png])
- [ ] T004 Create database migration script: auth-service/src/main/resources/db/migration/V2__add_refresh_tokens_and_profile_fields.sql

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T005 Run database migration to add profile fields (name, profile_photo_url, profile_photo_uploaded_at) to users table
- [ ] T006 Run database migration to create refresh_tokens table with all required columns (id, user_id, token_hash, expires_at, has_been_used, used_at, ip_address, user_agent, created_at)
- [ ] T007 [P] Create RefreshToken entity in auth-service/src/main/java/com/sms/auth/model/RefreshToken.java
- [ ] T008 [P] Update User entity in auth-service/src/main/java/com/sms/auth/model/User.java (add name, profilePhotoUrl, profilePhotoUploadedAt fields)
- [ ] T009 [P] Create RefreshTokenRepository in auth-service/src/main/java/com/sms/auth/repository/RefreshTokenRepository.java
- [ ] T010 [P] Create ApiResponse<T> generic wrapper class in auth-service/src/main/java/com/sms/auth/dto/ApiResponse.java with fields: errorCode (String), data (T), and static methods success(T data) and error(String errorCode)
- [ ] T011 [P] Create ErrorCodes constants class in auth-service/src/main/java/com/sms/auth/dto/ErrorCodes.java with all error codes from spec (SUCCESS, INVALID_TOKEN, TOKEN_REPLAY_DETECTED, UNAUTHORIZED, INVALID_PHONE_FORMAT, DUPLICATE_PHONE, INCORRECT_PASSWORD, WEAK_PASSWORD, PASSWORD_TOO_SHORT, PASSWORD_MISSING_UPPERCASE, PASSWORD_MISSING_LOWERCASE, PASSWORD_MISSING_DIGIT, PASSWORD_MISSING_SPECIAL, PASSWORD_TOO_COMMON, PHOTO_SIZE_EXCEEDED, INVALID_PHOTO_FORMAT, CORRUPTED_IMAGE, VALIDATION_ERROR, INTERNAL_SERVER_ERROR)
- [ ] T012 [P] Create custom exceptions: InvalidTokenException in auth-service/src/main/java/com/sms/auth/exception/InvalidTokenException.java
- [ ] T013 [P] Create custom exceptions: ProfileUpdateException in auth-service/src/main/java/com/sms/auth/exception/ProfileUpdateException.java
- [ ] T014 [P] Create custom exceptions: PhotoUploadException in auth-service/src/main/java/com/sms/auth/exception/PhotoUploadException.java
- [ ] T015 [P] Create PasswordStrengthValidator utility in auth-service/src/main/java/com/sms/auth/util/PasswordStrengthValidator.java (OWASP-aligned: min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special, not in common password list)
- [ ] T016 Create JwtAuthenticationFilter in auth-service/src/main/java/com/sms/auth/security/JwtAuthenticationFilter.java
- [ ] T017 Update SecurityConfig in auth-service/src/main/java/com/sms/auth/security/SecurityConfig.java to add JWT filter chain
- [ ] T018 Extend JwtTokenProvider in auth-service/src/main/java/com/sms/auth/security/JwtTokenProvider.java to support refresh token generation
- [ ] T019 Create GlobalExceptionHandler in auth-service/src/main/java/com/sms/auth/exception/GlobalExceptionHandler.java to map all exceptions to ApiResponse<Void> with appropriate error codes (no human-readable messages, only error codes)

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Secure Session Management (Priority: P1) 🎯 MVP

**Goal**: Implement JWT refresh token lifecycle with automatic rotation, logout, and token replay protection to enable secure 30-day authentication sessions

**Independent Test**: Login once, verify session persists across page refreshes, use refresh endpoint to get new tokens, logout and verify tokens are invalidated, attempt token replay and verify all sessions are invalidated

### Implementation for User Story 1

- [ ] T020 [P] [US1] Create RefreshTokenRequest DTO in auth-service/src/main/java/com/sms/auth/dto/RefreshTokenRequest.java
- [ ] T021 [P] [US1] Create RefreshTokenResponse DTO in auth-service/src/main/java/com/sms/auth/dto/RefreshTokenResponse.java (accessToken, refreshToken, expiresIn)
- [ ] T022 [US1] Create TokenService in auth-service/src/main/java/com/sms/auth/service/TokenService.java with methods: createRefreshToken, validateRefreshToken, markAsUsed, revokeAllUserTokens
- [ ] T023 [US1] Implement Redis caching in TokenService for refresh token storage (dual-write: PostgreSQL + Redis with key pattern "refresh_token:{userId}:{tokenId}" and 30-day TTL)
- [ ] T024 [US1] Implement token replay detection logic in TokenService (check has_been_used flag, throw InvalidTokenException with error code TOKEN_REPLAY_DETECTED if reused, invalidate all user sessions)
- [ ] T025 [US1] Extend AuthService in auth-service/src/main/java/com/sms/auth/service/AuthService.java to integrate TokenService for login flow
- [ ] T026 [US1] Add refreshToken method to AuthService (validate refresh token, rotate tokens, return ApiResponse<RefreshTokenResponse>)
- [ ] T027 [US1] Add logout method to AuthService (invalidate current session and all refresh tokens for user, return ApiResponse<Void>)
- [ ] T028 [US1] Add POST /api/auth/refresh endpoint in AuthController at auth-service/src/main/java/com/sms/auth/controller/AuthController.java returning ApiResponse<RefreshTokenResponse>
- [ ] T029 [US1] Add POST /api/auth/logout endpoint in AuthController at auth-service/src/main/java/com/sms/auth/controller/AuthController.java returning ApiResponse<Void>
- [ ] T030 [US1] Update AuthController endpoints to return ApiResponse wrapper for all responses (success with errorCode="SUCCESS", errors with appropriate error codes)

**Checkpoint**: At this point, User Story 1 should be fully functional - users can refresh tokens, logout, and token replay attacks are detected

---

## Phase 4: User Story 2 - Profile Viewing and Basic Updates (Priority: P2)

**Goal**: Enable teachers to view and update their profile information (name, phone number, language preference)

**Independent Test**: Login as a teacher, call GET /api/auth/me to view profile, call PUT /api/users/me to update name and phone, verify changes persist, attempt duplicate phone update and verify error code DUPLICATE_PHONE is returned

### Implementation for User Story 2

- [ ] T031 [P] [US2] Create ProfileResponse DTO in auth-service/src/main/java/com/sms/auth/dto/ProfileResponse.java (id, email, phoneNumber, name, preferredLanguage, profilePhotoUrl, profilePhotoUploadedAt, accountStatus, createdAt)
- [ ] T032 [P] [US2] Create UpdateProfileRequest DTO in auth-service/src/main/java/com/sms/auth/dto/UpdateProfileRequest.java (name, phoneNumber, preferredLanguage)
- [ ] T033 [US2] Create ProfileService in auth-service/src/main/java/com/sms/auth/service/ProfileService.java with methods: getProfile, updateProfile
- [ ] T034 [US2] Implement getProfile method in ProfileService (fetch user by JWT subject, map to ProfileResponse DTO)
- [ ] T035 [US2] Implement updateProfile method in ProfileService with validations: name (max 255 chars, trimmed), phone (Cambodia format regex), language ('en' or 'km')
- [ ] T036 [US2] Add phone number uniqueness check in ProfileService (query UserRepository, throw ProfileUpdateException with error code DUPLICATE_PHONE if duplicate)
- [ ] T037 [US2] Add methods to UserRepository in auth-service/src/main/java/com/sms/auth/repository/UserRepository.java: findByPhoneNumberAndIdNot(String phone, UUID id)
- [ ] T038 [US2] Create ProfileController in auth-service/src/main/java/com/sms/auth/controller/ProfileController.java with @RequestMapping("/api")
- [ ] T039 [US2] Add GET /api/auth/me endpoint in ProfileController (secured with @PreAuthorize, calls ProfileService.getProfile, returns ApiResponse<ProfileResponse>)
- [ ] T040 [US2] Add PUT /api/users/me endpoint in ProfileController (secured with @PreAuthorize, calls ProfileService.updateProfile, returns ApiResponse<ProfileResponse>)
- [ ] T041 [US2] Add validation error handling in ProfileController for profile updates (catch ValidationException, return ApiResponse.error with error code INVALID_PHONE_FORMAT, VALIDATION_ERROR, etc.)
- [ ] T042 [US2] Add audit logging for profile update operations (log user ID, fields changed, timestamp using SLF4J)

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently - users can manage tokens AND view/update profiles

---

## Phase 5: User Story 3 - Security Management (Priority: P2)

**Goal**: Enable teachers to change their password with current password verification and automatic session invalidation for security

**Independent Test**: Login, call PUT /api/users/me/password with correct current password and valid new password, verify password change succeeds (errorCode="SUCCESS"), verify other sessions are invalidated, try to login with old password and verify it fails, login with new password and verify success

### Implementation for User Story 3

- [ ] T043 [P] [US3] Create ChangePasswordRequest DTO in auth-service/src/main/java/com/sms/auth/dto/ChangePasswordRequest.java (currentPassword, newPassword)
- [ ] T044 [US3] Add changePassword method to ProfileService in auth-service/src/main/java/com/sms/auth/service/ProfileService.java
- [ ] T045 [US3] Implement current password verification in changePassword method (use BCrypt to compare hashes, throw ProfileUpdateException with error code INCORRECT_PASSWORD if mismatch)
- [ ] T046 [US3] Integrate PasswordStrengthValidator in changePassword method (validate new password against OWASP requirements, throw ProfileUpdateException with specific error codes: PASSWORD_TOO_SHORT, PASSWORD_MISSING_UPPERCASE, PASSWORD_MISSING_LOWERCASE, PASSWORD_MISSING_DIGIT, PASSWORD_MISSING_SPECIAL, PASSWORD_TOO_COMMON)
- [ ] T047 [US3] Implement session invalidation logic in changePassword method (delete all sessions except current, delete all refresh tokens except current)
- [ ] T048 [US3] Add methods to SessionRepository in auth-service/src/main/java/com/sms/auth/repository/SessionRepository.java: deleteByUserIdAndTokenJtiNot(UUID userId, String tokenJti)
- [ ] T049 [US3] Add methods to RefreshTokenRepository: deleteByUserIdAndIdNot(UUID userId, UUID tokenId)
- [ ] T050 [US3] Add PUT /api/users/me/password endpoint in ProfileController (secured with @PreAuthorize, calls ProfileService.changePassword, returns ApiResponse<Void>)
- [ ] T051 [US3] Add error handling for password change in ProfileController (catch exceptions, return ApiResponse.error with error codes INCORRECT_PASSWORD, WEAK_PASSWORD, PASSWORD_TOO_SHORT, etc.)
- [ ] T052 [US3] Add audit logging for password change operations (log user ID, timestamp, session invalidation count using SLF4J)

**Checkpoint**: At this point, User Stories 1, 2, AND 3 should all work independently - users can manage tokens, profiles, AND change passwords securely

---

## Phase 6: User Story 4 - Localization Preference (Priority: P3)

**Goal**: Enable teachers to select their preferred language (Khmer or English) with persistence in profile

**Independent Test**: Login, view current profile to see default language, call PUT /api/users/me with preferredLanguage='km', verify update succeeds (errorCode="SUCCESS"), logout and login again, verify language preference persists

**Note**: This user story is largely implemented as part of User Story 2 (updateProfile supports language updates). Frontend uses preferredLanguage to select UI translations.

### Implementation for User Story 4

- [ ] T053 [US4] Verify language preference update is working in ProfileService.updateProfile (should already be implemented from US2, just validate 'en' or 'km')
- [ ] T054 [US4] Add language preference validation in UpdateProfileRequest (must be 'en' or 'km', throw ValidationException with error code VALIDATION_ERROR if invalid)
- [ ] T055 [US4] Verify JWT token includes language claim when generated (should be implemented in JwtTokenProvider from foundational phase, verify 'lang' claim is set)
- [ ] T056 [US4] Test language preference persistence across sessions (login, change to 'km', logout, login, verify GET /api/auth/me returns preferredLanguage='km')

**Checkpoint**: Language preference feature is complete and integrated with profile management

---

## Phase 7: User Story 5 - Profile Photo Upload (Priority: P3)

**Goal**: Enable teachers to upload profile photos (JPG/PNG, max 5MB) with validation and storage

**Independent Test**: Login, upload a valid 2MB JPG photo via POST /api/users/me/photo, verify ApiResponse<PhotoUploadResponse> with errorCode="SUCCESS" is returned, call GET /api/auth/me and verify profilePhotoUrl is present, upload a new photo and verify old photo is replaced

### Implementation for User Story 5

- [ ] T057 [P] [US5] Create PhotoUploadResponse DTO in auth-service/src/main/java/com/sms/auth/dto/PhotoUploadResponse.java (photoUrl, uploadedAt)
- [ ] T058 [US5] Create PhotoStorageService in auth-service/src/main/java/com/sms/auth/service/PhotoStorageService.java with methods: savePhoto, deletePhoto, validatePhoto
- [ ] T059 [US5] Implement file size validation in PhotoStorageService.validatePhoto (max 5MB = 5242880 bytes, throw PhotoUploadException with error code PHOTO_SIZE_EXCEEDED if exceeded)
- [ ] T060 [US5] Implement file format validation in PhotoStorageService.validatePhoto using Apache Tika (detect MIME type, only allow image/jpeg and image/png, throw PhotoUploadException with error code INVALID_PHOTO_FORMAT if invalid)
- [ ] T061 [US5] Implement content type verification in PhotoStorageService.validatePhoto (detect actual content type vs extension, throw PhotoUploadException with error code CORRUPTED_IMAGE if mismatch)
- [ ] T062 [US5] Implement savePhoto method in PhotoStorageService (generate path: uploads/profile-photos/{userId}/profile.{ext}, delete old photo if exists, save new photo, return relative URL path)
- [ ] T063 [US5] Implement deletePhoto method in PhotoStorageService (delete file from filesystem)
- [ ] T064 [US5] Add uploadProfilePhoto method to ProfileService in auth-service/src/main/java/com/sms/auth/service/ProfileService.java
- [ ] T065 [US5] Integrate PhotoStorageService in ProfileService.uploadProfilePhoto (validate photo, save to filesystem, update user.profilePhotoUrl and user.profilePhotoUploadedAt, return PhotoUploadResponse)
- [ ] T066 [US5] Add POST /api/users/me/photo endpoint in ProfileController (secured with @PreAuthorize, accept multipart/form-data with parameter "photo", calls ProfileService.uploadProfilePhoto, returns ApiResponse<PhotoUploadResponse>)
- [ ] T067 [US5] Add error handling for photo upload in ProfileController (catch PhotoUploadException, return ApiResponse.error with error codes PHOTO_SIZE_EXCEEDED, INVALID_PHOTO_FORMAT, CORRUPTED_IMAGE)
- [ ] T068 [US5] Update ProfileResponse to include profilePhotoUrl and profilePhotoUploadedAt fields (should already be present from data model in T031)
- [ ] T069 [US5] Add audit logging for photo upload operations (log user ID, file size, timestamp using SLF4J)

**Checkpoint**: All user stories (1-5) are now complete and independently functional

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T070 [P] Verify GlobalExceptionHandler maps all custom exceptions to ApiResponse with appropriate error codes (InvalidTokenException → INVALID_TOKEN, ProfileUpdateException → INVALID_PHONE_FORMAT/DUPLICATE_PHONE, PhotoUploadException → PHOTO_SIZE_EXCEEDED/INVALID_PHOTO_FORMAT/CORRUPTED_IMAGE, etc.)
- [ ] T071 [P] Review and enhance structured logging across all services (ensure correlation IDs, user IDs, and operation types are logged)
- [ ] T072 [P] Add Swagger/OpenAPI annotations to all controllers for API documentation generation (ensure response schemas show ApiResponse<T> wrapper format)
- [ ] T073 [P] Verify all endpoints have proper @PreAuthorize annotations for security
- [ ] T074 Verify Redis connection pooling is configured optimally for 1000 concurrent requests (application.yml: spring.redis.lettuce.pool.max-active=10, max-idle=5)
- [ ] T075 Add database indexes verification: users(phone_number), refresh_tokens(user_id, expires_at, has_been_used)
- [ ] T076 Test token refresh performance (should be < 500ms) using quickstart.md Apache Bench test
- [ ] T077 Test profile update performance (should be < 200ms) with concurrent requests
- [ ] T078 Test photo upload performance (2MB file should be < 3s)
- [ ] T079 Run security scan on dependencies (mvn dependency-check:check)
- [ ] T080 Verify all API responses conform to {errorCode, data} format across all 6 endpoints
- [ ] T081 Test edge cases: token replay attack (should return TOKEN_REPLAY_DETECTED), concurrent sessions, duplicate phone updates (should return DUPLICATE_PHONE), corrupted image uploads (should return CORRUPTED_IMAGE)
- [ ] T082 Update auth-service README.md with new endpoints documentation and API response format examples
- [ ] T083 Verify no backend i18n code exists (no MessageSource, no .properties files, no locale resolution)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P2 → P3 → P3)
- **Polish (Phase 8)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1) - Secure Session Management**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2) - Profile Viewing and Basic Updates**: Can start after Foundational (Phase 2) - No dependencies on other stories (independent)
- **User Story 3 (P2) - Security Management**: Can start after Foundational (Phase 2) - Uses ProfileService from US2 but can be implemented independently
- **User Story 4 (P3) - Localization Preference**: Mostly implemented by US2 - Just needs verification tasks
- **User Story 5 (P3) - Profile Photo Upload**: Can start after Foundational (Phase 2) - Uses ProfileService from US2 but can be implemented independently

### Within Each User Story

- DTOs can be created in parallel [P]
- Services must be created after DTOs
- Controllers must be created after Services
- Error handling can be added in parallel with or after implementation

### Parallel Opportunities

- **Phase 1 (Setup)**: Tasks T001-T004 can all run in parallel
- **Phase 2 (Foundational)**: Tasks T007-T015 (entities, repos, DTOs, exceptions, validators) can all run in parallel
- **Phase 3 (US1)**: Tasks T020-T021 (DTOs) can run in parallel
- **Phase 4 (US2)**: Tasks T031-T032 (DTOs) can run in parallel
- **Phase 5 (US3)**: Task T043 (DTO) can run independently
- **Phase 7 (US5)**: Task T057 (DTO) can run independently
- **Phase 8 (Polish)**: Tasks T070-T073 (exception handling, logging, Swagger, security) can run in parallel

### Critical Path (Minimum for MVP)

**MVP = User Story 1 only (Secure Session Management)**

1. Phase 1: Setup (T001-T004) - 4 tasks
2. Phase 2: Foundational (T005-T019) - 15 tasks
3. Phase 3: User Story 1 (T020-T030) - 11 tasks

**Total MVP: 30 tasks**

This delivers the core value: secure 30-day authentication sessions with token refresh and logout.

**Full Feature = All User Stories**

1. Phase 1: Setup (4 tasks)
2. Phase 2: Foundational (15 tasks)
3. Phase 3: US1 P1 (11 tasks)
4. Phase 4: US2 P2 (12 tasks)
5. Phase 5: US3 P2 (10 tasks)
6. Phase 6: US4 P3 (4 tasks - verification only)
7. Phase 7: US5 P3 (13 tasks)
8. Phase 8: Polish (14 tasks)

**Total: 83 tasks**

---

## Parallel Example: User Story 1

```bash
# After Foundational phase completes, these can run in parallel:

# Developer 1: DTOs
git checkout -b us1-dtos
# Work on T020, T021

# Developer 2: TokenService (waits for DTOs merge)
git checkout -b us1-token-service
# Work on T022-T024

# Developer 3: AuthService integration (waits for TokenService merge)
git checkout -b us1-auth-service
# Work on T025-T027

# Developer 4: Controller (waits for AuthService merge)
git checkout -b us1-controller
# Work on T028-T030
```

---

## Parallel Example: Multiple User Stories

```bash
# After Foundational phase completes, entire user stories can be parallelized:

# Team A: US1 (Secure Session Management) - Priority 1
git checkout -b feature/us1-session-management
# Work on T020-T030

# Team B: US2 (Profile Viewing and Basic Updates) - Priority 2
git checkout -b feature/us2-profile-updates
# Work on T031-T042

# Team C: US5 (Profile Photo Upload) - Priority 3
git checkout -b feature/us5-photo-upload
# Work on T057-T069

# US3 and US4 can start after US2 ProfileService is available, or be implemented independently
```

---

## Implementation Strategy

### Recommended Approach: Incremental Delivery

1. **Week 1**: Setup + Foundational (T001-T019)
   - Get all infrastructure in place including ApiResponse wrapper and ErrorCodes
   - Checkpoint: Foundation ready

2. **Week 2**: User Story 1 (T020-T030) - MVP
   - Deliver core value: secure authentication sessions with standardized API responses
   - Checkpoint: MVP deployed and testable

3. **Week 3**: User Story 2 + 4 (T031-T042, T053-T056)
   - Add profile management
   - Language preference comes "free" with US2
   - Checkpoint: Users can manage profiles

4. **Week 4**: User Story 3 + 5 (T043-T052, T057-T069)
   - Add password security and photo uploads
   - Checkpoint: Full feature set complete

5. **Week 5**: Polish (T070-T083)
   - Performance testing, security hardening
   - Documentation and validation
   - Checkpoint: Production-ready

### Alternative Approach: Parallel Teams

If you have multiple developers:

- **Team 1**: US1 (Session Management) - Week 1-2
- **Team 2**: US2 + US4 (Profile Management) - Week 1-2
- **Team 3**: US5 (Photo Upload) - Week 2-3
- **Team 4**: US3 (Password Security) - Week 2-3
- **All Teams**: Polish together - Week 3-4

Each team can work independently after Foundational phase completes.

---

## Key Differences from Original Tasks

**REMOVED** (No longer needed due to API standards):
- ❌ T003-T004: i18n resource bundles (messages_en.properties, messages_km.properties)
- ❌ T030: i18n error messages for token errors
- ❌ T042-T043: i18n success/error messages for profile
- ❌ T054-T055: i18n error messages for password
- ❌ T073-T074: i18n error messages for photo upload
- ❌ T087: Verify English/Khmer translations

**ADDED** (New for API standards):
- ✅ T010: ApiResponse<T> wrapper class
- ✅ T011: ErrorCodes constants class
- ✅ T019: GlobalExceptionHandler returning ApiResponse format
- ✅ T030: Update AuthController to return ApiResponse wrapper
- ✅ T080: Verify {errorCode, data} format across all endpoints
- ✅ T083: Verify no backend i18n code exists

**Net Result**: 83 tasks (vs 90 original) - 7 fewer tasks, ~1 week saved

---

## Success Metrics

After implementation, verify these success criteria from spec.md:

- **SC-001**: ✅ Teachers remain authenticated 30 days without re-entering credentials
- **SC-002**: ✅ Token refresh < 500ms (test with T076)
- **SC-003**: ✅ 99% profile updates succeed on first attempt
- **SC-004**: ✅ Password change < 1 minute
- **SC-005**: ✅ Photo upload (2MB) < 3s (test with T078)
- **SC-006**: ✅ 95% profile viewing succeeds on first attempt
- **SC-007**: ✅ Logout invalidates tokens < 1s
- **SC-008**: ✅ Zero unauthorized access after token invalidation (test with T081)
- **SC-009**: ✅ Language change persists (backend stores, frontend applies)
- **SC-010**: ✅ 1000 concurrent token refreshes succeed (test with T076)
- **SC-011**: ✅ All API responses conform to {errorCode, data} format (test with T080)

---

## Summary

- **Total Tasks**: 83 (reduced from 90 due to API standards)
- **Phase 1 (Setup)**: 4 tasks
- **Phase 2 (Foundational)**: 15 tasks - BLOCKS all user stories
- **Phase 3 (US1 - P1)**: 11 tasks - MVP
- **Phase 4 (US2 - P2)**: 12 tasks
- **Phase 5 (US3 - P2)**: 10 tasks
- **Phase 6 (US4 - P3)**: 4 tasks (mostly verification)
- **Phase 7 (US5 - P3)**: 13 tasks
- **Phase 8 (Polish)**: 14 tasks

**MVP Scope**: 30 tasks (Setup + Foundational + US1)
**Full Feature**: 83 tasks

**Parallel Opportunities**: 14 tasks marked [P] can run concurrently
**Independent Stories**: All 5 user stories can be implemented and tested independently after Foundational phase

**Estimated Effort**:
- MVP (US1 only): 1-2 weeks
- MVP + Profile Management (US1+US2+US4): 2-3 weeks
- Full Feature (All user stories): 4-5 weeks
- With Polish: 5-6 weeks

**API Standards Impact**: 7 fewer tasks, simplified implementation, no backend i18n complexity
