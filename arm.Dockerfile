# ============================================================
# ARM64 build — all base images pinned to linux/arm64.
# Use: docker buildx build --platform linux/arm64 -f arm.dockerfile .
# Or on an ARM host (Apple M-series, AWS Graviton):
#   docker build -f arm.dockerfile .
# ============================================================
 
# ============================================================
# Stage 1: Dependency Cache
# Copies pom.xml only and downloads all dependencies.
# This layer is cached by Docker — re-used on every build
# as long as pom.xml hasn't changed. Saves ~2-3 min per build.
# ============================================================
FROM --platform=linux/arm64 maven:3.9.9-eclipse-temurin-17 AS deps
 
WORKDIR /app
 
COPY pom.xml .
 
RUN mvn dependency:go-offline -B --no-transfer-progress
 
 
# ============================================================
# Stage 2: Builder
# Pulls cached deps from Stage 1, copies source, builds JAR.
# Also extracts layered JAR here so Stage 3 can copy layers.
# ============================================================
FROM --platform=linux/arm64 maven:3.9.9-eclipse-temurin-17 AS builder
 
WORKDIR /app
 
# Reuse cached Maven local repo from Stage 1
COPY --from=deps /root/.m2 /root/.m2
 
COPY pom.xml .
COPY src ./src
 
# Build fat JAR — tests are skipped here because they
# run as a dedicated earlier stage in the Jenkins pipeline
RUN mvn clean package -DskipTests --no-transfer-progress
 
# Extract layered JAR into 4 layers (Spring Boot 3.x feature):
#   dependencies        — third-party libs (changes rarely)
#   spring-boot-loader  — Spring Boot loader (changes rarely)
#   snapshot-dependencies — SNAPSHOT libs (changes sometimes)
#   application         — your code only (changes every commit)
# On code-only changes, only the ~500KB application layer
# gets rebuilt. The other 3 layers are reused from cache.
RUN java -Djarmode=layertools \
         -jar target/ProjectManagement.jar \
         extract \
         --destination target/extracted
 
 
# ============================================================
# Stage 3: Production Runtime
# Minimal JRE only — no compiler, no Maven, no build tools.
# Non-root user, log directory, layered content copied in
# most-stable-first order to maximise Docker layer cache.
#
# ARM note: eclipse-temurin:17-jre-jammy is a multi-arch image.
# The --platform flag ensures the ARM64 variant is pulled.
# On Graviton (aarch64) this runs natively with no emulation.
# ============================================================
FROM --platform=linux/arm64 eclipse-temurin:17-jre-jammy AS production
 
# curl is needed for the Docker healthcheck
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
 
WORKDIR /app
 
# Non-root system user — no login shell, no home directory
RUN groupadd --system springgroup \
    && useradd --system \
               --gid springgroup \
               --shell /bin/false \
               springuser
 
# Log directory matches the volume mount in docker-compose files
RUN mkdir -p /app/logs && chown springuser:springgroup /app/logs
 
# Copy layered JAR contents — stable layers first
# so Docker cache is only invalidated for layers that actually changed
COPY --from=builder --chown=springuser:springgroup /app/target/extracted/dependencies/ ./
COPY --from=builder --chown=springuser:springgroup /app/target/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=springuser:springgroup /app/target/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=springuser:springgroup /app/target/extracted/application/ ./
 
USER springuser
 
EXPOSE 8080
 
LABEL maintainer="paves-technologies" \
      app="project-management-service" \
      java.version="17" \
      arch="arm64"
 
# JVM flags explained:
#   UseContainerSupport      — reads Docker memory limits, not host RAM
#   MaxRAMPercentage=75.0    — heap uses 75% of container memory limit
#   ExitOnOutOfMemoryError   — container dies cleanly on OOM (Docker restarts it)
#   HeapDumpOnOutOfMemoryError — writes heap dump to /app/logs for post-mortem
#   java.security.egd        — faster SecureRandom startup (important for JWT)
#                              /dev/./urandom trick applies on ARM Linux too
#   SPRING_PROFILES_ACTIVE   — overridden per environment via env var
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-XX:+ExitOnOutOfMemoryError", \
            "-XX:+HeapDumpOnOutOfMemoryError", \
            "-XX:HeapDumpPath=/app/logs/heapdump.hprof", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "org.springframework.boot.loader.launch.JarLauncher"]
 