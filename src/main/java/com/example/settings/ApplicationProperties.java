package com.example.settings;

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

}
