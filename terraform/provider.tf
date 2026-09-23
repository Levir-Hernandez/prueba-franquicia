########################################################################
# Proveedores y estado
#
# Bloque terraform, definicion del backend y configuracion de los proveedores
# de AWS y de MongoDB Atlas.
########################################################################

terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
    mongodbatlas = {
      source  = "mongodb/mongodbatlas"
      version = "~> 1.26"
    }
  }

  # Estado local: suficiente para un entorno de prueba de una sola persona.
  #
  # backend "s3" {
  #   bucket       = "franchise-tfstate"
  #   key          = "v2-free/terraform.tfstate"
  #   region       = "us-east-1"
  #   encrypt      = true
  #   use_lockfile = true
  # }
}

provider "aws" {
  region = var.aws_region

  # Etiquetas aplicadas a todo recurso que las admita, sin repetirlas una a una.
  default_tags {
    tags = local.tags_comunes
  }
}

# Si las claves no se pasan por variable, el proveedor las toma de las variables de
# entorno MONGODB_ATLAS_PUBLIC_KEY y MONGODB_ATLAS_PRIVATE_KEY.
provider "mongodbatlas" {
  public_key  = var.atlas_public_key
  private_key = var.atlas_private_key
}
