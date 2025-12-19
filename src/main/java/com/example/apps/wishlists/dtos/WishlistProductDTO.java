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

    private String productName;
    private String mainImageUrl;

    private BigDecimal price;
    private BigDecimal discountPrice;

    private Integer stockQuantity;
    private Boolean isAvailable;

    private String addedAt;

}
