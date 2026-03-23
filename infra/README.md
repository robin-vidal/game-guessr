# Infrastructure

GKE Autopilot cluster provisionné via Terraform, state stocké sur Google Cloud Storage.

## Prérequis

- [`terraform`](https://developer.hashicorp.com/terraform/install) >= 1.9
- [`gcloud`](https://cloud.google.com/sdk/docs/install) CLI, authentifié (`gcloud auth application-default login`)
- [`gsutil`](https://cloud.google.com/storage/docs/gsutil_install) (inclus dans gcloud SDK)
- Accès au projet GCP cible

## Mise en place (première fois)

### 1. Copier les fichiers de configuration

```bash
make setup
```

Crée `infra/terraform/backend.hcl` et `infra/terraform/terraform.tfvars` à partir des exemples.
Ces fichiers sont **gitignorés** — ne jamais les committer.

### 2. Renseigner les valeurs

**`infra/terraform/backend.hcl`** — bucket GCS pour le state Terraform :
```hcl
bucket = "mon-projet-tfstate"   # nom unique globalement
prefix = "game-guessr"
```

**`infra/terraform/terraform.tfvars`** — variables du cluster :
```hcl
project_id   = "mon-projet-gcp"
region       = "europe-west1"
cluster_name = "game-guessr"
```

### 3. Créer le bucket GCS

```bash
make bucket
```

Lit le nom du bucket dans `backend.hcl` et la région dans `terraform.tfvars`, crée le bucket et active le versioning.

### 4. Provisionner l'infrastructure

```bash
make up
```

Exécute `terraform init` + `terraform apply`.

## Commandes disponibles

| Commande        | Description                                      |
|-----------------|--------------------------------------------------|
| `make setup`    | Copie les fichiers de config (non-destructif)    |
| `make bucket`   | Crée le bucket GCS pour le state                 |
| `make plan`     | Prévisualise les changements sans appliquer      |
| `make up`       | Initialise et applique l'infrastructure          |
| `make destroy`  | Détruit toute l'infrastructure                   |

## Récupérer le kubeconfig

Après un `make up`, Terraform affiche la commande dans les outputs :

```bash
terraform -chdir=infra/terraform output kubeconfig_command
# puis exécuter la commande affichée
```

## Structure

```
infra/terraform/
├── provider.tf              # Provider Google + backend GCS
├── variables.tf             # Variables (project_id, region, cluster_name)
├── main.tf                  # Cluster GKE Autopilot
├── outputs.tf               # Outputs (nom, région, kubeconfig)
├── terraform.tfvars.example # Template à copier → terraform.tfvars
└── backend.hcl.example      # Template à copier → backend.hcl
```
