# Arquitectura

El monolito se ha separado en tres microservicios: franquicia, sucursal y producto.
Cada uno mantiene la arquitectura hexagonal y tiene su propia base MongoDB.
Se comunican por ActiveMQ con request-reply (sin replicar datos) protegido por un circuit breaker.

```
franquicia-service :8081  ◄── pregunta ──  sucursal-service :8082  ◄── pregunta ──  producto-service :8083
        │                    (ActiveMQ)             │                 (ActiveMQ)              │
   Mongo franquicias                          Mongo sucursales                          Mongo productos
```

## Tecnologías clave

| Área | Tecnología |
|---|---|
| Lenguaje y framework | Java 21, Spring Boot 4.1, Maven multi-módulo |
| API | Spring MVC, Spring HATEOAS, Bean Validation, springdoc OpenAPI (Swagger) |
| Persistencia | Spring Data MongoDB (una base por servicio) |
| Mensajería | ActiveMQ Classic, JMS request-reply (`JmsTemplate.sendAndReceive`) |
| Resiliencia | Resilience4j (circuit breaker + timeout), Spring Boot Actuator |
| Transversal | AspectJ (trazas de casos de uso), `X-Trace-Id` propagado por JMS, Lombok |
| Contenedores | Docker (una imagen por servicio), Docker Compose |
| Nube | Terraform, AWS: EC2, ECR, Parameter Store, IAM, SSM, más MongoDB Atlas M0 |

## Carpetas

```
R2/
├── microservicios/               proyecto Maven (pom padre con los tres módulos)
│   ├── franquicia-service/
│   ├── sucursal-service/
│   └── producto-service/         cada uno con su Dockerfile y la misma estructura:
│       └── src/main/java/.../
│           ├── domain/                   modelo y reglas del negocio
│           ├── application/
│           │   ├── port/in/              casos de uso (interfaces)
│           │   ├── port/out/             repositorio y consultas a otros servicios
│           │   └── service/              implementación de los casos de uso
│           └── infrastructure/
│               ├── adapter/in/web/           controladores, DTOs, HATEOAS, errores
│               ├── adapter/in/messaging/     listeners JMS que responden a otros servicios
│               ├── adapter/out/persistence/  documentos y repositorios Mongo
│               ├── adapter/out/messaging/    consultas JMS con timeout y circuit breaker
│               ├── config/               beans, OpenAPI y datos iniciales
│               └── observability/        traceId y logging de casos de uso
├── terraform/                    infraestructura en AWS + Atlas (capa gratuita)
│   └── templates/                arranque de la EC2 y docker compose que ejecuta
├── docker-compose.yml            entorno local: 3 servicios + 3 Mongo + ActiveMQ
└── postman_collection.json       colección con todos los endpoints
```

## Flujo de una petición entre servicios

```
POST /sucursales/{id}/productos → producto-service → caso de uso
     → SucursalConsultaPort → JMS "sucursal.consulta.existe" → sucursal-service responde
     → si existe: guarda en Mongo productos · si no responde: 503 (circuit breaker)
```
