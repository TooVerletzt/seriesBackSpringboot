FROM openjdk:22-rc-oracle

LABEL maintainer="abrahamcast@live.com.mx"

VOLUME /tmp

EXPOSE 8080

# Asegúrate de que el JAR está en la carpeta target
ARG JAR_FILE=target/SpringBootSecurityPostgresqlApplication-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]
