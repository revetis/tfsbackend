package com.example.tfs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.apps.shipments.Configurations.GeliverConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
public class AppConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Efendim, OffsetDateTime hatasını bitiren o asil modül
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        // Tarihlerin [2025,12,21] şeklinde liste olarak değil, ISO-8601 string olarak
        // dönmesini sağlar
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Sizin sisteminizde bilinmeyen alanlar gelirse patlamasın diye ek koruma
        // kalkanı
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }

    public static String generateUniqueFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString() + fileExtension;

    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public RestClient geliverRestClient(GeliverConfiguration props) {
        return RestClient.builder()
                .baseUrl(props.getApi().getBaseurl())
                .defaultHeader("Authorization", "Bearer " + props.getApi().getToken())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

}
