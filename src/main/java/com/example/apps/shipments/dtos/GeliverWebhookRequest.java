package com.example.apps.shipments.dtos;

import lombok.Data;

@Data
public class GeliverWebhookRequest {
    private String type; // e.g., TRACK_UPDATED
    private String url;
    private GeliverWebhookData data;
}
