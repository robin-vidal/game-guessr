# GameGuessr — Frontend

React + TypeScript + Vite frontend for the GameGuessr multiplayer game.

## Stack

| Concern       | Library                                 |
| ------------- | --------------------------------------- |
| Framework     | React 18 + TypeScript                   |
| Bundler       | Vite 5                                  |
| Routing       | React Router v6                         |
| UI Components | shadcn/ui + Tailwind CSS                |
| Server state  | TanStack Query v5 + Axios               |
| Client state  | React Context + useReducer              |
| Real-time     | Native WebSocket (custom hook)          |
| 3D viewer     | noclip.website via iframe + postMessage |

## Getting started

```bash
# 1. Install dependencies
npm install

# 3. Configure environment
cp .env.example .env.local
# Edit .env.local with your local service URLs

# 4. Start dev server (http://localhost:3000)
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
├── components/
│   ├── ui/                       # shadcn/ui components (auto-generated), global components
│   └── pages/
│       ├── components/           # Components specifics to the page
│       └── page.tsx              # Page component
├── features/
│   ├── auth/
│   │   └── AuthContext.tsx       # OAuth flow, JWT, user state
│   ├── lobby/
│   │   └── useLobby.ts           # TanStack Query hooks (rooms API)
│   ├── game/
│   │   ├── GameContext.tsx       # Round state, WS event reducer
│   │   └── useGame.ts            # Guess submission mutations
│   └── leaderboard/              # (to be implemented — Epic C5/F3)
├── hooks/
│   ├── useWebSocket.ts           # Subscribe to WS events
│   └── useNoclipBridge.ts        # Attach/use the noclip iframe bridge
├── lib/
│   ├── api-client.ts             # Axios instance (Gateway)
│   ├── noclip-bridge.ts          # postMessage abstraction (ADR 0004)
│   ├── ws-client.ts              # WebSocket singleton
│   └── utils.ts                  # cn() Tailwind helper
├── router/
│   ├── AppRouter.tsx             # Route definitions
│   └── ProtectedRoute.tsx        # Auth guard
├── types/
│   └── index.ts                  # All shared TypeScript types + Kafka event contracts
├── App.tsx                       # Provider composition
└── main.tsx                      # React root
```

## Environment variables

| Variable                  | Description                        |
| ------------------------- | ---------------------------------- |
| `VITE_API_BASE_URL`       | Gateway Service base URL           |
| `VITE_WS_BASE_URL`        | WebSocket Sync Service URL         |
| `VITE_OAUTH_REDIRECT_URL` | Authentik OAuth2 authorization URL |
| `NOCLIP_FRONTEND_URL`     |  Noclip Front-end URL              |

## Architecture notes

### Noclip iframe bridge (ADR 0004)

All communication with the `noclip.website` iframe goes through `src/lib/noclip-bridge.ts`.
Never use `postMessage` directly — always go through `noclipBridge` or the `useNoclipBridge` / `useNoclipEvent` hooks.

### WebSocket events

The `wsClient` singleton in `src/lib/ws-client.ts` connects to the Sync Service when a room is joined.
Typed event payloads are defined in `src/types/index.ts` and match the Kafka event contracts from Epic D3.
Subscribe to events with the `useWebSocket(eventType, handler)` hook — it auto-unsubscribes on unmount.

### State management

- **Server state** (rooms, users, scores): TanStack Query — see `features/lobby/useLobby.ts` and `features/game/useGame.ts`
- **Real-time game state** (round, phase, timer): `GameContext` reducer, fed by WebSocket events
- **Auth state**: `AuthContext` reducer

### What's left to implement (stubs)

| File                                  | Epic   | What to build                     |
| ------------------------------------- | ------ | --------------------------------- |
| `GamePage.tsx` — `GameGuessPanel`     | C2     | Autocomplete input for game title |
| `GamePage.tsx` — `LevelGuessPanel`    | C3     | Level name input                  |
| `GamePage.tsx` — `SpotGuessPanel`     | C4     | 2D map + pin drop                 |
| `GamePage.tsx` — `RoundResultsBanner` | C5     | True location reveal + score      |
| `features/leaderboard/`               | C5, F3 | Friends leaderboard               |
| `RoomPage.tsx` — settings UI          | B5     | Host room settings panel          |
