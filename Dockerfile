# ---- Stage 1: build the jar ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# pom.xml first: this layer is cached until dependencies change
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
# Tests need a live DB, which doesn't exist at build time
RUN mvn -q package -DskipTests

# ---- Stage 2: runtime image ----
# JRE only: smaller image, less attack surface than a full JDK
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as an unprivileged user, not root
RUN useradd --system --no-create-home appuser
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]