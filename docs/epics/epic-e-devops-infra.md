# EPIC E — DevOps & Infrastructure

## E1 — Monorepo Structure
**[TODO]:** voir avec team archi (aka Michael)

- `/frontend`
- `/services/auth`
- `/services/match`
- `/services/game`
- `/services/score`
- `/helm`
- `/infra`

---

## E2 — Infrastructure & Kubernetes Deployment
- Infrastructure-as-Code: GKE Autopilot clusters provisioned via **Terraform**
- Each microservice containerized
- Helm charts for application deployment
- ConfigMaps + Secrets properly scoped per cluster

---

## E3 — CI/CD
- PR → build Docker images
- Merge → push image + deploy via Helm
- All pushes → Lint + Format + Run unit tests
- Workflows are only ran for the sub-repositories that were changed (a push on backend mustn't trigger frontend workflows)
- Automated integration tests
