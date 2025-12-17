package com.example.apps.carts.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private List<CartItem> items = new ArrayList<>();
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void addItem(CartItem item) {
        // Check if item already exists
        for (CartItem existingItem : items) {
            if (existingItem.getProductId().equals(item.getProductId())) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                this.updatedAt = LocalDateTime.now();
                return;
            }
        }
        items.add(item);
        this.updatedAt = LocalDateTime.now();
    }

    public void removeItem(Long productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
        this.updatedAt = LocalDateTime.now();
    }

    public void updateItemQuantity(Long productId, Integer quantity) {
        for (CartItem item : items) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                this.updatedAt = LocalDateTime.now();
                return;
            }
        }
    }

    public void clear() {
        items.clear();
        this.updatedAt = LocalDateTime.now();
    }
}
