package com.example.apps.payments.dtos;

import lombok.Data;
import java.util.List;

@Data
public class OrderReturnRequestDTO {
    private Long orderId;
    private List<Long> orderItemIds;
    private String returnReason;
    private boolean restoreStock = true; // Default to true for backward compatibility logic if instantiated manually
    private java.math.BigDecimal refundAmount;
}