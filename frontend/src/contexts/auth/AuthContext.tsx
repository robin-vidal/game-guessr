import { createContext } from 'react';
import { AuthContextValue } from './AuthProvider';

export const AuthContext = createContext<AuthContextValue | null>(null);
