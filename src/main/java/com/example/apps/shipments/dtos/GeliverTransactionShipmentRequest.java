package com.example.apps.shipments.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class GeliverTransactionShipmentRequest {

    private Boolean test = true;

    @NotNull(message = "Recipient address is required")
    @Valid
    private GeliverRecipientAddressRequest recipientAddress;

    @NotBlank(message = "Length is required")
    private String length;

    @NotBlank(message = "Height is required")
    private String height;

    @NotBlank(message = "Width is required")
    private String width;

    private String distanceUnit = "cm";

    @NotBlank(message = "Weight is required")
    private String weight;

    private String massUnit = "kg";

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<GeliverItemRequest> items;

    private Boolean productPaymentOnDelivery = false;
    private Boolean hidePackageContentOnTag = false;

    @NotNull(message = "Order details are required")
    @Valid
    private GeliverTransactionOrderRequest order;
}