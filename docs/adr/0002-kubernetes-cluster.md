# ADR 0002: Kubernetes Cluster Strategy

**Date:** 2026-02-22

## Context
Deploying our stack (frontend, 4 Spring Boot microservices, Authentik, Kafka, Redis, PostgreSQL) to Kubernetes requires isolating environments. We need to decide whether to run two distinct clusters (e.g., `cluster-dev`, `cluster-prod`) or a single cluster with logic separation using namespaces. 

## Decision
We will use **1 Single Kubernetes Cluster** partitioned into **2 Namespaces** (`gameguessr-dev` and `gameguessr-prod`). 

## Rationale
- **Cost & Resource Efficiency**: Running two entirely separate clusters for a student/MVP project is overkill and consumes unnecessary baseline resources (Control Plane overhead).
- **Simplicity**: For an MVP, managing a single Helm deployment target with two release names (in different namespaces) is vastly simpler than managing contexts between two separate cloud providers or local setups.
- **Maintenance**: Upgrades to infrastructure tools (Ingress controllers, Cert Manager) only need to happen once.

## Consequences
- **Positive:** Lower infrastructure footprint, easier local development/testing loop (e.g., minikube).
- **Negative / Risk:** If the cluster goes down, both `dev` and `prod` go down simultaneously. A misconfigured network policy in `dev` might impact `prod`.
- **Implication:** We must rely heavily on Kubernetes **Namespaces** to isolate workloads and use **ConfigMaps/Secrets** strictly scoped to their respective namespace to prevent Dev services from reading Prod databases.