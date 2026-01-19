package com.example.apps.orders.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.apps.orders.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;
    private String orderNumber;
    private String paymentId;
    private String paymentConversationId;
    private String paymentStatus;
    private String paymentOption;
    private Long length;
    private Long width;
    private Long height;
    private Long weight;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal totalTaxAmount;
    private OrderStatus status;
    private List<OrderItemDTO> items;
    private OrderAddressDTO shippingAddress;
    private OrderAddressDTO billingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String customerName;
    private String customerFirstName;
    private String customerLastName;
    private String customerPhone;
    private String customerEmail;

    // Discount fields
    private String couponCode;
    private BigDecimal discountAmount;
    private Long campaignId;
    private String campaignName;

    // Extra fields
    private com.example.apps.shipments.dtos.ShipmentDTO shipment;

    // Shipping selection fields
    private String selectedShippingOfferId;
    private BigDecimal shippingCost;
    private String shippingProvider;

    private String trackingNumber;
    private String trackingUrl;
    private String labelUrl;
    private String barcode;

    // Return status flag
    private Boolean hasActiveReturn;

    // Invoice fields
    private String invoiceNumber;
    private String invoiceUrl;
    private String invoicePdfUrl;
    private LocalDateTime invoiceGeneratedAt;
}
