package com.example.apps.products.dtos;

import java.math.BigDecimal;
import com.example.apps.products.enums.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantDTO {
    private Long id;
    private BigDecimal variantPrice;
    private Integer stock;
    private Size size;
    private String sku;
    private Long productId;
    private Long colorId;
    private Boolean isActive;
}
