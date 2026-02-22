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
- Infrastructure-as-Code: Single GKE Autopilot cluster provisioned via **Terraform**
- Each microservice containerized
- Helm charts for application deployment
- Separate `dev` and `prod` namespaces
- ConfigMaps + Secrets properly scoped per namespace

---

## E3 — CI/CD
- PR → build Docker images
- Merge → push image + deploy via Helm
- All pushes → Lint + Format + Run unit tests
- Workflows are only ran for the sub-repositories that were changed (a push on backend mustn't trigger frontend workflows)
- Automated integration tests
