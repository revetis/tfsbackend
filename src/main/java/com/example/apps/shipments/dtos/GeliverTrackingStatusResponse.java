package com.example.apps.shipments.dtos;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class GeliverTrackingStatusResponse {
    private String id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String trackingStatusCode;
    private String trackingSubStatusCode;
    private String statusDetails;
    private OffsetDateTime statusDate;
    private String locationName;
    private Double locationLat;
    private Double locationLng;
    private String hash;
}