package com.example.apps.orders.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.orders.dtos.IyzicoWebhookDTO;
import com.example.apps.orders.services.IyzicoWebhookService;
import com.example.settings.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rest/api/public/webhook/iyzico")
@RequiredArgsConstructor
@Slf4j
public class IyzicoWebhookController {

    private final IyzicoWebhookService webhookService;

    @PostMapping("/payment")
    public ResponseEntity<ApiTemplate<Void, String>> handlePaymentWebhook(
            @RequestHeader(value = "X-IYZ-SIGNATURE-V3", required = false) String signature,
            @RequestBody IyzicoWebhookDTO webhook, HttpServletRequest servletRequest) {

        log.info("Received iyzico webhook: type={}, paymentId={}, status={}",
                webhook.getIyziEventType(), webhook.getPaymentId(), webhook.getStatus());

        // Validate signature
        if (signature == null || !webhookService.validateSignature(signature, webhook)) {
            log.warn("Invalid webhook signature for payment: {}", webhook.getPaymentId());
            return ResponseEntity.status(401).body(ApiTemplate.apiTemplateGenerator(false, 401,
                    servletRequest.getRequestURI(), null, "Invalid signature"));
        }

        try {
            // Process webhook based on event type
            if (webhook.getIyziEventType() != null &&
                    (webhook.getIyziEventType().contains("PAYMENT") ||
                            webhook.getIyziEventType().contains("AUTH"))) {
                webhookService.processPaymentWebhook(webhook);
            } else if (webhook.getIyziEventType() != null &&
                    webhook.getIyziEventType().contains("REFUND")) {
                webhookService.processRefundWebhook(webhook);
            }

            // Return 200 OK to prevent iyzico retries
            return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                    "Webhook processed successfully"));

        } catch (Exception e) {
            log.error("Error processing webhook", e);
            // Still return 200 to prevent retries for unrecoverable errors
            return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                    "Webhook received"));
        }
    }
}
