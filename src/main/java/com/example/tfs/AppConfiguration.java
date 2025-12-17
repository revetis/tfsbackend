package com.example.tfs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
public class AppConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // JavaTimeModule vs
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO format
        return mapper;
    }

}
