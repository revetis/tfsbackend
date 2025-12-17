package com.example.apps.shipments.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeliverWebhookDTO {
    private String shipmentId;
    private String trackingStatusCode; // PRE_TRANSIT, TRANSIT, DELIVERED, etc.
    private String trackingSubStatusCode; // package_accepted, delivered, etc.
    private String statusDetails;
    private LocalDateTime statusDate;
    private String locationName;
    private String trackingNumber;
    private String trackingUrl;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
}
