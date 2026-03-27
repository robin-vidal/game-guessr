import { useCallback, useMemo, useState } from 'react';

import { useMutation } from '@tanstack/react-query';
import { AuthResponse, LoginRequest, RegisterRequest } from '@/client/auth-service';
import {
  loginMutation,
  logoutMutation,
  registerMutation,
} from '@/client/auth-service/@tanstack/react-query.gen';
import { AuthContext } from './AuthContext';
import { defaultConfig } from '@/client/config';

function decodeJwt(token: string): Record<string, unknown> | null {
  try {
    const [, payloadB64] = token.split('.');
    const json = atob(payloadB64.replace(/-/g, '+').replace(/_/g, '/'));
    const payload = JSON.parse(json) as Record<string, unknown>;

    // Reject already-expired tokens immediately
    if (typeof payload.exp === 'number' && payload.exp * 1000 < Date.now()) {
      return null;
    }

    return payload;
  } catch {
    return null;
  }
}

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

export interface AuthUser {
  id: string;
  username: string;
  token: string;
}

export interface AuthContextValue {
  /** Currently authenticated user, or null when logged out */
  user: AuthUser | null;
  /** True while any auth operation (login / register / logout) is in flight */
  isLoading: boolean;
  /** True once the initial token check on mount has finished */
  isInitialized: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (credentials: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
}

export function AuthProvider({ children }: Readonly<{ children: React.ReactNode }>) {
  const [user, setUser] = useState<AuthUser | null>(() => {
    // Lazy initializer: runs once synchronously before the first render.
    // No effect needed — avoids the cascading-render lint warning.
    const token = localStorage.getItem(TOKEN_KEY);
    const storedUser = localStorage.getItem(USER_KEY);

    if (!token || !storedUser) return null;

    const payload = decodeJwt(token);
    if (!payload) {
      // Token expired or malformed — clean up immediately
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      return null;
    }

    return JSON.parse(storedUser) as AuthUser;
  });

  // isInitialized is always true after the lazy initializer runs,
  // but we keep the flag so consumers can rely on it.
  const isInitialized = true;

  const persistSession = useCallback((response: AuthResponse) => {
    const token = response.token ?? '';
    const payload = decodeJwt(token);

    const authUser: AuthUser = {
      id: (payload?.userId as string) ?? '',
      username: (payload?.sub as string) ?? '',
      token,
    };

    localStorage.setItem(TOKEN_KEY, authUser.token);
    localStorage.setItem(USER_KEY, JSON.stringify(authUser));
    setUser(authUser);
  }, []);

  const clearSession = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }, []);

  const { mutateAsync: loginAsync, isPending: isLoginPending } = useMutation(loginMutation());

  const { mutateAsync: registerAsync, isPending: isRegisterPending } =
    useMutation(registerMutation());

  const { mutateAsync: logoutAsync, isPending: isLogoutPending } = useMutation(
    logoutMutation({
      headers: {
        Authorization: user?.token ? `Bearer ${user.token}` : undefined,
      },
      ...defaultConfig,
    })
  );

  const login = useCallback(
    async (credentials: LoginRequest) => {
      const response = await loginAsync({ body: credentials, ...defaultConfig });
      persistSession(response);
    },
    [loginAsync, persistSession]
  );

  const register = useCallback(
    async (credentials: RegisterRequest) => {
      const response = await registerAsync({ body: credentials, ...defaultConfig });
      persistSession(response);
    },
    [registerAsync, persistSession]
  );

  const logout = useCallback(async () => {
    await logoutAsync({});
    clearSession();
  }, [logoutAsync, clearSession]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isLoading: isLoginPending || isRegisterPending || isLogoutPending,
      isInitialized,
      login,
      register,
      logout,
    }),
    [
      user,
      isLoginPending,
      isRegisterPending,
      isLogoutPending,
      isInitialized,
      login,
      register,
      logout,
    ]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
