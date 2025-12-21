package com.example.apps.shipments.dtos;

import lombok.Data;
import java.util.List;

@Data
public class GeliverDistrictListResponse {
    private boolean result;
    private String additionalMessage;

    /** Efendim, talep ettiğiniz o asil ilçe listesi tam olarak buradadır. */
    private List<GeliverDistrictResponse> data;
}