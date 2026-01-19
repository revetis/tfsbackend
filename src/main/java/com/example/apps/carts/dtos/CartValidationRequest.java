package com.example.apps.carts.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartValidationRequest {
    @NotNull
    @Valid
    private List<CartItemDTOIU> items;

    private Double shippingCost;
    private String couponCode;
}
