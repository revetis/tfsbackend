package com.example.apps.orders.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Long id;
    private Long productVariantId;
    private String productVariantName;
    private Integer quantity;
    private BigDecimal price;
    private Long orderId;

}
