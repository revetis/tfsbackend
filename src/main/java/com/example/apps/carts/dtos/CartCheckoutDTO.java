package com.example.apps.carts.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartCheckoutDTO {
    private List<CartItemDTO> validatedItems;
    private Double subTotal;
    private Double totalDiscount;
    private Double shippingFee;
    private Double taxAmount;
    private Double finalAmount;
    private boolean isStockAvailable;
    private String checkoutToken;
}