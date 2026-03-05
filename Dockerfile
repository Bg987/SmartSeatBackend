# Stage 1: Build the JAR using Maven with OpenJDK 21
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR using Eclipse Temurin 21 (Modern & Stable)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copies the generated jar from the build stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]