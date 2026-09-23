# Infraestructura — microservicios de franquicias (v3 reactiva, capa gratuita)

Pensada para costar 0: una EC2
`t3.micro` ejecuta los tres servicios y ActiveMQ con docker compose, y cada servicio
usa su propio cluster M0 (gratuito) de MongoDB Atlas.

```text
                     Internet
                         |   :8081  :8082  :8083
        +----------------+-----------------------------+
        |  EC2 t3.micro (IP elastica) - docker compose |
        |  franquicia-service  sucursal-service  producto-service
        |              \          |          /         |
        |               ActiveMQ (red interna)          |
        +----------------+-----------------------------+
                         |  solo desde la IP elastica
        MongoDB Atlas M0 x3 (franquicias, sucursales, productos)
```

## Estructura

| Fichero | Contenido |
|---|---|
| `provider.tf` | Bloque `terraform`, backend y proveedores AWS y MongoDB Atlas |
| `main.tf` | Los recursos, por componentes: red, seguridad, datos, imagenes y computo |
| `variables.tf` | Declaracion de variables, sin valores concretos |
| `outputs.tf` | Salidas del `apply` |
| `variables-dev.tfvars` | Valores del entorno |
| `secretos.auto.tfvars` | Id de la organizacion y claves de Atlas. **No se versiona**; se crea a mano |
| `templates/` | Arranque de la instancia y docker compose que ejecuta |

## Requisitos

1. Perfil de AWS con permisos (`AWS_PROFILE=franquicia`).
2. En [MongoDB Atlas](https://cloud.mongodb.com):
   - **Organization Settings**: copiar el *Organization ID*.
   - **Access Manager > Applications > API Keys > Create API Key**, con el rol
     *Organization Project Creator*. Copiar la clave publica y la privada, y agregar
     la IP propia en su *API Access List*.
3. Crear `secretos.auto.tfvars` en esta carpeta:

```hcl
atlas_org_id      = "..."
atlas_public_key  = "..."
atlas_private_key = "..."
```

## Uso

**1. Crear los repositorios** (el resto depende de que existan):

```bash
export AWS_PROFILE=franquicia

terraform init
terraform apply -var-file="variables-dev.tfvars" -target="aws_ecr_repository.servicio"
```

**2. Publicar las imagenes**, desde la raiz del proyecto:

```bash
REGISTRO=$(terraform -chdir=terraform output -raw ecr_registry)
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $REGISTRO

docker compose build
for s in franquicia sucursal producto; do
  docker tag microservicios-$s-service $REGISTRO/franchise-free/$s-service:3.0.0
  docker push $REGISTRO/franchise-free/$s-service:3.0.0
done
```

**3. Desplegar el resto**

```bash
terraform apply -var-file="variables-dev.tfvars"
terraform output api_urls
```

La instancia tarda unos 5 minutos en instalar Docker, descargar las imagenes y
arrancar. Si las imagenes aun no estan publicadas, las reintenta cada minuto durante
una hora.

Para destruirlo:

```bash
terraform destroy -var-file="variables-dev.tfvars"
```

## Por que cuesta 0

| Recurso | Capa gratuita |
|---|---|
| EC2 `t3.micro` | 750 h/mes durante 12 meses |
| EBS 20 GB | 30 GB/mes |
| IP publica (elastica) | 750 h/mes |
| ECR | 500 MB; las tres imagenes ocupan ~450 MB. Al publicar una version nueva hay que borrar la anterior |
| Parameter Store | Parametros estandar gratuitos (en vez de Secrets Manager) |
| MongoDB Atlas M0 ×3 | Gratuito sin limite de tiempo |

Las cuentas creadas desde julio de 2025 usan el plan de creditos en lugar de la capa
de 12 meses: el entorno consume creditos, muy pocos.

## Decisiones

**Memoria medida, no estimada.** Con los limites de `templates/docker-compose.yml.tftpl`
los tres servicios y el broker ocupan unos 800 MB y la coleccion de Postman pasa
completa. Se agregan 2 GB de swap para absorber picos.

**Atlas solo acepta la IP elastica.** La IP se crea antes que la instancia, de modo que
los clusters nunca quedan abiertos a Internet.

**El broker no se publica.** ActiveMQ solo es alcanzable dentro de la red de docker.

## Limites conocidos

- **Todo en una instancia.** Si la instancia cae, caen los tres servicios. Es un
  entorno de prueba; para algo estable, pasar a un servicio administrado (p. ej. ECS Fargate).
- **Arranque lento.** La `t3.micro` tiene CPU con creditos: tres JVM arrancando a la
  vez tardan un par de minutos.
- **Sin HTTPS.**
