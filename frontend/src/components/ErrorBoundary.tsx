import { Component } from 'react';
import type { ErrorInfo, ReactNode } from 'react';
import { Button } from '../components/ui';

interface ErrorBoundaryProps {
  children: ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
}

/**
 * ErrorBoundary component - Catches React errors and displays fallback UI.
 * 
 * Features:
 * - Catches errors in child component tree
 * - Displays user-friendly error message
 * - Provides reset button to recover
 * - Logs errors to console (can be extended to error tracking service)
 */
export class ErrorBoundary extends Component<
  ErrorBoundaryProps,
  ErrorBoundaryState
> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
    };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return {
      hasError: true,
      error,
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    // Log error to console (can be replaced with error tracking service)
    console.error('ErrorBoundary caught an error:', error, errorInfo);
  }

  handleReset = (): void => {
    this.setState({
      hasError: false,
      error: null,
    });
    // Reload the page to reset the app state
    window.location.href = '/';
  };

  render(): ReactNode {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-gray-50 flex flex-col justify-center items-center px-4">
          <div className="max-w-md text-center">
            <div className="text-6xl mb-6">⚠️</div>
            <h1 className="text-3xl font-bold text-gray-900 mb-4">
              Something went wrong
            </h1>
            <p className="text-gray-600 mb-6">
              We're sorry for the inconvenience. An unexpected error has
              occurred.
            </p>
            {this.state.error && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6 text-left">
                <p className="text-sm text-red-800 font-mono wrap-break-word">
                  {this.state.error.message}
                </p>
              </div>
            )}
            <Button onClick={this.handleReset} size="lg">
              Go Back Home
            </Button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
