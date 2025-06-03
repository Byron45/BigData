# Usa una imagen oficial de Maven para compilar el proyecto
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Usa una imagen ligera de Java para correr la app
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto de tu aplicación (ajusta si usas otro)
EXPOSE 8000

# Variable de entorno para el HDFS_URI (opcional, ya lo tienes en docker-compose)
ENV HDFS_URI=hdfs://namenode:8020

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]