package com.example.apps.orders.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.orders.dtos.OrderDTO;
import com.example.apps.orders.dtos.OrderDTOIU;
import com.example.apps.orders.dtos.OrderItemDTO;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.enums.OrderStatus;
import com.example.apps.orders.exceptions.OrderException;
import com.example.apps.orders.repositories.OrderRepository;
import com.example.apps.orders.services.IOrderService;
import com.example.apps.payments.enums.PaymentStatus;
import com.example.apps.products.services.IProductService;

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

    private final Random random = new Random();

    @Override
    @Transactional
    public OrderDTO create(OrderDTOIU orderDTOIU) {
        log.info("Starting order creation for user ID: {}", orderDTOIU.getUserId());

        Order order = new Order();
        order.setUserId(orderDTOIU.getUserId());
        order.setShippingAddress(orderDTOIU.getShippingAddress());
        order.setBillingAddress(orderDTOIU.getBillingAddress());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentOption(orderDTOIU.getPaymentOption());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderNumber(generateCorporateOrderNumber());

        Order savedOrder = orderRepository.save(order);

        List<OrderItemDTO> savedItemDTOs = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemDTOIU : orderDTOIU.getItems()) {
            OrderItemDTO savedItem = orderItemService.create(itemDTOIU, savedOrder);
            savedItemDTOs.add(savedItem);

            BigDecimal itemTotal = savedItem.getPrice().multiply(BigDecimal.valueOf(savedItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            productService.decreaseStock(savedItem.getProductVariantId().longValue(),
                    savedItem.getQuantity().longValue());
        }

        savedOrder.setTotalAmount(totalAmount.doubleValue());
        orderRepository.save(savedOrder);

        OrderDTO response = new OrderDTO();
        BeanUtils.copyProperties(savedOrder, response);
        response.setItems(savedItemDTOs);

        log.info("Order created successfully with order number: {}", savedOrder.getOrderNumber());
        return response;
    }

    private String generateCorporateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%04d", random.nextInt(10000));
        return datePart + "-" + randomPart;
    }

    @Override
    public OrderDTO getById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found with ID: " + orderId));
        if (!order.getUserId().equals(userId)) {
            throw new OrderException("Order not found with ID: " + orderId);
        }
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        dto.setItems(orderItemService.getByOrderId(orderId));
        return dto;
    }

    @Override
    public List<OrderDTO> getByUserId(Long userId) {
        return orderRepository.findAllByUserId(userId).stream().map(order -> {
            OrderDTO dto = new OrderDTO();
            BeanUtils.copyProperties(order, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getAll() {
        return orderRepository.findAll().stream().map(order -> {
            OrderDTO dto = new OrderDTO();
            BeanUtils.copyProperties(order, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancel(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order to cancel not found with ID: " + orderId));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new OrderException("Shipped or completed orders cannot be cancelled.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.warn("Order {} has been cancelled.", order.getOrderNumber());
    }

    @Override
    @Transactional
    public OrderDTO returnOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order to return not found with ID: " + orderId));

        order.setStatus(OrderStatus.RETURNED);
        orderRepository.save(order);

        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        return dto;
    }
}