package com.example.tfs;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ConfigurationProperties(prefix = "tfs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationProperties {

    private String SECRET_KEY;

    private String N8N_API_KEY;

    private String NAME;

    private String URL;

    private String N8N_BASE_URL;

    private String FRONTEND_URL;
}
