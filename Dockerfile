# Etapa 1: Compilación
FROM maven:3.9.6-eclipse-temurin-22 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM openjdk:22-rc-oracle
VOLUME /tmp
EXPOSE 8080
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]
