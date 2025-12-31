package com.example.apps.notifications.services.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.apps.notifications.services.IN8NService;
import com.example.apps.notifications.utils.N8NProperties;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class N8NServiceImpl implements IN8NService {

    private final N8NProperties n8NProperties;
    private final RestTemplate restTemplate;

    public N8NServiceImpl(N8NProperties n8NProperties) {
        this.n8NProperties = n8NProperties;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void triggerWorkflow(String webhookUrl, Map<String, Object> payload) {
        log.info("N8N Config - baseUrl: {}, apiKey present: {}",
                n8NProperties.getBaseUrl(),
                n8NProperties.getApiKey() != null && !n8NProperties.getApiKey().isBlank());
        log.info("Triggering N8N workflow at URL: {}", webhookUrl);
        log.info("Payload keys: {}", payload.keySet());

        // Fire and forget - run in background thread
        CompletableFuture.runAsync(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                String apiKey = n8NProperties.getApiKey();
                if (apiKey != null && !apiKey.isBlank()) {
                    headers.set("X-Webhook-Secret", apiKey);
                }

                String finalUrl = webhookUrl;
                if (webhookUrl.startsWith("/") && n8NProperties.getBaseUrl() != null) {
                    finalUrl = n8NProperties.getBaseUrl().replaceAll("/$", "") + webhookUrl;
                }

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(finalUrl, request, String.class);

                log.info("N8N workflow triggered successfully. Status: {}, Response: {}",
                        response.getStatusCode(), response.getBody());
            } catch (Exception e) {
                log.error("Error triggering N8N workflow at {}: {}", webhookUrl, e.getMessage(), e);
            }
        });

        log.info("N8N webhook request sent (async) to: {}", webhookUrl);
    }
}
