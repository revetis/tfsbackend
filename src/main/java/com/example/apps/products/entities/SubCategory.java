package com.example.apps.products.entities;

import java.util.List;

import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sub_categories")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class SubCategory extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "subCategory", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;

    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enable = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "main_category_id", nullable = false)
    private MainCategory mainCategory;

}
