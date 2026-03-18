# ADR 0001: Service Architecture and Inter-Service Communication

**Date:** 2026-02-22

## Context
Our multiplayer game, GameGuessr, requires real-time interactions, match lifecycle management, independent scaling of components, and potentially asynchronous score calculation. We need to decide whether to build a monolith or use microservices, and how these services will communicate (e.g., synchronous REST vs asynchronous Events).

## Decision
We will use a **Microservices Architecture (Spring Boot)** with **Kafka** for asynchronous event-driven communication (scoring, match events) and **REST/WebSocket** for synchronous client-facing operations. 

We will structure the backend into:
- `/services/auth`
- `/services/match`
- `/services/game`
- `/services/score`

## Rationale
- **Decoupling**: The Score Service can independently consume events (e.g., `GAME_GUESSED`, `SPOT_GUESSED`) from Kafka without blocking the game loop in the Game or Match services.
- **Scalability**: A Kafka-based event system allows us to handle spikes in real-time events efficiently and scale the Score Service independently from the Match Service.
- **Team Distribution**: Separating services allows different team members (e.g., Michael for Kafka/sync routes) to work on domains concurrently with fewer merge conflicts.

## Consequences
- **Positive:** High performance for the real-time core loop, strong decoupling, resilient to partial failures (Score service down won't stop the match).
- **Negative / Risk:** Increased operational complexity (need to manage Kafka, Zookeeper/KRaft, and multiple Spring Boot deployments) and steeper learning curve for the team.
- **Implication:** We must ensure strict payload validation for Kafka events and synchronize them correctly with REST routes (a priority task). We must also invest in a solid Kubernetes/Helm setup to deploy these microservices smoothly.
