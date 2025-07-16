# Phase 1: Compile the project with Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY ./pom.xml /app
RUN mvn clean verify --fail-never
COPY ./src /app/src
RUN mvn package -DskipTests

# Phase 2: Run application
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar /app
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar $(ls /app/*.jar)"]
