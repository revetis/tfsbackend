package com.example.apps.shipments.dtos;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class GeliverShipmentDataResponse {
    private String id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private boolean test;
    private String totalAmount;
    private String currency;
    private String desi;
    private String weight;
    private String massUnit;
    private String distanceUnit;

    private GeliverTrackingStatusResponse trackingStatus;
    private GeliverAddressResponse senderAddress;
    private GeliverAddressResponse recipientAddress;
    private GeliverOffersResponse offers;
    private List<GeliverItemResponse> items;

    private String statusCode;
    private String labelURL;
    private String barcode;
    private GeliverShipmentDataResponse shipment; // Nested shipment in some responses
    private boolean hasError;
    private int organizationShipmentID;
}