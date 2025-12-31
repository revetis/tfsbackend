package com.example.apps.products.dtos;

import com.example.apps.products.enums.ProductSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantStockDTOIU {
    @NotNull(message = "Quantity is required")
    private Long quantity;
    @NotNull(message = "Size is required")
    private ProductSize size;

    @NotBlank(message = "SKU is required")
    private String sku;
}
