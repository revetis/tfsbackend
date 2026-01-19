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
public class CartCheckoutDTO implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private List<CartItemDTO> validatedItems;
    private Double subTotal;
    private Double totalDiscount;
    private Double shippingFee;
    private Double taxAmount;
    private Double finalAmount;
    private Boolean isStockAvailable;
    private String checkoutToken;
    private com.example.apps.orders.enums.AppliedDiscountType appliedDiscountType;
    private String appliedDiscountName;
}