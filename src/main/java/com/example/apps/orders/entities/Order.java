package com.example.apps.orders.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.apps.auths.entities.User;
import com.example.apps.orders.enums.OrderStatus;
import com.example.apps.payments.enums.PaymentOptions;
import com.example.apps.payments.enums.PaymentStatus;
import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "basket_number")
    private String basketNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_option", nullable = false)
    private PaymentOptions paymentOption;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_conversation_id", unique = true)
    private String paymentConversationId;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "email_sent", nullable = false)
    private Boolean emailSent = false;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_first_name")
    private String customerFirstName;

    @Column(name = "customer_last_name")
    private String customerLastName;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "length")
    private Long length;
    @Column(name = "width")
    private Long width;
    @Column(name = "height")
    private Long height;
    @Column(name = "weight")
    private Long weight;

    // Discount fields
    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "campaign_name")
    private String campaignName;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "shipping_address_id")
    private OrderAddress shippingAddress;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "billing_address_id")
    private OrderAddress billingAddress;

    // Shipping selection fields
    @Column(name = "selected_shipping_offer_id")
    private String selectedShippingOfferId;

    @Column(name = "shipping_cost")
    private BigDecimal shippingCost;

    @Column(name = "shipping_provider")
    private String shippingProvider;

    @Column(name = "geliver_shipment_id")
    private String geliverShipmentId;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "tracking_url")
    private String trackingUrl;

    @Column(name = "label_url")
    private String labelUrl;

    @Column(name = "barcode")
    private String barcode;

    // ============= Invoice Fields =============
    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "invoice_url")
    private String invoiceUrl;

    @Column(name = "invoice_generated_at")
    private LocalDateTime invoiceGeneratedAt;

    @Column(name = "invoice_uploaded_at")
    private LocalDateTime invoiceUploadedAt;

    @Column(name = "invoice_pdf_url")
    private String invoicePdfUrl;

    // ============= Invoice Snapshot Fields (Değişmezlik için) =============
    @Column(name = "invoice_company_name")
    private String invoiceCompanyName;

    @Column(name = "invoice_company_address", columnDefinition = "TEXT")
    private String invoiceCompanyAddress;

    @Column(name = "invoice_company_tax_number")
    private String invoiceCompanyTaxNumber;

    @Column(name = "invoice_bank_info", columnDefinition = "TEXT")
    private String invoiceBankInfo;

    @Column(name = "invoice_footer_text")
    private String invoiceFooterText;

    // ------------------------------
    public void addOrderItem(OrderItem item) {
        if (this.orderItems == null) {
            this.orderItems = new ArrayList<>();
        }
        this.orderItems.add(item);
        item.setOrder(this); // İlişkinin sahibi tarafını set ediyoruz
    }
}
