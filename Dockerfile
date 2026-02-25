FROM maven:3.9-eclipse-temurin-17 as builder

WORKDIR /build
COPY . .
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /build/target/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9091
CMD ["java", "-jar", "app.jar"]
