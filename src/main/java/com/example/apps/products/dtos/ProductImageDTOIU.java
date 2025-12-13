package com.example.apps.products.dtos;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageDTOIU {
    private String name;
    private String description;
    private MultipartFile file;
    @NotNull(message = "Product Variant ID cannot be null")
    private Long productVariantId;
    private Boolean isActive;
}
