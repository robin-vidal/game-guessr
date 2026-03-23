variable "project_id" {
  description = "GCP project ID"
  type        = string
}

variable "region" {
  description = "GCP region for the GKE Autopilot cluster"
  type        = string
  default     = "europe-west1"
}

variable "cluster_name" {
  description = "Name of the GKE Autopilot cluster"
  type        = string
  default     = "game-guessr"
}
