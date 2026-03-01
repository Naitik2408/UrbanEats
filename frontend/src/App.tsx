import { ErrorBoundary } from './components/ErrorBoundary';
import { QueryProvider } from './providers/QueryProvider';
import { AppRouter } from './routes';
import ToastContainer from './components/ui/ToastContainer';

/**
 * Root App component.
 * 
 * Architecture:
 * - ErrorBoundary: Catches and handles React errors
 * - QueryProvider: Provides React Query context
 * - AppRouter: Handles routing and navigation
 * - ToastContainer: Global toast notifications
 */
function App() {
  return (
    <ErrorBoundary>
      <QueryProvider>
        <AppRouter />
        <ToastContainer />
      </QueryProvider>
    </ErrorBoundary>
  );
}

export default App;
