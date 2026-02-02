# syntax=docker/dockerfile:1.4

# Stage 1: Build with BuildKit cache mounts for maximum speed
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only pom.xml first - cached until dependencies change
COPY pom.xml ./

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B && \
    mv target/smart-attendance-system-*.jar target/app.jar

# Stage 2: Optimized runtime
FROM eclipse-temurin:17-jre-jammy

# Install curl for health checks
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/app.jar app.jar

# Create necessary directories
RUN mkdir -p uploads/faces data

# JVM performance optimizations via environment variable
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# Health check for standalone container monitoring
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Use JAVA_OPTS from docker-compose or environment
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
