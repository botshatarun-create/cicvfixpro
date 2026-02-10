# Multi-stage build for Spring Boot application
# Stage 1: Build
FROM maven:3.9.12-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:resolve

# Copy source code
COPY src ./src
COPY .mvn ./.mvn
COPY mvnw mvnw.cmd ./

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the JAR from builder
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose port 9091 (your configured port)
EXPOSE 9091

# Set environment variable for port
ENV SERVER_PORT=9091

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
