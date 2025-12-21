package com.example.apps.payments.dtos;

import lombok.Data;
import java.util.List;

@Data
public class OrderReturnRequestDTO {
    private Long orderId;
    private List<Long> orderItemIds;
    private String returnReason;
}