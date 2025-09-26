# Google OAuth Login API

Bu proje Google OAuth ile login işlemi yapmak için gerekli endpoint'leri sağlar.

## Kurulum

### 1. Google Cloud Console Ayarları

1. [Google Cloud Console](https://console.cloud.google.com/)'a gidin
2. Yeni bir proje oluşturun veya mevcut projeyi seçin
3. "APIs & Services" > "Credentials" bölümüne gidin
4. "Create Credentials" > "OAuth 2.0 Client IDs" seçin
5. Application type olarak "Web application" seçin
6. Authorized redirect URIs'ye şunu ekleyin: `http://localhost:8080/api/login/callback`

### 2. Environment Variables

Aşağıdaki environment variable'ları ayarlayın:

```bash
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
export JWT_SECRET="your-jwt-secret-key"
```

### 3. Application Properties

`application.properties` dosyasında aşağıdaki değerleri güncelleyin:

```properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID:your-google-client-id}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET:your-google-client-secret}
jwt.secret=${JWT_SECRET:mySecretKey}
```

## API Endpoints

### 1. Login URL Alma
```
GET /api/login
```

**Response:**
```json
{
  "authUrl": "https://accounts.google.com/o/oauth2/v2/auth?..."
}
```

### 2. OAuth Callback
```
POST /api/login/callback?code={authorization_code}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com",
  "message": "Login successful"
}
```

### 3. Token Doğrulama
```
POST /api/login/verify
Authorization: Bearer {token}
```

**Response:**
```json
{
  "valid": true,
  "email": "user@example.com"
}
```

## Frontend Entegrasyonu

### 1. Login İşlemi

```javascript
// 1. Login URL'ini al
const response = await fetch('/api/login');
const data = await response.json();
const authUrl = data.authUrl;

// 2. Kullanıcıyı Google'a yönlendir
window.location.href = authUrl;
```

### 2. Callback İşlemi

Google OAuth callback'inde:

```javascript
// URL'den authorization code'u al
const urlParams = new URLSearchParams(window.location.search);
const code = urlParams.get('code');

// Token al
const response = await fetch(`/api/login/callback?code=${code}`, {
  method: 'POST'
});
const data = await response.json();

if (data.token) {
  // Token'ı localStorage'a kaydet
  localStorage.setItem('token', data.token);
  // Kullanıcıyı ana sayfaya yönlendir
  window.location.href = '/dashboard';
}
```

### 3. API İstekleri

```javascript
// Token ile API istekleri
const token = localStorage.getItem('token');

const response = await fetch('/api/protected-endpoint', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

## Güvenlik

- JWT token'lar 24 saat geçerlidir
- Token'lar HMAC SHA-256 ile imzalanır
- CORS ayarları frontend domain'i için yapılandırılmıştır
- Tüm API endpoint'leri HTTPS kullanmalıdır (production'da)

## Test

Projeyi çalıştırmak için:

```bash
mvn spring-boot:run
```

API'yi test etmek için:

```bash
# Login URL'ini al
curl http://localhost:8080/api/login

# Token doğrula
curl -X POST http://localhost:8080/api/login/verify \
  -H "Authorization: Bearer YOUR_TOKEN"
```
