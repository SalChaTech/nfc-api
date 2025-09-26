# Google Drive Entegrasyonu Kurulumu

Bu doküman Google Drive API entegrasyonu için gerekli adımları açıklar.

## 1. Google Cloud Console Setup

### 1.1 Proje Oluşturma
1. [Google Cloud Console](https://console.cloud.google.com/)'a gidin
2. **New Project** tıklayın
3. Proje adı: `nfc-drive-api`
4. **Create** tıklayın

### 1.2 APIs & Services Etkinleştirme
1. **APIs & Services** > **Library** bölümüne gidin
2. **Google Drive API** arayın ve etkinleştirin
3. **Google+ API** arayın ve etkinleştirin

### 1.3 OAuth 2.0 Credentials Oluşturma
1. **APIs & Services** > **Credentials** bölümüne gidin
2. **Create Credentials** > **OAuth 2.0 Client IDs** seçin
3. **Application type**: Web application
4. **Name**: NFC Drive API
5. **Authorized redirect URIs**:
   - `http://localhost:8080/login/oauth2/code/google`
6. **Authorized JavaScript origins**:
   - `http://localhost:8080`
7. **Create** tıklayın
8. **Client ID** ve **Client Secret**'ı kopyalayın

### 1.4 Service Account Oluşturma (Opsiyonel)
1. **Create Credentials** > **Service Account** seçin
2. **Service account name**: `nfc-drive-service`
3. **Create and Continue** tıklayın
4. **Role**: **Editor** seçin
5. **Continue** tıklayın
6. **Done** tıklayın
7. Service account'a tıklayın
8. **Keys** sekmesine gidin
9. **Add Key** > **Create new key** seçin
10. **JSON** formatını seçin
11. **Create** tıklayın
12. JSON dosyasını indirin

## 2. Environment Variables

### 2.1 OAuth2 Credentials
```bash
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
```

### 2.2 Service Account (Opsiyonel)
```bash
export GOOGLE_SERVICE_ACCOUNT_KEY="/path/to/service-account-key.json"
export GOOGLE_DRIVE_FOLDER_ID="your-folder-id"
```

## 3. Application Properties

`application.properties` dosyasında:

```properties
# Google OAuth Configuration
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID:your-google-client-id}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET:your-google-client-secret}
spring.security.oauth2.client.registration.google.scope=openid,profile,email,https://www.googleapis.com/auth/drive.file

# Google Drive Service Account
google.drive.service-account-key=${GOOGLE_SERVICE_ACCOUNT_KEY:path/to/service-account-key.json}
google.drive.folder-id=${GOOGLE_DRIVE_FOLDER_ID:root}
```

## 4. Test Etme

### 4.1 Backend Başlatma
```bash
cd /home/akis-api/Desktop/salchaTech/nfc-api
mvn spring-boot:run
```

### 4.2 Frontend Başlatma
```bash
cd /home/akis-api/Desktop/salchaTech/nfc-ui
npm run dev
```

### 4.3 Test Adımları
1. `http://localhost:5173` - Login olun
2. `http://localhost:5173/upload` - Dosya yükleyin
3. Google Drive'da dosyanızı kontrol edin

## 5. Sorun Giderme

### 5.1 CORS Hatası
- Backend'de CORS ayarlarını kontrol edin
- Frontend origin'lerini ekleyin

### 5.2 OAuth2 Hatası
- Client ID ve Secret'ı kontrol edin
- Redirect URI'yi kontrol edin

### 5.3 Google Drive API Hatası
- Service account key dosyasını kontrol edin
- API'lerin etkinleştirildiğini kontrol edin

## 6. Güvenlik

- Service account key dosyasını güvenli tutun
- Production'da environment variables kullanın
- HTTPS kullanın
- CORS ayarlarını sınırlayın

## 7. Dosya Yönetimi

- Dosyalar Google Drive'ın root klasörüne yüklenir
- Klasör ID'si değiştirilebilir
- Dosya izinleri otomatik olarak ayarlanır
