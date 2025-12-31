package com.example.apps.products.entities;

import java.util.List;

import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Column(nullable = false, name = "name")
    private String name;
    @Column(nullable = false, name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "product", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private ProductMaterial productMaterial;

    @ManyToOne
    @JoinColumn(name = "sub_category_id", nullable = false)
    private SubCategory subCategory;

    @Column(name = "gender")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.apps.products.enums.Gender gender;

    @Column(name = "size_chart")
    private String sizeChart;

    @Column(name = "brand")
    private String brand;

    @Column(name = "care_instructions")
    private String careInstructions;

    @Column(name = "origin")
    private String origin;

    @Column(name = "quality")
    private String quality;

    @Column(name = "style")
    private String style;

    @Column(name = "season")
    private String season;

    private Boolean enable;

    @Column(name = "tax_ratio", nullable = false)
    private Double taxRatio;

}
