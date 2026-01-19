package com.example.apps.orders.entities;

import java.math.BigDecimal;

import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseEntity {

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Column(name = "product_id", nullable = true)
    private Long productId;

    @Column(name = "category_id", nullable = true)
    private Long categoryId;

    @Column(name = "product_variant_name", nullable = false)
    private String productVariantName;

    @Column(name = "product_variant_mainategory", nullable = true)
    private String MainCategory;

    @Column(name = "product_variant_subcategory", nullable = true)
    private String SubCategory;

    @Column(name = "item_type", nullable = true)
    private String itemType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "paid_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal paidPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "return_status", nullable = false)
    private Boolean returnStatus = false;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "size")
    private com.example.apps.products.enums.ProductSize size;

    @Column(name = "color")
    private String color;

    @Column(name = "gender")
    private String gender;

    @Column(name = "sku", nullable = true)
    private String sku;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "applied_discount_type")
    private com.example.apps.orders.enums.AppliedDiscountType appliedDiscountType = com.example.apps.orders.enums.AppliedDiscountType.NONE;

    // ============= Tax Fields (Fatura için) =============
    @Column(name = "tax_ratio")
    private Double taxRatio;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "unit_price_without_tax", precision = 10, scale = 2)
    private BigDecimal unitPriceWithoutTax;

    @Column(name = "unit_price_with_tax", precision = 10, scale = 2)
    private BigDecimal unitPriceWithTax;

}
