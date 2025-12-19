package com.example.apps.wishlists.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WishlistDTOIU {
    private Long id;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;
    @NotNull(message = "User ID cannot be null")
    private Long userId;

}
