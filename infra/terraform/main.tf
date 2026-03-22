resource "google_container_cluster" "main" {
  name     = var.cluster_name
  location = var.region

  enable_autopilot = true

  release_channel {
    channel = "REGULAR"
  }

  # Delete the default node pool — not applicable for Autopilot but required for the
  # resource block to be valid; Autopilot manages nodes automatically.
  deletion_protection = false
}
