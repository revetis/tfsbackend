package com.example.apps.orders.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refund_audits")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private BigDecimal refundAmount;

    private String refundReason;

    private String paymentTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status = RefundStatus.INITIATED;

    @Column(length = 2000)
    private String iyzicoResponse;

    @Column(length = 1000)
    private String errorMessage;

    @Column(nullable = false)
    private Integer retryCount = 0;

    private String initiatedBy; // User ID or "SYSTEM"

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum RefundStatus {
        INITIATED,
        SUCCESS,
        FAILED,
        RETRYING
    }
}
