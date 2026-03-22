import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AppRouter } from '@/router/AppRouter';
import { AuthProvider } from './contexts/auth/AuthProvider';
import { Toaster } from './components/ui/sonner';

/**
 * Provider order matters:
 * 1. QueryClientProvider  — TanStack Query, no deps
 * 2. AuthProvider         — needs QueryClient (uses apiClient internally)
 * 3. GameProvider (Incoming)        — needs Auth (connects WS with JWT)
 * 4. AppRouter            — needs all of the above
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
    },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <Toaster
          toastOptions={{
            style: {
              background: 'hsl(var(--card))',
              color: 'hsl(var(--card-foreground))',
              border: '1px solid hsl(var(--border))',
            },
          }}
        />
        <AppRouter />
      </AuthProvider>
    </QueryClientProvider>
  );
}
