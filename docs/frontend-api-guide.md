# Frontend API Guide — Game Guessr

Complete reference for frontend developers. All services expose a Swagger UI at `http://localhost:{port}/swagger-ui.html` for live exploration.

---

## Service Map

| Service          | Port | Base URL                        |
|------------------|------|---------------------------------|
| lobby-service    | 8083 | `http://localhost:8083/api/v1`  |
| game-service     | 8082 | `http://localhost:8082/api/v1`  |
| scoring-service  | 8084 | `http://localhost:8084/api/v1`  |
| leaderboard-service | 8085 | `http://localhost:8085/api/v1` |
| noclip frontend  | 8000 | `http://localhost:8000`         |

> No API gateway yet. Call each service directly.

---

## Complete Game Flow

```
1. Player creates or joins a room  (lobby-service)
2. Host starts the match           (game-service)
3. Players poll for current round  (game-service)
4. Players submit guesses per phase (game-service)
5. Scores update automatically     (scoring-service via Kafka)
6. Leaderboard updates live        (leaderboard-service via Kafka)
7. After all rounds: fetch results (game-service)
```

---

## 1. Lobby — Room Management

### Create a Room

```
POST http://localhost:8083/api/v1/rooms
```

**Request body:**
```json
{
  "hostId": "user-uuid-123",
  "isPrivate": false
}
```

**Response `201`:**
```json
{
  "roomCode": "ABC123",
  "hostId": "user-uuid-123",
  "isPrivate": false,
  "status": "OPEN",
  "maxPlayers": 8,
  "settings": {
    "roundCount": 5,
    "timeLimitSeconds": 60,
    "gamePack": "mario-kart"
  },
  "players": [],
  "createdAt": "2024-01-01T00:00:00Z"
}
```

---

### List Open Rooms

```
GET http://localhost:8083/api/v1/rooms
```

**Response `200`:** Array of room objects (same shape as above).

---

### Get Room Details

```
GET http://localhost:8083/api/v1/rooms/{code}
```

**Response `200`:** Room object including player list and current status.
**Errors:** `404` room not found.

---

### Join a Room

```
POST http://localhost:8083/api/v1/rooms/{code}/join
```

**Request body:**
```json
{
  "playerId": "user-uuid-456",
  "displayName": "Mario"
}
```

**Response `200`:** Updated room object.
**Errors:** `404` room not found, `409` room full or already joined.

---

### Leave a Room

```
DELETE http://localhost:8083/api/v1/rooms/{code}/leave
```

**Request body:**
```json
{
  "playerId": "user-uuid-456"
}
```

**Response `204` (no body).**
**Note:** If the **host** leaves, the room is closed for everyone.

---

### Update Room Settings *(host only)*

```
PATCH http://localhost:8083/api/v1/rooms/{code}/settings
```

**Request body:**
```json
{
  "playerId": "user-uuid-123",
  "roundCount": 3,
  "timeLimitSeconds": 90,
  "gamePack": "mario-kart"
}
```

All fields except `playerId` are optional — send only what you want to change.

**Response `200`:** Updated room object.
**Errors:** `403` not the host, `404` room not found.

---

## 2. Game — Match Lifecycle

### Start a Match *(host only)*

```
POST http://localhost:8082/api/v1/rooms/{code}/start
```

**Request body:**
```json
{
  "hostId": "user-uuid-123"
}
```

**Response `201` (empty body).**
**Errors:** `409` match already started, `404` room not found.

> After calling this, the match has 5 rounds. Round 1 begins immediately.

---

### Get Current Round

```
GET http://localhost:8082/api/v1/rooms/{code}/round
```

**Response `200`:**
```json
{
  "roundNumber": 1,
  "gameId": "mario-kart-8",
  "levelId": "TBD-1",
  "currentPhase": "GAME",
  "finished": false,
  "startedAt": 1700000000000
}
```

**Fields:**
- `currentPhase`: `GAME` → `LEVEL` → `SPOT` (phases progress as players submit)
- `startedAt`: epoch milliseconds — use for client-side countdown timer
- `finished`: `true` when all phases in the round are done

**Errors:** `404` room or match not found.

> **Poll this endpoint** to track round/phase transitions. There is no WebSocket yet.

---

### Submit a Guess

```
POST http://localhost:8082/api/v1/rooms/{code}/guess
```

**Request body (GAME phase):**
```json
{
  "playerId": "user-uuid-456",
  "phase": "GAME",
  "textAnswer": "Mario Kart 8"
}
```

