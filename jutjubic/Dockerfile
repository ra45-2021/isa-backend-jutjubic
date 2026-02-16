FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y ffmpeg curl && rm -rf /var/lib/apt/lists/*

RUN groupadd -r jutjubic && useradd -r -g jutjubic jutjubic

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/uploads/thumbs /app/uploads/thumbs/compressed /app/uploads/videos /app/uploads/tmp /app/uploads/transcoded \
    && chown -R jutjubic:jutjubic /app

USER jutjubic
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
