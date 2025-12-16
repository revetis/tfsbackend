package com.example.apps.orders.services;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.apps.orders.entities.Order;
import com.example.apps.orders.entities.Shipment;
import com.example.settings.ApplicationProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class N8NNotificationService {

    private final RestTemplate restTemplate;

    @Value("${tfs.n8n-base-url}")
    private String n8nBaseUrl;

    @Value("${tfs.n8n.webhook.order-confirmation}")
    private String orderConfirmationPath;

    @Value("${tfs.n8n.webhook.payment-success}")
    private String paymentSuccessPath;

    @Value("${tfs.n8n.webhook.shipment-created}")
    private String shipmentCreatedPath;

    @Value("${tfs.n8n.webhook.shipment-delivered}")
    private String shipmentDeliveredPath;

    @Value("${tfs.n8n.webhook.order-cancelled}")
    private String orderCancelledPath;

    @Value("${tfs.n8n.webhook.refund-processed}")
    private String refundProcessedPath;

    @Value("${tfs.n8n.webhook.shipment-shipped}")
    private String shipmentShippedPath;

    @Value("${tfs.n8n.webhook.shipment-returned}")
    private String shipmentReturnedPath;

    @Value("${tfs.n8n.webhook.shipment-failed}")
    private String shipmentFailedPath;

    @Autowired
    ApplicationProperties applicationProperties;

    @Async
    public void sendOrderConfirmation(Order order) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", order.getUser().getEmail());
        payload.put("name", order.getUser().getUsername());
        payload.put("firstName", order.getUser().getFirstName());
        payload.put("lastName", order.getUser().getLastName());
        payload.put("phoneNumber", order.getUser().getPhoneNumber());
        payload.put("orderNumber", order.getOrderNumber());
        payload.put("orderDate", order.getCreatedAt()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        payload.put("shippingAddress", order.getShippingAddress());
        payload.put("billingAddress", order.getBillingAddress());
        payload.put("totalAmount", order.getTotalAmount());
        payload.put("currency", "TRY");
        payload.put("orderDetailUrl", applicationProperties.getFRONTEND_URL() + "orders/" + order.getOrderNumber());
        payload.put("items", order.getItems().stream()
                .map(item -> Map.of(
                        "name", item.getProduct().getName(),
                        "quantity", item.getQuantity(),
                        "price", item.getUnitPrice()))
                .collect(Collectors.toList()));

        sendWebhook(orderConfirmationPath, payload);
    }

    @Async
    public void sendPaymentSuccess(Order order, String transactionId) {
        Map<String, Object> payload = Map.of(
                "email", order.getUser().getEmail(),
                "name", order.getUser().getUsername(),
                "orderNumber", order.getOrderNumber(),
                "amount", order.getTotalAmount(),
                "transactionId", transactionId);

        sendWebhook(paymentSuccessPath, payload);
    }

    @Async
    public void sendShipmentCreated(Order order, Shipment shipment) {
        Map<String, Object> payload = Map.of(
                "email", order.getUser().getEmail(),
                "name", order.getUser().getUsername(),
                "orderNumber", order.getOrderNumber(),
                "trackingUrl", shipment.getTrackingUrl() != null ? shipment.getTrackingUrl() : "",
                "trackingNumber", shipment.getTrackingNumber() != null ? shipment.getTrackingNumber() : "",
                "carrier", shipment.getProviderCode() != null ? shipment.getProviderCode() : "",
                "labelUrl", shipment.getLabelUrl() != null ? shipment.getLabelUrl() : "");

        sendWebhook(shipmentCreatedPath, payload);
    }

    @Async
    public void sendShipmentDelivered(Order order, Shipment shipment) {
        Map<String, Object> payload = Map.of(
                "email", order.getUser().getEmail(),
                "name", order.getUser().getUsername(),
                "orderNumber", order.getOrderNumber(),
                "deliveryDate", shipment.getDeliveredAt() != null ? shipment.getDeliveredAt().toString() : "");

        sendWebhook(shipmentDeliveredPath, payload);
    }

    @Async
    public void sendOrderCancelled(Order order, String reason) {
        Map<String, Object> payload = Map.of(
                "email", order.getUser().getEmail(),
                "name", order.getUser().getUsername(),
                "orderNumber", order.getOrderNumber(),
                "reason", reason != null ? reason : "No reason provided");

        sendWebhook(orderCancelledPath, payload);
    }

    @Async
    public void sendRefundProcessed(Order order, BigDecimal refundAmount) {
        Map<String, Object> payload = Map.of(
                "email", order.getUser().getEmail(),
                "name", order.getUser().getUsername(),
                "orderNumber", order.getOrderNumber(),
                "refundAmount", refundAmount,
                "refundReason", order.getRefundReason() != null ? order.getRefundReason() : "");

        sendWebhook(refundProcessedPath, payload);
    }

    private void sendWebhook(String path, Map<String, Object> payload) {
        try {
            String url = n8nBaseUrl + path;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + applicationProperties.getSECRET_KEY());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(url, request, String.class);

            log.info("N8N webhook sent: {}", path);
        } catch (Exception e) {
            log.error("Failed to send N8N webhook: {}", path, e);
        }
    }

    @Async
    public void sendShipmentShipped(Order order, Shipment shipment) {
        Map<String, Object> payload = Map.of(
                "email", order.getUser().getEmail(),
                "name", order.getUser().getUsername(),
                "orderNumber", order.getOrderNumber(),
                "trackingUrl", shipment.getTrackingUrl() != null ? shipment.getTrackingUrl() : "",
                "trackingNumber", shipment.getTrackingNumber() != null ? shipment.getTrackingNumber() : "",
                "carrier", shipment.getProviderCode() != null ? shipment.getProviderCode() : "");

        sendWebhook(shipmentShippedPath, payload);
    }

    @Async
    public void sendShipmentReturned(Order order, Shipment shipment) {
        Map<String, Object> payload = Map.of(
                "email", order.getUser().getEmail(),
                "name", order.getUser().getUsername(),
                "orderNumber", order.getOrderNumber(),
                "trackingNumber", shipment.getTrackingNumber() != null ? shipment.getTrackingNumber() : "",
                "returnDetails", shipment.getLastErrorMessage() != null ? shipment.getLastErrorMessage() : "");

        sendWebhook(shipmentReturnedPath, payload);
    }

    @Async
    public void sendShipmentFailed(Order order, Shipment shipment) {
        Map<String, Object> payload = Map.of(
                "email", order.getUser().getEmail(),
                "name", order.getUser().getUsername(),
                "orderNumber", order.getOrderNumber(),
                "trackingNumber", shipment.getTrackingNumber() != null ? shipment.getTrackingNumber() : "",
                "failureReason", shipment.getLastErrorMessage() != null ? shipment.getLastErrorMessage() : "");

        sendWebhook(shipmentFailedPath, payload);
    }
}
