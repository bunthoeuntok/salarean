import { cn } from '@/lib/utils'
import { getCookie } from '@/lib/cookies'
import { DirectionProvider } from '@/context/direction-provider'
import { LanguageProvider } from '@/context/language-provider'
import { LayoutProvider } from '@/context/layout-provider'
import { SidebarInset, SidebarProvider } from '@/components/ui/sidebar'
import { AppSidebar } from '@/components/layout/app-sidebar'

type AuthenticatedLayoutProps = {
  children?: React.ReactNode
}

export function AuthenticatedLayout({ children }: AuthenticatedLayoutProps) {
  const defaultOpen = getCookie('sidebar_state') !== 'false'
  return (
    <LanguageProvider>
      <DirectionProvider>
        <LayoutProvider>
          <SidebarProvider defaultOpen={defaultOpen}>
            <AppSidebar />
            <SidebarInset
              className={cn(
                '@container/content',
                'has-data-[layout=fixed]:h-svh',
                'peer-data-[variant=inset]:has-data-[layout=fixed]:h-[calc(100svh-(var(--spacing)*4))]'
              )}
            >
              {children}
            </SidebarInset>
          </SidebarProvider>
        </LayoutProvider>
      </DirectionProvider>
    </LanguageProvider>
  )
}
