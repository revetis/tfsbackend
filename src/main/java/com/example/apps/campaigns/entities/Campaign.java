package com.example.apps.campaigns.entities;

import com.example.tfs.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_type", nullable = false)
    private CampaignType campaignType;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private Coupon.DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "min_order_amount")
    private BigDecimal minOrderAmount;

    @Column(name = "max_discount_amount")
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_quantity")
    private Integer minQuantity;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "priority")
    private Integer priority = 0;

    @Column(name = "category_ids", length = 500)
    private String categoryIds; // Comma separated category IDs

    @Column(name = "product_ids", length = 1000)
    private String productIds; // Comma separated product IDs

    public enum CampaignType {
        PRODUCT_DISCOUNT, // Ürün indirimi
        CATEGORY_DISCOUNT, // Kategori indirimi
        CART_DISCOUNT, // Sepet indirimi
        BUY_X_GET_Y // X al Y öde
    }
}
