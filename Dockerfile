# 1. Stage: Build
FROM maven:3.9.4-eclipse-temurin-21 AS build

# Proje dosyalarını kopyala
WORKDIR /app
COPY . .

# Maven build (package)
RUN mvn clean package -DskipTests

# 2. Stage: Run
FROM openjdk:21-jdk-slim

WORKDIR /app

# Build stage’den jar dosyasını al
COPY --from=build /app/target/*.jar app.jar

COPY keystore.p12 keystore.p12

# Port aç
#EXPOSE 8080
EXPOSE 8443

# Uygulamayı çalıştır
ENTRYPOINT ["java","-jar","app.jar"]
