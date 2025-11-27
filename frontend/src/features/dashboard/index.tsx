import { useAuthStore } from '@/store/auth-store'
import { useLanguage } from '@/context/language-provider'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { ConfigDrawer } from '@/components/config-drawer'
import { LanguageSwitcher } from '@/components/language-switcher'

export function DashboardPage() {
  const { user } = useAuthStore()
  const { t } = useLanguage()

  return (
    <>
      <Header fixed>
        <h1 className='flex-1 text-lg font-semibold'>{t.dashboard.title}</h1>
        <LanguageSwitcher />
        <ConfigDrawer />
      </Header>
      <Main>
        <div className='space-y-6'>
          <div>
            <h2 className='text-2xl font-bold tracking-tight'>
              {t.dashboard.welcome}, {user?.email?.split('@')[0] || 'Teacher'}!
            </h2>
            <p className='text-muted-foreground'>
              {t.dashboard.subtitle}
            </p>
          </div>

          <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>{t.dashboard.totalStudents}</CardTitle>
              </CardHeader>
              <CardContent>
                <div className='text-2xl font-bold'>--</div>
                <p className='text-xs text-muted-foreground'>{t.dashboard.comingSoon}</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>{t.dashboard.classes}</CardTitle>
              </CardHeader>
              <CardContent>
                <div className='text-2xl font-bold'>--</div>
                <p className='text-xs text-muted-foreground'>{t.dashboard.comingSoon}</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>{t.dashboard.attendanceRate}</CardTitle>
              </CardHeader>
              <CardContent>
                <div className='text-2xl font-bold'>--</div>
                <p className='text-xs text-muted-foreground'>{t.dashboard.comingSoon}</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>{t.dashboard.pendingTasks}</CardTitle>
              </CardHeader>
              <CardContent>
                <div className='text-2xl font-bold'>--</div>
                <p className='text-xs text-muted-foreground'>{t.dashboard.comingSoon}</p>
              </CardContent>
            </Card>
          </div>
        </div>
      </Main>
    </>
  )
}
