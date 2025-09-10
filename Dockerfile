
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/fleetops-api.jar fleetops-app.jar
ENTRYPOINT ["java", "-jar", "fleetops-app.jar"]
