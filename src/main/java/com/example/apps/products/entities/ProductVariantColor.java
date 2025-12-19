package com.example.apps.products.entities;

import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantColor extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String hexCode;

    @OneToOne
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

}
