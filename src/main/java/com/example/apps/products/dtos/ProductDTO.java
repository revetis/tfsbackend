package com.example.apps.products.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {

    private Long id;
    private String name;
    private String description;
    private Boolean enable;
    private List<ProductVariantDTO> variants;
    private ProductMaterialDTO material;
    private SubCategoryDTO subCategory;
    private Double taxRatio;
    private String gender;
    private String sizeChart;
    private String brand;
    private String careInstructions;
    private String origin;
    private String quality;
    private String style;
    private String season;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
