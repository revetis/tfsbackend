package com.example.apps.payments.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.apps.payments.entities.RefundAudit;
import com.example.apps.payments.entities.RefundAudit.RefundStatus;

public interface RefundAuditRepository extends JpaRepository<RefundAudit, Long> {

    List<RefundAudit> findByOrderId(Long orderId);

    List<RefundAudit> findByStatus(RefundStatus status);

    List<RefundAudit> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<RefundAudit> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
