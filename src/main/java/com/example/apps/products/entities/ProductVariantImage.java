package com.example.apps.products.entities;

import org.hibernate.validator.constraints.URL;

import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_variant_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantImage extends BaseEntity {

    @Column(nullable = false)
    @URL
    private String url;

    @ManyToOne
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private String alt;

}
