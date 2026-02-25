FROM maven:3.9-eclipse-temurin-17 as builder

WORKDIR /build
COPY . .
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /build/target/demo-*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
