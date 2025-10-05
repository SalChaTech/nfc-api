# NFC API Docker Setup

Bu proje Docker ile çalıştırılmak üzere yapılandırılmıştır.

## Gereksinimler

- Docker
- Docker Compose

## Hızlı Başlangıç

### 1. Projeyi Docker ile Çalıştırma

```bash
# Tüm servisleri başlat (PostgreSQL + NFC API)
docker-compose up -d

# Logları takip et
docker-compose logs -f

# Sadece API'yi build et ve çalıştır
docker-compose up --build nfc-api
```

### 2. Servisleri Durdurma

```bash
# Tüm servisleri durdur
docker-compose down

# Veritabanı verilerini de sil
docker-compose down -v
```

### 3. Sadece Veritabanını Çalıştırma

```bash
# Sadece PostgreSQL'i çalıştır
docker-compose up -d postgres
```

## Servisler

- **nfc-api**: Spring Boot uygulaması (Port: 8080)
- **postgres**: PostgreSQL veritabanı (Port: 5432)

## Environment Variables

Environment değişkenlerini ayarlamak için `env.example` dosyasını `.env` olarak kopyalayın:

```bash
cp env.example .env
```

Sonra `.env` dosyasındaki değerleri güncelleyin.

## Veritabanı

- **Host**: localhost
- **Port**: 5432
- **Database**: salchatech
- **Username**: salih
- **Password**: SCokr1913.1914.

## API Endpoints

- **Base URL**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health

## Troubleshooting

### Port Çakışması
Eğer 8080 veya 5432 portları kullanımda ise, `docker-compose.yml` dosyasındaki port mapping'leri değiştirin.

### Veritabanı Bağlantı Sorunu
```bash
# PostgreSQL container'ının durumunu kontrol et
docker-compose ps postgres

# PostgreSQL loglarını kontrol et
docker-compose logs postgres
```

### Uygulama Başlamıyor
```bash
# Uygulama loglarını kontrol et
docker-compose logs nfc-api

# Container'ı yeniden başlat
docker-compose restart nfc-api
```

## Development

### Kod Değişiklikleri Sonrası
```bash
# Uygulamayı yeniden build et ve çalıştır
docker-compose up --build nfc-api
```

### Veritabanına Bağlanma
```bash
# PostgreSQL container'ına bağlan
docker-compose exec postgres psql -U salih -d salchatech
```

## Production

Production ortamı için:

1. Environment variables'ları güvenli bir şekilde ayarlayın
2. SSL sertifikalarını ekleyin
3. Reverse proxy (nginx) kullanın
4. Database backup stratejisi oluşturun
