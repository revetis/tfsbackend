package com.example.apps.shipments.dtos;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class GeliverTransactionShipmentResponse {
    private String id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private boolean test;
    private String barcode;
    private String labelURL;
    private String responsiveLabelURL;
    private String statusCode; // Örn: TRACKING_CODE_CREATED
    private String providerCode;
    private String providerServiceCode;
    private int organizationShipmentID;
    private boolean hasError;
    private String lastErrorMessage;
    private String senderAddressID;
    private String recipientAddressID;
}