import { QueryClient, QueryCache } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { toast } from 'sonner'
import { handleServerError } from '@/lib/handle-server-error'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error) => {
        if (
          error instanceof AxiosError &&
          [401, 403].includes(error.response?.status ?? 0)
        ) {
          return false
        }
        return failureCount < 3
      },
      refetchOnWindowFocus: import.meta.env.PROD,
      staleTime: 60 * 1000,
    },
    mutations: {
      onError: (error) => {
        handleServerError(error)
      },
    },
  },
  queryCache: new QueryCache({
    onError: (error) => {
      if (error instanceof AxiosError && error.response?.status === 401) {
        toast.error('Session expired!')
        // Auth store reset and navigation handled by auth interceptor
      }
    },
  }),
})
