# Sprint Planning (MVP)

Assumptions:
- 2 Backend Developers
- 1 Frontend Developer
- 1 DevOps/Fullstack Developer
- 2-week sprints
- MVP target: 4 sprints

---

## Sprint 0 — Foundations & Auth
**Goal:** Secure access & establish base architecture

### Deliverables:
- Monorepo setup
- Provision Kubernetes cluster (GKE Autopilot) via **Terraform**
- Create `gameguessr-dev` namespace
- Helm base chart
- CI pipeline
- Authentik deployed
- OAuth integration (backend)
- User persistence
- Protected API routes
- Initialize a React frontend repository

**Outcome:**
Users can log in and see a protected homepage.

---

## Sprint 1 — Rooms & Invitations
**Goal:** Implement multiplayer entry point

### Backend:
- Room service
- Public matchmaking
- Private room creation
- Invite token generation
- WebSocket lobby

### Frontend:
- Room UI
- Invite link display
- Lobby player list
- Host start button

**Outcome:**
Users can log in, create/join public or private rooms, and see players in real-time.

---

## Sprint 2 — Full 2-Phase Gameplay (Multiplayer)
**Goal:** Core loop functional (except phase 3 spatial precision)

### Backend:
- Match lifecycle management
- Kafka integration
- Score service

### Frontend:
- Noclip integration
- Game guess
- Level guess
- Round transitions

**Outcome:**
Multiplayer match is fully playable through the first two phases.

---

## Sprint 3 — Stabilization & Deployment to Production
**Goal:** MVP Ready for Production

### Backend:
- Error handling
- Basic persistence
- Logging
- Runtime payload validation (REST & Kafka)
- Polish Swagger REST API specification
- Unit tests

### Frontend:
- Leaderboard screen
- Summary visuals
- UI and UX polish
- Error handling
- Runtime validation of request and response payloads (if appropriate)

### DevOps:
- Create `gameguessr-prod` namespace
- Production deployment via CI/CD
- Horizontal scaling tests
- Observability baseline
- End-to-End (e2e) tests

**Outcome:**
Stable MVP deployed online, ready for the first evaluation.
