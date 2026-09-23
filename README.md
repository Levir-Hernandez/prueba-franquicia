# Franquicia API: microservicios

API REST para gestionar franquicias, sucursales y productos, separada en tres microservicios
(cada uno con su propia base MongoDB) que se comunican por ActiveMQ.

## Ejecutar en local

Ten Docker abierto y, desde la raíz del proyecto, ejecuta:

```bash
docker compose up --build
```

Espera a que arranque y prueba en:

| Servicio | API | Swagger |
|---|---|---|
| franquicia-service | http://localhost:8081/api/v1/franquicias | http://localhost:8081/swagger-ui.html |
| sucursal-service | http://localhost:8082/api/v1 | http://localhost:8082/swagger-ui.html |
| producto-service | http://localhost:8083/api/v1 | http://localhost:8083/swagger-ui.html |

La consola de ActiveMQ queda en http://localhost:8161 (admin/admin).

Si prefiere inspeccionar con Postman puede importar `postman_collection.json`.

Cuando termines, bájalo con:

```bash
docker compose down -v
```

## Infraestructura en AWS

Pensada para mantenerse en la capa gratuita:

- **VPC** con una subred pública.
- **EC2** (`t3.micro`) con IP elástica que corre los tres servicios y ActiveMQ con docker compose.
  El broker no se publica, solo es accesible dentro de la red de Docker.
- **MongoDB Atlas M0** (gratuito): un cluster por servicio, que solo acepta conexiones desde la IP elástica.
- **Parameter Store** con la cadena de conexión de cada base.
- **ECR** con un repositorio por servicio.
- **Rol IAM** para que la instancia lea los parámetros, descargue de ECR y se administre por SSM.

## Desplegar en AWS

Instala AWS CLI, Terraform 1.6+ y Docker. Configura las credenciales de tu usuario IAM como perfil:

```bash
aws configure --profile franquicia
export AWS_PROFILE=franquicia          # PowerShell: $env:AWS_PROFILE = "franquicia"
```

En [MongoDB Atlas](https://cloud.mongodb.com):

1. En **Organization Settings**, copia el *Organization ID*.
2. En **Access Manager > Applications > API Keys > Create API Key**, crea una clave con el rol
   *Organization Project Creator* y agrega tu IP en su *API Access List*.

Con esos datos crea `terraform/secretos.auto.tfvars` (no se versiona):

```hcl
atlas_org_id      = "..."
atlas_public_key  = "..."
atlas_private_key = "..."
```

Crea primero los repositorios de ECR:

```bash
cd terraform
terraform init
terraform apply -var-file="variables-dev.tfvars" -target="aws_ecr_repository.servicio"
cd ..
```

Sube las imágenes desde la raíz del proyecto:

```bash
REGISTRO=$(terraform -chdir=terraform output -raw ecr_registry)
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $REGISTRO

docker compose build
for s in franquicia sucursal producto; do
  docker tag microservicios-$s-service $REGISTRO/franchise-free/$s-service:2.0.0
  docker push $REGISTRO/franchise-free/$s-service:2.0.0
done
```

Despliega el resto:

```bash
cd terraform
terraform plan -var-file="variables-dev.tfvars"
terraform apply -var-file="variables-dev.tfvars"
```

Abre las `api_urls` o las `swagger_urls` que muestra al final. La instancia tarda unos 5 minutos
en instalar Docker, descargar las imágenes y arrancar los servicios. Si no responde, dale un par de minutos más.

Al finalizar la inspección se destruye usando:

```bash
terraform destroy -var-file="variables-dev.tfvars"
```
