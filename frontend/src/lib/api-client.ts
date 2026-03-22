import axios from 'axios';

/**
 * Pre-configured Axios instance pointing at the Gateway Service.
 * The base URL is injected via Vite env variables so it works
 * across dev, staging, and prod without code changes.
 *
 * Auth: JWT is attached automatically via the request interceptor.
 * Errors: 401 responses trigger a redirect to /login.
 */
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10_000,
});

// ── Request interceptor: attach JWT ──────────────────────────────────────────
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('gg_access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ── Response interceptor: handle 401 globally ────────────────────────────────
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('gg_access_token');
      // Hard redirect to login — the auth context will pick this up
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
