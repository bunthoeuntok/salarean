# DataTable Components Guide

This directory contains a comprehensive data table system with different variants optimized for specific use cases.

## 📋 Quick Reference

| Component | Use Case | Pagination | Sorting | Search | URL Params |
|-----------|----------|------------|---------|--------|------------|
| `ClientDataTable` | Small datasets, simple | ❌ None | ✅ Client | ✅ Local | ❌ No |
| `ClientDataTableWithUrl` | Small datasets, shareable | ❌ None | ✅ Client | ✅ Client | ✅ Search + Sort |
| `ServerDataTable` | Large datasets, paginated | ✅ Server | ✅ Server | ✅ Server | ✅ All |
| `DataTable` (base) | Custom requirements | 🔧 Custom | 🔧 Custom | 🔧 Custom | 🔧 Custom |

---

## 🎯 Component Selection Guide

### Use `ClientDataTable` when:
- ✅ Dataset < 1000 rows
- ✅ All data loaded upfront
- ✅ No URL persistence needed
- ✅ Simple use cases (modals, embedded tables)

**Example:**
```tsx
import { ClientDataTable } from '@/components/data-table'

function StudentModal() {
  const students = [...] // All data loaded

  return (
    <ClientDataTable
      data={students}
      columns={columns}
      searchPlaceholder="Search students..."
      enableColumnResizing
    />
  )
}
```

---

### Use `ClientDataTableWithUrl` when:
- ✅ Dataset < 1000 rows
- ✅ All data loaded upfront
- ✅ Want shareable search & sort links
- ✅ Search & sort state should persist on refresh

**Example:**
```tsx
import { ClientDataTableWithUrl } from '@/components/data-table'

function ClassStudentsTab({ classId }: { classId: string }) {
  const { data } = useClassStudents({ classId }) // Fetch all students

  return (
    <ClientDataTableWithUrl
      data={data?.students ?? []}
      columns={columns}
      storageKey={`class-students-${classId}`}
      searchPlaceholder="Search students..."
      enableColumnResizing
      enableColumnReordering
    />
  )
}
```

**URL Example:**
```
/classes/abc-123?search=john&sort=fullName&sortDir=asc
```

**Benefits:**
- 🔗 Shareable URLs with search & sort state
- 🔄 Search & sort persist on page refresh
- ⬅️ Browser back/forward works with search & sort
- 🚀 No API calls on sort/filter (client-side operations)
- 📋 Perfect for sharing filtered/sorted views with team

---

### Use `ServerDataTable` when:
- ✅ Dataset > 1000 rows
- ✅ Paginated API endpoint
- ✅ Backend handles sorting/filtering
- ✅ Want full URL persistence (page, size, search, sort)

**Example:**
```tsx
import { ServerDataTable } from '@/components/data-table'

function StudentsPage() {
  // TanStack Query automatically refetches when URL params change
  const { data, isLoading } = useQuery({
    queryKey: ['students', pageIndex, pageSize, searchValue, sorting, filters],
    queryFn: () => studentService.getStudents({
      page: pageIndex,
      size: pageSize,
      search: searchValue,
      sort: sorting[0] ? `${sorting[0].id},${sorting[0].desc ? 'desc' : 'asc'}` : undefined,
    })
  })

  return (
    <ServerDataTable
      data={data?.content ?? []}
      columns={columns}
      pageCount={data?.totalPages ?? 0}
      storageKey="students-table"
      isLoading={isLoading}
      searchPlaceholder="Search all students..."
    />
  )
}
```

**How it works:**
1. Component reads URL params (`?page=2&size=20&search=john&sort=name,asc`)
2. User changes page/sort/search → URL updates
3. TanStack Query sees URL change → triggers API call
4. Backend returns filtered/sorted/paginated data
5. Table displays results

**Benefits:**
- 🔗 Fully shareable URLs
- 📊 Backend handles heavy operations
- 💾 Efficient with large datasets
- 🔄 All state persists on refresh

---

### Use `DataTable` (base) when:
- ✅ Custom requirements not covered by wrappers
- ✅ Hybrid client/server operations
- ✅ Advanced features (row selection callbacks, custom toolbars)

**Example:**
```tsx
import { DataTable } from '@/components/data-table'

function CustomTable() {
  const [selectedRows, setSelectedRows] = useState([])

  return (
    <DataTable
      data={data}
      columns={columns}
      enableRowSelection
      onRowSelectionChange={setSelectedRows}
      // Full control over all props
    />
  )
}
```

---

## 🚀 Migration Examples

