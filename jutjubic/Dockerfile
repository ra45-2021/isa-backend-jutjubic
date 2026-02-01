# =============================================
# DOCKERFILE za Jutjubic aplikaciju
# =============================================
# Ovaj fajl definiše kako se pravi Docker image
# aplikacije. Image je kao "snimak" aplikacije
# koji se može pokrenuti bilo gde gde postoji Docker.
# =============================================

# FAZA 1: BUILD
# Koristimo Maven image sa Javom 21 za kompajliranje
FROM maven:3.9-eclipse-temurin-21 AS build

# Postavljamo radni direktorijum u kontejneru
WORKDIR /app

# Kopiramo pom.xml i izvorni kod
COPY pom.xml .
COPY src ./src

# Kompajliramo aplikaciju (bez testova za brži build)
# Dependencies se preuzimaju automatski tokom builda
RUN mvn package -DskipTests -B

# =============================================
# FAZA 2: RUN
# Koristimo manji JRE image za pokretanje
# (ne treba nam ceo JDK i Maven u produkciji)
# =============================================
FROM eclipse-temurin:21-jre

# Instaliramo ffmpeg za video transcoding
RUN apt-get update && apt-get install -y ffmpeg curl && rm -rf /var/lib/apt/lists/*

# Kreiramo non-root korisnika za sigurnost
RUN groupadd -r jutjubic && useradd -r -g jutjubic jutjubic

# Postavljamo radni direktorijum
WORKDIR /app

# Kopiramo JAR iz build faze
COPY --from=build /app/target/*.jar app.jar

# Kreiramo direktorijume za upload i transcoding fajlove
RUN mkdir -p /app/uploads/thumbs /app/uploads/thumbs/compressed /app/uploads/videos /app/uploads/tmp /app/uploads/transcoded \
    && chown -R jutjubic:jutjubic /app

# Prelazimo na non-root korisnika
USER jutjubic

# Izlažemo port 8080 (informativno)
EXPOSE 8080

# Health check - Docker proverava da li je kontejner zdrav
# Interval: svakih 30 sekundi
# Timeout: 10 sekundi za odgovor
# Retries: 3 pokušaja pre nego što se označi kao unhealthy
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

# Pokretanje aplikacije
# JAVA_OPTS omogućava podešavanje JVM parametara preko environment varijable
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
