# syntax=docker/dockerfile:1

# ---------- Stage 1: Build ----------
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY springboot/demo/pom.xml ./
COPY springboot/demo/.mvn ./.mvn
COPY springboot/demo/mvnw ./
RUN mvn -q -B -DskipTests dependency:go-offline
COPY springboot/demo/src ./src
COPY springboot/demo/config ./config
RUN mvn -q -B -DskipTests clean package

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

EXPOSE 8089
ENV TZ=UTC

# Limites de memória para Railway Free
ENV JAVA_OPTS="-Xms128m -Xmx256m -XX:+UseSerialGC -Dfile.encoding=UTF-8"

COPY --from=builder /app/target/*.jar app.jar

RUN useradd -r -u 1001 appuser && chown appuser:appuser /app
USER appuser

# Respeita $PORT do Railway; fallback 8089 local
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=${PORT:-8089} -jar app.jar"]