**Request body (LEVEL phase):**
```json
{
  "playerId": "user-uuid-456",
  "phase": "LEVEL",
  "textAnswer": "Rainbow Road"
}
```

**Request body (SPOT phase):**
```json
{
  "playerId": "user-uuid-456",
  "phase": "SPOT",
  "guessX": 1234.5,
  "guessY": 0.0,
  "guessZ": -567.8
}
```

**Response `202` (empty body).** Scoring happens asynchronously via Kafka.
**Errors:** `400` wrong phase or missing fields, `404` room or match not found.

**Phase rules:**
- You must submit `GAME` before you can submit `LEVEL`
- You must submit both `GAME` and `LEVEL` before `SPOT`
- SPOT phase is post-MVP — it always scores 0 for now

---

### Get Match Results *(end of game)*

```
GET http://localhost:8082/api/v1/rooms/{code}/results
```

**Response `200`:**
```json
{
  "roomCode": "ABC123",
  "matchStatus": "FINISHED",
  "rounds": [
    {
      "roundNumber": 1,
      "gameId": "mario-kart-8",
      "levelId": "TBD-1",
      "trueSpawnX": 0.0,
      "trueSpawnY": 0.0,
      "trueSpawnZ": 0.0,
      "finished": true
    }
  ]
}
```

> True spawn coordinates are **only** revealed here (not during active play).

---

## 3. Scoring — Score Queries

Scores are written automatically when a guess is submitted. Just read them.

### Get Scores for a Round

```
GET http://localhost:8084/api/v1/scoring/{roomCode}/rounds/{roundNumber}
```

**Response `200`:**
```json
{
  "roomCode": "ABC123",
  "scores": [
    {
      "id": "uuid",
      "roundNumber": 1,
      "playerId": "user-uuid-456",
      "phase": "GAME",
      "points": 1000,
      "correct": true,
      "timeBonusMs": 0,
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

---

### Get All Scores for a Match

```
GET http://localhost:8084/api/v1/scoring/{roomCode}
```

Same response shape — includes all rounds and all players.

---

## 4. Leaderboard

### Room Leaderboard *(current match ranking)*

```
GET http://localhost:8085/api/v1/leaderboard/room/{code}
```

**Response `200`:**
```json
{
  "leaderboardType": "room",
  "identifier": "ABC123",
  "entries": [
    { "rank": 1, "playerId": "user-uuid-456", "score": 1500.0 },
    { "rank": 2, "playerId": "user-uuid-123", "score": 1000.0 }
  ]
}
```

---

### Global Leaderboard

```
GET http://localhost:8085/api/v1/leaderboard/global?top=100
```

Same response shape with `leaderboardType: "global"` and `identifier: "all"`.

---

## Scoring Rules

| Phase | Points | Logic |
|-------|--------|-------|
| GAME  | 0 or 1000 | Binary: any non-empty answer = 1000 pts (MVP) |
| LEVEL | 500–1000 | 500 base + up to 500 time bonus (faster = more) |
| SPOT  | 0 | Post-MVP stub — always 0 |

**Time bonus (LEVEL phase):** Scales from 500 pts (answer in ≤0s) to 0 pts (answer in ≥60s).

---

## Room Status Values

| Status | Meaning |
|--------|---------|
| `OPEN` | Waiting for players, joinable |
| `IN_PROGRESS` | Match running, no new players |
| `FINISHED` | Match done |
| `CLOSED` | Host left, room defunct |

---

## Recommended Polling Strategy

Since there is no WebSocket yet, poll these endpoints:

| What to track | Endpoint | Suggested interval |
|---------------|----------|--------------------|
| Room player list / status | `GET /api/v1/rooms/{code}` | Every 2s in lobby |
| Round phase changes | `GET /api/v1/rooms/{code}/round` | Every 1–2s during game |
| Live scores during round | `GET /api/v1/scoring/{roomCode}/rounds/{n}` | After submitting a guess |
| Leaderboard | `GET /api/v1/leaderboard/room/{code}` | After each round |

---

## Noclip Integration

The noclip viewer runs at `http://localhost:8000` (or `http://gg_noclip` inside Docker). It renders 3D game maps used for the SPOT phase. Embed it as an iframe or navigate to it when showing the map for coordinate guessing.

---

## Error Reference

| Code | Meaning |
|------|---------|
| `400` | Invalid request body or phase |
| `403` | Operation requires host privileges |
| `404` | Room, match, or round not found |
| `409` | Conflict (room full, match already started, already in room) |
