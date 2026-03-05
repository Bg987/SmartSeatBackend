# Stage 1: Build the JAR using Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR using Eclipse Temurin (The fix for your error)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Note: Render usually puts the jar in /app/target/
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]