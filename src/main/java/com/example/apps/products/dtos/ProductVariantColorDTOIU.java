package com.example.apps.products.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantColorDTOIU {
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Hex code is required")
    private String hexCode;
}
