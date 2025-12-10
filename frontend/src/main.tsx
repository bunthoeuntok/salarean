import { StrictMode } from 'react'
import ReactDOM from 'react-dom/client'
import {
  QueryClient,
  QueryClientProvider,
  QueryCache,
} from '@tanstack/react-query'
import { RouterProvider, createRouter } from '@tanstack/react-router'
import { AxiosError } from 'axios'
import { toast } from 'sonner'
import { routeTree } from './routeTree.gen'
import { ThemeProvider } from './context/theme-provider'
import { useAuthStore } from './store/auth-store'
import { handleServerError } from './lib/handle-server-error'
import './styles/index.css'
import './lib/i18n'
// Import display store to initialize text size on page load
import './store/display-store'

const queryClient = new QueryClient({
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
        useAuthStore.getState().reset()
        const redirect = router.state.location.pathname
        router.navigate({ to: '/sign-in', search: { redirect } })
      }
    },
  }),
})

const router = createRouter({
  routeTree,
  context: { queryClient },
  defaultPreload: 'intent',
  defaultPreloadStaleTime: 0,
})

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router
  }
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <ThemeProvider defaultTheme="system" storageKey="sms-theme">
        <RouterProvider router={router} />
      </ThemeProvider>
    </QueryClientProvider>
  </StrictMode>
)
