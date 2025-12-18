package com.example.apps.products.dtos.search;

import com.example.apps.products.entities.Product;
import lombok.Getter;

@Getter
public class ProductSavedEvent {
    private final Product product;

    public ProductSavedEvent(Product product) {
        this.product = product;
    }
}