package com.example.apps.shipments.dtos;

import lombok.Data;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class GeliverTransactionShipmentResponse {
    private String id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private boolean test;

    @JsonProperty("barcode")
    private String barcode;

    @JsonProperty("labelURL")
    private String labelUrl;

    @JsonProperty("trackingUrl")
    private String trackingUrl;

    @JsonProperty("trackingNumber")
    private String trackingNumber;

    @JsonProperty("responsiveLabelURL")
    private String responsiveLabelUrl;

    @JsonProperty("statusCode")
    private String statusCode; // Örn: TRACKING_CODE_CREATED
    private String providerCode;
    private String providerServiceCode;
    private int organizationShipmentID;
    private boolean hasError;
    private String lastErrorMessage;
    private String senderAddressID;
    private String recipientAddressID;
}