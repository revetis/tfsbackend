package com.example.apps.shipments.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.apps.orders.entities.Order;
import com.example.apps.shipments.enums.ShipmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "geliver_shipments")
@Getter
@Setter
public class GeliverShipmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order internalOrder; // Sizin sisteminizdeki Order ile ilişki

    @Column(nullable = false)
    private String geliverShipmentId; // Geliver'den dönen ID

    private String trackingNumber; // Kargo takip numarası
    private String labelUrl; // Kargo etiketi URL'si

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status; // Dağıtımda, Teslim edildi vb.

    @Column(name = "is_test")
    private boolean testMode;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    private LocalDateTime createdAt = LocalDateTime.now();
}