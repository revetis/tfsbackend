package com.example.apps.orders.services;

import java.util.List;

import com.example.apps.orders.dtos.OrderDTO;
import com.example.apps.orders.dtos.OrderDTOIU;
import com.example.apps.orders.dtos.RefundRequest;

public interface IOrderService {

    OrderDTO createOrder(Long userId, OrderDTOIU orderDTO);

    OrderDTO getOrderById(Long orderId);

    List<OrderDTO> getOrdersByUserId(Long userId);

    List<OrderDTO> getAllOrders();

    OrderDTO updateOrderStatus(Long orderId, String status);

    OrderDTO cancelOrder(Long orderId);

    OrderDTO cancelOrder(Long orderId, Long userId);

    OrderDTO refundOrder(Long orderId, RefundRequest request);
}
