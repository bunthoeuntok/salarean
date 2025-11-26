import { useEffect } from 'react'
import { useLocation } from '@tanstack/react-router'
import NProgress from 'nprogress'

// Configure NProgress
NProgress.configure({
  showSpinner: false,
  trickleSpeed: 200,
  minimum: 0.1,
})

export function NavigationProgress() {
  const location = useLocation()

  useEffect(() => {
    NProgress.done()
  }, [location.pathname, location.search])

  return null
}

// Hook to manually control progress bar
export function useNavigationProgress() {
  return {
    start: () => NProgress.start(),
    done: () => NProgress.done(),
    inc: () => NProgress.inc(),
  }
}
