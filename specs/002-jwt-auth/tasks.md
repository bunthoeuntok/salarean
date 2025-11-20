# Tasks: JWT Authentication, Authorization and User Profile Management

**Input**: Design documents from `/specs/002-jwt-auth/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/api-contracts.yaml

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

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

- [ ] T001 Add Apache Tika dependency to auth-service/pom.xml for image validation
- [ ] T002 Create uploads directory at auth-service/uploads/profile-photos/
- [ ] T003 [P] Add i18n resource bundles: auth-service/src/main/resources/messages_en.properties
- [ ] T004 [P] Add i18n resource bundles: auth-service/src/main/resources/messages_km.properties
- [ ] T005 [P] Update application.yml with photo storage configuration (upload-dir, max-file-size, allowed-types)
- [ ] T006 Create database migration script: auth-service/src/main/resources/db/migration/V2__add_refresh_tokens_and_profile_fields.sql

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T007 Run database migration to add profile fields (name, profile_photo_url, profile_photo_uploaded_at) to users table
- [ ] T008 Run database migration to create refresh_tokens table with all required columns
- [ ] T009 [P] Create RefreshToken entity in auth-service/src/main/java/com/sms/auth/model/RefreshToken.java
- [ ] T010 [P] Update User entity in auth-service/src/main/java/com/sms/auth/model/User.java (add name, profilePhotoUrl, profilePhotoUploadedAt fields)
- [ ] T011 [P] Create RefreshTokenRepository in auth-service/src/main/java/com/sms/auth/repository/RefreshTokenRepository.java
- [ ] T012 [P] Create custom exceptions: InvalidTokenException in auth-service/src/main/java/com/sms/auth/exception/InvalidTokenException.java
- [ ] T013 [P] Create custom exceptions: ProfileUpdateException in auth-service/src/main/java/com/sms/auth/exception/ProfileUpdateException.java
- [ ] T014 [P] Create custom exceptions: PhotoUploadException in auth-service/src/main/java/com/sms/auth/exception/PhotoUploadException.java
- [ ] T015 [P] Create PasswordStrengthValidator utility in auth-service/src/main/java/com/sms/auth/util/PasswordStrengthValidator.java
- [ ] T016 Create JwtAuthenticationFilter in auth-service/src/main/java/com/sms/auth/security/JwtAuthenticationFilter.java
- [ ] T017 Update SecurityConfig in auth-service/src/main/java/com/sms/auth/security/SecurityConfig.java to add JWT filter chain
- [ ] T018 Extend JwtTokenProvider in auth-service/src/main/java/com/sms/auth/security/JwtTokenProvider.java to support refresh token generation

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Secure Session Management (Priority: P1) 🎯 MVP

**Goal**: Implement JWT refresh token lifecycle with automatic rotation, logout, and token replay protection to enable secure 30-day authentication sessions

**Independent Test**: Login once, verify session persists across page refreshes, use refresh endpoint to get new tokens, logout and verify tokens are invalidated, attempt token replay and verify all sessions are invalidated

### Implementation for User Story 1

- [ ] T019 [P] [US1] Create RefreshTokenRequest DTO in auth-service/src/main/java/com/sms/auth/dto/RefreshTokenRequest.java
- [ ] T020 [P] [US1] Create RefreshTokenResponse DTO in auth-service/src/main/java/com/sms/auth/dto/RefreshTokenResponse.java
- [ ] T021 [US1] Create TokenService in auth-service/src/main/java/com/sms/auth/service/TokenService.java with methods: createRefreshToken, validateRefreshToken, markAsUsed, revokeAllUserTokens
- [ ] T022 [US1] Implement Redis caching in TokenService for refresh token storage (dual-write: PostgreSQL + Redis with 30-day TTL)
- [ ] T023 [US1] Implement token replay detection logic in TokenService (check has_been_used flag, invalidate all sessions if reused)
- [ ] T024 [US1] Extend AuthService in auth-service/src/main/java/com/sms/auth/service/AuthService.java to integrate TokenService for login flow
- [ ] T025 [US1] Add refreshToken method to AuthService (validate refresh token, rotate tokens, return new access + refresh tokens)
- [ ] T026 [US1] Add logout method to AuthService (invalidate current session and all refresh tokens for user)
- [ ] T027 [US1] Add POST /api/auth/refresh endpoint in AuthController at auth-service/src/main/java/com/sms/auth/controller/AuthController.java
- [ ] T028 [US1] Add POST /api/auth/logout endpoint in AuthController at auth-service/src/main/java/com/sms/auth/controller/AuthController.java
- [ ] T029 [US1] Add error handling for token refresh endpoint (401 for invalid token, 403 for replay detection)
- [ ] T030 [US1] Add i18n error messages for token errors (error.token.invalid, error.token.replay) in both English and Khmer

**Checkpoint**: At this point, User Story 1 should be fully functional - users can refresh tokens, logout, and token replay attacks are detected

---

## Phase 4: User Story 2 - Profile Viewing and Basic Updates (Priority: P2)

**Goal**: Enable teachers to view and update their profile information (name, phone number, language preference)

**Independent Test**: Login as a teacher, call GET /api/auth/me to view profile, call PUT /api/users/me to update name and phone, verify changes persist, attempt duplicate phone update and verify error

### Implementation for User Story 2

- [ ] T031 [P] [US2] Create ProfileResponse DTO in auth-service/src/main/java/com/sms/auth/dto/ProfileResponse.java
- [ ] T032 [P] [US2] Create UpdateProfileRequest DTO in auth-service/src/main/java/com/sms/auth/dto/UpdateProfileRequest.java
- [ ] T033 [US2] Create ProfileService in auth-service/src/main/java/com/sms/auth/service/ProfileService.java with methods: getProfile, updateProfile
- [ ] T034 [US2] Implement getProfile method in ProfileService (fetch user by JWT subject, map to ProfileResponse DTO)
- [ ] T035 [US2] Implement updateProfile method in ProfileService with validations: name (max 255 chars), phone (Cambodia format), language ('en' or 'km')
- [ ] T036 [US2] Add phone number uniqueness check in ProfileService (query UserRepository, throw ProfileUpdateException if duplicate)
- [ ] T037 [US2] Add methods to UserRepository in auth-service/src/main/java/com/sms/auth/repository/UserRepository.java: findByPhoneNumberAndIdNot
- [ ] T038 [US2] Create ProfileController in auth-service/src/main/java/com/sms/auth/controller/ProfileController.java with @RequestMapping("/api")
- [ ] T039 [US2] Add GET /api/auth/me endpoint in ProfileController (secured with @PreAuthorize, calls ProfileService.getProfile)
- [ ] T040 [US2] Add PUT /api/users/me endpoint in ProfileController (secured with @PreAuthorize, calls ProfileService.updateProfile)
- [ ] T041 [US2] Add validation error handling for profile updates (400 for validation errors, return field-specific messages)
- [ ] T042 [US2] Add i18n error messages for profile errors (error.profile.phone.duplicate, error.profile.name.toolong) in both English and Khmer
- [ ] T043 [US2] Add i18n success messages (success.profile.updated) in both English and Khmer
- [ ] T044 [US2] Add audit logging for profile update operations (log user ID, fields changed, timestamp)

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently - users can manage tokens AND view/update profiles

---

## Phase 5: User Story 3 - Security Management (Priority: P2)

**Goal**: Enable teachers to change their password with current password verification and automatic session invalidation for security

**Independent Test**: Login, call PUT /api/users/me/password with correct current password and valid new password, verify password change succeeds, verify other sessions are invalidated, try to login with old password and verify it fails, login with new password and verify success

### Implementation for User Story 3

- [ ] T045 [P] [US3] Create ChangePasswordRequest DTO in auth-service/src/main/java/com/sms/auth/dto/ChangePasswordRequest.java
- [ ] T046 [US3] Add changePassword method to ProfileService in auth-service/src/main/java/com/sms/auth/service/ProfileService.java
- [ ] T047 [US3] Implement current password verification in changePassword method (use BCrypt to compare hashes)
- [ ] T048 [US3] Integrate PasswordStrengthValidator in changePassword method (validate new password against OWASP requirements)
- [ ] T049 [US3] Implement session invalidation logic in changePassword method (delete all sessions except current, delete all refresh tokens except current)
- [ ] T050 [US3] Add methods to SessionRepository in auth-service/src/main/java/com/sms/auth/repository/SessionRepository.java: deleteByUserIdAndTokenJtiNot
- [ ] T051 [US3] Add methods to RefreshTokenRepository: deleteByUserIdAndIdNot
- [ ] T052 [US3] Add PUT /api/users/me/password endpoint in ProfileController (secured with @PreAuthorize, calls ProfileService.changePassword)
- [ ] T053 [US3] Add error handling for password change (400 for incorrect current password, 400 for weak new password with detailed requirements)
- [ ] T054 [US3] Add i18n error messages for password errors (error.password.incorrect, error.password.weak, error.password.length, etc.) in both English and Khmer
- [ ] T055 [US3] Add i18n success message (success.password.changed) in both English and Khmer
- [ ] T056 [US3] Add audit logging for password change operations (log user ID, timestamp, session invalidation count)

**Checkpoint**: At this point, User Stories 1, 2, AND 3 should all work independently - users can manage tokens, profiles, AND change passwords securely

---

## Phase 6: User Story 4 - Localization Preference (Priority: P3)

**Goal**: Enable teachers to select their preferred language (Khmer or English) with immediate application to interface

**Independent Test**: Login, view current profile to see default language, call PUT /api/users/me with preferredLanguage='km', verify update succeeds, logout and login again, verify language preference persists

**Note**: This user story is largely implemented as part of User Story 2 (updateProfile supports language updates)

### Implementation for User Story 4

- [ ] T057 [US4] Verify language preference update is working in ProfileService.updateProfile (should already be implemented from US2)
- [ ] T058 [US4] Add language preference validation in UpdateProfileRequest (must be 'en' or 'km')
- [ ] T059 [US4] Verify JWT token includes language claim when generated (should be implemented in JwtTokenProvider from foundational phase)
- [ ] T060 [US4] Verify error messages respect user's language preference (MessageSource should use locale from JWT claim)
- [ ] T061 [US4] Test language preference persistence across sessions (login, change to 'km', logout, login, verify still 'km')

**Checkpoint**: Language preference feature is complete and integrated with profile management

---

## Phase 7: User Story 5 - Profile Photo Upload (Priority: P3)

**Goal**: Enable teachers to upload profile photos (JPG/PNG, max 5MB) with validation and storage

**Independent Test**: Login, upload a valid 2MB JPG photo via POST /api/users/me/photo, verify photo is saved to filesystem, verify profilePhotoUrl is returned, call GET /api/auth/me and verify photo URL is present, upload a new photo and verify old photo is replaced

### Implementation for User Story 5

- [ ] T062 [P] [US5] Create PhotoUploadResponse DTO in auth-service/src/main/java/com/sms/auth/dto/PhotoUploadResponse.java
- [ ] T063 [US5] Create PhotoStorageService in auth-service/src/main/java/com/sms/auth/service/PhotoStorageService.java with methods: savePhoto, deletePhoto, validatePhoto
- [ ] T064 [US5] Implement file size validation in PhotoStorageService.validatePhoto (max 5MB = 5242880 bytes)
- [ ] T065 [US5] Implement file format validation in PhotoStorageService.validatePhoto using Apache Tika (MIME types: image/jpeg, image/png)
- [ ] T066 [US5] Implement content type verification in PhotoStorageService.validatePhoto (detect actual content type, reject mismatched extensions)
- [ ] T067 [US5] Implement savePhoto method in PhotoStorageService (generate path: uploads/profile-photos/{userId}/profile.{ext}, delete old photo if exists, save new photo)
- [ ] T068 [US5] Implement deletePhoto method in PhotoStorageService (delete file from filesystem)
- [ ] T069 [US5] Add uploadProfilePhoto method to ProfileService in auth-service/src/main/java/com/sms/auth/service/ProfileService.java
- [ ] T070 [US5] Integrate PhotoStorageService in ProfileService.uploadProfilePhoto (validate photo, save to filesystem, update user.profilePhotoUrl and user.profilePhotoUploadedAt)
- [ ] T071 [US5] Add POST /api/users/me/photo endpoint in ProfileController (secured with @PreAuthorize, multipart/form-data, calls ProfileService.uploadProfilePhoto)
- [ ] T072 [US5] Add error handling for photo upload (400 for size exceeded, 400 for invalid format, 400 for corrupted image)
- [ ] T073 [US5] Add i18n error messages for photo errors (error.photo.size, error.photo.format, error.photo.corrupted) in both English and Khmer
- [ ] T074 [US5] Add i18n success message (success.photo.uploaded) in both English and Khmer
- [ ] T075 [US5] Update ProfileResponse to include profilePhotoUrl and profilePhotoUploadedAt fields (should already be present from data model)
- [ ] T076 [US5] Add audit logging for photo upload operations (log user ID, file size, timestamp)

**Checkpoint**: All user stories (1-5) are now complete and independently functional

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T077 [P] Add GlobalExceptionHandler in auth-service/src/main/java/com/sms/auth/exception/GlobalExceptionHandler.java to handle all custom exceptions consistently
- [ ] T078 [P] Review and enhance structured logging across all services (ensure correlation IDs, user IDs, and operation types are logged)
- [ ] T079 [P] Add Swagger/OpenAPI annotations to all controllers for API documentation generation
- [ ] T080 [P] Verify all endpoints have proper @PreAuthorize annotations for security
- [ ] T081 Verify Redis connection pooling is configured optimally for 1000 concurrent requests (application.yml: spring.redis.lettuce.pool)
- [ ] T082 Add database indexes verification: users(phone_number), refresh_tokens(user_id, expires_at, has_been_used)
- [ ] T083 Test token refresh performance (should be < 500ms) using quickstart.md Apache Bench test
- [ ] T084 Test profile update performance (should be < 200ms) with concurrent requests
- [ ] T085 Test photo upload performance (2MB file should be < 3s)
- [ ] T086 Run security scan on dependencies (mvn dependency-check:check)
- [ ] T087 Verify all success and error messages have both English and Khmer translations
- [ ] T088 Test edge cases: token replay attack, concurrent sessions, duplicate phone updates, corrupted image uploads
- [ ] T089 Update auth-service README.md with new endpoints documentation
- [ ] T090 Run full quickstart.md validation (all curl commands should work as documented)

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
- Error handling and i18n messages can be added in parallel with or after implementation

### Parallel Opportunities

- **Phase 1 (Setup)**: Tasks T001-T006 can all run in parallel
- **Phase 2 (Foundational)**: Tasks T009-T015 (entities, repos, exceptions, validators) can all run in parallel
- **Phase 3 (US1)**: Tasks T019-T020 (DTOs) can run in parallel
- **Phase 4 (US2)**: Tasks T031-T032 (DTOs) can run in parallel
- **Phase 5 (US3)**: Tasks T045 (DTO) can run independently while other tasks are in progress
- **Phase 7 (US5)**: Tasks T062 (DTO) can run independently
- **Phase 8 (Polish)**: Tasks T077-T080 (exception handling, logging, Swagger, security) can run in parallel

### Critical Path (Minimum for MVP)

**MVP = User Story 1 only (Secure Session Management)**

1. Phase 1: Setup (T001-T006)
2. Phase 2: Foundational (T007-T018)
3. Phase 3: User Story 1 (T019-T030)

This delivers the core value: secure 30-day authentication sessions with token refresh and logout.

**Full Feature = All User Stories**

1. Phase 1: Setup
2. Phase 2: Foundational
3. Phase 3: US1 (P1)
4. Phase 4: US2 (P2)
5. Phase 5: US3 (P2)
6. Phase 6: US4 (P3) - Quick verification only
7. Phase 7: US5 (P3)
8. Phase 8: Polish

---

## Parallel Example: User Story 1

```bash
# After Foundational phase completes, these can run in parallel:

