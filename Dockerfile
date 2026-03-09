# syntax=docker/dockerfile:1.7
# Stage 1: Build stage with dependency caching
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copy only the pom.xml first to resolve dependencies
COPY pom.xml .

# 2. Use a cache mount to speed up dependency downloads.
# This stays on the host and isn't included in the final image.
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

# 3. Copy source code after dependencies are cached
COPY src ./src

# 4. Build the package using the same cache mount
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# Stage 2: Run stage (Lightweight)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: Run as a non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the JAR from the build stage
# Note: Adjust the filename if your pom.xml generates a specific name
COPY --from=build /app/target/*.jar app.jar

RUN chown spring:spring app.jar
USER spring

# Best practice for container memory management
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]