# Etapa de construcción
FROM maven:3.9.5-openjdk-17-slim AS build

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY src ./src

# Construir la aplicación
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM openjdk:17-jdk-slim

WORKDIR /app

# Instalar curl para health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Crear usuario no root para seguridad
RUN groupadd -r fitlife && useradd -r -g fitlife fitlife

# Copiar el JAR construido
COPY --from=build /app/target/*.jar app.jar

# Crear directorios necesarios
RUN mkdir -p /app/config /app/uploads /app/logs && \
    chown -R fitlife:fitlife /app

# Cambiar al usuario no root
USER fitlife

# Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]