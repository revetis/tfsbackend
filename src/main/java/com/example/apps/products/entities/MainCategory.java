package com.example.apps.products.entities;

import java.util.List;

import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MainCategory extends BaseEntity {

    @Column(nullable = false)
    private String name;
    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enable = true;

    @OneToMany(mappedBy = "mainCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubCategory> subCategories;

}
