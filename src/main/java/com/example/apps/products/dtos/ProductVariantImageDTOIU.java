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
public class ProductVariantImageDTOIU {
    @NotBlank(message = "URL is required")
    private String url;

    @NotBlank(message = "Alt is required")
    private String alt;
}
