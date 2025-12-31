package com.example.apps.orders.dtos;

import lombok.Data;

@Data
public class ReturnItemRequestDTO {
    private Long orderItemId;
    private int quantity;
}
