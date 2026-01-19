// Re-indexing trigger
package com.example.apps.campaigns.dtos;

import com.example.apps.campaigns.entities.Campaign;
import com.example.apps.campaigns.entities.Coupon;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignDTO {
    private Long id;

    @NotBlank(message = "Campaign name is required")
    private String name;

    private String description;

    @NotNull(message = "Campaign type is required")
    private Campaign.CampaignType campaignType;

    @NotNull(message = "Discount type is required")
    private Coupon.DiscountType discountType;

    @NotNull(message = "Discount value is required")
    private BigDecimal discountValue;

    private BigDecimal minOrderAmount;
    private Integer minQuantity; // New field for Buy X Get Y
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private Boolean active;
    private Integer priority;
    private String categoryIds;
    private String productIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
