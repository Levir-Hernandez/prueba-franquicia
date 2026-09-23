# Franquicia API

API REST para gestionar franquicias, sucursales y productos.

## Ejecutar en local

Ten Docker abierto y, desde la raíz del proyecto, ejecuta:

```bash
docker compose up --build
```

Espera a que arranque y prueba en:

- API: http://localhost:8080/api/v1/franquicias
- Swagger: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/v3/api-docs

Si prefiere Postman, importe el archivo `postman_collection.json`.

Cuando termines, bájalo con:

```bash
docker compose down -v
```

## Infraestructura en AWS

- **VPC** con subredes públicas y privadas en dos zonas.
- **EC2** (`t3.small`) que corre la API en Docker, con IP elástica.
- **RDS PostgreSQL** (`db.t4g.micro`) en subred privada, solo accesible desde la API.
- **Secrets Manager** con las credenciales de la base.
- **Rol IAM** para que la instancia lea el secreto, descargue de ECR y se administre por SSM.

## Desplegar en AWS

Instala AWS CLI, Terraform 1.6+ y Docker. Configura las credenciales de tu usuario IAM como perfil:

```bash
aws configure --profile franquicia
export AWS_PROFILE=franquicia          # PowerShell: $env:AWS_PROFILE = "franquicia"
```

Sube la imagen a ECR, cambiando `<CUENTA>` por tu número de cuenta (`aws sts get-caller-identity --query Account --output text`):

```bash
aws ecr create-repository --repository-name franquicia --region us-east-1
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <CUENTA>.dkr.ecr.us-east-1.amazonaws.com
docker build -t <CUENTA>.dkr.ecr.us-east-1.amazonaws.com/franquicia:1.0.0 ./franquicia
docker push <CUENTA>.dkr.ecr.us-east-1.amazonaws.com/franquicia:1.0.0
```

Copia el archivo de variables, pon tu imagen en `api_image` y despliega:

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
terraform init
terraform plan
terraform apply
```

Abre la `api_url` o la `swagger_url` que muestra al final. Si no responde, dale un par de minutos.

Al finalizar la inspección destrúyelo con:

```bash
terraform destroy
```
