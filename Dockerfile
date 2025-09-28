# Stage 1: Build the application with JDK 21
# Importing JDK and copying required files
#FROM maven:3.9.4-eclipse-temurin-21 AS build
# Updated to use an available Maven/OpenJDK 21 image
#WORKDIR /app
# Set working directory inside the container
#COPY . .
#RUN mvn clean package -DskipTests

# Stage 2: Create the final Docker image using OpenJDK 21
#FROM openjdk:21-jdk
#VOLUME /tmp

# Copy the JAR from the build stage
#COPY --from=build /app/target/*.jar app.jar
#ENTRYPOINT ["java","-jar","/app.jar"]
#EXPOSE 8080

# Stage 1: Build the application with JDK 21
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the final Docker image using OpenJDK 21
FROM openjdk:21-jdk
WORKDIR /app
VOLUME /tmp

COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar","--server.port=${PORT}"]