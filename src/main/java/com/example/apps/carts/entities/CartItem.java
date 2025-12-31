package com.example.apps.carts.entities;

import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItem extends BaseEntity {

    @Column(nullable = false)
    private Long productVariantId;

    private Integer quantity;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.apps.products.enums.ProductSize size;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

}
