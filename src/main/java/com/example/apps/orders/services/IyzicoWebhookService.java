package com.example.apps.orders.services;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.orders.dtos.IyzicoWebhookDTO;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IyzicoWebhookService {

    private final OrderRepository orderRepository;
    private final PaymentMetricsService metricsService;

    @Value("${iyzico.secret.key}")
    private String secretKey;

    /**
     * Validates webhook signature using HMACSHA256
     * Format: secretKey + iyziEventType + paymentId + paymentConversationId +
     * status
     */
    public boolean validateSignature(String headerSignature, IyzicoWebhookDTO webhook) {
        try {
            String message = secretKey +
                    webhook.getIyziEventType() +
                    webhook.getPaymentId() +
                    webhook.getPaymentConversationId() +
                    webhook.getStatus();

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hashBytes = sha256_HMAC.doFinal(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            String calculatedSignature = sb.toString();
            boolean isValid = calculatedSignature.equals(headerSignature);

            if (!isValid) {
                log.warn("Invalid webhook signature. Expected: {}, Got: {}", calculatedSignature, headerSignature);
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error validating webhook signature", e);
            return false;
        }
    }

    @Transactional
    public void processPaymentWebhook(IyzicoWebhookDTO webhook) {
        try {
            metricsService.recordWebhookReceived(webhook.getIyziEventType());

            // Find order by conversation ID (order ID)
            Long orderId = Long.parseLong(webhook.getPaymentConversationId());
            Order order = orderRepository.findById(orderId).orElse(null);

            if (order == null) {
                log.warn("Order not found for webhook: {}", webhook.getPaymentConversationId());
                metricsService.recordWebhookProcessed(webhook.getIyziEventType(), false);
                return;
            }

            // Check idempotency - if already processed, skip
            if (order.getPaymentTransactionId() != null &&
                    order.getPaymentTransactionId().equals(webhook.getPaymentId())) {
                log.info("Webhook already processed for order: {}", orderId);
                metricsService.recordWebhookProcessed(webhook.getIyziEventType(), true);
                return;
            }

            // Update order based on payment status
            if ("SUCCESS".equals(webhook.getStatus())) {
                order.setPaymentStatus(Order.PaymentStatus.PAID);
                order.setStatus(Order.OrderStatus.CONFIRMED);
                order.setPaymentTransactionId(webhook.getPaymentId());
                log.info("Payment confirmed via webhook for order: {}", orderId);
            } else if ("FAILURE".equals(webhook.getStatus())) {
                order.setPaymentStatus(Order.PaymentStatus.FAILED);
                order.setStatus(Order.OrderStatus.CANCELLED);
                log.warn("Payment failed via webhook for order: {}", orderId);
            }

            orderRepository.save(order);
            metricsService.recordWebhookProcessed(webhook.getIyziEventType(), true);

        } catch (Exception e) {
            log.error("Error processing payment webhook", e);
            metricsService.recordWebhookProcessed(webhook.getIyziEventType(), false);
            throw e;
        }
    }

    @Transactional
    public void processRefundWebhook(IyzicoWebhookDTO webhook) {
        try {
            metricsService.recordWebhookReceived("REFUND_" + webhook.getIyziEventType());

            // Find order by payment ID
            Order order = orderRepository.findByPaymentTransactionId(webhook.getPaymentId())
                    .orElse(null);

            if (order == null) {
                log.warn("Order not found for refund webhook: {}", webhook.getPaymentId());
                metricsService.recordWebhookProcessed("REFUND", false);
                return;
            }

            // Update refund status
            if ("SUCCESS".equals(webhook.getStatus())) {
                order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
                order.setRefundedAt(LocalDateTime.now());
                log.info("Refund confirmed via webhook for order: {}", order.getId());
            }

            orderRepository.save(order);
            metricsService.recordWebhookProcessed("REFUND", true);

        } catch (Exception e) {
            log.error("Error processing refund webhook", e);
            metricsService.recordWebhookProcessed("REFUND", false);
            throw e;
        }
    }
}
