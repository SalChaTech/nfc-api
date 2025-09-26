# NFC API - Spring Boot Backend

Bu proje, NFC işlemlerini yöneten ve Google OAuth ile kimlik doğrulama sağlayan Spring Boot tabanlı bir REST API'dir.

## Özellikler

- 🔐 Google OAuth 2.0 entegrasyonu
- 🔑 JWT token tabanlı kimlik doğrulama
- 📱 NFC okuma/yazma API'leri
- 🛡️ Spring Security ile güvenlik
- 🌐 CORS desteği
- 📊 RESTful API tasarımı

## Teknolojiler

- Spring Boot 3.5.6
- Spring Security
- Spring OAuth2 Client
- JWT (JSON Web Tokens)
- Google API Client
- Maven
- Java 21

## Kurulum

### Gereksinimler

- Java 21
- Maven 3.6+
- Google Cloud Console hesabı

### Adımlar

1. Projeyi klonlayın:
```bash
git clone <repository-url>
cd nfc-api
```

2. Google OAuth konfigürasyonu:
   - [Google Cloud Console](https://console.cloud.google.com/)'a gidin
   - OAuth 2.0 Client ID oluşturun
   - Authorized redirect URIs: `http://localhost:8080/api/auth/google`

3. Environment variables ayarlayın:
```bash
export GOOGLE_CLIENT_ID=your-google-client-id
export GOOGLE_CLIENT_SECRET=your-google-client-secret
export JWT_SECRET=your-jwt-secret-key
```

4. Uygulamayı çalıştırın:
```bash
mvn spring-boot:run
```

## API Endpoints

### Authentication

#### Google OAuth Login
```http
POST /api/auth/google
Content-Type: application/json

{
  "credential": "google-jwt-token"
}
```

**Response:**
```json
{
  "access_token": "jwt-token",
  "token_type": "Bearer",
  "expires_in": 3600,
  "user": {
    "id": "user-id",
    "name": "User Name",
    "email": "user@example.com",
    "picture": "https://..."
  }
}
```

#### Get Current User
```http
GET /api/auth/me
Authorization: Bearer <jwt-token>
```

#### Logout
```http
POST /api/auth/logout
Authorization: Bearer <jwt-token>
```

### NFC Operations

#### Get NFC Status
```http
GET /api/nfc/status
Authorization: Bearer <jwt-token>
```

#### Read NFC
```http
POST /api/nfc/read
Authorization: Bearer <jwt-token>
```

#### Write NFC
```http
POST /api/nfc/write
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "data": "data-to-write"
}
```

#### Get NFC Devices
```http
GET /api/nfc/devices
Authorization: Bearer <jwt-token>
```

## Konfigürasyon

### application.properties

```properties
# Google OAuth
google.client.id=${GOOGLE_CLIENT_ID:your-google-client-id}
google.client.secret=${GOOGLE_CLIENT_SECRET:your-google-client-secret}

# JWT
jwt.secret=${JWT_SECRET:nfc-api-secret-key-2024-very-long-and-secure}
jwt.expiration=3600000

# Server
server.port=8080

# CORS
cors.allowed.origins=http://localhost:3000,http://localhost:5173,http://localhost:8080
```

## Güvenlik

### CORS Konfigürasyonu
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    return source;
}
```

### JWT Token
- HMAC SHA256 algoritması kullanılır
- Token süresi: 1 saat (3600000 ms)
- Secret key minimum 32 karakter olmalı

## Proje Yapısı

```
src/main/java/com/salcatech/nfc_api/
├── controller/
│   ├── AuthController.java      # Authentication endpoints
│   └── NfcController.java       # NFC operation endpoints
├── service/
│   └── GoogleOAuthService.java  # Google OAuth service
├── helper/
│   └── JwtUtil.java             # JWT utility
├── model/
│   └── User.java                # User model
├── config/
│   └── SecurityConfig.java      # Security configuration
└── NfcApiApplication.java       # Main application class
```

## Google OAuth Kurulumu

1. [Google Cloud Console](https://console.cloud.google.com/)'a gidin
2. Yeni proje oluşturun
3. "APIs & Services" > "Credentials" bölümüne gidin
4. "Create Credentials" > "OAuth 2.0 Client IDs"
5. Application type: "Web application"
6. Authorized JavaScript origins: `http://localhost:5173`
7. Authorized redirect URIs: `http://localhost:8080/api/auth/google`

## Test

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Manual Testing

1. Backend'i başlatın: `mvn spring-boot:run`
2. Frontend'i başlatın: `npm run dev`
3. `http://localhost:5173` adresine gidin
4. Google OAuth ile giriş yapın
5. NFC işlemlerini test edin

## Sorun Giderme

### CORS Hatası
- `SecurityConfig.java`'da CORS konfigürasyonunu kontrol edin
- Frontend URL'inin allowed origins listesinde olduğundan emin olun

### Google OAuth Hatası
- Client ID ve Secret'ın doğru olduğundan emin olun
- Redirect URI'nin doğru olduğundan emin olun
- Google Cloud Console'da OAuth consent screen'in yapılandırıldığından emin olun

### JWT Token Hatası
- JWT secret'ın yeterince uzun olduğundan emin olun (minimum 32 karakter)
- Token'ın expire olmadığından emin olun
- Authorization header'ın doğru formatda olduğundan emin olun

## Deployment

### Production Build
```bash
mvn clean package
java -jar target/nfc-api-0.0.1-SNAPSHOT.jar
```

### Docker
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/nfc-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Lisans

Bu proje MIT lisansı altında lisanslanmıştır.
