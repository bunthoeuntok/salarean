export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24">
      <div className="text-center">
        <h1 className="text-4xl font-bold mb-4">Student Management System</h1>
        <p className="text-lg text-muted-foreground mb-8">
          Comprehensive school management for Cambodia
        </p>
        <div className="flex gap-4 justify-center">
          <a
            href="/login"
            className="px-6 py-3 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors"
          >
            Login
          </a>
          <a
            href="/register"
            className="px-6 py-3 border border-input rounded-lg hover:bg-accent transition-colors"
          >
            Register
          </a>
        </div>
      </div>
    </main>
  )
}
