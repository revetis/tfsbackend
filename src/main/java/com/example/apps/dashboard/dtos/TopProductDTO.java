package com.example.apps.dashboard.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDTO {
    private Long productVariantId;
    private String productVariantName;
    private Long totalQuantity;
    private BigDecimal totalRevenue;
}
