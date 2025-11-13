# syntax=docker/dockerfile:1

########## Stage 1: build ##########
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache de dependencias
COPY pom.xml .
RUN mvn -q -U -DskipTests \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
    dependency:go-offline || true

# Código
COPY src ./src
# Jar con nombre fijo para simplificar el COPY
RUN mvn -q clean package -DskipTests -Dproject.build.finalName=app \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.http.retryHandler.requestSentEnabled=true

########## Stage 2: runtime ##########
# JRE (más liviano que JDK) y tag válido
FROM eclipse-temurin:21-jre
WORKDIR /app

# (opcional) usuario no-root
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

COPY --from=build /app/target/app.jar /app/app.jar

# Vars de entorno
ENV SERVER_PORT=8080
ENV JAVA_OPTS=""

EXPOSE 8080

# En exec-form no se expanden env vars; usamos sh -c
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]