package com.example.apps.orders.services;

import java.util.List;

import com.example.apps.orders.dtos.OrderDTO;
import com.example.apps.orders.dtos.OrderDTOIU;
import com.example.apps.orders.enums.OrderStatus;

public interface IOrderService {

    OrderDTO create(OrderDTOIU orderDTOIU);

    OrderDTO getById(Long orderId, Long userId);

    List<OrderDTO> getByUserId(Long userId);

    List<OrderDTO> getAll();

    void cancel(Long orderId, Long userId);

    OrderDTO returnOrder(Long orderId);

    OrderDTO updateStatus(Long orderId, OrderStatus status);

    OrderDTO getOrderByIdAdmin(Long orderId);

    OrderDTO trackOrder(String orderNumber, String email);

    OrderDTO getByOrderNumber(String orderNumber);
}
