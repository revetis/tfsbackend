package com.example.apps.orders.repositories;

import com.example.apps.orders.entities.ReturnRequest;
import com.example.apps.orders.enums.ReturnRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRepository extends JpaRepository<ReturnRequest, Long> {

    List<ReturnRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ReturnRequest> findByOrderId(Long orderId);

    boolean existsByOrderIdAndStatusNot(Long orderId, ReturnRequestStatus status);

    List<ReturnRequest> findByStatus(ReturnRequestStatus status);
}
