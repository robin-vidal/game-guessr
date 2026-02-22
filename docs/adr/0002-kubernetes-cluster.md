# ADR 0002: Kubernetes Cluster Strategy

**Date:** 2026-02-22

## Context
Deploying our stack (frontend, 4 Spring Boot microservices, Authentik, Kafka, Redis, PostgreSQL) to Kubernetes requires isolating environments. We need to decide whether to run two distinct clusters (e.g., `cluster-dev`, `cluster-prod`) or a single cluster with logic separation using namespaces. 

## Decision
We will use **2 distinct Kubernetes Clusters** (e.g., `gameguessr-dev` and `gameguessr-prod`). 

## Rationale
- **Strong Isolation**: A complete separation between development and production environments guarantees that experimental features, load spikes from testing, or misconfigured network policies in Dev will never impact the Production cluster.
- **Security**: Access rights, secrets, and database connections are physically isolated, drastically reducing the risk of a developer accidentally corrupting production data.
- **Upgrades**: We can test Kubernetes version upgrades, Ingress updates, or Helm charts on the Dev cluster safely before rolling them out to Prod.

## Consequences
- **Positive:** Maximum security, zero risk of cross-environment interference, easier compliance and auditing.
- **Negative / Risk:** Higher infrastructure costs (two Control Planes to pay for), and slightly increased operational overhead as configurations must be applied twice.
- **Implication:** Our CI/CD pipeline needs to be robust enough to handle multi-cluster deployments cleanly, explicitly targeting the `dev` cluster on PR merges, and the `prod` cluster for final releases.