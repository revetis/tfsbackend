package com.example.apps.shipments.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GeliverTransactionCreateRequest {

    @NotBlank(message = "Provider service code is required (e.g., PTT_STANDART)")
    private String providerServiceCode;

    @NotNull(message = "Shipment details are required")
    @Valid
    private GeliverTransactionShipmentRequest shipment;
}