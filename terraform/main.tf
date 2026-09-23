########################################################################
# Recursos de los microservicios de franquicias (v2, capa gratuita)
#
# Todo cabe en la capa gratuita de AWS y en el tier M0 de Atlas: una t3.micro
# ejecuta los tres servicios y ActiveMQ con docker compose.
#
# Organizado por componentes, en el orden en que se construye el entorno:
#   1. Valores locales
#   2. Red        - VPC, subred publica e IP elastica
#   3. Seguridad  - grupo de seguridad
#   4. Datos      - un cluster M0 de MongoDB Atlas por servicio
#   5. Imagenes   - repositorios ECR
#   6. Computo    - rol IAM e instancia EC2
########################################################################

# =======================================================================
# 1. Valores locales
# =======================================================================

data "aws_availability_zones" "available" {
  state = "available"
}

locals {
  name = "${var.project_name}-${var.environment}"

  tags_comunes = {
    Project     = "franchise-api"
    Version     = "v2-microservicios-free"
    Environment = var.environment
    ManagedBy   = "terraform"
  }

  servicios = {
    franquicia = { puerto = 8081, base = "franquicias", datos = "data/franquicias.json" }
    sucursal   = { puerto = 8082, base = "sucursales", datos = "data/sucursales.json" }
    producto   = { puerto = 8083, base = "productos", datos = "data/productos.json" }
  }

  registro = split("/", aws_ecr_repository.servicio["franquicia"].repository_url)[0]

  # Prefijo de los parametros de SSM con las URIs de MongoDB.
  parametros = "/${local.name}"
}

# =======================================================================
# 2. Red
#
# Una sola subred publica: no hay balanceador ni base de datos en la VPC.
# =======================================================================

resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = { Name = "${local.name}-vpc" }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = { Name = "${local.name}-igw" }
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = cidrsubnet(var.vpc_cidr, 8, 0)
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true

  tags = { Name = "${local.name}-public" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = { Name = "${local.name}-public-rt" }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

# La IP elastica se crea antes que la instancia: asi Atlas puede autorizar solo esta IP.
resource "aws_eip" "api" {
  domain = "vpc"

  tags = { Name = "${local.name}-eip" }
}

# =======================================================================
# 3. Seguridad
#
# Solo se abren los puertos de las APIs. El broker no se publica: vive en la red
# interna de docker. No se abre el puerto 22: la administracion va por SSM.
# =======================================================================

resource "aws_security_group" "api" {
  name        = "${local.name}-api-sg"
  description = "Trafico entrante a las APIs de franquicias"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "APIs de franquicias, sucursales y productos"
    from_port   = local.servicios.franquicia.puerto
    to_port     = local.servicios.producto.puerto
    protocol    = "tcp"
    cidr_blocks = var.api_allowed_cidrs
  }

  egress {
    description = "Salida libre (ECR, MongoDB Atlas, actualizaciones)"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${local.name}-api-sg" }
}

# =======================================================================
# 4. Datos
#
# Un proyecto de Atlas por servicio porque M0 solo admite un cluster por proyecto.
# Cada usuario solo puede leer y escribir la base de su servicio.
# =======================================================================

resource "mongodbatlas_project" "servicio" {
  for_each = local.servicios

  name   = "${local.name}-${each.key}"
  org_id = var.atlas_org_id
}

resource "mongodbatlas_advanced_cluster" "servicio" {
  for_each = local.servicios

  project_id   = mongodbatlas_project.servicio[each.key].id
  name         = "${local.name}-${each.key}"
  cluster_type = "REPLICASET"

  replication_specs {
    region_configs {
      provider_name         = "TENANT"
      backing_provider_name = "AWS"
      region_name           = var.atlas_region
      priority              = 7

      electable_specs {
        instance_size = "M0"
      }
    }
  }
}

# Solo la IP elastica de la instancia puede conectarse a los clusters.
resource "mongodbatlas_project_ip_access_list" "servicio" {
  for_each = local.servicios

  project_id = mongodbatlas_project.servicio[each.key].id
  cidr_block = "${aws_eip.api.public_ip}/32"
  comment    = "Instancia EC2 de ${local.name}"
}

resource "random_password" "atlas" {
  for_each = local.servicios

  length = 32
  # Sin caracteres especiales para no tener que codificarlos en la URI de conexion.
  special = false
}

resource "mongodbatlas_database_user" "servicio" {
  for_each = local.servicios

  project_id         = mongodbatlas_project.servicio[each.key].id
  auth_database_name = "admin"
  username           = "${each.key}-service"
  password           = random_password.atlas[each.key].result

  roles {
    role_name     = "readWrite"
    database_name = each.value.base
  }

  scopes {
    name = mongodbatlas_advanced_cluster.servicio[each.key].name
    type = "CLUSTER"
  }
}

# Parameter Store y no Secrets Manager: los parametros estandar son gratuitos.
resource "aws_ssm_parameter" "mongo_uri" {
  for_each = local.servicios

  name        = "${local.parametros}/${each.key}/mongo-uri"
  description = "URI de conexion a MongoDB Atlas de ${each.key}-service"
  type        = "SecureString"
  value = format(
    "mongodb+srv://%s:%s@%s/%s?retryWrites=true&w=majority",
    mongodbatlas_database_user.servicio[each.key].username,
    random_password.atlas[each.key].result,
    trimprefix(mongodbatlas_advanced_cluster.servicio[each.key].connection_strings[0].standard_srv, "mongodb+srv://"),
    each.value.base,
  )
}

# =======================================================================
# 5. Imagenes
# =======================================================================

resource "aws_ecr_repository" "servicio" {
  for_each = local.servicios

  name                 = "${var.project_name}/${each.key}-service"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}

# Sin politica de ciclo de vida: docker publica cada imagen como un indice con
# manifiestos hijos sin etiqueta, y expirarlos por cantidad romperia la descarga.
# Las tres imagenes (~145 MB cada una) caben en los 500 MB gratuitos de ECR.

# =======================================================================
# 6. Computo
# =======================================================================

data "aws_ssm_parameter" "amazon_linux" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-6.1-x86_64"
}

