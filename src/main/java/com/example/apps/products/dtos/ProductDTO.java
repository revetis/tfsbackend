package com.example.apps.products.dtos;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String slug;
    private BigDecimal mainPrice;
    private Double discountRatio;
    private Long categoryId;
    private Long brandId;
    private Boolean isActive;
}
