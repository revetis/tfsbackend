package com.example.apps.orders.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.apps.orders.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;
    private String orderNumber;
    private Long userId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderItemDTO> items;
    private String shippingAddress;
    private String billingAddress;
    private LocalDateTime createdAt;
}
