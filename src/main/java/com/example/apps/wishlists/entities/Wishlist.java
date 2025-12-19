package com.example.apps.wishlists.entities;

import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wishlists", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "product_id" })
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "product_id", nullable = false)
    private Long productId;
}
