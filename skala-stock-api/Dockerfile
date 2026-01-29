# Multi-stage build for Spring Boot application

# Stage 1: Build
FROM gradle:8.10.2-jdk21 AS builder

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .

# Download dependencies (cached if build files don't change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create data directory for H2 database (before user change)
RUN mkdir -p /app/data

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring && \
    chown -R spring:spring /app

# Copy the built JAR from builder stage
COPY --from=builder --chown=spring:spring /app/build/libs/*.jar app.jar

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 9080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:9080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Optional: JVM tuning parameters (uncomment if needed)
# ENTRYPOINT ["java", \
#   "-Xms512m", \
#   "-Xmx1024m", \
#   "-XX:+UseG1GC", \
#   "-XX:MaxGCPauseMillis=200", \
#   "-Djava.security.egd=file:/dev/./urandom", \
#   "-jar", "/app/app.jar"]
