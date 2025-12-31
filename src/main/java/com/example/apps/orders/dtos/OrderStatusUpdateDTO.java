package com.example.apps.orders.dtos;

import com.example.apps.orders.enums.OrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateDTO {

    @NotNull(message = "Order status is required")
    private OrderStatus status;
}