# game-guessr

GameGuessr is a multiplayer **"GeoGuessr for Video Games"**. Players explore iconic, accurately recreated 3D levels from classic video games (powered by `noclip.website`) and must use their game knowledge and spatial awareness to pinpoint their exact location on the map. 

The project features a **scalable microservices architecture**, real-time multiplayer synchronization, event-driven scoring, and a modern DevOps pipeline. It is designed to be highly competitive, lightweight, and engaging.

---

## Group Members
- Marie-Lou Allain (`marie-lou.allain`)
- Naïm Chefirat (`naim.chefirat`)
- Michaël Rousseau (`michael.rousseau`)
- Robin Vidal (`robin.vidal`)

---

## Tech Stack
- **Frontend**: React, WebGL (via `noclip.website` Iframe Bridge)
- **Backend**: Java / Spring Boot / Quarkus (Microservices)
- **Data & Real-Time**: PostgreSQL, Redis, Apache Kafka, WebSockets
- **Infrastructure**: Kubernetes (GKE Autopilot), Terraform, Helm, Docker
- **Security**: OAuth2 via Authentik

---

## Architecture Overview

The system is broken down into loosely coupled microservices communicating via REST APIs (synchronous) and Kafka Topics (asynchronous).

```mermaid
graph TD
    subgraph Frontend_Layer [Frontend Layer - React]
        UI[Game UI / Map Selector]
        Noclip[Noclip.website Iframe]
        WS_Client[WebSocket Client]
        UI -- postMessage --> Noclip
    end

    subgraph API_Gateway [Gateway Service]
        Auth[Auth / Entry Point]
    end

    subgraph Microservices [Microservices Layer - Spring Boot / Quarkus]
        Lobby[Lobby Service]
        Game[Game Service]
        Scoring[Scoring Service]
        Leaderboard[Leaderboard Service]
        WS_Service[WebSocket/Sync Service]
    end

    subgraph Data_Persistence [Storage Layer]
        Redis_Lobby[(Redis: Lobby/Rooms)]
        PG_Game[(PostgreSQL: Maps/Users)]
        Redis_LB[(Redis: ZSET Leaderboard)]
    end

    subgraph Event_Bus [Kafka Backbone]
        T_Room[game.room.events]
        T_Guess[player.guess.submitted]
        T_Score[score.calculated]
        T_Round[game.round.update]
    end

    %% Flow: Setup & Lobby
    UI --> Auth
    Auth --> Lobby
    Lobby <--> Redis_Lobby
    Lobby -- "room.created" --> T_Room

    %% Flow: Gameplay
    T_Room --> Game
    Game <--> PG_Game
    Game -- "game.started" --> T_Round
    T_Round --> WS_Service
    WS_Service -- "Push: Map Metadata" --> WS_Client
    
    %% Flow: Guessing & Scoring
    UI -- "POST /guess" --> Game
    Game -- "Produce: Guess Data" --> T_Guess
    T_Guess --> Scoring
    Scoring -- "Produce: Score" --> T_Score
    
    %% Flow: Results & Rankings
    T_Score --> Leaderboard
    T_Score --> WS_Service
    Leaderboard <--> Redis_LB
    WS_Service -- "Push: Player Score" --> WS_Client
```

---

## Services & Microservices
| **Service** | **Stack** | **Responsibility** |
| --- | --- | --- |
| **Lobby Service** | Spring Boot + Redis | Manages active game rooms, presence, and host settings. |
| **Game Service** | Quarkus + PostgreSQL | Manages the match lifecycle, current round data, and validates coordinates. |
| **Scoring Service** | Spring Boot + Kafka | Stateless worker calculating Euclidean distances and assigning points. |
| **WebSocket / Sync** | Spring Boot + Redis | Bridges backend state changes (Kafka/Redis) directly to React clients. |
| **Leaderboard Service** | Spring Boot + Redis | Maintains real-time room rankings using Redis Sorted Sets (`ZSET`). |
| **Gateway Service** | Spring Cloud Gateway | Acts as the main router and entry point for all API calls. |
| **Auth Service** | Spring Boot + PostgreSQL| Manages users, JWT tokens, and OAuth integration (via Authentik). |
| **Frontend Service** | React | Runs `<iframe src="noclip.website">` alongside custom React UI logic. |

---

## Documentation
For detailed information on the design decisions, user stories, and architecture:
- [Conception & Architecture Document (MVP Scope)](docs/conception.md)
- [Sprint Planning & Roadmap](docs/sprint-planning.md)
- [Architecture Decision Records (ADRs)](docs/adr/)
- [Feature Epics & User Stories](docs/epics/)
