package com.example.apps.shipments.Configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@ConfigurationProperties(prefix = "geliver")
@Data
@NoArgsConstructor
public class GeliverConfiguration {

    private Api api = new Api(); // İç içe yapı için (geliver.api.*)
    private String testMode; // geliver.test.mode için
    private String webhookUrl;

    private String senderAddressId;
    private String returnAddressId;

    @Data
    public static class Api {
        private String baseurl; // geliver.api.baseurl için
        private String token; // geliver.api.token için
    }

    @Bean
    public WebClient geliverWebClient(WebClient.Builder builder, ObjectMapper objectMapper) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    // Efendim, hem Decoder hem de Encoder için text/plain desteğini perçinliyoruz
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(objectMapper,
                                    MediaType.APPLICATION_JSON,
                                    MediaType.TEXT_PLAIN, // Geliver'in nazlı formatı
                                    new MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8)));
                    configurer.defaultCodecs().jackson2JsonEncoder(
                            new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                })
                .build();

        return builder
                .baseUrl(api.getBaseurl())
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Bearer " + api.getToken())
                .build();
    }
}