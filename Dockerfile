# 1. Maven build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Runtime
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Key-store dosyası mount ile geliyor, COPY artık opsiyonel
EXPOSE 8443
ENV KEYSTORE_PASSWORD=123456

ENTRYPOINT ["java","-jar","/app/app.jar"]
