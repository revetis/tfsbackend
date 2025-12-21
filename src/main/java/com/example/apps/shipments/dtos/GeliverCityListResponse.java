package com.example.apps.shipments.dtos;

import lombok.Data;
import java.util.List;

@Data
public class GeliverCityListResponse {
    private boolean result;
    private String additionalMessage;

    // Efendim, burada yukarıdaki City nesnesini liste olarak karşılıyoruz
    private List<GeliverCityResponse> data;
}