package com.example.apps.products.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTOIU {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    private String description;
    private String slug;
    private String image;
    private Boolean isActive;
}
