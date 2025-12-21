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
    private String paymentConversationId;
    private String paymentStatus;
    private String paymentOption;
    private Long length;
    private Long width;
    private Long height;
    private Long weight;
    private Long userId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderItemDTO> items;
    private OrderAddressDTO shippingAddress;
    private OrderAddressDTO billingAddress;
    private LocalDateTime createdAt;
}
