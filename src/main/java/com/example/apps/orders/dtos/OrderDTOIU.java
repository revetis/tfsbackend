package com.example.apps.orders.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTOIU {

    private Long userId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemDTOIU> items;

    @NotBlank(message = "Shipping address is required")
    @Size(min = 10, max = 500, message = "Shipping address must be between 10 and 500 characters")
    private String shippingAddress;

    @NotBlank(message = "Billing address is required")
    @Size(min = 10, max = 500, message = "Billing address must be between 10 and 500 characters")
    private String billingAddress;
}
