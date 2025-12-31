# Production Setup Guide

Bu dokümantasyon, uygulamanın production ortamına deploy edilmesi için gerekli adımları içerir.

## 1. Production Profile Kullanımı

Production ortamında çalıştırmak için:

```bash
java -jar app.jar --spring.profiles.active=prod
```

veya environment variable ile:

```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar
```

## 2. Environment Variables

Production ortamında aşağıdaki environment variable'ları ayarlayın:

### Database
- `SPRING_DATASOURCE_URL` - PostgreSQL connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

### Application Properties
- `TFS_SECRET_KEY` - JWT secret key (Base64 encoded, minimum 256 bits)
- `TFS_FRONTEND_URL` - Frontend URL (örn: https://admin.yourdomain.com)
- `TFS_DEBUG_MODE` - Debug mode (true/false, default: false)
- `TFS_TEST_MODE` - Test mode (true/false, default: false)

### Payment Gateway (iyzico)
- `IYZICO_API_KEY` - iyzico API key
- `IYZICO_SECRET_KEY` - iyzico secret key
- `IYZICO_BASE_URL` - iyzico base URL (production: https://api.iyzipay.com)

### Cargo API (Geliver)
- `GELIVER_API_TOKEN` - Geliver API token
- `GELIVER_TEST_MODE` - Test mode (true/false)
- `GELIVER_WEBHOOK_URL` - Webhook URL
- `GELIVER_SENDER_ADDRESS_ID` - Sender address ID
- `GELIVER_RETURN_ADDRESS_ID` - Return address ID

### N8N Webhooks
- `TFS_N8N_API_KEY` - N8N API key
- `TFS_N8N_BASE_URL` - N8N base URL

### Error Tracking (GlitchTip/Sentry)
- `SENTRY_DSN` - GlitchTip DSN (örn: https://xxx@app.glitchtip.com/xxx)
- `SENTRY_ENABLED` - Enable/disable Sentry (true/false, default: true)
- `SENTRY_ENVIRONMENT` - Environment name (development/production/staging)
- `SENTRY_RELEASE` - Release version (örn: 1.0.0, v2.1.3)

## 3. Security Headers

Uygulama aşağıdaki security header'ları otomatik olarak ekler:

- `X-Content-Type-Options: nosniff` - MIME type sniffing'i engeller
- `X-Frame-Options: DENY` - Clickjacking saldırılarını engeller (debug mode'da disable)
- `X-XSS-Protection: 1; mode=block` - XSS saldırılarını engeller
- `Referrer-Policy: strict-origin-when-cross-origin` - Referrer bilgisini kontrol eder
- `Strict-Transport-Security` - HTTPS zorunluluğu (debug mode'da disable)

## 4. Monitoring ve Metrics

### Prometheus Metrics

Uygulama aşağıdaki custom metrics'leri sağlar:

- `tfs.orders.created` - Oluşturulan sipariş sayısı
- `tfs.orders.completed` - Tamamlanan sipariş sayısı
- `tfs.orders.cancelled` - İptal edilen sipariş sayısı
- `tfs.orders.active` - Aktif sipariş sayısı
- `tfs.orders.total` - Toplam sipariş sayısı
- `tfs.orders.processing.time` - Sipariş işleme süresi
- `tfs.users.registered` - Kayıt olan kullanıcı sayısı
- `tfs.users.active` - Aktif kullanıcı sayısı

### Actuator Endpoints

Production'da aşağıdaki endpoint'ler erişilebilir:

- `/actuator/health` - Health check
- `/actuator/metrics` - Tüm metrics
- `/actuator/prometheus` - Prometheus formatında metrics

**ÖNEMLİ:** Production'da bu endpoint'leri sadece internal network'ten erişilebilir yapın!

## 5. Database Configuration

Production'da:

- `spring.jpa.hibernate.ddl-auto=validate` - Schema değişikliklerini validate eder
- `spring.jpa.show-sql=false` - SQL loglarını kapatır
- Database migration için Flyway veya Liquibase kullanın

## 6. Logging

Production'da:

- Log seviyesi: `INFO`
- Application logs: `INFO`
- Security logs: `WARN`
- Hibernate logs: `WARN`
- Log dosyaları: `logs/app.log` (rolling, 30 gün saklanır)

## 7. Debug ve Test Mode

### Debug Mode (`tfs.debug-mode=true`)

Debug mode açıkken:
- Security debug logging aktif
- Frame options disable (Swagger UI için)
- Content-Type-Options disable
- HSTS disable

### Test Mode (`tfs.test-mode=true`)

Test mode açıkken:
- Payment gateway sandbox kullanılır
- Cargo API test mode aktif

**ÖNEMLİ:** Production'da her ikisini de `false` yapın!

## 8. Docker Deployment

```bash
docker build -t tfs-backend .
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/tfs \
  -e TFS_SECRET_KEY=your-secret-key \
  -e TFS_FRONTEND_URL=https://admin.yourdomain.com \
  tfs-backend
```

## 9. Health Checks

Health check endpoint'i:

```bash
curl http://localhost:8080/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" },
    "elasticsearch": { "status": "UP" }
  }
}
```

## 10. Troubleshooting

### Uygulama başlamıyor
- Environment variable'ları kontrol edin
- Database connection'ı kontrol edin
- Port 8080'in kullanılabilir olduğundan emin olun

### Metrics görünmüyor
- `management.prometheus.metrics.export.enabled=true` olduğundan emin olun
- `/actuator/prometheus` endpoint'ine erişebildiğinizden emin olun

### Security headers görünmüyor
- Debug mode'un kapalı olduğundan emin olun
- Response header'larını browser developer tools ile kontrol edin

## 11. Error Tracking (GlitchTip/Sentry)

Uygulama tüm exception'ları otomatik olarak GlitchTip'e gönderir:

- Tüm exception'lar `GlobalExceptionHandler` tarafından yakalanır
- Exception detayları, path, HTTP status code gibi context bilgileri eklenir
- Environment tag'i otomatik olarak eklenir

### Configuration

`application.properties` veya environment variable ile:

```properties
sentry.dsn=https://a366f5166af4451ab842144355fee7d8@app.glitchtip.com/14256
sentry.enabled=true
sentry.environment=production
```

### Manual Error Reporting

Kod içinde manuel olarak error göndermek için:

```java
import io.sentry.Sentry;

try {
    // risky code
} catch (Exception e) {
    Sentry.capture(e);
    // handle error
}
```

### Context Information

Sentry'ye otomatik olarak eklenen bilgiler:
- Exception type
- HTTP status code
- Request path
- Error message
- Environment (development/production)
- Application name

## 12. Checklist

Production'a geçmeden önce:

- [ ] Environment variable'lar ayarlandı
- [ ] Production profile aktif (`spring.profiles.active=prod`)
- [ ] Debug mode kapalı (`tfs.debug-mode=false`)
- [ ] Test mode kapalı (`tfs.test-mode=false`)
- [ ] Database migration yapıldı
- [ ] SSL/TLS sertifikası yapılandırıldı
- [ ] Firewall kuralları ayarlandı
- [ ] Monitoring ve alerting kuruldu
- [ ] Backup stratejisi hazırlandı
- [ ] Log rotation yapılandırıldı

