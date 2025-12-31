package com.example.apps.shipments.dtos;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
public class ShipmentEventDTO {
    private Long id;
    private Long shipmentId;
    private String orderNumber;
    private String geliverShipmentId;
    private String trackingNumber;
    private String statusCode;
    private String trackingStatusCode;
    private String trackingSubStatusCode;
    private String statusDetails;
    private OffsetDateTime statusDate;
    private String locationName;
    private Double locationLat;
    private Double locationLng;
    private String carrier;
    private String trackingUrl;
    private String deliveryDate;
    private String failureReason;
    private String returnDetails;
    private LocalDateTime createdAt;
}