### Before (using base DataTable):
```tsx
// Lots of boilerplate
const { searchValue, sorting, setSorting, updateParams } = useTableUrlParams()
const handleSearchChange = (value: string) => updateParams({ search: value })

return (
  <DataTable
    data={data}
    columns={columns}
    searchValue={searchValue}
    onSearchChange={handleSearchChange}
    sorting={sorting}
    onSortingChange={setSorting}
    showPagination={false}
    storageKey="students"
    // ... more props
  />
)
```

### After (using wrapper):
```tsx
// Clean and simple
return (
  <ClientDataTableWithUrl
    data={data}
    columns={columns}
    storageKey="students"
    searchPlaceholder="Search..."
  />
)
```

---

## 🎨 Features Comparison

### Search
- **ClientDataTable**: Local state only (resets on unmount)
- **ClientDataTableWithUrl**: URL-persisted (shareable, survives refresh)
- **ServerDataTable**: URL-persisted + triggers API calls

### Sorting
- **ClientDataTable**: Client-side (TanStack Table), no URL persistence
- **ClientDataTableWithUrl**: Client-side (TanStack Table) + URL-persisted
- **ServerDataTable**: Server-side (triggers API calls) + URL-persisted

### Pagination
- **ClientDataTable**: None (shows all rows)
- **ClientDataTableWithUrl**: None (shows all rows)
- **ServerDataTable**: Server-side with URL persistence

### Debouncing
- All components have **300ms debounced search** to prevent excessive URL updates

---

## 📦 Props Reference

### Common Props (all components)
```typescript
{
  data: TData[]
  columns: ColumnDef<TData>[]
  isLoading?: boolean
  enableColumnResizing?: boolean
  enableColumnReordering?: boolean
  enableRowSelection?: boolean
  showToolbar?: boolean
  columnLabels?: Record<string, string>
  toolbarActions?: React.ReactNode
}
```

### ClientDataTable Specific
```typescript
{
  enableSearch?: boolean  // Default: true
  initialSearchValue?: string
  searchPlaceholder?: string
}
```

### ClientDataTableWithUrl Specific
```typescript
{
  storageKey?: string  // For localStorage persistence
  searchPlaceholder?: string
}
```

### ServerDataTable Specific
```typescript
{
  storageKey: string  // Required!
  pageCount: number   // From API response
  searchPlaceholder?: string
}
```

---

## 🔧 Advanced Usage

### With Filters (ServerDataTable)

Use `DataTableFilterToolbar` for advanced filtering:

```tsx
import { ServerDataTable, DataTableFilterToolbar } from '@/components/data-table'

function StudentsPage() {
  const { data } = useQuery(...)

  return (
    <>
      <DataTableFilterToolbar
        initialSearch={searchValue}
        initialFilters={filters}
        filterableColumns={[
          {
            id: 'status',
            title: 'Status',
            options: [
              { label: 'Active', value: 'ACTIVE' },
              { label: 'Inactive', value: 'INACTIVE' }
            ]
          }
        ]}
        onSubmit={submitFilters}
        onReset={resetFilters}
      />

      <ServerDataTable
        data={data?.content ?? []}
        columns={columns}
        pageCount={data?.totalPages ?? 0}
        storageKey="students-table"
        showToolbar={false}  // Using custom toolbar above
      />
    </>
  )
}
```

---

## 💡 Best Practices

1. **Choose the simplest component** that meets your needs
2. **Use `storageKey`** to persist column widths/order/visibility
3. **For server-side tables**, ensure your API returns:
   ```typescript
   {
     content: T[]
     totalPages: number
     totalElements: number
     page: number
     size: number
   }
   ```
4. **For client-side tables**, fetch all data upfront (no lazy loading)
5. **Use URL params** for shareable state (search, filters)

---

## 🐛 Troubleshooting

### Search not working
- **ClientDataTable**: Check `enableSearch` is not `false`
- **ClientDataTableWithUrl/ServerDataTable**: Check columns have proper `accessorKey`

### Sorting not working
- **Client-side**: Columns must have `accessorKey` defined
- **Client-side with URL**: Ensure `manualSorting={false}` is set (automatic in wrappers)
- **Server-side**: Backend must handle `sort` query param, `manualSorting={true}` required

### Pagination not showing
- **ClientDataTable/ClientDataTableWithUrl**: Pagination is disabled by design
- **ServerDataTable**: Check `pageCount` prop is provided

### URL not updating
- **ClientDataTable**: URL persistence is disabled by design
- **ClientDataTableWithUrl**: Check router supports search params
- **ServerDataTable**: Verify `useTableUrlParams` hook is working

---

## 📚 Related Documentation

- [TanStack Table Docs](https://tanstack.com/table/latest)
- [useTableUrlParams Hook](./use-table-url-params.ts)
- [Column Definitions Guide](../../../docs/table-columns.md)

---

**Version**: 1.0.0
**Last Updated**: 2025-12-04
