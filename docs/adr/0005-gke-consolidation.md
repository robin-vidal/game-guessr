# ADR 0005: Consolidating All Services into a Single GKE Cluster

## Status
Proposed

## Context
GameGuessr relies on stateful services (PostgreSQL, Redis, Kafka). Production best practices dictate using managed cloud services (Cloud SQL, Memorystore, Confluent) for high availability and data durability.

However, the academic defense project has a strict **300€/month budget limit**. Expected traffic is very low (only the professor and the team will access it).

## Decision
We decided to deploy our entire stack—both stateless microservices and stateful databases/brokers—as Pods within a **single Kubernetes (GKE) Zonal Cluster** using PersistentVolumeClaims (PVCs). 

To further optimize costs, we will:
1. Use a **Zonal GKE cluster** to avoid regional control plane fees.
2. Use **GitHub Container Registry (`ghcr.io`)** instead of Google Artifact Registry.

## Consequences

### Positive
*   **Budget Compliance**: Avoids ~150€+ in monthly managed service fees. Total compute cost stays ~100-150€/month.
*   **Centralized IaC**: Everything is managed as Kubernetes/Helm manifests.
*   **No Vendor Lock-in**: Helm charts for PG/Redis/Kafka can run on any generic K8s cluster.

### Negative
*   **No Automated Backups**: Loss of a Persistent Volume means permanent data loss.
*   **Lower Resiliency**: A Zonal cluster offers no cross-zone redundancy during an outage.

### Mitigation
Given the short 1-month lifespan for the academic defense, data loss is an acceptable risk. Database seeding (maps, users) will be fully automated via init-scripts to recover quickly.
