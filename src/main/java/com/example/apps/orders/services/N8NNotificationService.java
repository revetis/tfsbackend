package com.example.apps.orders.services;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.apps.orders.entities.Order;
import com.example.apps.orders.entities.Shipment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class N8NNotificationService {

    private final RestTemplate restTemplate;

    @Value("${n8n.webhook.base-url}")
    private String n8nBaseUrl;

    @Value("${n8n.webhook.order-confirmation}")
    private String orderConfirmationPath;

    @Value("${n8n.webhook.payment-success}")
    private String paymentSuccessPath;

    @Value("${n8n.webhook.shipment-created}")
    private String shipmentCreatedPath;

    @Value("${n8n.webhook.shipment-delivered}")
    private String shipmentDeliveredPath;

    @Value("${n8n.webhook.order-cancelled}")
    private String orderCancelledPath;

    @Value("${n8n.webhook.refund-processed}")
    private String refundProcessedPath;

    @Async
    public void sendOrderConfirmation(Order order) {
        Map<String, Object> payload = Map.of(
                "email", order.getUser().getEmail(),
                "name", order.getUser().getUsername(),
                "orderNumber", order.getOrderNumber(),
                "totalAmount", order.getTotalAmount(),
                "currency", "TRY",
                "items", order.getItems().stream()
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

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(url, request, String.class);

            log.info("N8N webhook sent: {}", path);
        } catch (Exception e) {
            log.error("Failed to send N8N webhook: {}", path, e);
        }
    }
}
