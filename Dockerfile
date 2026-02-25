FROM maven:3.9-eclipse-temurin-17 as builder

WORKDIR /build
COPY . .
RUN chmod +x ./mvnw
ENV MAVEN_OPTS="-Xmx300m"
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /build/target/demo-0.0.1-SNAPSHOT.jar app.jar
ENV PORT=10000
EXPOSE $PORT
CMD ["sh", "-c", "java -Dserver.port=${PORT:10000} -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]
