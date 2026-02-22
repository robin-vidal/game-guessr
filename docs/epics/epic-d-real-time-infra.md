# EPIC D — Real-Time Infrastructure

## D1 — WebSocket Gateway
- Lobby updates
- Timer synchronization
- Round transitions
- Score updates

---

## D2 — Redis Integration
- Room state caching
- Player presence
- Countdown synchronization

---

## D3 — Kafka Event System
**[TODO]:** voir avec team archi (aka Michael)

**Events:**
- `MATCH_CREATED`
- `MATCH_STARTED`
- `GAME_GUESSED`
- `LEVEL_GUESSED`
- `SPOT_GUESSED`
- `ROUND_FINISHED`
- `MATCH_FINISHED`

Score Service consumes events asynchronously.
