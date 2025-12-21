package com.example.apps.shipments.dtos;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class GeliverItemResponse {
    private String id;
    private OffsetDateTime createdAt;
    private String title;
    private int quantity;
    private String totalPrice;
    private String unitPrice;
    private String sku;
}