# Arquitectura

Con la arquitectura de microservicios ya establecida (franquicia, sucursal y producto, cada uno hexagonal,
con su propia base MongoDB y comunicados por ActiveMQ con request-reply y circuit breaker), el siguiente paso
es volverlos reactivos de punta a punta: WebFlux sobre Netty, MongoDB Reactive y casos de uso que devuelven
`Mono` / `Flux`, de modo que ninguna capa bloquee el event loop.

La separación en servicios, las colas y la infraestructura se mantienen igual que en R2.

```
franquicia-service :8081  ◄── pregunta ──  sucursal-service :8082  ◄── pregunta ──  producto-service :8083
        │                    (ActiveMQ)             │                 (ActiveMQ)              │
   Mongo franquicias                          Mongo sucursales                          Mongo productos
```

## Tecnologías clave

| Área | Tecnología |
|---|---|
| Lenguaje y framework | Java 21, Spring Boot 4.1, Maven multi-módulo |
| API | Spring WebFlux (Netty), Spring HATEOAS, Bean Validation, springdoc OpenAPI (Swagger) |
| Persistencia | Spring Data MongoDB Reactive, agregaciones con `ReactiveMongoTemplate` (una base por servicio) |
| Mensajería | ActiveMQ Classic, JMS request-reply (`JmsTemplate.sendAndReceive` en el scheduler `boundedElastic`) |
| Resiliencia | Resilience4j Reactor (circuit breaker + timeout), Spring Boot Actuator |
| Transversal | Reactor context-propagation (`X-Trace-Id` en el MDC y por JMS), AspectJ, Lombok |
| Pruebas | JUnit 5, Mockito, `reactor-test` (StepVerifier), `@WebFluxTest` |
| Contenedores | Docker (una imagen por servicio), Docker Compose |
| Nube | Terraform, AWS: EC2, ECR, Parameter Store, IAM, SSM, más MongoDB Atlas M0 |

## Carpetas

```
R3/
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
│               ├── adapter/in/web/           controladores WebFlux, DTOs, HATEOAS, errores
│               ├── adapter/in/messaging/     listeners JMS que responden a otros servicios
│               ├── adapter/out/persistence/  ReactiveMongoRepository y agregaciones
│               ├── adapter/out/messaging/    consultas JMS con timeout y circuit breaker
│               ├── config/               beans, OpenAPI y datos iniciales
│               └── observability/        WebFilter de traceId (contexto Reactor) y logging de casos de uso
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
