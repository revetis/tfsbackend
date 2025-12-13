package com.example.apps.products.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductMaterialDTOIU {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    private String description;
    private Boolean isActive;
    @NotNull(message = "Product ID cannot be null")
    private Long productId;
}
