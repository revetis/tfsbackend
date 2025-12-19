package com.example.apps.products.entities;

import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductMaterial extends BaseEntity {

    private String name;
}
