# Entorno de desarrollo en la capa gratuita, con datos de ejemplo cargados.
# atlas_org_id y las claves de Atlas van en secretos.auto.tfvars (excluido de git).

aws_region  = "us-east-1"
environment = "dev"

# Restringir a la IP publica del equipo de desarrollo.
api_allowed_cidrs = ["0.0.0.0/0"]

image_tag     = "3.0.0"
instance_type = "t3.micro"
seed_enabled  = true

atlas_region = "US_EAST_1"
