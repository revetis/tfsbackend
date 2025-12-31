package com.example.apps.shipments.dtos;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class GeliverWebhookData {
    private String id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String organizationID;
    private String type;
    private String url;
    private String headerName;
    private String headerValue;
    private boolean isActive;

    // Shipment details (these come in the webhook data field for TRACK_UPDATED)
    private String orderNumber;
    private String trackingNumber;
    private String carrier;
    private String trackingUrl;
    private String labelUrl;
    private String deliveryDate;
    private String failureReason;
    private String returnDetails;

    private GeliverTrackingStatusResponse trackingStatus;
    private String statusCode;
}
