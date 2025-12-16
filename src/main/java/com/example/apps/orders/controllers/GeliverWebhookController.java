package com.example.apps.orders.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.orders.dtos.GeliverWebhookDTO;
import com.example.apps.orders.services.GeliverWebhookService;
import com.example.settings.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rest/api/public/webhook/geliver")
@RequiredArgsConstructor
@Slf4j
public class GeliverWebhookController {

    private final GeliverWebhookService webhookService;

    @PostMapping("/shipment")
    public ResponseEntity<ApiTemplate<Void, String>> handleShipmentUpdate(@RequestBody GeliverWebhookDTO webhook,
            HttpServletRequest servletRequest) {
        log.info("Received Geliver webhook: shipmentId={}, status={}",
                webhook.getShipmentId(), webhook.getTrackingStatusCode());

        try {
            webhookService.processShipmentUpdate(webhook);
            return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                    "Webhook processed successfully"));
        } catch (Exception e) {
            log.error("Error processing Geliver webhook", e);
            // Return 200 OK to prevent Geliver retries
            return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                    "Webhook received"));
        }
    }
}
