package com.example.apps.products.dtos;

import java.time.LocalDateTime;
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
    private Long price;
    private Long discountRatio;
    private Long discountPrice;
    private ProductVariantStockDTO stock;
    private ProductVariantColorDTO color;
    private Boolean enable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductVariantImageDTO> images;
}
