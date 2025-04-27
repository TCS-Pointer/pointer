# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pointer-back/pom.xml .
COPY pointer-back/src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY .env .env

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"] 