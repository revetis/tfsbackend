package com.example.apps.shipments.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeliverShipmentCreateRequest {

    private Boolean test;

    private String senderAddressID;

    private String returnAddressID;

    @NotBlank(message = "Length is required")
    private String length;

    @NotBlank(message = "Height is required")
    private String height;

    @NotBlank(message = "Width is required")
    private String width;

    private String distanceUnit;

    @NotBlank(message = "Weight is required")
    private String weight;

    private String massUnit;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<GeliverItemRequest> items;

    @NotNull(message = "Recipient address details are required")
    @Valid
    private GeliverRecipientAddressRequest recipientAddress;

    private Boolean productPaymentOnDelivery;

    @NotNull(message = "Order details are required")
    @Valid
    private GeliverOrderRequest order;
}