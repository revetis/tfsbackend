package com.example.apps.shipments.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GeliverConfig {

    @Value("${geliver.api.token}")
    private String apiToken;

    @Bean
    public RestTemplate geliverRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Add interceptor for Bearer token
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("Authorization", "Bearer " + apiToken);
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
