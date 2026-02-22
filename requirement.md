## 3. Technical Architecture (Microservices & DevOps)

Using a **Java/Spring Boot** stack with **Kafka** ensures the system is resilient and can handle many concurrent guesses.

| **Service** | **Technology** | **Responsibility** |
| --- | --- | --- |
| **Lobby Service** |  spring + Redis | Manages Rooms, Player joins/leaves, and Match configuration |
| **Game Service** |  spring/quarqus + PostgreSQL | Picks the 5 random coordinates/maps; manages round timers |
| **Scoring Service** | spring  | Consumes "Guesses" from Kafka, calculates scores, and persists them\ |
| **Leaderboard Service** | Spring Boot + Redis | Maintains real-time rankings using Redis Sorted Sets (`ZSET`) |
| **Gateway Service** |  | Act as a router to redirect api calls |
| **Auth Service** | Spring boot + PostgreSQL | manage user, tokens, and accounts |
| **Frontend Service** | React or Vue | run `<iframe>` + all custom logic |

---

### **3.1. Integration with Noclip.website**

### The Iframe Bridge

The main Vue app will host the Noclip site in an `<iframe>`. We communicate via the **Window PostMessage API**.

```jsx
iframeRef.contentWindow.postMessage({
    type: 'SET_MAP',
    mapId: 'mario-kart-8/BabyPark',
    coords: { x: 100, y: 50, z: -200 }
}, '*');
```

### **3.2. Rest Endpoint**

Lobby service

| **Method** | **Endpoint** | **Description** |
| --- | --- | --- |
| **POST** | `/api/v1/rooms` | Create a new room. Returns a `roomCode`. |
| **GET** | `/api/v1/rooms/{code}` | Get room details (players, settings, status). |
| **PATCH** | `/api/v1/rooms/{code}/settings` | Update round count (default 5), time limit, or game pack. |
| **POST** | `/api/v1/rooms/{code}/join` | Add a player to the lobby. |
| **DELETE** | `/api/v1/rooms/{code}/leave` | Remove yourself from the lobby. |

Game service

| **Method** | **Endpoint** | **Description** |
| --- | --- | --- |
| **POST** | `/api/v1/rooms/{code}/start` | Transitions room state to `IN_PROGRESS`. Generates the 5 locations. |
| **GET** | `/api/v1/rooms/{code}/round` | Get current round info (game ID and map, but **not** the coordinates). |
| **POST** | `/api/v1/rooms/{code}/guess` | **The Producer:** Accepts (X, Y, Z) guess. This sends a message to **Kafka**. |
| **GET** | `/api/v1/rooms/{code}/results` | Returns final scores for all 5 rounds after the game ends. |

Leaderboard service

| **Method** | **Endpoint** | **Description** |
| --- | --- | --- |
| **GET** | `/api/v1/leaderboard/global` | Get top players across all games. |
| **GET** | `/api/v1/leaderboard/room/{code}` | Get the current ranking within a specific room. |

### 3.3. kafka topic

| **Topic Name** | **Producer** | **Primary Consumer** | **Importance** |
| --- | --- | --- | --- |
| `game.room.events` | Lobby Service | Game Service | Low (Setup) |
| `player.guess.submitted` | Game Service | Scoring Service | **High (Gameplay)** |
| `score.calculated` | Scoring Service | Leaderboard Service | **High (Results)** |
| `game.round.update` | Game Service | WebSocket Service | Medium (Sync) |