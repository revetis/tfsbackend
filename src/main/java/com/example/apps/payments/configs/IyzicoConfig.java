package com.example.apps.payments.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.iyzipay.Options;

@Configuration
public class IyzicoConfig {

    @Value("${iyzico.api.key}")
    private String apiKey;

    @Value("${iyzico.secret.key}")
    private String secretKey;

    @Value("${iyzico.base.url}")
    private String baseUrl;

    @Bean
    public Options iyzicoOptions() {
        Options options = new Options();
        options.setApiKey(apiKey);
        options.setSecretKey(secretKey);
        options.setBaseUrl(baseUrl);
        return options;
    }
}
