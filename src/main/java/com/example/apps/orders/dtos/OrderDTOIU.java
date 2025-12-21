package com.example.apps.orders.dtos;

import java.util.List;

import com.example.apps.payments.enums.PaymentOptions;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Payment status is required")
    private PaymentOptions paymentOption;

    @NotNull(message = "Shipping address is required")
    @Valid
    private OrderAddressDTO shippingAddress;

    @NotNull(message = "Billing address is required")
    @Valid
    private OrderAddressDTO billingAddress;
    @NotBlank(message = "Customer email is required")

    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotNull(message = "Length is required")
    private Long length;
    @NotNull(message = "Width is required")
    private Long width;
    @NotNull(message = "Height is required")
    private Long height;
    @NotNull(message = "Weight is required")
    private Long weight;

}
