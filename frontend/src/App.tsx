import { ErrorBoundary } from './components/ErrorBoundary';
import { QueryProvider } from './providers/QueryProvider';
import { AppRouter } from './routes';

/**
 * Root App component.
 * 
 * Architecture:
 * - ErrorBoundary: Catches and handles React errors
 * - QueryProvider: Provides React Query context
 * - AppRouter: Handles routing and navigation
 */
function App() {
  return (
    <ErrorBoundary>
      <QueryProvider>
        <AppRouter />
      </QueryProvider>
    </ErrorBoundary>
  );
}

export default App;
