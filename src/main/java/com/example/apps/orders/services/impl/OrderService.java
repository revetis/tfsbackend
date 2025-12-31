package com.example.apps.orders.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.orders.dtos.OrderAddressDTO;

import com.example.apps.orders.dtos.OrderDTO;
import com.example.apps.orders.dtos.OrderDTOIU;
import com.example.apps.orders.dtos.OrderItemDTO;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.entities.OrderAddress;
import com.example.apps.auths.entities.User;
import com.example.apps.orders.entities.OrderItem;
import com.example.apps.orders.enums.OrderStatus;
import com.example.apps.orders.exceptions.OrderException;
import com.example.apps.orders.repositories.OrderRepository;
import com.example.apps.orders.services.IOrderService;
import com.example.apps.payments.enums.PaymentStatus;
import com.example.apps.products.services.IProductService;
import com.example.apps.campaigns.services.ICampaignService;
import com.example.apps.campaigns.dtos.CouponDTO;
import com.example.apps.campaigns.dtos.CampaignDTO;
import com.example.apps.products.enums.ProductSize;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderService implements IOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private IProductService productService;

    @Autowired
    @Lazy
    private com.example.apps.auths.services.IUserService userService;

    @Autowired
    private ICampaignService campaignService;

    @Autowired
    private com.example.apps.payments.repositories.PaymentRepository paymentRepository;

    @Autowired
    private com.example.apps.auths.repositories.IUserRepository userRepository;

    @Autowired
    private com.example.apps.orders.repositories.ReturnRepository returnRepository;

    @Override
    @Transactional
    public OrderDTO create(OrderDTOIU orderDTOIU) {
        log.info("Starting order creation for user ID: {}", orderDTOIU.getUserId());

        Order order = new Order();

        // Link User if ID is provided
        if (orderDTOIU.getUserId() != null) {
            try {
                User user = userService.getById(orderDTOIU.getUserId());
                order.setUser(user);
            } catch (Exception e) {
                log.warn("User with ID {} not found, proceeding as guest order.", orderDTOIU.getUserId());
            }
        } else if (orderDTOIU.getCustomerEmail() != null) {
            // Guest checkout: Fail-safe / Soft-linking
            // If email exists in system, link the order to that user silently
            userRepository.findByEmail(orderDTOIU.getCustomerEmail())
                    .ifPresent(existingUser -> {
                        order.setUser(existingUser);
                        log.info("Soft-linked guest order to existing user ID: {}", existingUser.getId());
                    });
        }

        order.setStatus(OrderStatus.PENDING);
        order.setPaymentOption(orderDTOIU.getPaymentOption());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderNumber(generateCorporateOrderNumber());
        order.setCustomerEmail(orderDTOIU.getCustomerEmail());
        order.setCustomerName(orderDTOIU.getCustomerName());
        order.setCustomerFirstName(orderDTOIU.getCustomerFirstName());
        order.setCustomerLastName(orderDTOIU.getCustomerLastName());
        order.setCustomerPhone(orderDTOIU.getCustomerPhone());
        order.setLength(orderDTOIU.getLength());
        order.setWidth(orderDTOIU.getWidth());
        order.setHeight(orderDTOIU.getHeight());
        order.setWeight(orderDTOIU.getWeight());

        OrderAddress shippingAddress = mapToAddressEntity(orderDTOIU.getShippingAddress());
        OrderAddress billingAddress = mapToAddressEntity(orderDTOIU.getBillingAddress());

        order.setShippingAddress(shippingAddress);
        order.setBillingAddress(billingAddress);

        List<OrderItemDTO> savedItemDTOs = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemDTOIU : orderDTOIU.getItems()) {
            OrderItem itemEntity = orderItemService.create(itemDTOIU, order);

            order.addOrderItem(itemEntity);

            OrderItemDTO itemDTO = new OrderItemDTO();
            BeanUtils.copyProperties(itemEntity, itemDTO);
            savedItemDTOs.add(itemDTO);

            totalAmount = totalAmount.add(itemEntity.getPrice().multiply(BigDecimal.valueOf(itemEntity.getQuantity())));
            productService.decreaseStock(itemEntity.getProductVariantId(), itemEntity.getQuantity().longValue(),
                    itemEntity.getSize());
        }

        order.setTotalAmount(totalAmount);

        // Apply coupon discount if provided
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (orderDTOIU.getCouponCode() != null && !orderDTOIU.getCouponCode().trim().isEmpty()) {
            try {
                CouponDTO coupon = campaignService.validateCoupon(
                        orderDTOIU.getCouponCode(),
                        orderDTOIU.getUserId(),
                        totalAmount);
                discountAmount = campaignService.calculateCouponDiscount(coupon, totalAmount);
                order.setCouponCode(coupon.getCode());
                order.setDiscountAmount(discountAmount);
                log.info("Coupon '{}' applied with discount: {}", coupon.getCode(), discountAmount);
            } catch (Exception e) {
                log.warn("Coupon validation failed: {}", e.getMessage());
                // Don't fail order creation, just skip coupon
            }
        }

        // If no coupon, check for best campaign
        if (discountAmount.compareTo(BigDecimal.ZERO) == 0) {
            try {
                List<Long> productIds = order.getOrderItems().stream()
                        .map(OrderItem::getProductVariantId)
                        .collect(java.util.stream.Collectors.toList());

                CampaignDTO campaign = campaignService.findBestCampaign(totalAmount, productIds);
                if (campaign != null) {
                    discountAmount = campaignService.calculateCampaignDiscount(campaign, totalAmount);
                    order.setCampaignId(campaign.getId());
                    order.setCampaignName(campaign.getName());
                    order.setDiscountAmount(discountAmount);
                    log.info("Campaign '{}' applied with discount: {}", campaign.getName(), discountAmount);
                }
            } catch (Exception e) {
                log.warn("Campaign application failed: {}", e.getMessage());
            }
        }

        // Update total amount with discount
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        // Save shipping selection fields
        if (orderDTOIU.getSelectedShippingOfferId() != null) {
            order.setSelectedShippingOfferId(orderDTOIU.getSelectedShippingOfferId());
        }
        if (orderDTOIU.getShippingProvider() != null) {
            order.setShippingProvider(orderDTOIU.getShippingProvider());
        }
        if (orderDTOIU.getShippingCost() != null) {
            order.setShippingCost(orderDTOIU.getShippingCost());
            // Add shipping cost to final amount
            finalAmount = finalAmount.add(orderDTOIU.getShippingCost());
        }
        if (orderDTOIU.getGeliverShipmentId() != null) {
            order.setGeliverShipmentId(orderDTOIU.getGeliverShipmentId());
        }

        order.setTotalAmount(finalAmount);

        orderRepository.save(order);

        OrderDTO response = new OrderDTO();
        BeanUtils.copyProperties(order, response);
        response.setItems(savedItemDTOs);

        log.info("Order created successfully with order number: {}", order.getOrderNumber());
        return response;
    }

    private OrderAddress mapToAddressEntity(OrderAddressDTO dto) {
        if (dto == null)
            return null;
        return OrderAddress.builder()
                .contactName(dto.getContactName())
                .addressLine(dto.getAddressLine())
                .city(dto.getCity())
                .cityCode(dto.getCityCode())

                .countryCode("TR")
                .districtName(dto.getDistrictName())
                .country(dto.getCountry())
                .zipCode(dto.getZipCode())
                .phoneNumber(dto.getPhoneNumber())
                .build();
    }

    private String generateCorporateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // Use 6 digits (1,000,000 combinations per day) to reduce collision risk
        int randomInt = java.util.concurrent.ThreadLocalRandom.current().nextInt(100000, 999999);
        return "TFS" + datePart + "-" + randomInt;
    }

    @Override
    public OrderDTO getById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found with ID: " + orderId));
        if (order.getUser() != null && !order.getUser().getId().equals(userId)) {
            throw new OrderException("Order not found with ID: " + orderId);
        }
        return convertToDTO(order);
    }

    @Override
    public List<OrderDTO> getByUserId(Long userId) {
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO) // Use centralized conversion method
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getAll() {
        return orderRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Autowired
    private com.example.apps.payments.services.IPaymentService paymentService;

    @Autowired
    private com.example.apps.shipments.services.IShipmentService shipmentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order to cancel not found with ID: " + orderId));

        // Security Check: If userId is provided, ensure ownership
        if (userId != null) {
            if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
                log.warn("Unauthorized cancellation attempt via IDOR. OrderId: {}, RequestUserId: {}", orderId, userId);
                throw new org.springframework.security.access.AccessDeniedException(
                        "Bu siparişi iptal etme yetkiniz yok.");
            }
        }

        // State Validation (Race Condition Prevention)
        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.info("Order {} is already cancelled.", order.getOrderNumber());
            return;
        }
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.SHIPPED
                || order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderException("Sipariş bu aşamada iptal edilemez (Kargolandı veya Tamamlandı).");
        }

        // 1. PAYMENT REFUND (Critical: Must succeed to proceed with cancellation)
        boolean stockRestoredViaPayment = false;

        // Check for successful online payment
        var paymentOpt = paymentRepository.findByOrderNumber(order.getOrderNumber());
        if (paymentOpt.isPresent()
                && paymentOpt.get().getStatus() == com.example.apps.payments.enums.PaymentStatus.SUCCESS) {

            if (paymentOpt.get().getPaymentId() == null && paymentOpt.get().getToken() == null) {
                log.warn(
                        "Payment ID and Token missing for order {} (Zombie State). Skipping refund call to avoid transaction rollback. Proceeding with cancellation.",
                        order.getOrderNumber());
                // stockRestoredViaPayment remains false, triggering manual cleanup below
            } else {
                try {
                    // Propagate exception if this fails!
                    paymentService.returnPayment(order.getOrderNumber());
                    stockRestoredViaPayment = true;
                } catch (Exception e) {
                    log.error("Payment refund failed for order {}. Aborting cancellation to ensure consistency.",
                            order.getOrderNumber(), e);
                    // Throwing runtime exception will trigger transaction rollback
                    throw new OrderException(
                            "Ödeme iadesi başarısız olduğu için iptal işlemi durduruldu. Lütfen müşteri hizmetleri ile iletişime geçin. Hata: "
                                    + e.getMessage());
                }
            }

        }

        // 2. STOCK RESTORATION (If manual / not handled effectively by payment service
        // logic)
        // Note: PaymentService.returnPayment logic usually calls rollbackStock if
        // successful.
        // We double check if we need manual intervention here only if payment didn't
        // exist or wasn't online.
        if (!stockRestoredViaPayment)

        {
            try {
                List<OrderItemDTO> items = orderItemService.getByOrderId(orderId);
                for (OrderItemDTO item : items) {
                    productService.increaseStock(item.getProductVariantId(), item.getQuantity().longValue(),
                            item.getSize());
                }
            } catch (Exception e) {
                log.warn("Could not restore stock for order {}: {}", order.getOrderNumber(), e.getMessage());
                // Non-critical if stock fails compared to money, but ideally should rollback
                // too?
                // For now sticking to warning as per original impl logic for non-payment cases
            }
        }

        // 3. CANCEL SHIPMENT
        try {
            shipmentService.cancelShipment(order.getOrderNumber());
        } catch (Exception e) {
            log.warn("Could not cancel shipment for order {}: {}", order.getOrderNumber(), e.getMessage());
            // Proceed even if shipment API fails (agent might handle later)
        }

        // 4. FINALIZE STATUS
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled successfully by User/Admin ID: {}", order.getOrderNumber(),
                userId != null ? userId : "ADMIN");
    }

    @Override
    @Transactional
    public OrderDTO returnOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order to return not found with ID: " + orderId));

        if (order.getStatus() == OrderStatus.RETURNED) {
            return convertToDTO(order);
        }

        // 1. Try to return payment if online payment exists
        boolean stockRestoredViaPayment = false;
        try {
            paymentService.returnPayment(order.getOrderNumber());
            stockRestoredViaPayment = true;
        } catch (Exception e) {
            log.info("No online payment to refund or refund failed for order {}: {}", order.getOrderNumber(),
                    e.getMessage());
        }

        // 2. Restore stock manually if not done via payment service
        if (!stockRestoredViaPayment) {
            List<OrderItemDTO> items = orderItemService.getByOrderId(orderId);
            for (OrderItemDTO item : items) {
                productService.increaseStock(item.getProductVariantId(), item.getQuantity().longValue(),
                        item.getSize());
            }
        }

        order.setStatus(OrderStatus.RETURNED);
        orderRepository.save(order);

        log.info("Order {} has been returned and stock restored.", order.getOrderNumber());
        return convertToDTO(order);
    }

    @Override
    public OrderDTO getOrderByIdAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found with ID: " + orderId));
        return convertToDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found with ID: " + orderId));
        order.setStatus(status);
        orderRepository.save(order);
        log.info("Order {} status updated to {}", order.getOrderNumber(), status);
        return convertToDTO(order);
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        // Ignore properties that have same name but different types (address, enums to
        // strings) or
        // handled manually
        BeanUtils.copyProperties(order, dto, "shippingAddress", "billingAddress", "items", "paymentStatus",
                "paymentOption");

        // Convert enums to strings manually
        if (order.getPaymentStatus() != null) {
            dto.setPaymentStatus(order.getPaymentStatus().name());
        }
        if (order.getPaymentOption() != null) {
            dto.setPaymentOption(order.getPaymentOption().name());
        }

        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
        }

        if (order.getShippingAddress() != null) {
            dto.setShippingAddress(mapToAddressDTO(order.getShippingAddress()));
        }

        if (order.getBillingAddress() != null) {
            dto.setBillingAddress(mapToAddressDTO(order.getBillingAddress()));
        }

        try {
            dto.setShipment(shipmentService.getShipmentByOrderNumber(order.getOrderNumber()));
        } catch (Exception e) {
            log.warn("Failed to fetch shipment for order {}: {}", order.getOrderNumber(), e.getMessage());
        }

        // Get payment option (gateway) from Payment entity
        try {
            paymentRepository.findByOrderNumber(order.getOrderNumber())
                    .ifPresent(payment -> dto.setPaymentOption(payment.getSelectedGateway()));
        } catch (Exception e) {
            log.warn("Failed to fetch payment option for order {}: {}", order.getOrderNumber(), e.getMessage());
        }

        dto.setItems(orderItemService.getByOrderId(order.getId()));

        // Explicitly set shipping fields
        dto.setTrackingNumber(order.getTrackingNumber());
        dto.setLabelUrl(order.getLabelUrl());
        dto.setBarcode(order.getBarcode());
        dto.setShippingProvider(order.getShippingProvider());

        // Generate correct tracking URL based on provider if stored URL is invalid
        String trackingUrl = order.getTrackingUrl();
        String barcode = order.getBarcode();
        String provider = order.getShippingProvider();

        if (barcode != null && provider != null) {
            // Generate the correct tracking URL based on provider
            String generatedUrl = switch (provider) {
                case "Sürat Kargo" -> "https://www.suratkargo.com.tr/KargoTakip/?takipno=" + barcode;
                case "Yurtiçi Kargo" ->
                    "https://www.yurticikargo.com/tr/online-servisler/gonderi-sorgula?code=" + barcode;
                case "PTT Kargo" -> "https://gonderitakip.ptt.gov.tr/Track/PttResult?un=" + barcode;
                case "DHL Ecommerce" ->
                    "https://www.dhl.com/tr-tr/home/tracking/tracking-ecommerce.html?submit=1&tracking-id=" + barcode;
                case "hepsiJET" -> "https://www.hepsijet.com/gonderi-takibi/" + barcode;
                case "Kolay Gelsin" -> "https://esube.kolaygelsin.com/shipments?trackingId=" + barcode;
                case "Paket Taxi" -> "https://takip.pakettaxi.com/?orderId=" + barcode;
                case "Aras Kargo" -> "https://www.araskargo.com.tr/kargo-takip/" + barcode;
                case "Geliver Kargo" -> order.getGeliverShipmentId() != null
                        ? "https://app.geliver.io/tracking/" + order.getGeliverShipmentId()
                        : trackingUrl;
                default -> trackingUrl;
            };
            dto.setTrackingUrl(generatedUrl);
        } else {
            dto.setTrackingUrl(trackingUrl);
        }

        // Check if active return request exists for this order
        boolean hasActiveReturn = returnRepository.existsByOrderIdAndStatusNot(
                order.getId(),
                com.example.apps.orders.enums.ReturnRequestStatus.REJECTED);
        dto.setHasActiveReturn(hasActiveReturn);

        return dto;
    }

    private OrderAddressDTO mapToAddressDTO(OrderAddress entity) {
        OrderAddressDTO dto = new OrderAddressDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public OrderDTO trackOrder(String orderNumber, String email) {
        log.info("Tracking order: {} with email: {}", orderNumber, email);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderException("Order not found with number: " + orderNumber));

        // Verify email matches (case-insensitive)
        if (!order.getCustomerEmail().equalsIgnoreCase(email)) {
            log.warn("Email mismatch for order {}: provided={}, actual={}",
                    orderNumber, email, order.getCustomerEmail());
            throw new OrderException("Email does not match order records");
        }

        log.info("Order {} successfully tracked", orderNumber);
        return convertToDTO(order);
    }

    @Override
    public OrderDTO getByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderException("Order not found with number: " + orderNumber));
        return convertToDTO(order);
    }
}
