package com.example.apps.carts.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {
    private Long userId;
    private List<CartItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
