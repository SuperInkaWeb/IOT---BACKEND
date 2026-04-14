# 1. Usamos una imagen de Maven para compilar el código
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# 2. Usamos una imagen ligera de Java para ejecutar el proyecto
FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]