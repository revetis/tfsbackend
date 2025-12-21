package com.example.apps.shipments.dtos;

import lombok.Data;

@Data
public class GeliverMainResponse {
    private boolean result;
    private String additionalMessage;
    private GeliverShipmentDataResponse data;
}