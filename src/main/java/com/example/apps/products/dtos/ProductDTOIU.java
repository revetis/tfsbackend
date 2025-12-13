package com.example.apps.products.dtos;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTOIU {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    private String description;
    @NotNull(message = "Main price cannot be null")
    @Positive(message = "Main price must be positive")
    private BigDecimal mainPrice;
    private Double discountRatio;
    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;
    @NotNull(message = "Brand ID cannot be null")
    private Long brandId;
    private Boolean isActive;
}
