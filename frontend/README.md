# GameGuessr — Frontend

React + TypeScript + Vite frontend for the GameGuessr multiplayer game.

## Stack

| Concern       | Library                                 |
| ------------- | --------------------------------------- |
| Framework     | React 19 + TypeScript                   |
| Bundler       | Vite 8                                  |
| Routing       | React Router v7                         |
| UI Components | shadcn/ui + Tailwind CSS v4             |
| Server state  | TanStack Query v5 + Hey API             |
| Client state  | React Context + useReducer              |
| Real-time     | Native WebSocket (custom hook)          |
| 3D background | Three.js                                |
| 3D viewer     | noclip.website via iframe + postMessage |
| Notifications | Sonner                                  |

## Getting started

```bash
# 1. Install dependencies
npm install

# 2. Configure environment
cp .env.example .env.local
# Edit .env.local with your local service URLs

# 3. Start dev server (http://localhost:3000)
npm run dev
```

## Adding shadcn/ui components

With `components.json` already configured, add any component with:

```bash
npx shadcn-ui@latest add <component-name>
# e.g.
npx shadcn-ui@latest add input
npx shadcn-ui@latest add dialog
npx shadcn-ui@latest add badge
```

## Project structure

```
src/
├── client/
│   ├── game-service/         # Auto-generated: SDK + TanStack hooks for game-service
│   ├── lobby-service/        # Auto-generated: SDK + TanStack hooks for lobby-service
│   ├── scoring-service/      # Auto-generated: SDK + TanStack hooks for scoring-service
│   └── leaderboard-service/  # Auto-generated: SDK + TanStack hooks for leaderboard-service
├── components/
│   ├── background/
│   │   ├── GalaxyBackground.tsx  # Three.js scene wrapper (starfield + gradient sky)
│   │   └── Planet.tsx            # Three.js animated planet with procedural texture
│   ├── pages/
│   │   ├── game/
│   │   │   ├── components/
│   │   │   │   ├── GameHUD.tsx           # Round counter, phase label, timer
│   │   │   │   └── PlaceholderPanel.tsx  # Stub panel for guess phases
│   │   │   └── page.tsx                  # Main gameplay view (iframe + HUD + panels)
│   │   ├── home/
│   │   │   └── page.tsx                  # Landing page with 3D background
│   │   ├── lobby/
│   │   │   └── page.tsx                  # Public matchmaking / room creation
│   │   ├── notFound/
│   │   │   └── page.tsx                  # 404 page
│   │   ├── results/
│   │   │   └── page.tsx                  # End-of-match leaderboard
│   │   └── room/
│   │       └── page.tsx                  # Pre-game lobby (player list, host controls)
│   └── ui/                               # Shared UI components
│       ├── Button.tsx
│       ├── Container.tsx
│       ├── InfoBar.tsx
│       ├── Input.tsx
│       ├── Paper.tsx
│       ├── Tag.tsx
│       ├── Title.tsx
│       └── sonner.tsx                    # Toast notifications
├── contexts/
│   └── auth/
│       ├── AuthContext.tsx               # React context object
│       └── AuthProvider.tsx             # OAuth flow, JWT, user state (reducer + provider)
├── hooks/
│   └── useAuth.ts                        # Consumes AuthContext
├── lib/
│   ├── clients.ts                        # Hey API client instances per service
│   ├── api-client.ts                     # Axios instance (Gateway) with JWT interceptor
│   ├── noclip-bridge.ts                  # postMessage abstraction (ADR 0004)
│   ├── ws-client.ts                      # WebSocket singleton with auto-reconnect
│   └── utils.ts                          # cn() Tailwind helper
├── router/
│   ├── AppRouter.tsx                     # Route definitions
│   └── ProtectedRoute.tsx                # Auth guard
├── types/
│   └── index.ts                          # Auth, WS events, GamePhase — not generated
├── App.tsx                               # Provider composition
├── main.tsx                              # React root
└── index.css                             # Tailwind v4 imports + CSS variables
```

## Environment variables

