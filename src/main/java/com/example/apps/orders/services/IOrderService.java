package com.example.apps.orders.services;

import java.util.List;

import com.example.apps.orders.dtos.OrderDTO;
import com.example.apps.orders.dtos.OrderDTOIU;

public interface IOrderService {

    OrderDTO create(OrderDTOIU orderDTOIU);

    OrderDTO getById(Long orderId, Long userId);

    List<OrderDTO> getByUserId(Long userId);

    List<OrderDTO> getAll();

    void cancel(Long orderId);

    OrderDTO returnOrder(Long orderId);
}