resource "aws_iam_role" "api" {
  name = "${local.name}-api-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

# Administracion sin SSH: la instancia no expone el puerto 22.
resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.api.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "ecr_read" {
  role       = aws_iam_role.api.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

# Permiso minimo: leer unicamente los parametros de este despliegue.
resource "aws_iam_role_policy" "read_parameters" {
  name = "${local.name}-read-parameters"
  role = aws_iam_role.api.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["ssm:GetParameter"]
      Resource = [for p in aws_ssm_parameter.mongo_uri : p.arn]
    }]
  })
}

resource "aws_iam_instance_profile" "api" {
  name = "${local.name}-api-profile"
  role = aws_iam_role.api.name
}

resource "aws_instance" "api" {
  ami                    = data.aws_ssm_parameter.amazon_linux.value
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.api.id]
  iam_instance_profile   = aws_iam_instance_profile.api.name

  user_data = templatefile("${path.module}/templates/user-data.sh.tftpl", {
    aws_region = var.aws_region
    registro   = local.registro
    parametros = local.parametros
    compose = templatefile("${path.module}/templates/docker-compose.yml.tftpl", {
      imagenes     = { for nombre, r in aws_ecr_repository.servicio : nombre => "${r.repository_url}:${var.image_tag}" }
      servicios    = local.servicios
      host_publico = aws_eip.api.public_ip
      seed_enabled = var.seed_enabled
    })
  })

  # Cambiar la etiqueta de las imagenes o el arranque implica reemplazar la instancia.
  user_data_replace_on_change = true

  metadata_options {
    http_tokens = "required" # IMDSv2 obligatorio
  }

  # 30 GB es el maximo de EBS incluido en la capa gratuita.
  root_block_device {
    volume_size = 20
    volume_type = "gp3"
    encrypted   = true
  }

  depends_on = [
    aws_ssm_parameter.mongo_uri,
    aws_iam_role_policy.read_parameters,
    mongodbatlas_project_ip_access_list.servicio,
  ]

  tags = { Name = "${local.name}-api" }
}

resource "aws_eip_association" "api" {
  instance_id   = aws_instance.api.id
  allocation_id = aws_eip.api.id
}
