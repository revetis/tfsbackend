package com.example.apps.orders.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.apps.dashboard.dtos.TopProductDTO;
import com.example.apps.orders.entities.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT new com.example.apps.dashboard.dtos.TopProductDTO(oi.productVariantId, oi.productVariantName, SUM(oi.quantity), SUM(oi.price * oi.quantity)) "
            +
            "FROM OrderItem oi GROUP BY oi.productVariantId, oi.productVariantName ORDER BY SUM(oi.quantity) DESC")
    List<TopProductDTO> findTopProducts(Pageable pageable);
}
