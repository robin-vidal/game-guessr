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

## Cloud Architecture
The infrastructure has been specifically designed to ensure high availability during the final academic defense while remaining strictly under a **300€/month budget**. 

To achieve this, managed database services were discarded in favor of self-hosted solutions within a single Google Kubernetes Engine (GKE) cluster, and GitHub Container Registry (`ghcr.io`) is used instead of GCP Artifact Registry.

```mermaid
graph TD
    classDef gcp fill:#e3f2fd,stroke:#1565c0,stroke-width:2px;
    classDef gh fill:#f5f5f5,stroke:#24292f,stroke-width:2px;
    classDef ext fill:#fff3e0,stroke:#e65100,stroke-width:2px;

    User([Player / Professor]) -->|HTTPS| DNS
    
    subgraph "External Providers"
        DNS["Domaine (Ex: .site)"]:::ext
        GH["GitHub Container Registry<br/>`ghcr.io`"]:::gh
    end

    subgraph "Google Cloud Platform (GCP)"
        LB["Cloud Load Balancing<br/>IP Publique + SSL"]:::gcp
        GCS[(Cloud Storage<br/>Terraform State)]:::gcp
        
        subgraph "GKE Zonal Cluster"
            Ingress[Nginx Ingress]
            
            subgraph "Stateless Microservices (Pods)"
                Gateway[Gateway API]
                Front[Frontend React]
                SB[Spring Boot Services]
            end
            
            subgraph "Stateful Services (Pods)"
                PG[(PostgreSQL)]
                Redis[(Redis)]
                Kafka[Kafka / Zookeeper]
                Auth[Authentik IdP]
            end
        end
    end

    DNS --> LB
    LB --> Ingress
    Ingress --> Gateway
    Ingress --> Front
    Ingress --> Auth
    Gateway --> SB
    
    SB --> PG
    SB --> Redis
    SB --> Kafka
    Auth --> PG
    Auth --> Redis
    
    %% Registry pull
    Gateway -.->|Image Pulls| GH
    Front -.->|Image Pulls| GH
    SB -.->|Image Pulls| GH
    Auth -.->|Image Pulls| GH
```

### Justification of Technological Choices
1. **GitHub Container Registry (`ghcr.io`)**: Used to store our Docker images for free via GitHub Actions, saving the cost of GCP Artifact Registry.
2. **GKE Zonal Cluster**: We deploy a single zonal cluster to benefit from the free-tier management fee and use standard (or Autopilot with strict limits) nodes. 
3. **Stateful Pods over Managed Services**: Deploying PostgreSQL, Redis, and Kafka as Pods with Persistent Volumes directly in GKE avoids the massive costs associated with Cloud SQL and Memorystore, which are over-engineered for a project with limited traffic.
4. **Cloud Storage**: A simple bucket costing pennies per month to securely share the Terraform deploy state (`.tfstate`).

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
