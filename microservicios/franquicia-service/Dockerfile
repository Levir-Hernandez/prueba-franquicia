# --- BUILD STAGE ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Las dependencias se descargan en una capa aparte: solo se repite si cambia el pom
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

# --- RUNTIME STAGE ---
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --uid 10001 app
USER app

COPY --from=build /app/target/franquicia-service-0.0.1-SNAPSHOT.jar app.jar

# La JVM toma la memoria del limite del contenedor (Fargate, docker compose)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
