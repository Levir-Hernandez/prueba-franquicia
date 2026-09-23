########################################################################
# Declaracion de variables
#
# Los valores de cada entorno viven en variables-<entorno>.tfvars. Las claves de
# Atlas van aparte, en secretos.auto.tfvars (excluido de git) o en variables de entorno.
########################################################################

# --- Identidad del despliegue -----------------------------------------

variable "aws_region" {
  description = "Region de AWS donde se despliega la solucion"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Nombre del entorno"
  type        = string
  default     = "dev"

  validation {
    condition     = contains(["dev", "qa"], var.environment)
    error_message = "Esta variante es solo para entornos de prueba: dev o qa."
  }
}

variable "project_name" {
  description = "Prefijo de los nombres de los recursos"
  type        = string
  default     = "franchise-free"
}

# --- Red ---------------------------------------------------------------

variable "vpc_cidr" {
  description = "Rango de direcciones de la VPC"
  type        = string
  default     = "10.40.0.0/16"

  validation {
    condition     = can(cidrhost(var.vpc_cidr, 0))
    error_message = "vpc_cidr debe ser un bloque CIDR valido."
  }
}

variable "api_allowed_cidrs" {
  description = "Origenes autorizados a consumir las APIs"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

# --- Aplicacion ---------------------------------------------------------

variable "image_tag" {
  description = "Etiqueta de las imagenes publicadas en los repositorios ECR de cada servicio"
  type        = string
}

variable "instance_type" {
  description = "Tipo de instancia. t3.micro es la elegible para la capa gratuita."
  type        = string
  default     = "t3.micro"
}

variable "seed_enabled" {
  description = "Carga el juego de datos de ejemplo al arrancar cada servicio"
  type        = bool
  default     = true
}

# --- MongoDB Atlas ------------------------------------------------------

variable "atlas_org_id" {
  description = "Organizacion de MongoDB Atlas donde se crean los proyectos"
  type        = string
}

variable "atlas_public_key" {
  description = "Clave publica de la API de Atlas (null: se lee de MONGODB_ATLAS_PUBLIC_KEY)"
  type        = string
  default     = null
  sensitive   = true
}

variable "atlas_private_key" {
  description = "Clave privada de la API de Atlas (null: se lee de MONGODB_ATLAS_PRIVATE_KEY)"
  type        = string
  default     = null
  sensitive   = true
}

variable "atlas_region" {
  description = "Region de Atlas (formato Atlas). Debe coincidir con aws_region para reducir latencia."
  type        = string
  default     = "US_EAST_1"
}
