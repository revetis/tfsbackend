package com.example.apps.payments.entities;

import java.math.BigDecimal;
import java.util.List;

import com.example.apps.payments.enums.Currency;
import com.example.apps.payments.enums.PaymentStatus;
import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseEntity {
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "token")
    private String token;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "buyer_id")
    private PaymentBuyer buyer;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "shipping_address_id")
    private PaymentAddress shippingAddress;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "billing_address_id")
    private PaymentAddress billingAddress;

    @Column(name = "basket_id")
    private String basketId;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    private List<PaymentBasketItem> basketItems;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "signature")
    private String signature;

    @Column(name = "pay_with_iyzico_page_url", length = 1024)
    private String payWithIyzicoPageUrl;

    @Column(name = "bin_number")
    private String binNumber;

    @Column(name = "card_association")
    private String cardAssociation;

    @Column(name = "card_family")
    private String cardFamily;

    @Column(name = "card_type")
    private String cardType;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "currency", nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @ElementCollection
    @CollectionTable(name = "payment_installments", joinColumns = @JoinColumn(name = "payment_id"))
    @Column(name = "installment")
    private List<Integer> installments;

    @Column(name = "paid_price")
    private BigDecimal paidPrice;

    @Column(name = "conversation_id")
    private String conversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus status;

    @Column(name = "selected_gateway")
    private String selectedGateway;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    private List<PaymentTransaction> transactions;
}
