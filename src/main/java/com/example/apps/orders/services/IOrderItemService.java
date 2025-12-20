package com.example.apps.orders.services;

import java.util.List;

import com.example.apps.orders.dtos.OrderItemDTO;
import com.example.apps.orders.dtos.OrderItemDTOIU;
import com.example.apps.orders.entities.Order;

public interface IOrderItemService {
    OrderItemDTO create(OrderItemDTOIU orderItemDTOIU, Order order);

    OrderItemDTO getById(Long orderItemId);

    List<OrderItemDTO> getByOrderId(Long orderId);

    List<OrderItemDTO> getAll();

    void delete(Long orderItemId);

}
