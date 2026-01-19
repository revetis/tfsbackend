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
public class CartItemDTO implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productVariantId;
    private Integer quantity;
    private com.example.apps.products.enums.ProductSize size;
    private String productName;
    private String variantName;
    private String colorName;
    private String hexCode;
    private java.math.BigDecimal price;
    private java.math.BigDecimal discountPrice;
    private String imageUrl;
    private Integer stockQuantity;
    private Double taxRatio;
    private String mainCategory;
    private String subCategory;
    private Long productId;
    private Long categoryId;
}
