package com.example.apps.products.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubCategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean enable;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private MainCategoryDTO mainCategory;
}
