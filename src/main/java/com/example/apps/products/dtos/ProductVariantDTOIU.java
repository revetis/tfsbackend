package com.example.apps.products.dtos;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantDTOIU {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private java.math.BigDecimal price;

    @Min(value = 0, message = "Discount ratio must be greater than or equal to 0")
    private Long discountRatio;

    @NotNull(message = "Stocks are required")
    private List<ProductVariantStockDTOIU> stocks;

    @NotNull(message = "Color is required")
    private ProductVariantColorDTOIU color;

    @NotNull(message = "Enable is required")
    private Boolean enable;

    @Builder.Default
    private List<ProductVariantImageDTOIU> images = new java.util.ArrayList<>();

    private Long productId;
}
