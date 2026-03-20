import { AuthContext } from '@/contexts/auth/AuthContext';
import { AuthContextValue } from '@/types';
import { useContext } from 'react';

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within <AuthProvider>');
  return ctx;
}
