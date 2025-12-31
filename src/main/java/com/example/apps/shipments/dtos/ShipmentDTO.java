package com.example.apps.shipments.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ShipmentDTO {
    private Long id;
    private String orderNumber;
    private String geliverShipmentId;
    private String trackingNumber;
    private String trackingUrl;
    private String labelUrl;
    private String status;
    private boolean testMode;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
