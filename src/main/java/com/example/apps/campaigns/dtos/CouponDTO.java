package com.example.apps.campaigns.dtos;

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
public class CouponDTO {
    private Long id;

    @NotBlank(message = "Coupon code is required")
    private String code;

    @NotNull(message = "Discount type is required")
    private Coupon.DiscountType discountType;

    @NotNull(message = "Discount value is required")
    private BigDecimal discountValue;

    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private Integer usagePerUser;
    private Integer usageCount;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private Boolean active;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
