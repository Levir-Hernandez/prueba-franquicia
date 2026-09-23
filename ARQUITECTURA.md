# Arquitectura

Monolito con arquitectura hexagonal: el dominio no depende de nada, la aplicación define
puertos y casos de uso, y la infraestructura implementa los adaptadores.

## Tecnologías clave

| Área | Tecnología |
|---|---|
| Lenguaje y framework | Java 21, Spring Boot 4.1 |
| API | Spring MVC, Spring HATEOAS, Bean Validation, springdoc OpenAPI (Swagger) |
| Persistencia | Spring Data JPA, PostgreSQL 16 |
| Transversal | AspectJ (transacciones y trazas de casos de uso), Lombok |
| Pruebas | JUnit 5, Mockito, `@WebMvcTest` |
| Contenedores | Docker (build multi-etapa con Maven), Docker Compose |
| Nube | Terraform, AWS: EC2, RDS PostgreSQL, Secrets Manager, ECR, IAM, SSM |

## Carpetas

```
R1/
├── franquicia/                 código de la API (Maven)
│   ├── Dockerfile
│   └── src/main/java/.../franquicia/
│       ├── domain/             modelo y reglas del negocio, sin dependencias de framework
│       ├── application/
│       │   ├── port/in/        casos de uso (interfaces)
│       │   ├── port/out/       puertos de persistencia
│       │   ├── service/        implementación de los casos de uso
│       │   └── annotation/     @TransactionalUseCase, @ObservableUseCase
│       └── infrastructure/
│           ├── adapter/in/web/        controladores, DTOs, assemblers HATEOAS, manejo de errores
│           ├── adapter/out/persistence/ entidades JPA, repositorios y adaptadores de los puertos
│           ├── config/         OpenAPI y carga de datos iniciales
│           └── observability/  filtro X-Trace-Id y trazas de casos de uso
├── terraform/                  infraestructura en AWS
│   └── templates/              script de arranque de la EC2
├── docker-compose.yml          entorno local: API + PostgreSQL
└── postman_collection.json     colección con todos los endpoints
```

## Flujo de una petición

```
HTTP → controller (adapter/in/web) → caso de uso (port/in) → service
     → dominio → puerto de persistencia (port/out) → adaptador JPA → PostgreSQL
```
