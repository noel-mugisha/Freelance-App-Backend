# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR_FILE=target/MicroFreelance-Backend-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
