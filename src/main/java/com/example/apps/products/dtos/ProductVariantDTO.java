package com.example.apps.products.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantDTO {
    private Long id;
    private String name;
    private java.math.BigDecimal price;
    private Long discountRatio;
    private java.math.BigDecimal discountPrice;
    private List<ProductVariantStockDTO> stocks;
    private ProductVariantColorDTO color;
    private Boolean enable;
    private Long productId;
    private String productName;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private List<ProductVariantImageDTO> images;
}