# Developer 1: DTOs
git checkout -b us1-dtos
# Work on T019, T020

# Developer 2: TokenService (waits for DTOs merge)
git checkout -b us1-token-service
# Work on T021-T023

# Developer 3: AuthService integration (waits for TokenService merge)
git checkout -b us1-auth-service
# Work on T024-T026

# Developer 4: Controller (waits for AuthService merge)
git checkout -b us1-controller
# Work on T027-T028

# Developer 5: Error handling & i18n (can run in parallel with controllers)
git checkout -b us1-errors
# Work on T029-T030
```

---

## Parallel Example: Multiple User Stories

```bash
# After Foundational phase completes, entire user stories can be parallelized:

# Team A: US1 (Secure Session Management) - Priority 1
git checkout -b feature/us1-session-management
# Work on T019-T030

# Team B: US2 (Profile Viewing and Basic Updates) - Priority 2
git checkout -b feature/us2-profile-updates
# Work on T031-T044

# Team C: US5 (Profile Photo Upload) - Priority 3
git checkout -b feature/us5-photo-upload
# Work on T062-T076

# US3 and US4 can start after US2 ProfileService is available, or be implemented independently
```

---

## Implementation Strategy

### Recommended Approach: Incremental Delivery

1. **Week 1**: Setup + Foundational (T001-T018)
   - Get all infrastructure in place
   - Checkpoint: Foundation ready

2. **Week 2**: User Story 1 (T019-T030) - MVP
   - Deliver core value: secure authentication sessions
   - Checkpoint: MVP deployed and testable

3. **Week 3**: User Story 2 + 4 (T031-T044, T057-T061)
   - Add profile management
   - Language preference comes "free" with US2
   - Checkpoint: Users can manage profiles

4. **Week 4**: User Story 3 + 5 (T045-T056, T062-T076)
   - Add password security and photo uploads
   - Checkpoint: Full feature set complete

5. **Week 5**: Polish (T077-T090)
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

## Testing Strategy (Optional - Not in Spec)

If you decide to add tests later, follow this structure:

### Unit Tests
- PasswordStrengthValidator
- PhotoStorageService (file validation)
- TokenService (token generation, replay detection)

### Integration Tests
- Token refresh flow (Redis + PostgreSQL dual write)
- Profile update with phone duplication check
- Password change with session invalidation
- Photo upload with filesystem storage

### Contract Tests
- All 6 API endpoints (OpenAPI schema validation)
- Request/response payload validation
- Error response formats (English + Khmer)

### Performance Tests
- Token refresh: 1000 concurrent requests < 500ms
- Profile update: < 200ms
- Photo upload: 2MB < 3s

---

## Success Metrics

After implementation, verify these success criteria from spec.md:

- **SC-001**: ✅ Teachers remain authenticated 30 days without re-entering credentials
- **SC-002**: ✅ Token refresh < 500ms (test with T083)
- **SC-003**: ✅ 99% profile updates succeed on first attempt
- **SC-004**: ✅ Password change < 1 minute
- **SC-005**: ✅ Photo upload (2MB) < 3s (test with T085)
- **SC-006**: ✅ 95% profile viewing succeeds on first attempt
- **SC-007**: ✅ Logout invalidates tokens < 1s
- **SC-008**: ✅ Zero unauthorized access after token invalidation (test with T088)
- **SC-009**: ✅ Language change < 200ms
- **SC-010**: ✅ 1000 concurrent token refreshes succeed (test with T083)

---

## Summary

- **Total Tasks**: 90
- **Phase 1 (Setup)**: 6 tasks
- **Phase 2 (Foundational)**: 12 tasks - BLOCKS all user stories
- **Phase 3 (US1 - P1)**: 12 tasks - MVP
- **Phase 4 (US2 - P2)**: 14 tasks
- **Phase 5 (US3 - P2)**: 12 tasks
- **Phase 6 (US4 - P3)**: 5 tasks (mostly verification)
- **Phase 7 (US5 - P3)**: 15 tasks
- **Phase 8 (Polish)**: 14 tasks

**MVP Scope**: 30 tasks (Setup + Foundational + US1)
**Full Feature**: 90 tasks

**Parallel Opportunities**: 15 tasks marked [P] can run concurrently
**Independent Stories**: All 5 user stories can be implemented and tested independently after Foundational phase

**Estimated Effort**:
- MVP (US1 only): 1-2 weeks
- MVP + Profile Management (US1+US2+US4): 2-3 weeks
- Full Feature (All user stories): 4-5 weeks
- With Polish: 5-6 weeks
