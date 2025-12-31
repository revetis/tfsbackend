package com.example.apps.shipments.entities;

import java.math.BigDecimal;

import com.example.apps.shipments.enums.ShipmentStatus;
import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "geliver_shipments")
@Getter
@Setter
@NoArgsConstructor
public class GeliverShipmentEntity extends BaseEntity {

    @Column(name = "order_number")
    private String orderNumber; // Sizin sisteminizdeki Order ile ilişki

    @Column(nullable = false)
    private String geliverShipmentId; // Geliver'den dönen ID

    private String trackingNumber; // Kargo takip numarası
    private String trackingUrl; // Kargo takip URL'si
    private String labelUrl; // Kargo etiketi URL'si

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status; // Dağıtımda, Teslim edildi vb.

    @Column(name = "is_test")
    private boolean testMode;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

}