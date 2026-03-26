import { defineConfig } from '@hey-api/openapi-ts';
import 'dotenv/config'; 

export default defineConfig([
  {
    input: `${process.env.VITE_GAME_SERVICE_URL}/api-docs`,
    output: {
      path: 'src/client/game-service',
      format: 'prettier',
    },
    plugins: ['@hey-api/client-fetch', '@tanstack/react-query'],
  },
  {
    input: `${process.env.VITE_LOBBY_SERVICE_URL}/api-docs`,
    output: {
      path: 'src/client/lobby-service',
      format: 'prettier',
    },
    plugins: ['@hey-api/client-fetch', '@tanstack/react-query'],
  },
  {
    input: `${process.env.VITE_SCORING_SERVICE_URL}/api-docs`,
    output: {
      path: 'src/client/scoring-service',
      format: 'prettier',
    },
    plugins: ['@hey-api/client-fetch', '@tanstack/react-query'],
  },
  {
    input: `${process.env.VITE_LEADERBOARD_SERVICE_URL}/api-docs`,
    output: {
      path: 'src/client/leaderboard-service',
      format: 'prettier',
    },
    plugins: ['@hey-api/client-fetch', '@tanstack/react-query'],
  },
  {
    input: `${process.env.VITE_AUTH_SERVICE_URL}/api-docs`,
    output: {
      path: 'src/client/auth-service',
      format: 'prettier',
    },
    plugins: ['@hey-api/client-fetch', '@tanstack/react-query'],
  },
]);