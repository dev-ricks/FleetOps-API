
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/fleetops-api.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
