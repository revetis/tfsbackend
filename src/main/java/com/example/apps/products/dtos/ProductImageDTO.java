package com.example.apps.products.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageDTO {
    private Long id;
    private String name;
    private String description;
    private String slug;
    private String imagePath;
    private Long productVariantId;
    private Boolean isActive;
}
