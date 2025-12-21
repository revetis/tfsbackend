package com.example.apps.shipments.dtos;

import lombok.Data;

@Data
public class GeliverTransactionMainResponse {
    private boolean result;
    private String message;

    // Efendim, bu sefer data direkt bir nesne olarak geliyor
    private GeliverTransactionDataResponse data;
}