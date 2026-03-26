import { AuthAction, AuthState } from '@/types';
import { ReactNode, useCallback, useEffect, useReducer } from 'react';
import { AuthContext } from './AuthContext';

function authReducer(state: AuthState, action: AuthAction): AuthState {
  switch (action.type) {
    case 'AUTH_LOADING':
      return { ...state, isLoading: true };
    case 'AUTH_SUCCESS':
      return { user: action.payload, isLoading: false, isAuthenticated: true };
    case 'AUTH_FAILURE':
      return { user: null, isLoading: false, isAuthenticated: true };
    case 'LOGOUT':
      return { user: null, isLoading: false, isAuthenticated: false };
    default:
      return state;
  }
}

const initialState: AuthState = {
  user: null,
  isLoading: true,
  isAuthenticated: false,
};

const OAUTH_REDIRECT_URL = import.meta.env.VITE_OAUTH_REDIRECT_URL as string;
const TOKEN_KEY = 'gg_access_token';

export function AuthProvider({ children }: Readonly<{ children: ReactNode }>) {
  const [state, dispatch] = useReducer(authReducer, initialState);

  // On mount: handle OAuth callback token OR validate existing token
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const callbackToken = params.get('token');

    if (callbackToken) {
      // OAuth callback — persist token and clean URL
      localStorage.setItem(TOKEN_KEY, callbackToken);
      window.history.replaceState({}, '', window.location.pathname);
      fetchCurrentUser();
    } else if (localStorage.getItem(TOKEN_KEY)) {
      fetchCurrentUser();
    } else {
      dispatch({ type: 'AUTH_FAILURE' });
    }
  }, []);

  const fetchCurrentUser = useCallback(async () => {
    dispatch({ type: 'AUTH_LOADING' });
    try {
       // To implement
      dispatch({ type: 'AUTH_SUCCESS', payload: {
        username: "Tmp username",
        id: "1",
        displayName: "Tmp  displayName"
      } });
    } catch {
      localStorage.removeItem(TOKEN_KEY);
      dispatch({ type: 'AUTH_FAILURE' });
    }
  }, []);

  /** Redirect to the Authentik / Epita OAuth authorization endpoint. */
  const login = useCallback(() => {
    console.log(OAUTH_REDIRECT_URL);
    window.location.href = OAUTH_REDIRECT_URL;
  }, []);

  const logout = useCallback(async () => {
    try {
      // To implement
    } finally {
      localStorage.removeItem(TOKEN_KEY);
      dispatch({ type: 'LOGOUT' });
    }
  }, []);

  return (
    <AuthContext.Provider value={{ ...state, login, logout }}>{children}</AuthContext.Provider>
  );
}
