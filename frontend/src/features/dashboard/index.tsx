import { useAuthStore } from '@/store/auth-store'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'

export function DashboardPage() {
  const { user } = useAuthStore()

  return (
    <>
      <Header fixed>
        <h1 className='text-lg font-semibold'>Dashboard</h1>
      </Header>
      <Main>
        <div className='space-y-6'>
          <div>
            <h2 className='text-2xl font-bold tracking-tight'>
              Welcome back, {user?.email?.split('@')[0] || 'Teacher'}!
            </h2>
            <p className='text-muted-foreground'>
              Here's what's happening with your classes today.
            </p>
          </div>

          <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>Total Students</CardTitle>
              </CardHeader>
              <CardContent>
                <div className='text-2xl font-bold'>--</div>
                <p className='text-xs text-muted-foreground'>Coming soon</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>Classes</CardTitle>
              </CardHeader>
              <CardContent>
                <div className='text-2xl font-bold'>--</div>
                <p className='text-xs text-muted-foreground'>Coming soon</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>Attendance Rate</CardTitle>
              </CardHeader>
              <CardContent>
                <div className='text-2xl font-bold'>--</div>
                <p className='text-xs text-muted-foreground'>Coming soon</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>Pending Tasks</CardTitle>
              </CardHeader>
              <CardContent>
                <div className='text-2xl font-bold'>--</div>
                <p className='text-xs text-muted-foreground'>Coming soon</p>
              </CardContent>
            </Card>
          </div>
        </div>
      </Main>
    </>
  )
}
