# ADR 0003: Data Layer Selection (PostgreSQL & Redis)

**Date:** 2026-02-22

## Context
Our application requires persistent storage for user accounts (Auth Service) and game metadata (Game Service / Maps / Scores). It also requires extremely low-latency, real-time transient storage for matchmaking queues, lobby states, and live leaderboards. We must choose a technology stack that meets both requirements efficiently within our microservices architecture.

## Decision
We will use **PostgreSQL** as our primary relational database for persistent storage (Auth, Users, Match History, Scores, Game Metadata) and **Redis** for in-memory, real-time state management (Lobby States, Player Presence, Pub/Sub events for Websockets, and Sorted Sets for Leaderboards).

## Rationale
- **PostgreSQL**: It provides strong ACID guarantees necessary for user accounts and historical match data. The structured nature of relational databases perfectly fits our static entity models (Users, Maps, Match Results).
- **Redis (Key-Value/In-Memory)**: We need sub-millisecond read/write speeds for room presence and countdowns. Moreover, Redis's built-in `ZSET` (Sorted Sets) data structure is the industry standard for maintaining high-performance global and room-specific leaderboards without hammering a relational database with `ORDER BY` queries.
- **Ecosystem**: Both technologies are open-source, have excellent integration with Spring Data (JPA and Redis-Data), and are easily deployable via Helm in our Kubernetes cluster.

## Consequences
- **Positive:** High performance for both read-heavy persistent data and extremely fast write-heavy transient data. Offloading leaderboard logic to Redis saves significant DB compute resources.
- **Negative / Risk:** We now have two different datastore technologies to deploy, monitor, and back up. Redis data loss could drop current active lobbies (though this is acceptable for transient data).
- **Implication:** DevOps must provision and manage standard PostgreSQL and Redis statefulsets (or managed instances) and configure the Spring Boot services with the correct datasource properties depending on the environment (Dev/Prod).
