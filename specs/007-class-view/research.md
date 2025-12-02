# Research: Class Detail View

**Feature**: 007-class-view
**Date**: 2025-12-02
**Purpose**: Resolve technical unknowns and document technology decisions for implementation

## Research Questions

1. How to implement real-time search with debouncing using TanStack Table's filtering API?
2. What are the specific WCAG 2.1 Level AA requirements for tab navigation, search inputs, and data tables?
3. What are the best practices for preserving tab state in URL with TanStack Router?
4. How to implement lazy tab content loading without affecting accessibility?

---

## 1. TanStack Table + Real-time Search with Debouncing

### Decision

Use TanStack Table's `globalFilter` with custom debounced state (via `useDebouncedValue` hook).

### Rationale

- **TanStack Table v8** provides built-in client-side filtering via the `globalFilter` state
- **No additional dependencies**: React's state management is sufficient for debouncing
- **Performance**: Client-side filtering is fast for typical class sizes (20-100 students per page)
- **User experience**: Immediate visual feedback as user types, no server round-trips

### Implementation Approach

```typescript
// Custom debounce hook
function useDebouncedValue<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState(value)

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value)
    }, delay)

    return () => {
      clearTimeout(handler)
    }
  }, [value, delay])

  return debouncedValue
}

// In component
const [searchTerm, setSearchTerm] = useState('')
const debouncedSearch = useDebouncedValue(searchTerm, 300)

const table = useReactTable({
  data: students,
  columns,
  state: {
    globalFilter: debouncedSearch,
  },
  onGlobalFilterChange: setSearchTerm,
  globalFilterFn: 'includesString', // Case-insensitive substring match
})
```

### Alternatives Considered

| Alternative | Pros | Cons | Verdict |
|-------------|------|------|---------|
| **Server-side search** | Handles large datasets, reduces client memory | Requires API changes, network latency, more complex state management | ❌ Rejected - Unnecessary for expected class sizes (10-100 students) |
| **Lodash debounce** | Well-tested library | Adds dependency, overkill for simple use case | ❌ Rejected - React state is sufficient |
| **React useDeferredValue** | Built-in React hook | Less precise control over delay timing | ⚠️ Considered but custom hook preferred for explicit 300ms delay |

### References

