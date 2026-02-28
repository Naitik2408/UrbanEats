import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';

/**
 * React Query configuration.
 * 
 * Global settings:
 * - staleTime: 5 minutes (data considered fresh for 5 minutes)
 * - retry: 1 (only retry failed requests once)
 * - refetchOnWindowFocus: false (don't refetch when window regains focus)
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

interface QueryProviderProps {
  children: ReactNode;
}

/**
 * QueryProvider component - Wraps app with React Query context.
 */
export const QueryProvider: React.FC<QueryProviderProps> = ({ children }) => {
  return (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};
