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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.notifications.services.IN8NService;
import com.example.apps.notifications.utils.N8NProperties;
import com.example.apps.orders.dtos.OrderAddressDTO;
import com.example.apps.orders.dtos.OrderDTO;
import com.example.apps.orders.dtos.OrderDTOIU;
import com.example.apps.orders.dtos.OrderItemDTO;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.entities.OrderAddress;
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

    @Autowired
    private IN8NService n8nService;

    @Autowired
    private N8NProperties n8NProperties;

    @Override
    @Transactional
    public OrderDTO create(OrderDTOIU orderDTOIU) {
        log.info("Starting order creation for user ID: {}", orderDTOIU.getUserId());

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentOption(orderDTOIU.getPaymentOption());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderNumber(generateCorporateOrderNumber());
        order.setCustomerEmail(orderDTOIU.getCustomerEmail());
        order.setCustomerName(orderDTOIU.getCustomerName());
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
            OrderItemDTO savedItem = orderItemService.create(itemDTOIU, order);
            savedItemDTOs.add(savedItem);

            BigDecimal itemTotal = savedItem.getPrice().multiply(BigDecimal.valueOf(savedItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            productService.decreaseStock(savedItem.getProductVariantId().longValue(),
                    savedItem.getQuantity().longValue());
        }

        order.setTotalAmount(totalAmount);
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
        int randomInt = Math.abs(UUID.randomUUID().hashCode() % 10000);
        String randomPart = String.format("%04d", randomInt);
        return "TFS" + datePart + "-" + randomPart;
    }

    @Override
    public OrderDTO getById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found with ID: " + orderId));
        if (!order.getUser().getId().equals(userId)) {
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