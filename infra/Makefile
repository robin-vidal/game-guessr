TF_DIR := terraform
TF     := terraform -chdir=$(TF_DIR)

BACKEND_HCL   := $(TF_DIR)/backend.hcl
TFVARS        := $(TF_DIR)/terraform.tfvars

.PHONY: setup bucket up destroy plan help

## help: list available targets
help:
	@grep -E '^## ' $(MAKEFILE_LIST) | sed 's/## //' | column -t -s ':'

## setup: copy example config files (first-time, non-destructive)
setup:
	@[ -f $(BACKEND_HCL) ] \
		&& echo "$(BACKEND_HCL) already exists, skipping" \
		|| (cp $(TF_DIR)/backend.hcl.example $(BACKEND_HCL) && echo "Created $(BACKEND_HCL)")
	@[ -f $(TFVARS) ] \
		&& echo "$(TFVARS) already exists, skipping" \
		|| (cp $(TF_DIR)/terraform.tfvars.example $(TFVARS) && echo "Created $(TFVARS)")
	@echo ""
	@echo "Next steps:"
	@echo "  1. Edit $(BACKEND_HCL)  -> set bucket name"
	@echo "  2. Edit $(TFVARS) -> set project_id"
	@echo "  3. make bucket           -> create the GCS state bucket"
	@echo "  4. make up               -> provision the cluster"

## bucket: create the GCS bucket for Terraform state (reads bucket from backend.hcl)
bucket: $(BACKEND_HCL)
	@BUCKET=$$(grep -E '^\s*bucket\s*=' $(BACKEND_HCL) | sed -E 's/.*=\s*"(.*)"/\1/'); \
	REGION=$$(grep -E '^\s*region\s*=' $(TFVARS) 2>/dev/null | sed -E 's/.*=\s*"(.*)"/\1/'); \
	REGION=$${REGION:-europe-west1}; \
	if [ -z "$$BUCKET" ]; then echo "Error: bucket not set in $(BACKEND_HCL)"; exit 1; fi; \
	echo "Creating GCS bucket gs://$$BUCKET in $$REGION..."; \
	gsutil mb -l $$REGION gs://$$BUCKET || true; \
	gsutil versioning set on gs://$$BUCKET

## plan: preview changes without applying
plan: $(BACKEND_HCL) $(TFVARS)
	$(TF) init -backend-config=backend.hcl -reconfigure
	$(TF) plan

## up: initialise Terraform and apply the infrastructure
up: $(BACKEND_HCL) $(TFVARS)
	$(TF) init -backend-config=backend.hcl
	$(TF) apply

## destroy: destroy all Terraform-managed infrastructure
destroy: $(BACKEND_HCL)
	$(TF) destroy

$(BACKEND_HCL):
	@echo "Error: $(BACKEND_HCL) not found. Run 'make setup' first." && exit 1

$(TFVARS):
	@echo "Error: $(TFVARS) not found. Run 'make setup' first." && exit 1
