package com.example.apps.wishlists.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WishlistProductDTO {

    private Long id;
    private Long productId;
    private Long userId;

    private String productName;
    private String mainImageUrl;

    private BigDecimal price;
    private BigDecimal discountPrice;

    private Integer stockQuantity;
    private Boolean isAvailable;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
