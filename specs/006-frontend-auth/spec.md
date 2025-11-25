# Feature Specification: Frontend Authentication Integration

**Feature Branch**: `006-frontend-auth`
**Created**: 2025-11-25
**Status**: Draft
**Input**: User description: "Integrate frontend with auth-service, for frontend setup, styling, structure please refer to '/Volumes/DATA/my-projects/shadcn-admin' as reference"

## Clarifications

### Session 2025-11-25

- Q: Token storage security strategy? → A: HTTP-only cookies for tokens (secure, requires backend support)
- Q: Default language selection behavior? → A: Detect from browser language preference, fallback to English

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Teacher Sign In (Priority: P1)

A teacher opens the Salarean application and needs to sign in to access their dashboard. They enter their email or phone number along with their password to authenticate.

**Why this priority**: Authentication is the gateway to all other features. Without sign-in capability, users cannot access any protected functionality. This is the foundational feature that enables all subsequent user interactions.

**Independent Test**: Can be fully tested by entering valid credentials and verifying successful redirect to dashboard with session persistence.

**Acceptance Scenarios**:

1. **Given** a registered teacher with valid credentials, **When** they enter their email and password and click sign in, **Then** they are authenticated and redirected to the dashboard with their session persisted.

2. **Given** a registered teacher with valid credentials, **When** they enter their phone number and password and click sign in, **Then** they are authenticated and redirected to the dashboard.

3. **Given** invalid credentials (wrong email/phone or password), **When** user attempts to sign in, **Then** an appropriate error message is displayed without revealing which field is incorrect.

4. **Given** a user is already signed in, **When** they navigate to the sign-in page, **Then** they are automatically redirected to the dashboard.

5. **Given** a user's session expires, **When** they try to access a protected page, **Then** they are redirected to sign-in with a session expired message.

---

### User Story 2 - Teacher Registration (Priority: P2)

A new teacher wants to create an account in the Salarean system to manage their classes and students. They provide their email, phone number, and create a password.

**Why this priority**: Registration enables new user acquisition. While existing users need sign-in first (P1), the system needs registration to grow its user base.

**Independent Test**: Can be fully tested by filling registration form with valid data and verifying account creation and automatic sign-in.

**Acceptance Scenarios**:

1. **Given** a new teacher on the registration page, **When** they enter a valid email, Cambodia phone number, password meeting strength requirements, and submit, **Then** their account is created and they are automatically signed in and redirected to dashboard.

2. **Given** an email that already exists in the system, **When** user attempts to register, **Then** an appropriate error message is displayed.

3. **Given** a phone number that already exists, **When** user attempts to register, **Then** an appropriate error message is displayed.

4. **Given** a password that doesn't meet strength requirements, **When** user attempts to register, **Then** specific feedback is shown indicating which requirements are not met.

5. **Given** an invalid Cambodia phone format, **When** user attempts to register, **Then** a validation error is displayed indicating the correct format.

---

### User Story 3 - Session Persistence (Priority: P3)

A signed-in teacher closes their browser and returns later. They expect to remain signed in without having to re-enter credentials, until their session naturally expires or they explicitly sign out.

**Why this priority**: Session persistence improves user experience significantly by reducing friction for returning users, but sign-in and registration must work first.

**Independent Test**: Can be fully tested by signing in, closing browser, reopening, and verifying authenticated state is maintained.

**Acceptance Scenarios**:

1. **Given** a signed-in user, **When** they close the browser and reopen the application within the session validity period, **Then** they remain signed in and can access protected content.

2. **Given** a signed-in user whose access token expires, **When** they make a request, **Then** the system automatically refreshes the token using the refresh token without user intervention.

3. **Given** a user whose refresh token has expired, **When** they try to access protected content, **Then** they are redirected to sign-in page.

---

### User Story 4 - Teacher Sign Out (Priority: P4)

A teacher wants to sign out of the application, particularly important when using a shared or public computer.

**Why this priority**: Sign out is essential for security but is a secondary action after the primary authentication flows are in place.

**Independent Test**: Can be fully tested by clicking sign out and verifying session is cleared and user cannot access protected pages.

**Acceptance Scenarios**:

1. **Given** a signed-in teacher, **When** they click the sign out button, **Then** their session is terminated, tokens are invalidated, and they are redirected to the sign-in page.

2. **Given** a signed-out user, **When** they try to access a protected route, **Then** they are redirected to the sign-in page.

---

### User Story 5 - Forgot Password (Priority: P5)

A teacher forgets their password and needs to reset it to regain access to their account.

