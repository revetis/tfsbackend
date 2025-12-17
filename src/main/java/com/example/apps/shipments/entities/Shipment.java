package com.example.apps.shipments.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.apps.orders.entities.Order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shipments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "geliver_shipment_id")
    private String geliverShipmentId; // Geliver shipment ID

    private String barcode; // Tracking barcode

    @Column(name = "tracking_number")
    private String trackingNumber; // Carrier tracking number

    @Column(name = "tracking_url", length = 500)
    private String trackingUrl; // Carrier tracking URL

    @Column(name = "label_url", length = 500)
    private String labelUrl; // PDF label URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_status")
    private TrackingStatus trackingStatus = TrackingStatus.PRE_TRANSIT;

    @Column(name = "provider_code")
    private String providerCode; // PTT, SURAT, YURTICI, etc.

    @Column(name = "provider_service_code")
    private String providerServiceCode; // PTT_STANDART, SURAT_STANDART, etc.

    @Column(name = "shipping_cost", precision = 10, scale = 2)
    private BigDecimal shippingCost;

    private String currency = "TL";

    @Column(name = "sender_address_id")
    private String senderAddressId; // Geliver address ID

    @Column(name = "recipient_address_id")
    private String recipientAddressId; // Geliver address ID

    @Column(name = "shipment_date")
    private LocalDateTime shipmentDate;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "is_return")
    private Boolean isReturn = false;

    @Column(name = "return_reason", length = 500)
    private String returnReason;

    @Column(name = "last_error_message", length = 1000)
    private String lastErrorMessage;

    @Column(name = "has_error")
    private Boolean hasError = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ShipmentStatus {
        CREATED,
        GOT_OFFERS,
        OFFER_ACCEPTED,
        TRACKING_CODE_CREATED,
        LABEL_PRINTED,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        RETURNED,
        FAILED
    }

    public enum TrackingStatus {
        PRE_TRANSIT,
        TRANSIT,
        DELIVERED,
        RETURNED,
        FAILURE,
        UNKNOWN
    }
}
