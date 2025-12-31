package com.example.tfs;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.crypto.SecretKey;

@ConfigurationProperties(prefix = "tfs")
@Data
@NoArgsConstructor
public class ApplicationProperties {

    private String STATIC_PATH;

    private String SECRET_KEY;

    private String NAME;

    private String URL;

    private String FRONTEND_URL;

    private String IYZICO_API_KEY;
    private String IYZICO_SECRET_KEY;
    private String IYZICO_BASE_URL;

    // Debug and Test Mode Flags
    private Boolean DEBUG_MODE = false;
    private Boolean TEST_MODE = false;

    public SecretKey getJwtSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }
}
