package com.example.apps.orders.repositories;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.orders.dtos.OrderItemDTO;
import com.example.apps.orders.entities.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findBypaymentConversationId(String paymentConversationId);

    Collection<OrderItemDTO> findAllByUserId(Long userId);

}
