# ---- Build stage ----
FROM gradle:8.12-jdk21 AS build
WORKDIR /app

# Copy Gradle wrapper first (ensure JAR is present)
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle/gradle-daemon-jvm.properties gradle/gradle-daemon-jvm.properties

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached unless build files change)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src/ src/

# Build the JAR (skip tests for faster CI builds)
RUN ./gradlew bootJar --no-daemon -x test

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Create directory for external config
RUN mkdir -p /app/config && chown -R appuser:appgroup /app

USER appuser

# Expose the application port
EXPOSE 8080

# Spring Boot reads application.properties from classpath,
# but external config in /app/config can override it
ENTRYPOINT ["java", \
    "-jar", "app.jar", \
    "--spring.config.additional-location=file:/app/config/", \
    "--server.address=0.0.0.0"]
