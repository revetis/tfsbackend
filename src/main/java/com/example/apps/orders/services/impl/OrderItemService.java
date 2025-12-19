package com.example.apps.orders.services.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.orders.dtos.OrderItemDTO;
import com.example.apps.orders.dtos.OrderItemDTOIU;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.entities.OrderItem;
import com.example.apps.orders.exceptions.OrderItemException;
import com.example.apps.orders.repositories.OrderItemRepository;
import com.example.apps.orders.services.IOrderItemService;
import com.example.apps.products.entities.ProductVariant;
import com.example.apps.products.exceptions.ProductVariantException;
import com.example.apps.products.repositories.ProductVariantRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderItemService implements IOrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public OrderItemDTO create(OrderItemDTOIU orderItemDTOIU, Order order) {

        ProductVariant productVariant = productVariantRepository.findById(orderItemDTOIU.getProductVariantId())
                .orElseThrow(() -> new ProductVariantException("Product Variant not found for OrderItem"));
        if (productVariant.getStock().getQuantity() < orderItemDTOIU.getQuantity()) {
            throw new ProductVariantException("Not enough stock for OrderItem");
        }

        OrderItem newOrderItem = new OrderItem();
        newOrderItem.setProductVariantId(productVariant.getId());
        newOrderItem.setOrder(order);
        newOrderItem.setProductVariantName(productVariant.getName());
        newOrderItem.setQuantity(orderItemDTOIU.getQuantity());
        newOrderItem.setPrice(productVariant.getPrice());
        orderItemRepository.save(newOrderItem);

        OrderItemDTO response = new OrderItemDTO();
        BeanUtils.copyProperties(newOrderItem, response);
        return response;

    }

    @Override
    public OrderItemDTO getById(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new OrderItemException("Order Item not found"));
        OrderItemDTO response = new OrderItemDTO();
        BeanUtils.copyProperties(orderItem, response);
        return response;

    }

    @Override
    public List<OrderItemDTO> getByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        if (orderItems.isEmpty()) {
            throw new OrderItemException("Order Item not found");
        }

        return orderItems.stream().map(orderItem -> {
            OrderItemDTO orderItemDTO = new OrderItemDTO();
            BeanUtils.copyProperties(orderItem, orderItemDTO);
            return orderItemDTO;
        }).toList();

    }

    @Override
    public List<OrderItemDTO> getAll() {
        List<OrderItem> orderItems = orderItemRepository.findAll();
        return orderItems.stream().map(orderItem -> {
            OrderItemDTO orderItemDTO = new OrderItemDTO();
            BeanUtils.copyProperties(orderItem, orderItemDTO);
            return orderItemDTO;
        }).toList();

    }

    @Override
    @Transactional
    public void delete(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new OrderItemException("Order Item not found"));
        orderItemRepository.delete(orderItem);

    }

}
