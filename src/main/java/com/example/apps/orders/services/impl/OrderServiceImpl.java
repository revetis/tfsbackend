package com.example.apps.orders.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.auth.entities.User;
import com.example.apps.auth.repositories.IUserRepository;
import com.example.apps.orders.dtos.OrderDTO;
import com.example.apps.orders.dtos.OrderDTOIU;
import com.example.apps.orders.dtos.OrderItemDTO;
import com.example.apps.orders.dtos.PaymentRequest;
import com.example.apps.orders.dtos.PaymentResponse;
import com.example.apps.orders.dtos.RefundRequest;
import com.example.apps.orders.entities.Cart;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.entities.OrderItem;
import com.example.apps.orders.events.OrderEvent;
import com.example.apps.orders.exceptions.InvalidRefundAmountException;
import com.example.apps.orders.exceptions.OrderAlreadyCancelledException;
import com.example.apps.orders.exceptions.OrderAlreadyRefundedException;
import com.example.apps.orders.exceptions.OrderNotRefundableException;
import com.example.apps.orders.messaging.OrderEventProducer;
import com.example.apps.orders.repositories.CartRepository;
import com.example.apps.orders.repositories.OrderRepository;
import com.example.apps.orders.services.IOrderService;
import com.example.apps.orders.services.IPaymentService;
import com.example.apps.products.entities.Product;
import com.example.apps.products.entities.ProductVariant;
import com.example.apps.products.repositories.ProductRepository;
import com.example.apps.products.repositories.ProductVariantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final IUserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final IPaymentService paymentService;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    @Override
    public OrderDTO createOrder(Long userId, OrderDTOIU orderDTO, String ipAddress) {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Get cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart is empty for user: " + userId));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot create order from empty cart");
        }

        // Create order
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setBillingAddress(
                orderDTO.getBillingAddress() != null ? orderDTO.getBillingAddress() : orderDTO.getShippingAddress());
        order.setNotes(orderDTO.getNotes());

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Create order items and validate stock
        for (var cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + cartItem.getProductId()));

            // Stock validation - using first variant for simplicity
            // In real scenario, cart should store variant ID
            if (!product.getVariants().isEmpty()) {
                ProductVariant variant = product.getVariants().get(0);
                if (variant.getStock() < cartItem.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for product: " + product.getName());
                }
                // Decrease stock
                variant.setStock(variant.getStock() - cartItem.getQuantity());
                productVariantRepository.save(variant);
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getPrice());
            orderItem.calculateTotalPrice();

            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }

        order.setTotalAmount(totalAmount);

        // Save order first to get ID
        Order savedOrder = orderRepository.save(order);

        // Build payment request
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setCardNumber(orderDTO.getCardNumber());
        paymentRequest.setCardHolderName(orderDTO.getCardHolderName());
        paymentRequest.setExpireMonth(orderDTO.getExpireMonth());
        paymentRequest.setExpireYear(orderDTO.getExpireYear());
        paymentRequest.setCvc(orderDTO.getCvc());
        paymentRequest.setBuyerEmail(orderDTO.getBuyerEmail());
        paymentRequest.setBuyerPhone(orderDTO.getBuyerPhone());
        paymentRequest.setBuyerName(orderDTO.getBuyerName());
        paymentRequest.setBuyerSurname(orderDTO.getBuyerSurname());
        paymentRequest.setBuyerIdentityNumber(orderDTO.getBuyerIdentityNumber());
        paymentRequest.setBuyerAddress(orderDTO.getShippingAddress());
        paymentRequest.setBuyerCity(orderDTO.getBuyerCity());
        paymentRequest.setBuyerCountry(orderDTO.getBuyerCountry());
        paymentRequest.setBuyerZipCode(orderDTO.getBuyerZipCode());

        // Process payment with iyzico
        PaymentResponse paymentResponse = paymentService.processPayment(savedOrder, paymentRequest, ipAddress);

        if ("PAID".equals(paymentResponse.getStatus())) {
            savedOrder.setPaymentStatus(Order.PaymentStatus.PAID);
            savedOrder.setStatus(Order.OrderStatus.CONFIRMED);
            savedOrder.setPaymentTransactionId(paymentResponse.getPaymentId());
        } else if ("FAILED".equals(paymentResponse.getStatus())) {
            savedOrder.setPaymentStatus(Order.PaymentStatus.FAILED);
            savedOrder.setStatus(Order.OrderStatus.CANCELLED);
        } else {
            savedOrder.setPaymentStatus(Order.PaymentStatus.PENDING);
            savedOrder.setStatus(Order.OrderStatus.PENDING);
        }

        // Update order with payment info
        savedOrder = orderRepository.save(savedOrder);

        // Clear cart
        cartRepository.deleteByUserId(userId);

        // Publish event
        orderEventProducer.sendEvent(new OrderEvent(savedOrder.getId(), OrderEvent.EventType.CREATE));

        log.info("Order created: {}", savedOrder.getOrderNumber());
        return mapToDTO(savedOrder);
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return mapToDTO(order);
    }

    @Override
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        order.setStatus(Order.OrderStatus.valueOf(status));
        Order savedOrder = orderRepository.save(order);

        // Publish event
        orderEventProducer.sendEvent(new OrderEvent(savedOrder.getId(), OrderEvent.EventType.UPDATE));

        log.info("Order status updated: {} -> {}", orderId, status);
        return mapToDTO(savedOrder);
    }

    @Transactional
    @Override
    public OrderDTO cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        return performCancellation(order);
    }

    @Transactional
    @Override
    public OrderDTO cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Verify user owns the order
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied: User does not own this order");
        }

        return performCancellation(order);
    }

    @Transactional
    @Override
    public OrderDTO refundOrder(Long orderId, RefundRequest request) {
        // Validate order exists
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Validate payment status
        if (order.getPaymentStatus() != Order.PaymentStatus.PAID) {
            throw new OrderNotRefundableException(
                    "Order cannot be refunded. Payment status: " + order.getPaymentStatus());
        }

        // Validate order not already refunded
        if (order.getPaymentStatus() == Order.PaymentStatus.REFUNDED) {
            throw new OrderAlreadyRefundedException("Order has already been refunded");
        }

        // Validate refund amount
        BigDecimal refundAmount = request.getAmount();
        BigDecimal totalRefunded = order.getRefundedAmount() != null ? order.getRefundedAmount() : BigDecimal.ZERO;
        BigDecimal remainingAmount = order.getTotalAmount().subtract(totalRefunded);

        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRefundAmountException("Refund amount must be greater than 0");
        }

        if (refundAmount.compareTo(remainingAmount) > 0) {
            throw new InvalidRefundAmountException(
                    String.format("Refund amount (%.2f) exceeds remaining amount (%.2f)",
                            refundAmount.doubleValue(), remainingAmount.doubleValue()));
        }

        // Process refund with iyzico
        if (order.getPaymentTransactionId() == null) {
            throw new OrderNotRefundableException("No payment transaction ID found for this order");
        }

        PaymentResponse refundResponse = paymentService.refundPayment(
                order.getPaymentTransactionId(),
                refundAmount);

        if (!"REFUNDED".equals(refundResponse.getStatus())) {
            log.error("Refund failed for order {}: {}", orderId, refundResponse.getErrorMessage());
            throw new RuntimeException("Refund processing failed: " + refundResponse.getErrorMessage());
        }

        // Update order with refund information
        BigDecimal newRefundedAmount = totalRefunded.add(refundAmount);
        order.setRefundedAmount(newRefundedAmount);
        order.setRefundReason(request.getReason());
        order.setRefundedAt(LocalDateTime.now());

        // If fully refunded, update payment status
        if (newRefundedAmount.compareTo(order.getTotalAmount()) >= 0) {
            order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
        }

        // Update notes
        String refundNote = String.format("Refund: %.2f TRY. Reason: %s",
                refundAmount.doubleValue(),
                request.getReason() != null ? request.getReason() : "N/A");
        order.setNotes(order.getNotes() != null
                ? order.getNotes() + "\n" + refundNote
                : refundNote);

        Order savedOrder = orderRepository.save(order);

        // Publish refund event
        orderEventProducer.sendEvent(new OrderEvent(savedOrder.getId(), OrderEvent.EventType.REFUND));

        log.info("Order refunded: {} - Amount: {}", orderId, refundAmount);
        return mapToDTO(savedOrder);
    }

    private OrderDTO performCancellation(Order order) {
        // Validate order can be cancelled
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new OrderAlreadyCancelledException("Order is already cancelled");
        }

        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel delivered order");
        }

        // Restore stock for each order item with pessimistic locking
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (!product.getVariants().isEmpty()) {
                // Get first variant with pessimistic lock
                ProductVariant variant = productVariantRepository
                        .findByIdWithLock(product.getVariants().get(0).getId())
                        .orElseThrow(() -> new RuntimeException(
                                "Product variant not found: " + product.getVariants().get(0).getId()));

                // Restore stock
                variant.setStock(variant.getStock() + item.getQuantity());
                productVariantRepository.save(variant);

                log.info("Restored stock for product {}: +{} units",
                        product.getName(), item.getQuantity());
            }
        }

        // Process refund if payment was successful
        if (order.getPaymentStatus() == Order.PaymentStatus.PAID
                && order.getPaymentTransactionId() != null) {

            PaymentResponse refundResponse = paymentService.refundPayment(
                    order.getPaymentTransactionId(),
                    order.getTotalAmount());

            if ("REFUNDED".equals(refundResponse.getStatus())) {
                order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
                order.setRefundedAmount(order.getTotalAmount());
                order.setRefundedAt(LocalDateTime.now());
                order.setRefundReason("Order cancellation");
                log.info("Payment refunded for cancelled order: {}", order.getId());
            } else {
                log.warn("Refund failed for cancelled order {}: {}",
                        order.getId(), refundResponse.getErrorMessage());
                // Continue with cancellation even if refund fails
            }
        }

        // Update order status
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        // Publish cancellation event
        orderEventProducer.sendEvent(new OrderEvent(savedOrder.getId(), OrderEvent.EventType.CANCEL));

        log.info("Order cancelled: {}", order.getOrderNumber());
        return mapToDTO(savedOrder);
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD-" + timestamp + "-" + (int) (Math.random() * 1000);
    }

    private OrderDTO mapToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUser().getId());
        dto.setUsername(order.getUser().getUsername());
        dto.setItems(order.getItems().stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList()));
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setPaymentStatus(order.getPaymentStatus().name());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setBillingAddress(order.getBillingAddress());
        dto.setNotes(order.getNotes());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }

    private OrderItemDTO mapItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getTotalPrice());
        return dto;
    }
}
