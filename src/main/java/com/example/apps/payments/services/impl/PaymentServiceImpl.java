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
import org.springframework.scheduling.annotation.Async;

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
import com.example.apps.payments.dtos.PaymentAdminDTO;
import com.example.apps.products.services.IProductService;
import com.example.apps.products.enums.ProductSize;
import com.example.apps.carts.services.ICartService;
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

    private final com.example.apps.shipments.services.IShipmentService shipmentService;

    private final ICartService cartService;

    public PaymentServiceImpl(List<IGateway> gatewayList, PaymentRepository paymentRepository,
            OrderRepository orderRepository, IN8NService n8nService, ApplicationProperties applicationProperties,
            PaymentTransactionRepository paymentTransactionRepository, N8NProperties n8NProperties,
            IProductService productService, com.example.apps.shipments.services.IShipmentService shipmentService,
            ICartService cartService) {
        this.n8nService = n8nService;
        this.productService = productService;
        this.n8NProperties = n8NProperties;
        this.applicationProperties = applicationProperties;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.shipmentService = shipmentService;
        this.cartService = cartService;
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

        // Save card details
        payment.setBinNumber(result.binNumber());
        payment.setCardAssociation(result.cardAssociation());
        payment.setCardFamily(result.cardFamily());
        payment.setCardType(result.cardType());

        paymentRepository.save(payment);

        Order order = orderRepository.findByOrderNumber(payment.getOrderNumber())
                .orElseThrow(() -> new OrderException("Order not found"));

        if (result.status() == PaymentStatus.SUCCESS) {

            order.setPaymentStatus(PaymentStatus.SUCCESS);
            order.setStatus(OrderStatus.PROCESSING);

            // Auto-purchase logic moved to explicit frontend call
            // purchaseShipmentOffer(order);

            // Clear cart if user is authenticated
            if (order.getUser() != null) {
                try {
                    cartService.clearCart(order.getUser().getId(), order.getUser().getId());
                    log.info("Cart cleared for user ID: {}", order.getUser().getId());
                } catch (Exception e) {
                    log.warn("Failed to clear cart for user {}: {}", order.getUser().getId(), e.getMessage());
                }
            }

            sendPaymentSuccessMail(payment, transactionId);
            sendOrderMailOnce(order);

        } else {

            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.CANCELLED);

            rollbackStock(order);
        }

        orderRepository.saveAndFlush(order);

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

        if (payment.getPaymentId() == null) {
            log.warn("Payment ID is missing for Order: {}. Attempting to recover from gateway...", orderNumber);
            if (payment.getToken() != null) {
                try {
                    IGateway recoveryGateway = gateways.get(payment.getSelectedGateway());
                    GatewayResult recoveryResult = recoveryGateway.retrievePaymentDetails(payment.getToken(),
                            payment.getConversationId());
                    if (recoveryResult.paymentId() != null) {
                        payment.setPaymentId(recoveryResult.paymentId());
                        payment.setStatus(recoveryResult.status()); // Sync status too
                        paymentRepository.save(payment);
                        log.info("Payment ID recovered successfully: {}", recoveryResult.paymentId());
                    } else {
                        log.error("Recovery failed: Gateway did not return a Payment ID.");
                        throw new IllegalStateException("Payment ID missing and recovery failed.");
                    }
                } catch (Exception e) {
                    log.error("Recovery attempt failed for Order: {}", orderNumber, e);
                    throw new IllegalStateException(
                            "Payment ID missing and recovery threw exception: " + e.getMessage());
                }
            } else {
                log.error("Payment ID and Token are both missing. Cannot recover.");
                throw new IllegalStateException("Payment ID and Token missing. Cannot refund.");
            }
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
                sendOrderCancelledMail(order, ip);
            }

            log.info("Payment return process completed successfully.");

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
                if (returnRequest.isRestoreStock()) {
                    productService.increaseStock(item.getProductVariantId(), item.getQuantity().longValue(),
                            item.getSize());
                }

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
                    item.getQuantity().longValue(),
                    item.getSize());
        }
    }

    /**
     * Purchases the selected shipping offer after successful payment.
     * Creates shipment with the carrier via Geliver API.
     */
    private void purchaseShipmentOffer(Order order) {
        if (order.getSelectedShippingOfferId() == null || order.getShippingProvider() == null) {
            log.info("No shipping offer selected for order {}, skipping shipment creation", order.getOrderNumber());
            return;
        }

        try {
            var transactionRequest = new com.example.apps.shipments.dtos.GeliverTransactionCreateRequest();
            transactionRequest.setProviderServiceCode(order.getShippingProvider());
            transactionRequest.setOfferId(order.getSelectedShippingOfferId());

            var address = order.getShippingAddress();

            // Build recipient address
            var recipientAddress = com.example.apps.shipments.dtos.GeliverRecipientAddressRequest.builder()
                    .name(address.getContactName())
                    .email(order.getCustomerEmail())
                    .phone(cleanPhoneNumber(address.getPhoneNumber()))
                    .address1(address.getAddressLine())
                    .countryCode("TR")
                    .cityCode(address.getCityCode())
                    .zipCode(address.getZipCode())
                    .districtName(address.getDistrictName())
                    .build();

            // Build items list
            var items = order.getOrderItems().stream()
                    .map(item -> com.example.apps.shipments.dtos.GeliverItemRequest.builder()
                            .title(item.getProductVariantName())
                            .quantity(item.getQuantity())
                            .build())
                    .collect(java.util.stream.Collectors.toList());

            // Build order info
            var orderRequest = new com.example.apps.shipments.dtos.GeliverTransactionOrderRequest();
            orderRequest.setOrderNumber(order.getOrderNumber());
            orderRequest.setTotalAmount(order.getTotalAmount());
            orderRequest.setSourceCode("API");

            // Build shipment request
            var shipmentRequest = new com.example.apps.shipments.dtos.GeliverTransactionShipmentRequest();
            shipmentRequest.setRecipientAddress(recipientAddress);
            shipmentRequest.setLength(String.valueOf(order.getLength() != null ? order.getLength() : 10));
            shipmentRequest.setWidth(String.valueOf(order.getWidth() != null ? order.getWidth() : 10));
            shipmentRequest.setHeight(String.valueOf(order.getHeight() != null ? order.getHeight() : 10));
            shipmentRequest.setWeight(String.valueOf(order.getWeight() != null ? order.getWeight() : 1));
            shipmentRequest.setItems(items);
            shipmentRequest.setOrder(orderRequest);
            shipmentRequest.setTest(false); // Production mode

            transactionRequest.setShipment(shipmentRequest);

            // If we have an existing Geliver Shipment ID from the quote phase, use it
            if (order.getGeliverShipmentId() != null) {
                transactionRequest.setShipmentId(order.getGeliverShipmentId());
                log.info("Using existing Geliver Shipment ID: {} for auto-purchase", order.getGeliverShipmentId());
            }

            // Execute purchase
            var result = shipmentService.offerPurchase(transactionRequest);

            if (result != null && result.getData() != null && result.getData().getShipment() != null) {
                var shipmentDetail = result.getData().getShipment();
                order.setTrackingNumber(shipmentDetail.getTrackingNumber());
                order.setLabelUrl(shipmentDetail.getLabelUrl());
                order.setBarcode(shipmentDetail.getBarcode());

                // Map provider code to friendly name and tracking URL
                String providerCode = shipmentDetail.getProviderServiceCode();
                String trackingCode = shipmentDetail.getBarcode(); // Use barcode for tracking

                if (providerCode != null) {
                    String friendlyName = switch (providerCode) {
                        case "SURAT_STANDART" -> "Sürat Kargo";
                        case "YURTICI_STANDART" -> "Yurtiçi Kargo";
                        case "PTT_STANDART", "PTT_KAPIDA_ODEME" -> "PTT Kargo";
                        case "DHL_STANDART" -> "DHL Ecommerce";
                        case "HEPSIJET_STANDART" -> "hepsiJET";
                        case "KOLAYGELSIN_STANDART" -> "Kolay Gelsin";
                        case "PAKETTAXI_STANDART" -> "Paket Taxi";
                        case "ARAS_STANDART" -> "Aras Kargo";
                        case "GELIVER_STANDART" -> "Geliver Kargo";
                        default -> providerCode;
                    };
                    order.setShippingProvider(friendlyName);

                    // Generate tracking URL based on provider
                    if (trackingCode != null) {
                        String shipmentId = shipmentDetail.getId(); // For Geliver tracking
                        String trackingUrl = switch (providerCode) {
                            case "SURAT_STANDART" ->
                                "https://www.suratkargo.com.tr/KargoTakip/?takipno=" + trackingCode;
                            case "YURTICI_STANDART" ->
                                "https://www.yurticikargo.com/tr/online-servisler/gonderi-sorgula?code=" + trackingCode;
                            case "PTT_STANDART", "PTT_KAPIDA_ODEME" ->
                                "https://gonderitakip.ptt.gov.tr/Track/PttResult?un=" + trackingCode;
                            case "DHL_STANDART" ->
                                "https://www.dhl.com/tr-tr/home/tracking/tracking-ecommerce.html?submit=1&tracking-id="
                                        + trackingCode;
                            case "HEPSIJET_STANDART" -> "https://www.hepsijet.com/gonderi-takibi/" + trackingCode;
                            case "KOLAYGELSIN_STANDART" ->
                                "https://esube.kolaygelsin.com/shipments?trackingId=" + trackingCode;
                            case "PAKETTAXI_STANDART" -> "https://takip.pakettaxi.com/?orderId=" + trackingCode;
                            case "ARAS_STANDART" -> "https://www.araskargo.com.tr/kargo-takip/" + trackingCode;
                            case "GELIVER_STANDART" -> "https://app.geliver.io/tracking/" + shipmentId;
                            default -> shipmentDetail.getTrackingUrl(); // Use Geliver's URL as fallback
                        };
                        order.setTrackingUrl(trackingUrl);
                    }
                }

                // If the shipment ID changed or was initially null, update it
                if (shipmentDetail.getId() != null) {
                    order.setGeliverShipmentId(shipmentDetail.getId());
                }

                orderRepository.save(order);
                log.info("Shipment metadata saved for order {}. Barcode: {}, Tracking: {}, Provider: {}",
                        order.getOrderNumber(), shipmentDetail.getBarcode(), order.getTrackingUrl(),
                        order.getShippingProvider());
            }

            log.info("Shipment created successfully for order {}: {}", order.getOrderNumber(), result);
        } catch (Exception e) {
            log.error("Failed to create shipment for order {}: {}", order.getOrderNumber(), e.getMessage());
            throw new RuntimeException("Shipment purchase failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    @Async
    public void purchaseShipmentForOrder(String orderNumber) {
        log.info("Manual shipment purchase requested for order: {}", orderNumber);

        // Check if shipment already exists to prevent duplicates on page refresh
        var existingShipment = shipmentService.getShipmentByOrderNumber(orderNumber);
        if (existingShipment != null) {
            log.info("Shipment already exists for order: {}. Skipping purchase.", orderNumber);
            return;
        }

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderException("Order not found: " + orderNumber));

        purchaseShipmentOffer(order);
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

    @Override
    public List<PaymentAdminDTO> getAllPayments() {
        return paymentRepository
                .findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt"))
                .stream()

                .map(payment -> PaymentAdminDTO.builder()
                        .id(payment.getId())
                        .orderNumber(payment.getOrderNumber())
                        .token(payment.getToken())
                        .paymentId(payment.getPaymentId())
                        .totalPrice(payment.getTotalPrice())
                        .paidPrice(payment.getPaidPrice())
                        .currency(payment.getCurrency())
                        .status(payment.getStatus())
                        .selectedGateway(payment.getSelectedGateway())
                        .createdAt(payment.getCreatedAt())
                        .updatedAt(payment.getUpdatedAt())
                        .customerEmail(payment.getBuyer() != null ? payment.getBuyer().getEmail() : null)
                        .customerName(payment.getBuyer() != null
                                ? payment.getBuyer().getName() + " " + payment.getBuyer().getSurname()
                                : null)
                        .binNumber(payment.getBinNumber())
                        .cardAssociation(payment.getCardAssociation())
                        .cardFamily(payment.getCardFamily())
                        .cardType(payment.getCardType())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public PaymentAdminDTO getPaymentById(Long id) {
        com.example.apps.payments.entities.Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return PaymentAdminDTO.builder()
                .id(payment.getId())
                .orderNumber(payment.getOrderNumber())
                .token(payment.getToken())
                .paymentId(payment.getPaymentId())
                .totalPrice(payment.getTotalPrice())
                .paidPrice(payment.getPaidPrice())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .selectedGateway(payment.getSelectedGateway())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .customerEmail(payment.getBuyer() != null ? payment.getBuyer().getEmail() : null)
                .customerName(payment.getBuyer() != null
                        ? payment.getBuyer().getName() + " " + payment.getBuyer().getSurname()
                        : null)
                .binNumber(payment.getBinNumber())
                .cardAssociation(payment.getCardAssociation())
                .cardFamily(payment.getCardFamily())
                .cardType(payment.getCardType())
                .build();
    }

    /**
     * Cleans phone number for Geliver (5554443322 format)
     */
    private String cleanPhoneNumber(String phone) {
        if (phone == null)
            return null;
        // Strip everything except digits
        String cleaned = phone.replaceAll("\\D", "");
        // If it starts with 0, remove it
        if (cleaned.startsWith("0")) {
            cleaned = cleaned.substring(1);
        }
        // If it starts with 90, it might be 905..., remove 90
        if (cleaned.startsWith("90") && cleaned.length() > 10) {
            cleaned = cleaned.substring(2);
        }
        return cleaned;
    }
}
