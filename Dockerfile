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
COPY . .

# Empaquetar (single o multi-módulo)
RUN mvn -q clean package -DskipTests \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.http.retryHandler.requestSentEnabled=true

# Copiar el primer JAR "runnable" que encuentre en cualquier target/
# (excluye sources/javadoc/tests)
RUN set -eux; \
    JAR="$(find . -type f -path '*/target/*.jar' \
      ! -name '*-sources.jar' \
      ! -name '*-javadoc.jar' \
      ! -name '*-tests.jar' \
      | head -n1)"; \
    echo "Usando JAR: $JAR"; \
    cp "$JAR" /app/app.jar

########## Stage 2: runtime ##########
FROM eclipse-temurin:21-jre
WORKDIR /app

# (opcional) usuario no-root
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Traer el jar preparado en la etapa de build
COPY --from=build /app/app.jar /app/app.jar

# Vars de entorno
ENV SERVER_PORT=8080
ENV JAVA_OPTS=""

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]