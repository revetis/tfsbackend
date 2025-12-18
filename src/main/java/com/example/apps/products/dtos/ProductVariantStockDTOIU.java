package com.example.apps.products.dtos;

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
    @NotBlank(message = "SKU is required")
    private String sku;
}
