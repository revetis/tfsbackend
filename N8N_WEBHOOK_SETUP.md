# N8N Webhook Configuration Guide

## Overview
Bu dosya, n8n workflow'larınızı kurduktan sonra backend'e webhook URL'lerini nasıl ekleyeceğinizi açıklar.

## Adımlar

### 1. N8N Workflow'larını Oluşturun
Her bir email tipi için n8n'de ayrı bir workflow oluşturun:

1. **Order Confirmation** - Sipariş onayı
2. **Payment Success** - Ödeme başarılı
3. **Shipment Created** - Kargo oluşturuldu
4. **Shipment Delivered** - Kargo teslim edildi
5. **Order Cancelled** - Sipariş iptal edildi
6. **Refund Processed** - İade işlendi

### 2. Webhook URL'lerini Alın
Her workflow için n8n'den webhook URL'ini kopyalayın.

Örnek URL formatı:
```
https://your-n8n-instance.com/webhook/order-confirmation
https://your-n8n-instance.com/webhook/payment-success
```

### 3. application.properties Dosyasını Güncelleyin
`src/main/resources/application.properties` dosyasına aşağıdaki satırları ekleyin:

```properties
# N8N Email Notification Webhooks
n8n.webhook.base-url=https://your-n8n-instance.com/webhook
n8n.webhook.order-confirmation=/order-confirmation
n8n.webhook.payment-success=/payment-success
n8n.webhook.shipment-created=/shipment-created
n8n.webhook.shipment-delivered=/shipment-delivered
n8n.webhook.order-cancelled=/order-cancelled
n8n.webhook.refund-processed=/refund-processed
```

**Not:** `base-url` kısmını n8n instance URL'iniz ile değiştirin.

### 4. Webhook Payload Formatları

#### Order Confirmation
```json
{
  "email": "user@example.com",
  "name": "John Doe",
  "orderNumber": "ORD-12345",
  "totalAmount": 150.00,
  "currency": "TRY",
  "items": [
    {
      "name": "Product Name",
      "quantity": 2,
      "price": 75.00
    }
  ]
}
```

#### Payment Success
```json
{
  "email": "user@example.com",
  "name": "John Doe",
  "orderNumber": "ORD-12345",
  "amount": 150.00,
  "transactionId": "TXN-67890"
}
```

#### Shipment Created
```json
{
  "email": "user@example.com",
  "name": "John Doe",
  "orderNumber": "ORD-12345",
  "trackingUrl": "https://tracking.example.com/12345",
  "trackingNumber": "TRK-12345",
  "carrier": "PTT",
  "labelUrl": "https://labels.example.com/label.pdf"
}
```

#### Shipment Delivered
```json
{
  "email": "user@example.com",
  "name": "John Doe",
  "orderNumber": "ORD-12345",
  "deliveryDate": "2025-12-13T18:30:00"
}
```

#### Order Cancelled
```json
{
  "email": "user@example.com",
  "name": "John Doe",
  "orderNumber": "ORD-12345",
  "reason": "User requested cancellation"
}
```

#### Refund Processed
```json
{
  "email": "user@example.com",
  "name": "John Doe",
  "orderNumber": "ORD-12345",
  "refundAmount": 150.00,
  "refundReason": "Product defect"
}
```

## N8N Workflow Örneği

### Basit Email Workflow

1. **Webhook Node** (Trigger)
   - HTTP Method: POST
   - Path: `/order-confirmation`

2. **Email Send Node**
   - From: `noreply@yourstore.com`
   - To: `{{ $json.email }}`
   - Subject: `Siparişiniz Alındı - {{ $json.orderNumber }}`
   - Email Type: HTML
   - HTML Content:
   ```html
   <h1>Merhaba {{ $json.name }}</h1>
   <p>Siparişiniz başarıyla alındı!</p>
   <p><strong>Sipariş Numarası:</strong> {{ $json.orderNumber }}</p>
   <p><strong>Toplam Tutar:</strong> {{ $json.totalAmount }} {{ $json.currency }}</p>
   <h2>Ürünler:</h2>
   <ul>
   {{#each $json.items}}
     <li>{{ this.name }} - {{ this.quantity }} adet - {{ this.price }} TL</li>
   {{/each}}
   </ul>
   ```

## Test Etme

### 1. Backend'i Başlatın
```bash
./mvnw.cmd spring-boot:run
```

### 2. Test İsteği Gönderin
```bash
curl -X POST http://localhost:8080/rest/api/public/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{...order data...}'
```

### 3. N8N Workflow'unu Kontrol Edin
- N8N dashboard'da workflow execution'ları görün
- Email gönderildiğini doğrulayın

## Sorun Giderme

### Email Gönderilmiyor
1. N8N workflow'unun aktif olduğundan emin olun
2. Webhook URL'lerinin doğru olduğunu kontrol edin
3. Backend loglarını kontrol edin: `log.info("N8N webhook sent: {}", path)`
4. N8N execution history'yi kontrol edin

### Webhook Hatası
1. N8N instance'ınızın erişilebilir olduğundan emin olun
2. CORS ayarlarını kontrol edin
3. SSL sertifikası sorunlarını kontrol edin

## Güvenlik Notları

- N8N webhook URL'lerini güvenli tutun
- Production'da HTTPS kullanın
- N8N'de authentication ekleyin (opsiyonel)
- Rate limiting uygulayın

## Geliver Webhook Konfigürasyonu

Geliver dashboard'dan webhook URL'inizi ekleyin:
```
https://yourdomain.com/rest/api/public/webhook/geliver/shipment
```

Event Type: `TRACK_UPDATED`
