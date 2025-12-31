package com.example.apps.orders.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReturnItemResponseDTO {
    private Long orderItemId;
    private String productName;
    private String variantName;
    private int quantity;
}
