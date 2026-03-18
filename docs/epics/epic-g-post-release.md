# EPIC G — Post-Release & Advanced Infrastructure

## G1 — Public Matchmaking Enhancements

### User Story G1.1 — Dedicated Matchmaking Queue
> **As a player**, I want to enter a matchmaking queue  
> So that I am automatically grouped with other available players.

**Acceptance Criteria:**
- Queue system assigns players to rooms automatically.
- Rooms are created when minimum player threshold is reached.
- Timeout logic handles low-traffic scenarios.

### User Story G1.2 — Skill-Based Matchmaking (ELO)
> **As a competitive player**, I want to be matched with players of similar skill  
> So that matches feel balanced.

**Acceptance Criteria:**
- ELO rating stored per user.
- Rating updated after each match.
- Matchmaking logic considers rating ranges and configurable rating tolerance window.

### User Story G1.3 — Match History
> **As a player**, I want to see my past matches  
> So that I can track my progression.

**Acceptance Criteria:**
- Paginated match history endpoint.
- Display match date, score, ranking, and basic statistics (win/loss, average score).

---

## G2 — Observability and Monitoring

### User Story G2.1 — Prometheus Metrics Integration
> **As a developer**, I want application metrics exposed  
> So that system health can be monitored.

**Acceptance Criteria:**
- Spring Boot metrics enabled with Prometheus-compatible `/metrics` endpoint.
- Metrics for: Active matches, Concurrent players, Kafka consumers, WebSocket connections, Error rates.

### User Story G2.2 — Grafana Dashboards
> **As an operator**, I want dashboards visualizing system activity  
> So that operational issues can be detected early.

**Acceptance Criteria:**
- Dashboards for player concurrency, match lifecycle, and service health.
- Alert rules defined for critical thresholds.

### User Story G2.3 — Structured Logging
> **As a developer**, I want structured logs with correlation identifiers  
> So that distributed debugging is easier.

**Acceptance Criteria:**
- Correlation ID per match.
- User ID included in log context (JSON-formatted).
- Compatible with centralized log aggregation.

---

## G3 — GitOps and Deployment Maturity

### User Story G3.1 — ArgoCD Integration
> **As a DevOps engineer**, I want deployments managed declaratively via ArgoCD  
> So that releases are reproducible and auditable.

**Acceptance Criteria:**
- ArgoCD deployed in cluster.
- Separate GitOps repository for Kubernetes manifests.
- Automatic synchronization from main branch.

---

## G4 — Testing Strategy Expansion

### User Story G4.1 — Backend Unit Testing Standardization
> **As a developer**, I want comprehensive unit tests for business logic  
> So that refactoring and scaling remain safe.

**Acceptance Criteria:**
- High coverage on: Score calculations, Match lifecycle logic, Invite validation.
- Kafka and Redis tested using testcontainers.

### User Story G4.2 — Contract Testing Between Services
> **As a team**, I want service contracts validated automatically  
> So that microservices remain compatible.

**Acceptance Criteria:**
- Consumer-driven contract tests implemented.
- CI fails on breaking changes.
- Kafka event schema validation enforced.

### User Story G4.3 — End-to-End Multiplayer Testing
> **As a QA engineer**, I want automated end-to-end tests  
> So that full gameplay flows are validated continuously.

**Acceptance Criteria:**
- Automated browser tests for frontend (Playwright or Cypress).
- Scenario coverage: login → room → match → leaderboard.
- Tests integrated into CI pipeline.
