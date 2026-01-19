package com.example.apps.campaigns.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignRequestItem {
    private Long productId;
    private Long variantId; // Optional, strict product matching might need it, but mostly productId is
                            // enough for campaigns
    private Long categoryId;
    private Long mainCategoryId;
    private BigDecimal price; // Unit price
    private Integer quantity;
}
