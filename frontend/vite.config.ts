import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import path from 'path';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      '/api/auth': {
        target: process.env.VITE_AUTH_SERVICE_URL ?? "http://localhost:8081",
        changeOrigin: true,
      },
      '/api/v1/game': {
        target: process.env.VITE_GAME_SERVICE_URL ?? "http://localhost:8082",
        changeOrigin: true,
      },
      '/api/v1/rooms': {
        target: process.env.VITE_LOBBY_SERVICE_URL ?? "http://localhost:8083",
        changeOrigin: true,
      },
      '/api/v1/scoring': {
        target: process.env.VITE_SCORING_SERVICE_URL ?? "http://localhost:8084",
        changeOrigin: true,
        secure: false,
      },
      '/api/v1/leaderboard': {
        target: process.env.VITE_LEADERBOARD_SERVICE_URL ?? "http://localhost:8085",
        changeOrigin: true,
        secure: false,
      },
    },
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
});
