package com.example.apps.products.dtos;

import java.math.BigDecimal;
import com.example.apps.products.enums.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantDTOIU {
    @NotNull(message = "Variant price cannot be null")
    @Positive(message = "Variant price must be positive")
    private BigDecimal variantPrice;

    @NotNull(message = "Stock cannot be null")
    private Integer stock;

    @NotNull(message = "Size cannot be null")
    private Size size;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @NotNull(message = "Color ID cannot be null")
    private Long colorId;

    private Boolean isActive;
}
