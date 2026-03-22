import axios from 'axios';
import createClient from 'openapi-fetch';
import type { paths as GamePaths } from '@/types/game-service';
import type { paths as LobbyPaths } from '@/types/lobby-service';
import type { paths as ScoringPaths } from '@/types/scoring-service';
import type { paths as LeaderboardPaths } from '@/types/leaderboard-service';

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

export const gameClient = createClient<GamePaths>({
  baseUrl: import.meta.env.VITE_GAME_SERVICE_URL ?? 'http://localhost:8082',
});

export const lobbyClient = createClient<LobbyPaths>({
  baseUrl: import.meta.env.VITE_LOBBY_SERVICE_URL ?? 'http://localhost:8083',
});

export const scoringClient = createClient<ScoringPaths>({
  baseUrl: import.meta.env.VITE_SCORING_SERVICE_URL ?? 'http://localhost:8084',
});

export const leaderboardClient = createClient<LeaderboardPaths>({
  baseUrl: import.meta.env.VITE_LEADERBOARD_SERVICE_URL ?? 'http://localhost:8085',
});
