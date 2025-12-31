package com.example.apps.campaigns.entities;

import com.example.tfs.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "min_order_amount")
    private BigDecimal minOrderAmount;

    @Column(name = "max_discount_amount")
    private BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_per_user")
    private Integer usagePerUser = 1;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "description")
    private String description;

    public enum DiscountType {
        PERCENTAGE, // Yüzde indirim
        FIXED_AMOUNT, // Sabit tutar indirim
        FREE_SHIPPING // Ücretsiz kargo
    }
}
