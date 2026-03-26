import { AuthContext } from '@/contexts/auth/AuthContext';
import { AuthContextValue } from '@/contexts/auth/AuthProvider';
import { useContext } from 'react';

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used inside <AuthProvider>');
  }
  return ctx;
}
