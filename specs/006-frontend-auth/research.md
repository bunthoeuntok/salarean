# Research: Frontend Authentication Integration

**Feature**: 006-frontend-auth
**Date**: 2025-11-25
**Status**: Complete

## Research Topics

### 1. HTTP-Only Cookie Authentication with React SPA

**Decision**: Backend sets HTTP-only cookies on successful authentication; frontend relies on cookie presence for auth state.

**Rationale**:
- HTTP-only cookies cannot be accessed by JavaScript, mitigating XSS token theft
- Cookies are automatically sent with requests to the same origin
- Backend auth-service must be modified to set cookies instead of returning tokens in response body
- Frontend uses `/api/auth/me` endpoint to verify authentication status on app load

**Alternatives Considered**:
1. **localStorage tokens**: Rejected - vulnerable to XSS attacks
2. **Memory-only tokens with refresh**: Rejected - poor UX on page reload (requires re-authentication)
3. **Hybrid (memory + refresh in httpOnly)**: More complex, deferred for future if needed

**Implementation Notes**:
- Backend must set `Set-Cookie` header with `HttpOnly`, `Secure`, `SameSite=Strict` flags
- Frontend Axios client configured with `withCredentials: true`
- CORS configuration must allow credentials from frontend origin
- Consider adding `/api/auth/me` endpoint for session validation

### 2. TanStack Router Protected Routes

**Decision**: Use file-based routing with `_authenticated` layout route for protected pages.

**Rationale**:
- TanStack Router's `beforeLoad` hook provides route-level guards
- Layout routes (`_authenticated/route.tsx`) can check auth state before rendering children
- Integrates well with TanStack Query for data prefetching

**Alternatives Considered**:
1. **Higher-order component wrapper**: Rejected - less idiomatic for TanStack Router
2. **Global middleware**: Not supported in TanStack Router's architecture
3. **Context-based guards**: Works but layout routes are cleaner

**Implementation Pattern**:
```typescript
// routes/_authenticated/route.tsx
export const Route = createFileRoute('/_authenticated')({
  beforeLoad: async ({ context }) => {
    const isAuthenticated = await checkAuthStatus()
    if (!isAuthenticated) {
      throw redirect({ to: '/sign-in', search: { redirect: location.pathname } })
    }
  },
  component: AuthenticatedLayout,
})
```

### 3. Zustand Auth State Management

**Decision**: Use Zustand with persistence middleware for auth state, storing only user metadata (not tokens).

**Rationale**:
- Tokens stored in HTTP-only cookies (not accessible to JS)
- Zustand stores: `isAuthenticated`, `user` (profile data), `preferredLanguage`
- On app load, call `/api/auth/me` to validate session and hydrate store
- Simple API, no boilerplate compared to Redux

**State Shape**:
```typescript
interface AuthState {
  isAuthenticated: boolean
  user: AuthUser | null
  preferredLanguage: 'en' | 'km'
  setUser: (user: AuthUser | null) => void
  setLanguage: (lang: 'en' | 'km') => void
  reset: () => void
}
```

### 4. Bilingual i18n (English/Khmer)

**Decision**: Lightweight custom i18n using TypeScript objects with browser language detection.

**Rationale**:
- Only 2 languages needed (en, km)
- Error codes mapped to translations in a single file
- Browser's `navigator.language` for detection, localStorage for persistence
- Avoids heavy i18n library dependency (i18next, react-intl)

**Alternatives Considered**:
1. **i18next**: Rejected - overkill for 2 languages with primarily error code translations
2. **react-intl**: Rejected - adds complexity, designed for more extensive i18n
3. **Custom context provider**: Selected - simple, fits project needs

**Implementation Pattern**:
```typescript
// lib/i18n/translations.ts
export const translations = {
  en: {
    INVALID_CREDENTIALS: 'Invalid email/phone or password',
    DUPLICATE_EMAIL: 'This email is already registered',
    // ...
  },
  km: {
    INVALID_CREDENTIALS: 'អ៊ីមែល/ទូរស័ព្ទ ឬពាក្យសម្ងាត់មិនត្រឹមត្រូវ',
    DUPLICATE_EMAIL: 'អ៊ីមែលនេះបានចុះឈ្មោះរួចហើយ',
    // ...
  }
}
```

### 5. Cambodia Phone Number Validation

**Decision**: Use Zod custom validator with regex pattern for Cambodia phone formats.