- [TanStack Table - Global Filtering](https://tanstack.com/table/v8/docs/guide/global-filtering)
- [React - useDeferredValue](https://react.dev/reference/react/useDeferredValue)

---

## 2. WCAG 2.1 Level AA Compliance

### Decision

Implement ARIA roles, keyboard navigation, focus management, and 4.5:1 color contrast ratio for all interactive elements.

### Rationale

- **Legal requirement**: WCAG 2.1 AA is the standard for educational institutions in many jurisdictions
- **Inclusive design**: Ensures usability for students/teachers with disabilities (screen readers, keyboard-only navigation, visual impairments)
- **Best practice**: Accessibility improves UX for all users, not just those with disabilities

### Specific Requirements for This Feature

#### Tab Navigation

- Use semantic HTML: `<div role="tablist">`, `<button role="tab">`, `<div role="tabpanel">`
- **Keyboard support**:
  - `Tab`: Focus tab list, then move to tab panel content
  - `Arrow Left/Right`: Navigate between tabs
  - `Home/End`: Jump to first/last tab
  - `Enter/Space`: Activate focused tab
- **ARIA attributes**:
  - `aria-selected="true"` on active tab
  - `aria-controls="tabpanel-id"` linking tab to panel
  - `aria-labelledby="tab-id"` on panel
- **Focus management**: When tab is activated, focus moves to tab panel or first focusable element

#### Search Input

- `<label for="student-search">Search students by name or code</label>`
- `aria-label="Search students by name or code"` (if label hidden visually)
- **Live region** for search results count:
  ```html
  <div aria-live="polite" aria-atomic="true">
    {filteredStudents.length} students found
  </div>
  ```
- **Clear button**: `aria-label="Clear search"` with visible focus indicator

#### Data Table

- Use semantic `<table>`, `<thead>`, `<tbody>`, `<th>`, `<td>`
- `<caption>` element: "Students enrolled in {className}"
- **Sortable columns**:
  - `aria-sort="ascending"` / `"descending"` / `"none"`
  - Button inside `<th>` for sorting
- **Empty state**: Use `role="status"` or `aria-live="polite"`

#### Color Contrast

- **Text**: 4.5:1 contrast ratio (normal text), 3:1 (large text 18pt+)
- **UI components**: 3:1 contrast ratio (buttons, borders, icons)
- **Focus indicators**: 3:1 contrast ratio, 2px minimum outline

#### Focus Indicators

- Visible outline on all interactive elements when focused
- Never use `outline: none` without providing alternative focus style
- Use `:focus-visible` for keyboard-only focus styles

### Tools for Testing

| Tool | Purpose | When to Use |
|------|---------|-------------|
| **axe DevTools** (browser extension) | Automated accessibility scanning | During development (real-time) |
| **Playwright + axe-core** | Automated E2E accessibility tests | CI/CD pipeline |
| **NVDA / JAWS** (screen readers) | Manual testing with assistive tech | Before release (critical flows) |
| **Keyboard-only navigation** | Manual testing without mouse | During development |
| **Color Contrast Analyzer** | Verify contrast ratios | Design phase |

### Implementation Checklist

- [ ] Tab navigation with arrow keys implemented
- [ ] Search input has proper label and live region
- [ ] Table uses semantic HTML with caption
- [ ] Status filter dropdown is keyboard-accessible
- [ ] Focus indicators visible on all interactive elements
- [ ] Color contrast ratios meet 4.5:1 (text) and 3:1 (UI components)
- [ ] Automated tests with axe-core passing
- [ ] Manual keyboard navigation tested

### Alternatives Considered

| Alternative | Pros | Cons | Verdict |
|-------------|------|------|---------|
| **WCAG AAA** | Highest accessibility standard | Too strict for general use (7:1 contrast, advanced audio descriptions) | ❌ Rejected - AA is sufficient for educational software |
| **Basic keyboard support only** | Minimal effort | Doesn't meet legal requirements, poor screen reader support | ❌ Rejected - Not compliant |
| **No accessibility requirements** | Fastest to implement | Legal risk, excludes users with disabilities | ❌ Rejected - Unacceptable for educational software |

### References

- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [ARIA Authoring Practices - Tabs Pattern](https://www.w3.org/WAI/ARIA/apg/patterns/tabs/)
- [WebAIM - Keyboard Accessibility](https://webaim.org/techniques/keyboard/)
- [axe-core GitHub](https://github.com/dequelabs/axe-core)

---

## 3. URL-based Tab Routing with TanStack Router

### Decision

Use TanStack Router search params with `useSearch` hook and Zod schema validation.

### Rationale

- **Bookmarkable URLs**: Users can share direct links to specific tabs (e.g., `/classes/123?tab=students`)
- **Browser history**: Back/forward buttons work correctly
- **Type safety**: Zod schema ensures only valid tab names accepted
- **SSR-friendly**: Search params work with server-side rendering (if needed in future)

### Implementation Approach

```typescript
// Route file: routes/_authenticated/classes.$id.tsx
import { createFileRoute } from '@tanstack/react-router'
import { z } from 'zod'

const classDetailSearchSchema = z.object({
  tab: z.enum(['students', 'schedule', 'attendance', 'grades'])
    .optional()
    .default('students')
})

export const Route = createFileRoute('/_authenticated/classes/$id')({
  validateSearch: classDetailSearchSchema,
})

// In component
function ClassDetailPage() {
  const { tab } = Route.useSearch() // Type-safe: 'students' | 'schedule' | 'attendance' | 'grades'
  const navigate = Route.useNavigate()

  const handleTabChange = (newTab: string) => {
    navigate({
      search: (prev) => ({ ...prev, tab: newTab })
    })
  }

  return (
    <Tabs value={tab} onValueChange={handleTabChange}>
      {/* Tab content */}
    </Tabs>
  )
}
```

### Benefits

- **Persistence**: Refreshing page preserves active tab
- **Analytics**: Track tab views in analytics tools (URL changes)
- **SEO**: Search engines can index different tabs (if made public in future)
- **Deep linking**: Direct navigation to specific tab from external links

### Alternatives Considered

| Alternative | Pros | Cons | Verdict |
|-------------|------|------|---------|
| **Hash-based routing** (#students) | Simple, no router changes needed | Poor SEO, doesn't trigger browser history, limited analytics | ❌ Rejected - Inferior UX |
| **Separate routes per tab** (/classes/123/students) | Clean URLs, RESTful | Over-engineering (4 routes for one page), route duplication | ❌ Rejected - Too complex |
| **Local state only** (no URL sync) | Simplest implementation | No bookmarking, no browser history, state lost on refresh | ❌ Rejected - Poor UX |
| **localStorage + URL sync** | Preserves state across sessions | Complexity, stale state issues, privacy concerns | ❌ Rejected - URL search params sufficient |

### References

- [TanStack Router - Search Params](https://tanstack.com/router/latest/docs/framework/react/guide/search-params)
- [Zod Documentation](https://zod.dev/)

---

## 4. Lazy Tab Loading Strategy

### Decision

Use React.lazy() for tab content components with Suspense fallback, while keeping tab panel wrappers in DOM with `hidden` attribute.

### Rationale

- **Accessibility**: Screen readers need all tab panels in DOM to announce count ("4 tabs available")
- **Performance**: Tab content (StudentsList component) only loaded when clicked
- **User experience**: Instant tab switching for already-loaded tabs (cached by React)

### Implementation Approach

```typescript
// Lazy load tab content components
const StudentsTab = React.lazy(() => import('./components/students-tab'))
const ScheduleTab = React.lazy(() => import('./components/schedule-tab'))
const AttendanceTab = React.lazy(() => import('./components/attendance-tab'))
const GradesTab = React.lazy(() => import('./components/grades-tab'))

function ClassDetailPage() {
  const { tab } = Route.useSearch()

  return (
    <Tabs value={tab}>
      <TabsList>
        <TabsTrigger value="students">Students</TabsTrigger>
        <TabsTrigger value="schedule">Schedule</TabsTrigger>
        <TabsTrigger value="attendance">Attendance</TabsTrigger>
        <TabsTrigger value="grades">Grades</TabsTrigger>
      </TabsList>

      {/* All tab panels rendered, but content lazy-loaded */}
      <TabsContent value="students">
        <Suspense fallback={<StudentListSkeleton />}>
          <StudentsTab classId={classId} />
        </Suspense>
      </TabsContent>

      <TabsContent value="schedule">
        <Suspense fallback={<div>Loading schedule...</div>}>
          <ScheduleTab classId={classId} />
        </Suspense>
      </TabsContent>

      <TabsContent value="attendance">
        <Suspense fallback={<div>Loading attendance...</div>}>
          <AttendanceTab classId={classId} />
        </Suspense>
      </TabsContent>

      <TabsContent value="grades">
        <Suspense fallback={<div>Loading grades...</div>}>
          <GradesTab classId={classId} />
        </Suspense>
      </TabsContent>
    </Tabs>
  )
}
```

### How It Works

1. **Initial render**: All `<TabsContent>` components rendered, but inactive tabs have `hidden` attribute
2. **Tab click**: User clicks "Schedule" tab
3. **React.lazy() triggers**: `ScheduleTab` component code splits and loads (dynamic import)
4. **Suspense fallback**: Shows loading skeleton while code downloads
5. **Content renders**: Once loaded, component renders and is cached by React
6. **Subsequent clicks**: Already-loaded tabs render instantly (no re-fetch)

### Accessibility Considerations

- **Tab count announcement**: Screen readers announce "4 tabs" because all `<TabsContent>` exist in DOM
- **Hidden attribute**: Browser accessibility APIs correctly hide inactive tab panels
- **Focus management**: When tab is activated, focus moves to tab panel (handled by shadcn/ui Tabs component)

### Performance Benefits

- **Initial page load**: Only Students tab content downloads (smallest bundle)
- **On-demand loading**: Schedule/Attendance/Grades tabs only load if clicked
- **Code splitting**: Each tab component in separate chunk (better caching)
- **Network optimization**: Reduces initial bundle size by ~30-40% (estimated)

### Alternatives Considered

| Alternative | Pros | Cons | Verdict |
|-------------|------|------|---------|
| **Conditional rendering** (remove hidden tabs from DOM) | Simplest code | Screen readers can't announce tab count, violates WCAG | ❌ Rejected - Accessibility violation |
| **Preload all tabs on mount** | No loading delay on tab click | Wastes bandwidth, slows initial page load, unnecessary API calls | ❌ Rejected - Poor performance |
| **Server-side rendering for all tabs** | SEO benefits (if public) | Complex setup, unnecessary for authenticated pages | ❌ Rejected - Over-engineering |
| **Dynamic import without Suspense** | More control over loading state | Manual error handling, no fallback UI during load | ❌ Rejected - Suspense is best practice |

### References

- [React - lazy()](https://react.dev/reference/react/lazy)
- [React - Suspense](https://react.dev/reference/react/Suspense)
- [WCAG - Hidden Content](https://www.w3.org/WAI/WCAG21/Techniques/aria/ARIA19)

---

## Summary

All research questions resolved. Key decisions:

1. **TanStack Table** with custom debounce hook (300ms)
2. **WCAG 2.1 Level AA** compliance with ARIA, keyboard nav, 4.5:1 contrast
3. **TanStack Router search params** for tab state persistence
4. **React.lazy + Suspense** for tab content, tab panels in DOM for accessibility

No technical blockers identified. Ready to proceed to Phase 1 (Data Model & API Contracts).
