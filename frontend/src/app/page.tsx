import { redirect } from 'next/navigation'

export default function Home() {
  // Redirect to sign-in page
  // Client-side auth check will redirect to dashboard if already authenticated
  redirect('/sign-in')
}
