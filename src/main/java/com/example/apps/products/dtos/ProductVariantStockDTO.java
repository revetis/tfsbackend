package com.example.apps.products.dtos;

import com.example.apps.products.enums.ProductSize;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantStockDTO {
    private Long id;
    private Long quantity;
    private String sku;
    private ProductSize size;
}
