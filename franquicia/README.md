# Franquicia API (V1)

API REST para gestionar franquicias, sus sucursales y los productos de cada sucursal.

- Spring Boot 4.1 · Java 21 · Spring MVC + HATEOAS · JPA/PostgreSQL · Lombok
- Arquitectura hexagonal: `domain` → `application` (puertos y casos de uso) → `infrastructure` (web, persistencia, observabilidad)
- Documentación OpenAPI en `/swagger-ui.html`

## Ejecutar en local

### Opción 1: Docker Compose (recomendada)

Requiere Docker. Levanta PostgreSQL y la API:

```bash
docker compose up --build
```

La API queda en http://localhost:8080 y Swagger en http://localhost:8080/swagger-ui.html.
Los puertos se pueden cambiar con `API_PORT` y `DB_PORT`.

### Opción 2: Maven + PostgreSQL propio

Requiere JDK 21+ y un PostgreSQL accesible:

```bash
docker compose up -d postgres     # o un PostgreSQL existente
./mvnw spring-boot:run
```

| Variable                | Valor por defecto                                 |
|-------------------------|---------------------------------------------------|
| `DB_URL`                | `jdbc:postgresql://localhost:5432/franquicias`    |
| `DB_USER`               | `franquicias`                                     |
| `DB_PASSWORD`           | `franquicias`                                     |
| `SERVER_PORT`           | `8080`                                            |
| `DATOS_INICIALES_PATH`  | `data/franquicias.json` (vacío = no cargar datos) |

En el primer arranque se cargan datos de ejemplo desde `src/main/resources/data/franquicias.json`
(solo si no existe ninguna franquicia, para no duplicarlos).

## Pruebas

```bash
./mvnw test
```

Pruebas unitarias sin infraestructura: reglas del dominio, servicios de aplicación (Mockito)
y controladores (`@WebMvcTest`).

## Endpoints

Base: `/api/v1`

| Método | Ruta                                              | Descripción                                   |
|--------|---------------------------------------------------|-----------------------------------------------|
| POST   | `/franquicias`                                    | Agrega una franquicia                         |
| GET    | `/franquicias`                                    | Lista las franquicias                         |
| GET    | `/franquicias/{franquiciaId}`                     | Consulta una franquicia                       |
| PATCH  | `/franquicias/{franquiciaId}/nombre`              | Actualiza el nombre de una franquicia         |
| POST   | `/franquicias/{franquiciaId}/sucursales`          | Agrega una sucursal a la franquicia           |
| GET    | `/franquicias/{franquiciaId}/sucursales`          | Lista las sucursales de la franquicia         |
| GET    | `/sucursales/{sucursalId}`                        | Consulta una sucursal                         |
| PATCH  | `/sucursales/{sucursalId}/nombre`                 | Actualiza el nombre de una sucursal           |
| POST   | `/sucursales/{sucursalId}/productos`              | Agrega un producto a la sucursal              |
| GET    | `/sucursales/{sucursalId}/productos`              | Lista los productos de la sucursal            |
| GET    | `/productos/{productoId}`                         | Consulta un producto                          |
| DELETE | `/productos/{productoId}`                         | Elimina un producto de su sucursal            |
| PATCH  | `/productos/{productoId}/stock`                   | Modifica el stock de un producto              |
| PATCH  | `/productos/{productoId}/nombre`                  | Actualiza el nombre de un producto            |
| GET    | `/franquicias/{franquiciaId}/productos/mayor-stock` | Producto con más stock de cada sucursal     |

### Ejemplo

```bash
# Crear franquicia
curl -X POST localhost:8080/api/v1/franquicias \
  -H 'Content-Type: application/json' -d '{"nombre": "Franquicia Norte"}'

# Agregar sucursal
curl -X POST localhost:8080/api/v1/franquicias/{franquiciaId}/sucursales \
  -H 'Content-Type: application/json' -d '{"nombre": "Sucursal Centro"}'

# Agregar producto
curl -X POST localhost:8080/api/v1/sucursales/{sucursalId}/productos \
  -H 'Content-Type: application/json' -d '{"nombre": "Cafe 500g", "stock": 40}'

# Producto con más stock por sucursal
curl localhost:8080/api/v1/franquicias/{franquiciaId}/productos/mayor-stock
```

La consulta de mayor stock devuelve un elemento por sucursal indicando a cuál pertenece el
producto. Si varios productos empatan en el máximo se devuelven todos; las sucursales sin
productos no aparecen.

### Errores

Todas las respuestas de error comparten el mismo formato:

```json
{
  "timestamp": "2026-09-23T10:57:01Z",
  "status": 400,
  "error": "Bad Request",
  "mensaje": "La peticion contiene campos invalidos",
  "path": "/api/v1/productos/.../stock",
  "detalles": ["stock: El stock no puede ser negativo"]
}
```

| Código | Causa                                                                      |
|--------|----------------------------------------------------------------------------|
| 400    | Validación de la petición, regla del dominio o identificador mal formado   |
| 404    | El recurso consultado, renombrado o eliminado por su propio id no existe   |

Listar sucursales o productos de un padre inexistente devuelve `200` con la colección
vacía, no `404`: los servicios no consultan el repositorio de la entidad padre.

Cada respuesta incluye la cabecera `X-Trace-Id` (se respeta si el cliente la envía), que también
aparece en cada línea de log de esa petición.

## Estructura

```
domain/                  Entidades y reglas (nombre obligatorio, stock >= 0, relaciones)
application/
  annotation/            @TransactionalUseCase, @ObservableUseCase
  port/in/               Casos de uso (interfaces)
  port/out/              Puertos de persistencia
  service/               Implementación de los casos de uso
infrastructure/
  config/                OpenAPI y carga de datos iniciales
  observability/         Filtro X-Trace-Id y aspecto de trazas de casos de uso
  persistence/           Entidades JPA, repositorios, mapper y adaptadores
  web/                   Controladores, DTOs, mapper, assemblers HATEOAS y manejo de errores
```

## Otros ficheros

| Ruta | Contenido |
|---|---|
| `postman_collection.json` | Colección de Postman con los 15 endpoints |
| `../terraform/` | Infraestructura en AWS (EC2 + RDS + Secrets Manager) |
| `Dockerfile`, `../docker-compose.yml` | Imagen de la API y entorno local |
