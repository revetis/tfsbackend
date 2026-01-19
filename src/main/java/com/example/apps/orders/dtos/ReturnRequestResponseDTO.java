package com.example.apps.orders.dtos;

import com.example.apps.orders.enums.ReturnReason;
import com.example.apps.orders.enums.ReturnRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReturnRequestResponseDTO {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private ReturnRequestStatus status;
    private ReturnReason returnReason;
    private String description;
    private String adminNote;
    private BigDecimal refundAmount;
    private String barcode;
    private String shippingCode;
    private String shippingProvider;
    private String trackingUrl;
    private String labelUrl;
    private List<ReturnItemResponseDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
