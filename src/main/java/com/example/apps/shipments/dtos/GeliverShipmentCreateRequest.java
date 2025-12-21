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

    private boolean test;

    @NotBlank(message = "Sender Address ID is required")
    private String senderAddressID;

    @NotBlank(message = "Return Address ID is required")
    private String returnAddressID;

    @NotBlank(message = "Length is required")
    private String length;

    @NotBlank(message = "Height is required")
    private String height;

    @NotBlank(message = "Width is required")
    private String width;

    @NotBlank(message = "Distance unit is required (e.g., cm)")
    private String distanceUnit;

    @NotBlank(message = "Weight is required")
    private String weight;

    @NotBlank(message = "Mass unit is required (e.g., kg)")
    private String massUnit;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<GeliverItemRequest> items;

    @NotNull(message = "Recipient address details are required")
    @Valid
    private GeliverRecipientAddressRequest recipientAddress;

    private boolean productPaymentOnDelivery;

    @NotNull(message = "Order details are required")
    @Valid
    private GeliverOrderRequest order;
}