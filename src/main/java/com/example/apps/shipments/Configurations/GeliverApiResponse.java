package com.example.apps.shipments.Configurations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeliverApiResponse<T> {
    private boolean result;
    private String additionalMessage;
    private T data;
}