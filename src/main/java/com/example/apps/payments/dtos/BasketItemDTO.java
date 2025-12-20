package com.example.apps.payments.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BasketItemDTO {
    @NotNull(message = "Product ID is missing.")
    private Long id;

    @NotBlank(message = "Product name cannot be blank.")
    private String name;

    @NotBlank(message = "Main category is required.")
    private String mainCategory;

    @NotBlank(message = "Sub category is required.")
    private String subCategory;

    @NotBlank(message = "Item type (PHYSICAL/VIRTUAL) must be specified.")
    private String itemType;

    @NotNull(message = "Item price is required.")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @NotNull(message = "Quantity is required.")
    private Integer quantity;

}