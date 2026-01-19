package com.example.apps.orders.entities;

import com.example.apps.orders.enums.ReturnReason;
import com.example.apps.orders.enums.ReturnRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "return_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = true)
    private Long userId;

    @Column(nullable = true)
    private String initiator; // e.g. "GUEST:email" or "USER:123"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnRequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnReason returnReason;

    @Column(length = 1000)
    private String description; // User's detailed note

    @Column(length = 1000)
    private String adminNote;

    @Column(nullable = false)
    private BigDecimal refundAmount;

    // Shipping Info
    private String barcode; // Barcode from Geliver
    private String shippingCode; // Code from Geliver (barcode)
    private String shippingProvider; // Courier name (e.g., Surat Kargo)
    private String trackingUrl; // Tracking URL
    private String labelUrl; // PDF URL from Geliver
    private String geliverReturnShipmentId;

    private boolean restockItems; // Determined at Approval

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnRequestItem> items;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
