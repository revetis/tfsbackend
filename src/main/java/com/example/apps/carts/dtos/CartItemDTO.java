package com.example.apps.carts.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
    private Long id;
    private Long productVariantId;
    private Integer quantity;
    private com.example.apps.products.enums.ProductSize size;
    private String productName;
    private String variantName;
    private java.math.BigDecimal price;
    private String imageUrl;
    private Integer stockQuantity;
}
