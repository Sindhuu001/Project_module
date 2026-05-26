# ── Build Stage ──────────────────────────────────────────
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first (dependency caching)
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build application
RUN mvn clean package -DskipTests


# ── Runtime Stage ─────────────────────────────────────────
FROM eclipse-temurin:21-jdk-jammy

# Install non-root user
RUN useradd -m appuser

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder --chown=appuser:appuser /app/target/*.jar app.jar

USER appuser

# Expose application port
EXPOSE 8080

# Run Spring Boot app
CMD ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]