package com.example.apps.orders.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.apps.dashboard.dtos.StatusDistributionDTO;
import com.example.apps.orders.entities.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findBypaymentConversationId(String paymentConversationId);

    java.util.List<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // Pageable version
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    BigDecimal sumTotalAmount();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt >= :date")
    BigDecimal sumTotalAmountByCreatedAtAfter(@Param("date") LocalDateTime date);

    long countByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT new com.example.apps.dashboard.dtos.StatusDistributionDTO(CAST(o.status AS string), COUNT(o), '') FROM Order o GROUP BY o.status")
    List<StatusDistributionDTO> findOrderStatusDistribution();

    @Query("SELECT new com.example.apps.dashboard.dtos.StatusDistributionDTO(CAST(o.paymentStatus AS string), COUNT(o), '') FROM Order o GROUP BY o.paymentStatus")
    List<StatusDistributionDTO> findPaymentStatusDistribution();

    @Query("SELECT CAST(o.createdAt AS LocalDate) as date, COUNT(o) FROM Order o WHERE o.createdAt >= :startDate GROUP BY CAST(o.createdAt AS LocalDate) ORDER BY date")
    List<Object[]> findDailyOrderCounts(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT CAST(o.createdAt AS LocalDate) as date, SUM(o.totalAmount) FROM Order o WHERE o.createdAt >= :startDate GROUP BY CAST(o.createdAt AS LocalDate) ORDER BY date")
    List<Object[]> findDailyRevenue(@Param("startDate") LocalDateTime startDate);
}
