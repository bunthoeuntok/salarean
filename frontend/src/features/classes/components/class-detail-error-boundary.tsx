import { Component, type ErrorInfo, type ReactNode } from 'react'
import { ArrowLeft, RefreshCw } from 'lucide-react'
import { Button } from '@/components/ui/button'

interface Props {
  children: ReactNode
  onBack?: () => void
}

interface State {
  hasError: boolean
  error?: Error
}

export class ClassDetailErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props)
    this.state = { hasError: false }
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error }
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('ClassDetailErrorBoundary caught an error:', error, errorInfo)
  }

  handleRetry = () => {
    this.setState({ hasError: false, error: undefined })
  }

  render() {
    if (this.state.hasError) {
      return (
        <div
          className="flex min-h-[400px] flex-col items-center justify-center rounded-md border border-destructive/20 bg-destructive/5 p-8 text-center"
          role="alert"
        >
          <h2 className="text-xl font-semibold text-destructive">
            Something went wrong
          </h2>
          <p className="mt-2 text-sm text-muted-foreground">
            An unexpected error occurred while loading the class details.
          </p>
          <div className="mt-6 flex gap-4">
            {this.props.onBack && (
              <Button variant="outline" onClick={this.props.onBack}>
                <ArrowLeft className="mr-2 h-4 w-4" />
                Go Back
              </Button>
            )}
            <Button onClick={this.handleRetry}>
              <RefreshCw className="mr-2 h-4 w-4" />
              Try Again
            </Button>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}
