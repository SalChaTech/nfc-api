# ===========================
# 1. STAGE: BUILD
# ===========================
FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app

# Sadece pom.xml dosyasını önce kopyala (cache layer için)
COPY pom.xml .

# Dependencies indir (cache hız kazandırır)
RUN mvn dependency:go-offline

# Şimdi tüm kaynak kodunu kopyala
COPY . .

# Maven build (testleri atla)
RUN mvn clean package -DskipTests


# ===========================
# 2. STAGE: RUNTIME
# ===========================
FROM openjdk:21-jdk-slim

WORKDIR /app

# Build aşamasından jar dosyasını al
COPY --from=build /app/target/*.jar app.jar

# Keystore varsa ekle (opsiyonel)
#COPY keystore.p12 keystore.p12

# Sağlıklı Spring Boot logları için UTF-8
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# Portu belirt (uygulama zaten 8080'de çalışıyor)
EXPOSE 8080

# Uygulamayı çalıştır
ENTRYPOINT ["java", "-jar", "app.jar"]
