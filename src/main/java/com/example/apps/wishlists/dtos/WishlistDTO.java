package com.example.apps.wishlists.dtos;

import java.util.List;

import com.example.apps.products.dtos.ProductDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WishlistDTO {
    private Long id;
    private Long userId;
    private List<ProductDTO> products;

}
