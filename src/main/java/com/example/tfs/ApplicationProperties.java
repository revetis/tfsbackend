package com.example.tfs;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@ConfigurationProperties(prefix = "tfs")
@Data
@NoArgsConstructor
public class ApplicationProperties {

    private String STATIC_PATH = "tfsbackend\\src\\main\\resources\\static";

    private String SECRET_KEY;

    private String NAME;

    private String URL;

    private String FRONTEND_URL;

    private String IYZICO_API_KEY;
    private String IYZICO_SECRET_KEY;
    private String IYZICO_BASE_URL;

    private String GELIVER_API_BASEURL;
    private String GELIVER_API_TOKEN;
    private String GELIVER_TEST_MODE;
    private String GELIVER_WEBHOOK_URL;

}
