package com.example.apps.products.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColorDTOIU {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    @NotBlank(message = "Color code cannot be blank")
    private String code;
    private String image;
    private Boolean isActive;
}
