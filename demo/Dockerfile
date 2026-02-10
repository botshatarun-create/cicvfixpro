FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=0 /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Dserver.port=8080"

CMD ["java", "-jar", "app.jar"]
