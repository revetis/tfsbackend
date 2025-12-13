package com.example.apps.notification.services.impl;

import java.util.Map;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.apps.notification.services.IN8NService;
import com.example.settings.ApplicationProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class N8NServiceImpl implements IN8NService {

    private final ApplicationProperties applicationProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void triggerWorkflow(String webhookUrl, Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String apiKey = applicationProperties.getN8N_API_KEY();
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("X-N8N-API-KEY", apiKey);
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(webhookUrl, request, String.class);
        } catch (Exception e) {
            log.error("Error triggering N8N workflow: " + e.getMessage());
        }
    }
}
