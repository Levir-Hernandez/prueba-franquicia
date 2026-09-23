# --- BUILD STAGE ---

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /franquicia

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# --- RUNTIME STAGE ---

FROM eclipse-temurin:21-jre
WORKDIR /franquicia

ARG VERSION=0.0.1-SNAPSHOT
ENV APP_NAME=franquicia

COPY --from=build /franquicia/target/${APP_NAME}-${VERSION}.jar ${APP_NAME}.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar /franquicia/${APP_NAME}.jar"]
