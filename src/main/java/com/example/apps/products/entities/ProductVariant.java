package com.example.apps.products.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToOne(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProductVariantStock stock;

    @OneToMany(mappedBy = "productVariant")
    private List<ProductVariantStockMovement> stockMovements;

    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @Min(0)
    @Max(100)
    private Long discountRatio = 0L;

    @Column(name = "discount_price")
    private BigDecimal discountPrice = BigDecimal.ZERO;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariantImage> images;

    @OneToOne(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProductVariantColor color;

    @Column(nullable = false)
    private Boolean enable = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // --------------------------------------------
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        calculateDiscountPrice();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateDiscountPrice();
    }

    public void calculateDiscountPrice() {
        if (price != null && discountRatio != null) {
            BigDecimal ratio = BigDecimal.valueOf(discountRatio)
                    .divide(BigDecimal.valueOf(100));
            this.discountPrice = price.subtract(price.multiply(ratio));
        }
    }

    // --------------------------------------------
}
