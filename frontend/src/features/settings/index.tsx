import { Outlet } from '@tanstack/react-router'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'

export function Settings() {
  return (
    <>
      <Header fixed />
      <Main fixed>
        <Outlet />
      </Main>
    </>
  )
}
