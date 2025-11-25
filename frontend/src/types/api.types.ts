/**
 * Standard API response wrapper matching backend ApiResponse<T>
 */
export interface ApiResponse<T> {
  errorCode: string // "SUCCESS" or error code enum value
  data: T | null
}

/**
 * Pagination wrapper for list responses
 */
export interface PagedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number // current page (0-indexed)
}
