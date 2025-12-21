package com.example.apps.payments.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.notifications.services.IN8NService;
import com.example.apps.notifications.utils.N8NProperties;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.entities.OrderItem;
import com.example.apps.orders.enums.OrderStatus;
import com.example.apps.orders.exceptions.OrderException;
import com.example.apps.orders.repositories.OrderRepository;
import com.example.apps.payments.dtos.OrderReturnRequestDTO;
import com.example.apps.payments.dtos.PaymentRequestDTO;
import com.example.apps.payments.dtos.PaymentResponseDTO;
import com.example.apps.payments.entities.Payment;
import com.example.apps.payments.entities.PaymentAddress;
import com.example.apps.payments.entities.PaymentBasketItem;
import com.example.apps.payments.entities.PaymentBuyer;
import com.example.apps.payments.entities.PaymentTransaction;
import com.example.apps.payments.enums.PaymentStatus;
import com.example.apps.payments.exceptions.IyzicoPaymentException;
import com.example.apps.payments.exceptions.NoPaymentGatewayFoundException;
import com.example.apps.payments.exceptions.PaymentRecordNotFoundForTokenException;
import com.example.apps.payments.gateways.IGateway;
import com.example.apps.payments.gateways.utils.GatewayResult;
import com.example.apps.payments.repositories.PaymentRepository;
import com.example.apps.payments.repositories.PaymentTransactionRepository;
import com.example.apps.payments.services.IPaymentService;
import com.example.apps.products.services.IProductService;
import com.example.tfs.ApplicationProperties;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;

    private final OrderRepository orderRepository;

    private final Map<String, IGateway> gateways;

    private final IN8NService n8nService;

    private final ApplicationProperties applicationProperties;

    private final PaymentTransactionRepository paymentTransactionRepository;

    private final IProductService productService;

    private final N8NProperties n8NProperties;

    public PaymentServiceImpl(List<IGateway> gatewayList, PaymentRepository paymentRepository,
            OrderRepository orderRepository, IN8NService n8nService, ApplicationProperties applicationProperties,
            PaymentTransactionRepository paymentTransactionRepository, N8NProperties n8NProperties,
            IProductService productService) {
        this.n8nService = n8nService;
        this.productService = productService;
        this.n8NProperties = n8NProperties;
        this.applicationProperties = applicationProperties;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.gateways = gatewayList.stream()
                .collect(Collectors.toMap(IGateway::getGatewayName, g -> g));
    }

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        IGateway selectedGateway = gateways.get(request.getSelectedGateway());
        if (selectedGateway == null) {
            throw new NoPaymentGatewayFoundException("No payment gateway found for: " + request.getSelectedGateway());
        }
        log.info("Creating payment for order number: {}", request.getOrderNumber());

        Payment payment = new Payment();
        payment.setOrderNumber(request.getOrderNumber());
        payment.setTotalPrice(request.getPrice());
        payment.setPaidPrice(request.getPaidPrice());
        payment.setCurrency(request.getCurrency());
        payment.setInstallments(request.getInstallment());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setSelectedGateway(request.getSelectedGateway());

        PaymentBuyer buyer = new PaymentBuyer();
        buyer.setBuyerId(request.getBuyer().getId());
        buyer.setName(request.getBuyer().getName());
        buyer.setSurname(request.getBuyer().getSurname());
        buyer.setEmail(request.getBuyer().getEmail());
        buyer.setIdentityNumber(request.getBuyer().getIdentityNumber());
        buyer.setGsmNumber(request.getBuyer().getGsmNumber());
        buyer.setIp(request.getBuyer().getIp());
        payment.setBuyer(buyer);

        payment.setShippingAddress(mapAddress(request.getShippingAddress()));
        payment.setBillingAddress(mapAddress(request.getBillingAddress()));

        List<PaymentBasketItem> basketItems = request.getBasketItems().stream().map(itemDTO -> {
            PaymentBasketItem item = new PaymentBasketItem();
            item.setProductVariantId(itemDTO.getId());
            item.setProductName(itemDTO.getName());
            item.setPrice(itemDTO.getPrice());
            item.setQuantity(itemDTO.getQuantity());
            item.setMainCategory(itemDTO.getMainCategory());
            item.setSubCategory(itemDTO.getSubCategory());
            item.setItemType(itemDTO.getItemType());
            item.setPayment(payment);
            return item;
        }).collect(Collectors.toList());
        payment.setBasketItems(basketItems);

        PaymentResponseDTO gatewayResponse = selectedGateway.initializePayment(request);

        payment.setToken(gatewayResponse.getToken());
        payment.setConversationId(gatewayResponse.getConversationId());

        paymentRepository.save(payment);

        return gatewayResponse;
    }

    @Override
    @Transactional
    public PaymentResponseDTO completePayment(String token) {
        String transactionId = UUID.randomUUID().toString();

        Payment payment = paymentRepository.findByToken(token)
                .orElseThrow(() -> new PaymentRecordNotFoundForTokenException(token));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return mapResponse(payment);
        }

        IGateway gateway = gateways.get(payment.getSelectedGateway());

        GatewayResult result = gateway.retrievePaymentDetails(token, payment.getConversationId());

        payment.setStatus(result.status());
        payment.setPaymentId(result.paymentId());
        paymentRepository.save(payment);

        Order order = orderRepository.findByOrderNumber(payment.getOrderNumber())
                .orElseThrow(() -> new OrderException("Order not found"));

        if (result.status() == PaymentStatus.SUCCESS) {

            order.setPaymentStatus(PaymentStatus.SUCCESS);
            order.setStatus(OrderStatus.PROCESSING);

            sendPaymentSuccessMail(payment, transactionId);
            sendOrderMailOnce(order);

        } else {

            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.CANCELLED);

            rollbackStock(order);
        }

        orderRepository.save(order);

        paymentTransactionRepository.save(
                PaymentTransaction.builder()
                        .payment(payment)
                        .status(result.status().name())
                        .rawResponse(result.rawResponse())
                        .transactionId(transactionId)
                        .build());

        return mapResponse(payment);
    }

    @Override
    @Transactional
    public void returnPayment(String orderNumber) {
        log.info("Return request received for Order: {}", orderNumber);

        // 1. Ödemeyi Bul
        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new PaymentRecordNotFoundForTokenException(
                        "Payment not found for order: " + orderNumber));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Cannot return a payment that is not successful.");
        }

        IGateway gateway = gateways.get(payment.getSelectedGateway());

        String ip = payment.getBuyer() != null ? payment.getBuyer().getIp() : "85.34.78.112";
        GatewayResult result;

        boolean isSameDay = payment.getCreatedAt().toLocalDate().isEqual(LocalDate.now());

        if (isSameDay) {
            log.info("Transaction is on the same day. Executing CANCEL operation.");
            result = gateway.cancelPayment(payment.getPaymentId(), payment.getConversationId(), ip);
        } else {
            log.info("Transaction is in the past. Executing REFUND operation.");
            result = gateway.refundPayment(payment.getPaymentId(), payment.getConversationId(), ip,
                    payment.getPaidPrice());
        }

        if (result.status() == PaymentStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            Order order = orderRepository.findByOrderNumber(orderNumber).orElse(null);
            if (order != null) {
                order.setPaymentStatus(PaymentStatus.REFUNDED);
                orderRepository.save(order);
                rollbackStock(order);
            }

            log.info("Payment return process completed successfully.");
            sendOrderCancelledMail(order, ip);

        } else {
            throw new IyzicoPaymentException("Payment return failed: " + result.rawResponse());
        }
    }

    @Override
    @Transactional
    public void refundPartialPayment(OrderReturnRequestDTO returnRequest) {
        log.info("Partial return request received for Order ID: {}", returnRequest.getOrderId());

        Order order = orderRepository.findById(returnRequest.getOrderId())
                .orElseThrow(() -> new OrderException("Order not found"));

        Payment payment = paymentRepository.findByOrderNumber(order.getOrderNumber())
                .orElseThrow(() -> new PaymentRecordNotFoundForTokenException("Payment info not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only successful payments can be partially refunded.");
        }

        List<OrderItem> itemsToReturn = order.getOrderItems().stream()
                .filter(item -> returnRequest.getOrderItemIds().contains(item.getId()))
                .collect(Collectors.toList());

        if (itemsToReturn.isEmpty()) {
            throw new OrderException("No valid items found for refund.");
        }

        BigDecimal totalRefundAmount = itemsToReturn.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Total net refund amount to be sent to gateway: {}", totalRefundAmount);

        // 3. Gateway ile İletişim
        IGateway gateway = gateways.get(payment.getSelectedGateway());
        String ip = (payment.getBuyer() != null) ? payment.getBuyer().getIp() : "127.0.0.1";

        GatewayResult result = gateway.refundPayment(
                payment.getPaymentId(),
                payment.getConversationId(),
                ip,
                totalRefundAmount);

        if (result.status() == PaymentStatus.SUCCESS) {
            itemsToReturn.forEach(item -> {
                productService.increaseStock(item.getProductVariantId(), item.getQuantity().longValue());

                item.setReturnStatus(true);
            });

            BigDecimal newTotalAmount = order.getTotalAmount().subtract(totalRefundAmount);
            order.setTotalAmount(newTotalAmount);

            if (newTotalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                order.setStatus(OrderStatus.RETURNED);
                order.setPaymentStatus(PaymentStatus.REFUNDED);
            }

            orderRepository.save(order);

            paymentTransactionRepository.save(
                    PaymentTransaction.builder()
                            .payment(payment)
                            .status("PARTIAL_REFUND_SUCCESS")
                            .rawResponse("Refunded Amount: " + totalRefundAmount + " | Order ID: " + order.getId())
                            .transactionId(UUID.randomUUID().toString())
                            .build());

            log.info("Partial refund process completed successfully for Order: {}", order.getOrderNumber());
            sendRefundProcessedMail(order, totalRefundAmount, returnRequest.getReturnReason());
        } else {
            log.error("Partial refund failed: {}", result.rawResponse());
            throw new IyzicoPaymentException("Refund failed by provider: " + result.rawResponse());
        }
    }

    private void sendPaymentSuccessMail(Payment payment, String transactionId) {
        Map<String, Object> payload = new HashMap<>();

        payload.put("orderNumber", payment.getOrderNumber());
        payload.put("amount", payment.getPaidPrice());
        payload.put("transactionId", transactionId);

        n8nService.triggerWorkflow(n8NProperties.getWebhook().getPaymentSuccess(), payload);
    }

    private void sendOrderCancelledMail(Order order, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderNumber", order.getOrderNumber());
        payload.put("reason", reason); // HTML'deki {{$json.body.reason}} alanını doldurur

        n8nService.triggerWorkflow(n8NProperties.getWebhook().getOrderCancelled(), payload);
    }

    private void sendRefundProcessedMail(Order order, BigDecimal refundAmount, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderNumber", order.getOrderNumber());
        payload.put("refundAmount", refundAmount); // HTML'deki {{$json.body.refundAmount}}
        payload.put("refundReason", reason); // {{#if}} bloğu için

        n8nService.triggerWorkflow(n8NProperties.getWebhook().getRefundProcessed(), payload);
    }

    private PaymentAddress mapAddress(com.example.apps.payments.dtos.AddressDTO dto) {
        PaymentAddress address = new PaymentAddress();
        address.setContactName(dto.getContactName());
        address.setCity(dto.getCity());
        address.setCountry(dto.getCountry());
        address.setAddressLine(dto.getAddressLine());
        address.setZipCode(dto.getZipCode());
        address.setAddressType(dto.getAddressType());
        return address;
    }

    private PaymentResponseDTO mapResponse(Payment payment) {
        return PaymentResponseDTO.builder()
                .orderNumber(payment.getOrderNumber())
                .token(payment.getToken())
                .conversationId(payment.getConversationId())
                .paymentStatus(payment.getStatus().name())
                .totalPrice(payment.getTotalPrice())
                .paidPrice(payment.getPaidPrice())
                .currency(payment.getCurrency().name())
                .build();
    }

    private void rollbackStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            productService.increaseStock(
                    item.getProductVariantId(),
                    item.getQuantity().longValue());
        }
    }

    private void sendOrderMailOnce(Order order) {

        if (order.getEmailSent()) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("subject", "Siparişiniz onaylandı");
        payload.put("preHeader", "Siparişiniz onaylandı");
        payload.put("faviconURL",
                applicationProperties.getFRONTEND_URL() + "favicon.ico");
        payload.put("firstName", order.getCustomerName());
        payload.put("orderNumber", order.getOrderNumber());
        payload.put("orderDate", order.getCreatedAt());
        payload.put("totalAmount", order.getTotalAmount());
        payload.put("shippingAddress", order.getShippingAddress());
        payload.put("orderDetailUrl",
                applicationProperties.getFRONTEND_URL()
                        + "order-detail?orderNumber=" + order.getOrderNumber());

        n8nService.triggerWorkflow(
                n8NProperties.getWebhook().getOrderConfirmation(),
                payload);

        order.setEmailSent(true);
    }

}