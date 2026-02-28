import { useEffect } from 'react';
import { ErrorBoundary } from './components/ErrorBoundary';
import { QueryProvider } from './providers/QueryProvider';
import { AppRouter } from './routes';
import { setAuthHooks } from './api/axios';
import { useAuthStore } from './store/authStore';

/**
 * Root App component.
 * 
 * Architecture:
 * - ErrorBoundary: Catches and handles React errors
 * - QueryProvider: Provides React Query context
 * - AppRouter: Handles routing and navigation
 * - Auth hooks initialization for axios interceptors
 */
function App() {
  // Initialize auth hooks for axios interceptors
  useEffect(() => {
    setAuthHooks(
      () => useAuthStore.getState().token,
      () => useAuthStore.getState().logout()
    );
  }, []);

  return (
    <ErrorBoundary>
      <QueryProvider>
        <AppRouter />
      </QueryProvider>
    </ErrorBoundary>
  );
}

export default App;