**Rationale**:
- Cambodia formats: `+855XXXXXXXXX`, `855XXXXXXXXX`, `0XXXXXXXXX`
- Mobile prefixes: 10, 11, 12, 15, 16, 17, 18, 69, 70, 71, 76, 77, 78, 79, 85, 86, 87, 88, 89, 90, 91, 92, 93, 95, 96, 97, 98, 99
- Zod validation aligns with backend's `@KhmerPhone` annotation

**Implementation Pattern**:
```typescript
const khmerPhoneRegex = /^(\+855|855|0)(1[0-2]|1[5-8]|69|7[0-9]|8[5-9]|9[0-9])\d{6}$/

const phoneSchema = z.string()
  .regex(khmerPhoneRegex, 'Invalid Cambodia phone number format')
```

### 6. Password Strength Validation

**Decision**: Real-time password strength indicator with Zod validation matching backend rules.

**Rationale**:
- Backend requires: min 8 chars, uppercase, lowercase, digit, special character
- Frontend provides immediate feedback during typing
- Visual strength meter (weak/medium/strong) for UX
- Zod schema ensures consistency with backend validation

**Implementation Pattern**:
```typescript
const passwordSchema = z.string()
  .min(8, 'Password must be at least 8 characters')
  .regex(/[A-Z]/, 'Password must contain uppercase letter')
  .regex(/[a-z]/, 'Password must contain lowercase letter')
  .regex(/[0-9]/, 'Password must contain a digit')
  .regex(/[@#$%^&+=!*()_-]/, 'Password must contain special character')
```

### 7. Token Refresh Strategy

**Decision**: Axios interceptor with 401 response handling for automatic token refresh.

**Rationale**:
- Backend returns 401 when access token expires
- Interceptor catches 401, calls `/api/auth/refresh`, retries original request
- Refresh token in HTTP-only cookie, automatically included
- If refresh fails, redirect to sign-in

**Implementation Pattern**:
```typescript
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401 && !error.config._retry) {
      error.config._retry = true
      try {
        await axios.post('/api/auth/refresh', {}, { withCredentials: true })
        return axiosInstance(error.config)
      } catch {
        // Refresh failed, redirect to sign-in
        authStore.getState().reset()
        window.location.href = '/sign-in'
      }
    }
    return Promise.reject(error)
  }
)
```

### 8. Backend Cookie Support Assessment

**Decision**: Verify auth-service can be configured for HTTP-only cookie responses.

**Research Finding**:
- Current auth-service returns tokens in JSON response body
- Backend modification needed to support cookie-based auth
- Options:
  1. Modify auth-service to set cookies (preferred)
  2. Use frontend-managed tokens with localStorage (fallback, less secure)

**Recommendation**:
- If backend modification is feasible, implement HTTP-only cookies (spec decision)
- If not, document security tradeoff and use localStorage with XSS mitigations

**Required Backend Changes** (if HTTP-only cookies):
```java
// In AuthController.java - after successful login
ResponseCookie accessCookie = ResponseCookie.from("access_token", token)
    .httpOnly(true)
    .secure(true) // Set to false for localhost development
    .sameSite("Strict")
    .path("/")
    .maxAge(Duration.ofMinutes(15))
    .build();
response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
```

## Summary of Decisions

| Topic | Decision | Risk Level |
|-------|----------|------------|
| Token Storage | HTTP-only cookies | Low (if backend supports) |
| Route Protection | TanStack Router layout routes | Low |
| State Management | Zustand (user metadata only) | Low |
| i18n | Custom TypeScript translations | Low |
| Phone Validation | Zod regex (Cambodia formats) | Low |
| Password Validation | Real-time Zod validation | Low |
| Token Refresh | Axios 401 interceptor | Low |
| Backend Cookies | Requires auth-service modification | Medium |

## Open Items

1. **Backend Cookie Support**: Confirm with backend team that auth-service can be modified to set HTTP-only cookies. If not feasible, fall back to localStorage with documented security considerations.

2. **Add `/api/auth/me` Endpoint**: Backend needs endpoint to validate session and return current user (for app initialization).

## References

- [TanStack Router Auth Guide](https://tanstack.com/router/latest/docs/framework/react/guide/authenticated-routes)
- [Zustand Persist Middleware](https://docs.pmnd.rs/zustand/integrations/persisting-store-data)
- [OWASP Session Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html)
- [Cambodia Mobile Number Prefixes](https://en.wikipedia.org/wiki/Telephone_numbers_in_Cambodia)
