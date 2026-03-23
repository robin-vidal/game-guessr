terraform {
  required_version = ">= 1.9"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 6.0"
    }
  }

  backend "gcs" {
    # Configured via backend.hcl (not committed)
    # terraform init -backend-config=backend.hcl
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}
