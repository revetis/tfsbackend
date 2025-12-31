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
    private Long productId;
    private String productVariantName;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal price;
    private Long orderId;
    private com.example.apps.products.enums.ProductSize size;
    private String color;
    private String gender;

}
