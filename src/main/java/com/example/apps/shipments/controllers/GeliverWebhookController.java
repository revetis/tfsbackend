package com.example.apps.shipments.controllers;

import com.example.apps.shipments.dtos.GeliverWebhookRequest;
import com.example.apps.shipments.services.IShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest/api/public/webhook/geliver")
@RequiredArgsConstructor
@Slf4j
public class GeliverWebhookController {

    private final IShipmentService shipmentService;

    @Value("${geliver.webhook.secret:}")
    private String webhookSecret;

    @PostMapping({ "/track", "/transaction" })
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "X-Webhook-Token", required = false) String token,
            @RequestBody GeliverWebhookRequest request) {

        log.info("Received Geliver webhook: type={}, tokenPresence={}",
                request.getType(),
                token != null ? "Present" : "Missing");

        // Security check
        if (webhookSecret != null && !webhookSecret.isBlank()) {
            if (token == null || !token.equals(webhookSecret)) {
                log.warn(
                        "Unauthorized Geliver webhook attempt. Header token mismatch. Expected start: {}, Received start: {}",
                        webhookSecret.substring(0, Math.min(webhookSecret.length(), 4)),
                        token != null ? token.substring(0, Math.min(token.length(), 4)) : "null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        }

        try {
            shipmentService.processTrackingWebhook(request);
            return ResponseEntity.status(HttpStatus.OK).body("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing Geliver webhook: {}", e.getMessage(), e);
            // Return 200 to prevent infinite retries from Geliver if it's a non-critical
            // processing error, or follow their strict requirement for 200
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Webhook received but processing failed: " + e.getMessage());
        }
    }
}
