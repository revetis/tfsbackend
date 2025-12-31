package com.example.apps.products.dtos;

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
public class ProductDTOIU {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    private String description;

    @NotNull(message = "Material ID is required")
    private Long materialId;
    @NotNull(message = "Sub category ID is required")
    private Long subCategoryId;
    @NotNull(message = "Tax ratio is required")
    private Double taxRatio;

    private String gender;
    private String sizeChart;
    private String brand;
    private String careInstructions;
    private String origin;
    private String quality;
    private String style;
    private String season;

    private Boolean enable;

}
