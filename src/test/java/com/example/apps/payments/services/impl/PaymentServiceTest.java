package com.example.apps.payments.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.apps.notifications.services.IN8NService;
import com.example.apps.notifications.utils.N8NProperties;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.repositories.OrderRepository;
import com.example.apps.payments.dtos.AddressDTO;
import com.example.apps.payments.dtos.BuyerDTO;
import com.example.apps.payments.dtos.PaymentRequestDTO;
import com.example.apps.payments.dtos.PaymentResponseDTO;
import com.example.apps.payments.entities.Payment;
import com.example.apps.payments.enums.Currency;
import com.example.apps.payments.enums.PaymentStatus;
import com.example.apps.payments.gateways.IGateway;
import com.example.apps.payments.gateways.utils.GatewayResult;
import com.example.apps.payments.repositories.PaymentRepository;
import com.example.apps.payments.repositories.PaymentTransactionRepository;
import com.example.apps.products.services.IProductService;
import com.example.apps.shipments.services.IShipmentService;
import com.example.apps.carts.services.ICartService;
import com.example.tfs.ApplicationProperties;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private IN8NService n8nService;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private IProductService productService;

    @Mock
    private N8NProperties n8NProperties;

    @Mock
    private IGateway mockGateway;

    @Mock
    private IShipmentService shipmentService;

    @Mock
    private ICartService cartService;

    @Mock
    private com.example.apps.invoices.services.IInvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        when(mockGateway.getGatewayName()).thenReturn("iyzico");

        List<IGateway> gateways = Collections.singletonList(mockGateway);
        paymentService = new PaymentServiceImpl(
                gateways,
                paymentRepository,
                orderRepository,
                n8nService,
                applicationProperties,
                paymentTransactionRepository,
                n8NProperties,
                productService,
                shipmentService,
                cartService,
                invoiceService);
    }

    @Test
    void createPayment_Success() {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setOrderNumber("ORD-123");
        request.setPrice(new BigDecimal("100.00"));
        request.setPaidPrice(new BigDecimal("100.00"));
        request.setSelectedGateway("iyzico");
        BuyerDTO buyer = new BuyerDTO();
        buyer.setId("BYR-123");
        buyer.setName("John");
        buyer.setSurname("Doe");
        buyer.setEmail("john.doe@example.com");
        buyer.setIdentityNumber("12345678901");
        buyer.setGsmNumber("+905555555555");
        buyer.setIp("127.0.0.1");
        request.setBuyer(buyer);

        AddressDTO address = new AddressDTO();
        address.setContactName("John Doe");
        address.setCity("Istanbul");
        address.setCountry("Turkey");
        address.setAddressLine("Test Street 123");
        address.setZipCode("34000");
        request.setShippingAddress(address);
        request.setBillingAddress(address);

        request.setBasketItems(new ArrayList<>());

        PaymentResponseDTO gatewayResponse = PaymentResponseDTO.builder()
                .token("test-token")
                .conversationId("test-conv-id")
                .build();

        when(mockGateway.initializePayment(any())).thenReturn(gatewayResponse);

        PaymentResponseDTO result = paymentService.createPayment(request);

        assertNotNull(result);
        assertEquals("test-token", result.getToken());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void completePayment_Success() {
        Payment payment = new Payment();
        payment.setToken("test-token");
        payment.setOrderNumber("ORD-123");
        payment.setSelectedGateway("iyzico");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaidPrice(new BigDecimal("100.00"));
        payment.setCurrency(Currency.TRY);

        Order order = new Order();
        order.setOrderNumber("ORD-123");
        order.setEmailSent(false);

        when(paymentRepository.findByToken("test-token")).thenReturn(Optional.of(payment));
        when(mockGateway.retrievePaymentDetails(anyString(), any())).thenReturn(
                new GatewayResult(PaymentStatus.SUCCESS, "pay-id", "raw-resp"));
        when(orderRepository.findByOrderNumber("ORD-123")).thenReturn(Optional.of(order));
        // Mocking N8N configs to avoid NPE
        N8NProperties.Webhook mockWebhook = mock(N8NProperties.Webhook.class);
        when(n8NProperties.getWebhook()).thenReturn(mockWebhook);
        when(mockWebhook.getPaymentSuccess()).thenReturn("url");
        when(mockWebhook.getOrderConfirmation()).thenReturn("url");

        PaymentResponseDTO result = paymentService.completePayment("test-token");

        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCESS.name(), result.getPaymentStatus());
        verify(paymentRepository).save(payment);
        verify(orderRepository).save(order);
    }

    @Test
    void returnPayment_SameDayCancel_Success() {
        Payment payment = new Payment();
        payment.setOrderNumber("ORD-123");
        payment.setPaymentId("pay-id");
        payment.setSelectedGateway("iyzico");
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidPrice(new BigDecimal("100.00"));
        payment.setCreatedAt(LocalDateTime.now()); // Same day

        Order order = new Order();
        order.setOrderNumber("ORD-123");
        order.setOrderItems(new ArrayList<>());

        when(paymentRepository.findByOrderNumber("ORD-123")).thenReturn(Optional.of(payment));
        when(orderRepository.findByOrderNumber("ORD-123")).thenReturn(Optional.of(order));
        when(mockGateway.cancelPayment(anyString(), any(), anyString())).thenReturn(
                new GatewayResult(PaymentStatus.SUCCESS, "pay-id", "cancelled"));

        // Mock N8N webhook
        N8NProperties.Webhook mockWebhook = mock(N8NProperties.Webhook.class);
        when(n8NProperties.getWebhook()).thenReturn(mockWebhook);
        when(mockWebhook.getOrderCancelled()).thenReturn("url");

        paymentService.returnPayment("ORD-123");

        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        verify(paymentRepository).save(payment);
        verify(mockGateway).cancelPayment(anyString(), any(), anyString());
    }
}
