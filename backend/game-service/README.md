# game-service

Core gameplay microservice for **Game Guessr** — manages match lifecycle, round timers, and player guesses.

---

## Tech Stack

| Layer     | Technology                                 |
| --------- | ------------------------------------------ |
| Language  | Java 21                                    |
| Framework | Spring Boot 3.4                            |
| Database  | PostgreSQL 16 (prod/local) · H2 (dev/test) |
| Messaging | Apache Kafka                               |
| API Docs  | Springdoc OpenAPI 3 (Swagger UI)           |
| Build     | Maven 3.9                                  |

---

## Prerequisites

- **Java 21** (Corretto or Temurin recommended)
- **Maven 3.9+**
- **Docker + Docker Compose**

> ⚠️ If your `mvn --version` shows Java 25 (Homebrew default), add this to your `~/.zshrc`:
>
> ```bash
> export JAVA_HOME=$(/usr/libexec/java_home -v 21)
> export PATH="$JAVA_HOME/bin:$PATH"
> ```
>
> Then `source ~/.zshrc`.

---

## Running Locally (recommended)

This runs **Postgres + Kafka in Docker** with the **service on your host** (fast iteration, no rebuild needed).

### 1. Start infrastructure

```bash
# From the project root (game-guessr/)
docker compose up -d
```

This starts:
| Service | Port | URL |
|---|---|---|
| PostgreSQL | 5432 | — |
| Kafka | 9092 | — |
| **Kafka UI** | 8090 | http://localhost:8090 |

Wait ~15 seconds for Kafka to be ready.

### 2. Run the service

```bash
cd backend/game-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 3. Open Swagger UI

→ **http://localhost:8082/swagger-ui.html**

### 4. Open Kafka UI

→ **http://localhost:8090**

### Seed data

The `local` profile automatically loads `src/main/resources/db/seed.sql` on startup. It pre-creates two rooms ready to start:

| Room Code | Status    |
| --------- | --------- |
| `DEMO01`  | `WAITING` |
| `DEMO02`  | `WAITING` |

Try immediately in Swagger:

```
POST /api/v1/games/DEMO01/start   { "hostId": "my-user-id" }
GET  /api/v1/games/DEMO01/round
POST /api/v1/games/DEMO01/guess   { "playerId": "my-user-id", "phase": "GAME", "textAnswer": "Mario Kart 8" }
GET  /api/v1/games/DEMO01/results
```

---

## Running Tests

```bash
mvn test
# → Tests run: 14, Failures: 0, Errors: 0
```

---

## Running Everything in Docker

Builds and runs the service container alongside infra:

```bash
# From project root
docker compose --profile app up --build
```

Service will be available at **http://localhost:8082/swagger-ui.html**

---

## Spring Profiles

| Profile  | Database             | Kafka          | Use case                          |
| -------- | -------------------- | -------------- | --------------------------------- |
| `dev`    | H2 (in-memory)       | localhost:9092 | Quick local run, no Docker needed |
| `local`  | Postgres (Docker)    | localhost:9092 | **Normal development**            |
| `docker` | Postgres (container) | kafka:29092    | Full Docker Compose stack         |

---

## API Endpoints

| Method | Path                           | Description                             |
| ------ | ------------------------------ | --------------------------------------- |
| `POST` | `/api/v1/games/{code}/start`   | Start a match (generates 5 rounds)      |
| `GET`  | `/api/v1/games/{code}/round`   | Get current round info (no coordinates) |
| `POST` | `/api/v1/games/{code}/guess`   | Submit a guess → publishes Kafka event  |
| `GET`  | `/api/v1/games/{code}/results` | Final results with true coordinates     |

Full documentation: **http://localhost:8082/swagger-ui.html**

---

## Kafka Topics

| Topic                    | Direction  | Description                                            |
| ------------------------ | ---------- | ------------------------------------------------------ |
| `game.room.events`       | ← Consumed | Lobby creates room → service pre-creates WAITING match |
| `player.guess.submitted` | → Produced | Sent to Scoring Service on each guess                  |
| `game.round.update`      | → Produced | Sent to WebSocket Gateway on round change              |

Monitor topics at **http://localhost:8090** (Kafka UI).

---

## Project Structure

```
src/main/java/com/gameguessr/game/
├── domain/              ← Pure business logic (no framework imports)
│   ├── model/           Match, Round, GamePackEntry, Guess, enums
│   ├── port/
│   │   ├── inbound/     GameUseCase (interface)
│   │   └── outbound/    MatchRepository, GameEventPublisher (interfaces)
│   └── exception/       Domain exceptions
├── application/         ← Use case orchestration + REST adapters
│   ├── service/         GameApplicationService
│   └── rest/            GameController, GlobalExceptionHandler, DTOs
└── infrastructure/      ← Framework adapters
    ├── persistence/      JPA entities, repositories, adapter
    ├── messaging/        Kafka producer, consumer, event DTOs
    └── config/           KafkaConfig, OpenApiConfig
```
