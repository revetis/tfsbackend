package com.example.apps.products.entities;

import com.example.apps.products.enums.StockMovementType;
import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_movements")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductVariantStockMovement extends BaseEntity {

    private Long quantity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Enumerated(EnumType.STRING)
    private StockMovementType type;
    private Long orderId;
    private Long userId;

    @Version
    private Long version;

}
