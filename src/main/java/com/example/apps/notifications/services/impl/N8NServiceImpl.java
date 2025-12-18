package com.example.apps.notifications.services.impl;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.apps.notifications.services.IN8NService;
import com.example.tfs.ApplicationProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class N8NServiceImpl implements IN8NService {

    private final ApplicationProperties applicationProperties;

    private final WebClient webClient;

    @Override
    public void triggerWorkflow(String webhookUrl, Map<String, Object> payload) {

        try {
            webClient.post()
                    .uri(webhookUrl)
                    .headers(h -> h.setBearerAuth(applicationProperties.getSECRET_KEY()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            resp -> resp.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("N8N error: " + body)))
                    .bodyToMono(Void.class)
                    .block();
            log.info("N8N workflow triggered successfully");
        } catch (Exception e) {
            log.error("Error triggering N8N workflow: " + e.getMessage());
        }
    }
}
