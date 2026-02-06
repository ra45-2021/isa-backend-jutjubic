# =============================================
# Dockerfile za MQ JSON vs Protobuf Benchmark aplikaciju
# =============================================
# Multi-stage build:
# 1. Maven build stage - kompajlira aplikaciju
# 2. Runtime stage - pokreće aplikaciju
# =============================================

# ----- BUILD STAGE -----
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Kopiraj pom.xml i preuzmi zavisnosti (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Kopiraj source code
COPY src ./src

# Build aplikaciju (preskači testove za brži build)
RUN mvn clean package -DskipTests

# ----- RUNTIME STAGE -----
FROM eclipse-temurin:21-jre

WORKDIR /app

# Kopiraj JAR iz build stage-a
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=30s \
    CMD curl -f http://localhost:8080/api/benchmark/health || exit 1

# Pokreni aplikaciju
ENTRYPOINT ["java", "-jar", "app.jar"]
