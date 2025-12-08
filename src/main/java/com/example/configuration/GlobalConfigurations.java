package com.example.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tfs")
public class GlobalConfigurations {

    private final String secretKey;

    public GlobalConfigurations(String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new RuntimeException("TFS.SECRET-KEY AYARLANMAMIS, LUTFEN PROPERTIES'I KONTROL EDIN");
        }
        this.secretKey = secretKey;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

}