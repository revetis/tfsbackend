package com.example.apps.wishlists.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WishListDTOIU {
    @NotNull
    @Min(1)
    private Long userId;

    @NotNull
    @Min(1)
    private Long productId;
}