| Variable                       | Description                                                   |
| ------------------------------ | ------------------------------------------------------------- |
| `VITE_API_BASE_URL`            | Gateway Service base URL                                      |
| `VITE_WS_BASE_URL`             | WebSocket Sync Service URL                                    |
| `VITE_OAUTH_REDIRECT_URL`      | Authentik OAuth2 authorization URL                            |
| `VITE_GAME_SERVICE_URL`        | game-service base URL (default: http://localhost:8082)        |
| `VITE_LOBBY_SERVICE_URL`       | lobby-service base URL (default: http://localhost:8083)       |
| `VITE_SCORING_SERVICE_URL`     | scoring-service base URL (default: http://localhost:8084)     |
| `VITE_LEADERBOARD_SERVICE_URL` | leaderboard-service base URL (default: http://localhost:8085) |
| `NOCLIP_FRONTEND_URL`          | Noclip frontend URL (injected at build time via Docker ARG)   |

## Backend client generation

All API clients, TypeScript types, and TanStack Query hooks are auto-generated from the OpenAPI specs exposed by each Spring Boot service using [Hey API](https://heyapi.dev). **Never write these by hand.**

### How it works

Each service exposes its OpenAPI spec at `/api-docs`. The `openapi-ts` script reads `openapi-ts.config.ts` and generates a full typed client (types + SDK functions + TanStack Query hooks) for each service into `src/client/<service-name>/`.

The config reads service URLs from your `.env` file via `dotenv`, so no hardcoded ports.

### Regenerate clients

With all backend services running locally:

```bash
npm run openapi-ts
```

This reads `openapi-ts.config.ts` and generates all services clients in one pass.

> ⚠️ Treat the `src/client/` folders as build artifacts — never edit them directly. Changes will be overwritten on the next generation run.

### Using generated hooks in code

Each service client exports ready-to-use TanStack Query hooks:

```ts
import { useQuery, useMutation } from '@tanstack/react-query';
import { getRoomsOptions, createRoomMutation } from '@/client/lobby-service';

// Query
function RoomList() {
  const { data, isPending } = useQuery(getRoomsOptions());
  return <div>{data?.map(room => <p key={room.id}>{room.name}</p>)}</div>;
}

// Mutation
function CreateRoom() {
  const { mutate, isPending } = useMutation(createRoomMutation());

  return (
    <button onClick={() => mutate({ body: { name: 'My Room', maxPlayers: 4 } })} disabled={isPending}>
      Create Room
    </button>
  );
}
```

Each service has its own configured client in `src/lib/clients.ts`. Pass it explicitly if you need to override the default (e.g. for auth headers):

```ts
import { gameClient } from '@/lib/clients';
import { getGameOptions } from '@/client/game-service';

const { data } = useQuery(getGameOptions({ client: gameClient }));
```

TypeScript will infer the correct request body shape and response type directly from the Java DTOs. If a backend field changes and you regenerate, the compiler will highlight every broken call site.

### What is NOT generated

The following live in `src/types/index.ts` and are maintained manually because they have no corresponding backend OpenAPI spec:

| Type                                    | Reason                                                   |
| --------------------------------------- | -------------------------------------------------------- |
| `GamePhase`                             | Frontend-only enum for UI state machine                  |
| `AuthState`, `AuthContextValue`, `User` | Auth service has no OpenAPI spec yet                     |
| `RoundState`                            | Client-side derived state, not a direct backend response |
| `WsEvent`, `WsEventType`, `*Payload`    | WebSocket service has no OpenAPI spec yet                |

## Architecture notes

### Noclip iframe bridge (ADR 0004)

All communication with the noclip iframe goes through `src/lib/noclip-bridge.ts`.
Never use `postMessage` directly — always go through `noclipBridge` or the `useNoclipBridge` / `useNoclipEvent` hooks.

### WebSocket events

The `wsClient` singleton in `src/lib/ws-client.ts` connects to the Sync Service when a room is joined.
Typed event payloads are defined in `src/types/index.ts` and match the Kafka event contracts from Epic D3.

### Auth flow

1. User clicks login → redirected to `VITE_OAUTH_REDIRECT_URL` (Authentik/Epita)
2. OAuth callback returns a `?token=` query param
3. `AuthProvider` picks it up, stores the JWT in `localStorage`, cleans the URL, then fetches `/auth/me`
4. All routes except `*` (404) are protected via `ProtectedRoute`

### State management

- **Server state** (rooms, users, scores): TanStack Query + Hey API generated hooks
- **Real-time game state** (round, phase, timer): `GameContext` reducer, fed by WebSocket events
- **Auth state**: `AuthContext` / `AuthProvider` reducer

### 3D background

`GalaxyBackground` wraps a Three.js canvas (gradient sky shader + CSS-animated star divs) and a `Planet` component (orthographic camera + procedural noise-based texture). It accepts `children` so any page can layer UI on top of it.

## What's left to implement (stubs)

| File                                         | Epic   | What to build                                                            |
| -------------------------------------------- | ------ | ------------------------------------------------------------------------ |
| `pages/game/page.tsx` — `GameGuessPanel`     | C2     | Autocomplete input for game title                                        |
| `pages/game/page.tsx` — `LevelGuessPanel`    | C3     | Level name input                                                         |
| `pages/game/page.tsx` — `SpotGuessPanel`     | C4     | 2D map + pin drop                                                        |
| `pages/game/page.tsx` — `RoundResultsBanner` | C5     | True location reveal + score                                             |
| `contexts/game/`                             | D      | `GameContext` + `GameProvider` (imported in App.tsx but not yet created) |
| `hooks/useWebSocket.ts`                      | D      | Subscribe to WS events with auto-cleanup                                 |
| `hooks/useNoclipBridge.ts`                   | C1     | Attach/use the noclip iframe bridge                                      |
| `hooks/useLobby.ts`                          | B      | TanStack Query hooks wrapping lobby-service client                       |
| `hooks/useGuess.ts`                          | C      | Guess submission mutations wrapping game-service client                  |
| `features/leaderboard/`                      | C5, F3 | Friends leaderboard                                                      |
| `pages/room/page.tsx` — settings UI          | B5     | Host room settings panel                                                 |