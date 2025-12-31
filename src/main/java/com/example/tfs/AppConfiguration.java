package com.example.tfs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.scheduling.annotation.EnableAsync;

import com.example.apps.shipments.configurations.GeliverConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
@EnableAsync
public class AppConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // OffsetDateTime hatasını bitiren modül
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        // Tarihlerin [2025,12,21] şeklinde liste olarak değil, ISO-8601 string olarak
        // dönmesini sağlar
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Bilinmeyen alanlar gelirse patlamasın diye ek koruma
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
    public WebClient webClient() {
        return WebClient.builder().build();
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
