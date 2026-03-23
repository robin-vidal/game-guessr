output "cluster_name" {
  description = "GKE cluster name"
  value       = google_container_cluster.main.name
}

output "cluster_location" {
  description = "GKE cluster region"
  value       = google_container_cluster.main.location
}

output "kubeconfig_command" {
  description = "Command to configure kubectl"
  value       = "gcloud container clusters get-credentials ${google_container_cluster.main.name} --region ${google_container_cluster.main.location} --project ${var.project_id}"
}