**Why this priority**: Password reset is important for user recovery but is less frequently used than primary authentication flows.

**Independent Test**: Can be fully tested by requesting password reset and completing the reset flow with a new password.

**Acceptance Scenarios**:

1. **Given** a registered teacher who forgot their password, **When** they enter their email on the forgot password page and submit, **Then** they receive confirmation that reset instructions have been sent (regardless of whether email exists, for security).

2. **Given** a valid password reset token, **When** the teacher enters a new password meeting requirements, **Then** their password is updated and they can sign in with the new password.

3. **Given** an expired or invalid reset token, **When** user tries to reset password, **Then** an appropriate error is displayed with option to request a new reset.

---

### Edge Cases

- What happens when network connectivity is lost during authentication? The system should display appropriate error messages and allow retry.
- How does the system handle concurrent sessions? Multiple sessions are allowed per user.
- What happens if the backend auth-service is unavailable? Users see a friendly error message indicating the service is temporarily unavailable.
- What happens when user enters email in phone field or vice versa? The backend accepts either email or phone in a single field for login.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a sign-in page where teachers can authenticate using email/phone and password
- **FR-002**: System MUST provide a registration page where new teachers can create accounts with email, phone number (Cambodia format), and password
- **FR-003**: System MUST validate email format on both client and server side
- **FR-004**: System MUST validate Cambodia phone number format (e.g., +855XXXXXXXX, 855XXXXXXXX, or 0XXXXXXXX)
- **FR-005**: System MUST enforce password strength requirements during registration (minimum 8 characters, uppercase, lowercase, digit, special character)
- **FR-006**: System MUST display password strength feedback in real-time during registration
- **FR-007**: System MUST store authentication tokens in HTTP-only cookies (access token and refresh token managed via secure cookies, not accessible to JavaScript)
- **FR-008**: System MUST automatically refresh access tokens before expiration using refresh tokens
- **FR-009**: System MUST redirect unauthenticated users to sign-in when accessing protected routes
- **FR-010**: System MUST preserve the intended destination URL and redirect after successful sign-in
- **FR-011**: System MUST provide sign-out functionality that invalidates tokens on both client and server
- **FR-012**: System MUST provide forgot password flow with email-based reset
- **FR-013**: System MUST display user-friendly error messages mapped from backend error codes
- **FR-014**: System MUST support bilingual UI (English and Khmer) with error messages; default language detected from browser preference with English as fallback
- **FR-015**: System MUST follow the design system and component structure from the shadcn-admin reference project

### Key Entities

- **AuthUser**: Represents an authenticated user session with userId, email, phoneNumber, preferredLanguage, accessToken, refreshToken, and tokenExpiry
- **AuthState**: Application state managing current user, authentication tokens, and authentication status
- **Credentials**: User-provided login information (emailOrPhone, password)
- **RegistrationData**: New user registration information (email, phoneNumber, password, preferredLanguage)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Teachers can complete sign-in within 10 seconds of entering valid credentials
- **SC-002**: Teachers can complete registration within 2 minutes, including form validation feedback
- **SC-003**: Session persistence works correctly, maintaining authentication across browser restarts for the configured session duration
- **SC-004**: Token refresh operates transparently without any user-visible interruption
- **SC-005**: All authentication error messages are displayed in the user's preferred language (English or Khmer)
- **SC-006**: Form validation provides immediate feedback (under 200ms perceived response time) for all input fields
- **SC-007**: The application correctly redirects users to their intended destination after authentication
- **SC-008**: 100% of authentication API calls use the standardized API response format with error codes

## Assumptions

- The auth-service backend is already implemented and deployed with endpoints: `/api/auth/login`, `/api/auth/register`, `/api/auth/refresh`, `/api/auth/logout`, `/api/auth/forgot-password`, `/api/auth/reset-password`
- The API Gateway is configured to route frontend requests to the auth-service
- The frontend will use the same tech stack as shadcn-admin: React 19, TypeScript, Vite, TanStack Router, TanStack Query, Zustand, Tailwind CSS, shadcn/ui components
- Cambodia phone format validation follows standard patterns: +855XXXXXXXX, 855XXXXXXXX, or 0XXXXXXXX
- The frontend will be served on a port that can communicate with the API Gateway (typically localhost:8080)

## Out of Scope

- Social authentication (Google, Facebook, GitHub sign-in) - may be added in future iterations
- Email verification flow post-registration
- Two-factor authentication (2FA)
- Account lockout UI (handled by backend, frontend only displays error)
- Admin user management
- Profile management UI (separate feature)
